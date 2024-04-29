package whistlesAndMoans.dataSelector;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import whistlesAndMoans.alarm.WMAlarmParameters;

/**
 * JavaFX settings pane for the whsitle and moan detector data selector. 
 * 
 * @author Jamie Macaulay
 *
 */
public class WMDSelectPaneFX extends DynamicSettingsPane<Boolean> {
	
	private Pane mainPane;
	
	private WMDDataSelector wmdDataSelector;
	
	private Spinner<Double> minFreq;

	private Spinner<Double> maxFreq;

	private Spinner<Double> minAmplitude;

	private Spinner<Double> minLength;
	
	private PamToggleSwitch superDetOnly;

	public WMDSelectPaneFX(WMDDataSelector wmdDataSelector) {
		super(wmdDataSelector);
		this.wmdDataSelector =  wmdDataSelector;
		// TODO Auto-generated constructor stub
		mainPane = createPane();
	}
	
	
	private Pane createPane() {
		
		PamVBox mainPane = new PamVBox();
		mainPane.setSpacing(5.);
		
		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(5.);
		gridPane.setVgap(5.);
		
		int row = 0;
		int column = 0;

		minFreq = new Spinner<Double>(0.,Double.MAX_VALUE, 100., 100.);
		minFreq.setEditable(true);
		
		gridPane.add(new Label("Min. frequency"), column, row);
		column++;
		gridPane.add(minFreq, column, row);
		column++;
		gridPane.add(new Label("Hz"), column, row);

		maxFreq = new Spinner<Double>(0.,Double.MAX_VALUE, 30000., 100.);
		maxFreq.setEditable(true);
		
		row++;
		column=0;
		gridPane.add(new Label("Max. frequency"), column, row);
		gridPane.add(maxFreq, ++column, row);
		gridPane.add(new Label("Hz"), ++column, row);

		minAmplitude = new Spinner<Double>(0.,1000., 90., 1.);
		minAmplitude.setEditable(true);
		
		row++;
		column=0;
		gridPane.add(new Label("Min. amplitude"), column, row);
		gridPane.add(minAmplitude, ++column, row);
		gridPane.add(new Label("dB"), ++column, row);

		minLength = new Spinner<Double>(0.,Double.MAX_VALUE, 0., 1.);
		minLength.setEditable(true);
		
		row++;
		column=0;
		gridPane.add(new Label("Min. length"), column, row);
		gridPane.add(minLength, ++column, row);
		gridPane.add(new Label("milliseconds"), ++column, row);

		row++;
		column=0;
		superDetOnly = new PamToggleSwitch("Only whistles with super-detections");
		gridPane.add(superDetOnly, column, row);
		GridPane.setColumnSpan(superDetOnly, 3);

		mainPane.getChildren().add(gridPane); 

		return mainPane;
	}

	@Override
	public Boolean getParams(Boolean currParams) {
		
		WMAlarmParameters wmAlarmParameters = wmdDataSelector.getWmAlarmParameters().clone();
		try {
			wmAlarmParameters.minFreq 			= minFreq.getValue();
			wmAlarmParameters.maxFreq 			= maxFreq.getValue();
			wmAlarmParameters.minAmplitude 		= minAmplitude.getValue();
			wmAlarmParameters.minLengthMillis 	= minLength.getValue();
			wmAlarmParameters.superDetOnly 		= superDetOnly.isSelected();
		}
		catch (NumberFormatException e) {
			return false;
		}
		wmdDataSelector.setWmAlarmParameters(wmAlarmParameters);
		return true;
	}

	@Override
	public void setParams(Boolean input) {
		
		WMAlarmParameters wmAlarmParameters = wmdDataSelector.getWmAlarmParameters();

		minFreq.getValueFactory().setValue(wmAlarmParameters.minFreq);
		maxFreq.getValueFactory().setValue(wmAlarmParameters.maxFreq);
		minAmplitude.getValueFactory().setValue(wmAlarmParameters.minAmplitude);
		minLength.getValueFactory().setValue(wmAlarmParameters.minLengthMillis);
		
		superDetOnly.setSelected(wmAlarmParameters.superDetOnly);
		
	}

	@Override
	public String getName() {
		return "WMD Data Selector";
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
