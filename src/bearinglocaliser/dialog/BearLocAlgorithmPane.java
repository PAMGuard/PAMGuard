package bearinglocaliser.dialog;

import java.util.List;

import javax.swing.SwingUtilities;

import PamController.SettingsPane;
import PamUtils.PamUtils;
import PamView.GroupedSourceParameters;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
import bearinglocaliser.algorithms.BearingAlgorithmParams;
import bearinglocaliser.algorithms.BearingAlgorithmProvider;
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
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;

public class BearLocAlgorithmPane extends SettingsPane<BearingLocaliserParams> {

	private PamBorderPane mainPane;
	private GridPane algoPane;
	private BearingLocaliserControl bfLocControl;
	private ChoiceBox<String>[] algoSelection;
	private BearingLocSettingsPane bfLocSettingsPane;
	
	public BearLocAlgorithmPane(Object window, BearingLocaliserControl bfLocControl, BearingLocSettingsPane bfLocSettingsPane) {
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
	public BearingLocaliserParams getParams(BearingLocaliserParams params) {
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
			params.addAlgorithmName(algoName);
			BearingAlgorithmParams groupParams = params.getAlgorithmParms(groupList[i], groupChanMap, algoName);
			if (groupParams == null) {
				// algo params have not been set - need to issue a warning !
//				BeamAlgorithmProvider algoProvider = bfLocControl.findAlgorithmByName(algoName);
//				if (algoProvider )
			}
			
		}
		
		return params;
	}

	@Override
	public void setParams(BearingLocaliserParams params) {
		buildPanel(params.getRawOrFFTSourceParameters());
		int nGroup = GroupedSourcePaneFX.countChannelGroups(params.getChannelBitmap(), params.getChannelGroups());
		int n = Math.min(nGroup, algoSelection.length);
		int groupMap = GroupedSourcePaneFX.getGroupMap(params.getChannelBitmap(), params.getChannelGroups());
		int[] groupList = PamUtils.getChannelArray(groupMap);
		for (int i = 0; i < n; i++) {
			int groupChanMap = GroupedSourcePaneFX.getGroupChannels(groupList[i], params.getChannelBitmap(), params.getChannelGroups());
			String algoName = params.getAlgorithmName(i);
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
	public synchronized void changeChannelGrouping(BearingLocaliserParams bfLocParams) {
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
		List<BearingAlgorithmProvider> allAlgorithms = bfLocControl.getAlgorithmList();
		for (int i = 0; i < nGroup; i++) {
			iRow++;
			Label idLab;
			algoPane.add(idLab = new Label(String.format("%d", i)), 0, iRow);
			GridPane.setHalignment(idLab, HPos.CENTER);
			algoSelection[i] = new ChoiceBox<>();
			for (BearingAlgorithmProvider anAlgo:allAlgorithms) {
				algoSelection[i].getItems().add(anAlgo.getStaticProperties().getName());
			}
			algoPane.add(algoSelection[i], 2, iRow);
			
			int chanList = GroupedSourcePaneFX.getGroupChannels(i, sourceParameters.getChanOrSeqBitmap(), sourceParameters.getChannelGroups());
			int nChan = PamUtils.getNumChannels(chanList);
			Label chanLab;
			algoPane.add(chanLab = new Label(String.format("%d", nChan)), 1, iRow);
			GridPane.setHalignment(chanLab, HPos.CENTER);
			chanLab.setTooltip(new Tooltip("Channels [" + PamUtils.getChannelList(chanList) + "]"));

//			settingsButtons[i] = new Button("",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
			settingsButtons[i] = new Button("",PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
			algoPane.add(settingsButtons[i], 3, iRow);
			
			//make sure the combo box and button are the same size. 
			algoSelection[i].prefHeightProperty().bind(settingsButtons[i].heightProperty());
			
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
		BearingLocaliserParams params = bfLocSettingsPane.getCurrentParms();
		int nGroup = GroupedSourcePaneFX.countChannelGroups(params.getChannelBitmap(), params.getChannelGroups());
		int n = Math.min(nGroup, algoSelection.length);
		int groupMap = GroupedSourcePaneFX.getGroupMap(params.getChannelBitmap(), params.getChannelGroups());
		int[] groupList = PamUtils.getChannelArray(groupMap);
		int groupChanMap = GroupedSourcePaneFX.getGroupChannels(iGroup, params.getChannelBitmap(), params.getChannelGroups());
		String selectedName = algoSelection[iGroup].getSelectionModel().getSelectedItem();
		if (selectedName == null) {
			return;
		}
		BearingAlgorithmParams groupParams = params.getAlgorithmParms(groupList[iGroup], groupChanMap, selectedName);
		BearingAlgorithmProvider curralgoProvider = bfLocControl.findAlgorithmByName(selectedName);
		if (groupParams == null) {
			if (curralgoProvider == null) {
				return;
			}
			groupParams = curralgoProvider.createNewParams(groupList[iGroup], groupChanMap);
		}
		
		BearingAlgorithmParams newParams = curralgoProvider.showConfigDialog(getAWTWindow(), params, groupParams);
//		SettingsPane<BearingAlgorithmParams> theSettingsPane = 
//				(SettingsPane<BearingAlgorithmParams>) curralgoProvider.getSettingsPane(params, groupParams);
//		if (theSettingsPane == null) {
//			System.out.println("No available settings pane for bearing algorithm " + curralgoProvider.getStaticProperties().getName());
//			return;
//		}
//
//		PamDialogFX2AWT<BearingAlgorithmParams> algDialog = new PamDialogFX2AWT<BearingAlgorithmParams>(getAWTWindow(), theSettingsPane, false);
//		BearingAlgorithmParams newParams = algDialog.showDialog(groupParams);
		if (newParams instanceof BearingAlgorithmParams) {
			params.setAlgorithmParams(selectedName, groupList[iGroup], groupChanMap, (BearingAlgorithmParams) newParams);
		}
	}


}
