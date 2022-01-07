package PamView;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * Class to manage symbols for various PAMGAURD displays. Any part of PAMGAURD
 * using a PamSymbol may register with the PamSymbolManager and it's symbol and
 * description will appear in the Display / Symbols menu from where it is
 * possible to change the symbol.
 * 
 * Symbol choices are stored in the serialised settings file along with other
 * PAMGAURD configuration information.
 * 
 * Being replaced in 2017 by a newer and better symbol management system which 
 * allows for many more options. 
 * 
 * @author Douglas Gillespie
 * 
 */
public class PamOldSymbolManager implements PamSettings, Serializable {

	private static PamOldSymbolManager singleInstance;

	private ArrayList<ManagedSymbol> managedSymbols;

	private ArrayList<SymbolSettingsStoreUnit> oldSettings;

	private boolean[] usedOldSettings;

//	private Frame guiFrame;

	/**
	 * Private constructor - singleton class
	 * 
	 */
	private PamOldSymbolManager() {
		managedSymbols = new ArrayList<ManagedSymbol>();
		PamSettingManager.getInstance().registerSettings(this);
	}

	static private PamOldSymbolManager getInstance() {
		if (singleInstance == null) {
			singleInstance = new PamOldSymbolManager();
		}
		return singleInstance;
	}

	/**
	 * Add a managed symbol - wherever you want to use the symbol manager must
	 * implement the ManagedSymbol interface
	 * 
	 * @see ManagedSymbol
	 * @param managedSymbol
	 *            class with the symbol to be managed
	 */
	public void addManagesSymbol(ManagedSymbol managedSymbol) {
		if (managedSymbols.contains(managedSymbol) == false) {
			managedSymbols.add(managedSymbol);

			if (oldSettings != null) {
				for (int i = 0; i < oldSettings.size(); i++) {
					if (i < usedOldSettings.length && usedOldSettings[i])
						continue;
					if (managedSymbol.getSymbolInfo().getDescription().equals(
							oldSettings.get(i).getDescription())) {
						managedSymbol.setPamSymbol(oldSettings.get(i)
								.getPamSymbol());
						usedOldSettings[i] = true;
					}
				}
			}
		}
	}

	/**
	 * Fix primarily inserted to deal with developers who continually recreate
	 * PamProcesses. when the process is recreated, it will also recreate it's
	 * output data block and associated overlaydraw which will end up getting
	 * it's default setting back again. So when a managedSymbol is changed, it
	 * should update the oldSettings list, so that when a new instance is
	 * created, it get's the update.
	 * 
	 * @param managedSymbol
	 */
	public void updateManagedSymbol(ManagedSymbol managedSymbol) {
		if (managedSymbols.contains(managedSymbol) == false) {
			return;
		}

		if (oldSettings != null) {
			for (int i = 0; i < oldSettings.size(); i++) {
				if (i < usedOldSettings.length && usedOldSettings[i] == false)
					continue;
				if (managedSymbol.getSymbolInfo().getDescription().equals(
						oldSettings.get(i).getDescription())) {
					//managedSymbol.setPamSymbol(oldSettings.get(i).getPamSymbol
					// ());
					oldSettings.get(i).setPamSymbol(
							managedSymbol.getPamSymbol());
					usedOldSettings[i] = false;
				}
			}
		}

	}

	/**
	 * Remove an object from the symbol manager.
	 * 
	 * @param managedSymbol
	 *            instance to be removed from the list. Note that this is of
	 *            type Object so that it can be called from all cleaned up
	 *            DataBlocks overlay draw's whether they are using ManagedSymbol
	 *            or not.
	 */
	public void removeManagedSymbol(Object managedSymbol) {
		int ind = managedSymbols.indexOf(managedSymbol);
		if (ind >= 0) {
			updateManagedSymbol((ManagedSymbol) managedSymbol);
			managedSymbols.remove(managedSymbol);
		}
	}

	/**
	 * constructs the menu for the main GUI display menu
	 * 
	 * @return menu item for inclusion in main GUI menu
	 */
	public JMenuItem getMenu(Frame guiFrame) {
		if (managedSymbols.size() == 0)
			return null;
		JMenu menu = new JMenu("Symbols");
		JMenuItem menuItem;
		PamSymbol symbol;
		// PamKeyItem keyItem;
		for (int i = 0; i < managedSymbols.size(); i++) {
			// keyItem = managedSymbols.get(i).getMenuKeyItem();
			// if (keyItem != null) {
			// menuItem = new JMenuItem(keyItem.getIcon(PamKeyItem.KEY_SHORT,
			// 0))
			// }
			// else {
			symbol = managedSymbols.get(i).getPamSymbol().clone();
			symbol.setHeight(0);
			symbol.setWidth(0);
			symbol.setIconHorizontalAlignment(PamSymbol.ICON_HORIZONTAL_LEFT);
			symbol.setIconVerticalAlignment(PamSymbol.ICON_VERTICAL_MIDDLE);
			menuItem = new JMenuItem(managedSymbols.get(i).getSymbolInfo()
					.getDescription(), symbol);
			// }
			menuItem.addActionListener(new MenuAction(guiFrame, managedSymbols.get(i)));
			menu.add(menuItem);
		}
		return menu;
	}

	/**
	 * Menu action listener
	 * 
	 * @author Douglas Gillespie
	 * 
	 */
	private class MenuAction implements ActionListener {

		private ManagedSymbol managedSymbol;
		
		private Frame guiFrame;

		public MenuAction(Frame guiFrame, ManagedSymbol managedSymbol) {
			this.guiFrame = guiFrame;
			this.managedSymbol = managedSymbol;
		}

		public void actionPerformed(ActionEvent arg0) {

			changeSymbol(guiFrame, managedSymbol);

		}

	}

	/**
	 * Called by the menu actionlistener.
	 * 
	 * @param managedSymbol
	 */
	private void changeSymbol(Frame guiFrame, ManagedSymbol managedSymbol) {
		// pop up the symbol dialog...
		PamSymbol newSymbol = PamSymbolDialog.show(guiFrame, managedSymbol
				.getPamSymbol());
		if (newSymbol != null) {
			managedSymbol.setPamSymbol(newSymbol);
			PamController.getInstance().notifyModelChanged(
					PamControllerInterface.CHANGED_DISPLAY_SETTINGS);
			updateManagedSymbol(managedSymbol);
		}
	}

	public Serializable getSettingsReference() {

		ArrayList<SymbolSettingsStoreUnit> s = new ArrayList<SymbolSettingsStoreUnit>();

		for (int i = 0; i < managedSymbols.size(); i++) {
			s.add(new SymbolSettingsStoreUnit(managedSymbols.get(i)
					.getSymbolInfo().getDescription(), managedSymbols.get(i)
					.getPamSymbol()));
		}

		return s;
	}

	public long getSettingsVersion() {
		return SymbolSettingsStoreUnit.serialVersionUID;
	}

	public String getUnitName() {
		return "PamSymbolManager";
	}

	public String getUnitType() {
		return "PamSymbolManager";
	}

	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			oldSettings = (ArrayList<SymbolSettingsStoreUnit>) pamControlledUnitSettings
					.getSettings();
			if (oldSettings != null && oldSettings.size() > 0) {
				usedOldSettings = new boolean[oldSettings.size()];
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

}
