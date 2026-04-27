package com.huellas.view;

import com.huellas.controller.AuthController;
import com.huellas.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class AuthView {
    private final AuthController controller;
    private final Consumer<User> onLoginSuccess;
    private TabPane root;

    public AuthView(AuthController controller, Consumer<User> onLoginSuccess) {
        this.controller = controller;
        this.onLoginSuccess = onLoginSuccess;
        buildUI();
    }

    private void buildUI() {
        root = new TabPane();
        
        Tab tabLogin = new Tab("Iniciar Sesión", buildLoginUI());
        tabLogin.setClosable(false);
        
        Tab tabRegister = new Tab("Registrarse", buildRegisterUI());
        tabRegister.setClosable(false);
        
        root.getTabs().addAll(tabLogin, tabRegister);
    }

    private VBox buildLoginUI() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label title = new Label("Bienvenido a Huellas");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Correo electrónico");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");

        Button btnLogin = new Button("Ingresar");
        Label lblMessage = new Label();
        lblMessage.setStyle("-fx-text-fill: red;");

        btnLogin.setOnAction(e -> {
            controller.login(txtEmail.getText(), txtPassword.getText(), 
                user -> {
                    lblMessage.setStyle("-fx-text-fill: green;");
                    lblMessage.setText("¡Bienvenido, " + user.getName() + "!");
                    onLoginSuccess.accept(user); // Redirige al Dashboard
                }, 
                error -> {
                    lblMessage.setStyle("-fx-text-fill: red;");
                    lblMessage.setText(error);
                }
            );
        });

        vbox.getChildren().addAll(title, txtEmail, txtPassword, btnLogin, lblMessage);
        return vbox;
    }

    private VBox buildRegisterUI() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label title = new Label("Crear Cuenta Nueva");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtName = new TextField();
        txtName.setPromptText("Nombre Completo");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Correo electrónico");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");

        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("CLIENT", "VETERINARIAN", "ADMIN");
        cbRole.setValue("CLIENT");

        Button btnRegister = new Button("Registrarse");
        Label lblMessage = new Label();

        btnRegister.setOnAction(e -> {
            controller.register(txtName.getText(), txtEmail.getText(), txtPassword.getText(), cbRole.getValue(), 
                success -> {
                    lblMessage.setStyle("-fx-text-fill: green;");
                    lblMessage.setText(success);
                    txtName.clear();
                    txtEmail.clear();
                    txtPassword.clear();
                }, 
                error -> {
                    lblMessage.setStyle("-fx-text-fill: red;");
                    lblMessage.setText(error);
                }
            );
        });

        vbox.getChildren().addAll(title, txtName, txtEmail, txtPassword, cbRole, btnRegister, lblMessage);
        return vbox;
    }

    public Parent getView() {
        return root;
    }
}
