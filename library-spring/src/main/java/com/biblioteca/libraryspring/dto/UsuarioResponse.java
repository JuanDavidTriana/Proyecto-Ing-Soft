package com.biblioteca.libraryspring.dto;

import com.biblioteca.libraryspring.model.Rol;
import com.biblioteca.libraryspring.model.Usuario;

public class UsuarioResponse {

    private Long id;
    private String username;
    private Rol rol;
    // El passwordHash nunca se expone en las respuestas

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.rol = usuario.getRol();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Rol getRol() {
        return rol;
    }
}
