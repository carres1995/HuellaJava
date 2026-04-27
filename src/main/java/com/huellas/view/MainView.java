package com.huellas.view;

import com.huellas.controller.MedicalRecordController;
import com.huellas.controller.PetController;
import com.huellas.repository.jdbc.JdbcMedicalRecordRepository;
import com.huellas.repository.jdbc.JdbcPetRepository;
import com.huellas.repository.jdbc.JdbcUserRepository;
import com.huellas.service.impl.MedicalRecordServiceImpl;
import com.huellas.service.impl.PetServiceImpl;
import com.huellas.config.DatabaseInitializer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainView extends Application {

    private Stage mainStage;
    private JdbcUserRepository userRepo;
    private JdbcPetRepository petRepo;
    private JdbcMedicalRecordRepository medicalRepo;
    private com.huellas.repository.jdbc.JdbcAppointmentRepository appointmentRepo;

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        DatabaseInitializer.initialize();

        // Repositorios compartidos
        userRepo = new JdbcUserRepository();
        petRepo = new JdbcPetRepository();
        medicalRepo = new JdbcMedicalRecordRepository();
        appointmentRepo = new com.huellas.repository.jdbc.JdbcAppointmentRepository();

        // 1. Iniciar con Autenticación
        com.huellas.service.AuthService authService = new com.huellas.service.AuthService(userRepo);
        com.huellas.service.impl.UserServiceImpl userService = new com.huellas.service.impl.UserServiceImpl(userRepo);
        
        com.huellas.controller.AuthController authController = new com.huellas.controller.AuthController(authService, userService);
        
        com.huellas.view.AuthView authView = new com.huellas.view.AuthView(authController, loggedInUser -> {
            showDashboard(loggedInUser.getId());
        });

        Scene scene = new Scene(authView.getView(), 400, 550);
        mainStage.setTitle("Huellas App - Iniciar Sesión");
        mainStage.setScene(scene);
        mainStage.show();
    }

    private void showDashboard(Long loggedInUserId) {
        // 2. Crear Servicios
        PetServiceImpl petService = new PetServiceImpl(petRepo, userRepo);
        MedicalRecordServiceImpl medicalService = new MedicalRecordServiceImpl(medicalRepo, petRepo, userRepo, appointmentRepo);
        com.huellas.service.impl.AppointmentServiceImpl appointmentService = new com.huellas.service.impl.AppointmentServiceImpl(appointmentRepo, userRepo, petRepo);
        com.huellas.service.IntegralRegistrationService integralService = new com.huellas.service.IntegralRegistrationService(petRepo, appointmentRepo);
        
        // 3. Crear Controladores
        PetController petController = new PetController(petService);
        MedicalRecordController medicalController = new MedicalRecordController(medicalService);
        com.huellas.controller.AppointmentController appointmentController = new com.huellas.controller.AppointmentController(appointmentService);
        com.huellas.controller.IntegralController integralController = new com.huellas.controller.IntegralController(integralService);

        // 4. Crear Vistas
        PetView petView = new PetView(petController, loggedInUserId);
        MedicalRecordView medicalView = new MedicalRecordView(medicalController);
        AppointmentView appointmentView = new AppointmentView(appointmentController, loggedInUserId);
        IntegralView integralView = new IntegralView(integralController, loggedInUserId);

        // 5. Construir Dashboard
        TabPane tabPane = new TabPane();
        
        Tab tabIntegral = new Tab("1. Registro Integral", integralView.getView());
        tabIntegral.setClosable(false);
        
        Tab tabMascotas = new Tab("2. Mascota Sola", petView.getView());
        tabMascotas.setClosable(false);
        
        Tab tabCitas = new Tab("3. Agendar Citas", appointmentView.getView());
        tabCitas.setClosable(false);
        
        Tab tabHistorial = new Tab("4. Historial Clínico", medicalView.getView());
        tabHistorial.setClosable(false);

        tabPane.getTabs().addAll(tabIntegral, tabMascotas, tabCitas, tabHistorial);

        Scene scene = new Scene(tabPane, 650, 750);
        mainStage.setTitle("Huellas App - Panel Principal (Transaccional)");
        mainStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
