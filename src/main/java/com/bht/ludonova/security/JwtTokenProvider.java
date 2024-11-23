package com.bht.ludonova.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Slf4j
//@Component
//public class JwtTokenProvider {
//
//    private final SecretKey accessTokenKey;
//    private final SecretKey refreshTokenKey;
//    private final long accessTokenExpirationMs; // Changed to long
//    private final long refreshTokenExpirationMs; // Changed to long
//
//    public JwtTokenProvider(
//            @Value("${jwt.secret}") String jwtSecret,
//            @Value("${jwt.refresh-secret:${jwt.secret}}") String refreshSecret,
//            @Value("${jwt.expiration}") long accessTokenExpirationMs, // Changed to long
//            @Value("${jwt.refresh-expiration:2592000000}") long refreshTokenExpirationMs) { // Changed to long
//        this.accessTokenKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
//        this.refreshTokenKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
//        this.accessTokenExpirationMs = accessTokenExpirationMs;
//        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
//    }
//
//    public String generateAccessToken(Authentication authentication) {
//        return generateAccessToken(authentication.getName(), createClaims(authentication));
//    }
//
//    public String generateAccessToken(String username) {
//        return generateAccessToken(username, new HashMap<>());
//    }
//
//    public String generateAccessToken(String username, Map<String, Object> claims) {
//        return generateToken(username, claims, accessTokenKey, accessTokenExpirationMs);
//    }
//
//    public String generateRefreshToken(String username) {
//        return generateToken(username, new HashMap<>(), refreshTokenKey, refreshTokenExpirationMs);
//    }
//
//    private String generateToken(String username, Map<String, Object> claims, SecretKey key, long expirationMs) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + expirationMs);
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(username)
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(key, SignatureAlgorithm.HS512)
//                .compact();
//    }
//
//    private Map<String, Object> createClaims(Authentication authentication) {
//        Map<String, Object> claims = new HashMap<>();
//
//        // Convert authorities to a list
//        List<String> authorities = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
//
//        claims.put("authorities", authorities);  // Store as a list instead of a string
//        log.debug("Creating token with authorities: {}", authorities);
//
//        // Add token type
//        claims.put("type", "Bearer");
//
//        return claims;
//    }
//
////    public String getUsernameFromToken(String token) {
////        return getUsernameFromToken(token, false);
////    }
//
//    public String getUsernameFromToken(String token) {
//        Claims claims = Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//
//        log.debug("Token claims: {}", claims);
//        return claims.getSubject();
//    }
//
//    public boolean validateToken(String token) {
//        return validateToken(token, false);
//    }
//
//    public boolean validateToken(String token, boolean isRefreshToken) {
//        try {
//            SecretKey key = isRefreshToken ? refreshTokenKey : accessTokenKey;
//            Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token);
//            return true;
//        } catch (SignatureException e) {
//            log.error("Invalid JWT signature: {}", e.getMessage());
//        } catch (MalformedJwtException e) {
//            log.error("Invalid JWT token: {}", e.getMessage());
//        } catch (ExpiredJwtException e) {
//            log.error("JWT token is expired: {}", e.getMessage());
//        } catch (UnsupportedJwtException e) {
//            log.error("JWT token is unsupported: {}", e.getMessage());
//        } catch (IllegalArgumentException e) {
//            log.error("JWT claims string is empty: {}", e.getMessage());
//        }
//        return false;
//    }
//
//    public Claims getClaimsFromToken(String token, boolean isRefreshToken) {
//        SecretKey key = isRefreshToken ? refreshTokenKey : accessTokenKey;
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    public boolean isTokenExpired(String token, boolean isRefreshToken) {
//        try {
//            Claims claims = getClaimsFromToken(token, isRefreshToken);
//            return claims.getExpiration().before(new Date());
//        } catch (ExpiredJwtException e) {
//            return true;
//        }
//    }
//
//    public long getAccessTokenExpirationMs() {
//        return accessTokenExpirationMs;
//    }
//
//    public long getRefreshTokenExpirationMs() {
//        return refreshTokenExpirationMs;
//    }
//}

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.refresh-secret:${jwt.secret}}") String refreshSecret,
            @Value("${jwt.expiration}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-expiration:2592000000}") long refreshTokenExpirationMs) {
        this.accessTokenKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(Authentication authentication) {
        return generateAccessToken(authentication.getName(), createClaims(authentication));
    }

    public String generateAccessToken(String username) {
        return generateAccessToken(username, new HashMap<>());
    }

    public String generateAccessToken(String username, Map<String, Object> claims) {
        return generateToken(username, claims, accessTokenKey, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, new HashMap<>(), refreshTokenKey, refreshTokenExpirationMs);
    }

    private String generateToken(String username, Map<String, Object> claims, SecretKey key, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private Map<String, Object> createClaims(Authentication authentication) {
        Map<String, Object> claims = new HashMap<>();

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        claims.put("authorities", authorities);
        log.debug("Creating token with authorities: {}", authorities);
        claims.put("type", "Bearer");

        return claims;
    }

    public String getUsernameFromToken(String token, boolean isRefreshToken) {
        SecretKey key = isRefreshToken ? refreshTokenKey : accessTokenKey;
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        log.debug("Token claims: {}", claims);
        return claims.getSubject();
    }

    public String getUsernameFromToken(String token) {
        return getUsernameFromToken(token, false);
    }

    public boolean validateToken(String token) {
        return validateToken(token, false);
    }

    public boolean validateToken(String token, boolean isRefreshToken) {
        try {
            SecretKey key = isRefreshToken ? refreshTokenKey : accessTokenKey;
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaimsFromToken(String token, boolean isRefreshToken) {
        SecretKey key = isRefreshToken ? refreshTokenKey : accessTokenKey;
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token, boolean isRefreshToken) {
        try {
            Claims claims = getClaimsFromToken(token, isRefreshToken);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}