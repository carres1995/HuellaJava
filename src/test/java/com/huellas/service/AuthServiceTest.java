package com.huellas.service;

import com.huellas.exception.AuthException;
import com.huellas.model.Client;
import com.huellas.model.Role;
import com.huellas.model.User;
import com.huellas.repository.UserRepository;
import com.huellas.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Pruebas de Reglas de Negocio (SPEC-001)")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    // =====================================================================
    // ESCENARIO 1: HAPPY PATH
    // =====================================================================
    @Test
    @DisplayName("Escenario 1: Inicio de sesión exitoso con credenciales válidas — BR-006")
    void shouldLoginSuccessfullyWithValidCredentials() throws Exception {
        // GIVEN
        String email = "test@example.com";
        String password = "password123";
        String hashedPassword = PasswordUtil.hash(password);
        
        Client mockUser = new Client();
        mockUser.setId(1L);
        mockUser.setName("Test User");
        mockUser.setEmail(email);
        mockUser.setPhone("1234567");
        mockUser.setAddress("Address");
        mockUser.setPassword(hashedPassword);
        mockUser.setActive(true);
        mockUser.setRole(Role.CLIENT);

        try {
            doReturn(Optional.of(mockUser)).when(userRepository).findByEmail(email);
        } catch (SQLException e) {
            fail("Error en el setup del mock");
        }

        // WHEN
        User result = authService.login(email, password);

        // THEN
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(Role.CLIENT, result.getRole());
    }

    // =====================================================================
    // ESCENARIO 2: EMAIL VACÍO
    // =====================================================================
    @Test
    @DisplayName("Escenario 2: Debería fallar si el email es nulo o vacío — BR-001")
    void shouldThrowExceptionWhenEmailIsInvalid() throws Exception {
        // WHEN + THEN
        AuthException thrown = assertThrows(AuthException.class, 
            () -> authService.login("", "password123"));
        
        assertTrue(thrown.getMessage().contains("El email es obligatorio"));
    }

    // =====================================================================
    // ESCENARIO 3: PASSWORD VACÍO
    // =====================================================================
    @Test
    @DisplayName("Escenario 3: Debería fallar si la contraseña es nula o vacía — BR-002")
    void shouldThrowExceptionWhenPasswordIsInvalid() throws Exception {
        // WHEN + THEN
        AuthException thrown = assertThrows(AuthException.class, 
            () -> authService.login("test@example.com", ""));
        
        assertTrue(thrown.getMessage().contains("La contraseña es obligatoria"));
    }

    // =====================================================================
    // ESCENARIO 4: EMAIL NO EXISTE
    // =====================================================================
    @Test
    @DisplayName("Escenario 4: Debería fallar si el email no existe en la BD — BR-003")
    void shouldThrowExceptionWhenEmailNotFound() throws Exception {
        // GIVEN
        String email = "notfound@example.com";
        try {
            doReturn(Optional.empty()).when(userRepository).findByEmail(email);
        } catch (SQLException e) {
            fail("Error en el setup del mock");
        }

        // WHEN + THEN
        AuthException thrown = assertThrows(AuthException.class, 
            () -> authService.login(email, "password123"));
        
        assertTrue(thrown.getMessage().contains("Credenciales incorrectas"));
    }

    // =====================================================================
    // ESCENARIO 5: CONTRASEÑA INCORRECTA
    // =====================================================================
    @Test
    @DisplayName("Escenario 5: Debería fallar si la contraseña no coincide — BR-004")
    void shouldThrowExceptionWhenPasswordIncorrect() throws Exception {
        // GIVEN
        String email = "test@example.com";
        String correctPassword = "password123";
        String wrongPassword = "wrongPassword";
        String hashedCorrectPassword = PasswordUtil.hash(correctPassword);

        Client mockUser = new Client();
        mockUser.setId(1L);
        mockUser.setName("Test User");
        mockUser.setEmail(email);
        mockUser.setPhone("1234567");
        mockUser.setAddress("Address");
        mockUser.setPassword(hashedCorrectPassword);
        mockUser.setActive(true);
        mockUser.setRole(Role.CLIENT);

        try {
            doReturn(Optional.of(mockUser)).when(userRepository).findByEmail(email);
        } catch (SQLException e) {
            fail("Error en el setup del mock");
        }

        // WHEN + THEN
        AuthException thrown = assertThrows(AuthException.class, 
            () -> authService.login(email, wrongPassword));
        
        assertTrue(thrown.getMessage().contains("Credenciales incorrectas"));
    }

    // =====================================================================
    // ESCENARIO 6: USUARIO INACTIVO
    // =====================================================================
    @Test
    @DisplayName("Escenario 6: Debería fallar si el usuario está inactivo — BR-005")
    void shouldThrowExceptionWhenUserIsInactive() throws Exception {
        // GIVEN
        String email = "inactive@example.com";
        String password = "password123";
        String hashedPassword = PasswordUtil.hash(password);

        // active = false
        Client mockUser = new Client();
        mockUser.setId(1L);
        mockUser.setName("Inactive User");
        mockUser.setEmail(email);
        mockUser.setPhone("1234567");
        mockUser.setAddress("Address");
        mockUser.setPassword(hashedPassword);
        mockUser.setActive(false);
        mockUser.setRole(Role.CLIENT);

        try {
            doReturn(Optional.of(mockUser)).when(userRepository).findByEmail(email);
        } catch (SQLException e) {
            fail("Error en el setup del mock");
        }

        // WHEN + THEN
        AuthException thrown = assertThrows(AuthException.class, 
            () -> authService.login(email, password));
        
        assertTrue(thrown.getMessage().contains("Usuario inactivo"));
    }
}
