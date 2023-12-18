package PamView.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamUtils.PamUtils;
import PamView.GroupedSourceParameters;
import PamView.dialog.SourcePanel.SelectionListener;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;

public class GroupedSourcePanel extends SourcePanel {

	private JComboBox[] groupList;
	
	private boolean autoGrouping;
	
	public static final int GROUP_SINGLES = 0;
	public static final int GROUP_ALL = 1;
	public static final int GROUP_USER = 2;
	
	private JRadioButton allSingles, allTogether, userGrouped;
	JPanel autoGroupPanel;
	private JLabel channelListHeader;
	
	
	public GroupedSourcePanel(Window ownerWindow, Class sourceType, boolean hasChannels, boolean includeSubClasses, boolean autoGrouping) {
		super(ownerWindow, sourceType, hasChannels, includeSubClasses);
		
	}

	public GroupedSourcePanel(Window ownerWindow, String borderTitle, Class sourceType, boolean hasChannels, boolean includeSubClasses, boolean autoGrouping) {
		super(ownerWindow, borderTitle, sourceType, hasChannels, includeSubClasses);
	}

	protected void createPanel() {

		panel = new JPanel();
		JPanel sourcePanel = new JPanel();
		sourcePanel.setLayout(new BorderLayout());
		// add stuff to the panel.
		if (borderTitle != null) {
			sourcePanel.setBorder(new TitledBorder(borderTitle));
		}
		panel.setLayout(new BorderLayout());
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		sourcePanel.add(BorderLayout.CENTER, sourceList = new JComboBox());
		panel.add(BorderLayout.NORTH, sourcePanel);
		sourceList.addActionListener(this);
		if (hasChannels) {
			JPanel channelGroupPanel = new JPanel();
			channelGroupPanel.setLayout(new BorderLayout());
			channelGroupPanel.setBorder(new TitledBorder("Channel/Sequence list and grouping"));
			JPanel channelPanel = new JPanel();
			JPanel autoGroupPanel = new JPanel();
			autoGroupPanel.setLayout(new BoxLayout(autoGroupPanel, BoxLayout.Y_AXIS));
			autoGroupPanel.add(new JLabel("Auto Grouping"));
			autoGroupPanel.add(allSingles = new JRadioButton("No grouping"));
			autoGroupPanel.add(allTogether = new JRadioButton("One group"));
			autoGroupPanel.add(userGrouped = new JRadioButton("User groups"));
			allSingles.addActionListener(new GroupAction(GROUP_SINGLES));
			allTogether.addActionListener(new GroupAction(GROUP_ALL));
			userGrouped.addActionListener(new GroupAction(GROUP_USER));
			ButtonGroup bg = new ButtonGroup();
			bg.add(allSingles);
			bg.add(allTogether);
			bg.add(userGrouped);
//			channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.Y_AXIS));
			GridBagLayout layout;
//			channelPanel.setBorder(new TitledBorder("Channel list and grouping"));
			channelPanel.setLayout(layout = new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.WEST;
			c.gridx = c.gridy = 0;
			addComponent(channelPanel, autoGroupPanel, c);
//			addComponent(channelPanel, new JLabel("Channel list ..."), c);
			c.gridwidth = 1;
			c.insets = new Insets(0,5,0,5);
			c.gridy++;
			c.gridx = 0;
			channelListHeader = new JLabel("Channel");
			addComponent(channelPanel, channelListHeader, c);
			c.gridx++;
			addComponent(channelPanel, new JLabel("Group"), c);
			groupList = new JComboBox[PamConstants.MAX_CHANNELS];
			channelBoxes = new JCheckBox[PamConstants.MAX_CHANNELS];
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){
				channelBoxes[i] = new JCheckBox("Channel " + i);
				groupList[i] = new JComboBox();
				c.gridy ++;
				c.gridx = 0;
				addComponent(channelPanel, channelBoxes[i], c);
				c.gridx ++;
				addComponent(channelPanel, groupList[i], c);
//				channelPanel.add(channelBoxes[i]);
				channelBoxes[i].setVisible(false);
				groupList[i].setVisible(false);
				channelBoxes[i].addActionListener(new SelectionListener(i));
			}
			channelGroupPanel.add(BorderLayout.WEST, autoGroupPanel);
			channelGroupPanel.add(BorderLayout.CENTER, new ChannelListScroller(channelPanel));
			panel.add(BorderLayout.CENTER, channelGroupPanel);
		}
	}
	@Override
	protected void showChannels() {
		// called when the selection changes - set visibility of the channel list
		int channels = 0;
		PamDataBlock sb = getSource();
		String chanOrSeqString = "Channel ";
		channelListHeader.setText("Channel");
		
		// get the channel map and change the names of the list entries to either Channel or Sequence, depending on the source
		if (sb != null) {
//			channels = sb.getChannelMap();
			channels = sb.getSequenceMap(); // use the sequence bitmap instead of the channel bitmap, in case this is beamformer output
			if (sb.getSequenceMapObject()!=null) {
				chanOrSeqString = "Sequence ";
				channelListHeader.setText("Sequence");
			}
		}
		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			boolean isVisible = ((channels & 1<<i) != 0);
			channelBoxes[i].setText(chanOrSeqString+ i);
			channelBoxes[i].setVisible(isVisible);
			if (!isVisible) { // deselect anything that is hidden
				channelBoxes[i].setSelected(isVisible);
			}
			groupList[i].setVisible(isVisible);
			groupList[i].removeAllItems();
			for (int j = 0; j < PamConstants.MAX_CHANNELS; j++) {
				groupList[i].addItem(j);
			}
		}

