package beamformer.algorithms.basicFreqDomain;

import org.controlsfx.control.ToggleSwitch;

import Array.ArrayManager;
import PamController.SettingsPane;
import PamUtils.PamUtils;
import Spectrogram.WindowFunction;
import beamformer.BeamFormerParams;
import beamformer.localiser.BFLocaliserParams;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

public class BasicFreqDomBeamogramPane extends SettingsPane<BasicFreqDomParams>  {

	/**
	 * @param ownerWindow
	 */
	public BasicFreqDomBeamogramPane(Object ownerWindow) {
		super(ownerWindow);
		// TODO Auto-generated constructor stub
	}

	/**
	 * The main panel
	 */
	private PamBorderPane outerPane;

	/**
	 * Toggleswitch indicating whether or not a beamogram should be calculated
	 */
	private ToggleSwitch beamogramToggle; 
	
	/**
	 * Minimum beamogram main angle (relative to the array primary axis)
	 */
	private TextField minMainAngle;
	
	/**
	 * Maximum beamogram main angle (relative to the array primary axis)
	 */
	private TextField maxMainAngle;
	
	/**
	 * beamogram main angle step size (relative to the array primary axis)
	 */
	private TextField stepMainAngle;
	
	/**
	 * Minimum beamogram secondary angle (relative to perpendicular to the array primary axis)
	 */
	private TextField minSecAngle;
	
	/**
	 * Maximum beamogram secondary angle (relative to perpendicular to the array primary axis)
	 */
	private TextField maxSecAngle;
	
	/**
	 * beamogram secondary angle step size (relative to perpendicular to the array primary axis)
	 */
	private TextField stepSecAngle;
	
	/**
	 * Minimum beamogram frequency
	 */
	private TextField minFreq;
	
	/**
	 * Maximum beamogram frequency
	 */
	private TextField maxFreq;
	
	/**
	 * Pane containing the min/max/step values for the main angle
	 */
	private PamVBox mainAnglePane;
	
	/**
	 * Pane containing the min/max/step values for the secondary angle
	 */
	private PamVBox secAnglePane;
	
	/**
	 * The array shape
	 */
	private int arrayShape = 0;

	/**
	 * minimum allowable primary angle
	 */
	private int minPrime;
	
	/**
	 * maximum allowable primary angle
	 */
	private int maxPrime;
	
	/**
	 * minimum allowable secondary angle
	 */
	private int minSec;
	
	/**
	 * maximum allowable secondary angle
	 */
	private int maxSec;
	
	/**
	 * The window type to use for the beamogram
	 */
	private ChoiceBox<String> beamogramWindow;

	/**
	 * Main constructor
	 */
//	public BasicFreqDomBeamogramPane() {
//		this.createBeamogramPane();
//	}

