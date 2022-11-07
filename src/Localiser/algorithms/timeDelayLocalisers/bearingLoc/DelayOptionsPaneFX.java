package Localiser.algorithms.timeDelayLocalisers.bearingLoc;


import Localiser.DelayMeasurementParams;
import PamController.SettingsPane;
import fftFilter.FFTFilterParams;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.SimpleFilterPaneFX;

public class DelayOptionsPaneFX extends SettingsPane<DelayMeasurementParams> {
	
	
	private CheckBox filterBearings, envelopeBearings, useLeadingEdge, upSample;
	private PamButton filterSettings;
	private Label filterDescription;
	
	/**
	 * The current delay measurement parameters. 
	 */
	private DelayMeasurementParams delayMeasurementParams;

	
	/**
	 * Check box for restricting samples. 
	 */
	private CheckBox restrictSamples;
	
	/**
	 * Restrict the number of samples. 
	 */
	private TextField restrictSamplesField;
	
	private PamSpinner<Integer> upSampleSpinner;
	
	
	private SimpleFilterPaneFX simpleFilterPane = new SimpleFilterPaneFX();

	public DelayOptionsPaneFX() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	
	
	public Pane createDelayOptionsPane() {
		int gridy=0; 
		
		PamGridPane mainPanel = new PamGridPane();
//		mainPanel.setBorder(new TitledBorder("Delay measurement options"));
		mainPanel.add(filterBearings = new CheckBox("Filter data before measurement"),  0,gridy);
		PamGridPane.setColumnSpan(filterBearings, 2);
		filterBearings.setOnAction((action)->{
			enableControls();
		});
		gridy++;
		
		mainPanel.add(filterSettings = new PamButton("Settings"), 0,gridy);
		filterSettings.setOnAction((action)->{
			FFTFilterParams newParams = simpleFilterPane.getParams(delayMeasurementParams.delayFilterParams);
			if (newParams != null) {
				delayMeasurementParams.delayFilterParams = newParams.clone();
				describeFilter();
			}
		});
		gridy++;
		
		mainPanel.add(filterDescription = new Label(" "), 0,gridy);
		gridy++;
		mainPanel.add(upSample = new CheckBox("Up sample data x2"), 0,gridy);
		upSample.setTooltip(new Tooltip("Up sampling data to a higher frequency can improve timing accuracy for narrow band clicks (i.e. harbour porpoise)"));
		gridy++;

		
		//SpinnerListModel spinnerModel = new SpinnerListModel(Arrays.asList(new Integer[] {2,3,4})); //restrict the spinenr options. 
		
		mainPanel.add(upSampleSpinner = new PamSpinner<Integer>(2,4,2,1), 0,gridy);
		upSampleSpinner.valueProperty().addListener((obsval, oldVa, newVal)->{
			upSample.setText("Up sample data x" + this.upSampleSpinner.getValue());
		}); 
		gridy++;

		mainPanel.add(envelopeBearings = new CheckBox("Use waveform envelope"),  0, gridy);
		gridy++;

		
		mainPanel.add(new Label("     "),  0, gridy);
		gridy++;

		mainPanel.add(new Label("     "), 0, gridy);
		gridy++;
		
		
		mainPanel.add(useLeadingEdge = new CheckBox("Use envelope leading edge only"),  0, gridy);
		gridy++;
		PamGridPane.setColumnSpan(useLeadingEdge, 2);
		envelopeBearings.setOnAction((action)->{
			enableControls();
		});
		envelopeBearings.setOnAction((action)->{
			enableControls();
		});
		PamGridPane.setColumnSpan(envelopeBearings, 2);


		//restrict
		mainPanel.add(restrictSamples = new CheckBox("Restrict length"),  0, gridy);
		gridy++;
		PamGridPane.setColumnSpan(restrictSamples, 2);
		restrictSamples.setOnAction((action)->{
			enableControls();
		});
		mainPanel.add(restrictSamplesField = new TextField(), 2, gridy);

	
		restrictSamples.setTooltip(new Tooltip("In environments where echoes are an issue restricting inital samples of detections "
				+ "(e.g. click snippets) is a simple but effective way to increase the accuracy of  time delay calculations. "
				+ "WARNING: Remember that this must cover the potential time delay in grouped detections "));
		filterBearings.setTooltip(new Tooltip("Filter data prior to bearing measurement to imporve accuracy"));
		filterSettings.setTooltip(new Tooltip("Setup filter options"));
		envelopeBearings.setTooltip(new Tooltip("Using the envelope can provide more accurate bearings for some narrowband pulses"));
		filterDescription.setTooltip(new Tooltip("Current filter settings"));
		useLeadingEdge.setTooltip(new Tooltip("For long pulses, or where there are echoes, restrict the calculation to the leading edge of the envelope"));
		
		BorderPane pane = new BorderPane();
		pane.setCenter(mainPanel);
		
		return pane;
	}
	
	
	private void enableControls() {
		filterSettings.setDisable(!filterBearings.isSelected());
		filterDescription.setDisable(!filterBearings.isSelected());
		useLeadingEdge.setDisable(!envelopeBearings.isSelected());
		restrictSamplesField.setDisable(!restrictSamples.isSelected());
		if (!envelopeBearings.isSelected()) {
//			useLeadingEdge.setSelected(false);
		}
	}
	
