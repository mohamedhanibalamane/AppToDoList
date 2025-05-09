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

public class ConnectionController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signupButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private void handleSignup(ActionEvent event) {
        if (validateFields()) {
            try {
                // Vérifier si l'utilisateur existe déjà
                if (UserDAO.userExists(emailField.getText())) {
                    showError("Cet email est déjà utilisé");
                    return;
                }
                
                // Ajouter le nouvel utilisateur
                UserDAO.addUser(
                    fullNameField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText()
                );
                
                // Charger la vue Home
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vue/Hoooome2.fxml"));
                Parent root = loader.load();
                HomeController homeController = loader.getController();
                homeController.setUserName(fullNameField.getText().trim());
                
                Stage stage = (Stage) signupButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("To-Do List Application - Dashboard");
                stage.show();
            } catch (SQLException e) {
                showError("Erreur de base de données: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                showError("Erreur de chargement de la vue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLoginLink(ActionEvent event) {
        try {
            // Chargement de la vue de connexion
            Parent root = FXMLLoader.load(getClass().getResource("/vue/LoginLogout.fxml"));
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("To-Do List - Connexion");
            stage.show();
        } catch (IOException e) {
            showError("Erreur de chargement de la vue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        // Validation du nom complet
        if (fullNameField.getText().isEmpty()) {
            showError("Le nom complet est obligatoire");
            return false;
        }

        // Validation de l'email
        if (emailField.getText().isEmpty()) {
            showError("L'email est obligatoire");
            return false;
        }

        if (!emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Veuillez entrer une adresse email valide");
            return false;
        }

        // Validation du mot de passe
        if (passwordField.getText().isEmpty()) {
            showError("Le mot de passe est obligatoire");
            return false;
        }

        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }

        // Validation de la confirmation du mot de passe
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas");
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
}