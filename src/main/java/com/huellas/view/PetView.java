package com.huellas.view;

import com.huellas.controller.PetController;
import com.huellas.model.Pet;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class PetView {
    private final PetController controller;
    private final Long userId;
    private VBox root;
    private TextArea outputArea;

    public PetView(PetController controller, Long userId) {
        this.controller = controller;
        this.userId = userId;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(10);
        root.setPadding(new Insets(20));

        Label title = new Label("Gestión de Mascotas (MVC)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtName = new TextField();
        txtName.setPromptText("Nombre de la mascota");

        TextField txtSpecies = new TextField();
        txtSpecies.setPromptText("Especie (Perro, Gato...)");

        TextField txtBreed = new TextField();
        txtBreed.setPromptText("Raza");

        Button btnAdd = new Button("Registrar Mascota");
        Button btnList = new Button("Ver Mis Mascotas");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(150);

        // Eventos delegados al controlador
        btnAdd.setOnAction(e -> {
            controller.registrarMascota(txtName.getText(), txtSpecies.getText(), txtBreed.getText(), this.userId, msg -> {
                outputArea.appendText(msg + "\n");
                txtName.clear();
                txtSpecies.clear();
                txtBreed.clear();
            });
        });

        btnList.setOnAction(e -> {
            outputArea.appendText("Consultando...\n");
            controller.listarMascotas(this.userId, 
                pets -> {
                    if(pets.isEmpty()) outputArea.appendText("No hay mascotas.\n");
                    else pets.forEach(p -> outputArea.appendText("- " + p.getName() + " [" + p.getBreed() + "]\n"));
                }, 
                error -> outputArea.appendText(error + "\n")
            );
        });

        root.getChildren().addAll(title, 
            new Label("Nombre:"), txtName, 
            new Label("Especie:"), txtSpecies, 
            new Label("Raza:"), txtBreed,
            btnAdd, btnList, new Label("Resultados:"), outputArea);
    }

    public Parent getView() {
        return root;
    }
}
