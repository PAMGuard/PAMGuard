package IshmaelDetector.layoutFX;

import IshmaelDetector.EnergySumControl;
import IshmaelDetector.EnergySumParams;
import IshmaelDetector.IshDetParams;
import PamController.SettingsPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxNodes.utilsFX.ControlField;

/**
 * Settings pane for the Ishmael detector. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class EnergySumPane extends SettingsPane<IshDetParams> {
	
	/**
	 * The main pane. 
	 */
	private Pane mainPane; 

	/**
	 * The maximum frequency control
	 */
	private ControlField<Double> maxFreq;
	
	/**
	 * The minimum frequency control. 
	 */
	private ControlField<Double> minFreq;

	/**
	 * Reference to the energy sum control.  
	 */
	@SuppressWarnings("unused")
	private EnergySumControl ishEnergyControl;

	/**
	 * Check box for changing using log scale or not. 
	 */
	private PamToggleSwitch logScaleCheckBox;

	//comparing ratios
	
	/**
	 * Check box for comparing ratios in the Ishamel detector. 
	 */
	private PamToggleSwitch rationCheckBox;
	
	private ControlField<Double> minRatioFreq;

	private ControlField<Double> maxRatioFreq;

	/**
	 * Check box for the adaptive noise floor
	 */
	private PamToggleSwitch noiseFloorBox;

	//adaptive threshold 
	/**
	 * Long filter control for adaptive noise floor
	 */
	private ControlField<Double> longFilter;
	
	/**
	 * The spike threshold for the adaptive noise threshold. 
	 */
	private ControlField<Double> spikeThresh;
	

	//output smoothing
	
	/**
	 * Check box for enabling output smoothing. 
	 */
	private PamToggleSwitch outPutSmoothing;
	
	/**
	 * Long filter control for adaptive noise floor
	 */
	private ControlField<Double> shortFilter;
	
	public static final double INSET_RIGHT = 130; 

	public EnergySumPane(EnergySumControl ishEnergyControl) {
		super(null);
		this.ishEnergyControl=ishEnergyControl; 
		mainPane= createEnergyPane(); 
	}
	
	/**
	 * Create the energy pane. 
	 * @return the energy pane. 
	 */
	private Pane createEnergyPane() {
		
		PamVBox mainPane = new PamVBox(); 
		mainPane.setSpacing(5);
		
		Label titleLabel = new Label("Energy Sum"); 
		PamGuiManagerFX.titleFont2style(titleLabel);
		//titleLabel.setFont(PamGuiManagerFX.titleFontSize2);
		
		minFreq = new ControlField<Double>("Lower Frequency Bound", "Hz", 0, Double.MAX_VALUE, 500); 
		minFreq.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(3));
		minFreq.setTooltip(new Tooltip("The lower bound of the frequency band to analyse"));
