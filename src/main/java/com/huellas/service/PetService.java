package com.huellas.service;

import com.huellas.model.Pet;
import java.util.List;

/**
 * Servicio para la gestión de mascotas.
 * Referencia: SPEC-003
 */
public interface PetService {

    /**
     * Registra una nueva mascota vinculada a un cliente.
     * @param pet La mascota a registrar
     * @return El ID generado
     */
    Long registerPet(Pet pet);

    /**
     * Obtiene la lista de mascotas activas de un cliente.
     */
    List<Pet> getPetsByUserId(Long userId);

    /**
     * Actualiza la información de una mascota.
     */
    void updatePetInfo(Pet pet);

    /**
     * Desactiva una mascota (borrado lógico).
     */
    void deactivatePet(Long petId);
}
