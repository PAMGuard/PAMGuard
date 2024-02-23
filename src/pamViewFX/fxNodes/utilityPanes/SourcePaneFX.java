package pamViewFX.fxNodes.utilityPanes;


import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Window;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;
import pamViewFX.validator.PamValidator;
import PamController.PamController;
import PamController.PamGUIManager;
import PamDetection.LocalisationInfo;
import PamUtils.PamUtils;
import PamView.dialog.SourcePanelMonitor;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;

/**
 * Standard panel for dialogs that shows a list of
 * available data sources and, optionally a list of data channels.
 * <p>
 * This is for general use within other dialog panels. 
 *  
 * @author Doug Gillespie, Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class SourcePaneFX extends PamBorderPane {

	private ArrayList<SourceSelection> sourceType = new ArrayList<>();
	private boolean hasChannels;
	private String borderTitle;
	
	/**
	 * Combo Box which holds a list of data blocks 
	 */
	protected ComboBox<PamDataBlock> sourceList;
	
	/**
	 * List of cehck boxes for each channel up to the maximum number of PAM_CHANNELS. 
	 */
	protected CheckBox channelBoxes[];
	
	
	/**
	 * Flags to specify the minimum localisation data requirements from a data block. 
	 * N.B. Just because a data block says it data MAY have particular localisation information,
	 * that is no guarantee that those information are there within individual data units. 
	 */
	private int localisationRequirements;
	
	protected ArrayList<PamDataBlock> excludedBlocks = new ArrayList<PamDataBlock>();
	
	private ArrayList<SourcePanelMonitor> spMonitors = new ArrayList<SourcePanelMonitor>();
	
	/**
	 * Select all check box. 
	 */
	protected CheckBox selectAll;
	
	/**
	 * Pane whihc holds a list of channel check boxes.
	 */
	private PamVBox channelListPane;
	
	/**
	 * The title labe. Sits above the combo box. 
	 */
	private Label titleLabel;
	
	/**
	 * Validator for channels 
	 */
    protected PamValidator channelValidator;

	/**
	 * Construct a panel with a titles border
	 * @param borderTitle Title to go in border
	 * @param sourceType Data Source type
	 * @param hasChannels Include a set of checkboxes to list available channels
	 * @param includeSubClasses include all subclasses of sourceType in the list. 
	 */
	public SourcePaneFX(String borderTitle, Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		if (sourceType != null) {
			this.sourceType.add(new SourceSelection(sourceType, includeSubClasses));
		}
		channelValidator = new PamValidator();
		this.setHasChannels(hasChannels);
		this.setBorderTitle(borderTitle);
		createPanel();
		setSourceList();
	}

	/**
	 * Construct a panel without a border
	 * @param sourceType Data Source type
	 * @param hasChannels Include a set of checkboxes to list available channels
	 * @param include subclasses of the sourceType
	 */
	public SourcePaneFX(Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		this.sourceType.add(new SourceSelection(sourceType, includeSubClasses));
		channelValidator = new PamValidator();
		this.setHasChannels(hasChannels);
		createPanel();
		setSourceList();
	}
	
	class SelectionListener implements ChangeListener {
		
		private int channel;
		
		public SelectionListener(int channel) {
			super();
			this.channel = channel;
		}

		@Override
		public void changed(ObservableValue observable, Object oldValue,
				Object newValue) {
			selectionChanged(channel);
		}
		
	}
	
	public void setEnabled(boolean enabled) {
		sourceList.setDisable(!enabled);
	}
	
	public void setEnabledWithChannels(boolean enabled) {
		sourceList.setDisable(!enabled);
		CheckBox[] boxList = getChannelBoxes();
		for (CheckBox currentBox : boxList) {
			currentBox.setDisable(!enabled);
		}
	}
	
	/**
	 * Add a listener to the data source drop down list
	 * @param listener listener 
	 */
	@SuppressWarnings("unchecked")
	public void addSelectionListener(ChangeListener listener) {
		sourceList.valueProperty().addListener(listener);
	}
	
	protected void selectionChanged(int channel) {
		setAllChanStatus();
		notifySourcePanelMonitors();
	}
	
	
	protected void createPanel() {
				
		PamVBox comboBoxPane=new PamVBox();
		comboBoxPane.setSpacing(5);
		
		if (getBorderTitle()!=null) {
			titleLabel = new Label(getBorderTitle());
//			titleLabel.setFont(PamGuiManagerFXAWT.titleFontSize);
			comboBoxPane.getChildren().add(titleLabel);
		}

		this.sourceList=new ComboBox<PamDataBlock>();
		sourceList.setOnAction((action)->{
			this.sourceChanged();
		});
		///make sure stretches in pane.
		sourceList.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(sourceList, Priority.ALWAYS);
		
		comboBoxPane.getChildren().add(sourceList);
		
		//create pane to hold channels. 
		PamVBox channelPanel=new PamVBox();
		channelPanel.setSpacing(5);
		
		if (isHasChannels()) {
			Label channelLabel = new Label("Channel");
			//PamGuiManagerFX.titleFont2style(channelLabel);
//			channelLabel.setFont(PamGuiManagerFX.titleFontSize2);
			comboBoxPane.getChildren().add(channelLabel);

			channelPanel.getChildren().add(selectAll =new CheckBox("All"));
			channelBoxes =new CheckBox[PamConstants.MAX_CHANNELS];
			selectAll.setOnAction((action)->{
	            System.out.println("Stylesheets: 0: " +  getStylesheets().size());
				if (selectAll.isSelected()) selectAllChannels();
				else selectNoChannels();
	            channelValidator.validate(); //makes sure any error signs are removed.
	            System.out.println("Stylesheets: 1: " +  getStylesheets().size());
			});
		}
		
		//create pane to hold list of channels. 
		channelListPane=new PamVBox();
		channelListPane.setSpacing(5);
		
		channelPanel.getChildren().add(channelListPane);
		if (isHasChannels()){
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){
				channelBoxes[i] = new CheckBox("Channel " + i);
				 channelValidator.createCheck()
		          .dependsOn(("channel_" + i +  "_" + this), channelBoxes[i].selectedProperty())
		          .withMethod(c -> {
		            if (!isAChannelSelected() ) {
			              c.error("At least one channel needs to be selected for the module to work");
		            }
		          })
		          .decorates(channelBoxes[i])
		          .immediate();
		 
				//channelPanel.getChildren().add(channelBoxes[i]);
				final int n=i;
				channelBoxes[i].setOnAction((action)->{
					selectionChanged(n);
		            channelValidator.validate(); //makes sure any error signs are removed.
				});
				//System.out.println("SourcePanel.java creatPanel"+i);
			}
		}
		
		comboBoxPane.getChildren().add(channelPanel);
		
		//create source comboBox. 
		this.setCenter(comboBoxPane);
		
	}
	

	/**
	 * Check if a single channel is selected. 
	 * @return true if at least one channel is selected.
	 */
	public boolean isAChannelSelected() {
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
	 * Set the status of the 'select all' check box. Selected if all channels are selected, 
	 * intermediate if some channels are selected and unselected if no channels are selected. 
	 */
	private void setAllChanStatus(){
		
		selectAll.setIndeterminate(false);
		
		int channels = 0;
		int nChan=0;
		PamDataBlock<?> sb = getSource();
		if (sb != null) {
//			channels = sb.getChannelMap();
//			nChan = PamUtils.getNumChannels( sb.getChannelMap());
			channels = sb.getSequenceMap();
			nChan = PamUtils.getNumChannels( sb.getSequenceMap());
		}
		else{
			selectAll.setIndeterminate(true);
			return;
		}
		
		int n=0; 
		//remove all channels from vertical box pane. 
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, channelBoxes.length); i++) {
			if ((channels & 1<<i) != 0 && this.channelBoxes[i].isSelected()) n++;
		} 
		
		if (n==nChan) selectAll.setSelected(true);
		else if (n==0) selectAll.setSelected(false);
		else selectAll.setIndeterminate(true);

	}


	/**
	 * Tell the pane the source has changed. 
	 */
	public void sourceChanged() {
		//double h1 = this.getHeight();
		showChannels();
		//double h2 = this.getHeight();
//		System.out.printf("SourcePaneFX heights before/after %3.1f %3.1f\n" ,h1, h2);
	}
	
	protected void showChannels() {
		// called when the selection changes - set visibility of the channel list
		if (channelBoxes == null ||this.isHasChannels()==false) return;
		int channels = 0;
		PamDataBlock<?> sb = getSource();
		if (sb != null) {
//			channels = sb.getChannelMap();
			channels = sb.getSequenceMap();
		}
//		System.out.println(" SourcePaneFX: channels: "+channels);
		//remove all channels from vertical box pane. 
		channelListPane.getChildren().removeAll(channelListPane.getChildren());
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, channelBoxes.length); i++) {
			if ((channels & 1<<i) != 0){
				channelListPane.getChildren().add(channelBoxes[i]);
			}; 
		} 
		this.layout();
		rePackOwner(channels);
	}
	
	private int currentNShown = 0;
	private Class requiredClassType;
	
	
	/**
	 * Repack the owner window if the number of channels has changed
	 * @param channelsMap bitmap of used channels. 
	 */
	protected void rePackOwner(int channelsMap) {
		if (PamGUIManager.isSwing()) {
//		if (currentNShown != PamUtils.getNumChannels(channelsMap)) {
			try {
//				Stage stage = (Stage) this.getScene().getWindow();
				this.layout();
				Scene thisScene = this.getScene();
				double h1 = thisScene.getHeight();
				Window stage = this.getScene().getWindow();
				double h2 = stage.getHeight();
//				System.out.printf("Scene/Window heights before sizeToScene %3.1f %3.1f\n" ,h1, h2);
				
				// There is a chance that this component might be sitting in a Swing dialog.
				// Loop through the parent nodes until we get to the top-level FX Node, and see
				// if it's a BorderPaneFX2AWT class.  If so, it's got a reference to the parent
				// swing dialog which we can use to call a pack()
				Node thisNode = this.getParent();
				while (thisNode!=null) {
					thisNode = thisNode.getParent();
					if (thisNode instanceof BorderPaneFX2AWT) {
						final Node finalNode = thisNode;
						PamDialogFX2AWT swingDialog = (PamDialogFX2AWT) ((BorderPaneFX2AWT) finalNode).getAwtParent();
						double h3 = swingDialog.getDlgContent().getHeight();
//						System.out.printf("SourcePaneFX height is %3.1f\n" ,this.getHeight());
						stage.sizeToScene();
//						System.out.printf("SourcePaneFX height is %3.1f\n" ,this.getHeight());
						h1 = thisScene.getHeight();
						h2 = stage.getHeight();
						double h4 = swingDialog.getDlgContent().getHeight();
//						System.out.printf("Scene/Window heights after sizeToScene %3.1f %3.1f\n" ,h1, h2);
//						System.out.printf("dlgContent heights before/after %3.1f %3.1f\n" ,h3, h4);
						SwingUtilities.invokeLater(new Runnable() {
						    @Override
						    public void run() {
								PamDialogFX2AWT swingDialog = (PamDialogFX2AWT) ((BorderPaneFX2AWT) finalNode).getAwtParent();
//								System.out.println("Dialog height before resize = " + swingDialog.getHeight());
								swingDialog.pack();
								swingDialog.repaint();
//								System.out.println("Dialog height after resize = " + swingDialog.getHeight());
						    }
						});				
						thisNode = null;
					}
				}
				
			}
			catch (Exception e) {
				System.err.println("SourcePaneFX: Problems packing SourcePanel owner window " + e.getLocalizedMessage());
			}
//		}
		}
		currentNShown = PamUtils.getNumChannels(channelsMap);
	}
	
	/**
	 * Set the current data source using the data source name
	 * @param sourceName
	 * @return true if OK
	 */
	public boolean setSource(String sourceName) {
		// search the list for the string
		PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(sourceName);
		if (dataBlock != null) {
			setSource(dataBlock);
			return true;
		}
		for (int i = 0; i < sourceList.getItems().size(); i++){
			if (sourceList.getItems().get(i).toString().equals(sourceName)) {
				sourceList.getSelectionModel().select(i); 
				//System.out.println("SourcePanel.java :: setSource 1");
				return true;
			}
		}
		
		// just select the first data block
		//sourceList.getSelectionModel().select(0);
		setSourceIndex(0);
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
		 * This CANNOT be called here every time since some dialogs 
		 * are adding additional additional datablocks more dynamically.  
		 * Dialogs should always call this themselves each time they reopen. 
		 */
//		setSourceList();
		
		/*
		 * The direct way of doing this seems to have stopped working - very worrying !
		 */
		sourceList.getSelectionModel().select(sourceBlock);
		
		showChannels();
		sourceChanged();
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
		if (sourceIndex < 0 || sourceIndex >= sourceList.getItems().size()) {
			return;
		}
		sourceList.getSelectionModel().select(sourceIndex);
		
		// setting the selected index doesn't always fire an action event (I think if the Stage is 
		// not visible yet).  Just to be sure, force the display to update now
		showChannels();
		sourceChanged();
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
	
		PamDataBlock selectedItem = null;
		if (replaceChosen) {
			selectedItem = sourceList.getSelectionModel().getSelectedItem();
		}
		
		sourceList.getItems().removeAll(sourceList.getItems());
		
		List<PamDataBlock> sl = getSourceDataBlocks();
		
		if (sl == null || sl.size() <= 0) return false;
		
		LocalisationInfo availableLocData;
		
		for (int i = 0; i < sl.size(); i++) {
			
			if (excludedBlocks.contains(sl.get(i))) continue;
			
			availableLocData = sl.get(i).getLocalisationContents();
			
			if ((localisationRequirements & availableLocData.getLocContent()) < localisationRequirements) continue;
			
			if (requiredClassType != null) {
				if (requiredClassType.isAssignableFrom(sl.get(i).getClass()) == false) continue; 
			}
			
			//System.out.println("Add "+sl.get(i).getDataName() + " to combo box");
			sourceList.getItems().add(sl.get(i));
			
		}
		if (replaceChosen && selectedItem != null) {
			sourceList.getSelectionModel().select(selectedItem);
		}
		
//		if (ownerWindow != null) {
//			try {
//				ownerWindow.pack();
//			}
//			catch (Exception e) {
//				System.out.println("Problems packing SourcePanel owner window");
//			}
//		}
		return true;
	}
	
	/**
	 * Add an additional source to the list of available datas. 
	 * @param pamDataBlock
	 */
	public void addSource(PamDataBlock pamDataBlock) {
		sourceList.getItems().add(pamDataBlock);
	}

	/**
	 * Remove an source from the list of available datas. 
	 * @param pamDataBlock
	 */
	public boolean removeSource(PamDataBlock pamDataBlock) {
		return sourceList.getItems().remove(pamDataBlock);
	}
	
	/**
	 * 
	 * @return a list of available data blocks which may be of multiple types. 
	 */
	protected List<PamDataBlock> getSourceDataBlocks() {
		ArrayList<PamDataBlock> dataBlocks = new ArrayList<>();
		for (SourceSelection sourceSel:sourceType) {
			ArrayList<PamDataBlock> sl = PamController.getInstance().getDataBlocks(sourceSel.sourceType, sourceSel.allowSubClasses);
			for (PamDataBlock db:sl) {
				if (dataBlocks.contains(db) == false) {
					dataBlocks.add(db);
				}
			}
		}
		return dataBlocks;
	}
	
	/**
	 * Return the selected data source
	 * @return source data block
	 */
	public PamDataBlock<?> getSource() {
		if (sourceList.getItems().size() == 0) {
			return null;
		}
		if (sourceList.getSelectionModel().getSelectedIndex()==-1 && sourceList.getItems().size()>0) {
			sourceList.getSelectionModel().select(0);
		}
		return sourceList.getSelectionModel().getSelectedItem();
	}
	
	/**
	 * 
	 * @return the source data block name, or null if nothing selected. 
	 */
	public String getSourceName() {
		PamDataBlock<?> source = getSource();
		if (source == null) {
			return null;
		}
		return source.getDataName();
	}

	/**
	 * 
	 * @return the source data block long name, or null if nothing selected. 
	 */
	public String getSourceLongName() {
		PamDataBlock<?> source = getSource();
		if (source == null) {
			return null;
		}
		return source.getLongDataName();
	}

	/**
	 * Get a list of selected channels
	 * @return bitmap of selected channels
	 */
	public int getChannelList() {
		PamDataBlock<?> sb = getSource();
		if (sb == null) return 0;
		// if channels are not selectable, then return the default map ...
//		int sourceChannels = sb.getChannelMap();
		int sourceChannels = sb.getSequenceMap(); // use the sequence bitmap instead of the channel bitmap, in case this is beamformer output
		if (isHasChannels() == false) return sourceChannels;
		int channelList = 0;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((sourceChannels & (1<<i)) != 0 && channelBoxes[i].isSelected()) {
				channelList += (1<<i);
				//System.out.println("1. SourcePanel getchannelList " +i+":"+ channelList);
			}
		}
		return channelList;
	}
	
	/**
	 * Set the current channel selection
	 * @param channelList bitmap of currently selected channels
	 */
	public void setChannelList(int channelList) {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			//channelBoxes[i].setSelected((channelList & (1<<i)) > 0);  //Xiao Yan Deng commented
			//System.out.println("SourcePanel setchannelList " + i +":"+ channelList);
			channelBoxes[i].setSelected((channelList & (1<<i)) > 0);  //Xiao Yan Deng
		}
		setAllChanStatus();
	}
	
	/**
	 * get the data source type for this SourcePanel
	 * @return data type of a data source
	 */
	public ArrayList<SourceSelection> getSourceTypes() {
		return sourceType;
	}
	
	/**
	 * Set the source type for this SourcePanel
	 * @param sourceType
	 */ 
	public void addSourceType(Class sourceType, boolean includeSubClasses) {
		this.sourceType.add(new SourceSelection(sourceType, includeSubClasses));
		setSourceList();
	}
	
