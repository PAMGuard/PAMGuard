package tethys.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import Acquisition.DaqStatusDataUnit;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import SoundRecorder.RecordingInfo;
import binaryFileStorage.BinaryStore;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import pamMaths.STD;
import tethys.TethysControl;
import tethys.output.TethysExportParams;
import tethys.pamdata.TethysDataProvider;

/**
 * functions for working out total effort and periods of recording from a variety of sources, which may be
 * the recordings database, binary files, etc. 
 * @author dg50
 *
 */
public class EffortFunctions {

	private TethysControl tethysControl;


	/**
	 * @param tethysControl
	 */
	public EffortFunctions(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
	}

	private DeploymentOverview createOverview(RecordingList tempPeriods) {

		DutyCycleInfo dutyCycleinfo = assessDutyCycle(tempPeriods);
		if (dutyCycleinfo == null) {
			return null;
		}

		// if it's duty cycles, then we only want a single entry. 
		RecordingList deploymentPeriods;
		if (dutyCycleinfo.isDutyCycled == false) {
			deploymentPeriods = tempPeriods;
		}
		else {
			deploymentPeriods = new RecordingList();
			deploymentPeriods.add(new RecordingPeriod(tempPeriods.get(0).getRecordStart(), tempPeriods.get(tempPeriods.size()-1).getRecordStop()));
		}
		/*
		 * do another sort of the deploymentPeriods. The start stops were in the order they went into the 
		 * database in the hope that pairs were the right way round. Now check all data are/
		 */
		Collections.sort(deploymentPeriods, new Comparator<RecordingPeriod>() {
			@Override
			public int compare(RecordingPeriod o1, RecordingPeriod o2) {
				return (int) (o1.getRecordStart()-o2.getRecordStart());
			}
		});

		DeploymentOverview deploymentOverview = new DeploymentOverview(dutyCycleinfo, deploymentPeriods);
		return deploymentOverview;
	}


	public DeploymentOverview makeRecordingOverview() {
		
		RecordingList recordingPeriods = listSoundAcquisitionDatabase();
		
		RecordingList binaryPeriods = listBinaryFiles();
		
		long l1 = listDuration(recordingPeriods);
		long l2 = listDuration(binaryPeriods);
		if (listDuration(binaryPeriods) > listDuration(recordingPeriods)) {
			recordingPeriods = binaryPeriods;
		}
		
		DeploymentOverview deploymentOverview = createOverview(recordingPeriods);
		
		return deploymentOverview;
	}
	
	private long listDuration(RecordingList recordingList) {
		if (recordingList == null) {
			return -1;
		}
		return recordingList.duration();
	}

