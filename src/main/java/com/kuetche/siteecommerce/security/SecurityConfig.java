package com.kuetche.siteecommerce.security;

import com.kuetche.siteecommerce.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final ClientRepository clientRepository;

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth

                                                // Requêtes CORS envoyées par React
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // Autoriser Promotheus
                                                // Autoriser Actuator / Prometheus
                                                .requestMatchers("/actuator/health").permitAll()
                                                .requestMatchers("/actuator/prometheus").permitAll()

                                                // Routes publiques d'authentification
                                                .requestMatchers("/api/auth/**").permitAll()

                                                // Consultation publique des produits
                                                .requestMatchers(HttpMethod.GET, "/api/produits/**").permitAll()

                                                // Envoi du formulaire de contact public
                                                .requestMatchers(HttpMethod.POST, "/api/contacts").permitAll()

                                                // .requestMatchers(HttpMethod.POST, "/api/produits/**").permitAll()

                                                // Gestion des messages de contact réservée à l'administrateur
                                                .requestMatchers("/api/contacts/admin", "/api/contacts/admin/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                // Gestion des produits réservée à l'administrateur
                                                .requestMatchers(HttpMethod.POST, "/api/produits/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                .requestMatchers(HttpMethod.PUT, "/api/produits/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                .requestMatchers(HttpMethod.DELETE, "/api/produits/**")
                                                .hasAuthority("ROLE_ADMIN")

                                                // Commandes réservées aux utilisateurs connectés
                                                .requestMatchers("/api/commandes/**").authenticated()

                                                // Toutes les autres routes nécessitent une authentification
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter())))
                                .build();
        }

        @Bean
        public UserDetailsService userDetailsService() {
                return email -> clientRepository.findByEmail(email)
                                .map(client -> User
                                                .withUsername(client.getEmail())
                                                .password(client.getMotDePasse())
                                                .authorities("ROLE_" + client.getRole().name())
                                                .disabled(!client.getActive())
                                                .build())
                                .orElseThrow(() -> new RuntimeException("Client introuvable avec cet email"));
        }

        @Bean
        public ProviderManager authenticationManager(
                        UserDetailsService userDetailsService,
                        PasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder);

                return new ProviderManager(provider);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public JwtEncoder jwtEncoder() {
                SecretKeySpec secretKey = new SecretKeySpec(
                                jwtSecret.getBytes(StandardCharsets.UTF_8),
                                "HmacSHA256");

                return new NimbusJwtEncoder(
                                new com.nimbusds.jose.jwk.source.ImmutableSecret<>(secretKey));
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                SecretKeySpec secretKey = new SecretKeySpec(
                                jwtSecret.getBytes(StandardCharsets.UTF_8),
                                "HmacSHA256");

                return NimbusJwtDecoder
                                .withSecretKey(secretKey)
                                .macAlgorithm(MacAlgorithm.HS256)
                                .build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

                authoritiesConverter.setAuthoritiesClaimName("roles");
                authoritiesConverter.setAuthorityPrefix("");

                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

                return converter;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(List.of(
                                "http://localhost:3000",
                                "http://localhost:5173"));

                configuration.setAllowedMethods(List.of(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",
                                "DELETE",
                                "OPTIONS"));

                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}