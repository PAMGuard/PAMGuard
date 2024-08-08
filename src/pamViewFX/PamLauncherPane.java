package pamViewFX;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;

/**
 * Shows a launcher pane which allows a user to open a real time or viewer configuration. 
 */
public class PamLauncherPane extends PamBorderPane {
	
	public PamLauncherPane() {
		
		PamButton buttonNormal = new PamButton("Real time");
		buttonNormal.setGraphic(buttonNormal);
		
		 Image img = new Image("./src/Resources/pamguardIcon.png");
	      ImageView view = new ImageView(img);
	      
	      buttonNormal.setGraphic(view);
		
		this.setLeft(buttonNormal);
		
	}

}
