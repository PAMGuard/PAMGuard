package beamformer.localiser.dialog;

import PamController.SettingsPane;
import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamDataBlock;
import beamformer.localiser.BFLocaliserParams;
import beamformer.localiser.BeamFormLocaliserControl;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import pamViewFX.fxNodes.utilityPanes.BorderPaneFX2AWT;

public class BFLocSettingsPane2 extends SettingsPane<BFLocaliserParams>{
	
	private BeamFormLocaliserControl bfLocControl;
	private BFLocSourcePane locSourcePane;
	private BFLocGroupPane groupSourcePane;
	private BFLocaliserParams currentParms;
	private BFLocAlgorithmPane algoPane;
	
	private VBox mainPane;
	private TabPane tabPane;

	public BFLocSettingsPane2(Object window, BeamFormLocaliserControl bfLocControl) {
		super(window);
		this.bfLocControl = bfLocControl;
		locSourcePane = new BFLocSourcePane(window, bfLocControl, this);
//		locSourcePane.setPadding(new Insets(10));

		groupSourcePane = new BFLocGroupPane(window, bfLocControl, this);
		algoPane = new BFLocAlgorithmPane(window, bfLocControl, this);
		
		mainPane = new VBox();
		tabPane = new TabPane();
//		VBox.setVgrow(tabPane, Priority.ALWAYS);
//		tabPane.setAddTabButton(false);
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
//		tabPane.sett
		
		mainPane.getChildren().add(tabPane);
		tabPane.getTabs().add(new Tab(locSourcePane.getName(), locSourcePane.getContentNode()));
		tabPane.getTabs().add(new Tab(groupSourcePane.getName(), groupSourcePane.getContentNode()));
		tabPane.getTabs().add(new Tab(algoPane.getName(), algoPane.getContentNode()));
		tabPane.autosize();
		
//		GridPane gridPane = new GridPane();
//		gridPane.add(locSourcePane.getContentNode(), 0, 0);
//		gridPane.add(groupSourcePane.getContentNode(), 1, 0);
//		gridPane.add(algoPane.getContentNode(), 2, 0);
//		mainPane.setCenter(gridPane);
		
		locSourcePane.getBeamDataSourcePane().addSelectionListener(new ChangeListener<PamDataBlock>() {
			@Override
			public void changed(ObservableValue<? extends PamDataBlock> observable, PamDataBlock oldValue,
					PamDataBlock newValue) {
				changeBeamSource(newValue);
			}
		});
		groupSourcePane.getGroupedChannelPanel().getObservableProperty().addListener(new ChangeListener<GroupedSourceParameters>() {
			@Override
			public void changed(ObservableValue<? extends GroupedSourceParameters> observable,
					GroupedSourceParameters oldValue, GroupedSourceParameters newValue) {
				currentParms.setGroupedSourceParameters(newValue);
				changeChannelGrouping(currentParms);
			}
		});
	}
		
	/* (non-Javadoc)
	 * @see PamController.SettingsPane#repackContents()
	 */
	@Override
	public void repackContents() {
		//tabPane.repackTabs();
	}

	/**
	 * Called whenever anything changes in the channel grouping panel. 
	 * @param sourceParameters 
	 */
	protected void changeChannelGrouping(BFLocaliserParams sourceParameters) {
//		System.out.println("Channel grouping changed");
		algoPane.changeChannelGrouping(sourceParameters);
	}

	protected void changeBeamSource(PamDataBlock newValue) {
		if (newValue == null || currentParms == null) {
			return;
		}
		currentParms.setDataSource(newValue.getLongDataName());
		boolean groupedSource = (newValue instanceof GroupedDataSource);
		if (groupedSource) {
			currentParms.setGroupedSourceParameters(((GroupedDataSource) newValue).getGroupSourceParameters());
		}
		groupSourcePane.getGroupedChannelPanel().setParams(currentParms.getGroupedSourceParameters());
		groupSourcePane.getGroupedChannelPanel().enableGroupBoxes();
		groupSourcePane.getGroupedChannelPanel().disableAll(groupedSource);
		
		// now need to tell this thing to repack ...
		BorderPaneFX2AWT.repackSwingDialog(mainPane);
	}

	@Override
	public BFLocaliserParams getParams(BFLocaliserParams p) {
		BFLocaliserParams params = currentParms;
		params = locSourcePane.getParams(params);
		if (params == null) return null;
		params = groupSourcePane.getParams(params);
		if (params == null) return null;
		params = algoPane.getParams(params);
		if (params == null) return null;
		return params;
	}

	@Override
	public void setParams(BFLocaliserParams params) {
		this.currentParms = params;
		locSourcePane.setParams(params);
		groupSourcePane.setParams(params);
		algoPane.setParams(params);
	}

	@Override
	public String getName() {
		return "Beam Form Localiser";
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
	 * @return the currentParms
	 */
	public BFLocaliserParams getCurrentParms() {
		return currentParms;
	}

	/**
	 * @param currentParms the currentParms to set
	 */
	public void setCurrentParms(BFLocaliserParams currentParms) {
		this.currentParms = currentParms;
	}

}
