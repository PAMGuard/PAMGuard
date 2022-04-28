package pamViewFX.fxNodes.utilityPanes;

import PamController.PamController;
import PamUtils.PamUtils;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Priority;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * The grouping part of a grouped source pane which can be used
 * separately from the entire grouped source pane. 
 * @author Doug Gillespie
 *
 */
public class GroupedChannelPaneFX {

	private boolean autoGrouping;
	
	public static final int GROUP_SINGLES = 0;
	public static final int GROUP_ALL = 1;
	public static final int GROUP_USER = 2;
	
	/**
	 * List of combo boxes allowing user to select a group for each channel
	 */
	private ComboBox[] groupList;
	
	protected CheckBox channelBoxes[];
	
	private RadioButton allSingles, allTogether, userGrouped;

	private PamGridPane channelListPane;

	private PamVBox autoGroupPane;
	
	private PamBorderPane outerPane;

	/**
	 * Holds all the nodes for grouped pane. Can be accessed to add additonal nodes 
	 */
	private PamGridPane sourcePane;

	private Button selectAll;

	private PamDataBlock sourceDataBlock;
	
	private SimpleObjectProperty<GroupedSourceParameters> observableProperty = new SimpleObjectProperty<>();

	private PamDataBlock currentDataSource;

	private boolean disableEverything;

	private boolean paramsAreSet;

	public GroupedChannelPaneFX() {
		createPanel();
		showChannels(0xFFFF);
	}
	
	public Node getContentNode() {
		return outerPane;
	}

	private class SillyPamBorderPane extends PamBorderPane {

		public SillyPamBorderPane() {
			super();
			// TODO Auto-generated constructor stub
		}
		
	}
	protected void createPanel() {
		outerPane = new SillyPamBorderPane();
		
		sourcePane=new PamGridPane();
//		sourcePane.setVgap(5);
		sourcePane.setHgap(5);
		outerPane.setCenter(sourcePane);
//		outerPane.setTop(new Label("top"));
//		outerPane.setBottom(new Label("bottom"));
		
//		Label titleLabel = new Label(getBorderTitle());
//		titleLabel.setFont(PamGuiManagerFXAWT.titleFontSize2);
//		sourcePane.add(titleLabel,0,0);

//		this.sourceList=new ComboBox<PamDataBlock>();
//		sourceList.setOnAction((action)->{
//			this.sourceChanged();
//		});
//		///make sure stretches in pane.
//		GridPane.setColumnSpan(sourceList,3);
//		sourceList.setMaxWidth(Double.MAX_VALUE);
//		
//		sourcePane.add(sourceList,0,1);
		
		//create pane to hold channels. 
		PamVBox channelPanel=new PamVBox();
		channelPanel.setSpacing(5);
		
		Label channelLabel = new Label("Channels");
//		channelLabel.setFont(PamGuiManagerFXAWT.titleFontSize2);
		sourcePane.add(channelLabel,0,2);
		
		//create radio buttons to allow user to quickly select type of grouping. 
		autoGroupPane=new PamVBox();
		autoGroupPane.setSpacing(5);
		ToggleGroup group = new ToggleGroup();
		autoGroupPane.getChildren().add(allSingles = new RadioButton("No grouping"));
		allSingles.setOnAction(new GroupAction(GROUP_SINGLES));
		allSingles.setToggleGroup(group);
		autoGroupPane.getChildren().add(allTogether = new RadioButton("One group"));
		allTogether.setOnAction(new GroupAction(GROUP_ALL));
		allTogether.setToggleGroup(group);
		autoGroupPane.getChildren().add(userGrouped = new RadioButton("User groups"));
		userGrouped.setOnAction(new GroupAction(GROUP_USER));
		userGrouped.setToggleGroup(group);
	
		
		//create channel pane
		channelBoxes =new CheckBox[PamConstants.MAX_CHANNELS];
		groupList =new ComboBox[PamConstants.MAX_CHANNELS];
	
		channelListPane=new PamGridPane();
		channelListPane.setHgap(10);
		channelListPane.setVgap(5);
//		channelListPane.setBackground(new Background(new BackgroundFill(Color.CYAN, null, null)));
		channelPanel.getChildren().add(channelListPane);
		
		selectAll =new Button("All");
		selectAll.setOnAction((action)->{
//			if (selectAll.isSelected()) {
				selectAllChannels();
//			}
//			else {
//				selectNoChannels();
//			}
			enableGroupBoxes();
			notifyObservers();
		});
		
		//create a list of channels and combo boxes for groups. 
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){
			channelBoxes[i] = new CheckBox("Channel " + i);
			final int n=i;
			channelBoxes[i].setOnAction((action)->{
				selectionChanged(n);
			});
			groupList[i]=new ComboBox<Integer>();
			for (int j = 0; j < PamConstants.MAX_CHANNELS; j++) {
				groupList[i].getItems().add(j); 
			}
			groupList[i].setOnAction((action)->{
				notifyObservers();
			});
		}

