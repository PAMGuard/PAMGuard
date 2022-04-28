package Localiser.controls;

import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.FFTDataHolderBlock;
import PamguardMVC.FFTDataHolder;
import PamguardMVC.PamDataBlock;
import PamguardMVC.RawDataHolder;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamTextField;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;
import pamViewFX.fxNodes.utilityPanes.FreqResolutionPane;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;

public class RawOrFFTPane extends SettingsPane<RawOrFFTParamsInterface> {

	private SourcePaneFX beamDataSourcePane;
	private TextField fftHop;
	private Spinner<Integer> fftSpinner;
	private FFTValue fftSpinnerValue;
	private Button defaultHopButton;
	private PamDataBlock extraFFTSource;
	private FreqResolutionPane specResolution;
	private PamBorderPane borderPane;
	
	/**
	 * always allow a choice of source, even if the 
	 * source has it's own data within.
	 */
	private boolean allowSourceChoice = false;
	
	private PamDataBlock detectionDataSource;
	
	private PamGridPane fftPane;
	
	private RawOrFFTParamsInterface currentParams;
	
	private PamDataBlock<?> onlyAllowedDataBlock;
	
	public RawOrFFTPane(Object ownerWindow) {
		super(ownerWindow);		
		
		fftPane = new PamGridPane();
//		fftPane.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
//		fftPane.setVgap(3);
		fftPane.setHgap(4);
//		PamTitledBorderPane fftSourcePane = new PamTitledBorderPane("FFT / Raw Data Source", fftPane);
//		BorderPane.setMargin(fftSourcePane, new Insets(15, 0, 0, 0));
		beamDataSourcePane = new SourcePaneFX(FFTDataUnit.class, false, false);
		beamDataSourcePane.addSourceType(RawDataUnit.class, false);
		beamDataSourcePane.setMaxWidth(Double.POSITIVE_INFINITY);
		/*
		 * 
		sourceList.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(sourceList, Priority.ALWAYS);
		 */
//		beamDataSourcePane.setMaxWidth(Double.MAX_VALUE);
//		HBox.setHgrow(beamDataSourcePane, Priority.ALWAYS);
		int iRow = 0;
		fftPane.add(beamDataSourcePane, iRow, 0, 4, 1);
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
		
		borderPane = new PamBorderPane();
		borderPane.setCenter(fftPane);
//		HBox.setHgrow(borderPane, Priority.ALWAYS);

		beamDataSourcePane.addSelectionListener(new ChangeListener<PamDataBlock>() {
			@Override
			public void changed(ObservableValue<? extends PamDataBlock> observable, PamDataBlock oldValue,
					PamDataBlock newValue) {
				enableControls();
				if (newValue != null && FFTDataBlock.class.isAssignableFrom(newValue.getClass())) {
					setFFTParams(null);				
				}
				enableTimingOptions(newValue);
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

	protected void enableTimingOptions(PamDataBlock timingSource) {

	}
	

	protected void enableFFTSourceControl(PamDataBlock detectionSource) {
		if (detectionSource == null) {
			beamDataSourcePane.setEnabled(true);
			return;
		}
		if (shouldAllowSourceChoice(detectionSource)) {
			beamDataSourcePane.setEnabled(true);
		}
		else {
			beamDataSourcePane.setEnabled(false);
			beamDataSourcePane.setSource(detectionSource);
		}
	}
	
	private boolean shouldAllowSourceChoice(PamDataBlock detectionSource) {
		if (onlyAllowedDataBlock != null) {
			return false;
		}
		if (allowSourceChoice) {
			return true;
		}
		else {
			return (hasInternalTimingData(detectionSource) == false);
		}
	}
	
	private boolean hasInternalTimingData(PamDataBlock detectionSource) {
		if (detectionSource == null) {
			return false;
		}
		Class detClass = detectionSource.getUnitClass();
		return (FFTDataHolder.class.isAssignableFrom(detClass) || RawDataHolder.class.isAssignableFrom(detClass));
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
		if (hasInternalTimingData(newValue)) {
			beamDataSourcePane.addSource(extraFFTSource = newValue);
			if (currentParams != null && extraFFTSource.getLongDataName().equals(currentParams.getSourceName())) {
				beamDataSourcePane.setSource(currentParams.getSourceName());
			}
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


	public void enableControls() {
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
			return true;
		}
		return fftDisable;
	}
	
	@Override
	public RawOrFFTParamsInterface getParams(RawOrFFTParamsInterface currParams) {
		String fftSource = beamDataSourcePane.getSourceLongName();
		if (fftSource == null) {
//			PamDialogFX.showMessageDialog("Beam form localisation", "You must set a source of either RAW or FFT data for the beam former.");
			SwingFXDialogWarning.showWarning(this, "Localisation", "You must set a source of either RAW or FFT data.");
			return null;
		}
		currParams.setSourceName(fftSource);

		if (detectionDataSource != beamDataSourcePane.getSource()) {
			if (hasInternalTimingData(detectionDataSource)) {
				String msg = String.format("\"%s\" has internal data which can be used for timing measurements. "
						+ "Are you sure you want to use data from \"%s\" in it's place ? ", detectionDataSource.getLongDataName(),
						fftSource);
				int ans = WarnOnce.showWarning(getAWTWindow(), "Timing data source", msg, WarnOnce.OK_CANCEL_OPTION);
				if (ans == WarnOnce.CANCEL_OPTION) {
					return null;
				}
			}
		}

		try {
			currParams.setFftLength(Integer.valueOf(fftSpinner.getValue()));
			currParams.setFftHop(Integer.valueOf(fftHop.getText()));
		}
		catch (Exception e) {
//			PamDialogFX.showMessageDialog("Beam form localisation", "You must set valid FFT length and hop parameters");
			SwingFXDialogWarning.showWarning(this, "FFT Parameters", "You must set valid FFT length and hop parameters");
			return null;
		}
		return currParams;
	}

	@Override
	public void setParams(RawOrFFTParamsInterface currentParams) {
		beamDataSourcePane.setSource(currentParams.getSourceName());
		setFFTParams(currentParams);
		this.currentParams = currentParams;
		setFrequencyResolution();
		enableControls();
	}

	private void setFFTParams(RawOrFFTParamsInterface currentParams) {
		PamDataBlock beamData = beamDataSourcePane.getSource();
		if (beamData != null) {
			if (FFTDataBlock.class.isAssignableFrom(beamData.getClass())) {
				FFTDataBlock fftDataBlock = (FFTDataBlock) beamData;
				fftSpinner.getValueFactory().setValue(fftDataBlock.getFftLength());
				fftHop.setText(String.format("%d", fftDataBlock.getFftHop()));
				return;
			}
			else if (FFTDataHolderBlock.class.isAssignableFrom(beamData.getClass())) {
				int[] fftParams = ((FFTDataHolderBlock) beamData).getFFTparams();
				fftSpinner.getValueFactory().setValue(fftParams[0]);
				fftHop.setText(String.format("%d", fftParams[1]));
				return;
			}
		}
		if (currentParams != null) {
			fftSpinner.getValueFactory().setValue(currentParams.getFftLength());
			fftHop.setText(String.format("%d", currentParams.getFftHop()));
		}
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getContentNode() {
		return borderPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	private class FFTValue extends SpinnerValueFactory<Integer> {

		
		@Override
		public void decrement(int shift) {
			int val = checkValue();
			this.setValue(Math.max(1, val>>shift));
		}

		@Override
		public void increment(int shift) {
			int val = checkValue();
			this.setValue(Math.max(1, val<<shift));
		}
		
		private Integer checkValue() {
			Integer val = fftSpinner.getValue();
			if (val == null) {
				return 512;
			}
			int nBits = Integer.bitCount(val);
			if (nBits == 0) {
				return 512;
			}
			else if (nBits == 1) {
				return val;
			}
			else {
				int rv = 1;
				while (rv < val) {
					rv *= 2;
				}
				return rv;
			}
		}

	}

	/**
	 * If this pane is working in conjuction with a detector data source
	 * in a localiser dialog, then modifications may be required based on
	 * what type of data source it is ...
	 * @param newValue new data Source. 
	 */
	public void setDetectionSource(PamDataBlock newValue) {
		addAdditionalFFTSource(detectionDataSource, newValue);
		enableFFTSourceControl(newValue);
		detectionDataSource = newValue;
	}

	/**
	 * @return the beamDataSourcePane
	 */
	public SourcePaneFX getBeamDataSourcePane() {
		return beamDataSourcePane;
	}

	/**	
	 * always allow a choice of source, even if the 
	 * source has it's own data within.
	 * @return the allowSourceChoice
	 */
	public boolean isAllowSourceChoice() {
		return allowSourceChoice;
	}

	/**
	 * always allow a choice of source, even if the 
	 * source has it's own data within.
	 * @param allowSourceChoice the allowSourceChoice to set
	 */
	public void setAllowSourceChoice(boolean allowSourceChoice) {
		this.allowSourceChoice = allowSourceChoice;
	}

	/**
	 * @param onlyAllowedDataBlock the onlyAllowedDataBlock to set
	 */
	public void setOnlyAllowedDataBlock(PamDataBlock<?> onlyAllowedDataBlock) {
		this.onlyAllowedDataBlock = onlyAllowedDataBlock;
		if (onlyAllowedDataBlock != null) {
			setDetectionSource(onlyAllowedDataBlock);
			beamDataSourcePane.setEnabled(false);
			beamDataSourcePane.setSource(onlyAllowedDataBlock);
		}
		else {
			beamDataSourcePane.setEnabled(true);
		}
		beamDataSourcePane.setVisible(onlyAllowedDataBlock == null);
	}

	public void setTimingSource(PamDataBlock<?> timingSource) {
//		detectionDataSource = timingSource;
	}
}
