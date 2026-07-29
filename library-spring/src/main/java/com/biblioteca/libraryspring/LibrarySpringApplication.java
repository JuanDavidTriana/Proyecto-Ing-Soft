package com.biblioteca.libraryspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// El bean de PasswordEncoder (BCrypt) ahora vive en
// security/SecurityConfig.java, junto con el resto de la configuración
// de seguridad (JWT, reglas de autorización por rol, etc.).
@SpringBootApplication
public class LibrarySpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibrarySpringApplication.class, args);
    }
}
