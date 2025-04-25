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
import java.io.IOException;

public class LoginLogoutController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink createAccountLink;

    @FXML
    private void handleLogin(ActionEvent event) {
        if (validateFields()) {
            try {
                // Chargement de Hoooome2.fxml
                Parent root = FXMLLoader.load(getClass().getResource("/vue/Hoooome2.fxml"));
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                showError("Erreur de chargement de la vue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        try {
            // Chargement de Connection.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/vue/Connection.fxml"));
            Stage stage = (Stage) createAccountLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur de chargement de la vue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
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
}