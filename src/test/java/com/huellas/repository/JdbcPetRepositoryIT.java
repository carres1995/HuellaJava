package com.huellas.repository;

import com.huellas.model.Client;
import com.huellas.model.Pet;
import com.huellas.model.Role;
import com.huellas.repository.jdbc.JdbcPetRepository;
import com.huellas.repository.jdbc.JdbcUserRepository;
import com.huellas.util.PasswordUtil;

import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Tests - JdbcPetRepository")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JdbcPetRepositoryIT {

    private static JdbcPetRepository petRepo;
    private static JdbcUserRepository userRepo;
    private static Long testUserId;
    private static Long testPetId;

    @BeforeAll
    static void setup() throws SQLException {
        petRepo = new JdbcPetRepository();
        userRepo = new JdbcUserRepository();
        
        // Creamos un dueño para las pruebas
        Client owner = new Client();
        owner.setName("Dueño de Prueba");
        owner.setEmail("petowner_" + System.currentTimeMillis() + "@test.com");
        owner.setPhone("555-1234");
        owner.setAddress("Calle del Test 456");
        owner.setPassword(PasswordUtil.hash("12345678"));
        owner.setRole(Role.CLIENT);
        owner.setActive(true);
        testUserId = userRepo.save(owner);
    }

    @Test
    @Order(1)
    @DisplayName("Debería guardar una mascota correctamente")
    void shouldSavePet() throws SQLException {
        Pet pet = new Pet();
        pet.setName("Firulais");
        pet.setSpecies("Perro");
        pet.setBreed("Labrador");
        pet.setBirthDate(LocalDate.now().minusYears(2));
        pet.setActive(true);
        pet.setUserId(testUserId);

        testPetId = petRepo.save(pet);
        assertNotNull(testPetId);
        System.out.println("✅ Mascota guardada con ID: " + testPetId);
    }

    @Test
    @Order(2)
    @DisplayName("Debería encontrar mascotas por el ID del usuario")
    void shouldFindPetsByUser() throws SQLException {
        List<Pet> pets = petRepo.findByUserId(testUserId);
        assertFalse(pets.isEmpty());
        assertEquals("Firulais", pets.get(0).getName());
    }

    @Test
    @Order(3)
    @DisplayName("Debería desactivar una mascota (borrado lógico)")
    void shouldDeactivatePet() throws SQLException {
        petRepo.updateActiveStatus(testPetId, false);
        
        var pet = petRepo.findById(testPetId);
        assertTrue(pet.isPresent());
        assertFalse(pet.get().isActive(), "La mascota debería estar inactiva");
        System.out.println("✅ Mascota desactivada correctamente");
    }
}
