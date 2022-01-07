package beamformer.localiser.dialog;

import PamController.SettingsPane;
import PamDetection.PamDetection;
import PamDetection.RawDataUnit;
import PamguardMVC.FFTDataHolder;
import PamguardMVC.PamDataBlock;
import PamguardMVC.RawDataHolder;
import beamformer.localiser.BFLocaliserParams;
import beamformer.localiser.BeamFormLocaliserControl;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamTextField;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;
import pamViewFX.fxNodes.utilityPanes.FreqResolutionPane;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;

public class BFLocSourcePane extends SettingsPane<BFLocaliserParams> {

	public PamBorderPane mainPane;
	private SourcePaneFX detSourcePane;
	private SourcePaneFX beamDataSourcePane;
	private TextField fftHop;
	private Spinner<Integer> fftSpinner;
	private FFTValue fftSpinnerValue;
	private Button defaultHopButton;
	private PamDataBlock extraFFTSource;
	private FreqResolutionPane specResolution;
	private BFLocSettingsPane2 bfLocSettingsPane;
	
	public BFLocSourcePane(Object window, BeamFormLocaliserControl bfLocControl, BFLocSettingsPane2 bfLocSettingsPane) {
		super(window);
		this.bfLocSettingsPane = bfLocSettingsPane;
		mainPane = new PamBorderPane();
		mainPane.setPadding(new Insets(10, 5, 10, 5));
		GridPane sourcePane = new GridPane();
//		sourcePane.setVgap(3);
//		sourcePane.setHgap(4);
		detSourcePane = new SourcePaneFX(PamDetection.class, false, true);
//		sourcePane.add(detSourcePane, 0, 2);
		sourcePane.add(detSourcePane, 0, 0);
		mainPane.setTop(new PamTitledBorderPane("Detection Source", sourcePane));
		ToggleGroup group = new ToggleGroup();
		
		GridPane fftPane = new PamGridPane();
//		fftPane.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
//		fftPane.setVgap(3);
		fftPane.setHgap(4);
		PamTitledBorderPane fftSourcePane = new PamTitledBorderPane("FFT / Raw Data Source", fftPane);
		BorderPane.setMargin(fftSourcePane, new Insets(15, 0, 0, 0));
		mainPane.setCenter(fftSourcePane);
		beamDataSourcePane = new SourcePaneFX(FFTDataUnit.class, false, false);
		beamDataSourcePane.addSourceType(RawDataUnit.class, false);
		int iRow = 0;
		fftPane.add(beamDataSourcePane, iRow, 0, 3, 1);
		fftHop = new PamTextField(6);
		fftSpinner = new Spinner<Integer>(4, 65536, 512, 1);
		fftSpinner.getEditor().setPrefColumnCount(7);
		fftSpinner.setValueFactory(fftSpinnerValue = new FFTValue());
		fftSpinnerValue.setValue(1024);
		iRow++;
		fftPane.add(new Label("FFT Length "), 0, iRow);
		fftPane.add(fftSpinner, 1, iRow, 2, 1);
		iRow++;
		fftPane.add(new Label("FFT Hop "), 0, iRow);
		fftPane.add(fftHop, 1, iRow);
		fftPane.add(defaultHopButton = new Button("Default"), 2, iRow);
		iRow++;
		specResolution = new FreqResolutionPane();
		fftPane.add(specResolution, 0, iRow, 3, 1);
		
		detSourcePane.addSelectionListener(new ChangeListener<PamDataBlock>() {
			@Override
			public void changed(ObservableValue<? extends PamDataBlock> observable, PamDataBlock oldValue,
					PamDataBlock newValue) {
				addAdditionalFFTSource(oldValue, newValue);
				enableFFTSourceControl(newValue);
			}
		});
		
		beamDataSourcePane.addSelectionListener(new ChangeListener<PamDataBlock>() {
			@Override
			public void changed(ObservableValue<? extends PamDataBlock> observable, PamDataBlock oldValue,
					PamDataBlock newValue) {
				enableControls();
				if (newValue != null && FFTDataBlock.class.isAssignableFrom(newValue.getClass())) {
					setFFTParams();				
				}
			}
		});
		defaultHopButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setDefaultHop();
			}
		});
		fftHop.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				setFrequencyResolution();
			}
		});
		fftSpinner.getEditor().textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				setFrequencyResolution();
			}
		});
		
	}

	protected void enableFFTSourceControl(PamDataBlock newValue) {
		if (newValue == null) {
			beamDataSourcePane.setEnabled(true);
			return;
		}
		if (FFTDataHolder.class.isAssignableFrom(newValue.getUnitClass())) {
			beamDataSourcePane.setEnabled(false);
			beamDataSourcePane.setSource(newValue);
		}
		else {
			beamDataSourcePane.setEnabled(true);
		}
	}

	/**
	 * If the new Det source has RAW or FFT data it can be added as a source
	 * for the beam data. 
	 * @param oldValue
	 * @param newValue
	 */
	protected void addAdditionalFFTSource(PamDataBlock oldValue, PamDataBlock newValue) {
		if (oldValue != null) {
			beamDataSourcePane.removeSource(oldValue);
		}
		if (extraFFTSource != null) {
			PamDataBlock<?> currentSource = beamDataSourcePane.getSource();
			beamDataSourcePane.removeSource(extraFFTSource);
			if (currentSource == extraFFTSource) {
				beamDataSourcePane.setSourceIndex(0);
			}
			extraFFTSource = null;
		}
		if (newValue != null && (FFTDataHolder.class.isAssignableFrom(newValue.getUnitClass())  || RawDataHolder.class.isAssignableFrom(newValue.getUnitClass()) )) {
			beamDataSourcePane.addSource(extraFFTSource = newValue);
		}		
	}

	protected void setFrequencyResolution() {
		Integer fftLengthVal = fftSpinner.getValue();
		float sampleRate = 0;
		int fftHopVal = -1;
		try {
			fftHopVal = Integer.valueOf(fftHop.getText());
		}
		catch (NumberFormatException e) {
		}
		PamDataBlock sourceData = beamDataSourcePane.getSource();	
		if (sourceData != null) {
			sampleRate = sourceData.getSampleRate();
		}
		if (sampleRate == 0 || fftLengthVal == null) {
//			freqRes.setText("");
		}
		else {
//			freqRes.setText(String.format("%3.1f", sampleRate / (double) fftLengthVal));
			specResolution.setParams(sampleRate, fftLengthVal, fftHopVal);
		}
		
		
	}

	protected void setDefaultHop() {
		Integer fftLen = fftSpinner.getValue();
		if (fftLen == null) {
			return;
		}
		fftHop.setText(String.format("%d", fftLen/2));	
	}

	@Override
	public BFLocaliserParams getParams(BFLocaliserParams params) {
		params.detectionSource = detSourcePane.getSourceLongName();
		if (params.detectionSource == null) {
			SwingFXDialogWarning.showWarning(this, "Beamformer localisation", "You must set a detection source.");
			return null;
		}
		String fftSource = beamDataSourcePane.getSourceLongName();
		if (fftSource == null) {
//			PamDialogFX.showMessageDialog("Beam form localisation", "You must set a source of either RAW or FFT data for the beam former.");
			SwingFXDialogWarning.showWarning(this, "Beamformer localisation", "You must set a source of either RAW or FFT data for the beamformer.");
			return null;
		}
		params.getGroupedSourceParameters().setDataSource(fftSource);
		try {
			params.fftLength = Integer.valueOf(fftSpinner.getValue());
			params.fftHop = Integer.valueOf(fftHop.getText());
		}
		catch (Exception e) {
//			PamDialogFX.showMessageDialog("Beam form localisation", "You must set valid FFT length and hop parameters");
			SwingFXDialogWarning.showWarning(this, "Beamformer localisation", "You must set valid FFT length and hop parameters");
			return null;
		}
		
		return params;
	}

	@Override
	public void setParams(BFLocaliserParams params) {
		
		beamDataSourcePane.setSource(params.getDataSource());
		/*
		 * Do this one second, since it's notification may automatically
		 * set the beamDataSourcePane. 
		 */
		detSourcePane.setSource(params.detectionSource);
		
		setFFTParams();
		
		enableControls();
		
		setFrequencyResolution();
	}
	
	private void setFFTParams() {
		BFLocaliserParams currentParams = bfLocSettingsPane.getCurrentParms();
		boolean isFFTSource = false;
		PamDataBlock beamData = beamDataSourcePane.getSource();
		if (beamData != null) {
			isFFTSource = (FFTDataBlock.class.isAssignableFrom(beamData.getClass()));
		}
		if (isFFTSource) {
			FFTDataBlock fftDataBlock = (FFTDataBlock) beamData;
			fftSpinner.getValueFactory().setValue(fftDataBlock.getFftLength());
			fftHop.setText(String.format("%d", fftDataBlock.getFftHop()));
		}
		else {
			fftSpinner.getValueFactory().setValue(currentParams.fftLength);
			fftHop.setText(String.format("%d", currentParams.fftHop));
		}
	}
	
	private void enableControls() {
		boolean fftDisable = disableFFTControl();
		fftSpinner.setEditable(false);
		fftSpinner.setDisable(fftDisable);
		fftHop.setDisable(fftDisable);		
		defaultHopButton.setDisable(fftDisable);
	}
	
	private boolean disableFFTControl() {
		boolean fftDisable = false;
		PamDataBlock beamDataSource = beamDataSourcePane.getSource();
		if (beamDataSource == null) {
			return true;
		}
		if (FFTDataUnit.class.isAssignableFrom(beamDataSource.getUnitClass())) {
			return true;
		}
		if (RawDataHolder.class.isAssignableFrom(beamDataSource.getUnitClass())) {
			return false;
		}
		if (FFTDataHolder.class.isAssignableFrom(beamDataSource.getUnitClass())) {
			return false;
		}
		return fftDisable;
	}

	@Override
	public String getName() {
		return "Source";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	private class FFTValue extends SpinnerValueFactory<Integer> {

		
		@Override
		public void decrement(int arg0) {
			Integer val = fftSpinner.getValue();
//			val = fftSpinner.valueProperty().getValue();
			if (val == null) val = 1024;
			this.setValue(Math.max(1, val>>arg0));
		}

		@Override
		public void increment(int arg0) {
			Integer val = fftSpinner.getValue();
//			val = fftSpinner.valueProperty().getValue();
			if (val == null) val = 1024;
			this.setValue(Math.max(1, val<<arg0));
		}

	}

	/**
	 * @return the beamDataSourcePane
	 */
	public SourcePaneFX getBeamDataSourcePane() {
		return beamDataSourcePane;
	}
	
}
