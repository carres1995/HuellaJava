package com.huellas.repository;

import com.huellas.model.Appointment;
import com.huellas.model.Status;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato para operaciones de persistencia de citas médicas.
 * Referencia: SPEC-004
 */
public interface AppointmentRepository {
    
    /**
     * Guarda una nueva cita.
     */
    Long save(Appointment appointment) throws SQLException;

    /**
     * Guarda una nueva cita de forma transaccional.
     */
    Long save(Appointment appointment, java.sql.Connection conn) throws SQLException;

    /**
     * Busca una cita por su ID.
     */
    Optional<Appointment> findById(Long id) throws SQLException;

    /**
     * Cuenta cuántas citas tiene un veterinario en un rango de tiempo
     * que se solapen con el rango dado. (Para BR-006)
     */
    int countOverlapping(Long veterinarianId, LocalDateTime start, LocalDateTime end) throws SQLException;

    /**
     * Actualiza el estado de una cita.
     */
    void updateStatus(Long id, Status status) throws SQLException;

    /**
     * Busca citas por fecha de inicio.
     */
    List<Appointment> findByDate(java.time.LocalDate date) throws SQLException;
}
