package com.huellas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Pet {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private LocalDate birthDate;
    private boolean active;
    private Long userId; 
    private LocalDateTime createdAt;

    public Pet() {
        this.createdAt = LocalDateTime.now();
    }

    public Pet(Long id, String name, String species, String breed, LocalDate birthDate, boolean active, Long userId) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birthDate = birthDate;
        this.active = active;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
