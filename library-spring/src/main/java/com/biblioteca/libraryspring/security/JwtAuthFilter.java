package com.biblioteca.libraryspring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que se ejecuta UNA VEZ por cada request HTTP (OncePerRequestFilter),
 * ANTES de que la petición llegue al controller. Su trabajo:
 *
 *   1. Buscar el header "Authorization: Bearer <token>".
 *   2. Si existe y el token es válido, extraer el username y cargar sus
 *      datos (rol) con CustomUserDetailsService.
 *   3. Guardar esa información en el SecurityContext de Spring, para que
 *      el resto del framework (y nuestras reglas hasRole(...) en
 *      SecurityConfig) sepan "quién está haciendo esta petición" y "qué
 *      rol tiene", sin volver a pedir username/password en cada request.
 *
 * Si no hay token o es inválido, simplemente no se autentica a nadie:
 * la petición sigue su curso y, más adelante, SecurityConfig decide si
 * esa ruta requiere estar autenticado o no.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", dejamos pasar la
        // petición sin autenticar (SecurityConfig decidirá si la bloquea).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // quita el prefijo "Bearer "

        if (jwtService.validarToken(token)) {
            String username = jwtService.extraerUsername(token);

            // Solo autenticamos si todavía no hay nadie autenticado en
            // este contexto (evita trabajo redundante).
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
