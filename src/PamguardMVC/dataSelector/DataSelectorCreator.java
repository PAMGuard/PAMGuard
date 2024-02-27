package PamguardMVC.dataSelector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetDataBlock;
import annotation.CentralAnnotationsList;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import offlineProcessing.superdet.OfflineSuperDetFilter;

/**
 * Something that creates data selectors.
 * It also keeps a list of available 
 * data selectors, which are each named 
 * with a string so when asked for one, will 
 * be able to either give the existing one, or 
 * will create a new one.
 * <br>In this way, it's possible for multiple objects
 * to use the same data selector, but DataBlocks that
 * use these things should create separate settings
 * for each so that different parts of the system can 
 * work with different selections.  
 * <p>
 * Finally, these things can also keep a central 
 * register of all data selectors, so that their 
 * settings can get serialised when settings 
 * are saved. 
 * <p>16 April 2017. Rewrote how settings are saved. Previous system of each 
 * DS handlingit's own settings wasn't working since if a DS was never used
 * in a PAMGuard run it's settings fell out of the settings list since they
 * were never registered, so were never saved. Have now put a hashtable of 
 * settings into every DSCreator which will always get read and saved even
 * if DS's never get made in a particular PAMGuard run. This is similar to 
 * a problem in SymbolChoosers which suffered in the same way if nothing
 * got plotted on a particular display for a particular run.
 * @author Doug Gillespie
 *
 */
public abstract class DataSelectorCreator implements PamSettings {

	private PamDataBlock pamDataBlock;
	
	private HashMap<String, DataSelector> dataSelectors = new HashMap<>();
	
	private static ArrayList<DataSelector> globalSelectorList = new ArrayList<>();
	
	private DataSelectorSettings dataSelectorSettings = new DataSelectorSettings();

	public DataSelectorCreator(PamDataBlock pamDataBlock) {
		this.pamDataBlock = pamDataBlock;
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	/**
	 * Get a data selector of  a given name including all possible options for super detections and annotations. 
	 * @param selectorName
	 * @param allowScores
	 * @return data selector for given name. 
	 */
	public synchronized DataSelector getDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return getDataSelector(selectorName, allowScores, selectorType, true, true);
	}

	/**
	 * 
	 * Get a data selector of  a given name with optional inclusion of options for annotations and super detections
	 * @param selectorName data selector name
	 * @param allowScores allow scores
	 * @param selectorType 
	 * @param includeAnnotations include options from any annotators of this data stream
	 * @param includeSuperDetections include any possible super detection data selectors. 
	 * @return data selector for given name with appropriate options. 
	 */
	public synchronized DataSelector getDataSelector(String selectorName, boolean allowScores, String selectorType, 
			boolean includeAnnotations, boolean includeSuperDetections) {
		DataSelector ds = findDataSelector(selectorName);
		if (ds == null) {
			ds = createDataSelector(selectorName, allowScores, selectorType);
			
			if (includeAnnotations) {
				ds = addAnnotationOptions(ds, selectorName, allowScores, selectorType);
			}
			
			if (includeSuperDetections) {
				ds = addSuperDetectionOptions(ds, selectorName, allowScores, selectorType);
			}
						
			// and get it's params from the centralised list. 
			DataSelectParams params = dataSelectorSettings.getParams(selectorName);
			if (params != null && ds != null) {
				/*
				 *  we do want to keep even a null data selector since we don't want to keep trying
				 *  to create one and the hashtable will quickly return null.
				 */
				try {
					ds.setParams(params);
				}
				catch (ClassCastException e) {
					System.out.printf("Cannot cast parameter type for data selector %s: %s", ds.getLongSelectorName(), e.getMessage());
				}
			}
			dataSelectors.put(selectorName, ds);
			globalRegister(ds);
		}
		return ds;
	}
	
