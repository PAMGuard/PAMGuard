package bearinglocaliser.dialog;

import Localiser.controls.RawOrFFTPane;
import Localiser.controls.RawOrFFTParamsInterface;
import PamController.SettingsPane;
import PamDetection.PamDetection;
import PamDetection.RawDataUnit;
import PamguardMVC.FFTDataHolder;
import PamguardMVC.PamDataBlock;
import PamguardMVC.RawDataHolder;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
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

public class BearLocSourcePane extends SettingsPane<BearingLocaliserParams> {

	public PamBorderPane mainPane;
	private SourcePaneFX detSourcePane;
	private BearingLocSettingsPane bearLocSettingsPane;
	private RawOrFFTPane rawOrFFTPane;
	
	public BearLocSourcePane(Object window, BearingLocaliserControl bfLocControl, BearingLocSettingsPane bearLocSettingsPane) {
		super(window);
		this.bearLocSettingsPane = bearLocSettingsPane;
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
		
		rawOrFFTPane = new RawOrFFTPane(window);
		mainPane.setCenter(new PamTitledBorderPane("Detection Source Settings",rawOrFFTPane.getContentNode()));
		
		detSourcePane.addSelectionListener(new ChangeListener<PamDataBlock>() {
			@Override
			public void changed(ObservableValue<? extends PamDataBlock> observable, PamDataBlock oldValue,
					PamDataBlock newValue) {
				rawOrFFTPane.setDetectionSource(newValue);
			}
		});
		
	}


	@Override
	public BearingLocaliserParams getParams(BearingLocaliserParams params) {
		params.detectionSource = detSourcePane.getSourceLongName();
		if (params.detectionSource == null) {
			SwingFXDialogWarning.showWarning(this, "Beam form localisation", "You must set a detection source.");
			return null;
		}
		RawOrFFTParamsInterface nextparams = rawOrFFTPane.getParams(params);
		
		return params;
	}

	@Override
	public void setParams(BearingLocaliserParams params) {
		
		/*
		 * Do this one second, since it's notification may automatically
		 * set the beamDataSourcePane. 
		 */
		detSourcePane.setSource(params.detectionSource);
		
		rawOrFFTPane.setParams(params);
				
		enableControls();
		
	}
	
	
	private void enableControls() {
		rawOrFFTPane.enableControls();
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


	/**
	 * @return the beamDataSourcePane
	 */
	public SourcePaneFX getBeamDataSourcePane() {
		return rawOrFFTPane.getBeamDataSourcePane();
	}
	

}
