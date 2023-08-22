package group3dlocaliser.grouper.dialog;


import PamController.SettingsPane;
import PamUtils.PamArrayUtils;
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

	private DetectionGrouperParams currentParams;
	
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
	
		
		//need to disable if no data selector but do not enable unless requireSome selected
		if (!d) enableControls();
		else requiredN.setDisable(d);
		
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
		
		currParams.primaryDetGroup = getGroupParams(); 
		
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
		
		setGroupParams(params.primaryDetGroup); 
		
		enableControls();
	}
	
	/**
	 * Set which primary groups are ticked. 
	 * @param groups - the groups. 
	 */
	private int[] getGroupParams(){
	
		//the check box should have already had the correct source params set. 
		
		int n = 0; 
		for (int i=0; i<checkGroupBox.getItems().size(); i++) {
			if (checkGroupBox.getItemBooleanProperty(i).get()) n++;
		}
		
		if (n==0) return null; 
		
		int[] selectedGroups = new int[n]; 
		
		n=0;
		for (int i=0; i<checkGroupBox.getItems().size(); i++) {
			if (checkGroupBox.getItemBooleanProperty(i).get()) {
				selectedGroups[n] = checkGroupBox.getGroupedParams().getGroupChannels(i); 	
			
				n++; 
			}
		}
		
//		System.out.println("GETPARAMS: SELECTED GROUPS:"); 
//		PamArrayUtils.printArray(selectedGroups);
		
		return selectedGroups; 
	}
	
	
	/**
	 * Set which primary groups are ticked. 
	 * @param groups - the groups. 
	 */
	private void setGroupParams(int[] groups){
		
//		System.out.println("SETPARAMS: SELECTED GROUPS:"); 
//		PamArrayUtils.printArray(groups);
//	
		//the check box should have already had the correct source params set. 
		GroupedSourceParameters params = checkGroupBox.getGroupedParams(); 
		
		if (params==null) return; 
		
		for (int i=0; i<params.countChannelGroups(); i++) {

			int group = params.getGroupChannels(i);
			
//			System.out.println("CHANNEL ARRAY:"); 
//			PamArrayUtils.printArray(PamUtils.PamUtils.getChannelArray(group));
			
			checkGroupBox.getItemBooleanProperty(i).set(false);; 

			if (groups!=null) {
				for (int j=0; j<groups.length; j++) {
					if (groups[j] == group) {
						checkGroupBox.getItemBooleanProperty(i).set(true);; 
					}
				}
			}
		}
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
		int[] selected = null;
			
		//Need to preserve the selection - check whether the groups are equal and if so set the selected back again
		if (checkGroupBox.getGroupedParams()!=null && (PamArrayUtils.arrEquals(source.getChannelGroups(), checkGroupBox.getGroupedParams().getChannelGroups()))) {
			selected = getGroupParams(); 
		}
		
		checkGroupBox.setSource(source);
		
		setGroupParams(selected); 
		
	}

	/**
	 * Called whenever there is a new source group. 
	 * @param source
	 */
	public void newSourceGroup(GroupedSourceParameters source) {
		populateChannelBox(source); 
	}


}
