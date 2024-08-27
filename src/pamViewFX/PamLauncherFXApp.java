package pamViewFX;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class PamLauncherFXApp extends Application {
	
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("PAMGuard Launcher");
      
        
        StackPane root = new StackPane();
        root.getChildren().add(new PamLauncherPane());
        primaryStage.setScene(new Scene(root, 500, 250));
        primaryStage.show();
    }
}
