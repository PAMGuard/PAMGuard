package Localiser.controls;

import Localiser.DelayMeasurementParams;
import PamController.SettingsPane;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataHolder;
import fftFilter.FFTFilterDialog;
import fftFilter.FFTFilterParams;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;

public class TOADTimingPane extends SettingsPane<DelayMeasurementParams> {

	private CheckBox filterData; 
	
	private CheckBox useEnvelope;
	
	private CheckBox useLeadingEdge;
	
	private Label filterInfo;
	
	private PamBorderPane mainPane;

	private Button filterOptsButton;

	private PamDataBlock currentTimingSource;

	private DelayMeasurementParams currentParams;
	
	public TOADTimingPane(Object ownerWindow) {
		super(ownerWindow);
		mainPane = new PamBorderPane();
//		HBox.setHgrow(mainPane, Priority.ALWAYS);
//		mainPane.setBackground(Em.);
		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(4);
		int x = 0, y = 0;
		gridPane.add(filterData = new CheckBox("Filter data during measurement"), x, y, 4, 1);
//		filterOptsButton = new Button("",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS,Color.WHITE, PamGuiManagerFX.iconSize));
		filterOptsButton = new Button("",PamGlyphDude.createPamIcon("mdi2c-cog",Color.WHITE, PamGuiManagerFX.iconSize));
		gridPane.add(filterOptsButton, 4, 0);
		filterOptsButton.setTooltip(new Tooltip("Filter settings"));
		gridPane.add(filterInfo = new Label("no filter"), x=0, ++y, 5, 1);
		gridPane.add(useEnvelope = new CheckBox("Use waveform enveleope"), x=0, ++y, 5, 1);
		gridPane.add(useLeadingEdge = new CheckBox("Use envelope leading edge"), x=0, ++y, 5, 1);
		
		useEnvelope.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				enableTimingOptions(currentTimingSource);
			}
		});
		filterData.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				enableFilterOptions();
			}
		});
		filterOptsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				filterOptions();
			}
		});
		mainPane.setCenter(gridPane);
	}

	protected void enableFilterOptions() {
		filterOptsButton.setDisable(filterData.isSelected() == false);
	}

	protected void filterOptions() {
		if (currentParams == null) {
			return;
		}
		double sampleRate = Double.MAX_VALUE;
		if (currentTimingSource != null) {
			sampleRate = currentTimingSource.getSampleRate();
		}
		FFTFilterParams newFiltParams = FFTFilterDialog.showDialog(getAWTWindow(), currentParams.getFftFilterParams(), sampleRate);
		if (newFiltParams != null) {
			currentParams.setFftFilterParams(newFiltParams);
			filterInfo.setText(newFiltParams.toString());
		}
	}

	@Override
	public DelayMeasurementParams getParams(DelayMeasurementParams currParams) {
		if (currentParams == null) {
			currentParams = currParams;
		}
		if (currentParams == null) {
			currentParams = new DelayMeasurementParams();
		}
		currentParams.filterBearings = filterData.isSelected();
		currentParams.envelopeBearings = useEnvelope.isSelected();
		currentParams.useLeadingEdge = useLeadingEdge.isSelected();
		return currentParams;
	}

	@Override
	public void setParams(DelayMeasurementParams input) {
		this.currentParams = input;
		if (currentParams == null) {
			currentParams = new DelayMeasurementParams();
		}
		filterData.setSelected(currentParams.filterBearings);
		useEnvelope.setSelected(currentParams.envelopeBearings);
		useLeadingEdge.setSelected(currentParams.useLeadingEdge);
		FFTFilterParams filtParams = currentParams.getFftFilterParams();
		if (filtParams != null) {
			filterInfo.setText(filtParams.toString());
		}
		else {
			filterInfo.setText("no filtering");
		}
		enableFilterOptions();
	}
	
	public void enableTimingOptions(PamDataBlock timingSource) {
		currentTimingSource = timingSource;
		boolean hasRaw = hasRawData(timingSource);
		filterData.setDisable(false);
		useEnvelope.setDisable(!hasRaw);
		useLeadingEdge.setDisable(!hasRaw || !useEnvelope.isSelected());
	}
	
	private boolean hasRawData(PamDataBlock timingSource) {
		if (timingSource == null) {
			return false;
		}
		if (timingSource instanceof PamRawDataBlock) {
			return true;
		}
		if (RawDataHolder.class.isAssignableFrom(timingSource.getUnitClass())) {
			return true;
		}
		return false; // must be only FFT based. 
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getContentNode() {	
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	public void setTimingSource(PamDataBlock<?> timingSource) {
		currentTimingSource = timingSource;
		enableFilterOptions();
		enableTimingOptions(currentTimingSource);
	}

}
