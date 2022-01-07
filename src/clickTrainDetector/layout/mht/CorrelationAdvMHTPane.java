package clickTrainDetector.layout.mht;

import PamguardMVC.PamDataBlock;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.CorrelationChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.SimpleFilterPaneFX;

/**
 * Advanced settings pane for correlation
 * @author Jamie Macaulay
 *
 */
public class CorrelationAdvMHTPane extends AdvMHTVarPane { 


	/**
	 * Simple filter pane. 
	 */
	private SimpleFilterPaneFX filterPane;

	/**
	 * Check box selecting whether to have a pre filtering stage before correlation calculations. 
	 */
	private CheckBox useFilterBox ;

	/**
	 * Reference ot the current datablock selected in the GUI (may noit necassirily be the same datablock as
	 * currently set in the controlled unit as user will be changing settings in pane before saved)
	 */
	private PamDataBlock currentDataBlock = null;


	public CorrelationAdvMHTPane(SimpleChi2VarParams simpleChi2Var2 ) {
		super(simpleChi2Var2);
	}



	@Override
	protected Pane createAdvPane() {
		Pane pane  = super.createAdvPane();

		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);

		vBox.getChildren().add(pane); 

		useFilterBox = new CheckBox("Pre filter waveforms"); 
		useFilterBox.setOnAction(action->{
			enableFilterPane(useFilterBox.isSelected());
		});
		useFilterBox.setTooltip(new Tooltip(
				"If selected then waveforms are filtered using the filter settings \n"
						+ "before correlation values are clculated. Use this to filter out \n"
						+ "unwanted sections of the spectrum."));

		GridPane.setColumnSpan(useFilterBox,3);
		vBox.getChildren().add(useFilterBox);

		filterPane = new SimpleFilterPaneFX(); 

		GridPane.setColumnSpan(filterPane.getContentNode(), 10);
		vBox.getChildren().add(filterPane.getContentNode());

		return vBox;
	}

	/**
	 * Enable or disable the filter pane.
	 * @param enable - true to enable. 
	 */
	private void enableFilterPane(boolean enable) {
		filterPane.getContentNode().setDisable(!enable);
	}


	@Override
	public CorrelationChi2Params getParams(SimpleChi2VarParams currParams) {	


		CorrelationChi2Params newParams = new CorrelationChi2Params(super.getParams(currParams));

		System.out.println("GETPARAMS:fftFilterParams_OLD HIGHPASS: " + newParams.fftFilterParams.highPassFreq);

		newParams.fftFilterParams=filterPane.getParams(newParams.fftFilterParams); 

		System.out.println("GETPARAMS:fftFilterParams_NEW HIGHPASS: " + newParams.fftFilterParams.highPassFreq);

		newParams.useFilter=this.useFilterBox.isSelected();

		return newParams; 
	}


	@Override
	public void setParams(SimpleChi2VarParams currParams) {
		CorrelationChi2Params newParams = (CorrelationChi2Params) currParams;

		//set the parameters. 
		super.setParams(newParams);

	
		if (currentDataBlock!=null) {
			filterPane.setSampleRate(this.currentDataBlock.getSampleRate());			
		}
		else {
			System.err.println("CorrelationAdvPane: No sample rate available for FFT filler"); 
			filterPane.setSampleRate(1000);			
		}

		
		//set params - needs to be after settings sample rate. 
		filterPane.setParams(newParams.fftFilterParams);


		//		////FIXME - urrghh this is NOT how we should be setting the sample rate...
		//		if (PamController.getInstance().getRawDataBlock(0)!=null) {
		//			filterPane.setSampleRate(PamController.getInstance().getRawDataBlock(0).getSampleRate());
		//		}
		//		else {
		//			
		//		}
		useFilterBox.setSelected(newParams.useFilter); 
		enableFilterPane(useFilterBox.isSelected());
	}
	
	@Override
	public void notifyChange(int flag, Object data) {
		switch (flag) {
		case ClickTrainControl.NEW_PARENT_DATABLOCK:
			//pass along to the MHTChi2 pane. Nothing to change here. 
			this.currentDataBlock =  (PamDataBlock) data;
			break; 
		}
	}

}
