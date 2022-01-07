package offlineProcessing.superdet;

import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JComponent;

import PamController.PamController;
import PamController.PamViewParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.superdet.SuperDetDataBlock;
import dataMap.OfflineDataMapPoint;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import generalDatabase.clauses.FixedClause;
import generalDatabase.clauses.PAMSelectClause;

/**
 * Super detection filter for offline tasks. When added to a offlinetaskgroup it will 
 * show options (if there are any) for filtering processed data units by super detections. 
 * e.g. for click processing, this should be able to restrict processing to just files
 * that have porpoise events. Key will be to not even load a file that hasn't any data
 * associated with super detections.<p>
 * @author dg50
 *
 */
public class OfflineSuperDetFilter {

	private ArrayList<SuperDetDataBlock> availableSuperDetctors;
	private String dataName;
	private SuperDetDataBlock superDataBlock;

	private OfflineSuperDetFilter(ArrayList<SuperDetDataBlock> superDets, String dataName) {
		this.availableSuperDetctors = superDets;
		this.dataName = dataName;
	}

	/**
	 * Make a list of all possible data blocks that might hold super detection data for the data 
	 * in the given data block.<p>
	 * For a datablock to be identified as a super data block it must have SQLLogging with a sub table
	 * and the subDetectionType of the data in that block must be compatible with the data in the 
	 * core data block.
	 * @param coreDataBlock datablock containing sub detections
	 * @return list of datablocks that might have super detections. 
	 */
	public static ArrayList<SuperDetDataBlock> findPossibleSuperDetections(PamDataBlock coreDataBlock) {
		ArrayList<PamDataBlock> allBlocks = PamController.getInstance().getDataBlocks();
		ArrayList<SuperDetDataBlock> possBlocks = new ArrayList();
		for (PamDataBlock dataBlock : allBlocks) {
			if (dataBlock == coreDataBlock) {
				continue;
			}
			if (dataBlock instanceof SuperDetDataBlock == false) {
				continue;
			}
			SuperDetDataBlock sdDataBlock = (SuperDetDataBlock) dataBlock;
			SuperDetLogging logging = sdDataBlock.getLogging();
			if (logging == null) {
				continue;
			}

			SQLLogging subLogging = logging.getSubLogging();
			if (subLogging == null) {
				continue;
			}

			Class superClass = sdDataBlock.getSubDetectionClass();
			if (superClass == null) {
				continue;
			}
			if (dataBlock.getDataSelectCreator() == null) {
				// no data selector, so can't do anything
				continue;
			}

			if (sdDataBlock.canSuperDetection(coreDataBlock) == false) {
				continue;
			}

//			if (coreDataBlock != null && superClass.isAssignableFrom(coreDataBlock.getUnitClass())) {
				possBlocks.add(sdDataBlock);
//			}
		}
		return possBlocks;
	}
	
	/**
	 * Make a super detection filter for the given datablock. Return null
	 * if no appropriate super detections are available. 
	 * @param subDataBlock datablock of the sub detections
	 * @param dataName data name (will be used in dataselector settings for the super detection). 
	 * @return null if no suitable super detections, otherwise the basis of a filter. 
	 */
	public static OfflineSuperDetFilter makeSuperDetFilter(PamDataBlock subDataBlock, String dataName) {
		ArrayList<SuperDetDataBlock> superDets = findPossibleSuperDetections(subDataBlock);
		if (superDets.size() == 0) {
			return null;
		}
		OfflineSuperDetFilter sdf = new OfflineSuperDetFilter(superDets, dataName);
		return sdf;
	}

	public JComponent getSwingComponent(Window parent) {
		OSDFSwingComponent osfdSwing = new OSDFSwingComponent(parent, this);
		return osfdSwing.getComponent();
	}

	/**
	 * @return the availableSuperDetctors
	 */
	public ArrayList<SuperDetDataBlock> getAvailableSuperDetctors() {
		return availableSuperDetctors;
	}

