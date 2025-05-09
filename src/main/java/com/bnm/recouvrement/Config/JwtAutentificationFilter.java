package com.bnm.recouvrement.Config;

import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAutentificationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid JWT Token: " + e.getMessage());
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails instanceof User) {
                    User currentUser = (User) userDetails;
                    if (currentUser.getRole() == null || !currentUser.getRole().isActive()) {
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.getWriter().write("User role is deactivated or not assigned. Access denied.");
                        return;
                    }
                } else {
                    User freshUser = userRepository.findByEmail(username).orElse(null);
                    if (freshUser == null || freshUser.getRole() == null || !freshUser.getRole().isActive()) {
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.getWriter().write("User account issue or role deactivated. Access denied.");
                        return;
                    }
                }

                if (jwtService.validateToken(jwt, userDetails)) {
                    Map<String, Object> claims = jwtService.extractAllClaims(jwt);
                    String role = (String) claims.get("role");
                    List<String> permissions = (List<String>) claims.get("permissions");

                    System.out.println("User Role from token: " + role);
                    System.out.println("User Permissions from token: " + permissions);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                System.out.println("Authentication failed during user details processing or token validation: " + e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Authentication Failed: " + e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
