package PamguardMVC.toad;

import Localiser.DelayMeasurementParams;
import Localiser.controls.RawOrFFTPane;
import Localiser.controls.RawOrFFTParams;
import Localiser.controls.RawOrFFTParamsInterface;
import Localiser.controls.TOADTimingPane;
import Localiser.controls.TOADTimingParams;
import PamController.SettingsPane;
import PamguardMVC.PamDataBlock;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamTitledBorderPane;

/**
 * Dialog panel for a generic TOAD calculator. 
 * by default, it will include all RAW and FFT data sources which might
 * be used to get timing information and will also add the detection source
 * itself if that can provide FFT or RAW data. 
 * Note that the detection source and the source of data used for timing are
 * not necessarily the same !
 * @author dg50
 *
 */
public class GenericTOADSourcePane extends SettingsPane<GenericTOADSourceParams> {
	
	private PamBorderPane borderPane;
	
	private RawOrFFTPane rawOrFFTPane;

	private TOADTimingPane toadTimingPane;

	private GenericTOADCalculator genericTOADCalculator;

	public GenericTOADSourcePane(Object ownerWindow, GenericTOADCalculator genericTOADCalculator, PamDataBlock<?> detectionSource) {
		super(ownerWindow);
		this.genericTOADCalculator = genericTOADCalculator;
		rawOrFFTPane = new RawOrFFTPane(ownerWindow);
		toadTimingPane = new TOADTimingPane(ownerWindow);
		toadTimingPane.setTimingSource(detectionSource);
		rawOrFFTPane.setAllowSourceChoice(true);
		borderPane = new PamBorderPane(); // this is the pane that get's returned. 
		VBox vBox = new VBox(rawOrFFTPane.getContentNode(), toadTimingPane.getContentNode());
		
		PamTitledBorderPane tb = new PamTitledBorderPane("TDOA Options", vBox);
		borderPane.setTop(tb);
		PamGridPane gridPane = new PamGridPane();
		
		rawOrFFTPane.setOnlyAllowedDataBlock(genericTOADCalculator.getFftDataOrganiser().getOnlyAllowedDataBlock());
		
		rawOrFFTPane.getBeamDataSourcePane().addSelectionListener(new ChangeListener<PamDataBlock>() {
			@Override
			public void changed(ObservableValue<? extends PamDataBlock> observable, PamDataBlock oldValue,
					PamDataBlock newValue) {
				toadTimingPane.enableTimingOptions(newValue);
			}
		});
	}

	@Override
	public GenericTOADSourceParams getParams(GenericTOADSourceParams toadSourceParams) {
		if (toadSourceParams == null) {
			toadSourceParams = new GenericTOADSourceParams();
		}
		
		RawOrFFTParamsInterface params = rawOrFFTPane.getParams(toadSourceParams.getRawOrFFTParams());
		if (params == null) {
			return null;
		}
		else {
			toadSourceParams.setRawOrFFTParams((RawOrFFTParams) params);
		}
		DelayMeasurementParams ttp = toadTimingPane.getParams(toadSourceParams.getToadTimingParams());
		if (ttp == null) {
			return null;
		}
		else {
			toadSourceParams.setToadTimingParams(ttp);
		}
//		toad
		return toadSourceParams;
	}

	@Override
	public void setParams(GenericTOADSourceParams sourceParams) {
		if (sourceParams == null) {
			return;
		}
		rawOrFFTPane.setTimingSource(genericTOADCalculator.getTimingSource());
		toadTimingPane.setTimingSource(genericTOADCalculator.getTimingSource());
		if (sourceParams instanceof GenericTOADSourceParams) {
			GenericTOADSourceParams toadSourceParams = (GenericTOADSourceParams) sourceParams;
			rawOrFFTPane.setParams(toadSourceParams.getRawOrFFTParams());
			toadTimingPane.setParams(toadSourceParams.getToadTimingParams());
		}
	}

	@Override
	public String getName() {
		return "TDOA Timing Options";
	}

	@Override
	public Node getContentNode() {
		return borderPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Set the detection source, which will be needed
	 * by some of the panels ...
	 * @param detectionSource
	 */
	public void setDetectionSource(PamDataBlock detectionSource) {
		rawOrFFTPane.setDetectionSource(detectionSource);
	}
	
}
