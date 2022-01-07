package alfa.effortmonitor;

import java.util.ArrayList;

import GPS.GpsData;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.time.CalendarControl;
import alfa.comms.ALFACommsDataUnit;
import alfa.server.ServerRawData;
import detectiongrouplocaliser.DetectionGroupDataUnit;

/**
 * Data unit to summarise what's been going on in some defined time interval (e.g. an hour)
 * @author Doug Gillespie
 *
 */
public class IntervalDataUnit extends ALFACommsDataUnit {

	/**
	 * Actual effort in milliseconds. 
	 */
	private long actualEffort;

	private GpsData firstGPSData, lastGPSData;

	private int nClickTrains, nClicks;
	
	private ArrayList<AngleHistogram> angleHistograms = new ArrayList<>();
	
	private Long imeiNumber;

	private ServerRawData serverRawData;

	public IntervalDataUnit(long timeMilliseconds, GpsData gpsData) {
		super(timeMilliseconds);
		getBasicData().setEndTime(timeMilliseconds);
		firstGPSData = lastGPSData = gpsData;
	}

	/**
	 * Add a period of effort to the data unit. 
	 * @param effort active time in milliseconds. 
	 * @return new total effort. 
	 */
	public long addActualEffort(long effort) {
		actualEffort += effort;
		return actualEffort;
	}

	/**
	 * @return the actualEffort
	 */
	public long getActualEffort() {
		return actualEffort;
	}

	/**
	 * @param actualEffort the actualEffort to set
	 */
	public void setActualEffort(long actualEffort) {
		this.actualEffort = actualEffort;
	}

	/**
	 * 
	 * @return percentage of up time of PAMguard. 
	 */
	public double getPercentEffort() {
		double duration =  getDurationInMilliseconds();
		if (duration == 0) {
			return 0;
		}
		return (double) actualEffort / duration * 100.;
	}

	/**
	 * @return the firstGPSData
	 */
	public GpsData getFirstGPSData() {
		return firstGPSData;
	}

	/**
	 * @param firstGPSData the firstGPSData to set
	 */
	public void setFirstGPSData(GpsData firstGPSData) {
		this.firstGPSData = firstGPSData;
	}

	/**
	 * @return the lastGPSData
	 */
	public GpsData getLastGPSData() {
		return lastGPSData;
	}

	/**
	 * @param lastGPSData the lastGPSData to set
	 */
	public void setLastGPSData(GpsData lastGPSData) {
		this.lastGPSData = lastGPSData;
		if (firstGPSData == null) {
			firstGPSData = lastGPSData;
		}
	}

	/**
	 * 
	 * @return straight line track length between start and end in metres
	 */
	public Double getTrackLengthMetres() {
		if (firstGPSData == null || lastGPSData == null) {
			return null;
		}
		return firstGPSData.distanceToMetres(lastGPSData);
	}

	/**
	 * 
	 * @return straight line track length between start and end in miles
	 */
	public Double getTrackLengthMiles() {
		Double m = getTrackLengthMetres();
		if (m == null) {
			return null;
		}
		return m / LatLong.MetersPerMile;
	}


	@Override
	public String getCommsString() {
		double duration = Math.max(0, getDurationInMilliseconds() / 1000. / 60);
		LatLong startLat = firstGPSData;
		if (startLat == null) {
			startLat = new LatLong();
		}
		LatLong endLat = lastGPSData;
		if (endLat == null) {
			endLat = new LatLong();
		}
		String str = String.format("$PGSTA,1,%s,%s,%4.4f,%4.4f,%d,%d,%4.4f,%4.4f,%d,%d",
				PamCalendar.formatDate2(getTimeMilliseconds(),false), PamCalendar.formatTime2(getTimeMilliseconds(), 0, false),
				startLat.getLatitude(), startLat.getLongitude(),
				(int) duration, (int) getPercentEffort(), 
				endLat.getLatitude(), endLat.getLongitude(),
				nClickTrains, nClicks);
		synchronized (angleHistograms) {
			int nHist = angleHistograms.size();
			for (int i = 0; i < nHist; i++) {
				AngleHistogram angleHistogram = angleHistograms.get(i);
				int nBins = angleHistogram.getNBins();
				String hStr = String.format(",H,%d",nBins);
				double[] data = angleHistogram.getData();
				for (int b = 0; b < nBins; b++) {
					hStr += String.format(",%d", (int) data[b]); 
				}
				str += hStr;
			}
		}
		return str;
	}

