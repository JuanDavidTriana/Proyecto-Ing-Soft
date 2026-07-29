package com.biblioteca.libraryspring.dto;

import jakarta.validation.constraints.Size;

public class UsuarioUpdateRequest {

    @Size(min = 3, max = 50)
    private String username;

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
