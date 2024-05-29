package PamView.paneloverlay;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import PamController.PamController;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.DataBlockNameComparator;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;

/**
 * Provides functions for managing lists of overlays which might
 * be used with a specific GeneralProjector, i.e. any data block 
 * that might draw on that projector. Provides checkable menu lists, 
 * dialog panels with check boxes and options, etc. It will also handle storing 
 * of selections within lists of data, and provide names to 
 * dataselectors within datablocks. 
 * @author Doug Gillespie
 *
 */
public abstract class OverlayDataManager<TOverlayInfoType extends OverlayDataInfo> implements OverlayDataObserver {

//	private ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
//	private ImageIcon settingsIconNot = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmallWhite.png"));
	
//	private static final FontIcon settingsIcon =  FontIcon.of(MaterialDesignC.COG, 16, Color.DARK_GRAY);
//	private static final FontIcon settingsIconNot =  FontIcon.of(MaterialDesignC.COG, 16, Color.WHITE);


	private OverlaySwingPanel swingPanel;

	private ParameterType[] parameterTypes;

	private ParameterUnits[] parameterUnits;

	/**
	 * @param generalProjector
	 */
	public OverlayDataManager(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		super();
		this.parameterTypes = parameterTypes;
		this.parameterUnits = parameterUnits;
	}
	
	/**
	 * Get a list of all datablocks that can use this projector
	 * @param sortAlphabetical sort the list alphabetically. 
	 * @return a list of data blocks. 
	 */
	public List<PamDataBlock> listDataBlocks(boolean sortAlphabetical) {
		ArrayList<PamDataBlock> dataBlocks = new ArrayList<>();
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock db:allDataBlocks) {
			if (db.getOverlayDraw() == null) {
				continue;
			}
			if (canDraw(db)) {
				dataBlocks.add(db);
			}
		}
		if (sortAlphabetical) {
			// sort the list into alphabetical order. 
			Collections.sort(dataBlocks, new DataBlockNameComparator());
		}
		return dataBlocks;
	}
	
	private boolean canDraw(PamDataBlock dataBlock) {
		if (parameterTypes == null) {
			return true;
		}
		try {
			return dataBlock.canDraw(parameterTypes, parameterUnits);
		}
		catch (Exception e) {
			return true;
		}
	}
	
	public OverlaySwingPanel getSwingPanel(Window parentWindow) {
//		if (swingPanel == null) {
			swingPanel = new OverlaySwingPanel(this, parentWindow);
//		}
		return swingPanel;
	}
	
	/**
	 * 
	 * @return a String name for a dataselector associated with this. 
	 * this will generally be the name of an associated PamControlledUnit. 
	 */
	public abstract String getDataSelectorName();
	
	/**
	 * Get the OverlayDataInfo associated with these items - it's possible that 
	 * these will have been extended to contain additional information so don't handle them 
	 * too directly in the abstract class. . 
	 * @param dataBlock associated datablock. 
	 * @return an OverlayDataInfo - must not be null, so create one if needed. 
	 */
	public abstract TOverlayInfoType getOverlayInfo(PamDataBlock dataBlock);
	
	/**
	 * Add all items associated with the list to a 
	 * @param menu
	 * @return
	 */
	public int addSelectionMenuItems(JComponent menu, Window awtWindow, boolean sortAlphabetical, boolean allowScores, boolean includeSymbolManagement) {
		int nAdded = 0;
		List<PamDataBlock> datas = listDataBlocks(sortAlphabetical);
		OverlayCheckboxMenuItem checkMenuItem;
		for (PamDataBlock dataBlock:datas) {
			OverlayDataInfo dataInfo = getOverlayInfo(dataBlock);
			if (dataInfo == null) {
				dataInfo = new OverlayDataInfo(dataBlock.getDataName());
			}
			DataSelector dataSel = dataBlock.getDataSelector(getDataSelectorName(), allowScores);
			checkMenuItem = new OverlayCheckboxMenuItem(dataBlock, getDataSelectorName(), dataInfo.select, includeSymbolManagement);
//			if (dataSel != null) {
//				if (dataInfo != null && dataInfo.select) {
//					checkMenuItem = new OverlayCheckboxMenuItem(dataBlock.getDataName(), settingsIcon);
//				}
//				else {
//					checkMenuItem = new OverlayCheckboxMenuItem(dataBlock.getDataName(), settingsIconNot);
//				}
//			}
//			else {
//				checkMenuItem = new OverlayCheckboxMenuItem(dataBlock.getDataName());
//			}
//			try {
//				checkMenuItem.setToolTipText(dataBlock.getParentProcess().getPamControlledUnit().getUnitName());
//			}
//			catch (NullPointerException e) {				
//			}
			checkMenuItem.setSelected(dataInfo.select);
			PamSymbolChooser symbolChooser = null;
			PamSymbolManager symbolManager = dataBlock.getPamSymbolManager();
			if (symbolManager != null && getProjector() != null) {
				symbolChooser = symbolManager.getSymbolChooser(getDataSelectorName(), getProjector());
			}
			checkMenuItem.addActionListener(new OverlayCheckboxMenuSelect(awtWindow, this, dataBlock, dataInfo, dataSel, symbolChooser));
			
			menu.add(checkMenuItem);
			nAdded++;
		}
		return nAdded;
	}

	protected GeneralProjector getProjector() {
		/**
		 * Not 100% sure why, but this needs to return non-null if 
		 * the overlay symbol and data select dialog is to be built correctly. 
		 */
		return null;
	}

	/**
	 * @return the parameterTypes
	 */
	public ParameterType[] getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * @return the parameterUnits
	 */
	public ParameterUnits[] getParameterUnits() {
		return parameterUnits;
	}
	
}
