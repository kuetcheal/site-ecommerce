package com.kuetche.siteecommerce.security;

import com.kuetche.siteecommerce.entity.Client;
import com.kuetche.siteecommerce.enums.Role;
import com.kuetche.siteecommerce.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private ClientRepository clientRepository;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(clientRepository);

        ReflectionTestUtils.setField(
                securityConfig,
                "jwtSecret",
                "maCleJwtTresLongueEtTresSecretePourSiteEcommerce2026"
        );
    }

    @Test
    void passwordEncoder_doitEncoderEtVerifierMotDePasse() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        String encodedPassword = passwordEncoder.encode("password123");

        assertThat(encodedPassword).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrong-password", encodedPassword)).isFalse();
    }

    @Test
    void userDetailsService_doitChargerClientParEmail() {
        Client client = Client.builder()
                .id(1L)
                .nom("Kuetche")
                .prenom("Alex")
                .email("alex@test.com")
                .motDePasse("encoded-password")
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(clientRepository.findByEmail("alex@test.com"))
                .thenReturn(Optional.of(client));

        UserDetailsService userDetailsService = securityConfig.userDetailsService();

        UserDetails userDetails = userDetailsService.loadUserByUsername("alex@test.com");

        assertThat(userDetails.getUsername()).isEqualTo("alex@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("encoded-password");
        assertThat(userDetails.isEnabled()).isTrue();

        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CLIENT");

        verify(clientRepository, times(1)).findByEmail("alex@test.com");
    }

    @Test
    void userDetailsService_doitDesactiverUtilisateurSiClientInactif() {
        Client client = Client.builder()
                .id(1L)
                .email("inactive@test.com")
                .motDePasse("encoded-password")
                .role(Role.CLIENT)
                .active(false)
                .build();

        when(clientRepository.findByEmail("inactive@test.com"))
                .thenReturn(Optional.of(client));

        UserDetailsService userDetailsService = securityConfig.userDetailsService();

        UserDetails userDetails = userDetailsService.loadUserByUsername("inactive@test.com");

        assertThat(userDetails.isEnabled()).isFalse();

        verify(clientRepository, times(1)).findByEmail("inactive@test.com");
    }

    @Test
    void userDetailsService_doitLeverExceptionSiClientIntrouvable() {
        when(clientRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        UserDetailsService userDetailsService = securityConfig.userDetailsService();

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Client introuvable avec cet email");

        verify(clientRepository, times(1)).findByEmail("unknown@test.com");
    }

    @Test
    void authenticationManager_doitAuthentifierClientAvecBonMotDePasse() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        Client client = Client.builder()
                .id(1L)
                .email("alex@test.com")
                .motDePasse(passwordEncoder.encode("password123"))
                .role(Role.CLIENT)
                .active(true)
                .build();

        when(clientRepository.findByEmail("alex@test.com"))
                .thenReturn(Optional.of(client));

        UserDetailsService userDetailsService = securityConfig.userDetailsService();

        ProviderManager authenticationManager =
                securityConfig.authenticationManager(userDetailsService, passwordEncoder);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken("alex@test.com", "password123");

        Authentication authentication =
                authenticationManager.authenticate(authenticationToken);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getName()).isEqualTo("alex@test.com");

        verify(clientRepository, times(1)).findByEmail("alex@test.com");
    }

    @Test
    void jwtAuthenticationConverter_doitLireLesRolesDepuisLeClaimRoles() {
        JwtAuthenticationConverter converter =
                securityConfig.jwtAuthenticationConverter();

        Jwt jwt = new Jwt(
                "token-test",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", "admin@test.com",
                        "roles", List.of("ROLE_ADMIN")
                )
        );

        Authentication authentication = converter.convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("admin@test.com");

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void corsConfigurationSource_doitAutoriserReactLocalhost() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/produits");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();

        assertThat(configuration.getAllowedOrigins())
                .contains("http://localhost:3000", "http://localhost:5173");

        assertThat(configuration.getAllowedMethods())
                .contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

        assertThat(configuration.getAllowedHeaders())
                .contains("*");

        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void jwtEncoderEtJwtDecoder_doiventEncoderEtDecoderUnToken() {
        JwtEncoder jwtEncoder = securityConfig.jwtEncoder();
        JwtDecoder jwtDecoder = securityConfig.jwtDecoder();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("site-ecommerce-api")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("alex@test.com")
                .claim("clientId", 1L)
                .claim("roles", List.of("ROLE_CLIENT"))
                .build();

        Jwt encodedJwt = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        );

        Jwt decodedJwt = jwtDecoder.decode(encodedJwt.getTokenValue());

        Number clientId = decodedJwt.getClaim("clientId");
        List<String> roles = decodedJwt.getClaim("roles");

        assertThat(encodedJwt.getTokenValue()).isNotBlank();
        assertThat(decodedJwt.getSubject()).isEqualTo("alex@test.com");
        assertThat(clientId.longValue()).isEqualTo(1L);
        assertThat(roles).containsExactly("ROLE_CLIENT");
    }
}