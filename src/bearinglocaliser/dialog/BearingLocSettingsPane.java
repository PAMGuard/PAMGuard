package bearinglocaliser.dialog;

import PamController.SettingsPane;
import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamDataBlock;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.utilityPanes.BorderPaneFX2AWT;


public class BearingLocSettingsPane extends SettingsPane<BearingLocaliserParams> {

	private VBox mainPane;
	private TabPane tabPane;
	private BearingLocaliserControl bearingLocaliserControl;
	private BearingLocaliserParams currentParams;
	private BearLocSourcePane sourcePane;
	private BearLocGroupPane groupPane;
	private BearLocAlgorithmPane algoPane;
	
	public BearingLocSettingsPane(Object ownerWindow, BearingLocaliserControl bearingLocaliserControl) {
		super(ownerWindow);
		this.bearingLocaliserControl = bearingLocaliserControl;
		sourcePane = new BearLocSourcePane(ownerWindow, bearingLocaliserControl, this);
		groupPane = new BearLocGroupPane(ownerWindow, bearingLocaliserControl, this);
		algoPane = new BearLocAlgorithmPane(ownerWindow, bearingLocaliserControl, this);

		mainPane = new VBox();
		tabPane = new TabPane(); //no need to use PAMTabPane here as there are no detechable tabs 
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		
		mainPane.getChildren().add(tabPane);
		tabPane.getTabs().add(new Tab(sourcePane.getName(), sourcePane.getContentNode()));
		tabPane.getTabs().add(new Tab(groupPane.getName(), groupPane.getContentNode()));
		tabPane.getTabs().add(new Tab(algoPane.getName(), algoPane.getContentNode()));
		
		mainPane.setPadding(new Insets(5,5,5,5)); 

		sourcePane.getBeamDataSourcePane().addSelectionListener(new ChangeListener<PamDataBlock>() {
			@Override
			public void changed(ObservableValue<? extends PamDataBlock> observable, PamDataBlock oldValue,
					PamDataBlock newValue) {
				changeBeamSource(newValue);
			}
		});
		groupPane.getGroupedChannelPanel().getObservableProperty().addListener(new ChangeListener<GroupedSourceParameters>() {
			@Override
			public void changed(ObservableValue<? extends GroupedSourceParameters> observable,
					GroupedSourceParameters oldValue, GroupedSourceParameters newValue) {
				currentParams.setRawOrFFTSourceParameters(newValue);
				changeChannelGrouping(currentParams);
			}
		});
	}

	protected void changeChannelGrouping(BearingLocaliserParams currentParams) {
		algoPane.changeChannelGrouping(currentParams);
	}

	protected void changeBeamSource(PamDataBlock newValue) {
		if (newValue == null || currentParams == null) {
			return;
		}
		currentParams.setDataSource(newValue.getLongDataName());
		boolean groupedSource = (newValue instanceof GroupedDataSource);
		if (groupedSource) {
			GroupedSourceParameters gsp = ((GroupedDataSource) newValue).getGroupSourceParameters().clone();
			gsp.setDataSource(currentParams.getDataSource());
			currentParams.setRawOrFFTSourceParameters(gsp);
		}
		groupPane.getGroupedChannelPanel().setParams(currentParams.getRawOrFFTSourceParameters());
		groupPane.getGroupedChannelPanel().enableGroupBoxes();
		groupPane.getGroupedChannelPanel().disableAll(groupedSource && false);
		
		// now need to tell this thing to repack ...
		BorderPaneFX2AWT.repackSwingDialog(mainPane);
		
	}

	@Override
	public BearingLocaliserParams getParams(BearingLocaliserParams currParams) {
		BearingLocaliserParams params = currParams;
		params = sourcePane.getParams(params);
		if (params == null) return null;
		params = groupPane.getParams(params);
		if (params == null) return null;
		params = algoPane.getParams(params);
		
		return params;
	}

	@Override
	public void setParams(BearingLocaliserParams input) {
		currentParams = input;
		sourcePane.setParams(input);
		groupPane.setParams(input);
		algoPane.setParams(input);
	}

	@Override
	public String getName() {
		return bearingLocaliserControl.getUnitName();
	}
	
	/* (non-Javadoc)
	 * @see PamController.SettingsPane#repackContents()
	 */
	@Override
	public void repackContents() {
		//tabPane.repackTabs();
	}
	
	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	public BearingLocaliserParams getCurrentParms() {
		return currentParams;
	}

	@Override
	public String getHelpPoint() {
		return bearingLocaliserControl.getHelpPoint();
	}


}