	/**
	 * Add extra annotation options to the data selector. This may 
	 * generate an entirely new data selector, then again, it might not
	 * @param ds base data selector (may be null)
	 * @return original data selector, possibly a compound data selector, etc. 
	 */
	protected DataSelector addAnnotationOptions(DataSelector ds, String selectorName, boolean allowScores, String selectorType) {
		/*
		 * Also find annotation data selectors
		 * Development code that's not ready to run yet ...
		 */
		if (pamDataBlock == null) {
			return ds;
		}
		
		ArrayList<DataAnnotationType> allAnnotations = new ArrayList<>();
		AnnotationHandler annotHandler = pamDataBlock.getAnnotationHandler();
		if (annotHandler != null) {
			List<DataAnnotationType<?>> annotTypes = annotHandler.getUsedAnnotationTypes();
			if (annotTypes != null) {
				allAnnotations.addAll(annotTypes);
			}
			
		}
		ArrayList<DataAnnotationType> centAnnots = CentralAnnotationsList.getList().findAnnotators(getPamDataBlock());
		if (centAnnots != null && centAnnots.size() > 0) {
			allAnnotations.addAll(centAnnots);
		}
		
		if (allAnnotations.size() == 0) {
			return ds;
		}
		ArrayList<DataSelector> allSelectors = new ArrayList<DataSelector>();
		if (ds != null) {
			allSelectors.add(ds);
		}
		for (DataAnnotationType<?> annotType : allAnnotations) {
			DataSelector annotSel = annotType.getDataSelector(pamDataBlock, selectorName, allowScores, selectorType);
			if (annotSel != null) {
				allSelectors.add(annotSel);
			}
		}
		if (allSelectors.size() == 0) {
			return null;
		}
		if (allSelectors.size() == 1) {
			return allSelectors.get(0);
		}
		else {
			return new CompoundDataSelector(pamDataBlock, allSelectors, selectorName, allowScores, selectorType);
		}
	}

	protected DataSelector addSuperDetectionOptions(DataSelector ds, String selectorName, boolean allowScores,
			String selectorType) {
		if (pamDataBlock == null) {
			return ds;
		}
		if (pamDataBlock instanceof SuperDetDataBlock) {
			// restrict to one level or it can go into an infinite loop
			return ds;
		}
		ArrayList<SuperDetDataBlock> possSuperDets = OfflineSuperDetFilter.findPossibleSuperDetections(pamDataBlock);
		if (possSuperDets == null || possSuperDets.size() == 0) {
			return ds;
		}
		// will have to make all available filters and have a dropdown to say which one we want. FFS this is complicated. 
		// Or do we allow multiple super detection types and let the user chose to ignore them ? 
		ArrayList<DataSelector> allSelectors = new ArrayList<DataSelector>();
		if (ds != null) {
			allSelectors.add(ds);
		}
		for (SuperDetDataBlock sdBlock : possSuperDets) {
			DataSelector sdds = sdBlock.getDataSelector(selectorName, allowScores);
			if (sdds != null) {
				allSelectors.add(new SuperDetDataSelector(sdBlock, sdds));
			}
		}
		if (allSelectors.size() == 0) {
			return null;
		}
		if (allSelectors.size() == 1) {
			return allSelectors.get(0);
		}
		else {
			CompoundDataSelector selector = new CompoundDataSelector(pamDataBlock, allSelectors, selectorName, allowScores, selectorType);
			// not needed since it get's done after this call anyway. 
//			DataSelectParams params = dataSelectorSettings.getParams(selectorName);
//			if (params instanceof CompoundParams) {
//				selector.setParams(params);
//			}
			
			return selector;
		}
	}
	
	/**
	 * Destroy a data selector. Very rarely called, but for neatness 
	 * might call this if a module that was using a data selector 
	 * was removed from PAMGuard. 
	 * @param selectorName Name of data selector
	 * @return true if it existed. 
	 */
	public synchronized boolean destroyDataSelector(String selectorName) {
		DataSelector ds = findDataSelector(selectorName);
		if (ds == null) {
			return false;
		}
		dataSelectors.remove(ds);
		return globalUnRegister(ds);
	}
	
