package com.huellas.service.impl;

import com.huellas.exception.MedicalException;
import com.huellas.exception.ServiceException;
import com.huellas.model.MedicalRecord;
import com.huellas.repository.MedicalRecordRepository;
import com.huellas.repository.PetRepository;
import com.huellas.repository.UserRepository;
import com.huellas.service.MedicalRecordService;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementación de la lógica de negocio para el Historial Clínico.
 * Paso 6 del flujo de entidad.
 */
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final com.huellas.repository.AppointmentRepository appointmentRepository;

    public MedicalRecordServiceImpl(MedicalRecordRepository medicalRecordRepository, 
                                    PetRepository petRepository, 
                                    UserRepository userRepository,
                                    com.huellas.repository.AppointmentRepository appointmentRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public Long addMedicalRecord(MedicalRecord record) {
        try {
            // 1. Validaciones básicas
            validateRecord(record);
            if (record.getAppointmentId() == null) {
                throw new MedicalException("El ID de la cita es obligatorio");
            }

            // 2. BR-006, BR-009: Validar Cita
            com.huellas.model.Appointment app = appointmentRepository.findById(record.getAppointmentId())
                .orElseThrow(() -> new MedicalException("La cita especificada no existe"));
            
            if (app.getStatus() != com.huellas.model.Status.DONE) {
                throw new MedicalException("Solo se puede registrar historial en citas completadas");
            }
            if (!app.getPetId().equals(record.getPetId()) || !app.getVeterinarianId().equals(record.getVetId())) {
                throw new MedicalException("Los datos de la cita no coinciden con el registro");
            }

            // 3. BR-008: Un solo registro por cita
            if (medicalRecordRepository.findByAppointmentId(record.getAppointmentId()).isPresent()) {
                throw new MedicalException("Esta cita ya tiene un registro médico asociado");
            }

            // 4. Regla de Negocio: La mascota debe existir
            petRepository.findById(record.getPetId()).orElseThrow(() -> 
                new MedicalException("No se puede registrar historial: La mascota no existe"));

            // 5. Regla de Negocio: El veterinario debe existir
            userRepository.findById(record.getVetId()).orElseThrow(() -> 
                new MedicalException("No se puede registrar historial: El veterinario no existe"));

            // 6. Persistencia
            return medicalRecordRepository.save(record);

        } catch (SQLException e) {
            throw new ServiceException("Error técnico al intentar guardar el historial médico", e);
        }
    }

    @Override
    public List<MedicalRecord> getPetHistory(Long petId) {
        try {
            return medicalRecordRepository.findByPetId(petId);
        } catch (SQLException e) {
            throw new ServiceException("Error al recuperar el historial de la mascota", e);
        }
    }

    @Override
    public MedicalRecord getRecordDetails(Long id) {
        try {
            return medicalRecordRepository.findById(id).orElseThrow(() -> 
                new MedicalException("El registro médico solicitado no existe"));
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar el detalle del registro", e);
        }
    }

    private void validateRecord(MedicalRecord r) {
        if (r.getPetId() == null) {
            throw new MedicalException("El ID de la mascota es obligatorio");
        }
        if (r.getVetId() == null) {
            throw new MedicalException("El ID del veterinario es obligatorio");
        }
        if (r.getDiagnosis() == null || r.getDiagnosis().trim().isEmpty()) {
            throw new MedicalException("El diagnóstico es obligatorio para el registro clínico");
        }
        if (r.getTreatment() == null || r.getTreatment().trim().isEmpty()) {
            throw new MedicalException("El tratamiento médico es obligatorio");
        }
    }
}
