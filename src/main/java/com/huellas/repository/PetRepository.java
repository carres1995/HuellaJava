package com.huellas.repository;

import com.huellas.model.Pet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato para operaciones de persistencia de mascotas.
 * Referencia: SPEC-003
 */
public interface PetRepository {
    
    /**
     * Guarda una nueva mascota en la base de datos.
     * @return El ID generado
     */
    Long save(Pet pet) throws SQLException;

    /**
     * Busca una mascota por su ID único.
     */
    Optional<Pet> findById(Long id) throws SQLException;

    /**
     * Recupera todas las mascotas que pertenecen a un usuario específico.
     */
    List<Pet> findByUserId(Long userId) throws SQLException;

    /**
     * Actualiza la información de una mascota existente.
     */
    void update(Pet pet) throws SQLException;

    /**
     * Actualiza el estado de actividad de una mascota (borrado lógico).
     */
    void updateActiveStatus(Long id, boolean active) throws SQLException;
    /**
     * Guarda una mascota en la base de datos de forma transaccional.
     */
    Long save(Pet pet, java.sql.Connection conn) throws SQLException;
}