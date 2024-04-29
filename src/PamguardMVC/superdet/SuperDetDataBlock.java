package PamguardMVC.superdet;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.springframework.core.GenericTypeResolver;

import PamController.PamController;
import PamguardMVC.DataUnitFinder;
import PamguardMVC.DataUnitMatcher;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;
import binaryFileStorage.DataUnitFileInformation;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import generalDatabase.PamSubtableData;
import generalDatabase.SQLLogging;
import generalDatabase.SuperDetLogging;
import pamScrollSystem.LoadQueueProgressData;
import pamScrollSystem.ViewLoadObserver;

public class SuperDetDataBlock<Tunit extends SuperDetection, TSubDet extends PamDataUnit> extends PamDataBlock<Tunit> {


	public enum ViewerLoadPolicy {LOAD_ALWAYS_EVERYTHING, LOAD_OVERLAPTIME, LOAD_UTCNORMAL};
	
	private ViewerLoadPolicy viewerLoadPolicy = ViewerLoadPolicy.LOAD_OVERLAPTIME;
	
	/**
	 * Object containing all the information found in any SQLLogging subtables,
	 * relating subdetections to PamDataUnits held in this PamDataBlock. This is
	 * only used to temporarily store the data while it is being loaded from the
	 * database. Once all data is loaded and the correct objects are properly linked
	 * together, this list is cleared. Therefore an empty list means either all data
	 * units have already had their subdetections added, or there are no
	 * subdetections at all.
	 */
	private ArrayList<PamSubtableData> subtableData = new ArrayList<PamSubtableData>();

	private Class<TSubDet> subDetectionClass;
	
	public SuperDetDataBlock(Class unitClass, String dataName, PamProcess parentProcess, int channelMap,
			boolean isOffline, ViewerLoadPolicy viewerLoadPolicy) {
		super(unitClass, dataName, parentProcess, channelMap, isOffline);
		this.setViewerLoadPolicy(viewerLoadPolicy);
		resolveSubdetClass();
	}

	public SuperDetDataBlock(Class unitClass, String dataName, PamProcess parentProcess, int channelMap, ViewerLoadPolicy viewerLoadPolicy) {
		super(unitClass, dataName, parentProcess, channelMap);
		this.setViewerLoadPolicy(viewerLoadPolicy);
		resolveSubdetClass();
	}
	
