package controller;

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
import model.Task;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HomeController {

    // Éléments de navigation
    @FXML private Button dashboardButton;
    @FXML private Button progressButton;
    @FXML private Button historyButton;
    @FXML private Button corbayButton;
    @FXML private Button tasksButton;
    @FXML private Button logoutButton;
    
    // Éléments du tableau de bord
    @FXML private Button fullNameField;
    @FXML private DatePicker datePicker;
    @FXML private VBox tasksContainer;
    @FXML private Button addTaskButton;
    @FXML private Button tasksDoingButton;
    @FXML private Button tasksDoneButton;
    @FXML private Button tasksFailedButton;
    @FXML private Button dayButton;
    @FXML private Button coinsButton;
    @FXML private Button progressPercentButton;
    @FXML private Button createTaskButton;

    private int coins = 2057;
    private int progress = 94;

    @FXML
    public void initialize() {
        configureNavigationButtons();
        datePicker.setValue(LocalDate.now());
        
        // Configurer les boutons d'ajout/création de tâche
        addTaskButton.setOnAction(e -> showAddTaskDialog());
        createTaskButton.setOnAction(e -> showAddTaskDialog());
        
        // Initialiser les compteurs
        updateTaskCounters(0, 0, 0);
        updateCoinsAndProgress();
    }

    private void showAddTaskDialog() {
        // Créer une boîte de dialogue personnalisée
        Dialog<TaskData> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Tâche");
        dialog.setHeaderText("Ajouter une nouvelle tâche");

        // Configurer les boutons
        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Créer le formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        descriptionField.setStyle("-fx-text-fill: black;");

        // Date et heure de début
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now());
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, 12);
        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, 0);
        Spinner<Integer> startSecondSpinner = new Spinner<>(0, 59, 0);

        // Date et heure de fin
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, 12);
        Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, 0);
        Spinner<Integer> endSecondSpinner = new Spinner<>(0, 59, 0);

        // Ajouter les éléments au formulaire
        grid.add(new Label("Description:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        
        grid.add(new Label("Date de Début:"), 0, 1);
        HBox startDateTimeBox = new HBox(5, 
            startDatePicker,
            new Label("Heure:"),
            startHourSpinner,
            new Label(":"),
            startMinuteSpinner,
            new Label(":"),
            startSecondSpinner
        );
        grid.add(startDateTimeBox, 1, 1);
        
        grid.add(new Label("Date de Fin:"), 0, 2);
        HBox endDateTimeBox = new HBox(5, 
            endDatePicker,
            new Label("Heure:"),
            endHourSpinner,
            new Label(":"),
            endMinuteSpinner,
            new Label(":"),
            endSecondSpinner
        );
        grid.add(endDateTimeBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Convertir le résultat en objet TaskData
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                LocalDateTime startDateTime = LocalDateTime.of(
                    startDatePicker.getValue(),
                    LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue(), startSecondSpinner.getValue())
                );
                
                LocalDateTime endDateTime = LocalDateTime.of(
                    endDatePicker.getValue(),
                    LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue(), endSecondSpinner.getValue())
                );
                
                // Déterminer automatiquement l'état
                String state = determineTaskState(startDateTime, endDateTime);
                
                return new TaskData(
                    descriptionField.getText(),
                    startDateTime,
                    endDateTime,
                    state
                );
            }
            return null;
        });

        // Afficher la boîte de dialogue et traiter le résultat
        Optional<TaskData> result = dialog.showAndWait();
        result.ifPresent(taskData -> {
            if (!taskData.getDescription().isEmpty()) {
                createTaskItem(taskData);
                updateTaskCounters(1, 0, 0);
            }
        });
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

    private void createTaskItem(TaskData taskData) {
        // Créer le conteneur principal
        StackPane taskPane = new StackPane();
        taskPane.setStyle("-fx-background-color: WHITE; -fx-background-radius: 6; -fx-padding: 10;");
        
        // Créer le contenu de la tâche
        VBox taskContent = new VBox(5);
        
        // Description de la tâche
        Label descriptionLabel = new Label(taskData.getDescription());
        descriptionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        descriptionLabel.setStyle("-fx-text-fill: black;");
        
        // Dates et état
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        Label datesLabel = new Label(
            "Début: " + taskData.getStartDateTime().format(formatter) + 
            "\nFin: " + taskData.getEndDateTime().format(formatter) +
            "\nÉtat: " + taskData.getState()
        );
        datesLabel.setStyle("-fx-text-fill: black;");
        
        // Mettre à jour le style selon l'état
        updateTaskStyle(taskPane, taskData);
        
        // Boutons d'action
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button doneButton = new Button("Terminer");
        doneButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: WHITE;");
        doneButton.setOnAction(e -> handleTaskCompletion(taskPane, taskData, true));
        
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: WHITE;");
        editButton.setOnAction(e -> editTask(taskPane, taskData));
        
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: WHITE;");
        deleteButton.setOnAction(e -> handleTaskCompletion(taskPane, taskData, false));
        
        buttonBox.getChildren().addAll(doneButton, editButton, deleteButton);
        taskContent.getChildren().addAll(descriptionLabel, datesLabel, buttonBox);
        taskPane.getChildren().add(taskContent);
        
        // Stocker les données dans le pane
        taskPane.setUserData(taskData);
        
        // Ajouter la tâche en haut de la liste
        tasksContainer.getChildren().add(0, taskPane);
        
        // Vérifier si la tâche est en cours
        checkCurrentTasks();
    }

    private void editTask(StackPane taskPane, TaskData oldTaskData) {
        // Créer une boîte de dialogue similaire à showAddTaskDialog()
        Dialog<TaskData> dialog = new Dialog<>();
        dialog.setTitle("Modifier Tâche");
        dialog.setHeaderText("Modifier la tâche existante");

        // Configurer les boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Créer le formulaire pré-rempli
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField descriptionField = new TextField(oldTaskData.getDescription());
        descriptionField.setStyle("-fx-text-fill: black;");

        // Date et heure de début
        DatePicker startDatePicker = new DatePicker(oldTaskData.getStartDateTime().toLocalDate());
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, oldTaskData.getStartDateTime().getHour());
        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, oldTaskData.getStartDateTime().getMinute());
        Spinner<Integer> startSecondSpinner = new Spinner<>(0, 59, oldTaskData.getStartDateTime().getSecond());

        // Date et heure de fin
        DatePicker endDatePicker = new DatePicker(oldTaskData.getEndDateTime().toLocalDate());
        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, oldTaskData.getEndDateTime().getHour());
        Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, oldTaskData.getEndDateTime().getMinute());
        Spinner<Integer> endSecondSpinner = new Spinner<>(0, 59, oldTaskData.getEndDateTime().getSecond());

        // Ajouter les éléments au formulaire
        grid.add(new Label("Description:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        
        grid.add(new Label("Date de Début:"), 0, 1);
        HBox startDateTimeBox = new HBox(5, 
            startDatePicker,
            new Label("Heure:"),
            startHourSpinner,
            new Label(":"),
            startMinuteSpinner,
            new Label(":"),
            startSecondSpinner
        );
        grid.add(startDateTimeBox, 1, 1);
        
        grid.add(new Label("Date de Fin:"), 0, 2);
        HBox endDateTimeBox = new HBox(5, 
            endDatePicker,
            new Label("Heure:"),
            endHourSpinner,
            new Label(":"),
            endMinuteSpinner,
            new Label(":"),
            endSecondSpinner
        );
        grid.add(endDateTimeBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Convertir le résultat en objet TaskData
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                LocalDateTime startDateTime = LocalDateTime.of(
                    startDatePicker.getValue(),
                    LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue(), startSecondSpinner.getValue())
                );
                
                LocalDateTime endDateTime = LocalDateTime.of(
                    endDatePicker.getValue(),
                    LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue(), endSecondSpinner.getValue())
                );
                
                // Déterminer automatiquement le nouvel état
                String state = determineTaskState(startDateTime, endDateTime);
                
                return new TaskData(
                    descriptionField.getText(),
                    startDateTime,
                    endDateTime,
                    state
                );
            }
            return null;
        });

        // Afficher la boîte de dialogue et traiter le résultat
        Optional<TaskData> result = dialog.showAndWait();
        result.ifPresent(newTaskData -> {
            if (!newTaskData.getDescription().isEmpty()) {
                // Supprimer l'ancienne tâche
                tasksContainer.getChildren().remove(taskPane);
                // Ajouter la tâche modifiée
                createTaskItem(newTaskData);
            }
        });
    }

    private void updateTaskStyle(StackPane taskPane, TaskData taskData) {
        switch (taskData.getState()) {
            case "Terminer":
                taskPane.setStyle("-fx-background-color: #DDFFDD; -fx-background-radius: 6; -fx-padding: 10;");
                break;
            case "En Cours":
                taskPane.setStyle("-fx-background-color: #FFFFDD; -fx-background-radius: 6; -fx-padding: 10;");
                break;
            default: // En Attente
                taskPane.setStyle("-fx-background-color: WHITE; -fx-background-radius: 6; -fx-padding: 10;");
        }
    }

    private void handleTaskCompletion(StackPane taskPane, TaskData taskData, boolean completed) {
        tasksContainer.getChildren().remove(taskPane);
        
        LocalDateTime now = LocalDateTime.now();
        boolean wasOnTime = taskData.getEndDateTime().isAfter(now) || taskData.getEndDateTime().isEqual(now);
        
        if (completed) {
            updateTaskCounters(-1, 1, 0);
            if (wasOnTime) {
                coins += 30;
                progress = Math.min(100, progress + 10);
            }
        } else {
            updateTaskCounters(-1, 0, 1);
            if (!wasOnTime) {
                coins = Math.max(0, coins - 20);
                progress = Math.max(0, progress - 5);
            }
        }
        
        updateCoinsAndProgress();
        checkCurrentTasks();
    }

    private void checkCurrentTasks() {
        int currentTasks = 0;
        LocalDateTime now = LocalDateTime.now();
        
        for (javafx.scene.Node node : tasksContainer.getChildren()) {
            if (node instanceof StackPane) {
                StackPane taskPane = (StackPane) node;
                TaskData taskData = (TaskData) taskPane.getUserData();
                
                if (taskData.getState().equals("En Cours")) {
                    currentTasks++;
                }
            }
        }
        
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
                ex.printStackTrace();
            }
        });
    }

    private void loadTasksView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/Task.fxml"));
            Parent root = loader.load();
            
            TaskController taskController = loader.getController();
            if (taskController == null) {
                throw new RuntimeException("TaskController n'a pas été initialisé");
            }
            
            List<Task> tasksToPass = convertTaskDataToTask();
            taskController.updateTasks(tasksToPass);
            
            Stage stage = (Stage) tasksButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement de la vue des tâches");
        }
    }

    private List<Task> convertTaskDataToTask() {
        List<Task> tasks = new ArrayList<>();
        for (javafx.scene.Node node : tasksContainer.getChildren()) {
            if (node instanceof StackPane) {
                StackPane taskPane = (StackPane) node;
                TaskData taskData = (TaskData) taskPane.getUserData();
                
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                
                tasks.add(new Task(
                    taskData.getDescription(),
                    taskData.getState(),
                    taskData.getEndDateTime().format(dateFormatter),
                    taskData.getEndDateTime().format(timeFormatter)
                ));
            }
        }
        return tasks;
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (loader.getController() instanceof HomeController) {
                ((HomeController) loader.getController()).setUserName(fullNameField.getText().replace(" !", ""));
            }
            
            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement de la vue");
        }
    }

    public void setUserName(String name) {
        if (name != null && !name.isEmpty()) {
            fullNameField.setText(name + " !");
        } else {
            fullNameField.setText("Invité !");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe interne pour stocker les données de tâche
    private static class TaskData {
        private final String description;
        private final LocalDateTime startDateTime;
        private final LocalDateTime endDateTime;
        private final String state;

        public TaskData(String description, LocalDateTime startDateTime, LocalDateTime endDateTime, String state) {
            this.description = description;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.state = state;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getStartDateTime() {
            return startDateTime;
        }

        public LocalDateTime getEndDateTime() {
            return endDateTime;
        }

        public String getState() {
            return state;
        }
    }
}