package com.huellas.view;

import com.huellas.controller.AppointmentController;
import com.huellas.model.Appointment;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentView {
    private final AppointmentController controller;
    private final Long userId;
    private VBox root;
    private TextArea outputArea;

    public AppointmentView(AppointmentController controller, Long userId) {
        this.controller = controller;
        this.userId = userId;
        buildUI();
    }

    private void buildUI() {
        root = new VBox(10);
        root.setPadding(new Insets(20));

        Label title = new Label("Agendar Cita Médica (MVC)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField txtPetId = new TextField();
        txtPetId.setPromptText("ID Mascota");

        TextField txtVetId = new TextField();
        txtVetId.setPromptText("ID Veterinario");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField txtHour = new TextField("09");
        txtHour.setPrefWidth(50);
        TextField txtMin = new TextField("00");
        txtMin.setPrefWidth(50);
        
        HBox timeBox = new HBox(5, new Label("Hora:"), txtHour, new Label(":"), txtMin);

        TextField txtDuration = new TextField("30");
        txtDuration.setPromptText("Duración (minutos)");

        TextArea txtNotes = new TextArea();
        txtNotes.setPromptText("Motivo de la consulta...");
        txtNotes.setPrefHeight(60);

        Button btnSchedule = new Button("Agendar Cita");
        Button btnList = new Button("Ver Citas de Hoy");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(150);

        btnSchedule.setOnAction(e -> {
            try {
                LocalDate date = datePicker.getValue();
                LocalTime startTime = LocalTime.of(Integer.parseInt(txtHour.getText()), Integer.parseInt(txtMin.getText()));
                LocalDateTime start = LocalDateTime.of(date, startTime);
                LocalDateTime end = start.plusMinutes(Long.parseLong(txtDuration.getText()));

                controller.agendarCita(this.userId, Long.parseLong(txtPetId.getText()), Long.parseLong(txtVetId.getText()), start, end, txtNotes.getText(), msg -> {
                    outputArea.appendText(msg + "\n");
                });
            } catch (Exception ex) {
                outputArea.appendText("Error en los datos: " + ex.getMessage() + "\n");
            }
        });

        btnList.setOnAction(e -> {
            outputArea.appendText("Citas para " + datePicker.getValue() + ":\n");
            controller.listarCitasPorFecha(datePicker.getValue(), 
                list -> {
                    if(list.isEmpty()) outputArea.appendText("No hay citas agendadas.\n");
                    else list.forEach(a -> outputArea.appendText("- [" + a.getStartTime().toLocalTime() + "] Mascota: " + a.getPetId() + " - " + a.getStatus() + "\n"));
                },
                error -> outputArea.appendText(error + "\n")
            );
        });

        root.getChildren().addAll(title, 
            new Label("IDs (Mascota / Vet):"), new HBox(5, txtPetId, txtVetId),
            new Label("Fecha y Hora:"), datePicker, timeBox,
            new Label("Duración (min):"), txtDuration,
            new Label("Notas:"), txtNotes,
            btnSchedule, btnList, new Label("Salida:"), outputArea);
    }

    public Parent getView() {
        return root;
    }
}
