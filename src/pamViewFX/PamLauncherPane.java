package pamViewFX;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxStyles.PamAtlantaStyle;
import pamViewFX.fxStyles.PamDefaultStyle;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * Shows a launcher pane which allows a user to open a real time or viewer configuration. 
 */
public class PamLauncherPane extends PamBorderPane {
	
	public static final double BUTTON_SIZE = 120; 
	
	public PamLauncherPane() {
		
		PamButton buttonNormal = new PamButton("Real time");
		buttonNormal.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
		PamButton buttonViewer = new PamButton("Post processing");
		buttonViewer.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

		Path pathNormal = Paths.get("./src/Resources/pamguardIcon.png");
		Path pathViewer = Paths.get("./src/Resources/pamguardIconV.png");

		Image img;
		
		//create the normal mode button
		try {
			img = new Image(pathNormal.toUri().toURL().toExternalForm());
		    ImageView view = new ImageView(img);
		    buttonNormal.setGraphic(view);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		buttonNormal.setContentDisplay(ContentDisplay.TOP);
		
		//create the viewer mode button
		try {
			img = new Image(pathViewer.toUri().toURL().toExternalForm());
		    ImageView view = new ImageView(img);
		    buttonViewer.setGraphic(view);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		buttonViewer.setContentDisplay(ContentDisplay.TOP);
		
		HBox butttonBox = new HBox(); 
		butttonBox.setSpacing(40);
		butttonBox.setPadding(new Insets(40,40,40,40));
		
		butttonBox.getChildren().addAll(buttonNormal, buttonViewer);
		
		
		PamToggleSwitch newVersionSwitch = new PamToggleSwitch("PAMGuardFX"); 
		newVersionSwitch.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			this.getStylesheets().clear();
			if (newVal) {
				//this.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
//				Platform.runLater(()->{
//			        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
//		        });
				PamStylesManagerFX.getPamStylesManagerFX().setCurStyle(new PamAtlantaStyle());
		        this.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS());
		        this.setStyle("-fx-background-color: -fx-darkbackground");
			}
		});

		BorderPane.setAlignment(newVersionSwitch, Pos.CENTER_RIGHT);	
		newVersionSwitch.setAlignment(Pos.CENTER_RIGHT);
		this.setTop(newVersionSwitch);		
		this.setCenter(butttonBox);
		
		this.setPadding(new Insets(5,5,5,5)); 
		
		
//        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
//        this.getStylesheets().add(getClass().getResource(primerPAMGuard).toExternalForm());
//        this.getStylesheets().add(getClass().getResource(new PrimerDark().getUserAgentStylesheet()).toExternalForm());
		PamStylesManagerFX.getPamStylesManagerFX().setCurStyle(new PamDefaultStyle());
		

	}

}
