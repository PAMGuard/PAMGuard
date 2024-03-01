package dataPlotsFX.data;

import java.util.ArrayList;
import java.util.ListIterator;

import PamguardMVC.PamDataBlock;

/**
 * Static holder of information about plottable data. 
 * @author Doug Gillespie
 * 
 */
@SuppressWarnings("rawtypes")
public class TDDataProviderRegisterFX {

	private ArrayList<TDDataProviderFX> dataProviders = new ArrayList<TDDataProviderFX>();
	
	private static TDDataProviderRegisterFX singleInstance;
	
	public static TDDataProviderRegisterFX getInstance() {
		if (singleInstance == null) {
			singleInstance = new TDDataProviderRegisterFX();
		}
		return singleInstance;
	}
	
	/**
	 * Register a source of plottable data. 
	 * @param dataInfo info to register
	 */
	synchronized public void registerDataInfo(TDDataProviderFX dataInfo) {
		dataProviders.add(dataInfo);
	}
	
	/**
	 * Remove something from the register
	 * @param dataInfo info to remove from register
	 */
	synchronized public void unRegisterDataInfo(TDDataProviderFX dataInfo) {
		dataProviders.remove(dataInfo);
	}

	/**
	 * Remove all items associated with a particular data block from 
	 * the register. 
	 * @param dataBlock
	 */
	synchronized public void unRegisterDataInfo(PamDataBlock dataBlock) {
		ListIterator<TDDataProviderFX> it = dataProviders.listIterator();
		while (it.hasNext()) {
			TDDataProviderFX di = it.next();
			if (di.getDataBlock() == dataBlock) {
				it.remove();
			}
		}
	}
	
	/**
	 * Find a provider based on it's class and it's name. <br>
	 * This is mainly called when deserialising settings and recreating 
	 * old plots. 
	 * @param providerClass class
	 * @param providerName name 
	 * @return provider class or null. 
	 */
	synchronized public TDDataProviderFX findDataProvider(Class providerClass, String providerName) {
		for (TDDataProviderFX aProvider:dataProviders) {
			if (aProvider.getClass() == providerClass && aProvider.getName().equals(providerName)) {
				return aProvider;
			}
		}
		return null;
	}
	
	/**
	 * Find a provider based on it's datablock. If there is more than one data block 
	 * then first provider in the list is returned. 
	 * @param pamDataBlock data block to find TDDataProvider for. 
	 * @return provider class or null. 
	 */
	synchronized public TDDataProviderFX findDataProvider(PamDataBlock pamDataBlock) {
		for (TDDataProviderFX aProvider:dataProviders) {
//			System.out.println("Data providers: " + aProvider.getName() + " | " + aProvider.getDataBlock() +  " | "  +pamDataBlock);
			if (aProvider.getDataBlock()==pamDataBlock) {
				return aProvider;
			}
		}
		return null;
	}
	
//	/**
//	 * Add menu items to an existing menu. Done this way since
//	 * we may want to add them to a normal menu or to a popup menu. 
//	 * @param aMenu
//	 * @return number of items added.
//	 */
//	synchronized public int addMenuItems(MenuItem aMenu, TDInfoMonitor infoMonitor) {
//		int n = 0;
//		for (TDDataProviderFX aProvider:dataProviders) {
//			MenuItem menuItem = new MenuItem(aProvider.getName());
//			aMenu.add(menuItem);
//			menuItem.addActionListener(new ProviderAction(aProvider, infoMonitor));
//			
//		}
//		return n;
//	}
//	
//	private class ProviderAction implements ActionListener {
//		private TDDataProviderFX dataProvider;
//		private TDInfoMonitor infoMonitor;
//
//		public ProviderAction(TDDataProviderFX dataProvider, TDInfoMonitor infoMonitor) {
//			super();
//			this.dataProvider = dataProvider;
//			this.infoMonitor = infoMonitor;
//		}
//
//		@Override
//		public void actionPerformed(ActionEvent arg0) {
//			infoMonitor.selectProvider(dataProvider);
//		}
//	}
	
	/**
	 * @return the complete list of data infos
	 */
	public synchronized ArrayList<TDDataProviderFX> getDataInfos() {
		return dataProviders;
	}
	
}