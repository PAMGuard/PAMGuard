package Acquisition.layoutFX;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * A simple DAQ status pane factory that does nothing but show some text. 
 * @author Jamie Macaulay
 */
public class SimpleStatusPaneFactory extends DAQStatusPaneFactory {
	
	private String text;

	public SimpleStatusPaneFactory(String text) {
		this.text = text; 
	}

	@Override
	public PaneFactoryPane createPane() {
		PaneFactoryPane pane =  new PaneFactoryPane(this);
		pane.setCenter(new Label(text));
		return pane;
	}
	
	public String getPaneFactoryName(){
		return "Simple Status Pane Factory";
	}
	
}
