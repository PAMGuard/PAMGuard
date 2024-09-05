package cpod.fx;

import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.controlsfx.control.RangeSlider;

import PamView.panel.PamPanel;
import cpod.CPODClassification.CPODSpeciesType;
import cpod.CPODUtils;
import cpod.dataSelector.CPODDataSelector;
import cpod.dataSelector.StandardCPODFilterParams;
import export.MLExport.MLCPODExport;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * Pane for changing the CPOD data selector settings.
 * 
 * @author Jamie Macaulay
 *
 */
public class CPODDataSelectorPane extends DynamicSettingsPane<Boolean> {

	/**
	 * The CPOD data selector. 
	 */
	private CPODDataSelector cpodDataSelector;

	/**
	 * A list of filter panes. 
	 */
	private ArrayList<StandardCPODFilterPane> standardCPODFilterPanes = new ArrayList<StandardCPODFilterPane>(); 

	Pane mainPane;

	private PamToggleSwitch clcikTrainCheckBox;

	private ComboBox<String> speciesSelectBox; 

	public CPODDataSelectorPane(CPODDataSelector cpodDataSelector) {
		super(null);
		this.cpodDataSelector = cpodDataSelector;
		mainPane = createPane(); 
	}

	private Pane createPane() {

		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(10);

		ArrayList<StandardCPODFilterParams> standParams = cpodDataSelector.getParams().cpodDataFilterParams; 
		for (int i=0; i<standParams.size(); i++) {
			standardCPODFilterPanes.add(new StandardCPODFilterPane( cpodDataSelector.getParams().cpodDataFilterParams.get(i))); 

			vBox.getChildren().add(standardCPODFilterPanes.get(i)); 
		}
		
		
		PamHBox speciesBox = new PamHBox(); 
		speciesBox.setSpacing(5);
		speciesBox.setAlignment(Pos.CENTER_LEFT);
		
		this.clcikTrainCheckBox = new PamToggleSwitch("Select click trains only: Species"); 
		clcikTrainCheckBox.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			speciesSelectBox.setDisable(!newVal); 
			this.getParams(true);
			notifySettingsListeners(); 
		});
		this.speciesSelectBox = new ComboBox<String>(); 
		speciesSelectBox.setOnAction((action)->{
			this.getParams(true);
			notifySettingsListeners(); 

		});

		//CPODs and FPODs have set species identifiers.
		this.speciesSelectBox.getItems().add("All");
		this.speciesSelectBox.getItems().add("Unknown");
		this.speciesSelectBox.getItems().add("NBHF");
		this.speciesSelectBox.getItems().add("Dolphins");
		this.speciesSelectBox.getItems().add("Sonar");
		
		speciesBox.getChildren().addAll(clcikTrainCheckBox, speciesSelectBox); 
		
		vBox.getChildren().add(speciesBox); 

		return vBox; 
	}

	@Override
	public Boolean getParams(Boolean currParams) {

		for (int i=0; i<cpodDataSelector.getParams().cpodDataFilterParams.size(); i++) {
			standardCPODFilterPanes.get(i).getParams(cpodDataSelector.getParams().cpodDataFilterParams.get(i)); 
		}
		
		cpodDataSelector.getParams().selectClickTrain = clcikTrainCheckBox.isSelected();
		 
		cpodDataSelector.getParams().speciesID = getSpecies(speciesSelectBox.getSelectionModel().getSelectedIndex());
		
		return true;
	}

	@Override
	public void setParams(Boolean input) {
		for (int i=0; i<cpodDataSelector.getParams().cpodDataFilterParams.size(); i++) {
			standardCPODFilterPanes.get(i).setParams(cpodDataSelector.getParams().cpodDataFilterParams.get(i)); 
		}
		
		clcikTrainCheckBox.setSelected(cpodDataSelector.getParams().selectClickTrain);
		speciesSelectBox.getSelectionModel().select(getSpeciesIndex(cpodDataSelector.getParams().speciesID));
		
		speciesSelectBox.setDisable(!cpodDataSelector.getParams().selectClickTrain); 
		
	}
	
	protected static int getSpeciesIndex(CPODSpeciesType speciesType) {
		if (speciesType==null) return 0;
		else return MLCPODExport.getCPODSpecies(speciesType)+1;
	}

	
	protected static CPODSpeciesType getSpecies(int selectedIndex) {
		if (selectedIndex==0) return null;
		else return CPODUtils.getCPODSpecies((short) (selectedIndex-1)); 
	}

	@Override
	public String getName() {
		return "CPOD data selector";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	/**
	 * The CPOD data filter pane.
	 * @author Jamie Macaulay
	 *
	 */
	public class StandardCPODFilterPane extends BorderPane {

		/**
		 * The range slider. 
		 */
		private RangeSlider rangeSlider;

		public StandardCPODFilterPane(StandardCPODFilterParams params) {

			rangeSlider = new RangeSlider(); 
			Label topLabel = new Label(); 

			rangeSlider.setMin(0);
			rangeSlider.setMax(255);

			rangeSlider.setShowTickLabels(true);
			rangeSlider.setShowTickMarks(true);

			switch (params.dataType) {
			case StandardCPODFilterParams.AMPLITUDE:
				topLabel.setText("Amplitude (dB re 1\u03BCPa)");
				rangeSlider.setMin(80);
				rangeSlider.setMax(170);
				break;
			case StandardCPODFilterParams.PEAK_FREQ:
				topLabel.setText("Peak Frequency (kHz)");
				break;
			case StandardCPODFilterParams.BW:
				topLabel.setText("Bandwidth (kHz)");
				rangeSlider.setMin(0);
				rangeSlider.setMax(100);

				break;
			case StandardCPODFilterParams.END_F:
				topLabel.setText("End Frequency (kHz)");

				break;

			case StandardCPODFilterParams.NCYCLES:
				topLabel.setText("Number cycles");
				rangeSlider.setMin(0);
				rangeSlider.setMax(40);
				break;
			}


			this.setTop(topLabel);
			this.setCenter(rangeSlider);
			
			this.setParams(params);
			
			rangeSlider.highValueProperty().addListener((obsVal, oldVal,newVal)->{
				CPODDataSelectorPane.this.getParams(true);
				notifySettingsListeners(); 
			});
			
			
			rangeSlider.lowValueProperty().addListener((obsVal, oldVal,newVal)->{
				CPODDataSelectorPane.this.getParams(true);
				notifySettingsListeners(); 
			});

			
			rangeSlider.highValueChangingProperty().addListener((obsVal, oldVal,newVal)->{
				CPODDataSelectorPane.this.getParams(true);
				notifySettingsListeners(); 
			});

			rangeSlider.lowValueChangingProperty().addListener((obsVal, oldVal,newVal)->{
				CPODDataSelectorPane.this.getParams(true);
				notifySettingsListeners(); 
			});

		}

		public void setParams(StandardCPODFilterParams standardCPODFilterParams) {
//			System.out.println("StandardCPODFilterPane. SET PARAMS: min: " + standardCPODFilterParams.min + "  " + standardCPODFilterParams.max); 
			//set the parameters. 
			rangeSlider.setHighValue(standardCPODFilterParams.max);
			rangeSlider.setLowValue(standardCPODFilterParams.min);
		}

		public void getParams(StandardCPODFilterParams standardCPODFilterParams) {
			//standardCPODFilterParams.max = rangeSlider.getMax();
			
			//if the range sliders are maxed out then all values are used. 
			if (rangeSlider.getHighValue()==rangeSlider.getMax()) {
				standardCPODFilterParams.max = Double.POSITIVE_INFINITY; 
			}
			else {
				standardCPODFilterParams.max = rangeSlider.getHighValue();
			}

			if (rangeSlider.getLowValue()==rangeSlider.getMin()) {
				standardCPODFilterParams.min = Double.NEGATIVE_INFINITY; 
			}
			else {
				standardCPODFilterParams.min = rangeSlider.getLowValue();
			}
			
//			standardCPODFilterParams.max = rangeSlider.getHighValue();
//			standardCPODFilterParams.min = rangeSlider.getLowValue();
	
		}

	}

}