		//align channels so they sit in middle of parent pane.
		PamHBox.setHgrow(channelPanel, Priority.ALWAYS);

		//pane to hold radio buttons, channle check boxes and group combo boxes. 
		PamHBox channelGroupPane=new PamHBox();
		channelGroupPane.setSpacing(15);
		channelGroupPane.getChildren().addAll(autoGroupPane, channelPanel);
		
		sourcePane.add(channelGroupPane,0,3);
		
		
	}
	
	
	private void selectionChanged(int n) {
		enableGroupBoxes();
		notifyObservers();
	}

	private void selectNoChannels() {
		for (int i = 0; i < channelBoxes.length; i++) {
			channelBoxes[i].setSelected(false);
		}
	}

	private void selectAllChannels() {
		int availChannels = 0;
		if (sourceDataBlock != null) {
//			availChannels = sourceDataBlock.getChannelMap();
			availChannels = sourceDataBlock.getSequenceMap();
		}
		for (int i = 0; i < channelBoxes.length; i++) {
			channelBoxes[i].setSelected((1<<i & availChannels) != 0);
		}
		
	}

	protected void showChannels(int channels) {
		
		//remove all channels from vertical box pane. 
		channelListPane.getChildren().removeAll(channelListPane.getChildren());
		channelListPane.getChildren().remove(selectAll);		

		
		channelListPane.add(selectAll,0,0);
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, channelBoxes.length); i++) {
			if ((channels & 1<<i) != 0){
				channelListPane.add(channelBoxes[i], 0, i+1);
				channelListPane.add(groupList[i], 1, i+1);
			}; 
		} 
		channelListPane.requestLayout();