	/**
	 * @return
	 */
	private void createBeamogramPane() {
		outerPane = new PamBorderPane();

		PamHBox beamogramPane = new PamHBox();
		beamogramPane.setSpacing(50);
//		beamogramPane.setPadding(new Insets(25, 25, 25, 25));
		beamogramPane.setAlignment(Pos.CENTER_LEFT);

		// beamogram on/off toggle
		PamVBox togPane = new PamVBox();
		togPane.setAlignment(Pos.CENTER_LEFT);
		beamogramToggle = new ToggleSwitch("Calculate BeamOGram"); 
		beamogramToggle.selectedProperty().set(false);
		togPane.getChildren().add(beamogramToggle);
		beamogramPane.getChildren().add(togPane);
		
		// main angle min/max/step values
		mainAnglePane = new PamVBox();
		mainAnglePane.setAlignment(Pos.CENTER);
		mainAnglePane.setSpacing(5);
		Text angRange = new Text("Primary Angle Range:");
		angRange.setTextAlignment(TextAlignment.CENTER);
		mainAnglePane.getChildren().add(angRange);
		PamHBox mainAngleRangePane = new PamHBox();
		mainAngleRangePane.setSpacing(10);
		minMainAngle = new TextField();
		minMainAngle.setPromptText("Min");
		minMainAngle.setMaxWidth(60);
		minMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		minMainAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	minMainAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		mainAngleRangePane.getChildren().add(minMainAngle);
		maxMainAngle = new TextField();
		maxMainAngle.setPromptText("Max");
		maxMainAngle.setMaxWidth(60);
		maxMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		maxMainAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	maxMainAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		mainAngleRangePane.getChildren().add(maxMainAngle);
		stepMainAngle = new TextField();
		stepMainAngle.setPromptText("Step");
		stepMainAngle.setMaxWidth(60);
		stepMainAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		stepMainAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	stepMainAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		mainAngleRangePane.getChildren().add(stepMainAngle);
		mainAnglePane.getChildren().add(mainAngleRangePane);
		beamogramPane.getChildren().add(mainAnglePane);
		
		// don't allow users to set the angles yet.  If there are multiple beamograms created, they each have to have the same size
		// of data in the PamDataUnits in order to be added to the same PamDataBlock.  The number of units is dependent on the angles
		// and step size, so it would be very easy to mess it up if the users are able to change them.  The error gets thrown in
		// dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX.fillPowerSpecLine() (line 332 - magData is the actual data unit, and
		// dataLength is the number of angles set.  There are a number of different ways this could be fixed - add the angle info to the
		// PamDataUnits, interpolate missing angles or fill with blanks, etc.  But that will have to be done later.
		
		// secondary angle min/max/step values
		secAnglePane = new PamVBox();
		secAnglePane.setAlignment(Pos.CENTER);
		secAnglePane.setSpacing(5);
		Text secAngRange = new Text("Secondary Angle Range:");
		secAngRange.setTextAlignment(TextAlignment.CENTER);
		secAnglePane.getChildren().add(secAngRange);
		PamHBox secAngleRangePane = new PamHBox();
		secAngleRangePane.setSpacing(10);
		minSecAngle = new TextField();
		minSecAngle.setPromptText("Min");
		minSecAngle.setMaxWidth(60);
		minSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		minSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	minSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		secAngleRangePane.getChildren().add(minSecAngle);
		maxSecAngle = new TextField();
		maxSecAngle.setPromptText("Max");
		maxSecAngle.setMaxWidth(60);
		maxSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		maxSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	maxSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		secAngleRangePane.getChildren().add(maxSecAngle);
		stepSecAngle = new TextField();
		stepSecAngle.setPromptText("Step");
		stepSecAngle.setMaxWidth(60);
		stepSecAngle.disableProperty().bind(beamogramToggle.selectedProperty().not());
		stepSecAngle.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("\\d*")) {
	            	stepSecAngle.setText(newValue.replaceAll("[^\\d-]", ""));
	            }
	        }
	    });
		secAngleRangePane.getChildren().add(stepSecAngle);
		secAnglePane.getChildren().add(secAngleRangePane);
		beamogramPane.getChildren().add(secAnglePane);
		
		// min/max frequency
		PamVBox freqPane = new PamVBox();
		freqPane.setAlignment(Pos.CENTER);
		freqPane.setSpacing(5);
		Text freqRange = new Text("Frequency Range:");
		freqRange.setTextAlignment(TextAlignment.CENTER);
		freqPane.getChildren().add(freqRange);
		PamHBox freqRangePane = new PamHBox();
		freqRangePane.setSpacing(10);
		minFreq = new TextField();
		minFreq.setPromptText("Min");
		minFreq.setMaxWidth(60);
		minFreq.disableProperty().bind(beamogramToggle.selectedProperty().not());
		minFreq.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("[\\d\\.]*")) {
	            	minFreq.setText(newValue.replaceAll("[^[\\d\\.]]", ""));
	            }
	        }
	    });
		freqRangePane.getChildren().add(minFreq);
		maxFreq = new TextField();
		maxFreq.setPromptText("Max");
		maxFreq.setMaxWidth(60);
		maxFreq.disableProperty().bind(beamogramToggle.selectedProperty().not());
		maxFreq.textProperty().addListener(new ChangeListener<String>() {
	        @Override
	        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            if (!newValue.matches("[\\d\\.]*")) {
	            	maxFreq.setText(newValue.replaceAll("[^[\\d\\.]]", ""));
	            }
	        }
	    });
		freqRangePane.getChildren().add(maxFreq);
		freqPane.getChildren().add(freqRangePane);
		beamogramPane.setSpacing(30);
		beamogramPane.getChildren().add(freqPane);
		
		// window function - at the moment this is not visible
		beamogramWindow = new ChoiceBox<>(FXCollections.observableArrayList(WindowFunction.getNames()));
		beamogramWindow.getSelectionModel().selectFirst();
		beamogramWindow.setMinWidth(200);
		
	}


	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getParams()
	 */
