/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package bearinglocaliser.toad;

import Array.ArrayManager;
import Array.PamArray;
import PamController.SettingsPane;
import PamguardMVC.AcousticDataBlock;
import beamformer.algorithms.BeamAlgoParamsPane.BeamgramStatus;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;

/**
 * @author mo55
 *
 */
public class TOADSettingsPane extends SettingsPane<TOADBearingParams> {

	/**
	 * The parameters to display in the dialog
	 */
	protected TOADBearingParams curParams;
	
	/**
	 * The main pane holding everything
	 */
	private PamBorderPane mainPane;
	
	/**
	 * The FFT source currently selected in the main GUI
	 */
	private AcousticDataBlock<?> acousticDataSource;
	
	/**
	 * minimum allowable primary angle
	 */
	protected int minPrime;
	
	/**
	 * maximum allowable primary angle
	 */
	protected int maxPrime;
	
	/**
	 * minimum allowable secondary angle
	 */
	protected int minSec;
	
	/**
	 * maximum allowable secondary angle
	 */
	protected int maxSec;
		/**
	 * Minimum beamogram main angle (relative to the array primary axis)
	 */
	private TextField minMainAngle;
	
	/**
	 * Label associated with minMainAngle TextField
	 */
	private Label minMainAngleLabel;
	
	/**
	 * Maximum beamogram main angle (relative to the array primary axis)
	 */
	private TextField maxMainAngle;
	
	/**
	 * Label associated with maxMainAngle TextField
	 */
	private Label maxMainAngleLabel;
	
	/**
	 * beamogram main angle step size (relative to the array primary axis)
	 */
	private TextField stepMainAngle;
	
	/**
	 * Label associated with stepMainAngle TextField
	 */
	private Label stepMainAngleLabel;
	
	/**
	 * Minimum beamogram secondary angle (relative to perpendicular to the array primary axis)
	 */
	private TextField minSecAngle;
	
	/**
	 * Label associated with minSecAngle TextField
	 */
	private Label minSecAngleLabel;
	
	/**
	 * Maximum beamogram secondary angle (relative to perpendicular to the array primary axis)
	 */
	private TextField maxSecAngle;
	
	/**
	 * Label associated with maxSecAngle TextField
	 */
	private Label maxSecAngleLabel;
	
	/**
	 * beamogram secondary angle step size (relative to perpendicular to the array primary axis)
	 */
	private TextField stepSecAngle;
	
	/**
	 * Label associated with stepSecAngle TextField
	 */
	private Label stepSecAngleLabel;
	
	/**
	 * The array shape
	 */
	private int arrayShape;
	
	/**
	 * Text giving the current array type on beamogram tab
	 */
	private Text arrayTypeLabel;
	
	/**
	 * Info for the user regarding min/max main angle values, based on current array type, on beamogram tab
	 */
	private Text mainAngleInfo;
	
	/**
	 * Info for the user regarding min/max secondary angle values, based on current array type, on beamogram tab
	 */
	private Text secAngleInfo;
	
	/**
	 * Pane containing the min/max/step values for the main angle
	 */
	private GridPane mainAnglePane;
	
	/**
	 * Pane containing the min/max/step values for the secondary angle
	 */
	private GridPane secAnglePane;

	/**
	 * @param ownerWindow
	 */
	public TOADSettingsPane(Object ownerWindow) {
		super(ownerWindow);
		
		mainPane = new PamBorderPane();
		PamBorderPane beamogramPanel = new PamBorderPane(this.createBeamogramPane());
		VBox.setVgrow(beamogramPanel, Priority.NEVER);
		
		// add everything to the dialog window
		mainPane.setCenter(beamogramPanel);	
	}