//		rePackOwner(channels);
		
		
//		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
//			channelBoxes[i].setVisible((channels & 1<<i) != 0);
//			groupList[i].setVisible((channels & 1<<i) != 0);
//			groupList[i].removeAllItems();
//			for (int j = 0; j < PamConstants.MAX_CHANNELS; j++) {
//				groupList[i].addItem(j);
//			}
//		}
//
//		rePackOwner(channels);
	}


	public void setChannelGroups(int[] channelGroups) {
		if (channelGroups == null) return;
		for (int i = 0; i < Math.min(channelGroups.length, PamConstants.MAX_CHANNELS); i++) {
			groupList[i].getSelectionModel().select(channelGroups[i]);
		}
	}

	public boolean isAutoGrouping() {
		return autoGrouping;
	}

	public void setAutoGrouping(boolean autoGrouping) {
		this.autoGrouping = autoGrouping;
		autoGroupPane.setVisible(autoGrouping);
	}
	
	public void setGrouping(int groupType) {
		allSingles.setSelected(groupType == GROUP_SINGLES);
		allTogether.setSelected(groupType == GROUP_ALL);
		userGrouped.setSelected(groupType == GROUP_USER);
		for (int i = 0; i < groupList.length; i++) {
			if (groupType == GROUP_ALL) {
				groupList[i].getSelectionModel().select(0);
			}
			else if (groupType == GROUP_SINGLES) {
				groupList[i].getSelectionModel().select(i);
			}
		}
		enableGroupBoxes();
	}

	public void enableGroupBoxes() {
		int groupType = getGrouping();
		for (int i = 0; i < groupList.length; i++) {
			groupList[i].setDisable(groupType != GROUP_USER || !channelBoxes[i].isSelected() || disableEverything);
		}
	}
	
	public void disableAll(boolean disable) {
		this.disableEverything = disable;
		for (int i = 0; i < groupList.length; i++) {
			groupList[i].setDisable(disable);
			channelBoxes[i].setDisable(disable);
		}
		allSingles.setDisable(disable);
		allTogether.setDisable(disable);
		userGrouped.setDisable(disable);
		selectAll.setDisable(disable);
	}
	
	public int getGrouping() {
		if (allSingles.isSelected()) return GROUP_SINGLES;
		if (allTogether.isSelected()) return GROUP_ALL;
		if (userGrouped.isSelected()) return GROUP_USER;
		return -1;
	}
	
	public int[] getChannelGroups() {
		int[] groups = new int[PamConstants.MAX_CHANNELS];
		String str;
		for (int i = 0; i < Math.min(groupList.length, PamConstants.MAX_CHANNELS); i++) {
//			str = groupList[i].getSelectedItem().toString();
//			groups[i] = str.charAt(0);
			groups[i] = groupList[i].getSelectionModel().getSelectedIndex();
		}
		return groups;
	}
	
	public void setParams(GroupedSourceParameters params) {
		
		/**
		 * First try to find the source datablock and from there set up 
		 * the panel.
		 */		
		
		PamController pamController = PamController.getInstance();
		sourceDataBlock = pamController.getDataBlockByLongName(params.getDataSource());
		setSourceDataBlock(sourceDataBlock);

		if (sourceDataBlock != null) {
//			showChannels(sourceDataBlock.getChannelMap());
			showChannels(sourceDataBlock.getSequenceMap());
		}
		setGrouping(params.getGroupingType());
		setChannelGroups(params.getChannelGroups());
		if (sourceDataBlock != null) {
			setChannelList(params.getChanOrSeqBitmap());
		}
		paramsAreSet = true;
		notifyObservers();
	}
	
	/**
	 * Setting the list of used channels. 
	 * @param channelBitmap
	 */
	private void setChannelList(int channelBitmap) {
		for (int i = 0; i < channelBoxes.length; i++) {
			if (channelBoxes[i] != null) {
				channelBoxes[i].setSelected((1<<i & channelBitmap) != 0);
			}
		}
	}
	
	/**
	 * 
	 * @return list of selected channels. 
	 */
	private int getChannelList() {
		int channelList = 0;
		for (int i = 0; i < channelBoxes.length; i++) {
			if (channelBoxes[i] != null) {
				if (channelBoxes[i].isSelected()) {
					channelList |= 1<<i;
				}
			}
		}
		return channelList;
	}

	private void setSourceDataBlock(PamDataBlock sourceDataBlock) {
		this.currentDataSource = sourceDataBlock;
		if (sourceDataBlock != null) {
//			showChannels(sourceDataBlock.getChannelMap());
			showChannels(sourceDataBlock.getSequenceMap());
		}
	}

	public boolean getParams(GroupedSourceParameters params) {
		if (params == null) {
			return false;
		}

		params.setGroupingType(getGrouping());
		params.setChannelGroups(getChannelGroups());
		params.setChanOrSeqBitmap(getChannelList());
		
		return true;
	}


	private class GroupAction implements EventHandler<ActionEvent> {

		private int groupType;
		
		public GroupAction(int groupType) {
			super();
			this.groupType = groupType;
		}

		@Override
		public void handle(ActionEvent event) {
			setGrouping(groupType);
			notifyObservers();
		}
		
	}
	
	private void notifyObservers() {
		if (!paramsAreSet) return;
		GroupedSourceParameters gsp = new GroupedSourceParameters();
		if (currentDataSource != null) {
			gsp.setDataSource(currentDataSource.getLongDataName());
		}
		gsp.setChannelGroups(getChannelGroups());
		gsp.setChanOrSeqBitmap(getChannelList());
		getObservableProperty().setValue(gsp);
	}
	
	/**
	 * Creates a bitmap for the groups, the same idea as a channelmap.  The groupList array that
	 * is passed is of length [numChannels], and each index holds the group number for that
	 * channel (e.g. if channel 3 is in group 5, groupList[3]=5).  For channels that are not in
	 * a group, the groupList array will hold a -1.
	 * @param channelMap the channels that are available
	 * @param groupList an array of length [numChannels] holding the group number for each channel 
	 * @return
	 */
	static public int getGroupMap(int channelMap, int[] groupList) {
		int groupMap = 0;
		if (groupList == null) return 0;
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, groupList.length); i++) {
			if ((channelMap & (1<<i)) == 0) continue;
			groupMap |= (1<<groupList[i]);
		}
		return groupMap;
	}
	
	static public int countChannelGroups(int channelMap, int[] groupList) {
		if (groupList == null) return 0;
		int groupMap = getGroupMap(channelMap, groupList);
		return PamUtils.getNumChannels(groupMap);
	}
	
	static public int getGroupChannels(int group, int channelMap, int[] groupList) {
		// group is the n'th group - if the groups that got used started at 1, then the
		// 0th group would be all those that had group set to 1 !
		int groupMap = getGroupMap(channelMap, groupList);
		int groupChannels = 0;
		int channelNumber = PamUtils.getNthChannel(group, groupMap);
		for (int i = 0; i < groupList.length; i++) {
			if ((channelMap & (1<<i)) == 0) continue;
			if (groupList[i] == channelNumber) {
				groupChannels |= (1<<i);
			}
		}
		
		return groupChannels;
	}
	
	static public String getGroupList(int group, int channelMap, int[] groupList) {
		String str;
		int groupChannels = getGroupChannels(group, channelMap, groupList);
		if (groupChannels == 0) return null;
		str = String.format("%d", PamUtils.getNthChannel(0, groupChannels));
		for (int i = 1; i < PamUtils.getNumChannels(groupChannels); i++) {
			str += String.format(", %d", PamUtils.getNthChannel(i, groupChannels));
		}
		return str;
	}
	
	static public int getGroupIndex(int groupMap, int channelMap, int[] groupList) {
		for (int i = 0; i < groupList.length; i++) {
			if (groupMap == getGroupChannels(i, channelMap, groupList)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Get the grid pane which all main nodes. 
	 * @return the main source pane. 
	 */
	public PamGridPane getSourcePane() {
		return sourcePane;
	}
	
	/**
	 * Get the channel list pane- this is the grid pane in which channels and combo boxes sit. 
	 * @return channel list pane
	 */
	public PamGridPane getChannelListPane() {
		return channelListPane;
	}

	/**
	 * @return the observableProperty
	 */
	public SimpleObjectProperty<GroupedSourceParameters> getObservableProperty() {
		return observableProperty;
	}
	
}
