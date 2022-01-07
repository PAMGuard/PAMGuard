package clipgenerator;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;
import clipgenerator.clipDisplay.ClipDisplayDecorations;
import clipgenerator.clipDisplay.ClipDisplayPanel;
import clipgenerator.clipDisplay.ClipDisplayParent;
import clipgenerator.clipDisplay.ClipDisplayUnit;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.rawClipDataPlot.ClipPlotProviderFX;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.handler.ManualAnnotationHandler;

/**
 * The clip generator is used to generate short sound clips around detections. 
 * In principle, it can be triggered by just about any dataunit which is a
 * subclass of AcousticDataUnit, i.e. one having both a start time and a 
 * duration. 
 * <p>
 * The clip generator configuration contains a budget of how many clips it
 * can make for each possible trigger and will, based on usage, decide in a semi
 * random way whether or not to make a clip on the arrival of each detection. 
 * <p>
 * Trigger specific settings also include a pre and post sample time.
 * <p>
 * It would be good to add clips as annotations to other data units, but of course 
 * clips only get made a second or two after the data unit, which will already have been 
 * stored, so can only work if storage of data units is delayed somehow. Not yet !
 * @author Doug Gillespie
 *
 */

public class ClipControl extends PamControlledUnit implements PamSettings, ClipDisplayParent{

	protected ClipProcess clipProcess;
	
	protected ClipSettings clipSettings = new ClipSettings();

	private boolean initializationComplete;

//	private ClipTabPanel clipTabPanel;
	
	private UserDisplayProvider userDisplayProvider;
	
	public ClipControl(String unitName) {
		
		super("Clip Generator", unitName);

		addPamProcess(clipProcess = new ClipProcess(this));
		
		PamSettingManager.getInstance().registerSettings(this);
		
//		clipTabPanel = new ClipTabPanel(this);
		
		userDisplayProvider = new ClipDisplayProvider();
		UserDisplayControl.addUserDisplayProvider(userDisplayProvider);
		//register click detector for the javafx display. 
		TDDataProviderRegisterFX.getInstance().registerDataInfo(new ClipPlotProviderFX(this, getClipDataBlock()));
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		ManualAnnotationHandler annotationHandler = clipProcess.getManualAnnotaionHandler();
		
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new ClipSettingsMenu(parentFrame));
		
