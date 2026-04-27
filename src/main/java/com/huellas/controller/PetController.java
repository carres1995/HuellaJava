package com.huellas.controller;

import com.huellas.model.Pet;
import com.huellas.service.PetService;
import java.util.List;
import java.util.function.Consumer;

public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    public void registrarMascota(String nombre, String especie, String raza, Long userId, Consumer<String> callback) {
        try {
            Pet pet = new Pet();
            pet.setName(nombre);
            pet.setSpecies(especie);
            pet.setBreed(raza);
            pet.setUserId(userId);
            pet.setBirthDate(java.time.LocalDate.now());
            pet.setActive(true);

            Long id = petService.registerPet(pet);
            callback.accept("¡Éxito! Mascota registrada con ID: " + id);
        } catch (Exception e) {
            callback.accept("ERROR: " + e.getMessage());
        }
    }

    public void listarMascotas(Long userId, Consumer<List<Pet>> callback, Consumer<String> errorCallback) {
        try {
            List<Pet> pets = petService.getPetsByUserId(userId);
            callback.accept(pets);
        } catch (Exception e) {
            errorCallback.accept("ERROR al listar: " + e.getMessage());
        }
    }
}
