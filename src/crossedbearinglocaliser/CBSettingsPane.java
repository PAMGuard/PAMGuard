package crossedbearinglocaliser;

import PamController.SettingsPane;
import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import annotation.AnnotationSettingsDialog;
import annotation.localise.targetmotion.TMAnnotationOptions;
import annotation.localise.targetmotion.TMAnnotationType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;

public class CBSettingsPane extends SettingsPane<CBLocaliserSettngs> {

	private CBLocaliserControl cbLocaliserControl;
	
	private PamBorderPane mainPane = new PamBorderPane();
	
	private SourcePaneFX sourcePane;
	
	private Button dataSelection;
	
	private Spinner<Integer> minDetections;
	
	private Button locModelSelection;

	private CBLocaliserSettngs currentParams;

	private IntegerSpinnerValueFactory minDetsValue;

	public CBSettingsPane(Object ownerWindow, CBLocaliserControl cbLocaliserControl) {
		super(ownerWindow);
		this.cbLocaliserControl = cbLocaliserControl;
		sourcePane = new SourcePaneFX(PamDataUnit.class, false, true);
		sourcePane.setLocalisationRequirements(LocContents.HAS_BEARING);
		PamBorderPane outerSourcePane = new PamBorderPane(sourcePane);
//		dataSelection = new Button("Data Selection ",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS,Color.WHITE, PamGuiManagerFX.iconSize));
		dataSelection = new Button("Data Selection ",PamGlyphDude.createPamIcon("mdi2c-cog",Color.WHITE, PamGuiManagerFX.iconSize));
		PamBorderPane bPane = new PamBorderPane();
		dataSelection.setAlignment(Pos.TOP_LEFT);
		bPane.setRight(dataSelection);
		bPane.setTopSpace(5);
		outerSourcePane.setBottom(bPane);
		mainPane.setTop(new PamTitledBorderPane("Detection Source", outerSourcePane));
		
		GridPane locPane = new GridPane();
		mainPane.setBottom(new PamTitledBorderPane("Localisation", locPane));
		locPane.add(new Label("Minimum No. Detections "), 0	, 0);
		minDetections = new Spinner<>();
		minDetsValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(2, Integer.MAX_VALUE, 2);
		minDetections.setValueFactory(minDetsValue);
		locPane.add(minDetections, 1, 0);
//		locModelSelection = new Button("localisation Model ",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS,Color.WHITE, PamGuiManagerFX.iconSize));
		locModelSelection = new Button("localisation Model ",PamGlyphDude.createPamIcon("mdi2c-cog",Color.WHITE, PamGuiManagerFX.iconSize));
		locPane.add(locModelSelection, 1, 1);
		
		sourcePane.addSelectionListener(new ChangeListener<PamDataBlock>() {
			@Override
			public void changed(ObservableValue<? extends PamDataBlock> observable, PamDataBlock oldValue,
					PamDataBlock newValue) {
				changedSource();
			}		
		});
		locModelSelection.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				selLocModel();
			}
		});
		dataSelection.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				selDataSelection();
			}
		});
	}

	protected void selLocModel() {
		if (currentParams == null) return;
		TMAnnotationType tma = cbLocaliserControl.getTmAnnotationType();
		tma.setAnnotationOptions(currentParams.getTmAnnotationOptions());
		boolean ans = AnnotationSettingsDialog.showDialog(getAWTWindow(), tma);
		if (ans) {
			currentParams.setTmAnnotationOptions((TMAnnotationOptions) tma.getAnnotationOptions());
		}
	}

	protected void selDataSelection() {
		DataSelector dataSelector = getDataSelector();
		if (dataSelector == null) {
			SwingFXDialogWarning.showWarning("No data selector available");
			return;
		}
		/**
		 * this dialog is probably using AWT components. I hope it's
		 * OK to open it on the FX Application thread (where we are now!) if
		 * not use SwingUtilities.invokeLater(...). Seems Ok
		 */
		dataSelector.showSelectDialog(getAWTWindow());
	}
	
	private DataSelector getDataSelector() {
		PamDataBlock dataBlock = sourcePane.getSource();
		if (dataBlock == null) {
			dataSelection.setDisable(true);
			return null;
		}
		return dataBlock.getDataSelector(cbLocaliserControl.getDataSelectorName(), false);
	}

	protected void changedSource() {
		dataSelection.setDisable(getDataSelector() == null);
	}

	@Override
	public CBLocaliserSettngs getParams(CBLocaliserSettngs notUsed) {
		currentParams.setParentDataBlock(sourcePane.getSourceLongName());
		currentParams.setMinDetections(minDetsValue.getValue());
		return currentParams;
	}

	@Override
	public void setParams(CBLocaliserSettngs input) {
		currentParams = input.clone();
		sourcePane.setSource(input.getParentDataBlock());
		minDetsValue.setValue(input.getMinDetections());
	}

	@Override
	public String getName() {
		return cbLocaliserControl.getUnitName() + " settings";
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
