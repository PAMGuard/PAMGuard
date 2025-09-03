package PamguardMVC.datakeeper;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import pamViewFX.PamSettingsMenuPane;

/**
 * Class to manage minimum data storage times for all datablocks. 
 * @author dg50
 *
 */
public class DataKeeper implements PamSettings {

	private static DataKeeper singleInstance;
	
	private DataKeeperSettings keeperSettings = new DataKeeperSettings();
	
	private DataKeeper() {
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	public static DataKeeper getInstance() {
		if (singleInstance == null) {
			singleInstance = new DataKeeper();
		}
		return singleInstance;
	}
	
	public void setAllKeepTimes() {
		PamController pamController = PamController.getInstance();
		if (pamController == null) {
			return;
		}
		ArrayList<PamDataBlock> allData = pamController.getDataBlocks();
		for (PamDataBlock aDataBlock:allData) {
			setDataBlockKeepTime(aDataBlock);
		}
	}
	
	/**
	 * Should default behaviour for all blocks be to clear them at startup ?
	 * @return
	 */
	public boolean isClearAtStart() {
		return keeperSettings.isClearAtStart();
	}
	
	/**
	 * 
	 * Should default behaviour for all blocks be to clear them at startup ?
	 * @param clear
	 */
	public void setClearAtStart(boolean clear) {
		keeperSettings.setClearAtStart(clear);
	}
	
	public JMenuItem getSwingMenuItem(Window window) {
		JMenuItem menuItem = new JMenuItem("Internal Data Storage ...");
		menuItem.setToolTipText("<html>Control how long data are stored in memory before they are deleted" +
		"<p>(This does not affect storage in binary files or the database)</html>");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showKeeperDialog(window);
			}
		});
		return menuItem;
	}

	private void setDataBlockKeepTime(PamDataBlock aDataBlock) {
		Integer tSecs = keeperSettings.getKeepTimeSeconds(aDataBlock.getLongDataName());
		if (tSecs != null) {
			aDataBlock.setNaturalLifetimeMillis(tSecs*1000);
		}
	}
	
	/**
	 * Get how long a data is to be kept for in seconds. 
	 * @param aDataBlock data block reference
	 * @return time in seconds. 
	 */
	public int getKeepTimeSeconds(PamDataBlock aDataBlock) {
		Integer tSecs = keeperSettings.getKeepTimeSeconds(aDataBlock.getLongDataName());
		if (tSecs == null) {
			tSecs = 0;
		}
		return tSecs;
	}
	
	/**
	 * Set how long a data is to be kept for in seconds. 
	 * @param aDataBlock data block reference
	 * @param seconds time in seconds
	 */
	public void setKeepTimeSeconds(PamDataBlock aDataBlock, int seconds) {
		keeperSettings.setKeepTimeSeconds(aDataBlock.getLongDataName(), seconds);
	}
	
	public boolean showKeeperDialog(Window window) {
		boolean ans = DataKeepDialog.showDialog(window);
		if (ans) {
			setAllKeepTimes();
		}
		return ans;
	}

	@Override
	public String getUnitName() {
		return "Data Keep Control";
	}

	@Override
	public String getUnitType() {
		return "Data Keep Control";
	}

	@Override
	public Serializable getSettingsReference() {
		return keeperSettings;
	}

	@Override
	public long getSettingsVersion() {
		return DataKeeperSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		keeperSettings = (DataKeeperSettings) pamControlledUnitSettings.getSettings();
		return true;
	}
}
