package com.huellas.controller;

import com.huellas.model.User;
import com.huellas.model.Role;
import com.huellas.model.Client;
import com.huellas.model.Veterinarian;
import com.huellas.service.AuthService;
import com.huellas.service.UserService;

import java.util.function.Consumer;

public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    public void login(String email, String password, Consumer<User> onSuccess, Consumer<String> onError) {
        try {
            User user = authService.login(email, password);
            onSuccess.accept(user);
        } catch (Exception e) {
            onError.accept("Error de Autenticación: " + e.getMessage());
        }
    }

    public void register(String name, String email, String password, String roleStr, Consumer<String> onSuccess, Consumer<String> onError) {
        try {
            Role role = Role.valueOf(roleStr.toUpperCase());
            User user;

            // No podemos hacer 'new User()' porque es abstracta. 
            // Usamos las clases hijas permitidas: Client o Veterinarian.
            if (role == Role.VETERINARIAN) {
                user = new Veterinarian();
            } else {
                user = new Client();
            }

            user.setName(name);
            user.setEmail(email);
            user.setPhone("Sin especificar");
            user.setAddress("Sin especificar");
            user.setPassword(password);
            user.setRole(role);
            user.setActive(true);

            Long id = userService.registerUser(user);
            onSuccess.accept("¡Registro exitoso! Ya puedes iniciar sesión.");
        } catch (Exception e) {
            onError.accept("Error al registrar: " + e.getMessage());
        }
    }
}
