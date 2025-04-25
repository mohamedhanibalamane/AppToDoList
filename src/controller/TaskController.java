package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Task;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TaskController implements Initializable {

    @FXML private Button dashboardButton;
    @FXML private Button progressButton;
    @FXML private Button historyButton;
    @FXML private Button corbayButton;
    @FXML private Button tasksButton;
    @FXML private Button logoutButton;
    @FXML private Button backButton;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox tasksVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureNavigationButtons();
    }

    private void configureNavigationButtons() {
        dashboardButton.setOnAction(e -> loadView("/vue/Hoooome2.fxml"));
        progressButton.setOnAction(e -> loadView("/vue/Progres.fxml"));
        historyButton.setOnAction(e -> loadView("/vue/History.fxml"));
        corbayButton.setOnAction(e -> loadView("/vue/Corbay.fxml"));
        tasksButton.setOnAction(e -> loadView("/vue/Task.fxml"));
        backButton.setOnAction(e -> loadView("/vue/Hoooome2.fxml"));
        
        logoutButton.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/vue/Connection.fxml"));
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("To-Do List - Connexion");
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void loadView(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTasks(List<Task> tasks) {
        tasksVBox.getChildren().clear();
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Task task : tasks) {
            // Créer le conteneur principal de la tâche
            VBox taskContainer = new VBox();
            taskContainer.setStyle("-fx-background-color: #268396; -fx-padding: 20; -fx-background-radius: 10;");
            taskContainer.setSpacing(10);
            
            // Créer la ligne horizontale pour la tâche
            HBox taskHBox = new HBox();
            taskHBox.setSpacing(100);
            
            // Partie gauche (description et état)
            VBox leftVBox = new VBox();
            leftVBox.setSpacing(5);
            
            Button taskNameButton = new Button("• " + task.getDescription());
            taskNameButton.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-background-color: transparent; -fx-text-fill: white;");
            taskNameButton.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            
            Button stateButton = new Button(task.getState());
            stateButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18.0;");
            
            leftVBox.getChildren().addAll(taskNameButton, stateButton);
            
            // Partie droite (date et heure)
            VBox rightVBox = new VBox();
            rightVBox.setSpacing(5);
            rightVBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            
            Button dueDateButton = new Button("Due: " + task.getDueDate());
            dueDateButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            
            Button timeButton = new Button(task.getDueTime());
            timeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            
            rightVBox.getChildren().addAll(dueDateButton, timeButton);
            
            // Ajouter les deux parties à la HBox
            taskHBox.getChildren().addAll(leftVBox, rightVBox);
            
            // Ajouter la HBox à la VBox principale
            taskContainer.getChildren().add(taskHBox);
            
            // Ajouter la tâche à la liste
            tasksVBox.getChildren().add(taskContainer);
        }
    }
}