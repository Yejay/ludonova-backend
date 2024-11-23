package com.bht.ludonova.security;

import com.bht.ludonova.exception.AuthenticationException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // List of paths that don't require authentication
    private final List<String> publicPaths = Arrays.asList(
            "/api/auth/**",
            "/api/test/public"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return publicPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        try {
//            String jwt = getJwtFromRequest(request);
//
//            if (StringUtils.hasText(jwt)) {
//                if (!tokenProvider.validateToken(jwt)) {
//                    throw new AuthenticationException("Invalid or expired JWT token");
//                }
//
//                String username = tokenProvider.getUsernameFromToken(jwt);
//                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
//
//                log.debug("User authorities: {}", userDetails.getAuthorities());
//
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(
//                                userDetails,
//                                null,
//                                userDetails.getAuthorities()
//                        );
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }
//        } catch (Exception ex) {
//            log.error("Could not set user authentication in security context", ex);
//            SecurityContextHolder.clearContext();
//
//            if (ex instanceof AuthenticationException) {
//                throw ex;
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    Claims claims = tokenProvider.getClaimsFromToken(jwt, false);
                    String username = claims.getSubject();

                    // Extract authorities from claims
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (claims.get("authorities") instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> authoritiesList = (List<String>) claims.get("authorities");
                        authorities = authoritiesList.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                    }

                    log.debug("Token claims authorities: {}", authorities);

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities  // Use the authorities from token
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Helper method to check if token is about to expire
     * Could be used to implement token refresh logic
     */
    private boolean shouldRefreshToken(String token) {
        return tokenProvider.isTokenExpired(token, false);
    }
}