package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

public class ControlerA implements Initializable {

    // Navigation buttons
    @FXML private Button dashboardButton;
    @FXML private Button progressButton;
    @FXML private Button teamButton;
    @FXML private Button historyButton;
    @FXML private Button corboyButton;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;

    // Task statistics labels
    @FXML private Label yourTasksLabel;
    @FXML private Label tasksDoneLabel;
    @FXML private Label tasksFailedLabel;
    @FXML private Label inProgressLabel;

    // Table and columns
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
    @FXML private TableColumn<Task, String> statusColumn;

    // Task input fields
    @FXML private TextField titleField;
    @FXML private TextField descriptionField;
    @FXML private ChoiceBox<String> statusChoice;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize ChoiceBox
        statusChoice.setItems(FXCollections.observableArrayList("To Do", "In Progress", "Done"));
        statusChoice.setValue("To Do");

        // Set up table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    private void handleAddTask(ActionEvent event) {
        String title = titleField.getText();
        String description = descriptionField.getText();
        String status = statusChoice.getValue();

        if (!title.isEmpty() && !description.isEmpty()) {
            Task newTask = new Task(title, description, status);
            taskTable.getItems().add(newTask);
            titleField.clear();
            descriptionField.clear();
            statusChoice.setValue("To Do");

            // Update stats
            yourTasksLabel.setText(String.valueOf(taskTable.getItems().size()));
        }
    }

    @FXML
    private void handleDashboardButton(ActionEvent event) {
        System.out.println("Dashboard clicked!");
    }

    @FXML
    private void handleProgressButton(ActionEvent event) {
        System.out.println("Progress clicked!");
    }

    @FXML
    private void handleTeamButton(ActionEvent event) {
        System.out.println("Team clicked!");
    }

    @FXML
    private void handleHistoryButton(ActionEvent event) {
        System.out.println("History clicked!");
    }

    @FXML
    private void handleCorboyButton(ActionEvent event) {
        System.out.println("Corboy clicked!");
    }

    @FXML
    private void handleSettingsButton(ActionEvent event) {
        System.out.println("Settings clicked!");
    }

    @FXML
    private void handleLogoutButton(ActionEvent event) {
        System.out.println("Logout clicked!");
    }
}

// === Task model class ===
class Task {
    private String title;
    private String description;
    private String status;

    public Task(String title, String description, String status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
}