	/**
	 * @return
	 */
	protected Node createBeamogramPane() {
		// overall Pane
		PamVBox beamogramPane = new PamVBox();
		beamogramPane.setSpacing(20);
		beamogramPane.setPadding(new Insets(10));
		beamogramPane.setAlignment(Pos.TOP_LEFT);
		beamogramPane.setFillWidth(false);

		// Pane containing items in the left column (so that items don't stretch over entire beamogramPane)
		PamVBox leftColPane = new PamVBox();
		leftColPane.setAlignment(Pos.TOP_LEFT);
		leftColPane.setFillWidth(true);

		// Array information label
		PamHBox arrayBox = new PamHBox();
		arrayBox.setAlignment(Pos.CENTER_LEFT);
		arrayBox.setSpacing(10);
		Text arrayTypeHeader = new Text("Current Array type:");
		arrayTypeLabel = new Text(ArrayManager.getArrayTypeString(arrayShape));
		arrayTypeLabel.setFont(Font.font(arrayTypeLabel.getFont().getFamily(), FontWeight.BOLD, arrayTypeLabel.getFont().getSize()));
		arrayBox.getChildren().addAll(arrayTypeHeader,arrayTypeLabel);
		leftColPane.getChildren().add(arrayBox);
		leftColPane.setSpacing(20);
		
		// ****************************************************************************************************
		// DANGER with setting angles: If there are multiple beamograms created, they each have to have the same size
		// of data in the PamDataUnits in order to be added to the same PamDataBlock.  The number of units is dependent on the angles
		// and step size, so it would be very easy to mess it up if the users are able to change them.  The error gets thrown in
		// dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX.fillPowerSpecLine() (line 332 - magData is the actual data unit, and
		// dataLength is the number of angles set.  There are a number of different ways this could be fixed - add the angle info to the
		// PamDataUnits, interpolate missing angles or fill with blanks, etc.
		// ****************************************************************************************************
		
		// main angle min/max/step values
		mainAnglePane = new GridPane();
		mainAnglePane.getColumnConstraints().add(new ColumnConstraints(80));
		mainAnglePane.setAlignment(Pos.CENTER_LEFT);
		mainAnglePane.setHgap(10);
		mainAnglePane.setVgap(5);
		minMainAngleLabel = new Label("Minimum");
		minMainAngleLabel.setLabelFor(minMainAngle);
		mainAnglePane.add(minMainAngleLabel, 0, 0);
		minMainAngle = new TextField();
		minMainAngle.setPromptText("Min");
		minMainAngle.setMaxWidth(60);
//		minMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		minMainAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d-")) {
	            	minMainAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		mainAnglePane.add(minMainAngle, 1, 0);
		maxMainAngleLabel = new Label("Maximum");
		maxMainAngleLabel.setLabelFor(maxMainAngle);
		mainAnglePane.add(maxMainAngleLabel, 0, 1);
		maxMainAngle = new TextField();
		maxMainAngle.setPromptText("Max");
		maxMainAngle.setMaxWidth(60);
//		maxMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		maxMainAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d-")) {
	            	maxMainAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		mainAnglePane.add(maxMainAngle, 1, 1);
		stepMainAngleLabel = new Label("Step Size");
		stepMainAngleLabel.setLabelFor(stepMainAngle);
		mainAnglePane.add(stepMainAngleLabel, 0, 2);
		stepMainAngle = new TextField();
		stepMainAngle.setPromptText("Step");
		stepMainAngle.setMaxWidth(60);
//		stepMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		stepMainAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d")) {
	            	stepMainAngle.setText(newValue.replaceAll("[^\\d]", ""));
	            }
	        }
	    });
		mainAnglePane.add(stepMainAngle, 1, 2);
		mainAngleInfo = new Text("Temp");
		mainAngleInfo.setWrappingWidth(150);
		mainAnglePane.add(mainAngleInfo, 2, 0, 1, 3);
		PamTitledBorderPane mainAngleBorder = new PamTitledBorderPane("Primary Angle Range", mainAnglePane);
		leftColPane.getChildren().add(mainAngleBorder);
		
		// secondary angle min/max/step values
		secAnglePane = new GridPane();
		secAnglePane.getColumnConstraints().add(new ColumnConstraints(80));
		secAnglePane.setAlignment(Pos.CENTER_LEFT);
		secAnglePane.setHgap(10);
		secAnglePane.setVgap(5);
		minSecAngleLabel = new Label("Minimum");
		minSecAngleLabel.setLabelFor(minSecAngle);
		secAnglePane.add(minSecAngleLabel, 0, 0);
		minSecAngle = new TextField();
		minSecAngle.setPromptText("Min");
		minSecAngle.setMaxWidth(60);