		if (annotationHandler != null) {
			JMenu menu = new JMenu(getUnitName());
			menu.add(menuItem);
			menu.add(annotationHandler.getDialogMenuItem(parentFrame));
			return menu;
		}
		else {
			return menuItem;
		}
	}
	
	private class ClipSettingsMenu implements ActionListener {

		private Frame parentFrame;
		
		/**
		 * @param parentFrame
		 */
		public ClipSettingsMenu(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ClipSettings newSettings = ClipDialog.showDialog(parentFrame, ClipControl.this);
			if (newSettings != null) {
				clipSettings = newSettings.clone();
				clipProcess.subscribeDataBlocks();
				clipProcess.getClipDataBlock().getPamSymbolManager().clearChoosers();
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDisplayMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Display Settings ...");
		menuItem.addActionListener(new DisplayOptions(parentFrame));
		return menuItem;
	}
	
	private class DisplayOptions implements ActionListener {
		private Frame parentFrame;
		public DisplayOptions(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			displayOptions(parentFrame);
		}
		
	}

	public void displayOptions(Frame parentFrame) {
		ClipSettings newSettings = ClipDisplayDialog.showDialog(parentFrame, this);
		if (newSettings != null) {
			clipSettings = newSettings;
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return clipSettings;
	}

	@Override
	public long getSettingsVersion() {
		return ClipSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		clipSettings = ((ClipSettings) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch(changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initializationComplete = true;
			checkTriggerBlocks();
			clipProcess.subscribeDataBlocks();
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			if (initializationComplete) {
				checkTriggerBlocks(); 
				clipProcess.subscribeDataBlocks();
			}
			break;
		}
	
	}

	/**
	 * Check the list of blocks which can trigger 
	 * a clip and make sure there is a settings group for 
	 * each datablock. 
	 */
	private void checkTriggerBlocks() {
		// remove ones we've not got and add ones we have. 
		ArrayList<PamDataBlock> newList = getPotentialTriggers();
		ArrayList<ClipGenSetting> newGenSet = new ArrayList<>();
		/*
		 * Build a new list of sets, using existing ones if possible. 
		 * Otherwise creating new
		 */
		for (PamDataBlock aBlock:newList) {
			ClipGenSetting foundSet = clipSettings.findClipGenSetting(aBlock.getDataName());
			if (foundSet != null) {
				newGenSet.add(foundSet);
			}
			else {
				ClipGenSetting genSet = new ClipGenSetting(aBlock.getDataName());
				if (aBlock == clipProcess.getClipSpectrogramMarkDataBlock()) {
					genSet.useDataBudget = false;
				}
				newGenSet.add(genSet);
			}
		}
		/*
		 * Now clear the settings and add the new list. 
		 */
		clipSettings.clearClipGenSettings();
		for (ClipGenSetting genSet:newGenSet) {
			clipSettings.addClipGenSettings(genSet);
		}
	}
	
	/**
	 * Get a list of datablocks which might trigger a clip generation. 
	 * @return list of triggering data blocks. 
	 */
	public ArrayList<PamDataBlock> getPotentialTriggers() {
		ArrayList<PamDataBlock> acousticDataBlocks = PamController.getInstance().getDataBlocks(AcousticDataUnit.class, true);
		acousticDataBlocks.add(0, clipProcess.getClipSpectrogramMarkDataBlock());
		ArrayList<PamDataBlock> clipBlocks = new ArrayList<>();
		for (PamDataBlock aBlock:acousticDataBlocks) {
			if (aBlock.isCanClipGenerate()) {
				clipBlocks.add(aBlock);
			}
		}
		return clipBlocks;		
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#getTabPanel()
	 */
//	@Override
//	public PamTabPanel getTabPanel() {
//		return clipTabPanel;
//	}

	/**
	 * @return the clipProcess
	 */
	public ClipProcess getClipProcess() {
		return clipProcess;
	}
	
	private class ClipDisplayProvider implements UserDisplayProvider {

		@Override
		public String getName() {
			return getUnitName() + " display";
		}

		@Override
		public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
			return generateNewPanel();
		}

		@Override
		public Class getComponentClass() {
			return ClipDisplayPanel.class;
		}

		@Override
		public int getMaxDisplays() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean canCreate() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void removeDisplay(UserDisplayComponent component) {
			// TODO Auto-generated method stub
			
		}
		
	}

	public ClipDisplayPanel generateNewPanel() {
		ClipDisplayPanel aPanel = new ClipDisplayPanel(this);
		return aPanel;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#removeUnit()
	 */
	@Override
	public boolean removeUnit() {
		UserDisplayControl.removeDisplayProvider(userDisplayProvider);
		return super.removeUnit();
	}

	@Override
	public ClipDataBlock getClipDataBlock() {
		return clipProcess.getClipDataBlock();
	}

	@Override
	public String getDisplayName() {
		return getUnitName();
	}

	/* (non-Javadoc)
	 * @see clipgenerator.clipDisplay.ClipDisplayParent#getClipDecorations(clipgenerator.clipDisplay.ClipDisplayUnit)
	 */
	@Override
	public ClipDisplayDecorations getClipDecorations(
			ClipDisplayUnit clipDisplayUnit) {
		return new ClipDisplayDecorations(clipDisplayUnit);
	}
	
	/**
	 * Find the clip generator settings for a specific data stream. 
	 * 
	 * @param dataName data name for the data block. 
	 * @return clip generator settings, or null if none active. 
	 */
	public ClipGenSetting findClipGenSetting(String clipDataName) {
		return clipSettings.findClipGenSetting(clipDataName);
	}
	

	/* (non-Javadoc)
	 * @see clipgenerator.clipDisplay.ClipDisplayParent#displaySettingChange()
	 */
	@Override
	public void displaySettingChange() {
		// TODO Auto-generated method stub
		
	}

	public ClipSettings getClipSettings() {
		return clipSettings;
	}

	public PamDataUnit findTriggerDataUnit(ClipDataUnit clipDataUnit) {
		String trigName = clipDataUnit.triggerName;
		long trigMillis = clipDataUnit.triggerMilliseconds;
		long startMillis = clipDataUnit.getTimeMilliseconds();
		PamDataBlock<PamDataUnit> dataBlock = findTriggerDataBlock(trigName);
		if (dataBlock == null) {
			return null;
		}
		return dataBlock.findDataUnit(trigMillis, 0);
	}

	private String lastFoundName;
	private PamDataBlock<PamDataUnit> lastFoundBlock;
	private PamDataBlock<PamDataUnit> findTriggerDataBlock(String dataName) {
		if (dataName == null) {
			return null;
		}
		if (dataName.equals(lastFoundName)) {
			return lastFoundBlock;
		}
		PamDataBlock<PamDataUnit> dataBlock = PamController.getInstance().getDetectorDataBlock(dataName);
		if (dataBlock == null) {
			return null;
		}
		lastFoundName = new String(dataName);
		lastFoundBlock = dataBlock;
		return dataBlock;
	}




}
