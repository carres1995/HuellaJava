package com.huellas.service;

import com.huellas.model.MedicalRecord;
import java.util.List;

/**
 * Servicio para la gestión de historiales clínicos.
 */
public interface MedicalRecordService {

    /**
     * Registra una nueva consulta médica.
     * @param record Datos de la consulta.
     * @return ID generado.
     */
    Long addMedicalRecord(MedicalRecord record);

    /**
     * Recupera todas las consultas de una mascota.
     * @param petId ID de la mascota.
     * @return Lista de historiales.
     */
    List<MedicalRecord> getPetHistory(Long petId);

    /**
     * Busca un registro médico por su ID.
     */
    MedicalRecord getRecordDetails(Long id);
}
