package pamViewFX.fxNodes.connectionPane.test;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;
import pamViewFX.fxNodes.PamScrollPane;
import pamViewFX.fxNodes.connectionPane.ConnectionPane;
import pamViewFX.fxNodes.connectionPane.structures.ConnectionGroupStructure;
import pamViewFX.fxNodes.connectionPane.structures.ExtensionSocketStructure;

/**
 * Simple application to test a connectionPane;
 * @author Jamie Macaulay 
 *
 */
public class ConnectioPaneTest extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Hello Connection Pane!");

		double width = 700;
		double height = 700;

		ConnectionPane connectionPane = new ConnectionPane(); 
		connectionPane = new ConnectionPane(); 

		int count=0; 
		
//		//TEST 1//
//		for (int i=0; i<3; i++){
//			StandardConnectionNode connectionNode=new StandardConnectionNode(connectionPane, Orientation.VERTICAL);
//			connectionPane.addNewConnectionNode(connectionNode, Math.random()*width, Math.random()*height); 
//			connectionNode.getConnectionNodeBody().getChildren().add(new Label(String.format("%d", count))); 
//			count++;
//		}
//
//		for (int i=0; i<3; i++){
//			StandardConnectionNode connectionNode=new StandardConnectionNode(connectionPane, Orientation.HORIZONTAL);
//			connectionPane.addNewConnectionNode(connectionNode,Math.random()*width, Math.random()*height);
//			connectionNode.getConnectionNodeBody().getChildren().add(new Label(String.format("%d", count))); 
//			connectionNode=new StandardConnectionNode(connectionPane, Orientation.HORIZONTAL);
//			count++; 
//		}
//
//		//add a connection structure
//		ConnectionGroupStructure connectionGroupNode = new ConnectionGroupStructure(connectionPane); 
//		connectionPane.addNewConnectionNode(connectionGroupNode, Math.random()*width, Math.random()*height);
		
		//TEST 2//
		StandardConnectionNode connectionNode=new StandardConnectionNode(connectionPane, Orientation.VERTICAL);
		connectionPane.addNewConnectionNode(connectionNode, Math.random()*width, Math.random()*height); 
		connectionNode.getConnectionNodeBody().getChildren().add(new Label(String.format("%d", count))); 
		count++;
		
		
		connectionNode=new StandardConnectionNode(connectionPane, Orientation.HORIZONTAL);
		connectionPane.addNewConnectionNode(connectionNode, Math.random()*width, Math.random()*height); 
		connectionNode.getConnectionNodeBody().getChildren().add(new Label(String.format("%d", count))); 
		count++;
//		
		//add a connection structure
		ConnectionGroupStructure connectionGroupNode = new ConnectionGroupStructure(connectionPane); 
		connectionPane.addNewConnectionNode(connectionGroupNode, Math.random()*width, Math.random()*height);
		
		
		ExtensionSocketStructure extensionStructure = new ExtensionSocketStructure(connectionPane); 
		connectionPane.addNewConnectionNode(extensionStructure, Math.random()*width, Math.random()*height);

		
		//TEST END//
				
		
		PamScrollPane scrollPane = new PamScrollPane();
		scrollPane.setContent(connectionPane);

		StackPane root = new StackPane();
		root.getChildren().add(scrollPane);
		primaryStage.setScene(new Scene(root, 700, 700));
		primaryStage.show();
	}
}

