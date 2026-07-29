package com.biblioteca.libraryspring.model;

/**
 * Roles disponibles en la aplicación. Se guarda como texto en la BD
 * (@Enumerated(EnumType.STRING) en Usuario), así que en la columna
 * "rol" vas a ver literalmente "ADMIN" o "USER".
 */
public enum Rol {
    ADMIN,
    USER
}
