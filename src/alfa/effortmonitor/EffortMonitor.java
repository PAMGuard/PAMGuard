package alfa.effortmonitor;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.ListIterator;

import javax.swing.Timer;

import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamController.status.ModuleStatus;
import PamDetection.AbstractLocalisation;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import alfa.ALFAControl;
import alfa.ALFAParameters;
import alfa.clickmonitor.eventaggregator.ClickEventAggregate;
import alfa.server.ServerIntervalDataBlock;
import alfa.swinggui.IntervalOverlayDraw;
import clickDetector.ClickTrainDetector;
import detectiongrouplocaliser.DetectionGroupDataUnit;

/**
 * Monitors incoming data from the click train detector, summarises angles into a histogram, monitors 
 * total effort within an interval (usually around an hour), keeps summary of the GPS track lines and handles
 * making summary satellite messgaes.
 * 
 * @author Doug Gillespie
 *
 */
public class EffortMonitor extends PamProcess {
	
	private GPSDataBlock gpsDataBlock;
	
	private ClickTrainDetector clickTrainDetector;
	
	private Timer effortTimer;
		
	private IntervalDataBlock intervalDataBlock;
	
	private ServerIntervalDataBlock serverIntervalData;
	
	private IntervalDataLogging intervalLogging;

	private long effortStartTime;

	private long timerStartTime;
	
	private ALFAControl alfaControl;
	
	private AngleHistogram currentHistogram, prevHistogram;
	
	private IntervalDataUnit currentDataUnit;

	
	public EffortMonitor(ALFAControl alfaControl) {
		super(alfaControl, null, "Effort Monitor");
		this.alfaControl = alfaControl;
		effortTimer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timerAction();
			}
		});
		
		intervalDataBlock = new IntervalDataBlock(this, "ALFA Track Summary");
		intervalDataBlock.SetLogging(intervalLogging = new IntervalDataLogging(intervalDataBlock));
		intervalDataBlock.setOverlayDraw(new IntervalOverlayDraw(alfaControl));
		addOutputDataBlock(intervalDataBlock);
		
		if (alfaControl.isViewer()) {
			serverIntervalData = new ServerIntervalDataBlock(this, "ALFA Server Data");
			serverIntervalData.setOverlayDraw(new IntervalOverlayDraw(alfaControl));
//			serverIntervalData.SetLogging(new ALFAServerLoader(serverIntervalData));
			addOutputDataBlock(serverIntervalData);
		}
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			loadOldData();
		case PamController.ADD_CONTROLLEDUNIT:
		case PamController.REMOVE_CONTROLLEDUNIT:
			findGpsDataBlock();
			break;

		default:
			break;
		}
	}

	private synchronized void timerAction() {
		long now = PamCalendar.getTimeInMillis();
		long newEffort = now-timerStartTime;
		currentDataUnit.addActualEffort(newEffort);
		currentDataUnit.setDurationInMilliseconds(now-currentDataUnit.getTimeMilliseconds());
		GpsData gpsData = getLatestGPS();
		if (gpsData != null) {
			currentDataUnit.setLastGPSData(gpsData);
		}
		if (readyToSend(currentDataUnit)) {
			currentDataUnit.setReadyToSend(true);
			intervalDataBlock.updatePamData(currentDataUnit, now);
			currentDataUnit = createNewData(now);
		}
		else {
			checkAngleHistogram(now);
		}
		timerStartTime = now;
	}


	private void checkAngleHistogram(long now) {
		if (currentHistogram == null) {
			return;
		}
		ALFAParameters params = alfaControl.getAlfaParameters();
		long histoInterval = params.effortMsgIntervalNoWhales / Math.max(params.histosPerReportInterval,1);
		histoInterval = Math.max(120, histoInterval);
		long histoEndTime = currentHistogram.getStartTime() + histoInterval * 1000;
		if (now >= histoEndTime) {
			currentHistogram = makeAngleHistogram(now);
			currentDataUnit.addAngleHistogram(currentHistogram);
			intervalDataBlock.updatePamData(currentDataUnit, now);
		}
	}


	private boolean endDataUnit(IntervalDataUnit intervalDataUnit, long timeMilliseconds) {
		if (intervalDataUnit == null) {
			return true;
		}
		return intervalDataUnit.getTimeMilliseconds() + alfaControl.getAlfaParameters().effortMsgIntervalNoWhales*1000 < timeMilliseconds;
	}
	/**
	 * Called on initialisation complete to reload old data. 
	 */
	private void loadOldData() {
		if (alfaControl.getAlfaParameters().reloadOldReports) {
			intervalLogging.loadDataFrom(PamCalendar.getTimeInMillis() - 2*1000*alfaControl.getAlfaParameters().effortMsgIntervalNoWhales);
			currentDataUnit = intervalDataBlock.getLastUnit();
			if (currentDataUnit != null) {
				currentHistogram = currentDataUnit.getLastHistrogram();
			}
//			if (currentDataUnit != null && currentDataUnit.getTimeMilliseconds() < PamCa)
		}
	}


