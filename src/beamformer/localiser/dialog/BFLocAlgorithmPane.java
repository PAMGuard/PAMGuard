package beamformer.localiser.dialog;

import javax.swing.SwingUtilities;

import PamController.SettingsPane;
import PamUtils.PamUtils;
import PamView.GroupedSourceParameters;
import beamformer.BeamAlgorithmParams;
import beamformer.algorithms.BeamAlgorithmProvider;
import beamformer.localiser.BFLocaliserParams;
import beamformer.localiser.BeamFormLocaliserControl;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;

public class BFLocAlgorithmPane extends SettingsPane<BFLocaliserParams> {

	private PamBorderPane mainPane;
	private GridPane algoPane;
	private BeamFormLocaliserControl bfLocControl;
	private ChoiceBox<String>[] algoSelection;
	private BFLocSettingsPane2 bfLocSettingsPane;
	
	public BFLocAlgorithmPane(Object window, BeamFormLocaliserControl bfLocControl, BFLocSettingsPane2 bfLocSettingsPane) {
		super(window);
		this.bfLocSettingsPane = bfLocSettingsPane;
		this.bfLocControl = bfLocControl;
		algoPane = new GridPane();
		algoPane.setVgap(2);
		algoPane.setHgap(12);
//		algoPane.setPadding(new Insets(2, 12, 2, 12));
		mainPane = new PamBorderPane(new PamTitledBorderPane("Algorithm selection", algoPane));
		mainPane.setPadding(new Insets(10, 5, 10, 5));
		/**
		 * Need to make a dummy set of data here which will correclty 
		 * size the dialog. this is only important during a first configuration
		 * when 0 channels are selected, after that it's unimportant. 
		 */
		GroupedSourceParameters gsp = new GroupedSourceParameters();
		gsp.setChanOrSeqBitmap(1);
		gsp.setChannelGroups(new int[1]);
		buildPanel(gsp);
	}

	@Override
	public BFLocaliserParams getParams(BFLocaliserParams params) {
		int nGroup = GroupedSourcePaneFX.countChannelGroups(params.getChannelBitmap(), params.getChannelGroups());
		int n = Math.min(nGroup, algoSelection.length);
		int groupMap = GroupedSourcePaneFX.getGroupMap(params.getChannelBitmap(), params.getChannelGroups());
		int[] groupList = PamUtils.getChannelArray(groupMap);
		params.clearAlgorithmNames();
		for (int i = 0; i < n; i++) {
			int groupChanMap = GroupedSourcePaneFX.getGroupChannels(groupList[i], params.getChannelBitmap(), params.getChannelGroups());
			String algoName = algoSelection[i].getSelectionModel().getSelectedItem();
			if (algoName == null) {
				return null;
			}
			params.addAlgorithmNames(algoName);
			BeamAlgorithmParams groupParams = params.getAlgorithmParms(groupList[i], groupChanMap, algoName);
			if (groupParams == null) {
				// algo params have not been set - need to issue a warning !
//				BeamAlgorithmProvider algoProvider = bfLocControl.findAlgorithmByName(algoName);
//				if (algoProvider )
			}
			
		}
		
		return params;
	}

	@Override
	public void setParams(BFLocaliserParams params) {
		buildPanel(params.getGroupedSourceParameters());
		int nGroup = GroupedSourcePaneFX.countChannelGroups(params.getChannelBitmap(), params.getChannelGroups());
		int n = Math.min(nGroup, algoSelection.length);
		int groupMap = GroupedSourcePaneFX.getGroupMap(params.getChannelBitmap(), params.getChannelGroups());
		int[] groupList = PamUtils.getChannelArray(groupMap);
		for (int i = 0; i < n; i++) {
			int groupChanMap = GroupedSourcePaneFX.getGroupChannels(groupList[i], params.getChannelBitmap(), params.getChannelGroups());
			String algoName = params.getAlgorithmName(i);
//			BeamAlgorithmParams groupParams = params.getAlgorithmParms(groupList[i], groupChanMap, params.getAlgorithmName(i));
			if (algoName != null) {
				algoSelection[i].getSelectionModel().select(algoName);
			}
		}
	}

