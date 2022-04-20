package Acquisition.layoutFX;

import Acquisition.FolderInputSystem;
import javafx.scene.control.Label;
import pamViewFX.fxNodes.PamBorderPane;


/**
 * Pane with controls to fix wave file headers. 
 * @author Jamie Macaulay
 *
 */
public class FixWavPane extends PamBorderPane {
	
	public FixWavPane(FolderInputSystem folderInputSystem) {
		this.setCenter(new Label("Hello fix wav pane"));
	}

}
