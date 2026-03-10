package Acquisition;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;

/**
 * Data unit containing information on run starts and stops.
 * <p>
 * The main purpose of producing these is so that they get picked up
 * by the database and logged
 *
 * @author Doug Gillespie
 *
 */
public class DaqStatusDataUnit extends PamDataUnit {

	private String status = "Stop";

	private String reason = "";

	private String daqSystemType = "";

	public int sampleRate;

	public Double trueSampleRate;

	public int nChannels = 2;

	public double voltsPeak2Peak = 5;

	public double duration = 0;

	public double clockError;

	private long adcMilliseconds;

	private long samples;

	private Long gpsPPSMilliseconds;

	private Long serverTime;

	private AcquisitionParameters daqParameters;

	private long rawADCMillis;

	private String systemName;
	
	/**
	 * Source for start time of data. 
	 */
	private String startTimeSource;
//	private static DaqStatusDataUnit previousDaqStatusUnit = null;

	public DaqStatusDataUnit(long timeMilliseconds, long adcMilliseconds, long rawADCMillis,
			long samples, Long gpsPPSMillis,
			String status, String reason,
			AcquisitionParameters daqParameters, String systemName, double duration, double clockError) {
		super(timeMilliseconds);
		this.adcMilliseconds = adcMilliseconds;
		this.rawADCMillis = rawADCMillis;
		this.samples = samples;
		this.gpsPPSMilliseconds = gpsPPSMillis;
		this.status = status;
		this.reason = reason;
		if (daqParameters!=null) {
			//null if statement only used in viewer mode were DAQ params are not loaded from database.
			this.daqSystemType = daqParameters.daqSystemType;
			this.daqParameters = daqParameters;
			this.sampleRate = (int) daqParameters.getSampleRate();
			nChannels = daqParameters.nChannels;
			this.voltsPeak2Peak = daqParameters.voltsPeak2Peak;
		}
		this.systemName = systemName;
		this.duration = duration;
		this.clockError = clockError;

		// set the DataUnitBaseData duration (in samples) to this duration
		this.setSampleDuration((long) (duration*sampleRate));

//		/*
//		 * This won't work if data are being sent from multiple receivers !
//		 */
//		calculateTrueSampleRate(previousDaqStatusUnit);
//		previousDaqStatusUnit = this;
	}

	/**
	 * @return the systemName
	 */
	public String getSystemName() {
		return systemName;
	}

	/**
	 * @param systemName the systemName to set
	 */
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public Double calculateTrueSampleRate(DaqStatusDataUnit previousUnit) {
		if (previousUnit == null || gpsPPSMilliseconds == null || previousUnit.getGpsPPSMilliseconds() == null) {
			trueSampleRate = null;
			return null;
		}
		double samples = this.samples - previousUnit.getSamples();
		double tDiff = (this.gpsPPSMilliseconds - previousUnit.getGpsPPSMilliseconds()) / 1000.;
		if (tDiff > 0 && tDiff < 900 && samples > 0) {
			trueSampleRate = samples/tDiff;
		}
		return trueSampleRate;
	}

	@Override
	public String getSummaryString() {
		// TODO Auto-generated method stub
		String str = super.getSummaryString();
		str += String.format(" Sample %d", samples);
		if (gpsPPSMilliseconds != null) {
			str += String.format(", GPS PPS Time %s", PamCalendar.formatTime(gpsPPSMilliseconds, true));
		}
		str += String.format(", Clock Error %3.2fs", clockError);
		if (trueSampleRate != null) {
			str += String.format(", True Sample Rate = %6.1f Hz", trueSampleRate);
		}
		return str;
	}

	/**
	 * @return the samples
	 */
	public long getSamples() {
		return samples;
	}

	/**
	 * @return the gpsPPSMilliseconds
	 */
	public Long getGpsPPSMilliseconds() {
		return gpsPPSMilliseconds;
	}

	public String getReason() {
		return reason;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status, String reason) {
		this.status = status;
		this.reason = reason;
	}

	public String getDaqSystemType() {
		return daqSystemType;
	}

	public void setDaqSystemType(String daqSystemType) {
		this.daqSystemType = daqSystemType;
	}

	public int getNChannels() {
		return nChannels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public double getVoltsPeak2Peak() {
		return voltsPeak2Peak;
	}

	public double getDuration() {
		return duration;
	}

	/**
	 * @return the adcMilliseconds
	 */
	public long getAdcMilliseconds() {
		return adcMilliseconds;
	}

	/**
	 * Get the true sample rate which is calculated from successive arrivals of DAQ data units
	 * @return the trueSampleRate
	 */
	public Double getTrueSampleRate() {
		return trueSampleRate;
	}

	public void setServerTime(Long serverTime) {
		this.serverTime = serverTime;
	}

	public Long getServerTime() {
		return serverTime;
	}

	/**
	 * @return the daqParameters
	 */
	public AcquisitionParameters getDaqParameters() {
		return daqParameters;
	}

	/**
	 * @return the rawADCMillis
	 */
	public long getRawADCMillis() {
		return rawADCMillis;
	}

	/**
	 * @return the startTimeSource
	 */
	public String getStartTimeSource() {
		return startTimeSource;
	}

	/**
	 * @param startTimeSource the startTimeSource to set
	 */
	public void setStartTimeSource(String startTimeSource) {
		this.startTimeSource = startTimeSource;
	}

}
