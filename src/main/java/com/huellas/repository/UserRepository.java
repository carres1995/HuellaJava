package com.huellas.repository;

import com.huellas.model.User;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Contrato para operaciones de persistencia de usuarios.
 * Referencia: SPEC-002
 */
public interface UserRepository {
    
    /**
     * Busca un usuario por su ID único.
     */
    Optional<User> findById(Long id) throws SQLException;

    /**
     * Busca un usuario por su correo electrónico.
     * @param email Email a buscar.
     * @return Un Optional con el usuario si existe.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    Optional<User> findByEmail(String email) throws SQLException;

    /**
     * Actualiza el estado de actividad de un usuario.
     */
    void updateActiveStatus(Long id, boolean active) throws SQLException;

    /**
     * Guarda un nuevo usuario o actualiza uno existente.
     * @param user Usuario a persistir.
     * @return El ID generado.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    Long save(User user) throws SQLException;
}