	/**
	 * Create a data selector with a given name. 
	 * @param selectorName
	 * @return a new data selector. 
	 */
	abstract public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType);

	private DataSelector lastFoundSelector;
	/**
	 * Find a data selector with a given name. 
	 * @param selectorName Name of data selector
	 * @return selector, or null. 
	 */
	public synchronized DataSelector findDataSelector(String selectorName) {
		/**
		 * Quick fix since this likely to be called many many times by certain 
		 * functions for every data unit. 
		 */
//		try {
//		if (lastFoundSelector != null && selectorName.equals(lastFoundSelector.getSelectorName())) {
//			return lastFoundSelector;
//		}
//		for (DataSelector ds:dataSelectors) {
//			if (ds.getSelectorName().equals(selectorName)) {
//				return (lastFoundSelector = ds);
//			}
//		}
//		}
//		catch (NullPointerException e) {
//			return null;
//		}
		return dataSelectors.get(selectorName);
	}
	
	/**
	 * Add a data selector to the global register list. 
	 * @param dataSelector
	 */
	synchronized private static void globalRegister(DataSelector dataSelector) {
		globalSelectorList.add(dataSelector);
	}
	
	/**
	 * Remove a data selector from the global list. 
	 * @param dataSelector Data selector to remove
	 * @return true if the global list contained the specified data selector
	 */
	synchronized private static boolean globalUnRegister(DataSelector dataSelector) {
		return globalSelectorList.remove(dataSelector);
	}
	
	/**
	 * Clear all data selectors from all lists. This is to be called after annotations change, which may require a 
	 * data selector to rebuild itself with different annotation options. 
	 */
	public static void globalClear() {
		ArrayList<PamDataBlock> allBlocks = PamController.getInstance().getDataBlocks();
				
		for (PamDataBlock aBlock : allBlocks) {
			DataSelectorCreator dsc = aBlock.getDataSelectCreator();
			if (dsc != null) {
				dsc.clearDataSelectors();
			}
		}
	}
	
	/**
	 * Clear all data selectors for this datablock. This probably needs data selectors
	 * to be saved first. 
	 */
	public synchronized void clearDataSelectors() {
		/**
		 * This will populate the settings from existing selectors. 
		 * So should be all we need to do, then when selectors are recreated, they
		 * will get their settings from that list. 
		 */
		getSettingsReference();
		/**
		 * Clear them one by one so that they can also be
		 * deregistered from the global list. 
		 */
//		dataSelectors.clear();
		Set<String> keySet = dataSelectors.keySet();
		for (String key : keySet) {
			DataSelector ds = dataSelectors.get(key);
			if (ds != null) {
				globalUnRegister(ds);
			}
		}

		dataSelectors.clear();
	}

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}


	@Override
	public String getUnitName() {
		if (pamDataBlock == null) {
			return this.getClass().getName();
		}
		return pamDataBlock.getLongDataName();
	}

	@Override
	public String getUnitType() {
		return "Data Selector";
	}

	@Override
	public synchronized Serializable getSettingsReference() {
		/*
		 * Retrieve settings from every data selector
		 */
		Set<String> names = dataSelectors.keySet();
		for (String selName : names) {
			DataSelector ds = dataSelectors.get(selName);
			if (ds == null) {
				continue;
			}
			DataSelectParams params = ds.getParams();
			if (params != null) {
				dataSelectorSettings.setParams(selName, params);
			}
		}
		return dataSelectorSettings;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsVersion()
	 */
	@Override
	public long getSettingsVersion() {
		return DataSelectorSettings.serialVersionUID;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#restoreSettings(PamController.PamControlledUnitSettings)
	 */
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			dataSelectorSettings = (DataSelectorSettings) pamControlledUnitSettings.getSettings();
		}
		catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public abstract DataSelectParams createNewParams(String name);

}
