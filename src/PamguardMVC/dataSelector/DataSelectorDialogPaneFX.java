package PamguardMVC.dataSelector;

import org.controlsfx.control.SegmentedButton;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;


/**
 * Dialog panel to wrap around a standard dialog panel from a data selector. 
 * This adds a wrapper the data selector which enables or disables it based on 
 * whether it has been selected or not. 
 * @author Jamie Macaulay
 *
 */
public class DataSelectorDialogPaneFX extends DynamicSettingsPane<Boolean> {

	private static final double PREF_TOGGLE_WIDTH = 60;

	private DataSelector dataSelector;
	private DynamicSettingsPane<Boolean> innerPanel;
	private int setIndex;

	private PamVBox dsPane;
	private ToggleGroup buttonGroup;
	private ToggleButton andButton, orButton, disableButton;
	private HBox buttonPane;
	private Node contentPane;

	/**
	 * Create the DataSelectorDialogPaneFX
	 * @param dataSelector
	 * @param innerPanel
	 * @param setIndex
	 */
	public DataSelectorDialogPaneFX(DataSelector dataSelector,  DynamicSettingsPane<Boolean> innerPanel, int setIndex) {
		super(null);
		this.dataSelector = dataSelector;
		this.innerPanel = innerPanel;
		this.setIndex = setIndex;

		dsPane = new PamVBox(); // Use VBox for vertical layout
		dsPane.setSpacing(5);

		contentPane = innerPanel.getContentNode();

		//	        Border exBorder = innerComponent.getBorder();
		//	        
		//	        if (exBorder instanceof TitledBorder) {
		//	            innerComponent.setBorder(null);
		//	            // Set a lower bevel border if desired:
		//	            // innerComponent.setBorder(new BevelBorder(BevelBorder.LOWERED));
		//	            dsPane.setBorder(exBorder);
		//	        } else {
		//	            dsPane.setBorder(new TitledBorder(dataSelector.getSelectorTitle()));
		//	        }

		Label title = new Label(innerPanel.getName()); 
		title.setTooltip(new Tooltip("Data selector: " + dataSelector.getLongSelectorName()));
		PamGuiManagerFX.titleFont2style(title);


		buttonGroup = new ToggleGroup();
		andButton = new ToggleButton(setIndex == 0 ? "Enable" : "AND");
		andButton.setPrefWidth(PREF_TOGGLE_WIDTH);

		disableButton = new ToggleButton("Skip");
		disableButton.setPrefWidth(PREF_TOGGLE_WIDTH);

		orButton = new ToggleButton("OR");
		orButton.setPrefWidth(PREF_TOGGLE_WIDTH);

		buttonGroup.getToggles().addAll(andButton, orButton, disableButton);

		SegmentedButton segmentedButton ;
		if (setIndex > 0) {
			segmentedButton = new SegmentedButton(andButton, orButton, disableButton);
		}
		else {
			segmentedButton = new SegmentedButton(andButton, disableButton);

		}


		andButton.setOnAction(event -> {
			enableComponent();
			notifySettingsListeners();
		});
		orButton.setOnAction(event ->{
			enableComponent();
			notifySettingsListeners();
		});
		disableButton.setOnAction(event ->{
			enableComponent();
			notifySettingsListeners();
		});
		
		buttonPane = new HBox(); // Use HBox for horizontal button layout
		buttonPane.setSpacing(5);
		buttonPane.setAlignment(Pos.CENTER);
		buttonPane.getChildren().addAll(segmentedButton);

		//add everything to the main pane
		if (setIndex > 0) {
			dsPane.getChildren().add(new Separator());
		}
		dsPane.getChildren().add(title);
		dsPane.getChildren().add(buttonPane); // Add button pane at top
		dsPane.getChildren().add(contentPane);
		
		
		//need to add a listener for settings changes to these panes so it can be passed to this pane 
		innerPanel.addSettingsListener(()->{
			notifySettingsListeners();
		});
		
		
		orButton.setVisible(setIndex > 0);
		if (dataSelector instanceof CompoundDataSelector || setIndex < 0) {
			buttonPane.setVisible(false);
			dsPane.setBorder(null);
		}
		enableComponent();
		//buttonPane.setTooltip(new Tooltip("Options for " + dataSelector.getLongSelectorName()));
	}


	private void enableComponent() {
		contentPane.setDisable(disableButton.isSelected());
	}

	@Override
	public void setParams(Boolean input) {
		DataSelectParams params = dataSelector.getParams();
		if (params == null) {
			return;
		}
		if (buttonPane.isVisible()) {
			andButton.setSelected(params.getCombinationFlag() == DataSelectParams.DATA_SELECT_AND);
			orButton.setSelected(params.getCombinationFlag() == DataSelectParams.DATA_SELECT_OR);
			disableButton.setSelected(params.getCombinationFlag() == DataSelectParams.DATA_SELECT_DISABLE);
		} else {
			andButton.setSelected(true);
			orButton.setSelected(false);
			disableButton.setSelected(false);
		}

		innerPanel.setParams(input);
		enableComponent();
	}

	@Override
	public Boolean getParams(Boolean a) {
		DataSelectParams params = dataSelector.getParams();

		if (disableButton.isSelected()) {
			if (params != null) {
				params.setCombinationFlag(DataSelectParams.DATA_SELECT_DISABLE);
			}
			return true;
		}

		boolean innerOk = innerPanel.getParams(a);
		if (!innerOk) {
			return false;
		}

		params = dataSelector.getParams(); // Might have been recreated

		if (params != null) {
			if (andButton.isSelected()) {
				params.setCombinationFlag(DataSelectParams.DATA_SELECT_AND);
			} else if (orButton.isSelected()) {
				params.setCombinationFlag(DataSelectParams.DATA_SELECT_OR);
			} else if (disableButton.isSelected()) {
				params.setCombinationFlag(DataSelectParams.DATA_SELECT_DISABLE);
			}
		}

		return innerOk;
	}

	// Utility method for disabling/enabling a Node (doesn't work recursively)
	public static void setEnabled(Node node, boolean enabled) {

	}


	@Override
	public String getName() {
		return "Data selector";
	}

	@Override
	public Node getContentNode() {
		return dsPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}
}
