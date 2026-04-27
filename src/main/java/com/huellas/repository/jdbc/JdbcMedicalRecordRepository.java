package com.huellas.repository.jdbc;

import com.huellas.model.MedicalRecord;
import com.huellas.repository.BaseRepository;
import com.huellas.repository.MedicalRecordRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC para el Historial Clínico.
 * Paso 4 del flujo de entidad.
 */
public class JdbcMedicalRecordRepository extends BaseRepository implements MedicalRecordRepository {

    private static final String INSERT_SQL = 
        "INSERT INTO medical_records (appointment_id, pet_id, veterinarian_id, diagnosis, treatment, vaccines_applied, notes) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_PET_SQL = 
        "SELECT * FROM medical_records WHERE pet_id = ? ORDER BY recorded_at DESC";
    
    private static final String FIND_BY_ID_SQL =    
        "SELECT * FROM medical_records WHERE id = ?";    

    @Override
    public Long save(MedicalRecord record) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setObject(1, record.getAppointmentId()); 
            stmt.setLong(2, record.getPetId());
            stmt.setLong(3, record.getVetId());
            stmt.setString(4, record.getDiagnosis());
            stmt.setString(5, record.getTreatment());
            stmt.setString(6, record.getVaccinesApplied());
            stmt.setString(7, record.getNotes());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    record.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Error al guardar el registro médico");
    }

    @Override
    public List<MedicalRecord> findByPetId(Long petId) throws SQLException {
        List<MedicalRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_PET_SQL)) {
            stmt.setLong(1, petId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToRecord(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Optional<MedicalRecord> findById(Long id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToRecord(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<MedicalRecord> findByAppointmentId(Long appointmentId) throws SQLException {
        String sql = "SELECT * FROM medical_records WHERE appointment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToRecord(rs));
                }
            }
        }
        return Optional.empty();
    }

    private MedicalRecord mapToRecord(ResultSet rs) throws SQLException {
        MedicalRecord r = new MedicalRecord();
        r.setId(rs.getLong("id"));
        r.setAppointmentId(rs.getObject("appointment_id") != null ? rs.getLong("appointment_id") : null);
        r.setPetId(rs.getLong("pet_id"));
        r.setVetId(rs.getLong("veterinarian_id"));
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setTreatment(rs.getString("treatment"));
        r.setVaccinesApplied(rs.getString("vaccines_applied"));
        r.setNotes(rs.getString("notes"));
        r.setCreatedAt(rs.getTimestamp("recorded_at").toLocalDateTime()); 
        return r;
    }
}
