package com.biblioteca.libraryspring.controller;

import com.biblioteca.libraryspring.dto.RolUpdateRequest;
import com.biblioteca.libraryspring.dto.UsuarioRequest;
import com.biblioteca.libraryspring.dto.UsuarioResponse;
import com.biblioteca.libraryspring.dto.UsuarioUpdateRequest;
import com.biblioteca.libraryspring.model.Rol;
import com.biblioteca.libraryspring.model.Usuario;
import com.biblioteca.libraryspring.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Quién puede llamar cada endpoint NO se decide aquí, sino de forma
 * centralizada en security/SecurityConfig.java:
 *   - GET  /usuarios/**  -> ADMIN o USER (cualquiera autenticado)
 *   - POST/PUT/DELETE /usuarios/** -> solo ADMIN
 * Si alguien sin el rol correcto llama a estos endpoints, Spring
 * Security corta la petición con 403 ANTES de que este código se
 * ejecute.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        // Creado por un ADMIN: entra como USER por defecto. Para dejarlo
        // como ADMIN, usar después PUT /usuarios/{id}/rol.
        usuarioRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El username ya existe");
        });
        Usuario usuario = new Usuario(request.getUsername(), passwordEncoder.encode(request.getPassword()), Rol.USER);
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    @GetMapping
    public Page<UsuarioResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return usuarioRepository
                .findAll(PageRequest.of(page, size, Sort.by("id")))
                .map(UsuarioResponse::new);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return new UsuarioResponse(usuario);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (request.getUsername() != null) {
            usuario.setUsername(request.getUsername());
        }
        if (request.getPassword() != null) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    @PutMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @Valid @RequestBody RolUpdateRequest request) {
        // Endpoint dedicado (en vez de meterlo en actualizar()) para que
        // el cambio de rol sea explícito y fácil de auditar/loggear en
        // un proyecto real.
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        usuario.setRol(request.getRol());
        return new UsuarioResponse(usuarioRepository.save(usuario));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
