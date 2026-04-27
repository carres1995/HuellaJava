package com.huellas.service;

import com.huellas.exception.ServiceException;
import com.huellas.exception.ValidationException;
import com.huellas.model.Client;
import com.huellas.model.Role;
import com.huellas.repository.UserRepository;
import com.huellas.service.impl.UserServiceImpl;
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
@DisplayName("Unit Tests - UserService")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    @DisplayName("BR-003: Debería lanzar excepción si el email ya existe")
    void shouldThrowExceptionWhenEmailAlreadyExists() throws SQLException {
        // Arrange
        String email = "duplicate@test.com";
        Client user = createValidClient(email);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        ValidationException ex = assertThrows(ValidationException.class, () -> userService.registerUser(user));
        assertTrue(ex.getMessage().contains("ya está registrado"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-002: Debería lanzar excepción si el formato del email es inválido")
    void shouldThrowExceptionWhenEmailFormatIsInvalid() {
        // Arrange
        Client user = createValidClient("email-invalido");

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.registerUser(user));
    }

    @Test
    @DisplayName("BR-001: Debería lanzar excepción si el nombre es nulo o vacío")
    void shouldThrowExceptionWhenNameIsEmpty() {
        // Arrange
        Client user = createValidClient("test@test.com");
        user.setName("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.registerUser(user));
    }

    @Test
    @DisplayName("Debería registrar exitosamente y hashear la contraseña")
    void shouldRegisterUserSuccessfully() throws SQLException {
        // Arrange
        String email = "new@test.com";
        String rawPassword = "password123";
        Client user = createValidClient(email);
        user.setPassword(rawPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(1L);

        // Act
        Long id = userService.registerUser(user);

        // Assert
        assertEquals(1L, id);
        assertNotEquals(rawPassword, user.getPassword(), "La contraseña debería estar hasheada");
        assertTrue(user.isActive(), "El usuario debería estar activo por defecto");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Debería lanzar ServiceException ante un error de SQL")
    void shouldThrowServiceExceptionOnSqlError() throws SQLException {
        // Arrange
        String email = "error@test.com";
        Client user = createValidClient(email);

        when(userRepository.findByEmail(email)).thenThrow(new SQLException("DB Error"));

        // Act & Assert
        assertThrows(ServiceException.class, () -> userService.registerUser(user));
    }

    // Helper para crear clientes válidos
    private Client createValidClient(String email) {
        Client client = new Client();
        client.setName("Juan Perez");
        client.setEmail(email);
        client.setPassword("password123");
        client.setRole(Role.CLIENT);
        client.setPhone("1234567");
        client.setAddress("Calle Falsa 123");
        return client;
    }
}
