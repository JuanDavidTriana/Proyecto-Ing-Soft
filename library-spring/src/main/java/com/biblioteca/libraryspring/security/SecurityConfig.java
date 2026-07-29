package com.biblioteca.libraryspring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de Spring Security: define QUÉ rutas son públicas,
 * cuáles requieren solo estar autenticado, y cuáles requieren un rol
 * específico. Es el equivalente Spring al archivo security.py de FastAPI,
 * pero centralizado en un solo lugar en vez de repartido en "dependencies"
 * de cada endpoint.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitamos CSRF: es una protección pensada para apps
                // que usan cookies/sesiones de navegador. Nuestra API es
                // "stateless" y se autentica con un token en cada request,
                // no con cookies, así que CSRF no aplica aquí.
                .csrf(AbstractHttpConfigurer::disable)

                // STATELESS: le decimos a Spring que NO cree ni use
                // HttpSession. Cada request debe traer su propio JWT;
                // el servidor no "recuerda" quién inició sesión.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Login y registro son públicos: sin esto nadie
                        // podría ni loguearse para conseguir un token.
                        .requestMatchers("/auth/**").permitAll()

                        // Leer catálogo/listas: cualquier usuario logueado,
                        // sin importar el rol (ADMIN o USER).
                        .requestMatchers(HttpMethod.GET, "/usuarios/**", "/libros/**")
                        .hasAnyRole("ADMIN", "USER")

                        // Crear, modificar o borrar: solo ADMIN.
                        .requestMatchers(HttpMethod.POST, "/usuarios/**", "/libros/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/usuarios/**", "/libros/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/**", "/libros/**")
                        .hasRole("ADMIN")

                        // Cualquier otra ruta no listada arriba: por
                        // seguridad, exige estar autenticado por defecto.
                        .anyRequest().authenticated()
                )

                // Insertamos nuestro filtro JWT ANTES del filtro estándar
                // de usuario/password de Spring Security, para que el
                // SecurityContext ya quede armado (o no) antes de que
                // authorizeHttpRequests evalúe las reglas de arriba.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
