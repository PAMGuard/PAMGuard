package NMEA;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import PamController.PamController;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;

/**
 * Class which provided an editable ComboBNox for selecting
 * NMEA string. 
 * @author Doug
 *
 */
public class NMEAStringSelector {
	
	private JComboBox<String> nmeaStrings;
	private NMEADataBlock currentDataBlock;
	private NMEAObserver nmeaObserver;
	private JLabel exampleString;
	
	public NMEAStringSelector() {
		nmeaStrings = new JComboBox<String>();
		exampleString = new JLabel("   ");
		nmeaStrings.setEditable(true);
		nmeaObserver = new NMEAObserver();
	}

	public JComponent getComponent() {
		return nmeaStrings;
	}
	
	public JLabel getExampleLabel() {
		return exampleString;
	}

	public void setNMEAModule(String moduleName) {
		NMEAControl nmeaControl = (NMEAControl) PamController.getInstance().findControlledUnit(NMEAControl.nmeaUnitType, moduleName);
		NMEADataBlock nmeaDataBlock = null;
		if (nmeaControl != null) {
			nmeaDataBlock = nmeaControl.getNMEADataBLock();
		}
		setNMEADataBlock(nmeaDataBlock);
	}
	
	

	private void setNMEADataBlock(NMEADataBlock nmeaDataBlock) {
		if (nmeaDataBlock == currentDataBlock) {
			return;
		}
		if (currentDataBlock != null) {
			currentDataBlock.deleteObserver(nmeaObserver);
		}
		currentDataBlock = nmeaDataBlock;
		if (nmeaDataBlock != null) {
			nmeaDataBlock.addObserver(nmeaObserver);
			nmeaStrings.removeAllItems();
		}
	}
	
	private void newNMEAData(NMEADataUnit nmeaDataUnit) {
		String strName = nmeaDataUnit.getStringId();
		// strip off the first $
		if (strName == null || strName.length() < 2) {
			return;
		}
		strName = strName.substring(1);
		if (strName.equals(nmeaStrings.getSelectedItem())) {
			exampleString.setText(new String(nmeaDataUnit.getCharData()));
		}
		// and don't add it if it's already in the list. 
		if (haveItem(strName)) {
			return;
		}
		nmeaStrings.addItem(strName);
	}
	
	/**
	 * work out if list already has this item
	 * @param strName
	 * @return
	 */
	private boolean haveItem(String strName) {
		int n = nmeaStrings.getItemCount();
		for (int i = 0; i < n; i++) {
			if (strName.equals(nmeaStrings.getItemAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the selected NMEA string
	 * @param selString string to select
	 */
	public void setSelectedString(String selString) {
		nmeaStrings.setSelectedItem(selString);
	}
	
	/**
	 * 
	 * @return selected NMEA string, or null.
	 */
	public String getSelectedString() {
		return (String) nmeaStrings.getSelectedItem();
	}

	private class NMEAObserver extends PamObserverAdapter {

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			if (NMEADataUnit.class.isAssignableFrom(arg.getClass())) {
				newNMEAData((NMEADataUnit) arg);
			}
		}
		
		@Override
		public String getObserverName() {
			return "NMEA String List";
		}
	}

	/**
	 * Enable to combo box of available NMEA string. 
	 * @param enabled boolean value to enable or disable the combo box. 
	 */
	public void setEnabled(boolean enabled) {
		nmeaStrings.setEnabled(enabled);
	}
}
