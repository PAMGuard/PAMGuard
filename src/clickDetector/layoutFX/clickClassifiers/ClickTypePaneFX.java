package clickDetector.layoutFX.clickClassifiers;

import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import clickDetector.BasicClickIdParameters;
import clickDetector.ClickControl;
import clickDetector.ClickTypeParams;


/**
 * Pane for changing basic click classifier settings. 
 * @author Jamie Macaulay	
 *
 */
public class ClickTypePaneFX extends SettingsPane<ClickTypeProperty> {
	

    private ClickControl clickControl = null;

	ClickTypeParams clickTypeParams;
	
	BasicClickIdParameters basicClickIdParameters;

	static private int FREQ_FIELD_WIDTH = 8;

	private TextField name, code;
	
	private CheckBox[] enableBoxes = new CheckBox[5];

	private TextField[] band1Freq = new TextField[2]; // frequency range for test band

	private TextField[] band2Freq = new TextField[2]; // frequency range for control
												// band

	private TextField[] band1Energy = new TextField[2]; // energy range for test
													// band

	private TextField[] band2Energy = new TextField[2]; // energy range for control
													// band

	private TextField bandEnergyDifference; // minimum difference in ban energiesin
										// dB

	private TextField[] peakFrequencySearch = new TextField[2]; // search range for
															// peak frequency
															// (Hz)

	private TextField[] peakFrequencyRange = new TextField[2]; // allowable range
															// for peak
															// frequency (Hz)

	private TextField[] peakWidth = new TextField[2]; // max width of frequency peak
												// (Hz)

	private TextField widthEnergyFraction; // energy fraction to use in width
									// calculation

	private TextField[] clickLength = new TextField[2]; // allowable lengh of clicks
													// in ms.
	
	private TextField[] meanSumRange = new TextField[2];
	private TextField[] meanSelRange = new TextField[2];

	private TextField lengthEnergyFraction;

    /**
     * Maximum amount of elapsed time between detections to ring the alarm
     */
    private TextField maxTime;

	private PamButton symbolTypeButton, filtersButton;

	
    /**
     * ComboBox to list available alarms
     */
    private ComboBox alarmChooser;


    private PamBorderPane mainPane = new PamBorderPane(); 
	
	public ClickTypePaneFX(){
		super(null); 
		mainPane = new PamBorderPane(); 
		mainPane.setCenter(createClickTypePane());
	}


	private Node createClickTypePane() {
		return new Label("Helllo!!");
	}

	@Override
	public void setParams(ClickTypeProperty input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Basic Click Classifer Pane";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ClickTypeProperty getParams(ClickTypeProperty currParams) {
		// TODO Auto-generated method stub
		return null;
	}



}
