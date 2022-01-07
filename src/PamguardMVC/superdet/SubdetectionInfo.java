package PamguardMVC.superdet;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamSubtableData;

/**
 * 
 * A list of these is held within a SuperDetection data unit. Ideally, a superDetection should always 
 * have it's full list of these infos, so that it can handle questions about start and end times of 
 * events, etc. However, references to the actual data units may be missing, if not in the current data load
 * period. That's fine, since all questions likely to be asked can come from the broad information. 
 * 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public class SubdetectionInfo<T extends PamDataUnit> implements Comparable<SubdetectionInfo<T>> {

	/**	
	 * Subdetection object may be null if data not loaded.  
	 */
	private T subDetection;
	
	/**
	 * Sub detection datablock, should never be null. Needed for 
	 * reattaching data units offline. 
	 */
	private PamDataBlock subDetDataBlock;

	/**
	 * The database index of the parent PamDataUnit
	 */
	private int parentID;
	
	/**
	 * The UID of the parent PamDataUnit
	 */
	private long parentUID;
	
	/**
	 * The long name of the subdetection datablock
	 */
	private String longName;
	
	/**
	 * The binary file where the PamDataUnit can be found
	 */
	private String binaryFilename;
	
	/**
	 * The UID of the subdetection
	 */
	private long childUID;
	
	/**
	 * Millisecond time of the child
	 */
	private long childUTC;
	
	/**
	 * Database index in child table. 
	 */
	private int childDatabaseIndex;
	
	
	/**
	 * @param subDetection2
	 */
	public SubdetectionInfo(T subDetection, int parentID, long parentUID) {
		this.subDetection = subDetection;
		childUID = subDetection.getUID();
		childUTC = subDetection.getTimeMilliseconds();
		/*
		 * Don't set this! They are not the same thing. 
		 */
//		childDatabaseIndex = subDetection.getDatabaseIndex();
		if (subDetection.getParentDataBlock() != null) {
			longName = subDetection.getParentDataBlock().getLongDataName();
		}
		if (subDetection.getDataUnitFileInformation() != null) {
			binaryFilename = subDetection.getDataUnitFileInformation().getShortFileName(Integer.MAX_VALUE);
		}
		this.parentID = parentID;
		this.parentUID = parentUID;
	}
	
	/**
	 * Constructor used when adding a new data unit that isn't yet in the database. 
	 * @param subDetection
	 * @param superDetection
	 */
	public SubdetectionInfo(T subDetection, SuperDetection superDetection) {
		this(subDetection, superDetection.getDatabaseIndex(), superDetection.getUID());
	}
	
	/**
	 * Constructor used when reading back from database. 
	 * @param subTableData data object from database. 
	 */
	public SubdetectionInfo(PamSubtableData subTableData) {
		childUID = subTableData.getChildUID();
		childUTC = subTableData.getChildUTC();
		childDatabaseIndex = (int) subTableData.getDbIndex(); // do set it here!
		longName = subTableData.getLongName();
		binaryFilename = subTableData.getBinaryFilename();
		parentID = subTableData.getParentID();
		parentUID = subTableData.getParentUID();
	}
	
	public T getSubDetection() {
		return subDetection;
	}

	public void setSubDetection(T subDetection) {
		this.subDetection = subDetection;
	}

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int linkID) {
		this.parentID = linkID;
	}

	public long getParentUID() {
		return parentUID;
	}

	public void setParentUID(long linkUID) {
		this.parentUID = linkUID;
	}

	/**
	 * Calls the PamDataUnit compareTo method on the passed SubdetectionInfo object's subdetection
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SubdetectionInfo detectionToCompareAgainst) {
		if (subDetection != null && detectionToCompareAgainst.getSubDetection() != null) {
			return subDetection.compareTo(detectionToCompareAgainst.getSubDetection());
		}
		// otherwise, do the comparison here. 
		long comp = childUTC - detectionToCompareAgainst.childUTC;
		if (comp != 0) {
			return Long.signum(comp);
		}
		comp = longName.compareTo(detectionToCompareAgainst.longName);
		if (comp != 0) {
			return Long.signum(comp);
		}
		comp = childUID-detectionToCompareAgainst.childUID;
		return Long.signum(comp);
	}

	/**
	 * @return the subDetDataBlock
	 */
	public PamDataBlock getSubDetDataBlock() {
		return subDetDataBlock;
	}

	/**
	 * @param subDetDataBlock the subDetDataBlock to set
	 */
	public void setSubDetDataBlock(PamDataBlock subDetDataBlock) {
		this.subDetDataBlock = subDetDataBlock;
	}

	/**
	 * @return the longName
	 */
	public String getLongName() {
		return longName;
	}

	/**
	 * @return the binaryFilename
	 */
	public String getBinaryFilename() {
		return binaryFilename;
	}

	/**
	 * @return the childUID
	 */
	public long getChildUID() {
		return childUID;
	}

	/**
	 * @return the childUTC
	 */
	public long getChildUTC() {
		return childUTC;
	}

	/**
	 * @return the chileDatabaseIndex
	 */
	public int getChildDatabaseIndex() {
		return childDatabaseIndex;
	}

	/**
	 * @param childDatabaseIndex the chileDatabaseIndex to set
	 */
	public void setChildDatabaseIndex(int childDatabaseIndex) {
		this.childDatabaseIndex = childDatabaseIndex;
	}

}