	/**
	 * Get a dataname that can be used with data selectors. 
	 * @return the dataName
	 */
	public String getDataName() {
		return dataName;
	}

	/**
	 * Called to see if this sub detection will pass the super detection 
	 * filter, i.e. find an appropriate super detection then test
	 * that in the superdetection filter. <p>
	 * Defaults to returning true if there isn't a filter data block selected
	 * or if the super datablock doesn't have a data selector.
	 * Data selectors can themselves how to handle a null dataunit, e.g. if we're looking 
	 * at super detections of clicks - clcik trains - then the data selector has an option 
	 * to unclude clicks which are not part of a train, in  which case the superdetection of the 
	 * click will be null
	 * @param dataUnit Sub detectoin data unit. 
	 * @return true if conditions are met ...
	 */
	public boolean checkSubDetection(PamDataUnit dataUnit) {
		if (superDataBlock == null) {
			return true;
		}
		// find a super detection that fits the class of the super block. 
		PamDataUnit superDetection = dataUnit.getSuperDetection(superDataBlock.getUnitClass());
		DataSelector dataSelector = superDataBlock.getDataSelector(dataName, false);
		if (dataSelector == null) {
			return true;
		}
		
		return dataSelector.scoreData(superDetection) > 0;
	}

	public void setFilterDataBlock(SuperDetDataBlock currentSuperBlock) {
		superDataBlock = currentSuperBlock;
	}
	
	private SQLLogging getSubLogging() {
		return superDataBlock.getLogging().getSubLogging();
	}

	/**
	 * Work out if any of the data in the mapPoint, which is for
	 * the main OfflineTask datablock, might be associated with a 
	 * super detection which passes this filter. Will involve some
	 * querying of database tables, but hopefully this will be faster
	 * than loading a lot of data we don't need to process. 
	 * @param mapPoint Map Point for sub detection data.
	 * @return true if there are data worth processing. 
	 */
	public boolean shouldProcess(OfflineDataMapPoint mapPoint) {
		if (superDataBlock == null) {
			return true;
		}
		DataSelector dataSelector = superDataBlock.getDataSelector(dataName, false);
		if (dataSelector == null) {
			return true;
		}
		PamConnection pamCon = DBControlUnit.findConnection();
		if (pamCon == null) return true;
		SQLTypes sqlTypes = pamCon.getSqlTypes();
		PAMSelectClause superClause = dataSelector.getSQLSelectClause(sqlTypes);
		PAMSelectClause subClause;
//		if (mapPoint instanceof BinaryOfflineDataMapPoint) {
//			BinaryOfflineDataMapPoint binaryMapPoint = (BinaryOfflineDataMapPoint) mapPoint;
//			String str = String.format(" AND BinaryFile='%s'", binaryMapPoint.getBinaryFile(rootFolder))
//		}
		subClause = new PamViewParameters(mapPoint.getStartTime(), mapPoint.getEndTime());
		String str = String.format(" AND %s.UTC BETWEEN %s AND %s", 
				getSubLogging().getTableDefinition().getTableName(), 
				sqlTypes.formatDBDateTimeQueryString(mapPoint.getStartTime()),
				sqlTypes.formatDBDateTimeQueryString(mapPoint.getEndTime()));
		
		if (superClause==null) return false; 
		
		String totClause = superClause.getSelectClause(sqlTypes)+ str;
		subClause = new FixedClause(totClause);
//		Integer count = superDataBlock.getLogging().countSubTableItems(subClause, null);
//		if (count != null && count >0) {
//			System.out.println("Data count = " + count);
//			return true;
//		}
		Boolean any = superDataBlock.getLogging().anySubTableItems(subClause, null);
		if (any != null && any) {
			return true;
		}
		return false;
	}

	public void checkSubDetectionLinks() {
		//will need to reattach sub detections or ilters to work or nothing will pass
		if (superDataBlock != null) {
			superDataBlock.reattachSubdetections(null);
		}
	}



}
