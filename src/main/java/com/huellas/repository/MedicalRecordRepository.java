package com.huellas.repository;

import com.huellas.model.MedicalRecord;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository {
    Long save(MedicalRecord record) throws SQLException;
    List<MedicalRecord> findByPetId(Long petId) throws SQLException;
    Optional<MedicalRecord> findById(Long id) throws SQLException;
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId) throws SQLException;
}