		rePackOwner(channels);
	}
	
	public void setChannelGroups(int[] channelGroups) {
		if (channelGroups == null) return;
		for (int i = 0; i < Math.min(channelGroups.length, PamConstants.MAX_CHANNELS); i++) {
			groupList[i].setSelectedIndex(channelGroups[i]);
		}
	}

	public static void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}

	public boolean isAutoGrouping() {
		return autoGrouping;
	}

	public void setAutoGrouping(boolean autoGrouping) {
		this.autoGrouping = autoGrouping;
		autoGroupPanel.setVisible(autoGrouping);
	}
	
	public void setGrouping(int groupType) {
		allSingles.setSelected(groupType == GROUP_SINGLES);
		allTogether.setSelected(groupType == GROUP_ALL);
		userGrouped.setSelected(groupType == GROUP_USER);
		for (int i = 0; i < groupList.length; i++) {
			if (groupType == GROUP_ALL) {
				groupList[i].setSelectedItem(0);
			}
			else if (groupType == GROUP_SINGLES) {
				groupList[i].setSelectedItem(i);
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
			groupList[i].setEnabled(groupType == GROUP_USER && channelBoxes[i].isSelected());
		}
	}
	
	public int getGrouping() {
		if (allSingles.isSelected()) return GROUP_SINGLES;
		if (allTogether.isSelected()) return GROUP_ALL;
		if (userGrouped.isSelected()) return GROUP_USER;
		return -1;
	}
	
	/**
	 * Returns the channel group assignment.  The array returned is of length nChannels, and
	 * each index position 'i' holds the group number that is associated with channel 'i' 
	 * @return
	 */
	public int[] getChannelGroups() {
		int[] groups = new int[PamConstants.MAX_CHANNELS];
		String str;
		for (int i = 0; i < Math.min(groupList.length, PamConstants.MAX_CHANNELS); i++) {
//			str = groupList[i].getSelectedItem().toString();
//			groups[i] = str.charAt(0);
			groups[i] = groupList[i].getSelectedIndex();
		}
		return groups;
	}
	
	public void setParams(GroupedSourceParameters params) {
		setSource(params.getDataSource());
		setChannelList(params.getChanOrSeqBitmap()); // set the channel box selection earlier, so that the setGrouping can properly enable the groupList combo boxes 
		setGrouping(params.getGroupingType());
		setChannelGroups(params.getChannelGroups());
//		setChannelList(params.getChanOrSeqBitmap());
	}
	
	public boolean getParams(GroupedSourceParameters params) {
		if (params == null) {
			return false;
		}
		PamDataBlock sourceDB = getSource();
		if (sourceDB != null) {
			params.setDataSource(getSource().getLongDataName());
		}
		else {
			return false;
		}
		params.setGroupingType(getGrouping());
		params.setChannelGroups(getChannelGroups());
		params.setChanOrSeqBitmap(getChannelList());
		
		return true;
	}
	
	private class GroupAction implements ActionListener {

		private int groupType;
		
		public GroupAction(int groupType) {
			super();
			this.groupType = groupType;
		}

		public void actionPerformed(ActionEvent e) {
			setGrouping(groupType);	
		}
		
	}
	
	/**
	 * Get a look up table which can assign individual channels to 
	 * indexed groups. 
	 * @param channelMap total channel map
	 * @param groupList channel group assignments
	 * @return look up table from channels to groups. -1 where a channel isn't assigned. 
	 */
	public static int[] getGroupAssignments(int channelMap, int[] groupList) {
		int nChan = PamUtils.getHighestChannel(channelMap);
		int nGroups = countChannelGroups(channelMap, groupList);
		int[] lut = new int[nChan+1];
		for (int i = 0; i <= nChan; i++) {
			lut[i] = -1;
		}
		for (int i = 0; i < nGroups; i++) {
			int groupChannels = GroupedSourcePanel.getGroupChannels(i, channelMap, groupList);
			for (int c = 0; c < PamConstants.MAX_CHANNELS; c++) {
				if ((channelMap & groupChannels & 1<<c) != 0) {
					lut[c] = i;
				}
			}
		}
		
		return lut;
	}

	static public int getGroupMap(int channelMap, int[] groupList) {
		int groupMap = 0;
		if (groupList == null) return 0;
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, groupList.length); i++) {
			if ((channelMap & (1<<i)) == 0) continue;
			groupMap |= (1<<groupList[i]);
		}
		return groupMap;
	}
	
	/**
	 * Count the number of separate channel groups
	 * @param channelMap total channel map
	 * @param groupList channel group assignments
	 * @return number of groups
	 */
	static public int countChannelGroups(int channelMap, int[] groupList) {
		if (groupList == null) return 0;
		int groupMap = getGroupMap(channelMap, groupList);
		return PamUtils.getNumChannels(groupMap);
	}
	
	/**
	 * Get a bitmap of channels in the ith group
	 * @param group group number
	 * @param channelMap total channel map
	 * @param groupList channel group assignments
	 * @return bitmap of channels in the group
	 */
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
	
	/**
	 * Get a string representation of the channels in a group
	 * @param group group index
	 * @param channelMap total channel map
	 * @param groupList channel group assignments
	 * @return string representation of the group
	 */
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
	
	/**
	 * 
	 * @param groupMap
	 * @param channelMap
	 * @param groupList
	 * @return
	 */
	static public int getGroupIndex(int groupMap, int channelMap, int[] groupList) {
		if (groupList!=null) {
			for (int i = 0; i < groupList.length; i++) {
				if (groupMap == getGroupChannels(i, channelMap, groupList)) {
					return i;
				}
			}
		}
		return -1;
	}
	
}
