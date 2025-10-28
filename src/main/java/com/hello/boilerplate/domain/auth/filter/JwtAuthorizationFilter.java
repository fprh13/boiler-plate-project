package com.hello.boilerplate.domain.auth.filter;

import com.hello.boilerplate.domain.auth.exception.AuthenticationEntryPointImpl;
import com.hello.boilerplate.domain.auth.utils.JwtUtil;
import com.hello.boilerplate.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;

    public static final String AUTHORITIES_KEY = "role";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String accessToken = jwtUtil.resolveAccessToken(request.getHeader(HttpHeaders.AUTHORIZATION));
            authenticate(accessToken);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new AuthenticationException(e.getMessage()) {
            });
        };
    }

    private void authenticate(String accessToken) {
        try {
            jwtUtil.validateAccessToken(accessToken);
        } catch (CustomException e) {
            return;
        }
        Claims claims = jwtUtil.getAccessTokenClaims(accessToken);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                List.of(new SimpleGrantedAuthority(claims.get(AUTHORITIES_KEY).toString()))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
