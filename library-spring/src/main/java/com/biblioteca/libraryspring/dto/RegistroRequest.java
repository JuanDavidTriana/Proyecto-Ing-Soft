package com.biblioteca.libraryspring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registro público: cualquiera puede crear su cuenta, pero SIEMPRE queda
 * con rol USER. A propósito este DTO no tiene campo "rol": así el
 * cliente no puede autoasignarse ADMIN mandando un JSON manipulado.
 */
public class RegistroRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
