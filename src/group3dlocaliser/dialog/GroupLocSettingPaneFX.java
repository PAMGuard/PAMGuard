package group3dlocaliser.dialog;

import java.util.ArrayList;

import javax.swing.JFrame;

import Array.ArrayManager;
import PamController.SettingsPane;
import PamDetection.PamDetection;
import PamView.GroupedDataSource;
import PamView.symbol.SwingSymbolOptionsPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.Group3DParams;
import group3dlocaliser.algorithm.LocaliserAlgorithm3D;
import group3dlocaliser.algorithm.LocaliserAlgorithmParams;
import group3dlocaliser.algorithm.LocaliserAlgorithmProvider;
import group3dlocaliser.grouper.DetectionGrouperParams;
import group3dlocaliser.grouper.dialog.GrouperSettingsPane;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;
import pamViewFX.fxNodes.utilityPanes.BorderPaneFX2AWT;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;

public class GroupLocSettingPaneFX extends SettingsPane<Group3DParams>{

	private static final String helpPoint = "localisation.group3d.docs.3doverview";

	private SourcePaneFX sourcePanel;
	
	private GrouperSettingsPane grouperSettingsPane;
	
	private PamBorderPane mainPane = new PamBorderPane();

	private Group3DLocaliserControl group3dLocaliserControl;
	
	private ChoiceBox<String> algorithms;
	
	private ManagedSettingsPane<?> algorithmSourcePane;

	private Button algoOptsButton;
	
	private PamBorderPane algoSourceHolder;

	private Group3DParams currentParams;
	
	private Object ownerWindow;
	
