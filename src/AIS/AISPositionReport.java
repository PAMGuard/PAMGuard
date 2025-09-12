package AIS;

import java.io.Serializable;

import NMEA.NMEABitArray;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.LatLong;

/**
 * AIS Position report data
 * @author Doug Gillespie
 *
 */
public class AISPositionReport extends AISReport implements Serializable, ManagedParameters {

	public static final long serialVersionUID = 0;
	
	public int messageId;
	public int dataTerminalReady;
	public int dataIndicator;
	public int navigationStatus;
	public double rateOfTurn;
	public double speedOverGround;
	public int positionAccuracy;
	public LatLong latLong;
	public double courseOverGround;
	public double trueHeading;
	public int utcSeconds;
	public int utcMinutes;
	public int utcHours;
	public int repeatIndicator;
	public int commsState;
	public long timeMilliseconds;
	public boolean reportOk = false;
	
	
	AISPositionReport (int messageId, long timeMillis, NMEABitArray bitData) {
		this.messageId = messageId;
		this.timeMilliseconds = timeMillis;
		switch(messageId) {
		case 1:
		case 2:
		case 3:
			reportOk = unpackStandardPositionReport(bitData);
			break;
		case 4:
			reportOk = unpackBaseStationReport(bitData);
			break;
		case 18:
			reportOk = unpackClassBPositionReport(bitData);
			break;
		case 19:
			reportOk = unpackExtendedClassBPositionReport(bitData);
			break;
		case 21:
			reportOk = unpackAidToNavigationReport(bitData);
			break;
		}
	}
	
	/**
	 * Get the know speed of the vessel. Generally, this is just the
	 * speedoverground, but in the data unavailable case, this is set
	 * to 102.3, in which case return 0, so avoid messing up interpolation 
	 * on the map
	 * @return
	 */
	public double getKnownSpeed() {
		if (speedOverGround == 102.3) {
			return 0;
		}
		else {
			return speedOverGround;
		}
	}
	/**
	 * Get the speed as a string, return "Unknown" if it's set at 102.3
	 * @return
	 */
	public String getSpeedString() {
		if (speedOverGround == 102.3) {
			return "Unavailable";
		}
		else if (speedOverGround == 102.2) {
			return ">102.2";
		}
		else {
			return String.format("%3.1f", speedOverGround);
		}
	}
	
	/**
	 * Return COG string with degree symbol, or N/A if unavailable
	 * @return
	 */
	public String getCOGString() {
		if (courseOverGround >= 360) {
			return "N/A";
		}
		else {
			return String.format("%.1f\u00B0", courseOverGround);
		}
	}
	
	private boolean unpackAidToNavigationReport(NMEABitArray bitData) {
		double longitude = bitData.getSignedInteger(164, 191) / 600000.;
		double latitude = bitData.getSignedInteger(192, 218) / 600000.;
		latLong = new LatLong(latitude, longitude);
		return true;
	}
	private boolean unpackBaseStationReport(NMEABitArray bitData) {
		double longitude = bitData.getSignedInteger(79, 106) / 600000.;
		double latitude = bitData.getSignedInteger(107, 133) / 600000.;
		latLong = new LatLong(latitude, longitude);
		return true;
	}
	private boolean unpackClassBPositionReport(NMEABitArray bitData) {
		speedOverGround = bitData.getUnsignedInteger(46, 55)/10.;
		positionAccuracy = bitData.getUnsignedInteger(56, 56);
		double longitude = bitData.getSignedInteger(57, 84) / 600000.;
		double latitude = bitData.getSignedInteger(85, 111) / 600000.;
		latLong = new LatLong(latitude, longitude);
		courseOverGround = bitData.getUnsignedInteger(112,123)/10.;
		trueHeading = bitData.getUnsignedInteger(124,132);
		utcSeconds = bitData.getUnsignedInteger(133, 138);
		
		return true;
	}
	private boolean unpackExtendedClassBPositionReport(NMEABitArray bitData) {
		if (!unpackClassBPositionReport(bitData)) {
			return false;
		}
		return true;
	}
	private boolean unpackStandardPositionReport(NMEABitArray bitData) {

		dataTerminalReady = bitData.getSignedInteger(6, 6);
		navigationStatus = bitData.getUnsignedInteger(38, 41);
		/*
		 * 
±127. (-128 (80 hex) indicates not available, which shall be the default). Coded by
ROTAIS=4.733 SQRT(ROTIND) degrees/min
ROTIND is the Rate of Turn ( 720 degrees per minute), as indicated by an external
sensor.
+ 127 = turning right at 720 degrees per minute or higher;
- 127 = turning left at 720 degrees per minute or higher
		 */
		rateOfTurn = bitData.getSignedInteger(42, 49);
		speedOverGround = bitData.getUnsignedInteger(50, 59)/10.;
		positionAccuracy = bitData.getUnsignedInteger(60, 60);
		double latitude = bitData.getSignedInteger(89,115) / 600000.;
		double longitude = bitData.getSignedInteger(61, 88) / 600000.;
		latLong = new LatLong(latitude, longitude);
		courseOverGround = bitData.getUnsignedInteger(116, 127)/10.;
		trueHeading = bitData.getUnsignedInteger(128, 136);
		utcSeconds = bitData.getUnsignedInteger(137, 142);
		repeatIndicator = bitData.getUnsignedInteger(143, 144);
		commsState = bitData.getUnsignedInteger(150, 167);
		//		timeMilliseconds = PamCalendar.getTimeInMillis();

		//		System.out.println(this);
		return true;
	}
	

	/**
	 * Constructor to use when reading back from database. 
	 * @param navStatus navStatus
	 * @param rateOfTurn rateOfTurn
	 * @param speedOverGround speedOverGround
	 * @param latitude latitude
	 * @param longitude longitude
	 * @param courseOverGround courseOverGround
	 * @param trueHeading trueHeading
	 */
	public AISPositionReport(long timeMillis, int navStatus, double rateOfTurn, double speedOverGround,
			double latitude, double longitude, double courseOverGround, double trueHeading) {
		this.timeMilliseconds = timeMillis;
		this.navigationStatus = navStatus;
		this.rateOfTurn = rateOfTurn;
		this.speedOverGround = speedOverGround;
		this.latLong = new LatLong(latitude, longitude);
		this.courseOverGround = courseOverGround;
		this.trueHeading = trueHeading;
	}

	@Override
	public String toString() {
		return String.format("Position %.6f,%.6f; COG %f; SOG %f; ROT %.3f", 
				latLong.getLatitude(), latLong.getLongitude(), courseOverGround, speedOverGround, rateOfTurn);
	}
	
	public double getLatitude() {
		return latLong.getLatitude();
	}
	
	public double getLongitude() {
		return latLong.getLongitude();
	}
	
	public boolean hasTrueHeading() {
		return trueHeading < 500;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
