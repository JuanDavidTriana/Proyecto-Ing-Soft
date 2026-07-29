package com.biblioteca.libraryspring.controller;

import com.biblioteca.libraryspring.dto.UsuarioRequest;
import com.biblioteca.libraryspring.dto.UsuarioResponse;
import com.biblioteca.libraryspring.dto.UsuarioUpdateRequest;
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
        usuarioRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El username ya existe");
        });
        Usuario usuario = new Usuario(request.getUsername(), passwordEncoder.encode(request.getPassword()));
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
