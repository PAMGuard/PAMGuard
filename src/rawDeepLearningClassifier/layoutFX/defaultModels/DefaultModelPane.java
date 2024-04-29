package rawDeepLearningClassifier.layoutFX.defaultModels;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javafx.application.HostServices;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamStackPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import rawDeepLearningClassifier.defaultModels.DLDefaultModelManager;
import rawDeepLearningClassifier.defaultModels.DLModel;

/**
 * Pane which allows users to view and select default models. 
 */
public class DefaultModelPane extends PamBorderPane{
	
	/**
	 * Reference to the deafult model manager that contains the default models. 
	 */
	private DLDefaultModelManager defaultModelManager;
	
	
	private PamBorderPane hidingPaneContent;


	private HidingPane hidingPane;
	
	ObjectProperty<DLModel> defaultModel = new SimpleObjectProperty();



	/**
	 * Constructor for the DefaultModelPane
	 * @param defaultModelManager - the default model manager to use. 
	 */
	public DefaultModelPane(DLDefaultModelManager defaultModelManager) {
		this.defaultModelManager=defaultModelManager;
		this.setCenter(createDefaultModelPane() );
	}
	
	
	
	private Pane createDefaultModelPane() {
		
		PamVBox vBox = new PamVBox(); 
		
//		vBox.setSpacing(5);
		
		Label label = new Label("Default Models");
		PamGuiManagerFX.titleFont2style(label);
		
		vBox.getChildren().add(label); 
//		vBox.setPrefWidth(120);

		hidingPaneContent= new PamBorderPane(); 
		hidingPaneContent.setPrefWidth(150);
		 hidingPane = new HidingPane(Side.RIGHT,  hidingPaneContent,  vBox, true, 0); 
	
		PamButton button;
		for (int i=0; i<defaultModelManager.getNumDefaultModels(); i++) {
			final int ii = i;
			button= new PamButton(defaultModelManager.getDefaultModel(ii).getName()); 
			button.prefWidthProperty().bind(vBox.widthProperty());
			button.setOnAction((action)->{
				hidingPaneContent.setCenter(createModelPane(defaultModelManager.getDefaultModel(ii))); 
				hidingPane.showHidePane(true);
			});
			
			if (i>0 && i<defaultModelManager.getNumDefaultModels()-1) {
			    button.setStyle("-fx-border-radius: 0 0 0 0; -fx-background-radius: 0 0 0 0");
			}
			else if (i==0) {
			    button.setStyle("-fx-border-radius: 5 5 0 0; -fx-background-radius: 5 5 0 0");
			}
			else {
			    button.setStyle("-fx-border-radius: 0 0 5 5 ; -fx-background-radius: 0 0 5 5");
			}
			
			vBox.getChildren().add(button); 
		}
				
		hidingPane.setStyle("-fx-background-color: -fx-base");
//		this.setStyle("-fx-background-color: -fx-base");

		PamStackPane mainHolder = new PamStackPane(); 
		mainHolder.getChildren().addAll(vBox, hidingPane); 
		StackPane.setAlignment(hidingPane, Pos.TOP_RIGHT);
		
		return mainHolder;
		
	}



	private Node createModelPane(DLModel dlModel) {
		
		Label titleLabel = new Label(dlModel.getName());
		titleLabel.setWrapText(true);
		PamGuiManagerFX.titleFont2style(titleLabel);
		titleLabel.setPadding(new Insets(25,0,0,0));
		
		hidingPane.getChildren().add(titleLabel);
		
		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);;
		
		Label descriptionLabel = new Label(dlModel.getDescription());
		descriptionLabel.setWrapText(true);
		
		Hyperlink link = new Hyperlink(dlModel.getCitation());
		link.setWrapText(true);
		link.setOnAction((action)->{
			try {
				Desktop.getDesktop().browse(dlModel.getCitationLink());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
//			HostServices.getInstance(this).getHostServices().showDocument(yahooURL)
		});
		
		
		PamButton importButton = new PamButton("Import");
		importButton.setGraphic(PamGlyphDude.createPamIcon("mdi2d-download", PamGuiManagerFX.iconSize));
		importButton.setOnAction((action)->{
			defaultModel.set(dlModel);
		});
		
		PamBorderPane buttonHolder = new PamBorderPane();
		buttonHolder.setRight(importButton);
		BorderPane.setAlignment(importButton, Pos.BOTTOM_RIGHT);
		VBox.setVgrow(buttonHolder, Priority.ALWAYS);
		
		
		vBox.getChildren().addAll(titleLabel, descriptionLabel, link, buttonHolder);
		vBox.setPrefHeight(200);
		
		return vBox;
	}
	

	public ObjectProperty<DLModel> defaultModelProperty() {
		return defaultModel;
	}

	
	

}
