package com.huellas.service;

import com.huellas.model.User;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios (Clientes, Veterinarios, Admins).
 * Referencia: SPEC-002
 */
public interface UserService {

    /**
     * Registra un nuevo usuario en el sistema.
     * Aplica validaciones de negocio y hashing de contraseña.
     * 
     * @param user El usuario a registrar
     * @return El ID del usuario generado
     * @throws RuntimeException si hay errores de validación o persistencia
     */
    Long registerUser(User user);

    /**
     * Busca un usuario por su correo electrónico.
     */
    Optional<User> findByEmail(String email);

    /**
     * Cambia el estado de actividad de un usuario.
     */
    void setUserStatus(Long userId, boolean active);
}