//	/**
//	 * 
//	 * @return the current latest data unit, or create one. 
//	 */
//	private IntervalDataUnit findDataUnit(long currentTime) {
//		IntervalDataUnit dataunit = intervalDataBlock.getLastUnit();
//		if (dataunit == null) {
//			dataunit = createNewData(currentTime);
//		}
//		if (currentHistogram == null) {
//			currentHistogram = makeAngleHistogram(currentTime);
//			dataunit.addAngleHistogram(currentHistogram);
//		}
//		
//		return dataunit;
//	}

	/**
	 * Create a new data unit, add current GPS data and 
	 * add it to the data block. Also add it's first angle histogram. 
	 * @param timeMilliseconds
	 * @return data unit
	 */
	private IntervalDataUnit createNewData(long timeMilliseconds) {
		IntervalDataUnit dataUnit = new IntervalDataUnit(timeMilliseconds, getLatestGPS());
		currentHistogram = makeAngleHistogram(timeMilliseconds);
		dataUnit.addAngleHistogram(currentHistogram);
		intervalDataBlock.addPamData(dataUnit);
		
		return dataUnit;
	}
	
	private AngleHistogram makeAngleHistogram(long timeMilliseconds) {
		prevHistogram = currentHistogram;
		return new AngleHistogram(timeMilliseconds, 0, Math.PI, alfaControl.getAlfaParameters().getBinsPerhistogram());
	}


	@Override
	public void pamStart() {
		if (alfaControl.getAlfaParameters().reloadOldReports == false) {
			intervalDataBlock.clearAll();
		}
		effortTimer.start();
		timerStartTime = effortStartTime = PamCalendar.getTimeInMillis();
		if (currentDataUnit != null && endDataUnit(currentDataUnit, effortStartTime)) {
			currentDataUnit = null;
		}
		if (currentDataUnit == null) {
			currentDataUnit = createNewData(effortStartTime);
		}
		if (currentHistogram == null) {
			currentHistogram = makeAngleHistogram(effortStartTime);
			currentDataUnit.addAngleHistogram(currentHistogram);
		}
	}

	@Override
	public void pamStop() {
		effortTimer.stop();
//		timerAction();
	}
	
	/**
	 * See if it's time to send the current data unit. 
	 * @param dataUnit data unit 
	 * @return true if it's time to send it. 
	 */
	private boolean readyToSend(IntervalDataUnit dataUnit) {
		if (dataUnit == null) {
			return false;
		}
		double durationSecs = dataUnit.getDurationInMilliseconds() / 1000.;
		ALFAParameters params = alfaControl.getAlfaParameters();
		return durationSecs >= params.effortMsgIntervalNoWhales;
	}
	
	/**
	 * 
	 * @return the latest GPS data
	 */
	private GpsData getLatestGPS() {
		if (gpsDataBlock == null) {
			return null;
		}
		GpsDataUnit lastGPSUnit = gpsDataBlock.getLastUnit();
		if (lastGPSUnit == null) {
			return null;
		}
		return lastGPSUnit.getGpsData();
	}

	private boolean findGpsDataBlock() {
		GPSControl gpsControl = GPSControl.getGpsControl();
		if (gpsControl == null) {
			gpsDataBlock = null;
		}
		else {
			gpsDataBlock = gpsControl.getGpsDataBlock();
		}
		setParentDataBlock(gpsDataBlock);
		return gpsDataBlock != null;
	}


	/**
	 * @return the intervalDataBlock
	 */
	public IntervalDataBlock getIntervalDataBlock() {
		return intervalDataBlock;
	}


	/**
	 * Update the click historgram information. 
	 * @param aggregateEvent - the event. 
	 * @param detectionGroupDataUnit - the detection group data unit.
	 */
	public void updateClickInformation(ClickEventAggregate aggregateEvent,
			DetectionGroupDataUnit detectionGroupDataUnit) {
		currentDataUnit.addClickTrain(detectionGroupDataUnit);
		double medAngle = getMedianAngle(detectionGroupDataUnit);
		
		if (this.alfaControl.getAlfaParameters().useClkTrains) {
			currentHistogram.addData(medAngle);
		}
		else {
			for (int i=0; i<detectionGroupDataUnit.getSubDetectionsCount(); i++) {
				if (detectionGroupDataUnit.getSubDetection(i).getLocalisation()!=null) {
				currentHistogram.addData(Math.abs(detectionGroupDataUnit.getSubDetection(i).getLocalisation().getAngles()[0]));
				}
			}
		}
	}

	/**
	 * Get the median angle from the detectionGroupDataUnit. 
	 * @param detectionGroupDataUnit
	 * @return median angle in radians. 
	 */
	public double getMedianAngle(DetectionGroupDataUnit detectionGroupDataUnit) {
		int nSub = detectionGroupDataUnit.getSubDetectionsCount();
		if (nSub<=0) return Double.NaN; 
		int nLoc = 0;
		double[] angleList = new double[nSub];
		for (int i =0; i < nSub; i++) {
			PamDataUnit subDet = detectionGroupDataUnit.getSubDetection(i);
			if (subDet == null) {
				continue;
			}
			AbstractLocalisation subLoc = subDet.getLocalisation(); 
			if (subLoc == null) {
				continue;
			}
			double[] angles = subLoc.getAngles();
			if (angles == null) {
				continue;
			}
			angleList[nLoc++] = Math.abs(angles[0]);
		}
		if (nLoc < nSub) {
			angleList = Arrays.copyOf(angleList, nLoc);
		}
		Arrays.sort(angleList);
		int medInd = nLoc/2;
		
		if (angleList.length==0) return Double.NaN; 

		return angleList[medInd];
	}	
	
	public ModuleStatus getWhaleStatus() {
		long timeSpan = 15*60*1000;
		int nWhales;
		synchronized (intervalDataBlock.getSynchLock()) {
			ListIterator<IntervalDataUnit> intIt = intervalDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
		}
		if (currentHistogram == null) {
			return new ModuleStatus(ModuleStatus.STATUS_WARNING, "No data");
		}
		else {
			double n = currentHistogram.getTotalContent();
			if (n == 0) {
				return new ModuleStatus(ModuleStatus.STATUS_OK, "No Whales");
			}
			else if (n == 1) {
				return new ModuleStatus(ModuleStatus.STATUS_WARNING, "One whale detection");
			}
			else {
				return new ModuleStatus(ModuleStatus.STATUS_ERROR, String.format("%d whales detected", (int) n));
			}
		}
	}


	@Override
	public void setupProcess() {
		super.setupProcess();
		intervalLogging.checkHistogramColumns(alfaControl.getAlfaParameters().histosPerReportInterval);
		if (serverIntervalData != null) {
			serverIntervalData.getAlfaServerLoader().runAutoUpdates(alfaControl.getAlfaParameters().followOffline);
		}
	}


	/**
	 * Get a slightly averaged angle histogram for status display. 
	 * @return averaged angle histogram. 
	 */
	public AngleHistogram getAveragedAngleHistogram() {
		if (currentHistogram == null) {
			return null;
		}
		AngleHistogram angleHist = currentHistogram.clone();
		if (prevHistogram != null) {
			double[] data = angleHist.getData();
			double[] data2 = prevHistogram.getData();
			int n = Math.min(data.length, data2.length);
				for (int i = 0; i < n; i++) {
					data[i] = (data[i] + data2[i]);
				}
		}
		return angleHist;
	}
}
