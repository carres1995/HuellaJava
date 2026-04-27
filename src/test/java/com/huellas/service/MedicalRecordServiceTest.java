package com.huellas.service;

import com.huellas.exception.MedicalException;
import com.huellas.model.MedicalRecord;
import com.huellas.model.Pet;
import com.huellas.model.User;
import com.huellas.model.Role;
import com.huellas.model.Client;
import com.huellas.repository.MedicalRecordRepository;
import com.huellas.repository.PetRepository;
import com.huellas.repository.UserRepository;
import com.huellas.service.impl.MedicalRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests - MedicalRecordService")
public class MedicalRecordServiceTest {

    @Mock private MedicalRecordRepository medicalRepo;
    @Mock private PetRepository petRepo;
    @Mock private UserRepository userRepo;
    @Mock private com.huellas.repository.AppointmentRepository appointmentRepo;

    private MedicalRecordService medicalService;

    @BeforeEach
    void setUp() {
        medicalService = new MedicalRecordServiceImpl(medicalRepo, petRepo, userRepo, appointmentRepo);
    }

    @Test
    @DisplayName("Debería registrar historial clínico exitosamente")
    void shouldAddMedicalRecordSuccessfully() throws SQLException {
        // Arrange (Preparar)
        MedicalRecord record = createValidRecord();
        
        com.huellas.model.Appointment app = new com.huellas.model.Appointment();
        app.setStatus(com.huellas.model.Status.DONE);
        app.setPetId(1L);
        app.setVeterinarianId(2L);

        when(appointmentRepo.findById(5L)).thenReturn(Optional.of(app));
        when(medicalRepo.findByAppointmentId(5L)).thenReturn(Optional.empty());
        when(petRepo.findById(1L)).thenReturn(Optional.of(new Pet()));
        when(userRepo.findById(2L)).thenReturn(Optional.of(new Client()));
        when(medicalRepo.save(any())).thenReturn(100L);

        // Act (Actuar)
        Long id = medicalService.addMedicalRecord(record);

        // Assert (Verificar)
        assertEquals(100L, id);
        verify(medicalRepo).save(record);
    }

    @Test
    @DisplayName("Debería fallar si el diagnóstico está vacío")
    void shouldFailWhenDiagnosisIsEmpty() {
        // Arrange
        MedicalRecord record = createValidRecord();
        record.setDiagnosis("");

        // Act & Assert
        assertThrows(MedicalException.class, () -> medicalService.addMedicalRecord(record));
        verifyNoInteractions(medicalRepo);
    }

    @Test
    @DisplayName("Debería fallar si la mascota no existe")
    void shouldFailWhenPetNotFound() throws SQLException {
        // Arrange
        MedicalRecord record = createValidRecord();
        when(petRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        MedicalException ex = assertThrows(MedicalException.class, 
            () -> medicalService.addMedicalRecord(record));
        
        assertTrue(ex.getMessage().contains("La mascota no existe"));
    }

    private MedicalRecord createValidRecord() {
        MedicalRecord r = new MedicalRecord();
        r.setAppointmentId(5L);
        r.setPetId(1L);
        r.setVetId(2L);
        r.setDiagnosis("Gripe canina");
        r.setTreatment("Reposo y vitaminas");
        return r;
    }
}
