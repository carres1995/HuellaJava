package com.huellas.service.impl;

import com.huellas.service.UserService;
import com.huellas.exception.ServiceException;
import com.huellas.exception.ValidationException;
import com.huellas.model.User;
import com.huellas.repository.UserRepository;
import com.huellas.util.PasswordUtil;
import com.huellas.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Implementación de UserService.
 * Aplica Reglas de Negocio (BR-001 a BR-004) de SPEC-002.
 */
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Long registerUser(User user) {
        try {
            // 1. Validaciones de Negocio
            validateUserFields(user);
            
            // 2. Verificar duplicidad de email (BR-003)
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new ValidationException("El correo electrónico ya está registrado: " + user.getEmail());
            }

            // 3. Aplicar Hashing de contraseña (BR-004)
            String hashedPassword = PasswordUtil.hash(user.getPassword());
            user.setPassword(hashedPassword);

            // 4. Asegurar estado inicial
            user.setActive(true);

            // 5. Persistir
            return userRepository.save(user);

        } catch (SQLException e) {
            e.printStackTrace(); // Para la consola
            throw new ServiceException("Error de BD al guardar: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar usuario por email", e);
        }
    }

    @Override
    public void setUserStatus(Long userId, boolean active) {
        try {
            userRepository.updateActiveStatus(userId, active);
        } catch (SQLException e) {
            throw new ServiceException("Error al cambiar el estado del usuario", e);
        }
    }

    /**
     * Valida los campos obligatorios y el formato de datos.
     */
    private void validateUserFields(User user) {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new ValidationException("El nombre es obligatorio (BR-001)");
        }
        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new ValidationException("Formato de email inválido (BR-002)");
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new ValidationException("La contraseña debe tener al menos 8 caracteres");
        }
    }
}
