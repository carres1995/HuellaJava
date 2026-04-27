package com.huellas.repository.jdbc;

import com.huellas.model.Appointment;
import com.huellas.model.Status;
import com.huellas.repository.AppointmentRepository;
import com.huellas.repository.BaseRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de AppointmentRepository.
 */
public class JdbcAppointmentRepository extends BaseRepository implements AppointmentRepository {

    private static final String INSERT_APPOINTMENT_SQL = 
        "INSERT INTO appointments (user_id, veterinarian_id, pet_id, start_time, end_time, status, notes) " +
        "VALUES (?, ?, ?, ?, ?, ?::status_t, ?)";

    private static final String COUNT_OVERLAPPING_SQL = 
        "SELECT COUNT(*) FROM appointments " +
        "WHERE veterinarian_id = ? " +
        "AND status NOT IN ('CANCELLED') " +
        "AND start_time < ? " +
        "AND end_time > ?";

    @Override
    public Long save(Appointment appointment) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_APPOINTMENT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, appointment.getUserId());
            stmt.setLong(2, appointment.getVeterinarianId());
            stmt.setLong(3, appointment.getPetId());
            stmt.setTimestamp(4, Timestamp.valueOf(appointment.getStartTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(appointment.getEndTime()));
            stmt.setString(6, appointment.getStatus().name());
            stmt.setString(7, appointment.getNotes());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    appointment.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Error al crear la cita");
    }

    @Override
    public Long save(Appointment appointment, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_APPOINTMENT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, appointment.getUserId());
            stmt.setLong(2, appointment.getVeterinarianId());
            stmt.setLong(3, appointment.getPetId());
            stmt.setTimestamp(4, Timestamp.valueOf(appointment.getStartTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(appointment.getEndTime()));
            stmt.setString(6, appointment.getStatus().name());
            stmt.setString(7, appointment.getNotes());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    appointment.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Error al crear la cita transaccional");
    }

    @Override
    public Optional<Appointment> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public int countOverlapping(Long veterinarianId, LocalDateTime start, LocalDateTime end) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_OVERLAPPING_SQL)) {
            
            stmt.setLong(1, veterinarianId);
            stmt.setTimestamp(2, Timestamp.valueOf(end));   // newEndTime
            stmt.setTimestamp(3, Timestamp.valueOf(start)); // newStartTime

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public void updateStatus(Long id, Status status) throws SQLException {
        String sql = "UPDATE appointments SET status = ?::status_t WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE DATE(start_time) = ? ORDER BY start_time";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        }
        return list;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setVeterinarianId(rs.getLong("veterinarian_id"));
        a.setPetId(rs.getLong("pet_id"));
        a.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        a.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        a.setStatus(Status.valueOf(rs.getString("status").toUpperCase()));
        a.setNotes(rs.getString("notes"));
        a.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return a;
    }
}
