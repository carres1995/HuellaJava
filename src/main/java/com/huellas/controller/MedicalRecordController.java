package com.huellas.controller;

import com.huellas.model.MedicalRecord;
import com.huellas.service.MedicalRecordService;
import java.util.List;
import java.util.function.Consumer;

public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    public void registrarHistorial(Long petId, Long vetId, Long appointmentId, String diagnosis, String treatment, String notes, String vaccinesApplied, Consumer<String> callback) {
        try {
            MedicalRecord record = new MedicalRecord();
            record.setPetId(petId);
            record.setVetId(vetId);
            record.setAppointmentId(appointmentId);
            record.setDiagnosis(diagnosis);
            record.setTreatment(treatment);
            record.setNotes(notes);
            record.setVaccinesApplied(vaccinesApplied);

            Long id = medicalRecordService.addMedicalRecord(record);
            callback.accept("¡Éxito! Historial registrado con ID: " + id);
        } catch (Exception e) {
            callback.accept("ERROR: " + e.getMessage());
        }
    }

    public void listarHistorialPorMascota(Long petId, Consumer<List<MedicalRecord>> callback, Consumer<String> errorCallback) {
        try {
            List<MedicalRecord> records = medicalRecordService.getPetHistory(petId);
            callback.accept(records);
        } catch (Exception e) {
            errorCallback.accept("ERROR al listar: " + e.getMessage());
        }
    }
}
