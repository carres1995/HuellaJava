package com.huellas.repository;

import com.huellas.model.*;
import com.huellas.repository.jdbc.JdbcAppointmentRepository;
import com.huellas.repository.jdbc.JdbcPetRepository;
import com.huellas.repository.jdbc.JdbcUserRepository;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Tests - JdbcAppointmentRepository")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JdbcAppointmentRepositoryIT {

    private static JdbcAppointmentRepository appRepo;
    private static JdbcUserRepository userRepo;
    private static JdbcPetRepository petRepo;
    
    private static Long clientId;
    private static Veterinarian testVet; 
    private static Long petId;
    private static Long appointmentId;

    @BeforeAll
    static void setup() throws SQLException {
        appRepo = new JdbcAppointmentRepository();
        userRepo = new JdbcUserRepository();
        petRepo = new JdbcPetRepository();

        // 1. Crear Cliente
        Client client = new Client();
        client.setName("Cliente Citas");
        client.setEmail("client_it_" + System.currentTimeMillis() + "@test.com");
        client.setPhone("555-0001");
        client.setAddress("Calle 1");
        client.setPassword(com.huellas.util.PasswordUtil.hash("12345678"));
        client.setRole(Role.CLIENT);
        client.setActive(true);
        clientId = userRepo.save(client);

        // 2. Crear Veterinario
        testVet = new Veterinarian();
        testVet.setName("Vet Citas Hash");
        testVet.setEmail("vet_hash_" + System.currentTimeMillis() + "@test.com");
        testVet.setPhone("555-0002");
        testVet.setAddress("Clinica 1");
        testVet.setPassword(com.huellas.util.PasswordUtil.hash("12345678"));
        testVet.setRole(Role.VETERINARIAN);
        testVet.setActive(true);
        testVet.setSpeciality("Cirugía");
        userRepo.save(testVet);

        // 3. Crear Mascota
        Pet pet = new Pet();
        pet.setName("Rex IT");
        pet.setSpecies("Perro");
        pet.setBirthDate(java.time.LocalDate.now().minusYears(1));
        pet.setUserId(clientId);
        pet.setActive(true);
        petId = petRepo.save(pet);
    }

    @Test
    @Order(1)
    @DisplayName("Debería guardar una cita médica")
    void shouldSaveAppointment() throws SQLException {
        Appointment app = new Appointment();
        app.setUserId(clientId);
        app.setVeterinarianId(testVet.getVeterinarianId());
        app.setPetId(petId);
        app.setStartTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(0));
        app.setEndTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));
        app.setStatus(Status.PENDING);
        app.setNotes("Consulta de rutina IT");

        appointmentId = appRepo.save(app);
        assertNotNull(appointmentId);
        System.out.println("✅ Cita agendada con ID: " + appointmentId);
    }

    @Test
    @Order(2)
    @DisplayName("Debería detectar el solapamiento de horario")
    void shouldDetectOverlapping() throws SQLException {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(9).withMinute(30);
        LocalDateTime end = LocalDateTime.now().plusDays(2).withHour(10).withMinute(30);

        int count = appRepo.countOverlapping(testVet.getVeterinarianId(), start, end);
        assertTrue(count > 0, "Debería haber detectado al menos un solapamiento");
        System.out.println("✅ Solapamiento detectado correctamente");
    }

    @Test
    @Order(3)
    @DisplayName("Debería actualizar el estado de la cita")
    void shouldUpdateStatus() throws SQLException {
        appRepo.updateStatus(appointmentId, Status.CONFIRMED);
        
        Optional<Appointment> app = appRepo.findById(appointmentId);
        assertTrue(app.isPresent());
        assertEquals(Status.CONFIRMED, app.get().getStatus());
        System.out.println("✅ Cita confirmada correctamente");
    }
}
