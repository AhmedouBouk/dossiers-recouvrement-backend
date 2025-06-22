package com.bnm.recouvrement.Config;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAutentificationFilter jwtAutentificationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Désactiver CSRF (nécessaire pour les API stateless)
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(List.of("http://localhost:4200")); // Autoriser le frontend Angular
                corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Méthodes autorisées
                corsConfig.setAllowedHeaders(List.of("*")); // Autoriser tous les en-têtes
                corsConfig.setAllowCredentials(true); // Autoriser les cookies et les en-têtes d'authentification
                corsConfig.setExposedHeaders(List.of("Authorization")); // Exposer l'en-tête Authorization
                return corsConfig;
            }))
            .authorizeHttpRequests(auth -> auth
                // Autoriser les requêtes OPTIONS pour CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    
                // CLIENTS - Dynamic permission-based access control
                .requestMatchers("/auth/**").permitAll()  // Allow login
                .requestMatchers("/clients/**").hasAuthority("READ_CLIENT")
                .requestMatchers("/clients/create").hasAuthority("CREATE_CLIENT")
                .requestMatchers("/clients/update/**").hasAuthority("UPDATE_CLIENT")
                .requestMatchers("/clients/delete/**").hasAuthority("DELETE_CLIENT")
               

                // COMPTES - Apply CRUD permissions for accounts
                .requestMatchers("/comptes/**").hasAuthority("READ_COMPTE")
                .requestMatchers("/comptes/create").hasAuthority("CREATE_COMPTE")
                .requestMatchers("/comptes/update/**").hasAuthority("UPDATE_COMPTE")
                .requestMatchers("/comptes/delete/**").hasAuthority("DELETE_COMPTE")
    
                // CREDITS - Apply CRUD permissions for credit management
                .requestMatchers("/credits/**").hasAuthority("READ_CREDIT")
                .requestMatchers("/credits/create").hasAuthority("CREATE_CREDIT")
                .requestMatchers("/credits/update/**").hasAuthority("UPDATE_CREDIT")
                .requestMatchers("/credits/delete/**").hasAuthority("DELETE_CREDIT")
    
                // DOSSIER RECOUVREMENT - Apply CRUD permissions
                .requestMatchers("/DossierRecouvrement/**").hasAuthority("READ_DOSSIER")
                .requestMatchers("/DossierRecouvrement/create").hasAuthority("CREATE_DOSSIER")
                .requestMatchers("/DossierRecouvrement/update/**").hasAuthority("UPDATE_DOSSIER")
                .requestMatchers("/DossierRecouvrement/delete/**").hasAuthority("DELETE_DOSSIER")
    
                // ADMIN - Only Admins can access these endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/roles").hasAuthority("CREATE_ROLE")
                .requestMatchers(HttpMethod.GET, "/admin/roles").hasAuthority("READ_ROLE")
                .requestMatchers(HttpMethod.PUT, "/admin/roles/**").hasAuthority("UPDATE_ROLE")
                .requestMatchers(HttpMethod.DELETE, "/admin/roles/**").hasAuthority("DELETE_ROLE")
    
                // DASHBOARD - Read-only access to dashboard statistics
                .requestMatchers("/dashboard/**").permitAll()

                // USERS - Permettre l'accès aux endpoints utilisateurs pour le workflow de rejet
                .requestMatchers("/users/**").permitAll()
                .requestMatchers("/users/types").permitAll()
                .requestMatchers("/users/type/**").permitAll()

                // REJETS - Permettre l'accès aux endpoints de rejet pour le workflow multi-étapes
                .requestMatchers("/api/rejets/**").permitAll()
                .requestMatchers("/rejets/**").permitAll()
    
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Utiliser des sessions stateless
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAutentificationFilter, UsernamePasswordAuthenticationFilter.class);
    
        return http.build();
    }
}
