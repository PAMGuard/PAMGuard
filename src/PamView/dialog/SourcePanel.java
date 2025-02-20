package PamView.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;





import PamController.PamController;
import PamDetection.LocalisationInfo;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

/**
 * Standard panel for dialogs that shows a list of
 * available data sources and, optionally a list of data channels.
 * <p>
 * This is for general use within other dialog panels. 
 *  
 * @author Doug Gillespie
 *
 */
public class SourcePanel implements ActionListener{

	protected JPanel panel;
	protected ArrayList<Class> sourceTypes = new ArrayList();
	protected boolean hasChannels;
	protected String borderTitle;
	protected boolean includeSubClasses;
	
	protected JComboBox<PamDataBlock> sourceList;
	protected JCheckBox channelBoxes[];
	
	protected Window ownerWindow;
	
	/**
	 * Flags to specify the minimum localisation data requirements from a data block. 
	 * N.B. Just because a data block says it data MAY have particular localisation information,
	 * that is no guarantee that those information are there within individual data units. 
	 */
	private int localisationRequirements;
	
	protected ArrayList<PamDataBlock> excludedBlocks = new ArrayList<PamDataBlock>();
	
	private ArrayList<SourcePanelMonitor> spMonitors = new ArrayList<SourcePanelMonitor>();
	private JPanel finalCheckBoxPanel;
	private JComponent centralPanel;
	
	private JLabel channelListHeader;
	
	/**
	 * Allow a null selection, i.e. don't want to select anything at all. 
	 */
	private boolean allowNull;
	/**
	 * Construct a panel with a titles border
	 * @param ownerWindow parentWindow
	 * @param borderTitle Title to go in border
	 * @param sourceType Data Source type
	 * @param hasChannels Include a set of checkboxes to list available channels
	 * @param includeSubClasses inlcude all subclasses of sourceType in the list. 
	 */
	public SourcePanel(Window ownerWindow, String borderTitle, Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		this.ownerWindow = ownerWindow;
		this.sourceTypes.add(sourceType);
		this.hasChannels = hasChannels;
		this.borderTitle = borderTitle;
		this.includeSubClasses = includeSubClasses;
		createPanel();
		setSourceList();
	}

	/**
	 * Construct a panel without a border
	 * @param ownerWindow Parent window
	 * @param sourceType Data Source type
	 * @param hasChannels Include a set of checkboxes to list available channels
	 * @param include subclasses of the sourceType
	 */
	public SourcePanel(Window ownerWindow, Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		this.ownerWindow = ownerWindow;
		this.sourceTypes.add(sourceType);
		this.hasChannels = hasChannels;
		this.includeSubClasses = includeSubClasses;
		createPanel();
		setSourceList();
	}
	
	class SelectionListener implements ActionListener {
		
		private int channel;
		
		public SelectionListener(int channel) {
			super();
			this.channel = channel;
		}
		
		public void actionPerformed(ActionEvent e) {
			selectionChanged(channel);
		}
		
	}
	
	public void setEnabled(boolean enabled) {
		sourceList.setEnabled(enabled);
	}
	
	public void setEnabledWithChannels(boolean enabled) {
		sourceList.setEnabled(enabled);
		JCheckBox[] boxList = getChannelBoxes();
		for (JCheckBox currentBox : boxList) {
			currentBox.setEnabled(enabled);
		}
	}
	
	/**
	 * Add a listener to the data source drop down list
	 * @param listener listener 
	 */
	public void addSelectionListener(ActionListener listener) {
		sourceList.addActionListener(listener);
	}
	
	protected void selectionChanged(int channel) {
		notifySourcePanelMonitors();
	}
	
	protected void createPanel() {
		panel = new JPanel();
		// add stuff to the panel.
		if (borderTitle != null) {
			panel.setBorder(new TitledBorder(borderTitle));
		}
		panel.setLayout(new BorderLayout());
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(BorderLayout.NORTH, sourceList = new JComboBox());
		sourceList.addActionListener(this);
		if (hasChannels) {
			JPanel channelOuterPanel = new JPanel(new BorderLayout());
			JPanel channelPanel = new JPanel();
			channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.Y_AXIS));
			channelListHeader = new JLabel("Channel list ...");
			panel.add(channelListHeader);
			channelBoxes = new JCheckBox[PamConstants.MAX_CHANNELS];
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){
				channelBoxes[i] = new JCheckBox("Channel " + i);
				channelPanel.add(channelBoxes[i]);
				channelBoxes[i].setVisible(true);
				channelBoxes[i].addActionListener(new SelectionListener(i));
				//System.out.println("SourcePanel.java creatPanel"+i);
			}
			JPanel channelButtonPanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
