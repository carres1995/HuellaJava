package com.huellas.repository.jdbc;

import com.huellas.model.Pet;
import com.huellas.repository.BaseRepository;
import com.huellas.repository.PetRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de PetRepository.
 */
public class JdbcPetRepository extends BaseRepository implements PetRepository {

    private static final String INSERT_PET_SQL = 
        "INSERT INTO pets (name, species, breed, birth_date, active, user_id) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID_SQL = 
        "SELECT * FROM pets WHERE id = ?";

    private static final String FIND_BY_USER_ID_SQL = 
        "SELECT * FROM pets WHERE user_id = ? AND active = true";

    @Override
    public Long save(Pet pet) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PET_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getSpecies());
            stmt.setString(3, pet.getBreed());
            stmt.setDate(4, Date.valueOf(pet.getBirthDate()));
            stmt.setBoolean(5, pet.isActive());
            stmt.setLong(6, pet.getUserId());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    pet.setId(id);
                    return id;
                } else {
                    throw new SQLException("Error al obtener el ID generado de la mascota.");
                }
            }
        }
    }

    @Override
    public Long save(Pet pet, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_PET_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getSpecies());
            stmt.setString(3, pet.getBreed());
            stmt.setDate(4, pet.getBirthDate() != null ? Date.valueOf(pet.getBirthDate()) : null);
            stmt.setBoolean(5, pet.isActive());
            stmt.setLong(6, pet.getUserId());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    pet.setId(id);
                    return id;
                } else {
                    throw new SQLException("Error al obtener el ID generado de la mascota.");
                }
            }
        }
    }

    @Override
    public Optional<Pet> findById(Long id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPet(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Pet> findByUserId(Long userId) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USER_ID_SQL)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pets.add(mapResultSetToPet(rs));
                }
            }
        }
        return pets;
    }

    @Override
    public void update(Pet pet) throws SQLException {
        String sql = "UPDATE pets SET name = ?, species = ?, breed = ?, birth_date = ?, active = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getSpecies());
            stmt.setString(3, pet.getBreed());
            stmt.setDate(4, Date.valueOf(pet.getBirthDate()));
            stmt.setBoolean(5, pet.isActive());
            stmt.setLong(6, pet.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void updateActiveStatus(Long id, boolean active) throws SQLException {
        String sql = "UPDATE pets SET active = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    private Pet mapResultSetToPet(ResultSet rs) throws SQLException {
        Pet pet = new Pet();
        pet.setId(rs.getLong("id"));
        pet.setName(rs.getString("name"));
        pet.setSpecies(rs.getString("species"));
        pet.setBreed(rs.getString("breed"));
        
        Date dbDate = rs.getDate("birth_date");
        if (dbDate != null) {
            pet.setBirthDate(dbDate.toLocalDate());
        }
        
        pet.setActive(rs.getBoolean("active"));
        pet.setUserId(rs.getLong("user_id"));
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            pet.setCreatedAt(ts.toLocalDateTime());
        }
        return pet;
    }
}
