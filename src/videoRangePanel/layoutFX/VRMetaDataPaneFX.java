package videoRangePanel.layoutFX;

import java.util.ArrayList;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * Pane which shows the metadata for an image or video. 
 * @author Jamie Macaulay
 *
 */
public class VRMetaDataPaneFX extends PamBorderPane {
	
	/**
	 * Text area shows the Metadata for the image. 
	 */
	private TextArea textArea;

	/**
	 * The meta data pane. 
	 */
	public VRMetaDataPaneFX(){
		createPane(); 
	}
	
	/**
	 * Create the pane 
	 */
	private void createPane() {
		this.textArea= new TextArea(); 
		textArea.setWrapText(true); 
		
		Label imageMetaData = new Label("Metadata"); 
		PamGuiManagerFX.titleFont2style(imageMetaData);
		//imageMetaData.setFont(PamGuiManagerFX.titleFontSize2);
		
		this.setTop(imageMetaData);
		this.setCenter(textArea);
	}
	
	public void setMetaText(ArrayList<String> text){
		//clear the pane
		textArea.setText(null);
		//set text strings
		textArea.appendText("Image Metadata"+"\n");
		if (text!=null){
			for (int i=0; i<text.size(); i++){
				textArea.appendText(text.get(i)+"\n");
			}
		}
		else {
			textArea.appendText("No metadata"+"\n");
		}
		//set the scroll bar to the top
//		this.revalidate();
		textArea.setScrollTop(0);
	}

}
