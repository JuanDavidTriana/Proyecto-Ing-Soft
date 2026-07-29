package com.biblioteca.libraryspring.dto;

import com.biblioteca.libraryspring.model.Usuario;

public class UsuarioResponse {

    private Long id;
    private String username;
    // El passwordHash nunca se expone en las respuestas

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
