package com.huellas.service.impl;

import com.huellas.exception.AppointmentException;
import com.huellas.exception.ServiceException;
import com.huellas.model.Appointment;
import com.huellas.model.Status;
import com.huellas.repository.AppointmentRepository;
import com.huellas.repository.PetRepository;
import com.huellas.repository.UserRepository;
import com.huellas.service.AppointmentService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de AppointmentService aplicando reglas de SPEC-004.
 */
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, 
                                  UserRepository userRepository, 
                                  PetRepository petRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.petRepository = petRepository;
    }

    @Override
    public Long scheduleAppointment(Appointment appointment) {
        try {
            // 1. Validaciones de tiempo (BR-001, BR-002, BR-003)
            validateTimes(appointment);

            // 2. Validar Existencia y Estado de Entidades (BR-004, BR-005, BR-009)
            validateEntities(appointment);

            // 3. Validar Solapamiento (BR-006 / CALC-001)
            int overlaps = appointmentRepository.countOverlapping(
                appointment.getVeterinarianId(), 
                appointment.getStartTime(), 
                appointment.getEndTime()
            );
            if (overlaps > 0) {
                throw new AppointmentException("El veterinario ya tiene una cita en ese horario");
            }

            // 4. Forzar estado inicial
            appointment.setStatus(Status.PENDING);

            // 5. Persistir
            return appointmentRepository.save(appointment);

        } catch (SQLException e) {
            throw new ServiceException("Error al agendar la cita", e);
        }
    }

    @Override
    public void cancelAppointment(Long appointmentId) {
        updateAppointmentStatus(appointmentId, Status.CANCELLED, List.of(Status.PENDING, Status.CONFIRMED), 
            "Solo se pueden cancelar citas pendientes o confirmadas");
    }

    @Override
    public void completeAppointment(Long appointmentId) {
        updateAppointmentStatus(appointmentId, Status.DONE, List.of(Status.CONFIRMED), 
            "Solo se pueden completar citas confirmadas");
    }

    @Override
    public void confirmAppointment(Long appointmentId) {
        updateAppointmentStatus(appointmentId, Status.CONFIRMED, List.of(Status.PENDING), 
            "Solo se pueden confirmar citas pendientes");
    }

    @Override
    public List<Appointment> getDailyAgenda(LocalDate date) {
        try {
            return appointmentRepository.findByDate(date);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener la agenda diaria", e);
        }
    }

    private void validateTimes(Appointment a) {
        if (a.getStartTime() == null) {
            throw new AppointmentException("La hora de inicio es obligatoria");
        }
        if (a.getEndTime() == null) {
            throw new AppointmentException("La hora de fin es obligatoria");
        }
        if (a.getEndTime().isBefore(a.getStartTime()) || a.getEndTime().isEqual(a.getStartTime())) {
            throw new AppointmentException("La hora de fin debe ser posterior a la hora de inicio");
        }
        if (a.getStartTime().isBefore(LocalDateTime.now())) {
            throw new AppointmentException("No se puede agendar una cita en el pasado");
        }
    }

    private void validateEntities(Appointment a) throws SQLException {
        // Validar Cliente (BR-009)
        userRepository.findById(a.getUserId()).ifPresentOrElse(u -> {
            if (!u.isActive()) throw new AppointmentException("El cliente especificado no existe o está inactivo");
        }, () -> { throw new AppointmentException("El cliente especificado no existe o está inactivo"); });

        // Validar Mascota (BR-005)
        petRepository.findById(a.getPetId()).ifPresentOrElse(p -> {
            if (!p.isActive()) throw new AppointmentException("La mascota especificada no existe o está inactiva");
        }, () -> { throw new AppointmentException("La mascota especificada no existe o está inactiva"); });

        // Validar Veterinario (BR-004)
        userRepository.findById(a.getVeterinarianId()).ifPresentOrElse(v -> {
            if (!v.isActive()) throw new AppointmentException("El veterinario especificado no está disponible");
            // Nota: Podríamos validar que el rol sea VETERINARIAN aquí también.
        }, () -> { throw new AppointmentException("El veterinario especificado no está disponible"); });
    }

    private void updateAppointmentStatus(Long id, Status newStatus, List<Status> validSourceStatuses, String errorMessage) {
        try {
            appointmentRepository.findById(id).ifPresentOrElse(a -> {
                if (!validSourceStatuses.contains(a.getStatus())) {
                    throw new AppointmentException(errorMessage);
                }
                try {
                    appointmentRepository.updateStatus(id, newStatus);
                } catch (SQLException e) {
                    throw new ServiceException("Error al actualizar estado de la cita", e);
                }
            }, () -> { throw new AppointmentException("La cita no existe"); });
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar la cita", e);
        }
    }
}
