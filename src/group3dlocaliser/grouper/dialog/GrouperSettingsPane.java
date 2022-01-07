package group3dlocaliser.grouper.dialog;

import PamController.SettingsPane;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamLabel;
import pamViewFX.fxNodes.PamTitledBorderPane;
import pamViewFX.fxNodes.pamDialogFX.SwingFXDialogWarning;

public class GrouperSettingsPane extends SettingsPane<DetectionGrouperParams>{


	private PamBorderPane mainPane = new PamBorderPane();
		
	private ChoiceBox<String> groupOptions;
	
	private TextField maxDets, minDets;
	
	private RadioButton requireAll, requireSome;
	
	private TextField requiredN;
	
	private DataSelector currentDataSelector;

	private Button dataSelButton;
	
	public GrouperSettingsPane(Object ownerWindow, String borderTitle) {
		super(ownerWindow);
		
//		PamBorderPane setPane = new PamBorderPane();
		groupOptions = new ChoiceBox();
		groupOptions.getItems().add("Return all possible combinations");
		groupOptions.getItems().add("Return only the first combination");
//		setPane.setTop(groupOptions);
		
		int x = 0, y = 0;
		PamGridPane gridPane = new PamGridPane();
		gridPane.add(new Label("Min Detections "), x, y);
		gridPane.add(minDets = new TextField("123"), ++x, y);
		gridPane.add(new Label("Max Detections "), x=0, ++y);
		gridPane.add(maxDets = new TextField("123"), ++x, y);
		minDets.setPrefColumnCount(3);
		maxDets.setPrefColumnCount(3);
		
//		dataSelButton = new Button("",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS,Color.WHITE, PamGuiManagerFX.iconSize));
		dataSelButton = new Button("",PamGlyphDude.createPamIcon("mdi2c-cog",Color.WHITE, PamGuiManagerFX.iconSize));
		PamGridPane selPane = gridPane; //new PamGridPane();
		selPane.add(new PamLabel("Data selection ", Pos.CENTER_RIGHT), x=0, ++y);
		selPane.add(dataSelButton, ++x, +y);
		selPane.add(requireAll = new RadioButton("Require all detections pass data selection"), x=0, ++y, 4, 1);
		selPane.add(requireSome = new RadioButton("Require minimum of "), x=0, ++y, 2, 1);
		x+=2;
		selPane.add(requiredN = new TextField(), x, y);
		selPane.add(new Label(" pass data selection"), ++x, y);
		requiredN.setPrefColumnCount(2);
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
		
		
//		setPane.setBottom(gridPane);
		if (borderTitle == null) {
			mainPane.setCenter(vBox);
		}
		else {
			mainPane.setCenter(new PamTitledBorderPane(borderTitle, vBox));
		}
		
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


}
