package binaryFileStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryStore.BinaryDataMapMaker;
import dataGram.Datagram;
import dataGram.DatagramDataPoint;
import dataGram.DatagramManager;
import dataGram.DatagramPoint;
import dataGram.DatagramProvider;

/**
 * A binary offline data map is a datamap for a single data stream (i.e. the output 
 * of a PamDataBlock). <br>
 * For each binary file, one map point will be created in a list within the datamap 
 * which gives basic information about the data within that time period. The data
 * include the headers and footers read from either the data file or the index file and 
 * possibly also a Datagram - which gives more detail of the data than the simple counts
 * of detections. 
 * <br>Individual modules may also override the data map points within the datamap in order
 * to provide more detailed information, such as numbers of clicks splitup by species. 
 * @author Doug Gillespie
 *
 */
public class BinaryOfflineDataMap extends OfflineDataMap<BinaryOfflineDataMapPoint> {

	transient private BinaryDataSource binaryDataSource;
	transient private BinaryStore binaryStore;

	public BinaryOfflineDataMap(BinaryStore binaryStore, PamDataBlock parentDataBlock) {
		super(binaryStore, parentDataBlock);
		this.binaryStore = binaryStore;
		binaryDataSource = parentDataBlock.getBinaryDataSource();
	}

	/**
	 * Get a list of map points which have data between the two times. 
	 * @param dataStart start time in milliseconds
	 * @param dataEnd end time in milliseconds
	 * @return Array list of map points
	 */
	protected ArrayList<BinaryOfflineDataMapPoint> getFileList(long dataStart, long dataEnd) {
		ArrayList<BinaryOfflineDataMapPoint> fileList = new ArrayList<BinaryOfflineDataMapPoint>();
		Iterator<BinaryOfflineDataMapPoint> list = getListIterator();
		BinaryOfflineDataMapPoint aMapPoint;
		while (list.hasNext()) {
			aMapPoint = list.next();
			if (aMapPoint.coincides(dataStart, dataEnd)) {
				fileList.add(aMapPoint);
			}
		}
		return fileList;
	}
	
	
	
	/**
	 * Finds the mapPoint of a specific binary file 
	 * @param file
	 * @return
	 */
	public BinaryOfflineDataMapPoint findMapPoint(File file) {
		if (file == null) {
			return null;
		}
		Iterator<BinaryOfflineDataMapPoint> list = getListIterator();
		BinaryOfflineDataMapPoint aMapPoint;
		while (list.hasNext()) {
			aMapPoint = list.next();
			if (file.getName().equals(aMapPoint.getName())) {
				return aMapPoint;
			}
		}	
		return null;
	}


	/**
	 * Finds the mapPoint of a specific binary file 
	 * @param file
	 * @return
	 */
	public int removeMapPoint(File file) {
		if (file == null) {
			return 0;
		}
		int nRemoved = 0;
		Iterator<BinaryOfflineDataMapPoint> list = getListIterator();
		BinaryOfflineDataMapPoint aMapPoint;
		while (list.hasNext()) {
			aMapPoint = list.next();
			if (file.getName().equals(aMapPoint.getName())) {
				list.remove();
				nRemoved++;
			}
		}	
		return nRemoved;
	}

	@Override
	public BinaryOfflineDataMapPoint newPamDataUnit(PamDataUnit pamDataUnit) {
		BinaryOfflineDataMapPoint mapPoint = super.newPamDataUnit(pamDataUnit);
		if (getParentDataBlock().getDatagramProvider() != null && mapPoint != null) {
			addtoDataGram(mapPoint, pamDataUnit);
		}
		return mapPoint;
	}

	private void addtoDataGram(BinaryOfflineDataMapPoint mapPoint, PamDataUnit pamDataUnit) {
		DatagramProvider dataGramProvider = getParentDataBlock().getDatagramProvider(); 
		Datagram dataGram = mapPoint.getDatagram();
		DatagramManager datagramManager = binaryStore.getDatagramManager();
		int dataGramSecs = datagramManager.getDatagramSettings().datagramSeconds;
		
		long dataGramMillis = dataGramSecs * 1000L;
		int gramLength = dataGramProvider.getNumDataGramPoints();
		DatagramDataPoint datagramPoint = null;
		
		
		if (dataGram == null) {
			dataGram = new Datagram(dataGramSecs);
			long pStart = mapPoint.getStartTime();
			long pEnd = pStart;// + dataGramMillis;
			 datagramPoint = new DatagramDataPoint(dataGram, pStart, pEnd, gramLength);
			dataGram.addDataPoint(datagramPoint);
			mapPoint.setDatagram(dataGram);
		}
		else {
			datagramPoint = dataGram.getLastDataPoint();
		}
		/*
		 *  now go through the datagram and make sure that we've actually got data points that go up to the
		 *  time of this data unit ... 
		 */
		while (pamDataUnit.getTimeMilliseconds() >= datagramPoint.getStartTime()+dataGramMillis) {

			datagramPoint.setEndTime(datagramPoint.getStartTime()+dataGramMillis);
			long newStart =  datagramPoint.getEndTime();
			long newEnd = newStart + dataGramMillis;
			datagramPoint = new DatagramDataPoint(dataGram, newStart, newEnd, gramLength);
			dataGram.addDataPoint(datagramPoint);
		}
		datagramPoint.setEndTime(pamDataUnit.getTimeMilliseconds()+1);
		dataGramProvider.addDatagramData(pamDataUnit, datagramPoint.getData());
	}

}
