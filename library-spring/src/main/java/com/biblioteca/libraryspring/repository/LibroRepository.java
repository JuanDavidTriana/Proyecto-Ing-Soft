package com.biblioteca.libraryspring.repository;

import com.biblioteca.libraryspring.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LibroRepository extends JpaRepository<Libro, UUID> {
    Optional<Libro> findByIsbn(String isbn);
}
