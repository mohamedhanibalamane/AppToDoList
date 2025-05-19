package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.TaskDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ProgressController implements Initializable {

    @FXML private Button allTaskButton;
    @FXML private Button allDoneButton;
    @FXML private Button allFailedButton;
    @FXML private Button allCoinsButton;
    @FXML private Button allDoneButtonpo;  // Pour les tâches terminées
    @FXML private Button allFailedButtonpo; // Pour les tâches échouées
    @FXML private Button monthTaskButton;
    @FXML private Button monthDoneButton;
    @FXML private Button monthFailedButton;
    @FXML private Button monthCoinsButton;
    @FXML private Button monthTypeButton;
    @FXML private Button monthButton;
    @FXML private Circle progressCircleDone;
    @FXML private Circle progressCircleFailed;
    
    @FXML private Button dashboardButton;
    @FXML private Button progressButton;
    @FXML private Button historyButton;
    
    @FXML private Button tasksButton;
    @FXML private Button logoutButton;
    @FXML private Button backButton;
    
    private String userEmail;
    private String userName;
    private LocalDate currentDate = LocalDate.now();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le bouton du mois avec le mois actuel
        monthButton.setText(currentDate.getMonth().toString());
    }

    public void loadProgress(String email, String name) {
        this.userEmail = email;
        this.userName = name;
        refreshProgress();
    }

    private void refreshProgress() {
        try {
            // Statistiques globales (toutes les tâches dans l'historique)
            int totalTasks = TaskDAO.getTotalHistoryCount(userEmail);
            int totalDone = TaskDAO.getTotalHistoryCount(userEmail, "Terminer");
            int totalFailed = TaskDAO.getTotalHistoryCount(userEmail, "Échoué");
            int totalCoins = (totalDone * 20) - (totalFailed * 30);
            
            // Calcul des pourcentages
            double donePercentage = totalTasks > 0 ? (double) totalDone / totalTasks * 100 : 0;
            double failedPercentage = totalTasks > 0 ? (double) totalFailed / totalTasks * 100 : 0;
            
            // Mise à jour des boutons globaux
            allTaskButton.setText(String.valueOf(totalTasks));
            allDoneButton.setText(String.format("%.0f%%", donePercentage));
            allFailedButton.setText(String.format("%.0f%%", failedPercentage));
            allCoinsButton.setText(String.valueOf(totalCoins) + "c");
            
            // Mise à jour des nouveaux boutons (valeurs absolues)
            allDoneButtonpo.setText(String.valueOf(totalDone));       // Nombre total de tâches terminées
            allFailedButtonpo.setText(String.valueOf(totalFailed));   // Nombre total de tâches échouées
            
            // Statistiques mensuelles
            int currentMonth = currentDate.getMonthValue();
            int currentYear = currentDate.getYear();
            
            int monthTasks = TaskDAO.getHistoryCountForMonth(userEmail, currentMonth, currentYear);
            int monthDone = TaskDAO.getHistoryCountForMonth(userEmail, "Terminer", currentMonth, currentYear);
            int monthFailed = TaskDAO.getHistoryCountForMonth(userEmail, "Échoué", currentMonth, currentYear);
            int monthCoins = (monthDone * 20) - (monthFailed * 30);
            
            monthTaskButton.setText(String.valueOf(monthTasks));
            monthDoneButton.setText(String.valueOf(monthDone));
            monthFailedButton.setText(String.valueOf(monthFailed));
            monthCoinsButton.setText(String.valueOf(monthCoins) + "c");
            
            // Type le plus échoué le mois dernier
            String mostFailedType = TaskDAO.getMostFailedTaskTypeLastMonth(userEmail);
            monthTypeButton.setText(mostFailedType != null ? mostFailedType : "None");
            
            // Mise à jour des cercles de progression
            progressCircleDone.setStrokeDashOffset(452.16 - (452.16 * donePercentage / 100));
            progressCircleFailed.setStrokeDashOffset(452.16 - (452.16 * failedPercentage / 100));
            
        } catch (SQLException e) {
            showError("Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
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