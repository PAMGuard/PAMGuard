package PamguardMVC.dataSelector;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;

import PamController.PamController;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.SettingsButton;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetDataBlock;
import generalDatabase.SQLTypes;
import generalDatabase.clauses.PAMSelectClause;
import offlineProcessing.superdet.OfflineSuperDetFilter;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * Standard classed which are attached to a datablock and can be 
 * used to select certain types of data unit (e.g. clicks of 
 * a particular type or whistles within a particular frequency band)
 * @author Doug Gillespie
 *
 */
public abstract class DataSelector {

	private PamDataBlock pamDataBlock;
	
	private DataSelectDialog dataSelectDialog;

	private String selectorName;

	private String selectorTitle;
	
	private boolean allowScores;
	
	/**
	 * Create a data selector for a DataBlock. If allowScores is 
	 * true, then the selector MAY (but may not) offer a more complicated
	 * dialog panel that has score values for each thing selected
	 * rather than just yes / no's. 
	 * @param pamDataBlock
	 * @param selectorName
	 * @param allowScores
	 */
	public DataSelector(PamDataBlock pamDataBlock, String selectorName, boolean allowScores) {
		super();
		this.pamDataBlock = pamDataBlock;
		this.selectorName = selectorName;
		this.allowScores = allowScores;
	}


	/**
	 * Get a database clause which can be used with a database Query for this 
	 * data type. These may easily end up in an inner join, so in the clause, spell
	 * out column names in full. 
	 * @return a database clause. 
	 */
	public PAMSelectClause getSQLSelectClause(SQLTypes sqlTypes) {
		return null;
	}
	
	/**
	 * Set selection parameters from centralised storage. 
	 * @param dataSelectParams
	 */
	abstract public void setParams(DataSelectParams dataSelectParams); 
	
	/**
	 * Get selection parameters for more organised centralised storage. 
	 * This must never be null since all selectors have an enable / disable options added
	 * to them automatically in their dialogs. 
	 * @return
	 */
	abstract public DataSelectParams getParams();

	/**
	 * 
	 * @return a dialog panel which can be used in a wider dialog
	 */
	abstract public PamDialogPanel getDialogPanel();
	

	/**
	 * 
	 * @return a FX pane which can be used in a wider dialog
	 */
	abstract public DynamicSettingsPane<Boolean> getDialogPaneFX();
	
	/**
	 * Get a menu item for the data selector that can be easily added
	 * to any other menu. 
	 * @param parentFrame
	 * @return menu item
	 */
	public JMenuItem getMenuItem(Window parentFrame, DataSelectorChangeListener changeListener) {
		if (parentFrame == null) {
			parentFrame = PamController.getMainFrame();
		}
		Window localWin = parentFrame;
		DataSelectorChangeListener localChangeListener = changeListener;
		JMenuItem menuItem = new JMenuItem("Data selection ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean ok = showSelectDialog(localWin);
				if (ok && changeListener != null) {
					changeListener.selectorChange(DataSelector.this);
				}
			}
		});
		return menuItem;
	}

	public final boolean showSelectDialog(Window frame) {
		if (dataSelectDialog == null || dataSelectDialog.getOwner() != frame) {
			dataSelectDialog = new DataSelectDialog(frame,pamDataBlock, this, null);
		}
//		selectDialogToOpen();
		boolean ok = dataSelectDialog.showDialog();
//		selectDialogClosed(ok);
		return ok;
	}
	
	/**
	 * Score a PAMDataUnit. this is used in preference 
	 * to a boolean select function so that the user can add different
	 * return flags. Generally 0 indicates false. 
	 * @param pamDataUnit - the input data unit. 
	 * @return score of data out. 0 usually indicates false. 
	 */
	public abstract double scoreData(PamDataUnit pamDataUnit);
	
//	/**
//	 * The DialogPanel will have a setParams, 
//	 * but may need to rebuild the actual dialog panel
//	 * content at this point, e.g. with a new set of check boxes of 
//	 * available types to select. 
//	 */
//	public void selectDialogToOpen() {
//	}
//	
//	/**
//	 * Not needed to retrieve parameters from the 
//	 * dialog panel. Probably never needed in fact.  
//	 * @param ok ok if the dialog closed correctly 
//	 */
//	public void selectDialogClosed(boolean ok) {
//	}

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * This is the name used to identify the data selector in a hash table
	 * of all data selectors for a specific datablock
	 * so should be the unique name of the display in order that each display 
	 * can use a different selection.  
	 * @return the selectorName
	 */
	public String getSelectorName() {
		return selectorName;
	}
	
	/**
	 * This is a title for the data selector which can be used in dialogs. This can be 
	 * different to the name, since the name is generally the display, which we know anyway.
	 * Particularly with complicated compound data selectors, it's useful to have a more 
	 * informative name for various sub-components. 
	 * @return title to use in dialog components. 
	 */
	public String getSelectorTitle() {
		if (selectorTitle != null) {
			return selectorTitle;
		}
		else {
			return getSelectorName();
		}
	}
	
	/**
	 * This is a title for the data selector which can be used in dialogs. This can be 
	 * different to the name, since the name is generally the display, which we know anyway.
	 * Particularly with complicated compound data selectors, it's useful to have a more 
	 * informative name for various sub-components. 
	 * @param selectorTitle
	 */
	public void setSelectorTitle(String selectorTitle) {
		this.selectorTitle = selectorTitle;
	}
	
	/**
	 * Need a longer more unique name when making compound data selectors, 
	 * particularly those which are pulling in multiple similar super detections which 
	 * might be from different datablocks, but might be given the same name. 
	 * @return
	 */
	public String getLongSelectorName() {
		if (pamDataBlock == null) {
			return getSelectorName();
		}
		else {
			return pamDataBlock.getLongDataName() + " : " + getSelectorName();
		}
	}

	/**
	 * @return the allowScores
	 */
	public boolean isAllowScores() {
		return allowScores;
	}

	/**
	 * Create a settings type button that can be inserted into a
	 * larger dialog. 
	 * @param parentWindow
	 */
	public JButton getDialogButton(Window parentWindow) {
		JButton button = new SettingsButton();
		button.addActionListener(new ShowSettingsButton(parentWindow));
		button.setToolTipText("Data selection options for " + getSelectorTitle());
		return button;
	}
	
	private class ShowSettingsButton implements ActionListener {
		private Window parentWindow;
		/**
		 * @param parentWindow
		 */
		public ShowSettingsButton(Window parentWindow) {
			super();
			this.parentWindow = parentWindow;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			showSelectDialog(parentWindow);
		}
		
	}
	
	
	
}