	/**
	 * Work out the base class type of TSubDet. This is done by having a dummy public method
	 * which has TSubDet as a return type, then looking for the return trype of that method. 
	 * @return true if it managed to work it out. 
	 */
	private boolean resolveSubdetClass() {
		try {
			Class thisClass = this.getClass();
			Method method = thisClass.getMethod("dummyClassResolve");
			subDetectionClass = (Class<TSubDet>) GenericTypeResolver.resolveReturnType(method, thisClass);
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
		return true;
	}

	/**
	 * This is a dummy method which is not used for anything apart from 
	 * a call to resolve the return type. 
	 * @return null. This is never used, but don't delete it!
	 */
	public TSubDet dummyClassResolve() {
		return null;
	}
	
	public Class<TSubDet> getSubDetectionClass() {
		return subDetectionClass;
	}

	/**
	 * Return whether this type of super detection can hold the passed sub 
	 * detection data. Default is based on class compatibility, but some super det
	 * datablocks should override this if they only manage one stream of data. 
	 * @param subDataBlock Sub detection data block
	 * @return true if this data can hold those sub data. 
	 */
	public boolean canSuperDetection(PamDataBlock subDataBlock) {
		if (subDetectionClass == null || subDataBlock.getUnitClass() == null) {
			return false;
		}
		else {
			return subDetectionClass.isAssignableFrom(subDataBlock.getUnitClass());
		}
	}

//	/**
//	 * Goes through the subtableData list and tries to find all the subdetections
//	 * and add them back to their data units within this PamDataBlock. If
//	 * successful, the subtableData array list should be empty when this method
//	 * returns.
//	 * 
//	 * @return true if all subdetections were found and added back to their
//	 *         respective data units. false if there were any problems
//	 */
//	public boolean reattachSubdetections(ViewLoadObserver viewLoadObserver) {
//		boolean success = true;
//
//		if (getLogging() == null) {
//			return success;
//		}
//		if (getLogging().getSubtable() == null) {
//			return success;
//		}
//
//		// list of PamDataUnits we've found so far in this data block
//		ArrayList<SuperDetection> dataUnitsFound = new ArrayList<SuperDetection>();
//
//		// list of PamDataBlocks we've found so far that match subdetections
//		ArrayList<PamDataBlock> dataBlocksFound = new ArrayList<PamDataBlock>();
//
//		// start at beginning of table list and cycle through, getting the database
//		// index of each
//		// item. If it matches the index of the PamDataUnit we just found, try to match
//		// up the subdetection
//		SuperDetection curDataUnit = null;
//		PamDataBlock subBlock = null;
//		ListIterator<PamSubtableData> iter = subtableData.listIterator();
//		int i = 0;
//		int n = subtableData.size();
//		if (n == 0) {
//			Debug.out.println("No subtable data to match in " + getDataName());
//			return success;
//		}
//		int step = Math.max(n / 100, 1);
//		long loadStart = System.currentTimeMillis();
//		int nMatched = 0;
//		// iterating through all subtable data - all parents and all children.
//		while (iter.hasNext()) {
//			i++;
//			if (viewLoadObserver != null && i % step == 1) {
//				viewLoadObserver.sayProgress(LoadQueueProgressData.STATE_LINKINGSUBTABLE, 0, n, i, i);
//			}
//
//			// System.out.printf("Linking subtable data item %d of %d\n", i, n);
//
//			// get the database index of the current data unit we want to match
//			PamSubtableData curTableData = iter.next();
//			int dbIndexToLookFor = curTableData.getParentID();
//
//			// first try to find the PamDataBlock of the subdetection. If we can't find it,
//			// continue on to the next item in the table list but set the return boolean to
//			// false so the calling function knows that the matching failed at some stage
//			// check through our data block list to see if we've already found this one. If
//			// not,
//			// then find it
//			String subName = curTableData.getLongName();
//			boolean found = false;
//			/*
//			 * First see if the sub loggings own parent datablock is the correct one ...
//			 */
//			if (getLogging().getSubtable() != null) {
//				PamDataBlock subDataBlock = getLogging().getSubtable().getPamDataBlock();
//				if (subDataBlock != null && subDataBlock.getLongDataName().equals(subName)) {
//					found = true;
//					subBlock = subDataBlock;
//				}
//			}
//			if (!found)
//				for (int j = 0; j < dataBlocksFound.size(); j++) {
//					if (dataBlocksFound.get(j).getLongDataName().equals(subName)) {
//						found = true;
//						subBlock = dataBlocksFound.get(j);
//						break;
//					}
//				}
//			if (!found) {
//				subBlock = PamController.getInstance().getDataBlockByLongName(subName);
//				if (subBlock == null) {
//					success = false;
//					continue;
//				}
//				dataBlocksFound.add(subBlock);
//			}
//			/**
//			 * Now we've found the datablock, do a very quick check to see if the datablock
//			 * has data covering this sub detection - when processing offline clicks, there
//			 * often isn't so this should speed everything up a lot.
//			 */
//			PamDataUnit firstUnit = subBlock.getFirstUnit();
//			PamDataUnit lastUnit = subBlock.getLastUnit();
//			if (firstUnit == null || lastUnit == null) {
//				success = false;
//				continue;
//			}
//			if (firstUnit.getTimeMilliseconds() - 1000 > curTableData.getChildUTC()
//					|| lastUnit.getTimeMilliseconds() + 1000 < curTableData.getChildUTC()) {
//				success = false;
//				continue;
//			}
//
//			// check through our data unit list to see if we've already found this one. If
//			// not,
//			// then find it
//			found = false;
//			for (int j = 0; j < dataUnitsFound.size(); j++) {
//				if (dataUnitsFound.get(j).getDatabaseIndex() == dbIndexToLookFor) {
//					found = true;
//					curDataUnit = dataUnitsFound.get(j);
//					break;
//				}
//			}
//			if (!found) {
//				curDataUnit = findByDatabaseIndex(dbIndexToLookFor);
//				if (curDataUnit == null) {
//					success = false;
//					continue;
//				}
//				dataUnitsFound.add(curDataUnit);
//			}
//
//			// now try to find the PamDataUnit within the subdetections PamDataBlock,
//			// by matching the UID. If found, add the unit as a subdetection and
//			// remove that item from the table list. If not found, continue on to
//			// the next item in the table list but set the return boolean to
//			// false so the calling function knows that the matching failed at some stage
//			long subUID = curTableData.getChildUID();
//			PamDataUnit subUnit = subBlock.findUnitByUIDandUTC(subUID, curTableData.getChildUTC());
//			if (subUnit == null) {
//				success = false;
//				continue;
//			} else {
////				subUnit.setDatabaseIndex((int) curTableData.getChildUID()); 
//				// special case - if we're dealing with an OfflineEventDataUnit here, we want to
//				// call a
//				// special version of addSubDetection that won't increment it's nClicks field,
//				// because
//				// that field was set previously during the database load. If we call the
//				// standard
//				// addSubDetection here the nClicks field will end up doubled
//				if (curDataUnit instanceof OfflineEventDataUnit) {
//					((OfflineEventDataUnit) curDataUnit).addSubDetection(subUnit, false);
//				} else {
//					curDataUnit.addSubDetection(subUnit);
//				}
//				nMatched++;
//
//				// find the subdetection we just added, and log it's database index as well
//				int indexPos = curDataUnit.findSubdetectionInfo(subUnit);
//				if (indexPos < 0) {
//					indexPos = curDataUnit.findSubdetectionInfo(subUnit);
//				}
//				SubdetectionInfo whatWeJustAdded = curDataUnit.getSubdetectionInfo(indexPos);
//				whatWeJustAdded.setDbIndex(curTableData.getDbIndex());
//				whatWeJustAdded.setParentID(curTableData.getParentID());
//
//				// remove the subdetection from the subtable list, since we've successfully
//				// relinked it
//				// iter.remove();
//			}
//		}
//		Debug.out.printf("%s matched %d sub detections\n", this.getDataName(), nMatched);
//		return success;
//	}
	/**
	 * Returns the list of subtable data that gets generated when the PamDataBlock
	 * is loaded from the database. The list contains information about all the
	 * subdetections in each of the PamDataUnits contained in the PamDataBlock. It
	 * will be used to re-attach the subdetections
	 * 
	 * @return
	 */
	public ArrayList<PamSubtableData> getSubtableData() {
		return this.subtableData;
	}

	/**
	 * Called from the database when subtable data are loaded. The SuperDetection data units
	 * will have been loaded at this point, to it's OK to go ahead and sort the subtable data
	 * into their SuperDetecitons. However, its possible that the binary data won't have loaded
	 * yet, so it may not yet be possible to link the SubdetectionInfo objects to their 
	 * subdetection data units. That is done in reattach subdetections. 
	 * @param subTableDataList
	 * @param loadObserver 
	 */
	public void setSubtableData(ArrayList<PamSubtableData> subTableDataList, ViewLoadObserver loadObserver) {
		
		clearAllSubtableData();
		
		this.subtableData = subTableDataList;
		/*
		 *  add sub table data to appropriate data units in this datablock.
		 *  Start by sorting it by the type of data (based on dataName) then on data unit
		 *  based on time, then it should find all the right datablocks and data units super 
		 *  efficiently. Hopefully, the PamSubTableData will already have been pulled out 
		 *  of the database in this order. If that is the case, then the sort will be super
		 *  fast in any case.  
		 */
		/*
		 * Wrong - need to sorty by superdet UTC only so that they can match into the data in 
		 * this data block. 
		 */
		if (subtableData == null) {
			return;
		}
		Collections.sort(subtableData, new SubTableDataComparator());
		// now work through and find the datablocks and then the data units for each subtabledata
//		PamDataBlock childDataBlock = null;
//		String childBlockLongName = null;
		Tunit superDet = null;
		long superUID = -1;
		int superID = -1;
		PamDataBlock subDetDataBlock = null;
		ListIterator<PamSubtableData> it = subtableData.listIterator();
		int nDone = 0;
		int nMatch = 0, nNoMatch = 0;
		String lastFail = "";
		synchronized (this.getSynchLock()) {
			ListIterator<Tunit> superDetIterator = getListIterator(0); 
			int searchMax = getUnitsCount();
			while (it.hasNext()) {
				PamSubtableData psd = it.next();
				if (++nDone%1000 == 0 && loadObserver != null) {
					loadObserver.sayProgress(LoadQueueProgressData.STATE_LINKINGSUBTABLE, 0, subtableData.size(), nDone, nDone);
				}
				if (superUID != psd.getParentUID() || superID != psd.getParentID()) {
					//				superDetection = findByDatabaseIndex(psd.getParentID());
					// it back and forth until we find the correct super detection
					//				findSuperDetection(superDetIterator, psd);
					// need to do everything in place here in case we need to recreate the iterator. 
					// back up a little as needed ...
					// if we're not at the start, then superDet will not be null
					superUID = psd.getParentUID(); // remember these whatever happens to avoid researching for
					superID = psd.getParentID(); // something that doesnt' exist. 
					if (superDet != null) {
						while (superDetIterator.hasPrevious() && superDet.getTimeMilliseconds() >= psd.getChildUTC()) {
							superDet = superDetIterator.previous();
						}
					}
					int searchCount = 0;
					boolean found = false;
					// now work fowards as normal. 
					while (superDetIterator.hasNext()) {
						superDet = superDetIterator.next();
						searchCount++;
						if (isMatch(superDet, psd)) {
							found = true;
							break;
						}
					}
					if (!found) {
						superDetIterator = getListIterator(0); // reset to start. 
						// use searchcount to make sure we don't go round and round. 
						while (superDetIterator.hasNext() && searchCount++ <= searchMax) {
							superDet = superDetIterator.next();
							if (isMatch(superDet, psd)) {
								found = true;
								break;
							}
							
						}
					}
					if (!found) {
						superDet = null;
						Debug.out.printf("Can't find data unit id %d, UID %d in %s\n", superID, superUID, getDataName());
					}
				}
				if (superDet == null) {
					nNoMatch++;
					continue;
				}
				nMatch++;
				// the super dets dont' actually get subtableData, they get a  SubDetectionInfo!
				// we do also have to find the correct datablock at this point to include in the 
				// subdetectioninfo. 
				if (subDetDataBlock == null || !subDetDataBlock.getLongDataName().equals(psd.getLongName())) {
					subDetDataBlock = findSubDetDataBlock(psd.getLongName());
				}
				if (subDetDataBlock == null) {
					if (psd.getLongName().equals(lastFail) == false) {
						System.out.printf("Unable to find %s subdetections datablock %s\n", getDataName(), psd.getLongName());
						lastFail = psd.getLongName();
					}
					continue;
				}
				SubdetectionInfo<TSubDet> subDetInfo = new SubdetectionInfo<TSubDet>(psd);
				superDet.addSubDetectionInfo(subDetInfo);
			}
			Debug.out.printf("Matched %d of %d sub detections (%d failed) to data in %s\n", nMatch, subtableData.size(), nNoMatch, getDataName());
			/*
			 * Wrong below ! first we're just looking for the data units in THIS datablock, then those 
			 * dataunits will need to look for the datablocks of thier own sub detections and find 
			 * their units. This code is needed to find the actual 
			 */
//			if (childBlockLongName == null || childBlockLongName.equalsIgnoreCase(psd.getLongName()) == false) {
//				// the name of the datablock has changed, so find the datablock. 
//				childDataBlock = PamController.getInstance().getDataBlockByLongName(psd.getLongName());
//				childBlockLongName = psd.getLongName();
//				if (childDataBlock == null) {
//					System.out.printf("Unable to locate datablock %s for sub detection data in %s\n", childBlockLongName, getDataName());
//				}
//			}
//			if (childDataBlock == null) {
//				// this needs to call every time. The above warning only prints the first time we looked for the datablock. 
//				continue;
//			}
			// now find the data unit within that datablock. synchronisation ?????????
		}
	}
	
	/**
	 * Clear all subtable data from all data units in a datablock. 
	 */
	private void clearAllSubtableData() {
		synchronized (getSynchLock()) {
			ListIterator<Tunit> duIt = getListIterator(0);
			while (duIt.hasNext()) {
				duIt.next().clearSubDetectionData();
			}
		}
	}

	/**
	 * New version of this call that only has to link data units into their sub detections
	 * since the SubdetectionInfo lists are already in place in the SuperDetections.
	 * @param viewLoadObserver
	 * @return
	 */
	public boolean reattachSubdetections(ViewLoadObserver viewLoadObserver) {
		if (getUnitsCount() == 0) {
			return true;
		}
		Debug.out.println("Reattach sub detections in " + getLongDataName());
		long firstTime = getFirstUnit().getTimeMilliseconds();
		long lastTime = getLastUnit().getEndTimeInMilliseconds();
		synchronized (getSynchLock()) {
			ListIterator<Tunit> duIt = getListIterator(0);
			DataUnitFinder<PamDataUnit> subDetectionFinder = null;
			String currentSubBlockName = "";
			// do everything in one big loop here, so that we can use a 
			// single data unit finder to speed things up. 
			int iDone = 0;
			while (duIt.hasNext()) {
				Tunit aData = duIt.next(); // looping through superdetections.
//				if (aData.getDatabaseIndex() == 566) {
//					System.out.println("On event 566");
//				}
				if (viewLoadObserver != null) {
					viewLoadObserver.sayProgress(1, aData.getTimeMilliseconds(), firstTime, lastTime, iDone++);
				}
				List<SubdetectionInfo> subDets = aData.getSubDetectionInfo();
				if (subDets == null || subDets == null) {
					continue;
				}
				for (SubdetectionInfo sdInfo : subDets) {
					if (sdInfo.getLongName().equals(currentSubBlockName) == false) {
						currentSubBlockName = sdInfo.getLongName();
						PamDataBlock subDetDataBlock = findSubDetDataBlock(sdInfo.getLongName());
						// that may be null if the datablock has been removed!
						if (subDetDataBlock != null) {
							subDetectionFinder = new DataUnitFinder<>(subDetDataBlock, new SubTableMatcher());
						}
						else {
							subDetectionFinder = null;
						}
					}
					if (subDetectionFinder != null && subDetectionFinder.inUTCRange(sdInfo.getChildUTC())) {
						PamDataUnit subDet = subDetectionFinder.findDataUnit(sdInfo);
						sdInfo.setSubDetection(subDet);
						if (subDet != null) {
							aData.addSubDetection(subDet);
							subDet.addSuperDetection(aData);
						}
					}
					else {
						sdInfo.setSubDetection(null);
					}
				}
//				aData.weedMissingSubDetections();
			}
		}
		return true;
	}
	
	/**
	 * find the datablock for the subdetection. Generally, this is just the normal datablock, but in 
	 * some silly circumstances, it may need to change to something more bespoke (e.g. in QA module). 
	 * @param dataLongName data name
	 * @return data block,null if nothing found. 
	 */
	public PamDataBlock findSubDetDataBlock(String dataLongName) {
		PamDataBlock subDetDataBlock = PamController.getInstance().getDataBlockByLongName(dataLongName);
		return subDetDataBlock;
	}
	
	/**
	 * Data unit matcher that works with PamSubtableData using time and UID as criteria. 
	 * @author Doug Gilespie
	 *
	 */
	private class SubTableMatcher implements DataUnitMatcher {

		@Override
		public int match(PamDataUnit dataUnit, Object... criteria) {
			SubdetectionInfo subTableData = (SubdetectionInfo) criteria[0];
			long comp = subTableData.getChildUTC() - dataUnit.getTimeMilliseconds();
			if (comp != 0) {
				return Long.signum(comp);
			}
			comp = subTableData.getChildUID() - dataUnit.getUID();
			if (comp == 0) {
				return 0; 
			}
			/**
			 * Some problems in some datasets where the uid is corrupt. So also check 
			 * on binary file information. This is slower, but since we've already matched on 
			 * time, it doesn't get called very often. 
			 */
			String dbBin = subTableData.getBinaryFilename();
			DataUnitFileInformation duFileInfo = dataUnit.getDataUnitFileInformation();
			if (dbBin == null || duFileInfo == null) {
				return Long.signum(comp);
			}
//			String duBin = duFileInfo.get
			/*
			 * Note that some bin names are truncated, so need to match on startswith. 
			 */
			File binFile = duFileInfo.getFile();
			if (binFile == null) {
				return Long.signum(comp);
			}
			String fName = binFile.getName();
			if (fName.startsWith(dbBin) == false) {
				return Long.signum(comp);
			}
			/*
			 *  hope for a non null click number. If there isn't onw, then we'll just have to
			 *  go with the bin file being the same and the millis being the same. It's important
			 *  for clicks though since there can be >1 in a millisecond.  
			 */
			Integer clickNo = subTableData.getClickNumber();
			if (clickNo == null) {
				return 0;
			}
			else {
				subTableData.setChildUID(dataUnit.getUID());
				dataUnit.updateDataUnit(System.currentTimeMillis());
				return (int) (clickNo - duFileInfo.getIndexInFile());
			}
			
		}
		
	}

	private boolean isMatch(Tunit superDet, PamSubtableData psd) {
		if (superDet == null) {
			return false;
		}
		return (superDet.getDatabaseIndex() == psd.getParentID() && superDet.getUID() == psd.getParentUID());
	}


	public SubdetectionInfo<TSubDet> makeSubDetectionInfo(TSubDet subDetection, Tunit superDetection) {
		return new SubdetectionInfo<TSubDet>(subDetection, superDetection.getDatabaseIndex(), superDetection.getUID());
	}

	@Override
	public SuperDetLogging getLogging() {
		return (SuperDetLogging) super.getLogging();
	}

	@Override
	public boolean clearOnViewerLoad() {
		if (viewerLoadPolicy == ViewerLoadPolicy.LOAD_ALWAYS_EVERYTHING) {
			return false;
		}
		else {
			return true;
		}
	}
	/**
	 * @return the viewerLoadPolicy
	 */
	public ViewerLoadPolicy getViewerLoadPolicy() {
		return viewerLoadPolicy;
	}

	/**
	 * @param viewerLoadPolicy the viewerLoadPolicy to set
	 */
	public void setViewerLoadPolicy(ViewerLoadPolicy viewerLoadPolicy) {
		this.viewerLoadPolicy = viewerLoadPolicy;
	}

}
