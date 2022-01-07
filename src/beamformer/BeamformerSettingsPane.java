package beamformer;

import javax.swing.SwingUtilities;

import PamController.SettingsPane;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import beamformer.algorithms.BeamAlgorithmProvider;
import fftManager.FFTDataUnit;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;

public class BeamformerSettingsPane extends SettingsPane<BeamFormerParams> {

	/**
	 * Link to the BeamFormerControl object creating this pane
	 */
	private BeamFormerBaseControl beamformerControl;
	
	/**
	 * The main tab pane. 
	 */
	private PamTabPane pamTabbedPane;

	/**
	 * Group source pane for the FFT settings pane.
	 */
	private GroupedSourcePaneFX sourcePane;
	
	/**
	 * List of group numbers to show in Algorithm Provider tab
	 */
	private Label[] groupNumLabels;

	/**
	 * List of algorithm selections
	 */
	private int[] algoSel;

	/**
	 * List of buttons to change settings
	 */
	private Button[] settingsButtons;
	
	/**
	 * Local Beamformer parameters
	 */
	private BeamFormerParams beamformerParams;
	
	/**
	 * Information on the algorithms tab
	 */
	private Tab algoTab;
	
	/**
	 * The main content pane
	 */
	private PamBorderPane mainPane = new PamBorderPane();
	
	
	/**
	 * @param arg0
	 */
	public BeamformerSettingsPane(Object owner, BeamFormerBaseControl	beamformerControl) {
		super(owner);
		this.beamformerControl =  beamformerControl;
		
		pamTabbedPane=new PamTabPane();
		pamTabbedPane.setAddTabButton(false);
		pamTabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		//add a source pane
		VBox sourcePanel = new VBox();
		sourcePanel.setPadding(new Insets(10, 5, 10, 15));
		sourcePanel.getChildren().add(this.createSourcePane());
		pamTabbedPane.getTabs().add(new Tab("Source", sourcePanel));
		

		// add algorithms selector page
		pamTabbedPane.getTabs().add(algoTab = new Tab("Algorithms", createAlgoPane()));
		
		// place the tabbed pane in the center of the window
		mainPane.setCenter(new PamBorderPane(pamTabbedPane));
		
		// add an actionlistener so we can update the info on the algorithms tab when we switch to it, and
		// save the list when we switch away
		pamTabbedPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

			@Override
			public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
				if (newValue==algoTab && sourcePane != null && sourcePane.getSource() != null) {
					beamformerParams.setDataSource(sourcePane.getSource().getLongDataName());
					algoTab.setContent(createAlgoPane());
				} else if (oldValue==algoTab) {
					saveAlgorithmList(beamformerParams);
				}
			}
		});
	}

	/**
	 * Create the source pane using FFT class as source, including channel grouping option
	 * @return pane for FFT source, including channel grouping
	 */
	private GroupedSourcePaneFX createSourcePane(){
		sourcePane = new GroupedSourcePaneFX( "FFT Data Source", FFTDataUnit.class, true, false, true);
		sourcePane.excludeDataBlock(beamformerControl.getBeamFormerProcess().getBeamFormerOutput(), true);
		sourcePane.excludeDataBlock(beamformerControl.getBeamFormerProcess().getBeamOGramOutput(), true);
		return sourcePane;
	}
	
	/**
	 * @return
	 */
	public Node createAlgoPane() {
		//create the main pane. 
		PamGridPane holderPane=new PamGridPane();
		holderPane.setGridLinesVisible(false);
		holderPane.setPadding(new Insets(5,5,5,5));
		holderPane.setHgap(10);
		holderPane.setVgap(10);
		
		// set up the grid columns
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setHgrow(Priority.ALWAYS);
		col1.setHalignment(HPos.CENTER);
		holderPane.getColumnConstraints().add(col1);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHgrow(Priority.ALWAYS);
		col2.setHalignment(HPos.LEFT);
		holderPane.getColumnConstraints().add(col2);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setHgrow(Priority.ALWAYS);
		col3.setHalignment(HPos.CENTER);
		holderPane.getColumnConstraints().add(col3);

		//create grid headings
		Label title=new Label("Group");
		title.setMinWidth(40);
		title.setAlignment(Pos.CENTER);
//		title.setFont(PamGuiManagerFX.titleFontSize2);
		holderPane.add(title, 0, 0);
		Label algo=new Label("Algorithm");
		algo.setMinWidth(210);
//		algo.setFont(PamGuiManagerFX.titleFontSize2);
		holderPane.add(algo, 1, 0);
		Label settings=new Label("Settings");
		settings.setMinWidth(50);
		settings.setAlignment(Pos.CENTER);
//		settings.setFont(PamGuiManagerFX.titleFontSize2);
		holderPane.add(settings, 2, 0);
		
		// loop through the groups defined on the source page
		int groupMap = GroupedSourcePaneFX.getGroupMap(sourcePane.getChannelList(), sourcePane.getChannelGroups());
		int[] groupList = PamUtils.getChannelArray(groupMap);
		if (groupList==null) {
			return holderPane;
		}
		int nGroups = groupList.length;
		BeamAlgorithmProvider[] allAlgorithms = beamformerControl.getAlgorithmList();
		settingsButtons = new Button[nGroups];
		groupNumLabels = new Label[nGroups];
		algoSel = new int[nGroups];
		for (int i=0; i<nGroups; i++) {
			
			// create a final variable to hold the current group index - required for button actionlistener
			final int groupIdx = i;
			
			// get the channel map for this group and create a final variable for it as well
			int curChanMap = GroupedSourcePaneFX.getGroupChannels(groupList[i], sourcePane.getChannelList(), sourcePane.getChannelGroups());
			final int finalChanMap = curChanMap;
			
			// create the group number label, the list of algorithm providers, and the settings button
			groupNumLabels[i] = new Label(String.valueOf(groupList[i]));
			final ChoiceBox<String> algoList = new ChoiceBox<String>();
			
			// monitor button - debugging only
//			algoList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
//
//				@Override
//				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//					System.out.printf("Old - New Values = %d - %d\n", oldValue, newValue);
//				}
//			});

			// add all potential algorithms to the algorithm selection box
			for (int j=0; j<allAlgorithms.length; j++) {
				algoList.getItems().add(allAlgorithms[j].getStaticProperties().getName());
			}
			
			// check the parameters hashmap, to see if an algorithm has already been assigned to this group and channel map.  If it
			// has, select that one in the list.  If it hasn't, select the first one in the list and create a
			// new parameters object for it
			BeamAlgorithmParams thisAlgo = beamformerParams.getAlgorithmParms(groupList[i], curChanMap, beamformerParams.getAlgorithmName(i));
			if (thisAlgo!=null) {
				 algoList.getSelectionModel().select(thisAlgo.getAlgorithmName());
			} else {
				algoList.getSelectionModel().selectFirst();
			}
			algoSel[groupIdx] = algoList.getSelectionModel().getSelectedIndex();
			
			// create the button and add the listener
//			settingsButtons[i] = new Button("",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS,Color.WHITE, PamGuiManagerFX.iconSize));
			settingsButtons[i] = new Button("",PamGlyphDude.createPamIcon("mdi2c-cog",Color.WHITE, PamGuiManagerFX.iconSize));
			settingsButtons[i].setOnAction((action)->{
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						settingsButtonPress(groupIdx, groupList[groupIdx], finalChanMap, algoList.getSelectionModel().getSelectedIndex());
					}
				});
			});
			
			// add all to the current row in the grid
			holderPane.add(groupNumLabels[i], 0, i+1);
			holderPane.add(algoList, 1, i+1);
			holderPane.add(settingsButtons[i], 2, i+1);
		}
		// return the pane
		return holderPane;
	}
	



	/**
	 * @param groupIdx the index number of the group that was pressed (e.g. the first group listed would be 0, the second group 1, etc)
	 * @param groupNum the group number at the groupIdx position
	 * @param groupChanMap the group channel map for this group number
	 */
	private void settingsButtonPress(int groupIdx, int groupNum, int groupChanMap, int algoProviderIdx) {
		
		// save the algorithm selection to the selection field
		algoSel[groupIdx] = algoProviderIdx;
		
		// get the name of the algorithm the user has selected for this group
		BeamAlgorithmProvider[] allAlgorithms = beamformerControl.getAlgorithmList();
		String newAlgoName = allAlgorithms[algoProviderIdx].getStaticProperties().getName();
		
		// If the algorithm provider selected by the user doesn't match the BeamFormerParams hashmap, create a new parameters object
		BeamAlgorithmParams oldAlgo = beamformerParams.getAlgorithmParms(groupNum, groupChanMap, newAlgoName);
		if (oldAlgo==null) {
			BeamAlgorithmParams newParams = allAlgorithms[algoProviderIdx].createNewParams(
					allAlgorithms[algoProviderIdx].getStaticProperties().getName(),
					groupNum,
					groupChanMap);
			newParams.setCanBeam(true);
			newParams.setCanBeamogram(true);
			beamformerParams.setAlgorithmParams(newAlgoName, groupNum, groupChanMap, newParams);
			oldAlgo=newParams;
		}
		
		// create the FX pane and shove it into an AWT dialog to display
		SettingsPane<BeamAlgorithmParams> theSettingsPane = (SettingsPane<BeamAlgorithmParams>) allAlgorithms[algoProviderIdx].getParamsDialog(beamformerParams, oldAlgo);
		PamDialogFX2AWT<BeamAlgorithmParams> algDialog = new PamDialogFX2AWT<BeamAlgorithmParams>(getAWTWindow(), theSettingsPane, false);
		BeamAlgorithmParams newParams = algDialog.showDialog(oldAlgo);
		if (newParams != null) {
			beamformerParams.setAlgorithmParams(newAlgoName, groupNum, groupChanMap, newParams);
		}
	}

	/**
	 * Saves the values in the GUI to the parameters object
	 * @param bfParams is a parameters object passed into this function by PamDialogFX2AWT, but ignored
	 */
	@Override
	public BeamFormerParams getParams(BeamFormerParams bfParams) {
		PamDataBlock rawDataBlock = sourcePane.getSource();
		if (rawDataBlock == null){
			PamDialogFX.showWarning("There is no datablock set. The beamformer must have a datablock set."); 
			return null;
		}
		sourcePane.getParams(beamformerParams.getGroupedSourceParameters());
//		sourcePane.getParams(bfParams.getGroupedSourceParameters());
//		bfParams.dataSource = rawDataBlock.toString();
//		bfParams.channelBitmap = sourcePane.getChannelList();
//		bfParams.channelGroups = sourcePane.getChannelGroups();
//		bfParams.groupingType = sourcePane.getGrouping();
		
		// algorithm provider pane
//		this.saveAlgorithmList(bfParams);
		this.saveAlgorithmList(beamformerParams);

//		return bfParams;
		return beamformerParams;
	}
	
	/**
	 * Saves the current state of the algorithm tab to the hashtable.
	 * @param bfParams 
	 */
	public void saveAlgorithmList(BeamFormerParams bfParams) {
		int groupMap = GroupedSourcePaneFX.getGroupMap(sourcePane.getChannelList(), sourcePane.getChannelGroups());
		int[] groupList = PamUtils.getChannelArray(groupMap);
		bfParams.clearAlgorithmNames();
		if (groupList!=null) {
			
			// if algoSel==null, it means the user has selected channels to analyze but hasn't gone to the algorithms tab to actually set them up.  In that case, run createAlgoPane
			// in order to set up defaults for the selected channels.
			if (algoSel==null) {
				createAlgoPane();
			}
			int nGroups = groupList.length;
			BeamAlgorithmProvider[] allAlgorithms = beamformerControl.getAlgorithmList();
			int n = Math.min(nGroups, algoSel.length);
			for (int i=0; i<n; i++) {
				int curChanMap = GroupedSourcePaneFX.getGroupChannels(groupList[i], sourcePane.getChannelList(), sourcePane.getChannelGroups());
				int algoProviderIdx = algoSel[i];
				if (algoProviderIdx < 0) continue;
				BeamAlgorithmProvider currAlgo = allAlgorithms[algoProviderIdx];
				bfParams.addAlgorithmNames(currAlgo.getStaticProperties().getName());
				BeamAlgorithmParams curAlgoParams = bfParams.getAlgorithmParms(groupList[i], curChanMap, allAlgorithms[algoProviderIdx].getStaticProperties().getName());
				if (curAlgoParams == null) {
					curAlgoParams = currAlgo.createNewParams(currAlgo.getStaticProperties().getName(), 0, curChanMap);
				}
//				bfParams.setAlgorithmParams(groupList[i], curChanMap, curAlgoParams);
			}
		}
	}

	/**
	 * Loads the passed beamformer parameters into the GUi
	 */
	@Override
	public void setParams(BeamFormerParams input) {
		this.beamformerParams = input.clone();
		
		sourcePane.setParams(beamformerParams.getGroupedSourceParameters());
		// source pane
//		PamDataBlock fftDataBlock = PamController.getInstance().
//				getFFTDataBlock(beamformerParams.dataSource);
//		if (fftDataBlock != null) {
//			sourcePane.setSource(fftDataBlock);
//		}
//		else {
//			sourcePane.setSourceIndex(0);
//		}
//		sourcePane.setGrouping(beamformerParams.groupingType);
//		sourcePane.setChannelGroups(beamformerParams.channelGroups);
//		sourcePane.setChannelList(beamformerParams.channelBitmap);
		
		// algorithms pane
		this.createAlgoPane();
		pamTabbedPane.getSelectionModel().select(0); // always start with the source tab
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getName()
	 */
	@Override
	public String getName() {
		return "Beamformer Settings";
	}

	/* (non-Javadoc)
	 * @see PamController.SettingsPane#getContentNode()
	 */
	@Override
	public Node getContentNode() {
		return mainPane;
	}

	/**
	 * Always go to the first (source) pane when opening the dialog.  Without this, the window will open on whatever tab
	 * was last selected.  The algorithm pane was sometimes not repainting properly if it was the current pane,
	 * so this way the createAlgoPane() method will always be run because the user will have to switch to
	 * the tab manually
	 */
	@Override
	public void paneInitialized() {
		pamTabbedPane.getSelectionModel().selectFirst();
	}
	
	/* (non-Javadoc)
	 * @see PamController.SettingsPane#repackContents()
	 */
	@Override
	public void repackContents() {
		pamTabbedPane.repackTabs();
	}

	/**
	 * @return the pamTabbedPane
	 */
	public PamTabPane getPamTabbedPane() {
		return pamTabbedPane;
	}

	@Override
	public String getHelpPoint() {
		return "sound_processing.beamformer.docs.Beamformer_Settings";
	}

}
