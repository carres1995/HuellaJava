package com.huellas.controller;

import com.huellas.model.Appointment;
import com.huellas.model.Pet;
import com.huellas.service.IntegralRegistrationService;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class IntegralController {
    private final IntegralRegistrationService integralRegistrationService;

    public IntegralController(IntegralRegistrationService integralRegistrationService) {
        this.integralRegistrationService = integralRegistrationService;
    }

    public void registrarMascotaYCita(Long userId, String petName, String species, String breed, 
                                      Long vetId, LocalDateTime start, LocalDateTime end, String notes, 
                                      Consumer<String> callback) {
        try {
            // Preparar mascota
            Pet pet = new Pet();
            pet.setUserId(userId);
            pet.setName(petName);
            pet.setSpecies(species);
            pet.setBreed(breed);
            pet.setActive(true);

            // Preparar cita
            Appointment app = new Appointment();
            app.setUserId(userId);
            app.setVeterinarianId(vetId);
            app.setStartTime(start);
            app.setEndTime(end);
            app.setNotes(notes);
            app.setStatus(com.huellas.model.Status.PENDING);

            // Ejecutar transacción
            IntegralRegistrationService.RegistroIntegralResult result = integralRegistrationService.registerPetWithAppointment(pet, app);
            
            callback.accept("¡Éxito! Mascota ID: " + result.petId + " y Cita ID: " + result.appointmentId + " registradas correctamente.");
        } catch (Exception e) {
            callback.accept("ERROR: " + e.getMessage());
        }
    }
}
