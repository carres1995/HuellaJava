package com.huellas.service;

import com.huellas.config.ConnectionFactory;
import com.huellas.exception.AppointmentException;
import com.huellas.exception.PetException;
import com.huellas.exception.RegistroIntegralException;
import com.huellas.model.Appointment;
import com.huellas.model.Pet;
import com.huellas.repository.AppointmentRepository;
import com.huellas.repository.PetRepository;

import java.sql.Connection;

/**
 * Servicio para el Registro Integral (Mascota + Cita)
 * Implementa las reglas del Bloque A de SPEC-005.
 */
public class IntegralRegistrationService {

    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;

    public IntegralRegistrationService(PetRepository petRepository, AppointmentRepository appointmentRepository) {
        this.petRepository = petRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public static class RegistroIntegralResult {
        public final Long petId;
        public final Long appointmentId;

        public RegistroIntegralResult(Long petId, Long appointmentId) {
            this.petId = petId;
            this.appointmentId = appointmentId;
        }
    }

    public RegistroIntegralResult registerPetWithAppointment(Pet pet, Appointment appointment) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            try {
                // 1. Validaciones previas usando servicios si es posible, 
                // o confiaremos en que las reglas se apliquen antes/después
                
                // 2. Iniciar Transacción (BR-005)
                conn.setAutoCommit(false);

                // 3. Guardar Mascota (BR-001)
                Long petId = petRepository.save(pet, conn);
                
                // 4. Vincular mascota a cita
                appointment.setPetId(petId);

                // 5. Validar solapamientos ANTES de guardar (BR-002, BR-006)
                int overlaps = appointmentRepository.countOverlapping(
                    appointment.getVeterinarianId(), 
                    appointment.getStartTime(), 
                    appointment.getEndTime()
                );
                if (overlaps > 0) {
                    throw new AppointmentException("El veterinario ya tiene una cita en ese horario");
                }

                // 6. Guardar Cita
                Long apptId = appointmentRepository.save(appointment, conn);

                // 7. Confirmar Transacción
                conn.commit();
                return new RegistroIntegralResult(petId, apptId);

            } catch (AppointmentException | PetException e) {
                conn.rollback(); // (BR-003, BR-004)
                throw e; // Re-lanza la excepción de negocio
            } catch (Exception e) {
                conn.rollback();
                throw new RegistroIntegralException("Error inesperado en el registro integral: " + e.getMessage(), e);
            } finally {
                // Restaurar el auto-commit a true por buena práctica
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RegistroIntegralException("Error de conexión a la BD: " + e.getMessage(), e);
        }
    }
}
