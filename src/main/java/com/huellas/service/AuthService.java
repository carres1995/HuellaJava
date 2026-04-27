package com.huellas.service;

import com.huellas.exception.AuthException;
import com.huellas.exception.ServiceException;
import com.huellas.model.User;
import com.huellas.repository.UserRepository;
import com.huellas.util.PasswordUtil;
import com.huellas.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Servicio encargado de la lógica de autenticación.
 * Referencia: SPEC-001
 */
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Autentica a un usuario en el sistema.
     * @param email Email del usuario.
     * @param password Contraseña en texto plano.
     * @return El usuario autenticado.
     */
    public User login(String email, String password) {
        // BR-001: El email no puede ser nulo ni vacío
        if (ValidationUtil.isEmpty(email)) {
            throw new AuthException("El email es obligatorio");
        }

        // BR-002: La contraseña no puede ser nula ni vacía
        if (ValidationUtil.isEmpty(password)) {
            throw new AuthException("La contraseña es obligatoria");
        }

        try {
            // BR-003: El email debe existir en la base de datos
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new AuthException("Credenciales incorrectas");
            }

            User user = userOpt.get();

            // BR-004: El hash de la contraseña debe coincidir
            if (!PasswordUtil.verify(password, user.getPassword())) {
                throw new AuthException("Credenciales incorrectas");
            }

            // BR-005: El usuario debe tener active = true
            if (!user.isActive()) {
                throw new AuthException("Usuario inactivo. Contacte al administrador.");
            }

            // BR-006: Retornar el usuario autenticado para control de acceso
            return user;

        } catch (SQLException e) {
            // Encapsulamos el error técnico de base de datos
            throw new ServiceException("Error técnico durante la autenticación", e);
        }
    }
}
