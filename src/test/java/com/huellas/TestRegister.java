package com.huellas;

import com.huellas.model.Client;
import com.huellas.model.Role;
import com.huellas.repository.jdbc.JdbcUserRepository;
import org.junit.jupiter.api.Test;

public class TestRegister {
    @Test
    public void testRegistration() {
        try {
            JdbcUserRepository repo = new JdbcUserRepository();
            Client user = new Client();
            user.setName("carlos");
            user.setEmail("andres@gmail.com");
            user.setPhone("1234567890");
            user.setAddress("Test 123");
            user.setPassword("password");
            user.setRole(Role.CLIENT);
            user.setActive(true);
            
            repo.save(user);
            System.out.println("Registro exitoso!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
