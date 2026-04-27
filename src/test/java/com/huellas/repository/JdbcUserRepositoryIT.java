package com.huellas.repository;

import com.huellas.model.Role;
import com.huellas.model.User;
import com.huellas.model.Veterinarian;
import com.huellas.repository.jdbc.JdbcUserRepository;
import com.huellas.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de Integración - JdbcUserRepository")
public class JdbcUserRepositoryIT {

    @Test
    @DisplayName("Debería guardar y recuperar un Veterinario correctamente")
    void shouldSaveAndFindVeterinarian() throws SQLException {
        JdbcUserRepository repo = new JdbcUserRepository();

        // 1. Preparar datos
        String email = "house_" + System.currentTimeMillis() + "@huellas.com"; // Email único para cada ejecución
        Veterinarian vet = new Veterinarian();
        vet.setName("Dr. Gregory House");
        vet.setEmail(email);
        vet.setPhone("555-9876");
        vet.setAddress("Princeton Plainsboro 123");
        vet.setPassword(PasswordUtil.hash("diagnostico123"));
        vet.setActive(true);
        vet.setRole(Role.VETERINARIAN);
        vet.setSpeciality("Diagnóstico Médico");

        // 2. Ejecutar Guardado
        System.out.println("Ejecutando guardado en DB...");
        Long id = repo.save(vet);

        // 3. Verificaciones
        assertNotNull(id, "El ID generado no debe ser nulo");
        System.out.println("✅ Usuario guardado con ID: " + id);

        Optional<User> found = repo.findByEmail(email);
        assertTrue(found.isPresent(), "El usuario debería existir en la DB");
        
        User user = found.get();
        assertEquals(vet.getName(), user.getName());
        assertEquals(Role.VETERINARIAN, user.getRole());
        
        if (user instanceof Veterinarian recoveredVet) {
            assertEquals("Diagnóstico Médico", recoveredVet.getSpeciality());
            System.out.println("✅ Especialidad verificada: " + recoveredVet.getSpeciality());
        } else {
            fail("El usuario recuperado debería ser instancia de Veterinarian");
        }
    }
}