//			channelButtonPanel.setLayout(new BoxLayout(channelButtonPanel, BoxLayout.Y_AXIS));
			JButton allButton = new JButton(" Select All");
			channelButtonPanel.add(allButton, c);
			JButton noneButton = new JButton(" Select None");
			c.gridy++;
			channelButtonPanel.add(noneButton, c);
			
			allButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectAllChannels();
				}
			});
			noneButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectNoChannels();
				}
			});
			
			channelOuterPanel.add(BorderLayout.CENTER, (channelPanel));
			JPanel nePanel = new JPanel(new BorderLayout());
			nePanel.add(BorderLayout.NORTH, channelButtonPanel);
			channelOuterPanel.add(BorderLayout.EAST, nePanel);
			finalCheckBoxPanel = new JPanel(new BorderLayout());
			finalCheckBoxPanel.add(BorderLayout.WEST, (channelOuterPanel));
			panel.add(BorderLayout.CENTER, centralPanel = new ChannelListScroller(finalCheckBoxPanel));
		}
	}
	
	protected void selectNoChannels() {
		for (int i = 0; i < channelBoxes.length; i++) {
			if (channelBoxes[i] != null) {
				channelBoxes[i].setSelected(false);
			}
		}
	}

	protected void selectAllChannels() {
		for (int i = 0; i < channelBoxes.length; i++) {
			if (channelBoxes[i] != null && channelBoxes[i].isVisible()) {
				channelBoxes[i].setSelected(true);
			}
		}
	}

	/**
	 * action listener to update channel list when a 
	 * a different source is selected
	 */
	public void actionPerformed(ActionEvent e) {
		sourceChanged();
	}
	
	protected void sourceChanged() {
		showChannels();
	}
	
	protected void showChannels() {
		// called when the selection changes - set visibility of the channel list
		if (channelBoxes == null) return;
		int channels = 0;
		PamDataBlock sb = getSource();
		if (sb == null) {
			return;
		}
		String chanOrSeqString = "Channel ";
		channelListHeader.setText("Channel list ...");
		
		// get the channel map and change the names of the list entries to either Channel or Sequence, depending on the source
		if (sb != null) {
//			channels = sb.getChannelMap();
			channels = sb.getSequenceMap(); // use the sequence bitmap instead of the channel bitmap, in case this is beamformer output
			if (sb.getSequenceMapObject()!=null) {
				chanOrSeqString = "Sequence ";
				channelListHeader.setText("Sequence list ...");
			}
		}
		
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, channelBoxes.length); i++) {
			boolean isVisible = ((channels & 1<<i) != 0);
			channelBoxes[i].setText(chanOrSeqString+ i);
			channelBoxes[i].setVisible(isVisible);
			if (!isVisible) { // deselect anything that is hidden
				channelBoxes[i].setSelected(isVisible);
			}
		}
		rePackOwner(channels);
	}
	
	private int currentNShown = 0;
	private NullDataBlock nullDataBlock;
	/**
	 * Repack the owner window if the number of channels has changed
	 * @param channelsMap bitmap of used channels. 
	 */
	protected void rePackOwner(int channelsMap) {
//		selectScrolling();
		if (currentNShown != PamUtils.getNumChannels(channelsMap)) {
			if (ownerWindow != null) {
				try {
					ownerWindow.pack();
				}
				catch (Exception e) {
//					System.out.println("Problems packing SourcePanel owner window");
				}
			}
			currentNShown = PamUtils.getNumChannels(channelsMap);
		}
	}
	
	/**
	 * If there are > 8 visible check boxes, put the panel in a scroller 
	 * so that it all fits on a screen still. 
	 */
