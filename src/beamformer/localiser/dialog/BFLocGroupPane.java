package beamformer.localiser.dialog;

import PamController.SettingsPane;
import PamView.GroupedSourceParameters;
import beamformer.localiser.BFLocaliserParams;
import beamformer.localiser.BeamFormLocaliserControl;
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

public class BFLocGroupPane extends SettingsPane<BFLocaliserParams> {

	private BeamFormLocaliserControl bfLocControl;

	private GroupedChannelPaneFX groupedChannelPanel;

	private PamBorderPane mainPane;

	private BFLocSettingsPane2 bfLocSettingsPane;
	
	private CheckBox doAllGroups;

	public BFLocGroupPane(Object window, BeamFormLocaliserControl bfLocControl, BFLocSettingsPane2 bfLocSettingsPane) {
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
		doAllGroups = new CheckBox("Always beam form all channel groups");
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
	public BFLocaliserParams getParams(BFLocaliserParams currentParams) {
		boolean ok = groupedChannelPanel.getParams(currentParams.getGroupedSourceParameters());
		currentParams.doAllGroups = doAllGroups.isSelected();
		return (ok ? currentParams : null);
	}

	@Override
	public void setParams(BFLocaliserParams params) {
		groupedChannelPanel.setParams(params.getGroupedSourceParameters());
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
