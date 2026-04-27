package com.huellas.controller;

import com.huellas.model.Appointment;
import com.huellas.model.Status;
import com.huellas.service.AppointmentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public void agendarCita(Long userId, Long petId, Long vetId, LocalDateTime start, LocalDateTime end, String notes, Consumer<String> callback) {
        try {
            Appointment app = new Appointment();
            app.setUserId(userId);
            app.setPetId(petId);
            app.setVeterinarianId(vetId);
            app.setStartTime(start);
            app.setEndTime(end);
            app.setNotes(notes);
            app.setStatus(Status.PENDING);

            Long id = appointmentService.scheduleAppointment(app);
            callback.accept("¡Éxito! Cita agendada con ID: " + id);
        } catch (Exception e) {
            callback.accept("ERROR: " + e.getMessage());
        }
    }

    public void listarCitasPorFecha(java.time.LocalDate date, Consumer<List<Appointment>> callback, Consumer<String> errorCallback) {
        try {
            List<Appointment> list = appointmentService.getDailyAgenda(date);
            callback.accept(list);
        } catch (Exception e) {
            errorCallback.accept("Error al listar citas: " + e.getMessage());
        }
    }

    public void cancelarCita(Long id, Consumer<String> callback) {
        try {
            appointmentService.cancelAppointment(id);
            callback.accept("Cita ID " + id + " cancelada.");
        } catch (Exception e) {
            callback.accept("ERROR: " + e.getMessage());
        }
    }
}