//	private void selectScrolling() {
//		int nVisible = countVisible();
//		if (panel == null || finalCheckBoxPanel == null) {
//			return;
//		}
//		if (centralPanel != null) {
//			panel.remove(centralPanel);
//		}
//		if (nVisible > 8) {
//			JScrollPane scrollBar = new JScrollPane(finalCheckBoxPanel);
//			scrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//			scrollBar.setPreferredSize(new Dimension(0, 200));
//			centralPanel = scrollBar;
//		}
//		else {
//			centralPanel = finalCheckBoxPanel;
//		}
//		panel.add(centralPanel, BorderLayout.CENTER);
//	}
//	
//	private int countVisible() {
//		int n = 0;
//		if (channelBoxes == null) {
//			return 0;
//		}
//		for (int i = 0; i < channelBoxes.length; i++) {
//			if (channelBoxes[i].isVisible()) {
//				n++;
//			}
//		}
//		return n;
//	}

	/**
	 * Set the current data source using the data source name
	 * @param sourceName
	 * @return true if OK
	 */
	public boolean setSource(String sourceName) {
		// search the list for the string
		PamDataBlock byLongName = PamController.getInstance().getDataBlockByLongName(sourceName);
		if (byLongName != null) {
			sourceList.setSelectedItem(byLongName);
			return true;
		}
		for (int i = 0; i < sourceList.getItemCount(); i++){
			if (sourceList.getItemAt(i).toString().equals(sourceName)) {
				sourceList.setSelectedIndex(i); 
				//System.out.println("SourcePanel.java :: setSource 1");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Set the current data source by block reference
	 * @param sourceBlock
	 */
	synchronized public void setSource(PamDataBlock sourceBlock) {
		
		/*
		 * Several dialogs are not calling setSourceList each time
		 * they open. Although some are, I've decided to call this here anyway
		 * since calling it twice is better than not at all.
		 */
		setSourceList();
		/*
		 * The direct way of doing this seems to have stopped working - very worrying !
		 */
		sourceList.setSelectedItem(sourceBlock);
		//System.out.println("SourcePanel.java :: setSource 2");
//		sourceList.setSelectedIndex(2);
	}
	
	synchronized public void setSourceIndex(int sourceIndex) {
		/*
		 * Several dialogs are not calling setSourceList each time
		 * they open. Although some are, I've decided to call this here anyway
		 * since calling it twice is better than not at all.
		 */
		setSourceList();
		if (sourceIndex < 0 || sourceIndex >= sourceList.getItemCount()) {
			return;
		}
		sourceList.setSelectedIndex(sourceIndex);
	}
	
	/**
	 * Fill the list of available data sources, taking into account 
	 * the list of excluded sources
	 * @return sets a list of available sources. Returns false if no sources are available. 
	 */
	synchronized public boolean setSourceList() {
		return setSourceList(false);
	}
	
	synchronized public boolean setSourceList(boolean replaceChosen) {
	
		Object selectedItem = null;
		if (replaceChosen) {
			selectedItem = sourceList.getSelectedItem();
		}
		
		sourceList.removeAllItems();
		ArrayList<PamDataBlock> sl = getCompatibleDataBlocks();
		if (sl == null || sl.size() <= 0) return false;
		
		LocalisationInfo availableLocData;
		
		if (allowNull) {
			sourceList.addItem(getNullDataBlock());
		}
		
		for (int i = 0; i < sl.size(); i++) {
			
			if (excludedBlocks.contains(sl.get(i))) continue;
			
			availableLocData = sl.get(i).getLocalisationContents();
			
			if (!availableLocData.hasLocContent(localisationRequirements)) continue;
			//if ((localisationRequirements & availableLocData) < localisationRequirements) continue;
			
			sourceList.addItem(sl.get(i));
			
		}
		if (replaceChosen && selectedItem != null) {
			sourceList.setSelectedItem(selectedItem);
		}
		if (ownerWindow != null) {
			try {
				ownerWindow.pack();
			}
			catch (Exception e) {
//				System.out.println("Problems packing SourcePanel owner window");
			}
		}
		return true;
	}
	
	public ArrayList<PamDataBlock> getCompatibleDataBlocks() {
		ArrayList<PamDataBlock> blocks = new ArrayList<PamDataBlock>();
		for (Class c : sourceTypes) {
			ArrayList<PamDataBlock> sl = PamController.getInstance().getDataBlocks(c, includeSubClasses);
			for (PamDataBlock b : sl) {
				if (blocks.contains(b)) {
					continue;
				}
				blocks.add(b);
			}
		}
		
		return blocks;
	}
	
	/**
	 * return the selected data source
	 * @return source data block
	 */
	public PamDataBlock getSource() {
		PamDataBlock source = (PamDataBlock) sourceList.getSelectedItem();
		if (source == getNullDataBlock()) {
			return null;
		}
		else {
			return source;
		}
	}
	
	/**
	 * 
	 * @return the source data block name, or null if nothing selected. 
	 */
	public String getSourceName() {
		PamDataBlock source = getSource();
		if (source == null) {
			return null;
		}
		return source.getLongDataName();
	}

	/**
	 * Get a list of selected channels or sequences (depending on the source)
	 * @return bitmap of selected channels
	 */
	public int getChannelList() {
		PamDataBlock sb = getSource();
		if (sb == null) return 0;
		// if channels are not selectable, then return the default map ...
//		int sourceChannels = sb.getChannelMap();
		int sourceChannels = sb.getSequenceMap(); // use the sequence bitmap instead of the channel bitmap, in case this is beamformer output
		if (hasChannels == false) return sourceChannels;
		int channelList = 0;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((sourceChannels & (1<<i)) != 0 && channelBoxes[i].isSelected()) {
				channelList |= (1<<i);
				//System.out.println("1. SourcePanel getchannelList " +i+":"+ channelList);
			}
		}
		return channelList;
	}
	
	/**
	 * Set the current channel selection
	 * Only enables channels that are available (remainder are silently disabled).
	 * @param channelList bitmap of currently selected channels
	 */
	public void setChannelList(int channelList) {
		PamDataBlock source = getSource();
		if (source == null) {
			return;
		}
		var availableChannels = source.getSequenceMap();
		var toSelect = channelList & availableChannels;

		if (toSelect != channelList) {
			System.out.printf("SourcePanel: availableChannels=%d, channelList=%d, toSelect=%d - deselecting unavailable channels.\n", availableChannels, channelList, toSelect);
		}

		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			channelBoxes[i].setSelected((toSelect & (1<<i)) != 0);
		}
	}
	
	public void addSourceType(Class sourceType) {
		sourceTypes.add(sourceType);
		setSourceList();
	}
//	/**
//	 * get the data source type for this SourcePanel
//	 * @return data type of a data source
//	 */
//	public Class getSourceType() {
//		return sourceType;
//	}
//	
//	/**
//	 * Set the source type for this SourcePanel
//	 * @param sourceType
//	 */ 
//	public void setSourceType(Class sourceType) {
//		this.sourceType = sourceType;
//		setSourceList();
//	}
	
	/**
	 * Get the currently selected source index
	 * @return source index within the source list
	 */
	public int getSourceIndex() {
		return sourceList.getSelectedIndex();
	}
	
	/**
	 * Get a reference to the JPanel containing the controls
	 * @return JPanel container
	 */
	public JPanel getPanel() {
		return panel;
	}
	
	/**
	 * Exclude specific data blocks from the source list. e.g. a process would normally not
	 * be able to use it's own output data block as an input source and should therefore
	 * exclude it from the list.
	 * @param block PamDataBlock to exlcude
	 * @param exclude true - excluse; false - allow
	 */
	public void excludeDataBlock(PamDataBlock block, boolean exclude) {
		if (exclude) {
			if (excludedBlocks.contains(block) == false) {
				excludedBlocks.add(block);
			}
		}
		else {
			excludedBlocks.remove(block);
		}
		setSourceList(true);
	}
	
	/**
	 * Clear the list of excluded data blocks. 
	 *
	 */
	public void clearExcludeList() {
		excludedBlocks.clear();
		setSourceList(true);
	}

	public JCheckBox[] getChannelBoxes() {
		return channelBoxes;
	}

	public int getLocalisationRequirements() {
		return localisationRequirements;
	}

	public void setLocalisationRequirements(int localisationRequirements) {
		this.localisationRequirements = localisationRequirements;
		setSourceList();
	}
	
	/**
	 * Get the number of items in the list. 
	 * @return number of sources of this type of data
	 */
	public int getSourceCount() {
		return sourceList.getItemCount();
	}

	public void addSourcePanelMonitor(SourcePanelMonitor gspm) {
		spMonitors.add(gspm);
	}
	public void removeSourcePanelMonitor(SourcePanelMonitor gspm) {
		spMonitors.remove(gspm);
	}
	private void notifySourcePanelMonitors() {
		for (int i = 0; i < spMonitors.size(); i++) {
			spMonitors.get(i).channelSelectionChanged();
		}
	}

	/**
	 * Set a tooltip text for the source panel. 
	 * @param toolTip Tooltip text. 
	 */
	public void setSourceToolTip(String toolTip) {
		sourceList.setToolTipText(toolTip);
	}

	/**
	 * @return the allowNull
	 */
	public boolean isAllowNull() {
		return allowNull;
	}

	/**
	 * Allow a null selection. 
	 * @param allowNull the allowNull to set
	 */
	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
		setSourceList();
	}
	
	private NullDataBlock getNullDataBlock() {
		if (nullDataBlock == null) {
			nullDataBlock = new NullDataBlock();
		}
		return nullDataBlock;
	}
	
	private class NullDataBlock extends PamDataBlock {

		public NullDataBlock() {
			super(PamDataUnit.class, "Null data", null, 0);
		}

		@Override
		public String toString() {
			return "No (null) selection";
		}
		
	}
}
