package com.bnm.recouvrement.Config;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAutentificationFilter jwtAutentificationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(List.of("http://localhost:4200"));
                corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfig.setAllowedHeaders(List.of("*"));
                corsConfig.setExposedHeaders(List.of("Authorization"));
                return corsConfig;
            }))
            .authorizeHttpRequests(auth -> auth
                // CLIENTS - Dynamic permission-based access control
                .requestMatchers("/auth/register").permitAll()  // Allow public registration
                .requestMatchers("/auth/login").permitAll()  // Allow login
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

                // DASHBOARD - Read-only access to dashboard statistics
                

                // ADMIN - Only Admins can access these endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/roles").hasAuthority("CREATE_ROLE")
                .requestMatchers(HttpMethod.GET, "/admin/roles").hasAuthority("READ_ROLE")
                .requestMatchers(HttpMethod.PUT, "/admin/roles/**").hasAuthority("UPDATE_ROLE")
                .requestMatchers(HttpMethod.DELETE, "/admin/roles/**").hasAuthority("DELETE_ROLE")
                .requestMatchers("/dashboard/**").permitAll()
                

                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAutentificationFilter, UsernamePasswordAuthenticationFilter.class);;

        return http.build();
    }
}
