package PamController;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import PamController.settings.output.xml.PamguardXMLWriter;
import PamDetection.PamDetection;
import PamDetection.RawDataUnit;
import PamView.GeneralProjector;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import binaryFileStorage.BinaryStore;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import offlineProcessing.OfflineTask;
import offlineProcessing.OfflineTaskGroup;

/**
 * Class to take all of the configuration information out of PamController. This 
 * is because PamController is a singleton class, so there can only every be one of 
 * them, but for some of the batch processing control, we need to be able to load and
 * manipulate a second set of modules, which do nothing within the running 
 * configuration, but are there to allow us to set up a configuration to send to 
 * other batch processes in viewer mode. This is also being used in an improved module import system. 
 * @author dg50
 *
 */
public class PamConfiguration {

	/**
	 * List of the current controlled units (PAMGuard modules)
	 */
	private ArrayList<PamControlledUnit> pamControlledUnits;

	public PamConfiguration() {
		super();
		
		// create the array list to hold multiple views
		pamControlledUnits = new ArrayList<PamControlledUnit>();
	}

	/**
	 * Call setupControlledUnit() on all modules. 
	 */
	public void setupProcesses() {
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			pamControlledUnits.get(i).setupControlledUnit();
		}
	}

	/**
	 * Can PAMGUARD shut down. This question is asked in turn to 
	 * every module. Each module should attempt to make sure it can 
	 * answer true, e.g. by closing files, but if any module
	 * returns false, then canClose() will return false;
	 * @return whether it's possible to close PAMGUARD 
	 * without corrupting or losing data. 
	 */
	public boolean canClose() {
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			if (!pamControlledUnits.get(i).canClose()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Called after canClose has returned true to finally tell 
	 * all modules that PAMGUARD is definitely closing down.so they
	 * can free any resources, etc.  
	 */
	public void pamClose() {

		
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			pamControlledUnits.get(i).pamClose();
		}
	}

	/**
	 * @return the pamControlledUnits
	 */
	public ArrayList<PamControlledUnit> getPamControlledUnits() {
		return pamControlledUnits;
	}

	/**
	 * Add a PamControlledUnit to the main list. 
	 * @param controlledUnit
	 */
	public void addControlledUnit(PamControlledUnit controlledUnit) {
		pamControlledUnits.add(controlledUnit);
	}

	public boolean removeControlledUnt(PamControlledUnit controlledUnit) {

		boolean removed = false;
		while (pamControlledUnits.contains(controlledUnit)) {
			pamControlledUnits.remove(controlledUnit);
			removed = true;
		}
		return removed;
		//		getMainFrame().revalidate(); //handled inside the GUIFrameManager by notify model changed. The controller should have 
		//as few direct GUI calls as possible. 
	}

	/**
	 * re-order the modules according to the given list. 
	 * @param newOrder
	 * @return
	 */
	public boolean reOrderModules(int[] newOrder) {

		if (pamControlledUnits.size() != newOrder.length) return false;

		ArrayList<PamControlledUnit> newList = new ArrayList<PamControlledUnit>();

		for (int i = 0; i < newOrder.length; i++) {

			newList.add(pamControlledUnits.get(newOrder[i]));

		}

		pamControlledUnits = newList;

		return true;
	}

	public PamControlledUnit getControlledUnit(int iUnit) {
		if (iUnit < getNumControlledUnits()) {
			return pamControlledUnits.get(iUnit);
		}
		return null;
	}

	public PamControlledUnit findControlledUnit(String unitType) {
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getUnitType().equalsIgnoreCase(unitType)) {
				return pamControlledUnits.get(i);
			}
		}
		return null;
	}
	
	public int getNumControlledUnits() {
		return pamControlledUnits.size();
	}

	public PamRawDataBlock getRawDataBlock(int id) {
		return (PamRawDataBlock) getDataBlock(RawDataUnit.class, id);
	}
	
	public PamRawDataBlock getRawDataBlock(String name) {
		return (PamRawDataBlock) getDataBlock(RawDataUnit.class, name);
	}
	
	/** 
	 * Find a block of a given type with the given name, or null if it
	 * doesn't exist.
	 * @param  blockType -- RAW, FFT, DETECTOR, null, etc.
	 * @param  name -- the block name
	 * @return  block, which you may want to cast to a subtype
	 */
	public PamDataBlock getDataBlock(Class blockType, String name) {
		if (name == null) return null;
		ArrayList<PamDataBlock> blocks = getDataBlocks(blockType, true);
		for (PamDataBlock dataBlock:blocks) {
			if (name.equals(dataBlock.getLongDataName())) { // check for a long name match first 
				return dataBlock;
			}
			if (dataBlock instanceof FFTDataBlock) {
				FFTDataBlock fb = (FFTDataBlock) dataBlock;
				if (name.equals(fb.getOldLongDataName())) {
					return dataBlock;
				}
			}
			if (name.equals(dataBlock.toString())) {
				return dataBlock;
			}
		}
		return tryShortName(blockType, name);
	}
	
	/**
	 * For backwards compatibility, some blocks may still use the short name.
	 * @param blockType
	 * @param name
	 * @return
	 */
	private PamDataBlock tryShortName(Class blockType, String name) {
		if (name == null) return null;
		ArrayList<PamDataBlock> blocks = getDataBlocks(blockType, true);
		for (PamDataBlock dataBlock:blocks) {
			if (name.equals(dataBlock.getDataName())) { // check for a long name match first 
				return dataBlock;
			}
		}
		return null;
	}

	public ArrayList<PamDataBlock> getDataBlocks(Class blockType, boolean includeSubClasses) {
		return makeDataBlockList(blockType, includeSubClasses);
	}

	public ArrayList<PamDataBlock> getDetectorDataBlocks() {
		return makeDataBlockList(PamDetection.class, true);
	}

	public ArrayList<PamDataBlock> getFFTDataBlocks() {
		return makeDataBlockList(FFTDataUnit.class, true);
	}

	public PamDataBlock getFFTDataBlock(int id) {
		return getDataBlock(FFTDataUnit.class, id);
	}

	public PamDataBlock getFFTDataBlock(String name) {
		return getDataBlock(FFTDataUnit.class, name);
	}
	
	/** 
	 * Find a block of a given type with the id number, or null if the number
	 * is out of range.
	 * 
	 * @param  blockType
	 * @param  id -- the block id number
	 * @return  block, which you may want to cast to a subtype
	 */
	public PamDataBlock getDataBlock(Class blockType, int id) {

		ArrayList<PamDataBlock> blocks = getDataBlocks(blockType, true);
		if (id >= 0 && id < blocks.size()) return blocks.get(id);
		return null;
	}
	public ArrayList<PamDataBlock> getRawDataBlocks() {
		return makeDataBlockList(RawDataUnit.class, true);
	}
	/**
	 * Find a block with the given long name, or null if it doesn't exist.
	 * @param longName the long name of the PamDataBlock
	 * @return block
	 */
	public PamDataBlock getDataBlockByLongName(String longName) {
		if (longName == null) return null;
		ArrayList<PamDataBlock> allBlocks = getDataBlocks();
		for (PamDataBlock dataBlock:allBlocks) {
			if (longName.equals(dataBlock.getLongDataName())) {
				return dataBlock;
			}
			if (dataBlock instanceof FFTDataBlock) {
				FFTDataBlock fb = (FFTDataBlock) dataBlock;
				if (longName.equals(fb.getOldLongDataName())) {
					return dataBlock;
				}
			}
		}
		return null;
	}

	public ArrayList<PamDataBlock> getDataBlocks() {
		return makeDataBlockList(PamDataUnit.class, true);
	}
	
	/**
	 * Get a list of PamControlledUnit units of a given type
	 * @param unitType Controlled unit type
	 * @return list of units. 
	 */
	public ArrayList<PamControlledUnit> findControlledUnits(String unitType) {
		ArrayList<PamControlledUnit> l = new ArrayList<PamControlledUnit>();
		int n = getNumControlledUnits();
		PamControlledUnit pcu;
		for (int i = 0; i < n; i++) {
			pcu = getControlledUnit(i);
			if (pcu.getUnitType().equals(unitType)) {
				l.add(pcu);
			}
		}

		return l;
	}
	
	/**
	 * Get a list of PamControlledUnit units of a given type and name, allowing for nulls. 
	 * @param unitType Controlled unit type, can be null for all units of name
	 * @param unitName Controlled unit name, can be null for all units of type
	 * @return list of units. 
	 */
	public ArrayList<PamControlledUnit> findControlledUnits(String unitType, String unitName) {
		ArrayList<PamControlledUnit> l = new ArrayList<PamControlledUnit>();
		int n = getNumControlledUnits();
		PamControlledUnit pcu;
		for (int i = 0; i < n; i++) {
			pcu = getControlledUnit(i);
			if (unitType != null && !unitType.equals(pcu.getUnitType())) {
				continue;
			}
			if (unitName != null && !unitName.equals(pcu.getUnitName())) {
				continue;
			}
			l.add(pcu);
		}

		return l;
	}

	/**
	 * find the first controlled unit with the given type and name. 
	 * @param unitType
	 * @param unitName
	 * @return
	 */
	public PamControlledUnit findControlledUnit(String unitType, String unitName) {
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getUnitType().equalsIgnoreCase(unitType) &&
					pamControlledUnits.get(i).getUnitName().equalsIgnoreCase(unitName)) {
				return pamControlledUnits.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Find the first instance of a module with a given class type and name.
	 * <p>Name can be null in which case the first module with the correct class
	 * will be returned
	 * @param unitClass Module class (sub class of PamControlledUnit)
	 * @param unitName Module Name
	 * @return Existing module with that class and name. 
	 */
	public PamControlledUnit findControlledUnit(Class unitClass, String unitName) {
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getClass() == unitClass && (unitName == null ||
					pamControlledUnits.get(i).getUnitName().equalsIgnoreCase(unitName))) {
				return pamControlledUnits.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Get an Array list of PamControlledUnits of a particular class (exact matches only). 
	 * @param unitClass PamControlledUnit class
	 * @return List of current instances of this class. 
	 */
	public ArrayList<PamControlledUnit> findControlledUnits(Class unitClass) {
		ArrayList<PamControlledUnit> foundUnits = new ArrayList<>();
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getClass() == unitClass) {
				foundUnits.add(pamControlledUnits.get(i));
			}
		}
		return foundUnits;
	}

	/**
	 * Get an Array list of PamControlledUnits of a particular class (exact matches only). 
	 * @param unitClass PamControlledUnit class
	 * @return List of current instances of this class. 
	 */
	public ArrayList<PamControlledUnit> findControlledUnits(Class unitClass, boolean includeSubClasses) {
		if (!includeSubClasses) {
			return findControlledUnits(unitClass);
		}
		ArrayList<PamControlledUnit> foundUnits = new ArrayList<>();
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (unitClass.isAssignableFrom(pamControlledUnits.get(i).getClass())) {
				foundUnits.add(pamControlledUnits.get(i));
			}
		}
		return foundUnits;
	}
	/**
	 * Check whether a controlled unit exists based on it's name. 
	 * @param the controlled unit name e.g. "my crazy click detector", not the default name. 
	 */
	public boolean isControlledUnit(String controlName) {
		for (int i = 0; i < getNumControlledUnits(); i++) {
			if (pamControlledUnits.get(i).getUnitName().equalsIgnoreCase(controlName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets called in pamStart and may / will attempt to store all
	 * PAMGUARD settings via the database and binary storage modules. 
	 */
	public void saveSettings(long timeNow) {
		PamControlledUnit pcu;
		PamSettingsSource settingsSource;
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			pcu = pamControlledUnits.get(iU);
			if (PamSettingsSource.class.isAssignableFrom(pcu.getClass())) {
				settingsSource = (PamSettingsSource) pcu;
				settingsSource.saveStartSettings(timeNow);
			}
		}
		PamguardXMLWriter.getXMLWriter().writeStartSettings(timeNow);
	}

	/**
	 * 
	 * @return a list of PamControlledUnits which implements the 
	 * PamSettingsSource interface
	 * @see PamSettingsSource
	 */
	public ArrayList<PamSettingsSource> findSettingsSources() {
		ArrayList<PamSettingsSource> settingsSources = new ArrayList<PamSettingsSource>();
		PamControlledUnit pcu;
		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			pcu = pamControlledUnits.get(iU);
			if (PamSettingsSource.class.isAssignableFrom(pcu.getClass())) {
				settingsSources.add((PamSettingsSource) pcu);
			}
		}
		return settingsSources;
	}

	public ArrayList<PamDataBlock> getPlottableDataBlocks(GeneralProjector generalProjector) {

		ArrayList<PamDataBlock> blockList = new ArrayList<PamDataBlock>();
		PamProcess pP;
		Class unitClass;
		PanelOverlayDraw panelOverlayDraw;

		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			for (int iP = 0; iP < pamControlledUnits.get(iU)
					.getNumPamProcesses(); iP++) {
				pP = pamControlledUnits.get(iU).getPamProcess(iP);
				for (int j = 0; j < pP.getNumOutputDataBlocks(); j++) {
					if(pP.getOutputDataBlock(j).canDraw(generalProjector)) {
						blockList.add(pP.getOutputDataBlock(j));
					}
				}
			}
		}
		return blockList;
	}

	public ArrayList<PamDataBlock> makeDataBlockList(Class classType, boolean includSubClasses) {

		ArrayList<PamDataBlock> blockList = new ArrayList<PamDataBlock>();
		PamProcess pP;
		Class unitClass;

		for (int iU = 0; iU < pamControlledUnits.size(); iU++) {
			for (int iP = 0; iP < pamControlledUnits.get(iU)
					.getNumPamProcesses(); iP++) {
				pP = pamControlledUnits.get(iU).getPamProcess(iP);
				for (int j = 0; j < pP.getNumOutputDataBlocks(); j++) {
					//System.out.println("Comparing "+pP.getOutputDataBlock(j).getUnitClass().getCanonicalName()+" to "+classType.getCanonicalName());
					if ((unitClass = pP.getOutputDataBlock(j).getUnitClass()) == classType) {
						blockList.add(pP.getOutputDataBlock(j));
					}
					else if (includSubClasses) {
						if (classType != null && classType.isAssignableFrom(unitClass)) {
							blockList.add(pP.getOutputDataBlock(j));
						}
						//						while ((unitClass = unitClass.getSuperclass()) != null) {
						//							if (unitClass == classType) {
						//								blockList.add(pP.getOutputDataBlock(j));
						//								break;
						//							}
						//						}
					}
				}
			}
		}

		return blockList;
	}

	public void notifyModelChanged(int changeType) {
		// also tell all PamControlledUnits since they may want to find their data source 
		// it that was created after they were - i.e. dependencies have got all muddled
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			pamControlledUnits.get(i).notifyModelChanged(changeType);
		}
	}

	public Serializable getSettingsReference() {
		ArrayList<UsedModuleInfo> usedModules = new ArrayList<UsedModuleInfo>();
		for (int i = 0; i < pamControlledUnits.size(); i++) {
			usedModules.add(new UsedModuleInfo(pamControlledUnits.get(i).getClass().getName(), 
					pamControlledUnits.get(i).getUnitType(),
					pamControlledUnits.get(i).getUnitName()));
		}
		return usedModules;
	}

	public void destroyModel() {

		for (int i = 0; i < pamControlledUnits.size(); i++) {
			pamControlledUnits.get(i).notifyModelChanged(PamControllerInterface.DESTROY_EVERYTHING);
		}
		pamControlledUnits.clear();
	}
	
	/**
	 * Get the index of a PamControlledUnit
	 * @param unit
	 * @return
	 */
	public int getControlledUnitIndex(PamControlledUnit unit) {
		return pamControlledUnits.indexOf(unit);
	}

	/**
	 * Find the path to the binary store ....
	 * @return path to the binary store. 
	 */
	public String findBinaryStorePath() {
		BinaryStore binaryStore = (BinaryStore) findControlledUnit(BinaryStore.getBinaryUnitType());
		if (binaryStore == null) {
			return null;
		}
		String storeLoc = binaryStore.getBinaryStoreSettings().getStoreLocation();
		if (storeLoc == null) {
			return "";
		}
		if (!storeLoc.endsWith(File.separator)) {
			storeLoc += File.separator;
		}
		return storeLoc;
	}
	
	/**
	 * Get a list of all offline task groups in this configuration
	 * @return task group list
	 */
	public ArrayList<OfflineTaskGroup> getAllOfflineTaskGroups() {
		ArrayList<OfflineTaskGroup> tgs = new ArrayList<OfflineTaskGroup>();
		for (PamControlledUnit unit : pamControlledUnits){
			int numGroups = unit.getNumOfflineTaskGroups();
			for (int iGp=0;iGp<numGroups;iGp++){
				tgs.add( unit.getOfflineTaskGroup(iGp));
				
			}
		}
		return tgs;
	}
	
	/**
	 * Get a list of all offline tasks in the configuration
	 * @return offline task list
	 */
	public ArrayList<OfflineTask> getAllOfflineTasks() {
		ArrayList<OfflineTask> ots = new ArrayList<OfflineTask>();
		ArrayList<OfflineTaskGroup> groups = getAllOfflineTaskGroups();
		for (OfflineTaskGroup group : groups) {
			int nTasks = group.getNTasks();
			for (int i = 0; i < nTasks; i++) {
				ots.add(group.getTask(i));
			}
		}
		return ots;
	}
}
