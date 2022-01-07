package detectionPlotFX.data;

import java.util.ArrayList;
import java.util.ListIterator;

import PamguardMVC.PamDataBlock;

public class DDPlotRegister  {

	private static DDPlotRegister singleInstance;
	
	/*
	 *A list of data providers for the Detection Display.  
	 */
	private ArrayList<DDDataProvider> dataProviders = new ArrayList<DDDataProvider>();

	
	public static DDPlotRegister getInstance() {
		if (singleInstance == null) {
			singleInstance = new DDPlotRegister();
		}
		return singleInstance;
	}
	
	/**
	 * Register a source of plotable data. 
	 * @param dataInfo info to register
	 */
	synchronized public void registerDataInfo(DDDataProvider dataInfo) {
		dataProviders.add(dataInfo);
	}
	
	/**
	 * Remove something from the register
	 * @param dataInfo info to remove from register
	 */
	synchronized public void unRegisterDataInfo(DDDataProvider dataInfo) {
		dataProviders.remove(dataInfo);
	}

	/**
	 * Remove all items associated with a particular data block from 
	 * the register. 
	 * @param dataBlock
	 */
	synchronized public void unRegisterDataInfo(PamDataBlock dataBlock) {
		ListIterator<DDDataProvider> it = dataProviders.listIterator();
		while (it.hasNext()) {
			DDDataProvider di = it.next();
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
	synchronized public DDDataProvider findDataProvider(Class providerClass, String providerName) {
		for (DDDataProvider aProvider:dataProviders) {
			if (aProvider.getClass() == providerClass && aProvider.getName().equals(providerName)) {
				return aProvider;
			}
		}
		return null;
	}
	
	/**
	 * Find a provider based on it's data block. If there is more than one data block 
	 * then first provider in the list is returned. 
	 * @param pamDataBlock data block to find DDDataProvider for. 
	 * @return provider class or null. 
	 */
	synchronized public DDDataProvider findDataProvider(PamDataBlock pamDataBlock) {
		for (DDDataProvider aProvider:dataProviders) {
			if (aProvider.getDataBlock()==pamDataBlock) {
				return aProvider;
			}
		}
		return null;
	}
	
	/**
	 * @return the complete list of data infos
	 */
	public synchronized ArrayList<DDDataProvider> getDataInfos() {
		return dataProviders;
	}
	
	
}