//	@Override
//	public BasicFreqDomParams getParams() {
//		BasicFreqDomParams currentParams = bfLocSettingsPane.getCurrentParms();
//		if (beamWeights != null) {
//			beamWeights[beamList.size()]=
//					WindowFunction.getWindowFunc(beamogramWindow.getSelectionModel().selectedIndexProperty().get(),
//							PamUtils.getNumChannels(curParams.getChannelMap()));
//		}
//		beamFreqs[beamList.size()][0] = Double.parseDouble(minFreq.textProperty().getValue());
//		beamFreqs[beamList.size()][1] = Double.parseDouble(maxFreq.textProperty().getValue());
//		
//		int[] beamogramAngles = new int[3];
//		beamogramAngles[0] = Integer.parseInt(minMainAngle.textProperty().getValue());
//		beamogramAngles[1] = Integer.parseInt(maxMainAngle.textProperty().getValue());
//		beamogramAngles[2] = Integer.parseInt(stepMainAngle.textProperty().getValue());
//		curParams.setBeamOGramAngles(beamogramAngles);
//		beamogramAngles = new int[3];
//		beamogramAngles[0] = Integer.parseInt(minSecAngle.textProperty().getValue());
//		beamogramAngles[1] = Integer.parseInt(maxSecAngle.textProperty().getValue());
//		beamogramAngles[2] = Integer.parseInt(stepSecAngle.textProperty().getValue());
//		curParams.setBeamOGramSlants(beamogramAngles);
//	}
	
	/**
	 * if the beamogram data is valid, return a 1
	 * @return
	 */
//	public int getNumBeamograms() {
//		if (beamogramDataGood()) {
//			return 1;
//		} else {
//			return 0;
//		}
//	}
	
	/**
	 * Test whether the beamogram data is valid
	 * 
	 * @return
	 */
//	public boolean beamogramDataGood() {
//		if (beamogramToggle.selectedProperty().getValue() &&
//				!minMainAngle.getText().isEmpty() &&
//				Double.parseDouble(minMainAngle.textProperty().getValue())>=minPrime &&
//				!maxMainAngle.getText().isEmpty() &&
//				Double.parseDouble(maxMainAngle.textProperty().getValue())<=maxPrime &&
//				!stepMainAngle.getText().isEmpty() &&
//				Double.parseDouble(stepMainAngle.textProperty().getValue())>0 &&
//				Double.parseDouble(minMainAngle.textProperty().getValue())<=Double.parseDouble(maxMainAngle.textProperty().getValue()) &&
//				!minSecAngle.getText().isEmpty() &&
//				Double.parseDouble(minSecAngle.textProperty().getValue())>=minSec &&
//				!maxSecAngle.getText().isEmpty() &&
//				Double.parseDouble(maxSecAngle.textProperty().getValue())<=maxSec &&
//				!stepSecAngle.getText().isEmpty() &&
//				Double.parseDouble(stepSecAngle.textProperty().getValue())>0 &&
//				Double.parseDouble(minSecAngle.textProperty().getValue())<=Double.parseDouble(maxSecAngle.textProperty().getValue()) &&
//				!minFreq.getText().isEmpty() &&
//				Double.parseDouble(minFreq.textProperty().getValue())>=0 &&
//				!maxFreq.getText().isEmpty() &&
//				Double.parseDouble(minFreq.textProperty().getValue())<=Double.parseDouble(maxFreq.textProperty().getValue())
//				) {
//			return true;
//		}
//		return false;
//	}

	/**
	 * Called whenever the pane is first shown/open to set pane to show current settings.  Note that
	 * the setArrayShape(arrayShape) method should be called before this is called, in order to ensure
	 * that the allowable angle ranges are set appropriately.
	 */
