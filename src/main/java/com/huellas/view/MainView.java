package com.huellas.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import com.huellas.config.DatabaseInitializer;

/**
 * Main entry point for the JavaFX desktop application.
 *
 * <p>This View layer ONLY renders UI and captures user input.
 * All logic is delegated to Controller classes.</p>
 *
 * <p>Note: Refer to CONSTITUTION.md §2.6 — Presentation Layer</p>
 */
public class MainView extends Application {

    // TODO: Inject your Controller here
    // private final [Entity]Controller controller;

    @Override
    public void start(Stage stage) {
        // Initialize database schema
        DatabaseInitializer.initialize();

        Label label = new Label("🚀 huellas — SDD + JavaFX");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("huellas");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
