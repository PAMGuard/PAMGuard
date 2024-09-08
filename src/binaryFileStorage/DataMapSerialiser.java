package binaryFileStorage;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;

public class DataMapSerialiser {


	/*
	 *  This was changed to binaryStore rather than offlinedataStore so when
	 *  it calls checkfilesExist and listAllFiles it can tell it the BinaryStore
	 *  to look in meaning the BinaryStore can be moved. but retain the same
	 *  mappoints as now the hold only the relative path.
	 *  
	 *    both checkfilesExist and listAllFiles currently rely on the mappoint
	 *    being of a binaryStore if undone they could just cast it to binaryStore
	 *    Graham Weatherup 11Jul2012
	 */
	private BinaryStore binaryStore;

	private ArrayList<String> allFiles = new ArrayList<String>();

	private boolean hasChanges;

	private  ArrayList<PamDataBlock> streams;

	private File serialisedFile;

	public DataMapSerialiser(BinaryStore binaryStore) {
		this.binaryStore = binaryStore;
	}

	public boolean loadDataMap(ArrayList<PamDataBlock> streams, File file) {
		this.streams = streams;
		this.serialisedFile = file;
		InputStream os;
		String streamName;
		List mapPoints;
		PamDataBlock dataBlock;
		int nRemovals;
		ObjectInputStream oos = null;
		try {
			os = new FileInputStream(file);
			oos = new ObjectInputStream(os);
		} catch (FileNotFoundException e) {
			System.out.println("Cached datamap file " + file.getAbsolutePath() + " not found.");
			return false;
		} catch (IOException e) {
			System.out.println("Cached datamap file " + file.getAbsolutePath() + " corrupted or unreadable.");
			e.printStackTrace();
			return false;
		}
		while (true) {
			try {
				OfflineDataMap dm;
				//			Object o;
				//			for (int i = 0; i < streams.size(); i++) {
				streamName = (String) oos.readObject();
				mapPoints = (List) oos.readObject();
				
				// if there are no map points, try to match up the stream using only the stream name
				if (mapPoints.isEmpty()) {
					dataBlock = findDataStream(streams, streamName);
				}
				
				// if there are map points, get the unit name and type from the header of the first map point and use those
				// as well as the stream name.  What we should be doing is changing findDataStream to match by the long name
				// instead of just the name, but changing it now may cause headaches for existing data maps.  Safer just
				// to add extra checks to make sure we're really matching up the correct information
				else {
					String mapPointsName = ((BinaryOfflineDataMapPoint) mapPoints.get(0)).getBinaryHeader().getModuleName();
					String mapPointsType = ((BinaryOfflineDataMapPoint) mapPoints.get(0)).getBinaryHeader().getModuleType();
					dataBlock = findDataStream(streams, streamName, mapPointsName, mapPointsType);
				}
				
				if (dataBlock == null) {
					System.out.println("No data block match found for data map stream " + streamName);
					continue;
				}
				System.out.println("Matched data map stream " + streamName + " to data block " + dataBlock.getLongDataName());
				dm = dataBlock.getOfflineDataMap(binaryStore);
				if (dm == null) {
					continue;
				}
				nRemovals = checkFilesExist(mapPoints);
				if (nRemovals > 0) {
					System.out.println(String.format("%d files referred to in the serialised data map for %s cannot be found. This will occurr if the ",
							nRemovals, dataBlock.getDataName()));
					System.out.println("   binary storage location has moved of if files were deleted and may not be a problem");
				}
				dm.setMapPoints(mapPoints);
				dm.setParentDataBlock(dataBlock);
				dm.sortRanges();
				listAllFiles(mapPoints);
			} catch (EOFException eof) {
				break;
			}catch (IOException e) {
				System.out.println("Error reading datamap file " + file.getAbsolutePath() + " - not all data map streams may have been matched to data blocks");
				System.out.println(e.getMessage());
//				e.printStackTrace();
				break;
				//			break;
			} catch (ClassNotFoundException e) {
				System.out.println("Error reading datamap file " + file.getAbsolutePath() + " - not all data map streams may have been matched to data blocks");
//				e.printStackTrace();
				break;
			} 
			
		}
		try {
			oos.close();
		} catch (IOException e) {
//			e.printStackTrace();
			System.out.println("loadDataMap: " + e.getMessage());
		}
		/*
		 * Now sort the files so we can use a fast find algorithm later on
		 */
		Collections.sort(allFiles);
		return true;
	}

