package com.huellas.model;

import java.time.LocalDateTime;

public class MedicalRecord {
    private Long id;
    private Long appointmentId;
    private Long petId;
    private Long vetId;
    private String diagnosis;
    private String treatment;
    private String notes;
    private String vaccinesApplied;
    private LocalDateTime createdAt;

    public MedicalRecord() {
        this.createdAt = LocalDateTime.now();
    }

    public MedicalRecord(Long id, Long appointmentId, Long petId, Long vetId, String diagnosis, String treatment, String vaccinesApplied, String notes) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.petId = petId;
        this.vetId = vetId;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.vaccinesApplied = vaccinesApplied;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public Long getVetId() {
        return vetId;
    }

    public void setVetId(Long vetId) {
        this.vetId = vetId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getVaccinesApplied() {
        return vaccinesApplied;
    }

    public void setVaccinesApplied(String vaccinesApplied) {
        this.vaccinesApplied = vaccinesApplied;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