	private void describeFilter() {
		if (delayMeasurementParams == null || delayMeasurementParams.delayFilterParams == null) {
			filterDescription.setText("No filter");
			return;
		}
		filterDescription.setText(delayMeasurementParams.delayFilterParams.toString());
	}
	
	@Override
	public void setParams(DelayMeasurementParams delayMeasurementParams) {
		this.delayMeasurementParams = delayMeasurementParams;
		filterBearings.setSelected(delayMeasurementParams.filterBearings);
		upSample.setSelected(delayMeasurementParams.getUpSample() > 1);
		envelopeBearings.setSelected(delayMeasurementParams.envelopeBearings);
		useLeadingEdge.setSelected(delayMeasurementParams.useLeadingEdge);
		restrictSamples.setSelected(delayMeasurementParams.useRestrictedBins);
		restrictSamplesField.setText(String.format("%d", delayMeasurementParams.restrictedBins));
		
		upSample.setText("Up sample data x" + this.upSampleSpinner.getValue());
		
		enableControls();
		describeFilter();
	}
	
	@Override
	public DelayMeasurementParams getParams(DelayMeasurementParams delayMeasurementParams) {
		delayMeasurementParams.delayFilterParams = this.delayMeasurementParams.delayFilterParams;
		delayMeasurementParams.filterBearings = filterBearings.isSelected();
		delayMeasurementParams.setUpSample(upSample.isSelected() ? ((Integer) this.upSampleSpinner.getValue()).intValue() : 1);
		delayMeasurementParams.envelopeBearings = envelopeBearings.isSelected();
		delayMeasurementParams.useLeadingEdge = useLeadingEdge.isSelected() && delayMeasurementParams.envelopeBearings;
		
		delayMeasurementParams.useRestrictedBins=this.restrictSamples.isSelected(); 
		
		try {
			delayMeasurementParams.restrictedBins=Integer.valueOf(this.restrictSamplesField.getText()); 
		}
		catch(Exception e) {
			 PamDialogFX.showWarning(null, "Delay measurement settings", "The entry in the samples text field is invalid.");
			 return null;
		}
		
		if (delayMeasurementParams.useRestrictedBins && delayMeasurementParams.restrictedBins<10) {
			 PamDialogFX.showWarning(null, "Delay measurement settings", "The entry in the samples text field is invalid. It must be >= 10");
			 return null;
		}
		
		if (delayMeasurementParams.filterBearings && delayMeasurementParams.delayFilterParams == null) {
			 PamDialogFX.showWarning(null, "Delay measurement settings", "Filter parameters have not been set");
			 return null;
		}
		return null;
	}

	@Override
	public String getName() {
		return "Delay Measurment Params";
	}

	@Override
	public Node getContentNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