	private int checkFilesExist(List<BinaryOfflineDataMapPoint> mapPoints) {
		/*
		 * Check all the files in the list exist - and remove them 
		 * if they don't. Start at the back of the list and remove
		 * from there - will be quicker for an array list. 
		 */
		if (mapPoints == null || mapPoints.size() == 0) {
			return 0;
		}
		int nRemovals = 0;
		ListIterator<BinaryOfflineDataMapPoint> it = mapPoints.listIterator(mapPoints.size());
		BinaryOfflineDataMapPoint aPoint;
		File file;
		while (it.hasPrevious()) {
			aPoint = it.previous();
			file = aPoint.getBinaryFile(binaryStore);
			if (file == null || !file.exists()) {
				it.remove();
				nRemovals ++;
			}
		}
		return nRemovals;
	}

	private void listAllFiles(List<BinaryOfflineDataMapPoint> mapPoints) {
		ListIterator<BinaryOfflineDataMapPoint> it = mapPoints.listIterator();
		BinaryOfflineDataMapPoint aPoint;
		File aFile;
		while (it.hasNext()) {
			aPoint = it.next();
			aFile = aPoint.getBinaryFile(binaryStore);
			allFiles.add(aFile.getName());
		}
	}

	/**
	 * Search to see if a file exists already, in which case it won't 
	 * be necessary to add it to the data map. 
	 * @param aFile file to search for. Note that the search is done only on the 
	 * final name part of the file. 
	 * @return index of file in list or -1 if not found
	 */
	public int findFile(File aFile) {
		String fileName = aFile.getName();
		return Collections.binarySearch(allFiles, fileName);
	}

	/**
	 * Remove a file from the datamap 
	 * @param fileIndex index of file
	 * @return true if file was found and removed. 
	 */
	public boolean removeFileAtIndex(int fileIndex) {
		try {
			String a = allFiles.remove(fileIndex);
			return (a != null);
		}
		catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	private PamDataBlock findDataStream(ArrayList<PamDataBlock> streams, String streamName) {
		for (int i = 0; i < streams.size(); i++) {
			if (streams.get(i).getDataName().equals(streamName)) {
				return streams.get(i);
			}
		}
		return null;
	}
	
	private PamDataBlock findDataStream(ArrayList<PamDataBlock> streams, String streamName, String mapName, String mapType) {
		for (int i = 0; i < streams.size(); i++) {
			if (streams.get(i).getDataName().equals(streamName) &&
					streams.get(i).getParentProcess().getPamControlledUnit().getUnitName().equals(mapName) &&
					streams.get(i).getParentProcess().getPamControlledUnit().getUnitType().equals(mapType) )
					{
				return streams.get(i);
			}
		}
		return null;
	}
	
	

	public boolean saveDataMaps() {
		OutputStream os;
		try {
			os = new FileOutputStream(serialisedFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);
			OfflineDataMap dm;
			Object o;
			for (int i = 0; i < streams.size(); i++) {
				dm = streams.get(i).getOfflineDataMap(binaryStore);
				if (dm == null) {
					continue;
				}
				oos.writeObject(streams.get(i).getDataName());
				oos.writeObject(dm.getMapPoints());
			}
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void setHasChanges(boolean hasChanges) {
		this.hasChanges = hasChanges;
	}

	/**
	 * @return the hasChanges
	 */
	public boolean isHasChanges() {
		return hasChanges;
	}

	/**
	 * @return the serialisedFile
	 */
	public File getSerialisedFile() {
		return serialisedFile;
	}
}
