package com.huellas.view;

import com.huellas.controller.IntegralController;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class IntegralView {
    private final IntegralController controller;
    private final Long userId;
    private VBox root;
    private TextArea outputArea;

    public IntegralView(IntegralController controller, Long userId) {
        this.controller = controller;
        this.userId = userId;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(10);
        root.setPadding(new Insets(20));

        Label title = new Label("Registro Integral (Mascota + Cita) - SPEC-005");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Datos de Mascota
        Label lblPet = new Label("1. Datos de la nueva Mascota:");
        lblPet.setStyle("-fx-font-weight: bold;");
        TextField txtPetName = new TextField();
        txtPetName.setPromptText("Nombre de la Mascota");
        TextField txtSpecies = new TextField();
        txtSpecies.setPromptText("Especie (ej. Perro, Gato)");
        TextField txtBreed = new TextField();
        txtBreed.setPromptText("Raza");

        // Datos de Cita
        Label lblAppt = new Label("2. Datos de su Primera Cita:");
        lblAppt.setStyle("-fx-font-weight: bold;");
        TextField txtVetId = new TextField();
        txtVetId.setPromptText("ID Veterinario");
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField txtHour = new TextField("09");
        txtHour.setPrefWidth(40);
        TextField txtMin = new TextField("00");
        txtMin.setPrefWidth(40);
        HBox timeBox = new HBox(5, new Label("Hora:"), txtHour, new Label(":"), txtMin);
        
        TextField txtDuration = new TextField("30");
        txtDuration.setPromptText("Duración (min)");

        TextArea txtNotes = new TextArea();
        txtNotes.setPromptText("Motivo de consulta...");
        txtNotes.setPrefHeight(50);

        Button btnRegister = new Button("Ejecutar Transacción Integral");
        btnRegister.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(100);

        btnRegister.setOnAction(e -> {
            try {
                Long vetId = Long.parseLong(txtVetId.getText());
                LocalDate date = datePicker.getValue();
                LocalTime startTime = LocalTime.of(Integer.parseInt(txtHour.getText()), Integer.parseInt(txtMin.getText()));
                LocalDateTime start = LocalDateTime.of(date, startTime);
                LocalDateTime end = start.plusMinutes(Long.parseLong(txtDuration.getText()));

                controller.registrarMascotaYCita(
                    this.userId, txtPetName.getText(), txtSpecies.getText(), txtBreed.getText(),
                    vetId, start, end, txtNotes.getText(),
                    msg -> outputArea.appendText(msg + "\n")
                );
            } catch (Exception ex) {
                outputArea.appendText("Error en los datos de entrada: " + ex.getMessage() + "\n");
            }
        });

        root.getChildren().addAll(
            title, 
            lblPet, txtPetName, txtSpecies, txtBreed,
            lblAppt, txtVetId, datePicker, timeBox, txtDuration, txtNotes,
            btnRegister, 
            new Label("Resultado Transacción:"), outputArea
        );
    }

    public Parent getView() {
        return root;
    }
}
