package tethys.species;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFileChooser;

import PamController.PamConfiguration;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamFolders;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamFileFilter;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import tethys.species.swing.SpeciesMapIODialog;

/**
 * Master manager for species maps which will eventually allow for export and import from XML
 * documents, databases and other things ...<br>
 * Might be a problem for some batch task management having these so global and having datablocks in the 
 * external config that are not in the main model. Will move storage to individual datablocks and here simply
 * have functions to gather all for some global output. 
 * <br>
 * (Perhaps not as XML, will simply output the serialized map - easier. 
 * @author dg50
 *
 */
public class SpeciesMapManager implements PamSettings {
	
	private static SpeciesMapManager singleInstance = null;
	
	/**
	 * Synch object to survive multithreading. 
	 */
	private static Object synch = new Object(); 
	
	/**
	 * Map of all species maps. 
	 */
	private GlobalSpeciesMap globalSpeciesMap;

	private JFileChooser ioFileChooser;
	
	private static final String unitName = "Global Species Codes";
	
	/**
	 * file end type for map files 
	 */
	public static final String mapFileEnd = ".spmap";

	private SpeciesMapManager() {
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	/**
	 * Get an instance of the global species manager. This handles look up tables 
	 * for each datablock to convert from internal PAMGuard names to ITIS species codes and
	 * usefully call types for output to Tethys. 
	 * @return
	 */
	public static SpeciesMapManager getInstance() {
		if (singleInstance == null) {
			synchronized (synch) {
				if (singleInstance == null) {
					singleInstance = new SpeciesMapManager();
				}
			}
		}
		return singleInstance;
	}

	@Override
	public String getUnitName() {
		return unitName;
	}

	@Override
	public String getUnitType() {
		return unitName;
	}

	/**
	 * Gathers species maps from datablocks using the default configuration. 
	 */
	@Override
	public Serializable getSettingsReference() {
//		gatherSpeciesMaps(PamController.getInstance().getPamConfiguration());
//		return globalSpeciesMap;
		return getSettingsReference(PamController.getInstance().getPamConfiguration());
	}

	/**
	 * Gathers species maps from datablocks using the given configuration (used in batch control)
	 * @param pamConfiguration
	 * @return serialised data for species maps. 
	 */
	public Serializable getSettingsReference(PamConfiguration pamConfiguration) {
		gatherSpeciesMaps(pamConfiguration);
		return globalSpeciesMap;
	}

	/**
	 * Get species maps from all PAMGuard datablocks which have such a map
	 * Nothing is ever removed from here, which probably matters. 
	 */
	public void gatherSpeciesMaps() {
		gatherSpeciesMaps(PamController.getInstance().getPamConfiguration());
	}
	/**
	 * Get species maps from all PAMGuard datablocks which have such a map
	 * Nothing is ever removed from here, which probably matters. 
	 * @param configuration PAMGuard configuration if not the main one (used in batch processing job control)
	 */
	public void gatherSpeciesMaps(PamConfiguration configuration) {
		if (globalSpeciesMap == null) {
			globalSpeciesMap = new GlobalSpeciesMap();
		}
		ArrayList<PamDataBlock> allDataBlocks = configuration.getDataBlocks();// won't work for external config. 
		for (PamDataBlock aBlock : allDataBlocks) {
			DataBlockSpeciesManager spManager = aBlock.getDatablockSpeciesManager();
			if (spManager == null) {
				continue;
			}
			DataBlockSpeciesMap speciesMap = spManager.getDatablockSpeciesMap();
			globalSpeciesMap.put(aBlock, speciesMap);
		}
	}
	
	public DataBlockSpeciesMap getSpeciesMap(PamDataBlock pamDataBlock) {
		if (globalSpeciesMap == null) {
			return null;
		}
		return globalSpeciesMap.get(pamDataBlock);
	}

	@Override
	public long getSettingsVersion() {
		return GlobalSpeciesMap.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		Object obj = pamControlledUnitSettings.getSettings();
		if (obj instanceof GlobalSpeciesMap) {
			this.globalSpeciesMap = (GlobalSpeciesMap) obj;
			return true;
		}
		else {
			return false;
		}
	}
	
	public ActionListener getExportAction(Window parentFrame) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportSpeciesMaps(parentFrame);
			}
		};
	}

	public ActionListener getImportAction(Window parentFrame) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importSpeciesMaps(parentFrame);
			}
		};
	}
	
	/**
	 * Export all species maps to a serialized object file. 
	 * @param parentFrame 
	 * @return
	 */
	public boolean exportSpeciesMaps(Window parentFrame) {
		// gather the species maps from the data blocks...
		gatherSpeciesMaps();
		GlobalSpeciesMap toExport = SpeciesMapIODialog.showDialog(parentFrame, globalSpeciesMap, true);
		if (toExport == null) {
			return false;
		}
		if (toExport.getDatablockMaps().size() == 0) {
			return false;
		}
		
		
		JFileChooser chooser = getFileChooser();
		int ans = chooser.showSaveDialog(parentFrame);
		if (ans != JFileChooser.APPROVE_OPTION) {
			return false;
		}
		File opFile = chooser.getSelectedFile();
		opFile = PamFileFilter.checkFileEnd(opFile, mapFileEnd, true);
		// write it. 
		try {
			ObjectOutputStream op = new ObjectOutputStream(new FileOutputStream(opFile));
			op.writeObject(toExport);
			op.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}
	
	/**
	 * Get a file chooser, which will remember folders, etc. while PAMGuard is open 
	 * @return file chooser. 
	 */
	private JFileChooser getFileChooser() {
		if (ioFileChooser != null) {
			return ioFileChooser;
		}
		PamFileFilter fileFilter = new PamFileFilter("Species map files", mapFileEnd);
		ioFileChooser = new JFileChooser();
		ioFileChooser.setFileFilter(fileFilter);
		ioFileChooser.setCurrentDirectory(new File(PamFolders.getDefaultProjectFolder()));
		return ioFileChooser;
	}
	
	/**
	 * Import global species maps from selected file. 
	 * @param parentFrame
	 * @return
	 */
	public boolean importSpeciesMaps(Window parentFrame) {
		JFileChooser chooser = getFileChooser();
		int ans = chooser.showOpenDialog(parentFrame);
		if (ans != JFileChooser.APPROVE_OPTION) {
			return false;
		}
		File ipFile = chooser.getSelectedFile();
		ipFile = PamFileFilter.checkFileEnd(ipFile, mapFileEnd, true);
		GlobalSpeciesMap readSpeciesMap = null;
		// read it. 
		try {
			ObjectInputStream ip = new ObjectInputStream(new FileInputStream(ipFile));
			readSpeciesMap = (GlobalSpeciesMap) ip.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		GlobalSpeciesMap keptMaps = SpeciesMapIODialog.showDialog(parentFrame, readSpeciesMap, false);
		if (keptMaps == null) {
			return false;
		}
		if (keptMaps.getDatablockMaps().size() == 0) {
			return false;
		}
		
		return handleNewSpeciesMap(keptMaps);
	}

	private boolean handleNewSpeciesMap(GlobalSpeciesMap readSpeciesMap) {
		if (readSpeciesMap == null) {
			return false;
		}
		
		
		// could put in a dialog to only select parts of the map if we wanted to ? 
		int ans = WarnOnce.showWarning("Global Species Map", 
				"Do you want to overwrite PAMGaurd species maps with the imported data ?",
				WarnOnce.YES_NO_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return false;
		}
		
		Set<Entry<String, DataBlockSpeciesMap>> mapSet = readSpeciesMap.getDatablockMaps().entrySet();
		Iterator<Entry<String, DataBlockSpeciesMap>> iter = mapSet.iterator();
		while (iter.hasNext()) {
			Entry<String, DataBlockSpeciesMap> entry = iter.next();
			PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(entry.getKey());
			if (dataBlock == null) {
				String err = String.format("Data block %s does not exist in the current configuration", entry.getKey());
				WarnOnce.showWarning("Missing data block", err, WarnOnce.WARNING_MESSAGE);
				continue;
			}
			globalSpeciesMap.put(dataBlock, entry.getValue());
			DataBlockSpeciesManager spManager = dataBlock.getDatablockSpeciesManager();
			if (spManager == null) {
				String err = String.format("Data block %s does not have a species manager", entry.getKey());
				WarnOnce.showWarning("Missing species manager", err, WarnOnce.WARNING_MESSAGE);
				continue;
			}
			spManager.setDatablockSpeciesMap(entry.getValue());
		}
		
		
//		globalSpeciesMap = readSpeciesMap;
//		// no wupdate all datablock maps since they keep their own copies. 
//		ArrayList<PamDataBlock> allDatablocks = PamController.getInstance().getDataBlocks();
//		for (PamDataBlock aBlock : allDatablocks) {
//			DataBlockSpeciesManager spManager = aBlock.getDatablockSpeciesManager();
//			if (spManager == null) {
//				continue;
//			}
//			DataBlockSpeciesMap blockMap = globalSpeciesMap.get(aBlock);
//			if (blockMap != null) {
//				spManager.setDatablockSpeciesMap(blockMap);
//			}
//		}
		
		return true;
	}
}
