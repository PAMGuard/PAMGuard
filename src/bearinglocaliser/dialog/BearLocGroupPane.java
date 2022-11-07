package bearinglocaliser.dialog;

import PamController.SettingsPane;
import PamView.GroupedSourceParameters;
import beamformer.localiser.BFLocaliserParams;
import beamformer.localiser.BeamFormLocaliserControl;
import beamformer.localiser.dialog.BFLocSettingsPane2;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.utilityPanes.GroupedChannelPaneFX;

public class BearLocGroupPane extends SettingsPane<BearingLocaliserParams> {

	private BearingLocaliserControl bfLocControl;

	private GroupedChannelPaneFX groupedChannelPanel;

	private PamBorderPane mainPane;

	private BearingLocSettingsPane bfLocSettingsPane;
	
	private CheckBox doAllGroups;

	public BearLocGroupPane(Object window, BearingLocaliserControl bfLocControl, BearingLocSettingsPane bfLocSettingsPane) {
		super(window);
		this.bfLocSettingsPane = bfLocSettingsPane;
		this.bfLocControl = bfLocControl;
		groupedChannelPanel = new GroupedChannelPaneFX();
		PamTitledBorderPane groupPane = new PamTitledBorderPane("Channel grouping", groupedChannelPanel.getContentNode());
		BorderPane.setMargin(groupPane, new Insets(0, 0, 15, 0));

		mainPane = new PamBorderPane(groupPane);
		mainPane.setPadding(new Insets(10, 5, 10, 5));
		GridPane optionsPane = new GridPane();
		mainPane.setBottom(new PamTitledBorderPane("Group options", optionsPane));
		doAllGroups = new CheckBox("Always localise all channel groups");
		String txt = "When a detection or spectrogram mark arrives associated with one channel group, beam form on all groups. \n" +
		"This is most useful with manual spectrogram selection since detectors can be configured to detect simultaneously on all groups";
		doAllGroups.setTooltip(new Tooltip(txt));
		optionsPane.add(doAllGroups, 0, 0);
		groupedChannelPanel.getObservableProperty().addListener(new ChangeListener<GroupedSourceParameters>() {
			@Override
			public void changed(ObservableValue<? extends GroupedSourceParameters> observable,
					GroupedSourceParameters oldValue, GroupedSourceParameters newValue) {
				groupingChanged(newValue);
			}
		});
	}

	protected void groupingChanged(GroupedSourceParameters groupedSourceParameters) {
		int nGroups = groupedSourceParameters.countChannelGroups();
		doAllGroups.setDisable(nGroups < 2);
		if (nGroups < 2) {
			doAllGroups.setSelected(false);
		}
	}

	@Override
	public BearingLocaliserParams getParams(BearingLocaliserParams currentParams) {
		boolean ok = groupedChannelPanel.getParams(currentParams.getRawOrFFTSourceParameters());
		currentParams.doAllGroups = doAllGroups.isSelected();
		return (ok ? currentParams : null);
	}

	@Override
	public void setParams(BearingLocaliserParams params) {
		System.out.println("Bearing Localiser: params: " + params.getChannelBitmap()); 
		groupedChannelPanel.setParams(params.getRawOrFFTSourceParameters());
		doAllGroups.setSelected(params.doAllGroups);
	}

	@Override
	public String getName() {
		return "Channel Groups";
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
	 * @return the groupedChannelPanel
	 */
	public GroupedChannelPaneFX getGroupedChannelPanel() {
		return groupedChannelPanel;
	}


}
