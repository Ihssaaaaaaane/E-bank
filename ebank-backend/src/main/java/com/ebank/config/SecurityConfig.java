// src/main/java/com/ebank/config/SecurityConfig.java
package com.ebank.config;

import com.ebank.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays; // Import nécessaire

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Injection du filtre JWT et du service pour charger les utilisateurs
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService; // Implémenté par UserDetailsServiceImpl

    /**
     * Définit la chaîne de filtres de sécurité.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // 1. CONFIGURATION CORS : Autorise les requêtes provenant de localhost:3000 (React)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Désactiver CSRF car c'est une API REST sans état
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Gestion des sessions : nous utilisons JWT, donc pas de sessions côté serveur (STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Définition des règles d'autorisation basées sur les rôles
                .authorizeHttpRequests(auth -> auth
                        // UC-1 : Autoriser l'accès public au service d'authentification
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/forgot-password",
                                "/auth/reset-password"
                        ).permitAll()

                        // Agents Guichet : Restriction au profil AGENT_GUICHET
                        .requestMatchers("/api/agents/**").hasAuthority("AGENT_GUICHET")

                        // Clients : Restriction au profil CLIENT
                        .requestMatchers("/api/clients/**").hasAuthority("CLIENT")

                        // Toutes les autres requêtes doivent être authentifiées
                        .anyRequest().authenticated()
                )

                // 5. Injecter l'AuthenticationProvider personnalisé
                .authenticationProvider(authenticationProvider())

                // 6. Ajouter le filtre JWT personnalisé AVANT le filtre standard de Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Définit la source de configuration CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Autoriser l'origine de votre application React
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

        // Autoriser les méthodes HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Autoriser l'envoi des Headers nécessaires (y compris Authorization pour le JWT)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Appliquer cette configuration à toutes les routes
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Définit l'encodeur de mot de passe (BCrypt).
     * RG_1: Le mot de passe doit être crypté en base de données.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Définit l'AuthenticationProvider qui utilise le UserDetailsService et le PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expose l'AuthenticationManager pour qu'il puisse être utilisé dans l'AuthController.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}