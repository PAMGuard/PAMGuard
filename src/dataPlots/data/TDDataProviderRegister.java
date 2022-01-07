package dataPlots.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JMenuItem;

import PamguardMVC.PamDataBlock;

/**
 * Static holder of information about plottable data. 
 * @author Doug Gillespie
 *
 */
public class TDDataProviderRegister {

	private ArrayList<TDDataProvider> dataProviders = new ArrayList<TDDataProvider>();
	
	private static TDDataProviderRegister singleInstance;
	
	public static TDDataProviderRegister getInstance() {
		if (singleInstance == null) {
			singleInstance = new TDDataProviderRegister();
		}
		return singleInstance;
	}
	/**
	 * Register a source of plottable data. 
	 * @param dataInfo info to register
	 */
	synchronized public void registerDataInfo(TDDataProvider dataInfo) {
		dataProviders.add(dataInfo);
	}
	
	/**
	 * Remove something from the register
	 * @param dataInfo info to remove from register
	 */
	synchronized public void unRegisterDataInfo(TDDataProvider dataInfo) {
		dataProviders.remove(dataInfo);
	}

	/**
	 * Remove all items associated with a particular data block from 
	 * the register. 
	 * @param dataBlock
	 */
	synchronized public void unRegisterDataInfo(PamDataBlock dataBlock) {
		ListIterator<TDDataProvider> it = dataProviders.listIterator();
		while (it.hasNext()) {
			TDDataProvider di = it.next();
			if (di.getDataBlock() == dataBlock) {
				it.remove();
			}
		}
	}
	
	/**
	 * find a provider based on it's class and it's name. <br>
	 * This is mainly called when deserialising settings and recreating 
	 * old plots. 
	 * @param providerClass class
	 * @param providerName name 
	 * @return provider class or null. 
	 */
	synchronized public TDDataProvider findDataProvider(Class providerClass, String providerName) {
		for (TDDataProvider aProvider:dataProviders) {
			if (aProvider.getClass() == providerClass && aProvider.getName().equals(providerName)) {
				return aProvider;
			}
		}
		return null;
	}
	
	/**
	 * Add menu items to an existing menu. Done this way since
	 * we may want to add them to a normal menu or to a popup menu. 
	 * @param aMenu
	 * @return number of items added.
	 */
	synchronized public int addMenuItems(JMenuItem aMenu, TDInfoMonitor infoMonitor) {
		int n = 0;
		for (TDDataProvider aProvider:dataProviders) {
			JMenuItem menuItem = new JMenuItem(aProvider.getName());
			aMenu.add(menuItem);
			menuItem.addActionListener(new ProviderAction(aProvider, infoMonitor));
			
		}
		return n;
	}
	
	private class ProviderAction implements ActionListener {
		private TDDataProvider dataProvider;
		private TDInfoMonitor infoMonitor;

		public ProviderAction(TDDataProvider dataProvider, TDInfoMonitor infoMonitor) {
			super();
			this.dataProvider = dataProvider;
			this.infoMonitor = infoMonitor;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			infoMonitor.selectProvider(dataProvider);
		}
	}
	
	/**
	 * @return the complete list of data infos
	 */
	synchronized ArrayList<TDDataProvider> getDataInfos() {
		return dataProviders;
	}

//	/**
//	 * 
//	 * @param dataUnits data units to search for. 
//	 * @return a partial list of data infos having the specified data units. 
//	 */
//	synchronized ArrayList<TDDataInfo> getDataInfos(String dataUnits) {
//		ArrayList<TDDataProvider> someInfos = new ArrayList<TDDataProvider>();
//		ListIterator<TDDataProvider> it = dataInfos.listIterator();
//		while (it.hasNext()) {
//			TDDataProvider di = it.next();
//			if (di.hasDataUnits(dataUnits)) {
//				someInfos.add(di);
//			}
//		}
//		return someInfos;
//	}
}
