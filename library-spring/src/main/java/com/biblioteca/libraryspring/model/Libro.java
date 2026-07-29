package com.biblioteca.libraryspring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "libros")
public class Libro {

    // Nuevo: id de tipo UUID en vez de Long autoincremental.
    // GenerationType.UUID (disponible desde Hibernate 6 / JPA en Spring
    // Boot 3.x) le pide a Hibernate que genere el UUID en la aplicación
    // ANTES del INSERT (a diferencia de IDENTITY, que dependía de que la
    // base de datos generara el número). Postgres lo guarda en su tipo
    // nativo "uuid" (16 bytes, no texto).
    // Ver ../../PAGINACION_UUID.md para la explicación completa de por
    // qué el catálogo de libros usa UUID y Usuario sigue con Long.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false)
    private Integer cantidad;

    public Libro() {
    }

    public Libro(String titulo, String autor, String isbn, Integer cantidad) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.cantidad = cantidad;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