	public RecordingList listBinaryFiles() {
		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		if (binaryStore == null) {
			return null;
		}
		RecordingList bestList = null;
		ArrayList<PamDataBlock> allBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aBlock : allBlocks) {
			OfflineDataMap dataMap = aBlock.getOfflineDataMap(binaryStore);
			if (dataMap == null) {
				continue;
			}
			TethysDataProvider tethysProvider = aBlock.getTethysDataProvider(tethysControl);
			if (tethysProvider == null) {
				continue; // do we really need this ? 
			}
			RecordingList blockList = listMapPoints(dataMap);
			if (blockList == null) {
				continue;
			}
			if (bestList == null) {
				bestList = blockList;
			}
			else {
				long l1 = bestList.duration();
				long l2 = blockList.duration();
				if (l2>l1) {
					bestList = blockList;
				}
			}
		}
		bestList = mergeRecordings(bestList);
		return bestList;
	}
	
	
	public RecordingList listMapPoints(OfflineDataMap dataMap) {
		List<OfflineDataMapPoint> mapPoints = dataMap.getMapPoints();
		if (mapPoints == null) {
			return null;
		}
		RecordingList periods = new RecordingList();
		for (OfflineDataMapPoint mapPoint : mapPoints) {
			periods.add(new RecordingPeriod(mapPoint.getStartTime(), mapPoint.getEndTime()));
		}
		return periods;
	}
	
	
	
	public RecordingList listSoundAcquisitionDatabase() {
		// first find an acquisition module.
		PamControlledUnit aModule = PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (!(aModule instanceof AcquisitionControl)) {
			// will return if it's null. Impossible for it to be the wrong type.
			// but it's good practice to check anyway before casting.
			return null;
		}
		// cast it to the right type.
		AcquisitionControl daqControl = (AcquisitionControl) aModule;
		AcquisitionParameters daqParams = daqControl.getAcquisitionParameters();
		/**
		 * The daqParams class has most of what we need about the set up in terms of sample rate,
		 * number of channels, instrument type, ADC input range (part of calibration), etc.
		 * It also has a hydrophone list, which maps the input channel numbers to the hydrophon numbers.
		 * Realistically, this list is always 0,1,2,etc or it goes horribly wrong !
		 */
		// so write functions here to get information from the daqParams.
		//				System.out.printf("Sample regime: %s input with rate %3.1fHz, %d channels, gain %3.1fdB, ADCp-p %3.1fV\n", daqParams.getDaqSystemType(),
		//						daqParams.getSampleRate(), daqParams.getNChannels(), daqParams.preamplifier.getGain(), daqParams.voltsPeak2Peak);
		/**
		 * then there is the actual sampling. This is a bit harder to find. I thought it would be in the data map
		 * but the datamap is a simple count of what's in the databasase which is not quite what we want.
		 * we're going to have to query the database to get more detailed informatoin I think.
		 * I'll do that here for now, but we may want to move this when we better organise the code.
		 * It also seems that there are 'bad' dates in the database when it starts new files, which are the date
		 * data were analysed at. So we really need to check the start and stop records only.
		 */
		PamDataBlock<DaqStatusDataUnit> daqInfoDataBlock = daqControl.getAcquisitionProcess().getDaqStatusDataBlock();
		// just load everything. Probably OK for the acqusition, but will bring down
		daqInfoDataBlock.loadViewerData(0, Long.MAX_VALUE, null);
		ArrayList<DaqStatusDataUnit> allStatusData = daqInfoDataBlock.getDataCopy();
		/**
		 * Due to weird file overlaps we need to resort this by id if we can.
		 * 
		 */
		Collections.sort(allStatusData, new Comparator<DaqStatusDataUnit>() {

			@Override
			public int compare(DaqStatusDataUnit o1, DaqStatusDataUnit o2) {
				if (o1.getDatabaseIndex() == 0) {
					return (int) (o1.getTimeMilliseconds()-o2.getTimeMilliseconds());
				}
				return o1.getDatabaseIndex()-o2.getDatabaseIndex();
			}
		});

		RecordingList tempPeriods = null;

		if (allStatusData == null || allStatusData.size() == 0) {
			System.out.println("Data appear to have no logged recording periods. Try to extract from raw audio ...");
			tempPeriods = extractTimesFromFiles(daqControl);
		}
		else {
			tempPeriods = extractTimesFromStatus(allStatusData);
		}
		if (tempPeriods == null || tempPeriods.size() == 0) {
			System.out.println("Data appear to have no logged recording periods available either from the database or the raw recordings.");
			tempPeriods = extractTimesFromOutputMaps();
		}
		if (tempPeriods == null || tempPeriods.size() == 0) {
			System.out.println("Data appear to have no logged recording periods available either from the database or the raw recordings.");
			return null;
		}

		int nPeriods = tempPeriods.size();
		//				int i = 0;
		//				for (RecordingPeriod aP : tempPeriods) {
		//					System.out.printf("Pre merge %d : %s to %s\n", i++, PamCalendar.formatDBDateTime(aP.getRecordStart()), 
		//							PamCalendar.formatDBDateTime(aP.getRecordStop()));
		//				}

		tempPeriods = mergeRecordings(tempPeriods);

		return tempPeriods;
	}

	/**
	 * Merge close recordings and discard ones that are too short. 
	 * @param tempPeriods all recording periods, may be from consecutive files. 
	 * @return merged list. 
	 */
	private RecordingList mergeRecordings(RecordingList tempPeriods) {
		// now go through those and merge into longer periods where there is no gap between files.
		if (tempPeriods == null) {
			return null;
		}

		DeploymentExportOpts exportOptions = tethysControl.getDeploymentHandler().getDeploymentExportOptions();

		ListIterator<RecordingPeriod> iterator = tempPeriods.listIterator();
		RecordingPeriod prevPeriod = null;
		while (iterator.hasNext()) {
			RecordingPeriod nextPeriod = iterator.next();
			long nextDur = nextPeriod.getRecordStop()-nextPeriod.getRecordStart();
			if (nextDur == 0) {
				continue;
			}
			if (prevPeriod != null) {
				long gap = nextPeriod.getRecordStart() - prevPeriod.getRecordStop();
				long prevDur = prevPeriod.getRecordStop()-prevPeriod.getRecordStart();
				if (gap < exportOptions.maxRecordingGapSeconds*1000) {
					// ignoring up to 3s gap or a sample error < 2%.Dunno if this is sensible or not.
					prevPeriod.setRecordStop(nextPeriod.getRecordStop());
					iterator.remove();
					nextPeriod = prevPeriod;
				}
			}
			prevPeriod = nextPeriod;
		}
		// now remove ones which are too short even after merging. 
		iterator = tempPeriods.listIterator();
		while (iterator.hasNext()) {
			RecordingPeriod nextPeriod = iterator.next();
			long duration = nextPeriod.getDuration();
			if (duration < exportOptions.minRecordingLengthSeconds*1000L) {
				iterator.remove();
			}
		}

		return tempPeriods;
	}

	/**
	 * Work out whether or not the data are evenly duty cycled by testing the
	 * distributions of on and off times.
	 * @param tempPeriods
	 * @return
	 */
	private DutyCycleInfo assessDutyCycle(RecordingList tempPeriods) {
		if (tempPeriods == null) {
			return null;
		}
		int n = tempPeriods.size();
		if (n < 2) {
			return new DutyCycleInfo(false, 0,0,n);
		}
		double[] ons = new double[n-1]; // ignore the last one since it may be artificially shortened which is OK
		double[] gaps = new double[n-1];
		for (int i = 0; i < n-1; i++) {
			ons[i] = tempPeriods.get(i).getDuration()/1000.;
			gaps[i] = (tempPeriods.get(i+1).getRecordStart()-tempPeriods.get(i).getRecordStop())/1000.;
		}
		/* now look at how consistent those values are
		 * But some data gets messed by small gaps, so want to 
		 * remove outliers and concentrate on say 80% of the data. 
		 */
		ons = getDistributionCentre(ons, 80);
		gaps = getDistributionCentre(gaps, 80);
		Arrays.sort(gaps);


		STD std = new STD();
		double onsMean = std.getMean(ons);
		double onsSTD = std.getSTD(ons);
		double gapsMean = std.getMean(gaps);
		double gapsSTD = std.getSTD(gaps);
		boolean dutyCycle = onsSTD/onsMean < .05 && gapsSTD/gapsMean < 0.05;
		DutyCycleInfo cycleInfo = new DutyCycleInfo(dutyCycle, onsMean, gapsMean, tempPeriods.size());
		return cycleInfo;
	}

	/**
	 * Get the central part of a distribution without any outliers so 
	 * that we can get a better assessment of duty cycle. 
	 * @param data unsorted distribution data. 
	 * @param percent percentage to include (half this removed from top and bottom)
	 * @return
	 */
	private double[] getDistributionCentre(double[] data, double percent) {
		if (data == null) {
			return null;
		}
		Arrays.sort(data);
		int nRem = (int) Math.round(data.length * (100-percent)/200);
		int newLen = data.length-nRem*2;
		double[] subdata = Arrays.copyOfRange(data, nRem, data.length-2*nRem);
		if (subdata.length < 2) {
			return data;
		}
		return subdata;
	}


	/**
	 * Get data times from any other datamap, since this will generally match the acquisition anyway
	 * @return
	 */
	private RecordingList extractTimesFromOutputMaps() {
		OfflineDataMap bestMap = null;
		PamDataBlock bestBlock = null;
		long firstStart = Long.MAX_VALUE;
		long lastEnd = Long.MIN_VALUE;
		ArrayList<PamDataBlock> dataBlocks = PamController.getInstance().getDetectorDataBlocks();
		for (PamDataBlock aBlock : dataBlocks) {
			if (aBlock instanceof PamRawDataBlock) {
				continue; // don't want acquisition !
			}
			OfflineDataMap dataMap = aBlock.getPrimaryDataMap();
			if (dataMap == null) {
				continue;
			}
			if (dataMap.getFirstDataTime() < firstStart && dataMap.getLastDataTime() > lastEnd) {
				bestMap = dataMap;
				bestBlock = aBlock;
				firstStart = dataMap.getFirstDataTime();
				lastEnd = dataMap.getLastDataTime();
			}
		}
		if (bestMap == null) {
			return null;
		}
		// get the times out of it. 
		RecordingList recPeriods = new RecordingList();
		List<OfflineDataMapPoint> mapPoints = bestMap.getMapPoints();
		for (OfflineDataMapPoint mapPoint : mapPoints) {
			recPeriods.add(new RecordingPeriod(mapPoint.getStartTime(), mapPoint.getEndTime()));
		}
		return recPeriods;
	}

	private RecordingList extractTimesFromStatus(ArrayList<DaqStatusDataUnit> allStatusData) {
		RecordingList tempPeriods = new RecordingList();
		long dataStart = Long.MAX_VALUE;
		long dataEnd = Long.MIN_VALUE;
		Long lastStart = null;
		int nStart = 0;
		int nStop = 0;
		int nFile = 0;
		for (DaqStatusDataUnit daqStatus : allStatusData) {
			switch (daqStatus.getStatus()) {
			case "Start":
				nStart++;
				dataStart = Math.min(dataStart, daqStatus.getTimeMilliseconds());
				lastStart = daqStatus.getTimeMilliseconds();
				//				System.out.println("Start at " + PamCalendar.formatDBDateTime(lastStart));
				break;
			case "Stop":
				nStop++;
				dataEnd = Math.max(dataEnd, daqStatus.getEndTimeInMilliseconds());
				long lastEnd = daqStatus.getEndTimeInMilliseconds();
				if (lastStart != null) {
					//					System.out.printf("Adding period %s to %s\n", PamCalendar.formatDBDateTime(lastStart), 
					//							PamCalendar.formatDBDateTime(lastEnd));
					tempPeriods.add(new RecordingPeriod(lastStart, lastEnd));
				}
				else {
					//					System.out.println("Skipping stop at " + PamCalendar.formatDBDateTime(lastEnd));
				}
				lastStart = null;
				break;
			case "NextFile":
				nFile++;
				break;
			}
		}
		return tempPeriods;
	}

	private RecordingList extractTimesFromFiles(AcquisitionControl daqControl) {
		// TODO Auto-generated method stub
		return null;
	}
}
