package pamViewFX.fxNodes.utilityPanes;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import net.synedra.validatorfx.Validator;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import PamUtils.PamUtils;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;

/**
 * Pane which shows a combo box to select a parent data block and has channel 
 * check boxes which can be grouped. 
 * @author Jamie Macaulay
 *
 */
public class GroupedSourcePaneFX extends SourcePaneFX {

	
	private boolean autoGrouping;
	
	public static final int GROUP_SINGLES = 0;
	public static final int GROUP_ALL = 1;
	public static final int GROUP_USER = 2;
	
	/**
	 * List of combo boxes allowing user to select a group for each channel
	 */
	private ComboBox[] groupList;
	
	private RadioButton allSingles, allTogether, userGrouped;

	private PamGridPane channelListPane;

	private PamVBox autoGroupPane;

	/**
	 * Holds all the nodes for grouped pane. Can be accessed to add additonal nodes 
	 */
	private PamGridPane sourcePane;

	/**
	 * Holds channels and group settings. 
	 */
	private PamVBox channelPanel;
	
	/**
	 * Validator for channels 
	 */
    private Validator validator;


	public GroupedSourcePaneFX(Class sourceType, boolean hasChannels, boolean includeSubClasses, boolean autoGrouping) {
		super(sourceType, hasChannels, includeSubClasses);
		
	}

	public GroupedSourcePaneFX(String borderTitle, Class sourceType, boolean hasChannels, boolean includeSubClasses, boolean autoGrouping) {
		super(borderTitle, sourceType, hasChannels, includeSubClasses);
	}

	@Override
	protected void createPanel() {
		
		validator = new Validator();
		
		sourcePane=new PamGridPane();
		sourcePane.setVgap(5);
		sourcePane.setHgap(5);
		
		//sourcePane.setStyle("-fx-background-color: green");

		Label titleLabel = new Label(getBorderTitle());
		PamGuiManagerFX.titleFont2style(titleLabel);
//		titleLabel.setFont(PamGuiManagerFX.titleFontSize2);
		sourcePane.add(titleLabel,0,0);

		this.sourceList=new ComboBox<PamDataBlock>();
		sourceList.setOnAction((action)->{
			this.sourceChanged();
		});
		///make sure stretches in pane.
		GridPane.setColumnSpan(sourceList,3);
		//make sure this fills the width
		GridPane.setHgrow(sourceList, Priority.ALWAYS);
        //GridPane.setHgrow(sourceList, Priority.ALWAYS);
		sourceList.setMaxWidth(Double.MAX_VALUE);
		
		sourcePane.add(sourceList,0,1);
		sourcePane.setMaxWidth(Double.MAX_VALUE);

		//create pane to hold channels. 
		channelPanel=new PamVBox();
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
		channelPanel.getChildren().add(channelListPane);
		
		selectAll =new CheckBox("All");
		selectAll.setOnAction((action)->{
			if (selectAll.isSelected()) selectAllChannels();
			else selectNoChannels();
            validator.validate(); //makes sure any error signs are removed
		});
		
		//create check to show at least some check boxes need to be selected.
		validator.createCheck()
        .dependsOn(("select all"), selectAll.selectedProperty())
        .withMethod(c -> {
          if (!isAChannelSelected() ) {
	              c.error("At least one channel needs to be selected for the module to work");
          }
        })
        .decorates(selectAll)
        .immediate();
      ;
		//create a list of channels and combo boxes for groups. 
		if (isHasChannels()){
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){
				channelBoxes[i] = new CheckBox("Channel " + i);
				 validator.createCheck()
		          .dependsOn(("channel " + i), channelBoxes[i].selectedProperty())
		          .withMethod(c -> {
		            if (!isAChannelSelected() ) {
			              c.error("At least one channel needs to be selected for the module to work");
		            }
		          })
		          .decorates(channelBoxes[i])
		          .immediate();
		        ;
				//channelPanel.getChildren().add(channelBoxes[i]);
				final int n=i;
				channelBoxes[i].setOnAction((action)->{
					selectionChanged(n);
		            validator.validate(); //makes sure any error signs are removed.
				});
				groupList[i]=new ComboBox<Integer>();
				//System.out.println("SourcePanel.java creatPanel"+i);
				for (int j = 0; j < PamConstants.MAX_CHANNELS; j++) {
					groupList[i].getItems().add(j); 
				}
			}
		}
		
		//align channels so they sit in middle of parent pane.
		PamHBox.setHgrow(channelPanel, Priority.ALWAYS);

		//pane to hold radio buttons, channle check boxes and group combo boxes. 
		PamHBox channelGroupPane=new PamHBox();
		channelGroupPane.setSpacing(15);
		channelGroupPane.getChildren().addAll(channelPanel, autoGroupPane);
		channelGroupPane.setAlignment(Pos.TOP_LEFT);
		
		sourcePane.add(channelGroupPane,0,3);
		
		//create source comboBox. 
		this.setCenter(sourcePane);

	}
	
	/**
	 * Check if 
	 * @return
	 */
	private boolean isAChannelSelected() {
		int channels = 0;
		PamDataBlock sb = getSource();
		if (sb != null) {
	//		channels = sb.getChannelMap();
			channels = sb.getSequenceMap();
		}
		int n=0; 
		//remove all channels from vertical box pane. 
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, channelBoxes.length); i++) {
			if ((channels & 1<<i) != 0 && this.channelBoxes[i].isSelected()) n++;
		} 
		if (n==0) return false;
		else return true;
	}
	
	@Override
	protected void showChannels() {
		// called when the selection changes - set visibility of the channel list
		int channels = 0;
		PamDataBlock sb = getSource();
		Character ch;
		if (sb != null) {
//			channels = sb.getChannelMap();
			channels = sb.getSequenceMap();
		}
		
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
		rePackOwner(channels);
		
		
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
	
	@Override
	protected void selectionChanged(int channel) {
		super.selectionChanged(channel);
		enableGroupBoxes();
	}

	public void enableGroupBoxes() {
		int groupType = getGrouping();
		for (int i = 0; i < groupList.length; i++) {
			groupList[i].setDisable(groupType != GROUP_USER || !channelBoxes[i].isSelected());
		}
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
	
	/**
	 * Set the source pane parameters. 
	 * @param params the grouped source parameters to set. 
	 */
	public void setParams(GroupedSourceParameters params) {
//		System.out.println("GroupedSourceParameters: " + params.getDataSource());
		//set the source
		setSource(params.getDataSource());
		//set grouping
		setGrouping(params.getGroupingType());
		//set channel group
		setChannelGroups(params.getChannelGroups());
		//set channel list
		setChannelList(params.getChanOrSeqBitmap());
	}
	
	/**
	 * Saves the values in the GUI to the passed parameters object
	 * @param params object to save the GUI values into
	 * @return true. 
	 */
	public boolean getParams(GroupedSourceParameters params) {
		if (params == null) {
			return false;
		}
		PamDataBlock sourceDB = getSource();
		if (sourceDB != null) {
//			params.setDataSource(getSource().getDataName());
			params.setDataSource(getSource().getLongDataName()); // use long data name, to match beamformer param settings
		}
		else {
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
		}
		
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
	 * The pane that holds the channels and the group settings. 
	 * @return the channel pane. 
	 */
	public Pane getChannelPane() {
		return channelPanel;
	}
	
}
