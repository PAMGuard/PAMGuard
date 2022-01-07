package pamViewFX.fxNodes.orderedList;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class PamDraggableListApp extends Application {
	
    public static void main(String[] args) {
        launch(PamDraggableListApp.class);
    }


	@Override
	public void start(Stage primaryStage) throws Exception {
		
		ArrayList<Pane > panes = new ArrayList<Pane>(); 
		
		BorderPane borderPane; 
		
		for (int i=0; i<7; i++) {
			borderPane = new BorderPane(); 
			borderPane.setCenter(new Label("Hello " + i));
			panes.add(borderPane); 
		}
		
		PamDraggableList draggableList= new PamDraggableList(panes);  
		
		primaryStage.setScene(new Scene(draggableList, 890, 570));
		primaryStage.show();
		
	}

}
