package com.huellas.service.impl;

import com.huellas.exception.PetException;
import com.huellas.exception.ServiceException;
import com.huellas.model.Pet;
import com.huellas.repository.PetRepository;
import com.huellas.repository.UserRepository;
import com.huellas.service.PetService;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementación de PetService.
 * Gestiona la lógica de negocio para las mascotas.
 */
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public PetServiceImpl(PetRepository petRepository, UserRepository userRepository) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Long registerPet(Pet pet) {
        try {
            // 1. Validaciones básicas (BR-001, BR-002, BR-005)
            validatePetFields(pet);

            // 2. Verificar que el dueño existe y es válido (BR-003)
            userRepository.findById(pet.getUserId()).ifPresentOrElse(owner -> {
                if (!owner.isActive()) {
                    throw new PetException("El dueño especificado no existe o está inactivo");
                }
            }, () -> {
                throw new PetException("El dueño especificado no existe o está inactivo");
            });

            // 3. Forzar estado activo inicial (BR-004)
            pet.setActive(true);

            // 4. Guardar
            return petRepository.save(pet);

        } catch (SQLException e) {
            throw new ServiceException("Error al registrar la mascota en la base de datos", e);
        }
    }

    @Override
    public List<Pet> getPetsByUserId(Long userId) {
        try {
            return petRepository.findByUserId(userId);
        } catch (SQLException e) {
            throw new ServiceException("Error al obtener las mascotas del usuario", e);
        }
    }

    @Override
    public void updatePetInfo(Pet pet) {
        try {
            validatePetFields(pet);
            petRepository.update(pet);
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar la mascota", e);
        }
    }

    @Override
    public void deactivatePet(Long petId) {
        try {
            petRepository.updateActiveStatus(petId, false);
        } catch (SQLException e) {
            throw new ServiceException("Error al desactivar la mascota", e);
        }
    }

    private void validatePetFields(Pet pet) {
        if (pet.getName() == null || pet.getName().trim().isEmpty()) {
            throw new PetException("El nombre de la mascota es obligatorio");
        }
        if (pet.getSpecies() == null || pet.getSpecies().trim().isEmpty()) {
            throw new PetException("La especie es obligatoria");
        }
        if (pet.getUserId() == null) {
            throw new PetException("La mascota debe estar vinculada a un dueño");
        }
        if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(java.time.LocalDate.now())) {
            throw new PetException("La fecha de nacimiento no puede ser futura");
        }
    }
}
