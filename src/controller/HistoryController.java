package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import model.Task;
import model.TaskDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox historyVBox;
    @FXML private Button dashboardButton;
    @FXML private Button progressButton;
    @FXML private Button historyButton;
    
    @FXML private Button tasksButton;
    @FXML private Button logoutButton;
    @FXML private Button backButton;

    private String userEmail;
    private String userName;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }


    public void loadHistory(String email, String name) {
        this.userEmail = email;
        this.userName = name; // Stockez le nom
        refreshHistory();
    }

    private void refreshHistory() {
        try {
            List<Task> historyTasks = TaskDAO.getUserHistory(userEmail);
            historyVBox.getChildren().clear();
            
            for (Task task : historyTasks) {
                // Créer l'affichage de la tâche historique
                VBox taskBox = createHistoryTaskBox(task);
                historyVBox.getChildren().add(taskBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private VBox createHistoryTaskBox(Task task) {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: #268396; -fx-padding: 12; -fx-background-radius: 10;");

        Label titleLabel = new Label("• " + task.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");

        Label paragraphLabel = new Label(task.getParagraph());
        paragraphLabel.setWrapText(true);
        paragraphLabel.setStyle("-fx-text-fill: white;");

        Label startLabel = new Label("Début : " + task.getStartDate() + " " + task.getStartTime());
        startLabel.setStyle("-fx-text-fill: white;");

        Label endLabel = new Label("Fin : " + task.getDueDate() + " " + task.getDueTime());
        endLabel.setStyle("-fx-text-fill: white;");

        Label typeLabel = new Label("Type : " + task.getType());
        typeLabel.setStyle("-fx-text-fill: white;");

        Label priorityLabel = new Label("Priorité : " + task.getPriority());
        priorityLabel.setStyle("-fx-text-fill: white;");

        Label stateLabel = new Label("État : " + task.getState());
        stateLabel.setStyle("-fx-text-fill: " + 
            (task.getState().equals("Terminer") ? "#4CAF50" : "#FF4444") + ";");

        box.getChildren().addAll(
            titleLabel,
            paragraphLabel,
            startLabel,
            endLabel,
            typeLabel,
            priorityLabel,
            stateLabel
        );

        return box;
    }
     
    @FXML
    private void handleDashboard(ActionEvent event) {
        loadView("/vue/Hoooome2.fxml");
    }

    @FXML
    private void handleProgress(ActionEvent event) {
        loadView("/vue/Progres.fxml");
    }

    @FXML
    private void handleHistory(ActionEvent event) {
        loadView("/vue/History.fxml");
    }

    

    @FXML
    private void handleTasks(ActionEvent event) {
        loadView("/vue/Task.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/vue/Connection.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("To-Do List - Connexion");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de déconnexion: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        loadView("/vue/Hoooome2.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (loader.getController() instanceof HomeController) {
                HomeController controller = (HomeController) loader.getController();
                controller.initUserData(userEmail, userName);
            } else if (loader.getController() instanceof TaskController) {
                TaskController controller = (TaskController) loader.getController();
                controller.initUserData(userEmail, userName);
            } else if (loader.getController() instanceof HistoryController) {
                HistoryController controller = (HistoryController) loader.getController();
                controller.loadHistory(userEmail, userName);
            } else if (loader.getController() instanceof ProgressController) {
                ProgressController controller = (ProgressController) loader.getController();
                controller.loadProgress(userEmail, userName);
            }
            
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur de chargement de la vue: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}