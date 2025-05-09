package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.UserDAO;

import java.io.IOException;
import java.sql.SQLException;

public class LoginLogoutController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink createAccountLink;

    @FXML
    private void handleLogin(ActionEvent event) {
        if (validateFields()) {
            try {
                String email = emailField.getText().trim();
                String password = passwordField.getText();
                
                // 1. Validation des identifiants
                if (!UserDAO.validateLogin(email, password)) {
                    showError("Email ou mot de passe incorrect");
                    return;
                }
                
                // 2. Récupération des infos utilisateur
                String fullName = UserDAO.getUserFullName(email);
                if (fullName == null) {
                    showError("Impossible de récupérer les informations utilisateur");
                    return;
                }
                
                // 3. Chargement de la vue Home
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/Hoooome2.fxml"));
                Parent root = loader.load();
                
                // 4. Configuration du contrôleur Home
                HomeController homeController = loader.getController();
                if (homeController == null) {
                    throw new RuntimeException("HomeController non initialisé");
                }
                
                homeController.initUserData(email, fullName);
                
                // 5. Affichage de la nouvelle scène
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("To-Do List Application - Dashboard");
                stage.show();
                
            } catch (SQLException e) {
                showDatabaseError(e);
            } catch (IOException e) {
                showError("Erreur de chargement de la vue: " + e.getMessage());
                e.printStackTrace();
            } catch (RuntimeException e) {
                showError("Erreur d'initialisation: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/vue/Connection.fxml"));
            Stage stage = (Stage) createAccountLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("To-Do List - Création de compte");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de chargement de la vue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        // Validation de l'email
        if (emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showError("Tous les champs sont obligatoires");
            return false;
        }

        if (!emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Veuillez entrer une adresse email valide");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showDatabaseError(SQLException e) {
        showError("Erreur de base de données: " + e.getMessage());
        e.printStackTrace();
    }
}