//	@Override
//	public void setParams(BasicFreqDomParams newParams) {
//		beamogramToggle.selectedProperty().set(true);
//		double[] beamogramFreqs=newParams.getBeamOGramFreqRange();
//		minFreq.textProperty().set(String.valueOf(beamogramFreqs[0]));
//		maxFreq.textProperty().set(String.valueOf(beamogramFreqs[1]));
//		int[] beamogramAngles = newParams.getBeamOGramAngles();
//		if (beamogramAngles != null && beamogramAngles.length == 3) {
//			minMainAngle.textProperty().set(String.valueOf(beamogramAngles[0]));
//			maxMainAngle.textProperty().set(String.valueOf(beamogramAngles[1]));
//			stepMainAngle.textProperty().set(String.valueOf(beamogramAngles[2]));
//		}
//		beamogramAngles = newParams.getBeamOGramSlants();
//		if (beamogramAngles != null && beamogramAngles.length == 3) {
//			minSecAngle.textProperty().set(String.valueOf(beamogramAngles[0]));
//			maxSecAngle.textProperty().set(String.valueOf(beamogramAngles[1]));
//			stepSecAngle.textProperty().set(String.valueOf(beamogramAngles[2]));
//		}
//		setAngleRange();
//	}
	
	/**
	 * Sets the valid angle range, based on the array shape
	 */
	public void setAngleRange() {
		switch (arrayShape) {
		case ArrayManager.ARRAY_TYPE_LINE:
			mainAnglePane.setDisable(false);
			minPrime = 0;
			maxPrime = 180;
			secAnglePane.setDisable(true);
			break;
			
		case ArrayManager.ARRAY_TYPE_PLANE:
			mainAnglePane.setDisable(false);
			minPrime = -180;
			maxPrime = 180;
			secAnglePane.setDisable(false);
			minSec = -90;
			maxSec = 0;
			break;
			
		case ArrayManager.ARRAY_TYPE_VOLUME:
			minPrime = -180;
			maxPrime = 180;
			secAnglePane.setDisable(false);
			minSec = -180;
			maxSec = 180;
			break;
			
		// if it's not a line, plane or volume array, disable everything and warn the user
		default:
			mainAnglePane.setDisable(true);
			secAnglePane.setDisable(true);
		}				

	}
	
	/**
	 * Clears the parameters in the window
	 */
	public void clearParams() {
		beamogramToggle.selectedProperty().set(false);
		minMainAngle.clear();
		maxMainAngle.clear();
		stepMainAngle.clear();
		minSecAngle.clear();
		maxSecAngle.clear();
		stepSecAngle.clear();
		minFreq.clear();
		maxFreq.clear();
		setAngleRange();
	}
	
	
	/**
	 * @return the minPrime
	 */
	public int getMinPrime() {
		return minPrime;
	}

	/**
	 * @param minPrime the minPrime to set
	 */
	public void setMinPrime(int minPrime) {
		this.minPrime = minPrime;
	}

	/**
	 * @return the maxPrime
	 */
	public int getMaxPrime() {
		return maxPrime;
	}

	/**
	 * @param maxPrime the maxPrime to set
	 */
	public void setMaxPrime(int maxPrime) {
		this.maxPrime = maxPrime;
	}

	/**
	 * @return the minSec
	 */
	public int getMinSec() {
		return minSec;
	}

	/**
	 * @param minSec the minSec to set
	 */
	public void setMinSec(int minSec) {
		this.minSec = minSec;
	}

	/**
	 * @return the maxSec
	 */
	public int getMaxSec() {
		return maxSec;
	}

	/**
	 * @param maxSec the maxSec to set
	 */
	public void setMaxSec(int maxSec) {
		this.maxSec = maxSec;
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getName()
	 */
//	@Override
//	public String getName() {
//		return "Beamogram Parameters";
//	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getContentNode()
	 */
//	@Override
//	public Node getContentNode() {
//		return outerPane;
//	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#paneInitialized()
	 */
//	@Override
//	public void paneInitialized() {
//	}

	/**
	 * @return the arrayShape
	 */
	public int getArrayShape() {
		return arrayShape;
	}

	/**
	 * @param arrayShape the arrayShape to set
	 */
	public void setArrayShape(int arrayShape) {
		this.arrayShape = arrayShape;
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getParams(java.lang.Object)
	 */
	@Override
	public BasicFreqDomParams getParams(BasicFreqDomParams currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#setParams(java.lang.Object)
	 */
	@Override
	public void setParams(BasicFreqDomParams input) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getContentNode()
	 */
	@Override
	public Node getContentNode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#paneInitialized()
	 */
	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
