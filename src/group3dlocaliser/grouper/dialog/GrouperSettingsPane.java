package group3dlocaliser.grouper.dialog;


import PamController.SettingsPane;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import group3dlocaliser.grouper.DetectionGrouperParams;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamLabel;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;
import pamViewFX.fxNodes.utilityPanes.GroupedChannelBox;

public class GrouperSettingsPane extends SettingsPane<DetectionGrouperParams>{


	private static final double LABELED_WIDTH = 40;

	private PamBorderPane mainPane = new PamBorderPane();
		
	private ChoiceBox<String> groupOptions;
	
	private TextField maxDets, minDets;
	
	private RadioButton requireAll, requireSome;
	
	private TextField requiredN;
	
	private DataSelector currentDataSelector;

	private Button dataSelButton;

	private GroupedChannelBox checkGroupBox;
	
	public GrouperSettingsPane(Object ownerWindow, String borderTitle) {
		super(ownerWindow);
		
//		PamBorderPane setPane = new PamBorderPane();
		groupOptions = new ChoiceBox();
		groupOptions.setMaxWidth(Double.MAX_VALUE);
		groupOptions.getItems().add("Return all possible combinations");
		groupOptions.getItems().add("Return only the first combination");
//		setPane.setTop(groupOptions);
		
		int x = 0, y = 0;
		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		gridPane.add(new Label("Min Detections "), x, y);
		gridPane.add(minDets = new TextField("123"), ++x, y);
		minDets.setPrefColumnCount(3);

		gridPane.add(new Label("Max Detections "), x=0, ++y);
		gridPane.add(maxDets = new TextField("123"), ++x, y);
		maxDets.setPrefColumnCount(3);
		
		
		minDets.setMaxWidth(LABELED_WIDTH);
		maxDets.setMaxWidth(LABELED_WIDTH);

		gridPane.add(new Label("Requires channels "), x=0, ++y);
		checkGroupBox = new GroupedChannelBox(); 
		checkGroupBox.setTooltip(new Tooltip("Select channels that are required to loclaise. \n"
				+ "No selected channels means any combination of channels can be used."));
		
		PamGridPane.setColumnSpan(checkGroupBox, 2);
		PamGridPane.setColumnSpan(minDets, 1);
		PamGridPane.setColumnSpan(maxDets, 1);

		gridPane.add(checkGroupBox, ++x, y);
				
//		dataSelButton = new Button("",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS,Color.WHITE, PamGuiManagerFX.iconSize));
		dataSelButton = new Button("",PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));

		PamGridPane selPane = gridPane; //new PamGridPane();
		selPane.add(new PamLabel("Data selection ", Pos.CENTER_LEFT), x=0, ++y);
		selPane.add(dataSelButton, ++x, +y);
		dataSelButton.setMaxWidth(LABELED_WIDTH);
		selPane.add(requireAll = new RadioButton("Require all detections pass data selection"), x=0, ++y, 4, 1);
		selPane.add(requireSome = new RadioButton("Require minimum of "), x=0, ++y, 2, 1);
		x+=2;
		selPane.add(requiredN = new TextField(), x, y);
		selPane.add(new Label(" pass data selection"), ++x, y);
		requiredN.setPrefColumnCount(2);
		
		requireAll.setTooltip(new Tooltip("Select if all detections within a combination must pass the data selector"));
		requiredN.setTooltip(new Tooltip("Select if at least N detections within a combination must pass the data selector"));

		ToggleGroup group = new ToggleGroup();
		requireAll.setToggleGroup(group);
		requireSome.setToggleGroup(group);
		dataSelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dataSelectOptions();
			}
		});
		requireAll.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				requireOptions();
			}
		});
		requireSome.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				requireOptions();
			}
		});
		
		VBox vBox = new VBox(groupOptions, gridPane);
		vBox.setSpacing(5);
		
//		setPane.setBottom(gridPane);
		if (borderTitle == null) {
			mainPane.setCenter(vBox);
		}
		else {
			mainPane.setCenter(new PamTitledBorderPane(borderTitle, vBox));
		}
		
		 enableControls();
		
	}
	
	protected void requireOptions() {
		enableControls();
	}

	protected void dataSelectOptions() {
		if (currentDataSelector == null) {
			return;
		}
		currentDataSelector.showSelectDialog(getAWTWindow());
	}
	
	private void enableControls() {
		boolean rAll = requireAll.isSelected();
		requiredN.setDisable(rAll);		
	}

	public void setDataSelector(DataSelector dataSelector) {
		currentDataSelector = dataSelector;
		boolean d = dataSelector == null;
		requireAll.setDisable(d);
		requireSome.setDisable(d);
		requiredN.setDisable(d);
		dataSelButton.setDisable(d);
	}
	
	public void setDataSelector(PamDataBlock pamDataBlock, String selectorName) {
		if (pamDataBlock == null) {
			setDataSelector(null);
			return;
		}
		DataSelector ds = pamDataBlock.getDataSelector(selectorName, false);
		setDataSelector(ds);
	}

	@Override
	public DetectionGrouperParams getParams(DetectionGrouperParams currParams) {
		currParams.groupingChoice = groupOptions.getSelectionModel().getSelectedIndex();
		try {
			currParams.minSubGroups = Integer.valueOf(minDets.getText());
			currParams.maxPerGroup = Integer.valueOf(maxDets.getText());
		}
		catch (NumberFormatException e) {
			SwingFXDialogWarning.showWarning(this.getOwnerWindow(), "Grouping parameters", "Invalid minimum or maximum detections");
			return null;
		}
		
		if (requireAll.isSelected()) {
			currParams.dataSelectOption = DetectionGrouperParams.DATA_SELECT_ALL;
		}
		else {
			currParams.dataSelectOption = DetectionGrouperParams.DATA_SELECT_MIN_N;
			try {
				currParams.dataSelectMinimum = Integer.valueOf(requiredN.getText());
			}
			catch (NumberFormatException e) {
				SwingFXDialogWarning.showWarning(getOwnerWindow(), "Data Selection", "Invalid minimum number of detections");
				return null;
			}
		}
		
		return currParams;
	}

	@Override
	public void setParams(DetectionGrouperParams params) {
						
		
		groupOptions.getSelectionModel().select(params.groupingChoice);
		minDets.setText(String.format("%d", params.minSubGroups));
		maxDets.setText(String.format("%d", params.maxPerGroup));
		
		requireAll.setSelected(params.dataSelectOption == DetectionGrouperParams.DATA_SELECT_ALL);
		requireSome.setSelected(params.dataSelectOption == DetectionGrouperParams.DATA_SELECT_MIN_N);
		requiredN.setText(String.format("%d", params.dataSelectMinimum));
		
		enableControls();
	}

	@Override
	public String getName() {
		return "Grouping Options";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	private void populateChannelBox(GroupedSourceParameters source) {
		checkGroupBox.setSource(source);
	}

	/**
	 * Called whenever there is a new source group. 
	 * @param source
	 */
	public void newSourceGroup(GroupedSourceParameters source) {
		populateChannelBox(source); 
	}


}