//		minSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		minSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	minSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		secAnglePane.add(minSecAngle, 1, 0);
		maxSecAngleLabel = new Label("Maximum");
		maxSecAngleLabel.setLabelFor(maxSecAngle);
		secAnglePane.add(maxSecAngleLabel, 0, 1);
		maxSecAngle = new TextField();
		maxSecAngle.setPromptText("Max");
		maxSecAngle.setMaxWidth(60);
//		maxSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		maxSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	maxSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		secAnglePane.add(maxSecAngle, 1, 1);
		stepSecAngleLabel = new Label("Step Size");
		stepSecAngleLabel.setLabelFor(stepSecAngle);
		secAnglePane.add(stepSecAngleLabel, 0, 2);
		stepSecAngle = new TextField();
		stepSecAngle.setPromptText("Step");
		stepSecAngle.setMaxWidth(60);
//		stepSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		stepSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	stepSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		secAnglePane.add(stepSecAngle, 1, 2);
		secAngleInfo = new Text("Temp");
		secAngleInfo.setWrappingWidth(150);
		secAnglePane.add(secAngleInfo, 2, 0, 1, 3);
		PamTitledBorderPane secAngleBorder = new PamTitledBorderPane("Secondary Angle Range", secAnglePane);
		leftColPane.getChildren().add(secAngleBorder);
		beamogramPane.getChildren().add(leftColPane);
		
		// return pane
		return beamogramPane;
	}

	/**
	 * Sets the valid angle range, based on the array shape
	 */
	public void setAngleRange() {
		switch (arrayShape) {
		case ArrayManager.ARRAY_TYPE_LINE:
			minMainAngle.setDisable(false);
			minMainAngleLabel.setDisable(false);
			maxMainAngle.setDisable(false);
			maxMainAngleLabel.setDisable(false);
			stepMainAngle.setDisable(false);
			stepMainAngleLabel.setDisable(false);
			minPrime = 0;
			maxPrime = 180;
			minSecAngle.setDisable(true);
			minSecAngleLabel.setDisable(true);
			maxSecAngle.setDisable(true);
			maxSecAngleLabel.setDisable(true);
			stepSecAngle.setDisable(true);
			stepSecAngleLabel.setDisable(true);
			break;
			
		case ArrayManager.ARRAY_TYPE_PLANE:
			minMainAngle.setDisable(false);
			minMainAngleLabel.setDisable(false);
			maxMainAngle.setDisable(false);
			maxMainAngleLabel.setDisable(false);
			stepMainAngle.setDisable(false);
			stepMainAngleLabel.setDisable(false);
			minPrime = -180;
			maxPrime = 180;
			minSecAngle.setDisable(false);
			minSecAngleLabel.setDisable(false);
			maxSecAngle.setDisable(false);
			maxSecAngleLabel.setDisable(false);
			stepSecAngle.setDisable(false);
			stepSecAngleLabel.setDisable(false);
			minSec = -90;		// for a linear array, the secondary angle range is either -90 to 0 or 0 to 90.
			maxSec = 90;		// use the extreme limits here, and do more extensive error checking in checkBeamogramStatus
			break;
			
		case ArrayManager.ARRAY_TYPE_VOLUME:
			minMainAngle.setDisable(false);
			minMainAngleLabel.setDisable(false);
			maxMainAngle.setDisable(false);
			maxMainAngleLabel.setDisable(false);
			stepMainAngle.setDisable(false);
			stepMainAngleLabel.setDisable(false);
			minPrime = -180;
			maxPrime = 180;
			minSecAngle.setDisable(false);
			minSecAngleLabel.setDisable(false);
			maxSecAngle.setDisable(false);
			maxSecAngleLabel.setDisable(false);
			stepSecAngle.setDisable(false);
			stepSecAngleLabel.setDisable(false);
			minSec = -90;
			maxSec = 90;
			break;
			
		// if it's not a line, plane or volume array, disable everything and warn the user
		default:
			minMainAngle.setDisable(true);
			minMainAngleLabel.setDisable(true);
			maxMainAngle.setDisable(true);
			maxMainAngleLabel.setDisable(true);
			stepMainAngle.setDisable(true);
			stepMainAngleLabel.setDisable(true);
			minSecAngle.setDisable(true);
			minSecAngleLabel.setDisable(true);
			maxSecAngle.setDisable(true);
			maxSecAngleLabel.setDisable(true);
			stepSecAngle.setDisable(true);
			stepSecAngleLabel.setDisable(true);
		}	
		
		// change the labels to reflect the current min/max
		setArrayTypeLables();
	}
	
	public void setArrayTypeLables() {
		arrayTypeLabel.setText(ArrayManager.getArrayTypeString(arrayShape));
		mainAngleInfo.setOpacity(1);
		secAngleInfo.setOpacity(1);
		mainAngleInfo.setText("Angles must be between "
				+ minPrime + "\u00B0 and "
				+ maxPrime + "\u00B0");
		if (arrayShape == ArrayManager.ARRAY_TYPE_PLANE) {
			secAngleInfo.setText("Angles must be between -90\u00B0 and 0\u00B0, or between 0\u00B0 and 90\u00B0");
		} else if (arrayShape == ArrayManager.ARRAY_TYPE_VOLUME) {
			secAngleInfo.setText("Angles must be between "
					+ minSec + "\u00B0 and "
					+ maxSec + "\u00B0");
		} else {
			secAngleInfo.setText("No secondary angle available for this type of array");
		}
	}
	
	
		/* (non-Javadoc)
	 * @see PamController.SettingsPane#getParams(java.lang.Object)
	 */
	@Override
	public TOADBearingParams getParams(TOADBearingParams currParams) {
		if (checkBeamogramStatus()==BeamgramStatus.BEAMOGRAMBAD) {
			return null;
		}

		int[] beamogramAngles = new int[3];
		beamogramAngles[0] = Integer.parseInt(minMainAngle.textProperty().getValue());
		beamogramAngles[1] = Integer.parseInt(maxMainAngle.textProperty().getValue());
		beamogramAngles[2] = Integer.parseInt(stepMainAngle.textProperty().getValue());
		curParams.setBearingHeadings(beamogramAngles);

		int[] beamogramSlants = {0,0,1};
		if (!minSecAngle.isDisabled()) {
			beamogramSlants[0] = Integer.parseInt(minSecAngle.textProperty().getValue());
			beamogramSlants[1] = Integer.parseInt(maxSecAngle.textProperty().getValue());
			beamogramSlants[2] = Integer.parseInt(stepSecAngle.textProperty().getValue());
		}
		curParams.setBearingSlants(beamogramSlants);

		// return the parameters
		return curParams;	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#setParams(java.lang.Object)
	 */
	@Override
	public void setParams(TOADBearingParams newParams) {
		curParams = newParams;
		
		// get the array shape, set the angle limits and fill in the data
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		PamArray currentArray = arrayManager.getCurrentArray();
		int phones = acousticDataSource.getChannelListManager().channelIndexesToPhones(curParams.getChannelMap());
		arrayShape = arrayManager.getArrayShape(currentArray, phones);
		this.setAngleRange();

		// set the beamogram information, or clear it if there is no beamogram
		setBeamogramData();
		
	}

	/**
	 * Fills in the data on the beamogram tab
	 */
	public void setBeamogramData() {
		int[] beamogramAngles = curParams.getBearingHeadings();
		if (beamogramAngles != null && beamogramAngles.length == 3) {
			minMainAngle.textProperty().set(String.valueOf(beamogramAngles[0]));
			maxMainAngle.textProperty().set(String.valueOf(beamogramAngles[1]));
			stepMainAngle.textProperty().set(String.valueOf(beamogramAngles[2]));
		}
		beamogramAngles = curParams.getBearingSlants();
		if (beamogramAngles != null && beamogramAngles.length == 3) {
			minSecAngle.textProperty().set(String.valueOf(beamogramAngles[0]));
			maxSecAngle.textProperty().set(String.valueOf(beamogramAngles[1]));
			stepSecAngle.textProperty().set(String.valueOf(beamogramAngles[2]));
		}
	}

	/**
	 * Test whether the beamogram data is valid.  This should only be called if the beamogram check box
	 * is checked
	 * 
	 * @return
	 */
	public BeamgramStatus checkBeamogramStatus() {
		if (minMainAngle.getText().isEmpty() ||
				Double.parseDouble(minMainAngle.textProperty().getValue())<minPrime ||
				maxMainAngle.getText().isEmpty() ||
				Double.parseDouble(maxMainAngle.textProperty().getValue())>maxPrime ||
				stepMainAngle.getText().isEmpty() ||
				Double.parseDouble(stepMainAngle.textProperty().getValue())<=0 ||
				Double.parseDouble(minMainAngle.textProperty().getValue())>Double.parseDouble(maxMainAngle.textProperty().getValue())
				) {
			SwingFXDialogWarning.showWarning(this, "TDOA Localiser Parameters", "Warning - primary angle parameters not correctly configured");
			return BeamgramStatus.BEAMOGRAMBAD;
		}
		if (!minSecAngle.isDisabled()) {
			if (minSecAngle.getText().isEmpty() ||
				maxSecAngle.getText().isEmpty() ||
				stepSecAngle.getText().isEmpty() ||
				Double.parseDouble(stepSecAngle.textProperty().getValue())<=0) {
				SwingFXDialogWarning.showWarning(this, "TDOA Localiser Parameters", "Warning - secondary angle parameters not correctly configured");
				return BeamgramStatus.BEAMOGRAMBAD;
			}
			// special case: Planar array angles can go from -90 to 0, OR 0 to 90.  Need to check both possibilities
			if (arrayShape==ArrayManager.ARRAY_TYPE_PLANE) {
				if ( (Double.parseDouble(minSecAngle.textProperty().getValue())<0 && Double.parseDouble(maxSecAngle.textProperty().getValue())>0) 
						||
					 (Double.parseDouble(minSecAngle.textProperty().getValue())>=0 && Double.parseDouble(maxSecAngle.textProperty().getValue())>90) 
					    ||
					    Double.parseDouble(minSecAngle.textProperty().getValue())<-90 ||
					    Double.parseDouble(minSecAngle.textProperty().getValue())>90 ||
  					  Double.parseDouble(minSecAngle.textProperty().getValue())>Double.parseDouble(maxSecAngle.textProperty().getValue())) {
					SwingFXDialogWarning.showWarning(this, "TDOA Localiser Parameters", "Warning - secondary angle parameters not correctly configured");
					return BeamgramStatus.BEAMOGRAMBAD;
				}
			} else {
				if (Double.parseDouble(minSecAngle.textProperty().getValue())<minSec ||
						Double.parseDouble(maxSecAngle.textProperty().getValue())>maxSec ||
						Double.parseDouble(minSecAngle.textProperty().getValue())>Double.parseDouble(maxSecAngle.textProperty().getValue())) {
					SwingFXDialogWarning.showWarning(this, "TDOA Localiser Parameters", "Warning - secondary angle parameters not correctly configured");
					return BeamgramStatus.BEAMOGRAMBAD;
				}
			}
		}
		return BeamgramStatus.BEAMOGRAMGOOD;
	}

	/**
	 * @param fftSource the fftSource to set
	 */
	public void setDataSource(AcousticDataBlock<?> fftSource) {
		this.acousticDataSource = fftSource;
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getName()
	 */
	@Override
	public String getName() {
		return ("Group " + String.valueOf(curParams.getGroupNumber()) + " Parameters");
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getContentNode()
	 */
	@Override
	public Node getContentNode() {
		return mainPane;
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#paneInitialized()
	 */
	@Override
	public void paneInitialized() {

	}

}
