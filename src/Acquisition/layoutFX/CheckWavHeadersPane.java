package Acquisition.layoutFX;

import Acquisition.FolderInputSystem;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import pamViewFX.fxNodes.PamBorderPane;


/**
 * Pane with controls to fix wave file headers. 
 * @author Jamie Macaulay
 *
 */
public class CheckWavHeadersPane extends PamBorderPane {
	
	
	/**
	 * The text area.
	 */
	private TextArea textArea;
	
	/**
	 * The nma eo fhte label
	 */
	private Label folderName;
	
	/**
	 * Progress bar showing progress of file write
	 */
	private ProgressBar progressBar;

	public CheckWavHeadersPane(FolderInputSystem folderInputSystem) {
		this.setCenter(new Label("Hello fix wav pane"));
		
		
	folderName = new Label(" ");
		textArea = new TextArea();
		textArea.setEditable(false);
		ScrollPane scrollPane = new ScrollPane(textArea);
		scrollPane.setPrefSize(322, 300);
		this.setCenter(scrollPane);
		
		progressBar = new ProgressBar();
		this.setBottom(progressBar);

	}

}
