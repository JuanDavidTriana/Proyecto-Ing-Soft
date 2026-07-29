package com.biblioteca.libraryspring.controller;

import com.biblioteca.libraryspring.dto.LoginRequest;
import com.biblioteca.libraryspring.dto.RegistroRequest;
import com.biblioteca.libraryspring.dto.TokenResponse;
import com.biblioteca.libraryspring.dto.UsuarioResponse;
import com.biblioteca.libraryspring.model.Rol;
import com.biblioteca.libraryspring.model.Usuario;
import com.biblioteca.libraryspring.repository.UsuarioRepository;
import com.biblioteca.libraryspring.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoints PÚBLICOS (ver SecurityConfig: "/auth/**".permitAll()).
 * Son la única puerta de entrada para conseguir un JWT.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest request) {
        // Registro público: SIEMPRE se crea con Rol.USER. El DTO
        // RegistroRequest ni siquiera tiene un campo "rol", así que no
        // hay forma de que el cliente pida ser ADMIN desde aquí.
        usuarioRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El username ya existe");
        });

        Usuario usuario = new Usuario(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                Rol.USER
        );
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username o password incorrectos"));

        // Comparamos con el HASH guardado (BCrypt), nunca comparando
        // strings directamente: en la BD solo existe el hash, jamás la
        // contraseña en texto plano.
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username o password incorrectos");
        }

        String token = jwtService.generarToken(usuario.getUsername());
        return new TokenResponse(token);
    }
}
