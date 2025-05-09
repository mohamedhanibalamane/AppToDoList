package controller;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import java.time.LocalTime;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Task;
import model.TaskDAO;
import model.UserDAO;
import javafx.scene.layout.StackPane;

import java.time.format.DateTimeFormatter;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.io.IOException;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HomeController {

    @FXML private Button dashboardButton, progressButton, historyButton, corbayButton, tasksButton, logoutButton;
    @FXML private Button fullNameField, addTaskButton, tasksDoingButton, tasksDoneButton, tasksFailedButton;
    @FXML private Button dayButton, coinsButton, progressPercentButton, createTaskButton;
    @FXML private DatePicker datePicker;
    @FXML private VBox tasksContainer;
    @FXML private Label congratsLabel;
    @FXML private VBox reminderBox;

    private String userEmail;
    private String userName;
    private int coins = 0;
    private int progress = 0;
    private LocalDate currentDate = LocalDate.now();

    @FXML
    public void initialize() {
        configureNavigationButtons();
        datePicker.setValue(currentDate);
        setupTaskButtons();
        initializeCounters();
        checkExpiredTasks();
        updateDoingTasksCount();
        
        // Initialiser les valeurs des coins et progression
        coinsButton.setText("0c");
        progressPercentButton.setText("0%");
        
        // Planifier la vérification périodique
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.minutes(1), 
            event -> {
                checkExpiredTasks();
                updateDoingTasksCount();
            }
        ));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void initUserData(String email, String name) {
        this.userEmail = email;
        this.userName = name;
        updateUserNameDisplay();
        loadUserTasks();
        updateDailyStats();
        updateDoingTasksCount(); // Ajoutez cette ligne
    }

    private void updateUserNameDisplay() {
        if (userName != null && !userName.isEmpty()) {
            fullNameField.setText(userName + " !");
        } else {
            fullNameField.setText("Invité !");
        }
    }

    private void configureNavigationButtons() {
        dashboardButton.setOnAction(e -> loadView("/vue/Hoooome2.fxml"));
        progressButton.setOnAction(e -> loadView("/vue/Progres.fxml"));
        historyButton.setOnAction(e -> loadView("/vue/History.fxml"));
        corbayButton.setOnAction(e -> loadView("/vue/Corbay.fxml"));
        tasksButton.setOnAction(e -> loadTasksView());
        
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

    private void setupTaskButtons() {
        addTaskButton.setOnAction(e -> showAddTaskDialog());
        createTaskButton.setOnAction(e -> showAddTaskDialog());
        
        datePicker.setOnAction(e -> {
            currentDate = datePicker.getValue();
            updateDailyStats();
            loadUserTasks();
        });
    }

    private void initializeCounters() {
        updateTaskCounters(0, 0, 0);
        updateCoinsAndProgress();
    }

    private void showAddTaskDialog() {
        Dialog<Task> dialog = createTaskDialog();
        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(task -> {
            try {
                // Convertir les dates/heures
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                
                LocalDateTime startDateTime = LocalDateTime.of(
                    LocalDate.parse(task.getStartDate(), dateFormatter),
                    LocalTime.parse(task.getStartTime(), timeFormatter)
                );
                
                LocalDateTime endDateTime = LocalDateTime.of(
                    LocalDate.parse(task.getDueDate(), dateFormatter),
                    LocalTime.parse(task.getDueTime(), timeFormatter)
                );

                // Ajouter à la base de données
                TaskDAO.addTask(
                    userEmail,
                    task.getTitle(),
                    task.getParagraph(),
                    startDateTime,
                    endDateTime,
                    task.getType(),
                    task.getPriority()
                );
                
                // Rafraîchir l'affichage
                loadUserTasks();
                updateDailyStats();
                
            } catch (Exception e) {
                showError("Erreur lors de l'ajout de la tâche: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private GridPane createFormGrid(TextField titleField, TextArea paragraphArea,
            ComboBox<String> typeComboBox, ComboBox<String> priorityComboBox,
            DatePicker startDatePicker, Spinner<Integer> startHourSpinner,
            Spinner<Integer> startMinuteSpinner, DatePicker endDatePicker,
            Spinner<Integer> endHourSpinner, Spinner<Integer> endMinuteSpinner) {

// Configuration des ComboBox
typeComboBox.getItems().addAll("Sport", "Education", "Religion", "Work", 
               "General Culture", "Daily Activity", "Other");
typeComboBox.setValue("Other");

priorityComboBox.getItems().addAll("High", "Medium", "Low");
priorityComboBox.setValue("Medium");

// Configuration des champs
titleField.setPromptText("Titre de la tâche");
paragraphArea.setPromptText("Description");
paragraphArea.setPrefRowCount(3);

// Configuration des Spinners pour les heures/minutes
startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 17));
endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

// Création du GridPane
GridPane grid = new GridPane();
grid.setHgap(10);
grid.setVgap(10);
grid.setPadding(new Insets(20, 150, 10, 10));

// Ajout des composants au GridPane
grid.add(new Label("Titre:"), 0, 0);
grid.add(titleField, 1, 0);

grid.add(new Label("Description:"), 0, 1);
grid.add(paragraphArea, 1, 1);

grid.add(new Label("Type:"), 0, 2);
grid.add(typeComboBox, 1, 2);

grid.add(new Label("Priorité:"), 0, 3);
grid.add(priorityComboBox, 1, 3);

// Ligne pour la date de début
grid.add(new Label("Date de début:"), 0, 4);
HBox startDateTimeBox = new HBox(5, 
startDatePicker,
new Label("Heure:"),
startHourSpinner,
new Label(":"),
startMinuteSpinner
);
grid.add(startDateTimeBox, 1, 4);

// Ligne pour la date de fin
grid.add(new Label("Date de fin:"), 0, 5);
HBox endDateTimeBox = new HBox(5, 
endDatePicker,
new Label("Heure:"),
endHourSpinner,
new Label(":"),
endMinuteSpinner
);
grid.add(endDateTimeBox, 1, 5);

return grid;
}
    
    private Dialog<Task> createTaskDialog() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Tâche");
        dialog.setHeaderText("Remplissez les détails de la tâche");
        
        // Configuration des boutons
        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Création des champs
        TextField titleField = new TextField();
        TextArea paragraphArea = new TextArea();
        ComboBox<String> typeComboBox = new ComboBox<>();
        ComboBox<String> priorityComboBox = new ComboBox<>();
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(1));
        
        // Ajout des Spinners pour les heures/minutes
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, 8);
        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, 0);
        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, 17);
        Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, 0);
        
        // Ajout des composants au dialogue
        dialog.getDialogPane().setContent(createFormGrid(
            titleField, paragraphArea, typeComboBox, priorityComboBox,
            startDatePicker, startHourSpinner, startMinuteSpinner,
            endDatePicker, endHourSpinner, endMinuteSpinner
        ));
        
        // Conversion du résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                // Validation des champs obligatoires
                if (titleField.getText().isEmpty()) {
                    showError("Le titre est obligatoire");
                    return null;
                }
                
                // Formatage de l'heure
                String startTime = String.format("%02d:%02d", startHourSpinner.getValue(), startMinuteSpinner.getValue());
                String endTime = String.format("%02d:%02d", endHourSpinner.getValue(), endMinuteSpinner.getValue());
                
                return new Task(
                    titleField.getText(),
                    paragraphArea.getText(),
                    "", // État déterminé plus tard
                    startDatePicker.getValue().toString(),
                    startTime,
                    endDatePicker.getValue().toString(),
                    endTime,
                    typeComboBox.getValue(),
                    priorityComboBox.getValue()
                );
            }
            return null;
        });
        
        return dialog;
    }

    private void addFormElements(GridPane grid, TextField titleField, TextArea paragraphArea,
                               ComboBox<String> typeComboBox, ComboBox<String> priorityComboBox,
                               DatePicker startDatePicker, Spinner<Integer> startHourSpinner,
                               Spinner<Integer> startMinuteSpinner, DatePicker endDatePicker,
                               Spinner<Integer> endHourSpinner, Spinner<Integer> endMinuteSpinner) {
        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);
        
        grid.add(new Label("Description:"), 0, 1);
        grid.add(paragraphArea, 1, 1);
        
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeComboBox, 1, 2);
        
        grid.add(new Label("Priorité:"), 0, 3);
        grid.add(priorityComboBox, 1, 3);
        
        grid.add(new Label("Date de Début:"), 0, 4);
        HBox startDateTimeBox = createDateTimeBox(startDatePicker, startHourSpinner, startMinuteSpinner);
        grid.add(startDateTimeBox, 1, 4);
        
        grid.add(new Label("Date de Fin:"), 0, 5);
        HBox endDateTimeBox = createDateTimeBox(endDatePicker, endHourSpinner, endMinuteSpinner);
        grid.add(endDateTimeBox, 1, 5);
    }

    private HBox createDateTimeBox(DatePicker datePicker, Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(
            datePicker,
            new Label("Heure:"),
            hourSpinner,
            new Label(":"),
            minuteSpinner
        );
        return box;
    }

    private Task createTaskFromInput(TextField titleField, TextArea paragraphArea,
                                   ComboBox<String> typeComboBox, ComboBox<String> priorityComboBox,
                                   DatePicker startDatePicker, Spinner<Integer> startHourSpinner,
                                   Spinner<Integer> startMinuteSpinner, DatePicker endDatePicker,
                                   Spinner<Integer> endHourSpinner, Spinner<Integer> endMinuteSpinner) {
        LocalDateTime startDateTime = LocalDateTime.of(
            startDatePicker.getValue(),
            LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue())
        );
        
        LocalDateTime endDateTime = LocalDateTime.of(
            endDatePicker.getValue(),
            LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue())
        );
        
        String state = determineTaskState(startDateTime, endDateTime);
        
        return new Task(
            titleField.getText(),
            paragraphArea.getText(),
            state,
            startDatePicker.getValue().toString(),
            String.format("%02d:%02d", startHourSpinner.getValue(), startMinuteSpinner.getValue()),
            endDatePicker.getValue().toString(),
            String.format("%02d:%02d", endHourSpinner.getValue(), endMinuteSpinner.getValue()),
            typeComboBox.getValue(),
            priorityComboBox.getValue()
        );
    }

    private void processNewTask(Task task) {
        if (task != null && !task.getTitle().isEmpty()) {
            try {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                
                LocalDateTime startDateTime = LocalDateTime.of(
                    LocalDate.parse(task.getStartDate(), dateFormatter),
                    LocalTime.parse(task.getStartTime(), timeFormatter)
                );
                
                LocalDateTime endDateTime = LocalDateTime.of(
                    LocalDate.parse(task.getDueDate(), dateFormatter),
                    LocalTime.parse(task.getDueTime(), timeFormatter)
                );
                
                TaskDAO.addTask(
                    userEmail,
                    task.getTitle(),
                    task.getParagraph(),
                    startDateTime,
                    endDateTime,
                    task.getType(),
                    task.getPriority()
                );
                
                loadUserTasks();
                updateDailyStats();
                
            } catch (Exception e) {
                showError("Erreur lors de l'ajout de la tâche: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String determineTaskState(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            return "En Attente";
        } else if (now.isAfter(end)) {
            return "Terminer";
        } else {
            return "En Cours";
        }
    }

    private void createTaskItem(Task task) {
        StackPane taskPane = new StackPane();
        taskPane.setStyle("-fx-background-color: WHITE; -fx-background-radius: 6; -fx-padding: 10;");
        
        // Créer le contenu de la tâche avec la référence au taskPane
        VBox taskContent = createTaskContent(task, taskPane);
        
        taskPane.getChildren().add(taskContent);
        taskPane.setUserData(task);
        
        tasksContainer.getChildren().add(0, taskPane);
    }

    private VBox createTaskContent(Task task, StackPane taskPane) {
        VBox taskContent = new VBox(5);
        
        // Titre
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.BLACK);
        
        // Description
        Label paragraphLabel = new Label(task.getParagraph());
        paragraphLabel.setWrapText(true);
        paragraphLabel.setTextFill(Color.BLACK);
        
        // Dates et état
        Label datesLabel = new Label(
            "Début: " + task.getStartDate() + " " + task.getStartTime() + 
            "\nFin: " + task.getDueDate() + " " + task.getDueTime() +
            "\nType: " + task.getType() +
            "\nPriorité: " + task.getPriority() +
            "\nÉtat: " + task.getState()
        );
        datesLabel.setTextFill(Color.BLACK);
        
        // Boutons d'action
        HBox buttonBox = createActionButtons(task, taskPane);
        
        taskContent.getChildren().addAll(titleLabel, paragraphLabel, datesLabel, buttonBox);
        updateTaskStyle(taskContent, task);
        
        return taskContent;
    }
    
    private HBox createActionButtons(Task task, StackPane taskPane) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // 1. Bouton Terminer
        Button doneButton = new Button("Terminer");
        doneButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: WHITE;");
        
        // Désactiver si la tâche n'a pas commencé
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime startDateTime = LocalDateTime.parse(
            task.getStartDate() + " " + task.getStartTime(), 
            formatter
        );
        
        if (LocalDateTime.now().isBefore(startDateTime)) {
            doneButton.setDisable(true);
            doneButton.setTooltip(new Tooltip("La tâche n'a pas encore commencé"));
        }
        
        // Gestion du clic sur "Terminer"
        doneButton.setOnAction(e -> handleTaskCompletion(taskPane, task, true));
        
        // 2. Bouton Modifier
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: WHITE;");
        
        // Gestion du clic sur "Modifier"
        editButton.setOnAction(e -> {
            // Créer et afficher la boîte de dialogue de modification
            Dialog<Task> dialog = createEditDialog(task);
            Optional<Task> result = dialog.showAndWait();
            
            // Traiter le résultat si l'utilisateur a cliqué sur Enregistrer
            result.ifPresent(modifiedTask -> {
                try {
                    // Mettre à jour la tâche dans la base de données
                    TaskDAO.updateTask(userEmail, task, modifiedTask);
                    
                    // Recharger les tâches
                    loadUserTasks();
                    
                    // Mettre à jour les statistiques
                    updateDailyStats();
                    
                } catch (SQLException ex) {
                    showError("Erreur lors de la modification de la tâche: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        });
        
        // 3. Bouton Supprimer
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: WHITE;");
        
        // Gestion du clic sur "Supprimer"
        deleteButton.setOnAction(e -> {
            // Confirmation avant suppression
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText("Supprimer la tâche");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette tâche ?");
            
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Supprimer la tâche de la base de données
                    TaskDAO.deleteTask(userEmail, task.getTitle(), task.getStartDate(), task.getStartTime());
                    
                    // Supprimer visuellement la tâche
                    tasksContainer.getChildren().remove(taskPane);
                    
                    // Mettre à jour les statistiques
                    updateDailyStats();
                    
                } catch (SQLException ex) {
                    showError("Erreur lors de la suppression de la tâche: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        
        // Ajouter tous les boutons au HBox
        buttonBox.getChildren().addAll(doneButton, editButton, deleteButton);
        return buttonBox;
    }
    
    private HBox createTaskActionButtons(Task task, VBox taskContent) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button doneButton = new Button("Terminer");
        doneButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: WHITE;");
        
        // Désactiver le bouton Terminer si la tâche n'a pas encore commencé
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime startDateTime = LocalDateTime.parse(task.getStartDate() + " " + task.getStartTime(), 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        if (LocalDateTime.now().isBefore(startDateTime)) {
            doneButton.setDisable(true);
            doneButton.setTooltip(new Tooltip("La tâche n'a pas encore commencé"));
        }
        
        doneButton.setOnAction(e -> handleTaskCompletion(taskContent, task, true));
        
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: WHITE;");
        editButton.setOnAction(e -> editTask(taskContent, task));
        
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: WHITE;");
        deleteButton.setOnAction(e -> handleTaskCompletion(taskContent, task, false));
        
        buttonBox.getChildren().addAll(doneButton, editButton, deleteButton);
        return buttonBox;
    }

    private void updateTaskStyle(Pane taskPane, Task task) {
        String style = switch (task.getState()) {
            case "Terminer" -> "-fx-background-color: #DDFFDD;";
            case "En Cours" -> "-fx-background-color: #FFFFDD;";
            default -> "-fx-background-color: WHITE;";
        };
        taskPane.setStyle(style + " -fx-background-radius: 6; -fx-padding: 10;");
    }

    private void editTask(Pane taskPane, Task oldTask) {
        Dialog<Task> dialog = createEditDialog(oldTask);
        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(newTask -> processEditedTask(taskPane, oldTask, newTask));
    }

    private Dialog<Task> createEditDialog(Task oldTask) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Modifier Tâche");
        dialog.setHeaderText("Modifier la tâche existante");

        // Configuration des boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Création du formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Champs du formulaire pré-remplis avec les valeurs actuelles
        TextField titleField = new TextField(oldTask.getTitle());
        TextArea descriptionArea = new TextArea(oldTask.getParagraph());
        descriptionArea.setPrefRowCount(3);
        
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

        // Ajout des composants au grid
        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeCombo, 1, 2);
        grid.add(new Label("Priorité:"), 0, 3);
        grid.add(priorityCombo, 1, 3);
        grid.add(new Label("Date de début:"), 0, 4);
        grid.add(createDateTimeBox(startDatePicker, startHourSpinner, startMinuteSpinner), 1, 4);
        grid.add(new Label("Date de fin:"), 0, 5);
        grid.add(createDateTimeBox(endDatePicker, endHourSpinner, endMinuteSpinner), 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Conversion du résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Créer une nouvelle tâche avec les valeurs modifiées
                return new Task(
                    titleField.getText(),
                    descriptionArea.getText(),
                    oldTask.getState(), // Garder le même état
                    startDatePicker.getValue().toString(),
                    String.format("%02d:%02d", startHourSpinner.getValue(), startMinuteSpinner.getValue()),
                    endDatePicker.getValue().toString(),
                    String.format("%02d:%02d", endHourSpinner.getValue(), endMinuteSpinner.getValue()),
                    typeCombo.getValue(),
                    priorityCombo.getValue()
                );
            }
            return null;
        });

        return dialog;
    }

    private void processEditedTask(Pane taskPane, Task oldTask, Task newTask) {
        if (!newTask.getTitle().isEmpty()) {
            try {
                // Mise à jour dans la base de données
                TaskDAO.updateTask(userEmail, oldTask, newTask);
                
                // Mise à jour de l'interface
                tasksContainer.getChildren().remove(taskPane);
                createTaskItem(newTask);
                updateDailyStats();
            } catch (SQLException e) {
                showError("Erreur lors de la mise à jour de la tâche: " + e.getMessage());
            }
        }
    }

    private void handleTaskCompletion(Pane taskPane, Task task, boolean completed) {
        try {
            TaskDAO.archiveToHistory(userEmail, task, completed);
            TaskDAO.deleteTask(userEmail, task.getTitle(), task.getStartDate(), task.getStartTime());
            
            if (completed) {
                updateCoins(20); // +20 coins
                updateProgressPercentage(Integer.parseInt(coinsButton.getText().replace("c", "")));
            } else {
                updateCoins(-30); // -30 coins
                updateProgressPercentage(Integer.parseInt(coinsButton.getText().replace("c", "")));
            }
            
            tasksContainer.getChildren().remove(taskPane);
            updateDailyStats();
            updateDoingTasksCount();
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void checkExpiredTasks() {
        List<Pane> tasksToRemove = new ArrayList<>();
        
        for (javafx.scene.Node node : tasksContainer.getChildren()) {
            if (node instanceof Pane) {
                Pane taskPane = (Pane) node;
                Task task = (Task) taskPane.getUserData();
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime endDateTime = LocalDateTime.parse(
                    task.getDueDate() + " " + task.getDueTime(), 
                    formatter
                );
                
                if (LocalDateTime.now().isAfter(endDateTime) && !task.getState().equals("Terminer")) {
                    try {
                        // Archiver comme échec
                        TaskDAO.archiveToHistory(userEmail, task, false);
                        // Supprimer de la table principale
                        TaskDAO.deleteTask(userEmail, task.getTitle(), task.getStartDate(), task.getStartTime());
                        tasksToRemove.add(taskPane);
                        
                        // Mise à jour des compteurs
                        updateTaskCounters(-1, 0, 1);
                        updateCoins(-30);
                        updateProgress(-5);
                    } catch (SQLException e) {
                        showError("Erreur lors de l'archivage de la tâche expirée: " + e.getMessage());
                    }
                }
            }
        }
        
        // Supprimer les tâches expirées de l'interface
        tasksContainer.getChildren().removeAll(tasksToRemove);
        updateCoinsAndProgress();
        updateDailyStats();
    }

    private void updateCoins(int amount) {
        try {
            int currentCoins = Integer.parseInt(coinsButton.getText().replace("c", ""));
            int newCoins = Math.min(currentCoins + amount, 200);
            newCoins = Math.max(newCoins, 0);
            coinsButton.setText(newCoins + "c");
            updateProgressPercentage(newCoins);

            // Afficher le message si on atteint 200
            if (newCoins == 200) {
                congratsLabel.setVisible(true);
                Timeline hideMessage = new Timeline(new KeyFrame(Duration.seconds(10), e -> congratsLabel.setVisible(false)));
                hideMessage.setCycleCount(1);
                hideMessage.play();
            }

        } catch (NumberFormatException e) {
            coinsButton.setText("0c");
            progressPercentButton.setText("0%");
        }
    }

    
    private void updateProgressPercentage(int coins) {
        // Calculer le pourcentage (200 coins = 100%)
        int percentage = (int) Math.round((coins / 200.0) * 100);
        percentage = Math.min(percentage, 100); // Ne pas dépasser 100%
        
        // Mettre à jour l'affichage
        progressPercentButton.setText(percentage + "%");
    }

    private void updateProgress(int amount) {
        try {
            int currentProgress = Integer.parseInt(progressPercentButton.getText().replace("%", ""));
            progressPercentButton.setText(Math.max(0, Math.min(100, currentProgress + amount)) + "%");
        } catch (NumberFormatException e) {
            progressPercentButton.setText("0%");
        }
    }

    private void checkCurrentTasks() {
        long currentTasks = tasksContainer.getChildren().stream()
            .filter(node -> node instanceof Pane)
            .map(node -> (Pane) node)
            .filter(pane -> {
                Task task = (Task) pane.getUserData();
                return "En Cours".equals(task.getState());
            })
            .count();
        
        tasksDoingButton.setText(String.valueOf(currentTasks));
    }

    private void updateTaskCounters(int doingDelta, int doneDelta, int failedDelta) {
        int currentDoing = Integer.parseInt(tasksDoingButton.getText());
        int currentDone = Integer.parseInt(tasksDoneButton.getText());
        int currentFailed = Integer.parseInt(tasksFailedButton.getText());
        
        tasksDoingButton.setText(String.valueOf(currentDoing + doingDelta));
        tasksDoneButton.setText(String.valueOf(currentDone + doneDelta));
        tasksFailedButton.setText(String.valueOf(currentFailed + failedDelta));
    }

    private void updateCoinsAndProgress() {
        coinsButton.setText(coins + "c");
        progressPercentButton.setText(progress + "%");
    }

    private void updateDailyStats() {
        try {
        	// Réinitialiser les compteurs si c'est un nouveau jour
            if (!LocalDate.now().equals(currentDate)) {
                coinsButton.setText("0c");
                progressPercentButton.setText("0%");
                currentDate = LocalDate.now();
            }
            // Calcul des tâches en cours
            int totalTasksToday = TaskDAO.getTaskCount(userEmail, "En Cours", currentDate);
            int doneTasksToday = TaskDAO.getHistoryCount(userEmail, "Terminer", currentDate);
            int failedTasksToday = TaskDAO.getHistoryCount(userEmail, "Échoué", currentDate);
            
            // Calcul du pourcentage de progression
            if (totalTasksToday > 0) {
                progress = (int) ((double) doneTasksToday / totalTasksToday * 100);
            } else {
                progress = 0;
            }
            
            // Calcul des coins
            coins = (doneTasksToday * 20) - (failedTasksToday * 30);
            coins = Math.max(0, coins);
            
            // Mise à jour des affichages
            tasksDoingButton.setText(String.valueOf(totalTasksToday));
            tasksDoneButton.setText(String.valueOf(doneTasksToday));
            tasksFailedButton.setText(String.valueOf(failedTasksToday));
            coinsButton.setText(coins + "c");
            progressPercentButton.setText(progress + "%");
            updateDoingTasksCount();
         // Mettre à jour le pourcentage en fonction des coins actuels
            int currentCoins = Integer.parseInt(coinsButton.getText().replace("c", ""));
            updateProgressPercentage(currentCoins);
        } catch (SQLException e) {
            showError("Erreur de chargement des statistiques: " + e.getMessage());
        }
    }

    private void loadUserTasks() {
        try {
            List<Task> tasks = TaskDAO.getUserTasks(userEmail);
            tasksContainer.getChildren().clear();
            reminderBox.getChildren().clear();
            reminderBox.setVisible(false);

            List<String> highPriorityTitles = new ArrayList<>();

            for (Task task : tasks) {
                createTaskItem(task);
                if ("High".equalsIgnoreCase(task.getPriority())) {
                    highPriorityTitles.add(task.getTitle());
                }
            }

            if (!highPriorityTitles.isEmpty()) {
                showPriorityReminder("High", highPriorityTitles);
            }

            updateDailyStats();
            updateDoingTasksCount();

        } catch (SQLException e) {
            showError("Erreur de chargement des tâches: " + e.getMessage());
        }
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
                controller.initUserData(userEmail); // Utilisez initUserData au lieu de updateTasks
            } else if (loader.getController() instanceof HistoryController) {
                HistoryController controller = (HistoryController) loader.getController();
                controller.loadHistory(userEmail);
            } else if (loader.getController() instanceof ProgressController) {
                ProgressController controller = (ProgressController) loader.getController();
                controller.loadProgress(userEmail);
            }
            
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur de chargement de la vue: " + e.getMessage());
        }
    }

    private void loadTasksView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/Task.fxml"));
            Parent root = loader.load();
            
            TaskController taskController = loader.getController();
            if (taskController != null) {
                taskController.initUserData(userEmail);
            }
            
            Stage stage = (Stage) tasksButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur de chargement de la vue des tâches: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Task> convertTaskDataToTask() {
        List<Task> tasks = new ArrayList<>();
        
        for (javafx.scene.Node node : tasksContainer.getChildren()) {
            if (node instanceof Pane) {
                Pane taskPane = (Pane) node;
                Task task = (Task) taskPane.getUserData();
                tasks.add(task);
            }
        }
        return tasks;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

	public void setUserName(String trim) {
		// TODO Auto-generated method stub
		
	}
	
	private void checkAndHandleExpiredTasks() {
	    List<Pane> tasksToRemove = new ArrayList<>();
	    
	    for (javafx.scene.Node node : tasksContainer.getChildren()) {
	        if (node instanceof Pane) {
	            Pane taskPane = (Pane) node;
	            Task task = (Task) taskPane.getUserData();
	            
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	            LocalDateTime endDateTime = LocalDateTime.parse(
	                task.getDueDate() + " " + task.getDueTime(), 
	                formatter
	            );
	            
	            if (LocalDateTime.now().isAfter(endDateTime) && !task.getState().equals("Terminer")) {
	                try {
	                    // 1. Archiver comme échouée
	                    TaskDAO.archiveToHistory(userEmail, task, false);
	                    
	                    // 2. Supprimer de la table principale
	                    TaskDAO.deleteTask(userEmail, task.getTitle(), task.getStartDate(), task.getStartTime());
	                    
	                    // 3. Marquer pour suppression de l'interface
	                    tasksToRemove.add(taskPane);
	                    
	                    // 4. Mettre à jour les statistiques
	                    updateCoins(-30); // Pénalité pour tâche non terminée
	                    updateProgress(-5);
	                    
	                } catch (SQLException e) {
	                    showError("Erreur lors du traitement de la tâche expirée: " + e.getMessage());
	                }
	            }
	        }
	        
	    }
	    
	    // Supprimer les tâches expirées de l'interface
	    tasksContainer.getChildren().removeAll(tasksToRemove);
	    updateDailyStats();
	    updateDoingTasksCount();
	}
	
	private void updateDoingTasksCount() {
	    try {
	        int doingCount = TaskDAO.getCurrentTasksCount(userEmail);
	        tasksDoingButton.setText(String.valueOf(doingCount));
	    } catch (SQLException e) {
	        showError("Erreur lors du chargement des tâches en cours: " + e.getMessage());
	        tasksDoingButton.setText("0");
	    }
	}
	
	private void showPriorityReminder(String priority, List<String> titles) {
	    StringBuilder message = new StringBuilder("Rappel : Vous avez des tâches importantes (" + priority + "):\n");

	    for (String title : titles) {
	        message.append("• ").append(title).append("\n");
	    }

	    Label reminder = new Label(message.toString());
	    reminder.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14;");
	    reminderBox.getChildren().add(reminder);
	    reminderBox.setVisible(true);

	    Timeline hide = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
	        reminderBox.getChildren().remove(reminder);
	        if (reminderBox.getChildren().isEmpty()) reminderBox.setVisible(false);
	    }));
	    hide.setCycleCount(1);
	    hide.play();
	}

    
	
}