package com.biblioteca.libraryspring.controller;

import com.biblioteca.libraryspring.dto.LibroRequest;
import com.biblioteca.libraryspring.dto.LibroUpdateRequest;
import com.biblioteca.libraryspring.model.Libro;
import com.biblioteca.libraryspring.repository.LibroRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Igual que en UsuarioController: la autorización por rol se decide de
 * forma centralizada en security/SecurityConfig.java, no aquí:
 *   - GET  /libros/**  -> ADMIN o USER
 *   - POST/PUT/DELETE /libros/** -> solo ADMIN
 *
 * Nuevo: el id de Libro es un UUID (ver model/Libro.java y
 * ../../../../../../PAGINACION_UUID.md). Spring convierte automáticamente
 * el "{id}" de la URL a UUID; si el texto no es un UUID válido, responde
 * 400 sin que este código se ejecute.
 */
@RestController
@RequestMapping("/libros")
public class LibroController {

    private final LibroRepository libroRepository;

    public LibroController(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro crear(@Valid @RequestBody LibroRequest request) {
        libroRepository.findByIsbn(request.getIsbn()).ifPresent(l -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ISBN ya existe");
        });
        Libro libro = new Libro(request.getTitulo(), request.getAutor(), request.getIsbn(), request.getCantidad());
        return libroRepository.save(libro);
    }

    @GetMapping
    public Page<Libro> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Nota: como el id ahora es UUID (aleatorio, no secuencial),
        // ordenar por "id" ya NO refleja el orden de creación. Para un
        // catálogo real conviene ordenar por otro campo (ej. "titulo")
        // o agregar una columna "fechaCreacion". Spring Data ya incluye
        // en la respuesta JSON toda la metadata de paginación
        // (totalPages, totalElements, first, last, etc.) sin que
        // tengamos que calcularla a mano, a diferencia de FastAPI.
        return libroRepository.findAll(PageRequest.of(page, size, Sort.by("titulo")));
    }

    @GetMapping("/{id}")
    public Libro obtener(@PathVariable UUID id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));
    }

    @PutMapping("/{id}")
    public Libro actualizar(@PathVariable UUID id, @Valid @RequestBody LibroUpdateRequest request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));

        if (request.getTitulo() != null) libro.setTitulo(request.getTitulo());
        if (request.getAutor() != null) libro.setAutor(request.getAutor());
        if (request.getIsbn() != null) libro.setIsbn(request.getIsbn());
        if (request.getCantidad() != null) libro.setCantidad(request.getCantidad());

        return libroRepository.save(libro);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        if (!libroRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado");
        }
        libroRepository.deleteById(id);
    }
}
