package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Task;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import model.TaskDAO;


public class TaskController implements Initializable {

    // Déclaration des composants FXML
    @FXML private Button dashboardButton;
    @FXML private Button progressButton;
    @FXML private Button historyButton;
   
    @FXML private Button tasksButton;
    @FXML private Button logoutButton;
    @FXML private Button backButton;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox tasksVBox;
    @FXML private ChoiceBox<String> typeFilter;
    @FXML private ChoiceBox<String> priorityFilter;
    @FXML private TextField titleFilter;
    
    
    private String userEmail;
    private String userName;
    
    private List<Task> allTasks = new ArrayList<>();

    // Méthode d'initialisation appelée automatiquement
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Étape 1: Configurer les boutons de navigation
        configureNavigationButtons();
        
        // Étape 2: Initialiser les filtres
        initializeFilters();
    }

    // Étape 3: Initialisation des filtres
    private void initializeFilters() {
        // Configuration du filtre par type
        typeFilter.getItems().addAll("Tous", "Sport", "Education", "Religion", "Work", 
                                   "General Culture", "Daily Activity", "Other");
        typeFilter.setValue("Tous");
        
        // Configuration du filtre par priorité
        priorityFilter.getItems().addAll("Tous", "High", "Medium", "Low");
        priorityFilter.setValue("Tous");
        
        // Ajout des écouteurs pour les filtres
        typeFilter.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> filterTasks());
        priorityFilter.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> filterTasks());
        titleFilter.textProperty().addListener(
            (obs, oldVal, newVal) -> filterTasks());
    }

    // Étape 4: Configuration des boutons de navigation
    private void configureNavigationButtons() {
        // Bouton Dashboard
        dashboardButton.setOnAction(e -> loadView("/vue/Hoooome2.fxml"));
        
        // Bouton Progres
        progressButton.setOnAction(e -> loadView("/vue/Progres.fxml"));
        
        // Bouton History
        historyButton.setOnAction(e -> loadView("/vue/History.fxml"));
        
        
        // Bouton Tasks (recharge la vue actuelle)
        tasksButton.setOnAction(e -> loadView("/vue/Task.fxml"));
        
        // Bouton Retour
        backButton.setOnAction(e -> loadView("/vue/Hoooome2.fxml"));
        
        // Bouton Logout
        logoutButton.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/vue/Connection.fxml"));
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("To-Do List - Connexion");
                stage.show();
            } catch (IOException ex) {
                showError("Erreur de déconnexion: " + ex.getMessage());
            }
        });
    }

    // Étape 5: Méthode pour charger une vue
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


    // Étape 6: Initialisation des données utilisateur
    public void initUserData(String email, String name) {
        this.userEmail = email;
        this.userName = name;
        try {
            allTasks = TaskDAO.getUserTasks(userEmail);
            updateTasks(allTasks);
        } catch (SQLException e) {
            showError("Erreur de chargement des tâches: " + e.getMessage());
        }
    }



    // Étape 7: Mise à jour de l'affichage des tâches
    public void updateTasks(List<Task> tasks) {
        tasksVBox.getChildren().clear();
        
        for (Task task : tasks) {
            // Création du conteneur de tâche
            VBox taskContainer = createTaskContainer(task);
            tasksVBox.getChildren().add(taskContainer);
        }
    }

    // Étape 8: Création d'un conteneur de tâche
    private VBox createTaskContainer(Task task) {
        VBox container = new VBox(10);
        container.setStyle("-fx-background-color: #268396; -fx-padding: 20; -fx-background-radius: 10;");
        
        // Titre et état
        HBox titleBox = new HBox(10);
        Label titleLabel = new Label("• " + task.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: black;");
        
        Label stateLabel = new Label(task.getState());
        String stateColor = task.getState().equals("Terminer") ? "#4CAF50" : "#FF4444";
        stateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + stateColor + ";");
        
        titleBox.getChildren().addAll(titleLabel, stateLabel);
        
        // Description
        Label descLabel = new Label(task.getParagraph());
        descLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");
        descLabel.setWrapText(true);
        
        // Dates
        HBox datesBox = new HBox(20);
        datesBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label startLabel = new Label("Début: " + task.getStartDate() + " " + task.getStartTime());
        startLabel.setStyle("-fx-text-fill: black;");
        
        Label endLabel = new Label("Fin: " + task.getDueDate() + " " + task.getDueTime());
        endLabel.setStyle("-fx-text-fill: black;");
        
        datesBox.getChildren().addAll(startLabel, endLabel);
        
        // Type et priorité
        HBox detailsBox = new HBox(20);
        detailsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label typeLabel = new Label("Type: " + task.getType());
        typeLabel.setStyle("-fx-text-fill: black;");
        
        Label priorityLabel = new Label("Priority: " + task.getPriority());
        priorityLabel.setStyle("-fx-text-fill: black;");
        
        detailsBox.getChildren().addAll(typeLabel, priorityLabel);
        
        // Boutons d'action
        HBox actionBox = createActionButtons(task, container);
        
        // Assemblage du conteneur
        container.getChildren().addAll(titleBox, descLabel, datesBox, detailsBox, actionBox);
        
        return container;
    }

    // Étape 9: Création des boutons d'action
    private HBox createActionButtons(Task task, VBox container) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        // Bouton Terminer
        Button doneButton = new Button("Terminer");
        doneButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        doneButton.setOnAction(e -> handleTaskCompletion(container, task, true));
        
        // Bouton Modifier
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        editButton.setOnAction(e -> editTask(task, container));
        
        // Bouton Supprimer
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> handleTaskCompletion(container, task, false));
        
        buttonBox.getChildren().addAll(doneButton, editButton, deleteButton);
        return buttonBox;
    }

    // Étape 10: Gestion de la complétion d'une tâche
    private void handleTaskCompletion(VBox container, Task task, boolean completed) {
        try {
            // Archive la tâche
            model.TaskDAO.archiveToHistory(userEmail, task, completed);
            
            // Supprime la tâche
            model.TaskDAO.deleteTask(userEmail, task.getTitle(), 
                                   task.getStartDate(), task.getStartTime());
            
            // Met à jour l'interface
            tasksVBox.getChildren().remove(container);
            
            // Affiche un message de confirmation
            showAlert("Succès", "Tâche " + (completed ? "terminée" : "supprimée") + " avec succès");
            
        } catch (Exception e) {
            showError("Erreur lors de l'opération: " + e.getMessage());
        }
    }

    // Étape 11: Édition d'une tâche
    private void editTask(Task oldTask, VBox container) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Modifier la tâche");
        dialog.setHeaderText("Modifiez les détails de la tâche");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Crée les champs avec les anciennes valeurs
        TextField titleField = new TextField(oldTask.getTitle());
        TextArea paragraphArea = new TextArea(oldTask.getParagraph());

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Sport", "Education", "Religion", "Work", "General Culture", "Daily Activity", "Other");
        typeCombo.setValue(oldTask.getType());

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("High", "Medium", "Low");
        priorityCombo.setValue(oldTask.getPriority());

        DatePicker startDatePicker = new DatePicker(LocalDate.parse(oldTask.getStartDate()));
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, Integer.parseInt(oldTask.getStartTime().split(":")[0]));
        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, Integer.parseInt(oldTask.getStartTime().split(":")[1]));

        DatePicker endDatePicker = new DatePicker(LocalDate.parse(oldTask.getDueDate()));
        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, Integer.parseInt(oldTask.getDueTime().split(":")[0]));
        Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, Integer.parseInt(oldTask.getDueTime().split(":")[1]));

        // Mise en page
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Description:"), 0, 1);
        grid.add(paragraphArea, 1, 1);

        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeCombo, 1, 2);

        grid.add(new Label("Priorité:"), 0, 3);
        grid.add(priorityCombo, 1, 3);

        grid.add(new Label("Date Début:"), 0, 4);
        grid.add(new HBox(5, startDatePicker, startHourSpinner, new Label(":"), startMinuteSpinner), 1, 4);

        grid.add(new Label("Date Fin:"), 0, 5);
        grid.add(new HBox(5, endDatePicker, endHourSpinner, new Label(":"), endMinuteSpinner), 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String startTime = String.format("%02d:%02d", startHourSpinner.getValue(), startMinuteSpinner.getValue());
                String endTime = String.format("%02d:%02d", endHourSpinner.getValue(), endMinuteSpinner.getValue());

                return new Task(
                    titleField.getText(),
                    paragraphArea.getText(),
                    oldTask.getState(), // L'état ne change pas ici
                    startDatePicker.getValue().toString(),
                    startTime,
                    endDatePicker.getValue().toString(),
                    endTime,
                    typeCombo.getValue(),
                    priorityCombo.getValue()
                );
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();

        result.ifPresent(newTask -> {
            try {
                // Mise à jour en base
                TaskDAO.updateTask(userEmail, oldTask, newTask);
                // Mise à jour de l'affichage
                initUserData(userEmail, userEmail); // recharge toutes les tâches
            } catch (Exception e) {
                showError("Erreur lors de la mise à jour de la tâche: " + e.getMessage());
            }
        });
    }


    // Étape 12: Filtrage des tâches
    private void filterTasks() {
        String selectedType = typeFilter.getValue();
        String selectedPriority = priorityFilter.getValue();
        String titleText = titleFilter.getText().toLowerCase();

        List<Task> filtered = allTasks.stream()
            .filter(task -> {
                boolean matchType = selectedType.equals("Tous") || task.getType().equals(selectedType);
                boolean matchPriority = selectedPriority.equals("Tous") || task.getPriority().equals(selectedPriority);
                boolean matchTitle = titleText.isEmpty() || task.getTitle().toLowerCase().contains(titleText);
                return matchType && matchPriority && matchTitle;
            })
            .toList();

        updateTasks(filtered);
    }


    // Méthodes utilitaires
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}