	@Override
	public String getSummaryString() {	
		String str = "<html>";
		str += "UID: " + getUID() + "<p>";
		if (getParentDataBlock() != null) {
			str += getParentDataBlock().getDataName() + "<p>";
		}
		if (imeiNumber != null) {
			str += String.format("imei: %d<p>", imeiNumber);
		}
		Integer momsn = getMOMSN();
		if (momsn != null) {
			str += String.format("momsn: %d<p>", momsn);
		}
		str += String.format("%s %s %s<p>", PamCalendar.formatDate(getTimeMilliseconds(), true),
				PamCalendar.formatTime(getTimeMilliseconds(), 3, true),
				CalendarControl.getInstance().getTZCode(true));
		if (CalendarControl.getInstance().isUTC() == false) {
			str += String.format("(%s %s %s)<p>", PamCalendar.formatDate(getTimeMilliseconds(), false),
					PamCalendar.formatTime(getTimeMilliseconds(), 3, false),
					"UTC");
		}
		double duration = Math.max(0, getDurationInMilliseconds() / 1000.);
		LatLong startLat = firstGPSData;
		if (startLat == null) {
			startLat = new LatLong();
		}
		LatLong endLat = lastGPSData;
		if (endLat == null) {
			endLat = new LatLong();
		}
		str += String.format("Start %s<p>End %s<p>Click Trains: %d<p>Clicks %d</html>",
				startLat.toString(), endLat.toString(), nClickTrains, nClicks);
		return str;
	}

	@Override
	public Double getDurationInMilliseconds() {
		return Math.max(0,super.getDurationInMilliseconds());
	}

	public void addClickTrain(DetectionGroupDataUnit detectionGroupDataUnit) {
		nClickTrains++;
		nClicks += detectionGroupDataUnit.getSubDetectionsCount();
	}
	

	/**
	 * @return the nClickTrains
	 */
	public int getnClickTrains() {
		return nClickTrains;
	}

	/**
	 * @param nClickTrains the nClickTrains to set
	 */
	public void setnClickTrains(int nClickTrains) {
		this.nClickTrains = nClickTrains;
	}

	/**
	 * @return the nClicks
	 */
	public int getnClicks() {
		return nClicks;
	}

	/**
	 * @param nClicks the nClicks to set
	 */
	public void setnClicks(int nClicks) {
		this.nClicks = nClicks;
	}

	public void addAngleHistogram(AngleHistogram angleHistogram) {
		synchronized (angleHistograms) {
			angleHistograms.add(angleHistogram);
		}
	}
	
	public AngleHistogram getAngleHistogram(int iHist) {
		synchronized (angleHistograms) {
			if (angleHistograms == null || angleHistograms.size() <= iHist) {
				return null;
			}
			return angleHistograms.get(iHist);
		}
	}

	/**
	 * @return the angleHistograms
	 */
	public ArrayList<AngleHistogram> getAngleHistograms() {
		return angleHistograms;
	}

	public AngleHistogram getLastHistrogram() {
		if (angleHistograms == null) {
			return null;
		}
		else {
			return angleHistograms.get(angleHistograms.size()-1);
		}
	}

	/**
	 * @return the imeiNumber
	 */
	public Long getImeiNumber() {
		return imeiNumber;
	}

	/**
	 * @param imeiNumber the imeiNumber to set
	 */
	public void setImeiNumber(Long imeiNumber) {
		this.imeiNumber = imeiNumber;
	}

	public void setServerRawData(ServerRawData serverRaw) {
		this.serverRawData = serverRaw;
	}
	
	public Integer getMOMSN() {
		if (serverRawData == null) {
			return null;
		}
		else {
			return serverRawData.getMOMSN();
		}
	}

}
