package model;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import vue.*;
import controller.*; 
public class Main extends Application {
	
	
	
	@Override
	public void start(Stage stage) {
		try {

			Parent root = FXMLLoader.load(getClass().getResource("/vue/Connection.fxml"));
			Scene scene = new Scene(root, 800, 600);
			Image icon = new Image("to_doo.jpg");
			stage.getIcons().add(icon);
			
			stage.setScene(scene);
			stage.show();
			
			stage.setOnCloseRequest(event -> {
				event.consume();
				logout(stage);	
			});
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void logout(Stage stage){	
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Logout");
		alert.setHeaderText("You're about to logout!");
		alert.setContentText("Do you want to save before exiting?");
		
		if (alert.showAndWait().get() == ButtonType.OK){
			System.out.println("You successfully logged out");
			stage.close();
		} 
	}

	public static void main(String[] args) {
		launch(args);
	}
}