//	public void removeSourceType(Class sourceType) {
//		this.sourceType.remove(sourceType);
//		setSourceList();
//	}
	
	/**
	 * Get the currently selected source index
	 * @return source index within the source list
	 */
	public int getSourceIndex() {
		return sourceList.getSelectionModel().getSelectedIndex();
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

	public CheckBox[] getChannelBoxes() {
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
	 * Set a required class type, e.g. that it implements
	 * GroupedDataSource
	 * @param classType (class type or interface on the Data Block)
	 */
	public void setDataBlockClassType(Class classType) {
		this.requiredClassType = classType;
		setSourceList();
	}
	
	/**
	 * Get the number of items in the list. 
	 * @return number of sources of this type of data
	 */
	public int getSourceCount() {
		return sourceList.getItems().size();
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
		sourceList.setTooltip(new Tooltip(toolTip));
	}

	/**
	 * @return the hasChannels
	 */
	public boolean isHasChannels() {
		return hasChannels;
	}

	/**
	 * @param hasChannels the hasChannels to set
	 */
	public void setHasChannels(boolean hasChannels) {
		this.hasChannels = hasChannels;
	}

	/**
	 * @return the borderTitle
	 */
	public String getBorderTitle() {
		return borderTitle;
	}

	/**
	 * @param borderTitle the borderTitle to set
	 */
	public void setBorderTitle(String borderTitle) {
		this.borderTitle = borderTitle;
	}
	
	public class SourceSelection {
		public Class sourceType; 
		public boolean allowSubClasses;
		public SourceSelection(Class sourceType, boolean allowSubClasses) {
			super();
			this.sourceType = sourceType;
			this.allowSubClasses = allowSubClasses;
		}
		
	}

	/**
//	 * Set the font of the title label whihc sits above the datablock ComboBox
	 * @param titleFont - the title label. 
	 */
	public void setTitleFont(Font titleFont) {
		titleLabel.setFont(titleFont);
		
	}
	
	/**
	 * Get the title label. 
	 * @return the title label. 
	 */
	public Label getTitleLabel() {
		return titleLabel; 
	}

	/**
	 * Get the combo box which holds the datablocks
	 * @return the datablock combo box. 
	 */
	public ComboBox<PamDataBlock> getDataBlockBox() {
		return this.sourceList; 
	
	}
	
	
    /**
     * Get the channel validator for the source pane. 
     * @return the channel validator
     */
	public PamValidator getChannelValidator() {
		return channelValidator;
	}
}