	public GroupLocSettingPaneFX(Group3DLocaliserControl group3dLocaliserControl, Object ownerWindow) {
		super(ownerWindow);		
		this.ownerWindow = ownerWindow;
		this.group3dLocaliserControl = group3dLocaliserControl;
		
		PamTabPane tabPane = new PamTabPane();
		tabPane.setAddTabButton(false);
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		
		sourcePanel = new SourcePaneFX(PamDataUnit.class, false, true);
		sourcePanel.setDataBlockClassType(GroupedDataSource.class);
		sourcePanel.addSelectionListener(new ChangeListener<PamDataBlock>() {
			@Override
			public void changed(ObservableValue<? extends PamDataBlock> observable, PamDataBlock oldValue,
					PamDataBlock newValue) {
				newDataBlockSelection(newValue);
				
			}
		});
		
		grouperSettingsPane = new GrouperSettingsPane(ownerWindow, "Detection matching options");
		PamBorderPane gsp = new PamBorderPane();
		gsp.setTop(grouperSettingsPane.getContentNode());
		

		// to put source and matching on separate tabs ....
//		tabPane.getTabs().add(new Tab("Source", new PamTitledBorderPane("Detection Source", sourcePanel)));
//		tabPane.getTabs().add(new Tab("Matching", gsp));
		// or on the same tab. 
		PamBorderPane borderPane = new PamBorderPane();
		borderPane.setTop(new PamTitledBorderPane("Source", sourcePanel));
		borderPane.setCenter(gsp);
		tabPane.getTabs().add(new Tab("Detection source", borderPane));
		
		
		
		algorithms = new ChoiceBox<>();
		setAlgorithmList(); // call here so that box gets correct size. 
		
		PamBorderPane algoGrid = new PamBorderPane();
//		HBox.setHgrow(algoGrid, Priority.ALWAYS);
		algoGrid.setCenter(algorithms);
		algorithms.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		GridPane.setFillWidth(algorithms, true);
//		algoGrid.add(new Label("Algorithm Options "), 0, 1);
//		algoOptsButton = new Button("",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS,Color.WHITE, PamGuiManagerFX.iconSize));
		algoOptsButton = new Button("",PamGlyphDude.createPamIcon("mdi2c-cog",Color.WHITE, PamGuiManagerFX.iconSize));
		algoGrid.setRight(algoOptsButton);
		algoOptsButton.setTooltip(new Tooltip("More Algorithm Options ..."));
		algoSourceHolder = new PamBorderPane();
//		HBox.setHgrow(algoSourceHolder, Priority.ALWAYS);
//		algoGrid.add(algoSourceHolder, 0, 2, 4, 1);
		PamBorderPane algoMainPane = new PamBorderPane();
		PamTitledBorderPane ptb = new PamTitledBorderPane("Select Localisation Algorithm", algoGrid);
		algoMainPane.setTop(ptb);
		algoMainPane.setCenter(algoSourceHolder);
		tabPane.getTabs().add(new Tab("Algorithm", algoMainPane));
		
		algoOptsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				moreAlgorithmOptions();
			}
		});
		
		algorithms.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				selectAlgorithm();
			}
		});
		
		
		mainPane.setCenter(tabPane);
	}

	/**
	 * Called whenever the algorithm selection is changed. 
	 */
	protected void selectAlgorithm() {
		/**
		 * Algorithm options fall into two separate parts. The first, which primarily involves
		 * timing measurements is included in the bottom half of the main dialog tab, so 
		 * is always visible. In principle an algorithm can put anything here, but the TOAD based
		 * localisers will all fill this space with TOAD options from the source data block and 
		 * channel selection. Currently, crossed bearings put nothing here at all. 
		 * There is also the morealgorithmOptions button do the right of the algorithm selector
		 * which can contain additional algorithm options, such as the number of MCMC chains, whether
		 * Simplex should start with a hyperbolic, etc. 
		 */
		String algoName  = algorithms.getSelectionModel().getSelectedItem();
		if (algoName == null) {
			algoOptsButton.setDisable(true);
			return;
		}
		LocaliserAlgorithm3D localiserAlgorithm = group3dLocaliserControl.findAlgorithm(algoName);
		if (localiserAlgorithm == null) {
			algoOptsButton.setDisable(true);
			return;
		}
		// also enable / disable the more options button ...
		algoOptsButton.setDisable(localiserAlgorithm.hasParams() == false);
		
		/**
		 * Need to immediately tell the algorithm which input we're using so that it can 
		 * show the correct settings...
		 */
		PamDataBlock<?> currSource = sourcePanel.getSource();
		if (currSource != null) {
			localiserAlgorithm.prepare(currSource);
		}
		
		/*
		 * This gets called whenever either the source or the algorithm change ...
		 * The algorithm will return an options panel, but that algorithm (especially
		 * TOAD based ones may delegate back into the source information to decide 
		 * what options to display, i.t TOAD options for clicks, whistles and generic
		 * Time frequency things are NOT the same. 
		 */
		ManagedSettingsPane<?> newPane = localiserAlgorithm.getSourceSettingsPane(getAWTWindow(), currSource);
		if (newPane == algorithmSourcePane) {
			return;
		}
		if (algorithmSourcePane != null) {
			algoSourceHolder.setCenter(null);
		}
		if (newPane != null) {
//			PamTitledBorderPane p = new PamTitledBorderPane("Algorithm options", newPane.getSettingsPane().getContentNode());
			PamBorderPane borderPane = new PamBorderPane(newPane.getSettingsPane().getContentNode());
			borderPane.setLeftSpace(5);
			borderPane.setRightSpace(5);
			borderPane.setBottomSpace(5);
			algoSourceHolder.setCenter(borderPane);
			LocaliserAlgorithmParams locParams = group3dLocaliserControl.getLocaliserAlgorithmParams(localiserAlgorithm);
			newPane.setParams();
//			newPane.setDetectionSource(sourcePanel.getSource());
		}
		algorithmSourcePane = newPane;
				
		
		repackDialog();
	}

	private void repackDialog() {
//		if (ownerWindow instanceof JFrame) {
//			//			((JFrame) ownerWindow).pack();
//		}
//		try {
//			Parent parent = getContentNode().parentProperty().getValue();
//			System.out.println(parent);
//			if (parent instanceof BorderPaneFX2AWT) {
//				BorderPaneFX2AWT bp = (BorderPaneFX2AWT) parent;
//				bp.repackSwingDialog(getContentNode());
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
		BorderPaneFX2AWT.repackSwingDialog(getContentNode());
	}

	/**
	 * Handle algorithm options ...
	 */
	protected void moreAlgorithmOptions() {
		String algoName  = algorithms.getSelectionModel().getSelectedItem();
		if (algoName == null) {
			return;
		}
		LocaliserAlgorithm3D localiserAlgorithm = group3dLocaliserControl.findAlgorithm(algoName);
		if (localiserAlgorithm == null) {
			return;
		}
		if (localiserAlgorithm.hasParams() == false) {
			return;
		}
		LocaliserAlgorithmParams algorithmPaams = group3dLocaliserControl.getLocaliserAlgorithmParams(localiserAlgorithm);
		algorithmPaams = localiserAlgorithm.showAlgorithmDialog(getAWTWindow(), algorithmPaams);
		if (algorithmPaams != null) {
			group3dLocaliserControl.setAlgorithmParams(localiserAlgorithm, algorithmPaams);
		}
	}

	@Override
	public Group3DParams getParams(Group3DParams currParams) {
		PamDataBlock<?> currSource = sourcePanel.getSource();
		if (currSource == null) {
			SwingFXDialogWarning.showWarning(this.getOwnerWindow(), "Invalid DataBlock", "You must select a data source");
			return null;
		}
		currParams.setSourceName(currSource.getLongDataName());
//		currParams.getGroupedSourceParams().setChanOrSeqBitmap(sourcePanel.getChannelList());
		
		DetectionGrouperParams grouping = grouperSettingsPane.getParams(currParams.getGrouperParams());
		if (grouping == null) {
			return null;
		}
		else {
			currParams.setGrouperParams(grouping);
		}
		
		String algoName  = algorithms.getSelectionModel().getSelectedItem();
		if (algoName == null) {
			SwingFXDialogWarning.showWarning(this.getOwnerWindow(), "Localisation algorithm", "You must select a localisation algorithm");
			return null;
		}
		currParams.setAlgorithmName(algoName);
		LocaliserAlgorithm3D algoProvider = group3dLocaliserControl.findAlgorithm(algoName);
		
		if (algorithmSourcePane != null && currParams != null && algoProvider != null) {
			LocaliserAlgorithmParams locParams = currParams.getAlgorithmParams(algoProvider);
			if (algorithmSourcePane != null) {
				Object ans = algorithmSourcePane.getParams();
				if (ans == null) {
					return null;
				}
//				else {
//					locParams.setAlgorithmParams(algoProvider, ans);
//				}
//				
//				if (ans instanceof LocaliserAlgorithmParams) {
//					currParams.setAlgorithmParams(algoProvider, (LocaliserAlgorithmParams) ans);
//				}
			}
////			LocaliserAlgorithmParams sourceParams = algorithmSourcePane.getParams(locParams);
//			if (sourceParams == null) {
////				SwingFXDialogWarning.showWarning(this.getOwnerWindow(), "Localisation algorithm", "Invalid al");
//				return null;
//			}
//			currParams.setAlgorithmParams(algoProvider, sourceParams);
		}
		
		return currParams;
	}
	private LocaliserAlgorithm3D getSelectedAlgorithm() {
		String algoName  = algorithms.getSelectionModel().getSelectedItem();
		if (algoName == null) {
			return null;
		}
		return group3dLocaliserControl.findAlgorithm(algoName);
	}

	@Override
	public void setParams(Group3DParams input) {
		sourcePanel.setSourceList();
		sourcePanel.setSource(input.getSourceName());
//		sourcePanel.setChannelList(input.getGroupedSourceParams().getChanOrSeqBitmap());
		grouperSettingsPane.setParams(input.getGrouperParams());

		if (algorithmSourcePane != null) {
			algorithmSourcePane.setParams();
		}
//		algorithms.getSelectionModel().select(input.getAlgorithmName());
		currentParams = input;
		newDataBlockSelection(sourcePanel.getSource());
	}
	
	/**
	 * Make an algorithm list, only including those compatible with the 
	 * currently selected input data block. 
	 */
	private void setAlgorithmList() {
		LocaliserAlgorithm3D currAlgo = getSelectedAlgorithm();
		int currentIndex = -1;
		PamDataBlock inputDataBlock = sourcePanel.getSource();
		algorithms.getItems().clear();
		ArrayList<LocaliserAlgorithm3D> algoList = group3dLocaliserControl.getAlgorithmProviders();
		for (int i = 0; i < algoList.size(); i++) {
			LocaliserAlgorithm3D algo = algoList.get(i);
			if (inputDataBlock != null && algo.canLocalise(inputDataBlock) == false) {
				continue;
			}
			if (inputDataBlock != null) {
				int phones = inputDataBlock.getHydrophoneMap();
				int shape = ArrayManager.getArrayManager().getArrayType(phones);
				ArrayManager.getArrayManager().getArrayType(phones);
				if (algo.canArrayShape(shape) == false) {
					continue;
				}
			}
			if (algo == currAlgo) {
				currentIndex = algorithms.getItems().size();
			}
			algorithms.getItems().add(algo.getName());
		}
		if (currentIndex >= 0) {
			algorithms.getSelectionModel().select(currentIndex);
		}
		else if (currentParams != null){
			algorithms.getSelectionModel().select(currentParams.getAlgorithmName());
		}
	}
	
	private void setAlgorithmSourceParameters() {
		if (currentParams == null) {
			return;
		}
		if (algorithmSourcePane == null) {
			return;
		}
		LocaliserAlgorithm3D currAlgo = getSelectedAlgorithm();
		if (currAlgo == null) {
			return;
		}
//		algorithmSourcePane.setDetectionSource(sourcePanel.getSource());
//		LocaliserAlgorithmParams locParams = currentParams.getAlgorithmParams(currAlgo);
//		if (locParams != null) {
//			algorithmSourcePane.setParams(locParams);
//		}
	}

	@Override
	public String getName() {
		return group3dLocaliserControl.getUnitName();
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}


	private void newDataBlockSelection(PamDataBlock pamDataBlock) {
		if (pamDataBlock == null) {
			return;
		}
		if (grouperSettingsPane != null) {
			grouperSettingsPane.setDataSelector(pamDataBlock, group3dLocaliserControl.getDataSelectorName());
		}
		setAlgorithmList();
		setAlgorithmSourceParameters();
	}

	@Override
	public String getHelpPoint() {
		return helpPoint;
	}


}
