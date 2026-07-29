package com.biblioteca.libraryspring.security;

import com.biblioteca.libraryspring.model.Usuario;
import com.biblioteca.libraryspring.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Puente entre nuestra entidad Usuario y el modelo de usuario que Spring
 * Security entiende (UserDetails). Spring Security la usa internamente
 * para saber, a partir de un username, cuál es su password (hash) y
 * qué "authorities" (roles/permisos) tiene.
 *
 * IMPORTANTE: el prefijo "ROLE_" es una convención que exige Spring
 * Security para que hasRole("ADMIN") funcione: internamente busca la
 * authority "ROLE_ADMIN", no "ADMIN".
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())))
                .build();
    }
}