	@Override
	public String getName() {
		return "Algorithms";
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
	 * Update algorithm list based on channel grouping. 
	 * @param sourceParameters
	 */
	public synchronized void changeChannelGrouping(BFLocaliserParams bfLocParams) {
		setParams(bfLocParams);
	}
	
	private synchronized void buildPanel(GroupedSourceParameters sourceParameters) {
		algoPane.getChildren().removeAll(algoPane.getChildren());
		int iRow = 0;
		algoPane.add(new Label("Group"), 0, iRow);
		algoPane.add(new Label("Algorithm"), 2, iRow);
		algoPane.add(new Label("n Chan"), 1, iRow);
		algoPane.add(new Label("Settings"), 3, iRow);
		int nGroup = GroupedSourcePaneFX.countChannelGroups(sourceParameters.getChanOrSeqBitmap(), sourceParameters.getChannelGroups());
		algoSelection = new ChoiceBox[nGroup];
		Button[] settingsButtons = new Button[nGroup];
		if (bfLocControl == null) return;
		BeamAlgorithmProvider[] allAlgorithms = bfLocControl.getAlgorithmList();
		for (int i = 0; i < nGroup; i++) {
			iRow++;
			Label idLab;
			algoPane.add(idLab = new Label(String.format("%d", i)), 0, iRow);
			GridPane.setHalignment(idLab, HPos.CENTER);
			algoSelection[i] = new ChoiceBox<>();
			for (int a = 0; a < allAlgorithms.length; a++) {
				algoSelection[i].getItems().add(allAlgorithms[a].getStaticProperties().getName());
			}
			algoPane.add(algoSelection[i], 2, iRow);
			
			int chanList = GroupedSourcePaneFX.getGroupChannels(i, sourceParameters.getChanOrSeqBitmap(), sourceParameters.getChannelGroups());
			int nChan = PamUtils.getNumChannels(chanList);
			Label chanLab;
			algoPane.add(chanLab = new Label(String.format("%d", nChan)), 1, iRow);
			GridPane.setHalignment(chanLab, HPos.CENTER);
			chanLab.setTooltip(new Tooltip("Channels [" + PamUtils.getChannelList(chanList) + "]"));

//			settingsButtons[i] = new Button("",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS,Color.WHITE, PamGuiManagerFX.iconSize));
			settingsButtons[i] = new Button("",PamGlyphDude.createPamIcon("mdi2c-cog",Color.WHITE, PamGuiManagerFX.iconSize));
			algoPane.add(settingsButtons[i], 3, iRow);
			GridPane.setHalignment(settingsButtons[i], HPos.CENTER);
			settingsButtons[i].setOnAction(new AlgorithmSettings(i));
		}
	}
	
	private class AlgorithmSettings implements EventHandler<ActionEvent> {

		private int iGroup;

		public AlgorithmSettings(int iGroup) {
			super();
			this.iGroup = iGroup;
		}

		@Override
		public void handle(ActionEvent event) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					algorithmSettings(iGroup);
				}
			});
		}
	}

	protected void algorithmSettings(int iGroup) {
		/**
		 * Get the current algorithm name and its current settings ....
		 */
		BFLocaliserParams params = bfLocSettingsPane.getCurrentParms();
		int nGroup = GroupedSourcePaneFX.countChannelGroups(params.getChannelBitmap(), params.getChannelGroups());
		int n = Math.min(nGroup, algoSelection.length);
		int groupMap = GroupedSourcePaneFX.getGroupMap(params.getChannelBitmap(), params.getChannelGroups());
		int[] groupList = PamUtils.getChannelArray(groupMap);
		int groupChanMap = GroupedSourcePaneFX.getGroupChannels(iGroup, params.getChannelBitmap(), params.getChannelGroups());
		String selectedName = algoSelection[iGroup].getSelectionModel().getSelectedItem();
		if (selectedName == null) {
			return;
		}
		BeamAlgorithmParams groupParams = params.getAlgorithmParms(groupList[iGroup], groupChanMap, selectedName);
		BeamAlgorithmProvider curralgoProvider = bfLocControl.findAlgorithmByName(selectedName);
		if (groupParams == null) {
			if (curralgoProvider == null) {
				return;
			}
			groupParams = curralgoProvider.createNewParams(selectedName, groupList[iGroup], groupChanMap);
		}
		groupParams.setCanBeam(false); // localiser always uses beamogram, not individual beams
		groupParams.setCanBeamogram(true);
		SettingsPane<BeamAlgorithmParams> theSettingsPane = (SettingsPane<BeamAlgorithmParams>) curralgoProvider.getParamsDialog(params, groupParams);
//		SettingsDialog<?> settingsDialog=new SettingsDialog<>(theSettingsPane);
//		settingsDialog.setResizable(true);
//		settingsDialog.setOnShown((value)->{theSettingsPane.paneInitialized();});
//		//show the dialog 
//		settingsDialog.showAndWait().ifPresent(response -> {
//			if (response!=null) {
//				params.setAlgorithmParams(selectedName, groupList[iGroup], groupChanMap, curralgoProvider.getCurrentParams());
//			}
//		});
		PamDialogFX2AWT<BeamAlgorithmParams> algDialog = new PamDialogFX2AWT<BeamAlgorithmParams>(getAWTWindow(), theSettingsPane, false);
		BeamAlgorithmParams newParams = algDialog.showDialog(groupParams);
		if (newParams instanceof BeamAlgorithmParams) {
			params.setAlgorithmParams(selectedName, groupList[iGroup], groupChanMap, (BeamAlgorithmParams) newParams);
		}
	}


}
