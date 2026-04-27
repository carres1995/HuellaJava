package com.huellas.service;

import com.huellas.model.Appointment;
import com.huellas.model.Status;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la gestión de citas médicas.
 * Referencia: SPEC-004
 */
public interface AppointmentService {

    /**
     * Agendar una nueva cita validando reglas de negocio y solapamientos.
     */
    Long scheduleAppointment(Appointment appointment);

    /**
     * Cancela una cita existente.
     */
    void cancelAppointment(Long appointmentId);

    /**
     * Marca una cita como completada.
     */
    void completeAppointment(Long appointmentId);

    /**
     * Confirma una cita pendiente.
     */
    void confirmAppointment(Long appointmentId);

    /**
     * Obtiene las citas de un día específico.
     */
    List<Appointment> getDailyAgenda(LocalDate date);
}
