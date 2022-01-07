package difar.calibration;

import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.StaticOriginMethod;
import Array.streamerOrigin.StaticOriginSettings;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamguardMVC.PamDataUnit;

public class CalibrationDataUnit extends PamDataUnit {

	private long streamerUID;
	private double compassCorrection;
	private double correctionStdDev;
	private int numClips;
 
	
	public CalibrationDataUnit(long timeMilliseconds, long streamerUID, 
			double compassCorrection, double correctionStdDev, int numClips ) {
		super(timeMilliseconds);
		this.streamerUID = streamerUID;
		this.compassCorrection = compassCorrection;
		this.correctionStdDev = correctionStdDev;
		this.numClips = numClips;
	}


	public long getStreamerUID() {
		return streamerUID;
	}


	public void setStreamerUID(long streamerUID) {
		this.streamerUID = streamerUID;
	}


	public double getCompassCorrection() {
		return compassCorrection;
	}


	public void setCompassCorrection(double compassCorrection) {
		this.compassCorrection = compassCorrection;
	}


	public double getCorrectionStdDev() {
		return correctionStdDev;
	}


	public void setCorrectionStdDev(double correctionStdDev) {
		this.correctionStdDev = correctionStdDev;
	}


	public int getNumClips() {
		return numClips;
	}


	public void setNumClips(int numClips) {
		this.numClips = numClips;
	}


}
