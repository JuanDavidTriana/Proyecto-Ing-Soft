package com.biblioteca.libraryspring.dto;

import com.biblioteca.libraryspring.model.Rol;
import jakarta.validation.constraints.NotNull;

/** Usado solo por un ADMIN, en PUT /usuarios/{id}/rol, para ascender o
 * degradar a otro usuario. */
public class RolUpdateRequest {

    @NotNull
    private Rol rol;

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
