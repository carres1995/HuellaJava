package com.huellas.repository.jdbc;

import com.huellas.model.Client;
import com.huellas.model.Role;
import com.huellas.model.User;
import com.huellas.model.Veterinarian;
import com.huellas.repository.BaseRepository;
import com.huellas.repository.UserRepository;

import java.sql.*;
import java.util.Optional;

/**
 * Implementación JDBC de UserRepository.
 * Maneja persistencia en tablas 'users' y 'veterinarians' con transacciones.
 */
public final class JdbcUserRepository extends BaseRepository implements UserRepository {

    // --- Consultas SQL como Constantes ---
    private static final String FIND_BY_EMAIL_SQL = 
        "SELECT u.*, v.id as vet_id, v.speciality " +
        "FROM users u " +
        "LEFT JOIN veterinarians v ON u.id = v.user_id " +
        "WHERE u.email = ?";

    private static final String FIND_BY_ID_SQL = 
        "SELECT u.*, v.id as vet_id, v.speciality " +
        "FROM users u " +
        "LEFT JOIN veterinarians v ON u.id = v.user_id " +
        "WHERE u.id = ?";

    private static final String INSERT_USER_SQL = 
        "INSERT INTO users (name, email, phone, address, password, active, role) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?::role_t)";

    private static final String INSERT_VETERINARIAN_SQL = 
        "INSERT INTO veterinarians (user_id, speciality) VALUES (?, ?)";

    private static final String UPDATE_STATUS_SQL = 
        "UPDATE users SET active = ? WHERE id = ?";

    @Override
    public Optional<User> findById(Long id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_EMAIL_SQL)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Long save(User user) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Iniciamos Transacción

            Long userId;
            // 1. Insertar en tabla base 'users'
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPhone());
                stmt.setString(4, user.getAddress());
                stmt.setString(5, user.getPassword());
                stmt.setBoolean(6, user.isActive());
                stmt.setString(7, user.getRole().name());

                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getLong(1);
                        user.setId(userId);
                    } else {
                        throw new SQLException("Error al obtener el ID generado del usuario.");
                    }
                }
            }

            // 2. Si es Veterinario, insertar en tabla extendida 'veterinarians'
            if (user instanceof Veterinarian vet) {
                try (PreparedStatement stmt = conn.prepareStatement(INSERT_VETERINARIAN_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setLong(1, userId);
                    stmt.setString(2, vet.getSpeciality());
                    stmt.executeUpdate();
                    
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            vet.setVeterinarianId(rs.getLong(1));
                        }
                    }
                }
            }

            conn.commit(); // Todo salió bien, confirmamos cambios
            return userId;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Error detectado, deshacemos todo
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public void updateActiveStatus(Long id, boolean active) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS_SQL)) {
            stmt.setBoolean(1, active);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role").toUpperCase());
        User user;

        if (role == Role.VETERINARIAN) {
            Veterinarian vet = new Veterinarian();
            vet.setSpeciality(rs.getString("speciality"));
            vet.setVeterinarianId(rs.getObject("vet_id") != null ? rs.getLong("vet_id") : null);
            user = vet;
        } else {
            user = new Client();
        }

        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setPassword(rs.getString("password"));
        user.setActive(rs.getBoolean("active"));
        user.setRole(role);
        
        if (rs.getTimestamp("created_at") != null) {
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        return user;
    }
}
