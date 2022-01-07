package detectionPlotFX.plots;

import PamController.SettingsPane;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.SimpleFilterPaneFX;

/**
 * Waveform plot. Plots the waveforms from detections. 
 * @author Jamie Macaulay
 *
 */
public class WaveformSettingsPane extends SettingsPane<WaveformPlotParams>{
	
	/**
	 * Separate waveform check box. 
	 */
	private CheckBox seperateWaveform;
	
	/**
	 * Have a constant time scale check box. 
	 */
	//private CheckBox constantTimeScale;
	
	/**
	 * Show the filtered waveform check box.
	 */
	private CheckBox showWaveformEnvelope;

	/**
	 * Show filtered waveform check box.  
	 */
	private CheckBox showFilteredWaveform;

	/**
	 * Simple filter pane check box. 
	 */
	private SimpleFilterPaneFX simpleFilterPane;

	/**
	 * Reference to the waveform plot. 
	 */
	private WaveformPlot waveformPlot;

	/**
	 * Copy of waveform plot params. 
	 */
	private WaveformPlotParams waveformPlotParams; 
	
	private PamBorderPane mainPane = new PamBorderPane();

	/**
	 * Settings for the waveform plot. 
	 */
	public WaveformSettingsPane(WaveformPlot waveformPlot){
		super(null);
		this.waveformPlot=waveformPlot; 
		mainPane.setCenter(createWaveformPlot());
	}
	
	/**
	 * Create waveform plot
	 * @return the waveform plot pane. 
	 */
	public Pane createWaveformPlot(){
		
		PamVBox vBox= new PamVBox();
		vBox.setSpacing(5);
		vBox.setPadding(new Insets(15,5,5,15));
		
		Label label = new Label("Settings");
		PamGuiManagerFX.titleFont2style(label);
//		label.setFont(PamGuiManagerFX.titleFontSize2);
		vBox.getChildren().add(label);
		
		//create controls 
		seperateWaveform=new CheckBox("Seperate waveform"); 
		seperateWaveform.setOnAction((value)->{
			newSettings();
		});
		
//		constantTimeScale=new CheckBox("Constant time scale"); 
//		constantTimeScale.setOnAction((value)->{
//			newSettings();
//		});
		
		showWaveformEnvelope=new CheckBox("Show waveform envelope"); 
		showWaveformEnvelope.setOnAction((value)->{
			newSettings();
		});
		
		showFilteredWaveform=new CheckBox("Show filtered waveform");
		showFilteredWaveform.setOnAction((value)->{
			simpleFilterPane.getContentNode().setDisable(!showFilteredWaveform.isSelected());
			newSettings();
		});
		
		simpleFilterPane=new SimpleFilterPaneFX(Orientation.VERTICAL);
		//make sure there is a settings listener
		simpleFilterPane.addSettingsListener(()->{
			newSettings();
		});
		
		vBox.getChildren().addAll(seperateWaveform,
				showWaveformEnvelope, showFilteredWaveform, simpleFilterPane.getContentNode());
		
		return vBox;
	}
	
	/**
	 * Called whenever a control is set to allow for settings updates as they happen
	 */
	public void newSettings(){
		getParams(waveformPlot.getWaveformPlotParams());
		
		waveformPlot.reDrawLastUnit();
	}
	
	/**
	 * Get the paramters class for the pane. 
	 * @return a parameters class with all a params corresponding to setting of controls. 
	 */
	@Override
	public WaveformPlotParams getParams(WaveformPlotParams waveformPlotParams){
		
		waveformPlotParams.showSperateWaveform=seperateWaveform.isSelected();
		//waveformPlotParams.waveFixedXScale=constantTimeScale.isSelected();
		waveformPlotParams.waveShowEnvelope=this.showWaveformEnvelope.isSelected();
		waveformPlotParams.showFilteredWaveform=this.showFilteredWaveform.isSelected();
		waveformPlotParams.waveformFilterParams = this.simpleFilterPane.getParams(waveformPlotParams.waveformFilterParams);

		return waveformPlotParams;
	}
	
	/**
	 * Set the params
	 * @param waveformPlotParams
	 */
	public void setParams(WaveformPlotParams waveformPlotParams){
		
		//keep a copy if this is used as a dialog
		this.waveformPlotParams=waveformPlotParams.clone();
		
		
		//set settings for the waveform envelope. 
		seperateWaveform.setSelected(waveformPlotParams.showSperateWaveform);
	//	constantTimeScale.setSelected(waveformPlotParams.waveFixedXScale);
		showWaveformEnvelope.setSelected(waveformPlotParams.waveShowEnvelope);
		showFilteredWaveform.setSelected(waveformPlotParams.showFilteredWaveform);
		simpleFilterPane.setParams(waveformPlotParams.waveformFilterParams);
		//simpleFilterPane.setSampleRate(waveformPlot.getSampleRate());
		simpleFilterPane.getContentNode().setDisable(!showFilteredWaveform.isSelected());
	}

	@Override
	public String getName() {
		return "Waveform Plot Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	

	
	
	

}
