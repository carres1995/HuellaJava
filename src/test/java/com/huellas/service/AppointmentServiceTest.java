package com.huellas.service;

import com.huellas.exception.AppointmentException;
import com.huellas.model.*;
import com.huellas.repository.AppointmentRepository;
import com.huellas.repository.PetRepository;
import com.huellas.repository.UserRepository;
import com.huellas.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests - AppointmentService")
public class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepo;
    @Mock private UserRepository userRepo;
    @Mock private PetRepository petRepo;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl(appointmentRepo, userRepo, petRepo);
    }

    @Test
    @DisplayName("Debería agendar una cita exitosamente si no hay conflictos")
    void shouldScheduleAppointmentSuccessfully() throws SQLException {
        // Arrange
        Appointment app = createValidAppointment();
        
        when(userRepo.findById(app.getUserId())).thenReturn(Optional.of(createActiveUser(Role.CLIENT)));
        when(userRepo.findById(app.getVeterinarianId())).thenReturn(Optional.of(createActiveUser(Role.VETERINARIAN)));
        when(petRepo.findById(app.getPetId())).thenReturn(Optional.of(createActivePet()));
        when(appointmentRepo.countOverlapping(anyLong(), any(), any())).thenReturn(0);
        when(appointmentRepo.save(any())).thenReturn(1L);

        // Act
        Long id = appointmentService.scheduleAppointment(app);

        // Assert
        assertEquals(1L, id);
        assertEquals(Status.PENDING, app.getStatus());
        verify(appointmentRepo).save(app);
    }

    @Test
    @DisplayName("BR-006: Debería lanzar excepción por solapamiento de horario")
    void shouldThrowExceptionWhenOverlapping() throws SQLException {
        // Arrange
        Appointment app = createValidAppointment();
        
        when(userRepo.findById(anyLong())).thenReturn(Optional.of(createActiveUser(Role.CLIENT)));
        when(petRepo.findById(anyLong())).thenReturn(Optional.of(createActivePet()));
        // Simulamos que ya hay 1 cita en ese rango
        when(appointmentRepo.countOverlapping(anyLong(), any(), any())).thenReturn(1);

        // Act & Assert
        AppointmentException ex = assertThrows(AppointmentException.class, 
            () -> appointmentService.scheduleAppointment(app));
        assertTrue(ex.getMessage().contains("ya tiene una cita"));
    }

    @Test
    @DisplayName("BR-003: Debería lanzar excepción si la cita es en el pasado")
    void shouldThrowExceptionWhenStartTimeInPast() {
        // Arrange
        Appointment app = createValidAppointment();
        app.setStartTime(LocalDateTime.now().minusHours(1));

        // Act & Assert
        assertThrows(AppointmentException.class, () -> appointmentService.scheduleAppointment(app));
    }

    @Test
    @DisplayName("BR-007: Debería lanzar excepción al cancelar una cita DONE")
    void shouldThrowExceptionWhenCancellingDoneAppointment() throws SQLException {
        // Arrange
        Appointment app = new Appointment();
        app.setStatus(Status.DONE);
        when(appointmentRepo.findById(1L)).thenReturn(Optional.of(app));

        // Act & Assert
        assertThrows(AppointmentException.class, () -> appointmentService.cancelAppointment(1L));
    }

    // Helpers
    private Appointment createValidAppointment() {
        Appointment a = new Appointment();
        a.setUserId(1L);
        a.setVeterinarianId(2L);
        a.setPetId(3L);
        a.setStartTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        a.setEndTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0));
        return a;
    }

    private User createActiveUser(Role role) {
        Client u = new Client();
        u.setActive(true);
        u.setRole(role);
        return u;
    }

    private Pet createActivePet() {
        Pet p = new Pet();
        p.setActive(true);
        return p;
    }
}
