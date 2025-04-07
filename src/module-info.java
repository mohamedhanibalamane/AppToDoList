module AppToDoList {
			requires javafx.controls;
			requires javafx.graphics;
			requires javafx.fxml;
			requires javafx.base;
			requires javafx.media;
			requires java.desktop;
		
		opens model to javafx.graphics, javafx.fxml;
		opens controller to javafx.graphics, javafx.fxml;
		opens vue to javafx.graphics, javafx.fxml;
	}
