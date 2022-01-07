package alfa;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import loggerForms.FormDescription;

public class ALFAParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	/**
	 * interval for sending effort data when no
	 * whales are present in SECONDS
	 */
	public int effortMsgIntervalNoWhales = 3600;

	/**
	 * interval for sending effort data when
	 * whales are present in SECONDS
	 */
//	public int effortMsgIntervalWhales = 900;
	public int histosPerReportInterval = 4;
	
	/**
	 * The histogram bin interval for a 0-180 degree bearing. 
	 */
	private int binsPerhistogram = 5;
	
	/**
	 * 
	 * True to use click train median angle for angle histogram. 
	 * False to use clicks in angle histogram. 
	 */
	public boolean useClkTrains = false; 
	
	/**
	 * Reload old reports at startup. This is important if 
	 * the system crashes to reload old data and continue histogram. 
	 */
	public boolean reloadOldReports = true;
	
	/**
	 * Automatically launch the screen mirror service
	 */
	public boolean autoScreenMirror = true;
	
	public int loggerFormFormat = FormDescription.LOGGER_FORMS_JSON;
	
	public boolean followOffline = false;

	@Override
	public ALFAParameters clone() {
		try {
			return (ALFAParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the binsPerhistogram
	 */
	public int getBinsPerhistogram() {
		if (binsPerhistogram <= 0) {
			binsPerhistogram = 4;
		}
		return binsPerhistogram;
	}

	/**
	 * @param binsPerhistogram the binsPerhistogram to set
	 */
	public void setBinsPerhistogram(int binsPerhistogram) {
		this.binsPerhistogram = binsPerhistogram;
	}


	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
