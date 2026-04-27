package com.huellas.view;

import com.huellas.controller.MedicalRecordController;
import com.huellas.model.MedicalRecord;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class MedicalRecordView {
    private final MedicalRecordController controller;
    private VBox root;
    private TextArea outputArea;

    public MedicalRecordView(MedicalRecordController controller) {
        this.controller = controller;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(10);
        root.setPadding(new Insets(20));

        Label title = new Label("Historial Médico (MVC)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtPetId = new TextField();
        txtPetId.setPromptText("ID Mascota");

        TextField txtVetId = new TextField();
        txtVetId.setPromptText("ID Veterinario");

        TextField txtAppointmentId = new TextField();
        txtAppointmentId.setPromptText("ID Cita (Opcional)");

        TextField txtDiagnosis = new TextField();
        txtDiagnosis.setPromptText("Diagnóstico");

        TextField txtTreatment = new TextField();
        txtTreatment.setPromptText("Tratamiento");

        TextField txtVaccines = new TextField();
        txtVaccines.setPromptText("Vacunas aplicadas");

        TextField txtNotes = new TextField();
        txtNotes.setPromptText("Notas adicionales");

        Button btnAdd = new Button("Registrar Historial");
        Button btnList = new Button("Ver Historial de Mascota");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(150);

        // Eventos delegados al controlador
        btnAdd.setOnAction(e -> {
            try {
                Long petId = Long.parseLong(txtPetId.getText());
                Long vetId = Long.parseLong(txtVetId.getText());
                Long appointmentId = txtAppointmentId.getText().isEmpty() ? null : Long.parseLong(txtAppointmentId.getText());

                controller.registrarHistorial(petId, vetId, appointmentId, txtDiagnosis.getText(), txtTreatment.getText(), txtNotes.getText(), txtVaccines.getText(), msg -> {
                    outputArea.appendText(msg + "\n");
                    txtDiagnosis.clear();
                    txtTreatment.clear();
                    txtVaccines.clear();
                    txtNotes.clear();
                });
            } catch (NumberFormatException ex) {
                outputArea.appendText("Error: IDs deben ser numéricos.\n");
            }
        });

        btnList.setOnAction(e -> {
            try {
                Long petId = Long.parseLong(txtPetId.getText());
                outputArea.appendText("Consultando...\n");
                controller.listarHistorialPorMascota(petId, 
                    records -> {
                        if(records.isEmpty()) outputArea.appendText("No hay historiales para esta mascota.\n");
                        else records.forEach(r -> outputArea.appendText("- [" + r.getCreatedAt().toLocalDate() + "] " + r.getDiagnosis() + "\n"));
                    }, 
                    error -> outputArea.appendText(error + "\n")
                );
            } catch (NumberFormatException ex) {
                outputArea.appendText("Error: Ingresa un ID de mascota válido para buscar.\n");
            }
        });

        root.getChildren().addAll(title, 
            new Label("IDs:"), txtPetId, txtVetId, txtAppointmentId,
            new Label("Detalles Clínicos:"), txtDiagnosis, txtTreatment, txtVaccines, txtNotes,
            btnAdd, btnList, new Label("Resultados:"), outputArea);
    }

    public Parent getView() {
        return root;
    }
}
