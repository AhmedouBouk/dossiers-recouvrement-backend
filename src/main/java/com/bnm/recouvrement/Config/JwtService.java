package com.bnm.recouvrement.Config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.bnm.recouvrement.entity.Permission;
import com.bnm.recouvrement.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service // This ensures the class is a Spring-managed component
public class JwtService {

    private static final String SECRET_KEY = "pFR/4gKqQWIdGIO+dE37DCthVlCbcI1bGtprGDW18+M=";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

public String generateToken(UserDetails userDetails) {
    User user = (User) userDetails; // Cast UserDetails to User
    Map<String, Object> claims = new HashMap<>();
    
    // Add user ID to claims
    claims.put("userId", user.getId());
    
    // Add role and permissions
    claims.put("role", user.getRole().getName());  // Add role
    claims.put("permissions", user.getRole().getPermissions().stream()
                                  .map(Permission::getName)
                                  .collect(Collectors.toList())); // Add permissions
    
    // Add user type if available
    if (user.getUserType() != null) {
        claims.put("userType", user.getUserType());
    }
    
    return generateToken(claims, userDetails);
}

    
    

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
