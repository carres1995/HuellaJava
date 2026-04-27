package com.huellas.model;

public final class Veterinarian extends User {
    private Long veterinarianId; // ID específico en la tabla veterinarians
    private String speciality;

    public Veterinarian() {}

    public Veterinarian(Long id, String name, String email, String phone, String address, String password, boolean active, Role role, String speciality) {
        super(id, name, email, phone, address, password, active, role);
        this.speciality = speciality;
    }

    public Long getVeterinarianId() {
        return veterinarianId;
    }

    public void setVeterinarianId(Long veterinarianId) {
        this.veterinarianId = veterinarianId;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }
}