//		minFreq.getLabel1().setPadding(new Insets(0,INSET_RIGHT,0,0));
		minFreq.getLabel1().setPrefWidth(INSET_RIGHT);

		maxFreq = new ControlField<Double>("Upper Frequency Bound", "Hz", 0, Double.MAX_VALUE, 500);
		maxFreq.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(3));
		maxFreq.setTooltip(new Tooltip("The upper bound frequency band to analyse"));
		maxFreq.getLabel1().setPrefWidth(INSET_RIGHT);

		rationCheckBox = new PamToggleSwitch("Use Energy Ratio");
		rationCheckBox.setTooltip(new Tooltip(
						"Sometimes, pulsed noises (clicks or thumps) can be enough like a call of interest\n" +
						"to trigger false detections by energy measurement. In this case, it can work to use\n "+
						"an energy ratio instead -- the ratio between energy in the band of interest and\n "+
						"energy in a nearby band."));
		rationCheckBox.selectedProperty().addListener((obsval, oldval, newval)->{
			enableControls(); 
			//adaptive noise floor and ration are not compatible
			if (rationCheckBox.isSelected()) noiseFloorBox.setSelected(false);
		});

		minRatioFreq = new ControlField<Double>("Lower Ratio Bound", "Hz", 0, Double.MAX_VALUE, 500); 
		minRatioFreq.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(3));
		minRatioFreq.setTooltip(new Tooltip("The lower bound frequency band to compare frequency ratios"));
		minRatioFreq.getLabel1().setPrefWidth(INSET_RIGHT);

		maxRatioFreq = new ControlField<Double>("Upper Ratio Bound", "Hz", 0, Double.MAX_VALUE, 500);
		maxRatioFreq.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(3));
		maxRatioFreq.setTooltip(new Tooltip("The upper bound frequency band to compare frequency ratios"));
		maxRatioFreq.getLabel1().setPrefWidth(INSET_RIGHT);

		
		
		noiseFloorBox = new PamToggleSwitch("Use Adaptive Threshold");
		noiseFloorBox.setTooltip(new Tooltip(
							"The default Ishmael detector uses a static threshold. If noise suddenly increases\n"
						+	"then the threshold can be below the noise either triggerring lots of detections or\n"
						+	"if max value over threshold value has been set appropriately none, even loud noises\n"
						+	"above the noise floor. An adaptive noise floor tracks the noise with a detection \n"
						+	"triggered if the energy measurment reaches threshold above the adaptive noise floor."));
		
		longFilter = new ControlField<Double>("Long filter", "", 0, Double.MAX_VALUE, 0.00001); 
		longFilter.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(9));
		longFilter.setTooltip(new Tooltip("The long filter. Lower values mean the adaptive noise floor changes more slowly with changing energy"));
		longFilter.getLabel1().setPrefWidth(INSET_RIGHT);

		spikeThresh = new ControlField<Double>("Spike Threshold", "", 1, Double.MAX_VALUE, 10); 
		spikeThresh.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(9));
		spikeThresh.setTooltip(new Tooltip(		"The maximum multiple above the Ishmael FFT detection output the adaptive threshold can be before\n"
											+ 	"an exponential decay is added. Since the adaptive threshold is essentially an averaging filter\n"
											+ 	"for the raw Ishmael FFT data, if there is a sudden and very loud noise then the value of the\n "
											+ 	"adaptive threshold can end up being so high it takes a long time to settle back to tracking the\n"
											+ 	"raw ouput. The spike threshold means that if this occurs the adaptive threshold will fall back to\n"
											+ 	"sensible values much faster."));
		spikeThresh.getLabel1().setPrefWidth(INSET_RIGHT);

		noiseFloorBox.selectedProperty().addListener((obsval, oldval, newval)->{
			enableControls();
			//adaptive noise floor and ration are not compatible
			if (noiseFloorBox.isSelected()) {
				rationCheckBox.setSelected(false);
			}
		});

		
		outPutSmoothing = new PamToggleSwitch("Use Detector Smoothing");
		outPutSmoothing.setTooltip(new Tooltip(
							"It can be advantageous to smooth the output from the detector. This can mean that the detectors\n "
							+ "are more robust for detecting some multi modal sounds, e.g. dynamite bombs, which have multiple\n"
							+ "closely spaced peaks do not briefly go below threshold and thus are not detected.")); 
		outPutSmoothing.selectedProperty().addListener((obsval, oldval, newval)->{
			enableControls();
		});
		
		shortFilter = new ControlField<Double>("Short filter			", "", 0, Double.MAX_VALUE, 0.1); 
		shortFilter.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(9));
		shortFilter.setTooltip(new Tooltip("The short filter which defines smoothing. Lower values mean the smoothing is greater"));
		shortFilter.getLabel1().setPrefWidth(INSET_RIGHT);
		
		logScaleCheckBox = new PamToggleSwitch("Use log scale"); 
		
		mainPane.getChildren().addAll(titleLabel, minFreq, maxFreq, rationCheckBox, minRatioFreq, maxRatioFreq,
				noiseFloorBox, longFilter, spikeThresh, outPutSmoothing, shortFilter, logScaleCheckBox);
		
		enableControls(); 
		
		return mainPane; 
	}
	
	/**
	 * Enable and disable controls based on parameter selection. 
	 */
	private void enableControls() {
		
		longFilter.setDisable(!noiseFloorBox.isSelected());
		spikeThresh.setDisable(!noiseFloorBox.isSelected());

		minRatioFreq.setDisable(!rationCheckBox.isSelected());
		maxRatioFreq.setDisable(!rationCheckBox.isSelected());
		
		shortFilter.setDisable(!outPutSmoothing.isSelected());
	}
	

	@Override
	public String getName() {
		return "Ishmael Energy Sum";
	}


	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EnergySumParams getParams(IshDetParams currParams) {
		EnergySumParams params = (EnergySumParams) currParams; 
		
		//the params 
		params.f0 = minFreq.getValue();
		params.f1 = maxFreq.getValue();
		params.useLog = logScaleCheckBox.isSelected(); 
		
		//energy ratio
		params.useRatio= rationCheckBox.isSelected();
		params.ratiof0=minRatioFreq.getValue(); 
		params.ratiof1=maxRatioFreq.getValue(); 
		
		//adaptive noise floor. 
		params.adaptiveThreshold = noiseFloorBox.isSelected(); 
		params.longFilter= longFilter.getValue(); 
		params.spikeDecay=spikeThresh.getValue(); 
		
		//detector smoothing
		params.outPutSmoothing = outPutSmoothing.isSelected(); 
		params.shortFilter= shortFilter.getValue(); 

		return params;
	}

	@Override
	public void setParams(IshDetParams input) {
		
		EnergySumParams params = (EnergySumParams) input; 
		
		//the minimum and maximum frequency limits. 
		minFreq.setValue(params.f0); 
		maxFreq.setValue(params.f1); 
		
		//energy ratio
		rationCheckBox.setSelected(params.useRatio);
		
		minRatioFreq.setValue(params.ratiof0); 
		maxRatioFreq.setValue(params.ratiof1); 
		
		//adaptive noise floor
		noiseFloorBox.setSelected(params.adaptiveThreshold);
		longFilter.setValue(params.longFilter); 
		spikeThresh.setValue(params.spikeDecay); 
		
		//detector smoothing
		outPutSmoothing.setSelected(params.outPutSmoothing);
		shortFilter.setValue(params.shortFilter);
		
		//set log scale to true
		this.logScaleCheckBox.setSelected(params.useLog);
		
		//enable the controls. 
		enableControls(); 

	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}


	

	

}
