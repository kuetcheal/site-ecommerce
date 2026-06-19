package com.kuetche.siteecommerce.security;

import com.kuetche.siteecommerce.entity.Client;
import com.kuetche.siteecommerce.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "expirationMinutes", 60L);
    }

    @Test
    void generateToken_doitRetournerLeTokenEncode() {
        // Arrange
        Client client = Client.builder()
                .id(1L)
                .email("alex@test.com")
                .role(Role.CLIENT)
                .build();

        Jwt jwtMock = new Jwt(
                "jwt-token-test",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "alex@test.com")
        );

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwtMock);

        // Act
        String token = jwtService.generateToken(client);

        // Assert
        assertThat(token).isEqualTo("jwt-token-test");

        verify(jwtEncoder, times(1))
                .encode(any(JwtEncoderParameters.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateToken_doitConstruireLesClaimsCorrectement() {
        // Arrange
        Client client = Client.builder()
                .id(10L)
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();

        Jwt jwtMock = new Jwt(
                "jwt-admin-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "admin@test.com")
        );

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwtMock);

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);

        // Act
        String token = jwtService.generateToken(client);

        // Assert
        assertThat(token).isEqualTo("jwt-admin-token");

        verify(jwtEncoder).encode(captor.capture());

        JwtEncoderParameters params = captor.getValue();
        JwtClaimsSet claims = params.getClaims();

        String issuer = claims.getClaim("iss");
        Long clientId = claims.getClaim("clientId");
        List<String> roles = claims.getClaim("roles");

        assertThat(issuer).isEqualTo("site-ecommerce-api");
        assertThat(claims.getSubject()).isEqualTo("admin@test.com");
        assertThat(clientId).isEqualTo(10L);
        assertThat(roles).containsExactly("ROLE_ADMIN");

        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiresAt()).isNotNull();
        assertThat(claims.getExpiresAt()).isAfter(claims.getIssuedAt());
    }
}