package com.huellas.repository;

import com.huellas.model.*;
import com.huellas.repository.jdbc.*;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Tests - JdbcMedicalRecordRepository")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JdbcMedicalRecordRepositoryIT {

    private static JdbcMedicalRecordRepository medicalRepo;
    private static JdbcUserRepository userRepo;
    private static JdbcPetRepository petRepo;
    private static JdbcAppointmentRepository appRepo;

    private static Long petId;
    private static Long vetProfessionalId;
    private static Long clientId;
    private static Long appointmentId;
    private static Long recordId;

    @BeforeAll
    static void setup() throws SQLException {
        medicalRepo = new JdbcMedicalRecordRepository();
        userRepo = new JdbcUserRepository();
        petRepo = new JdbcPetRepository();
        appRepo = new JdbcAppointmentRepository();

        // 1. Veterinario
        Veterinarian vet = new Veterinarian();
        vet.setName("Vet IT Med Final");
        vet.setEmail("vet_f_" + System.currentTimeMillis() + "@test.com");
        vet.setRole(Role.VETERINARIAN);
        vet.setActive(true);
        vet.setSpeciality("Cirugía");
        vet.setPhone("555-9999");
        vet.setAddress("Calle IT 10");
        vet.setPassword("12345");
        userRepo.save(vet);
        vetProfessionalId = vet.getVeterinarianId();

        // 2. Cliente
        Client client = new Client();
        client.setName("Cliente IT Med Final");
        client.setEmail("cli_f_" + System.currentTimeMillis() + "@test.com");
        client.setRole(Role.CLIENT);
        client.setActive(true);
        client.setPhone("555-8888");
        client.setAddress("Calle IT 20");
        client.setPassword("12345");
        clientId = userRepo.save(client);

        // 3. Mascota
        Pet pet = new Pet();
        pet.setName("Pacientito Final");
        pet.setSpecies("Perro");
        pet.setBirthDate(java.time.LocalDate.now().minusYears(2));
        pet.setUserId(clientId);
        pet.setActive(true);
        petId = petRepo.save(pet);

        // 4. Cita (Obligatoria para el historial)
        Appointment app = new Appointment();
        app.setUserId(clientId);
        app.setVeterinarianId(vetProfessionalId);
        app.setPetId(petId);
        app.setStartTime(LocalDateTime.now().plusDays(1));
        app.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        app.setStatus(Status.PENDING);
        app.setNotes("Cita previa IT");
        appointmentId = appRepo.save(app);
    }

    @Test
    @Order(1)
    @DisplayName("Debería persistir un registro médico real en Supabase")
    void shouldSaveMedicalRecord() throws SQLException {
        MedicalRecord record = new MedicalRecord();
        record.setAppointmentId(appointmentId); // Ahora sí lo enviamos
        record.setPetId(petId);
        record.setVetId(vetProfessionalId);
        record.setDiagnosis("Diagnóstico Final IT");
        record.setTreatment("Tratamiento Final IT");
        record.setVaccinesApplied("Ninguna");
        record.setNotes("Todo listo");

        recordId = medicalRepo.save(record);
        
        assertNotNull(recordId);
        System.out.println("✅ Historial clínico guardado exitosamente con ID: " + recordId);
    }

    @Test
    @Order(2)
    @DisplayName("Debería recuperar el historial de la mascota desde DB")
    void shouldFindHistoryByPet() throws SQLException {
        List<MedicalRecord> history = medicalRepo.findByPetId(petId);
        assertFalse(history.isEmpty());
        assertEquals("Diagnóstico Final IT", history.get(0).getDiagnosis());
    }
}
