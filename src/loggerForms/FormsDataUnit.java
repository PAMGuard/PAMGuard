package loggerForms;

import GPS.GpsData;
import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamguardMVC.PamDataUnit;
import generalDatabase.SQLTypes;
import loggerForms.controlDescriptions.ControlDescription;
/**
 * 
 * @author Graham Weatherup
 * 
 * ID
 * UTC
 * UTCmillisecond
 * PCLocalTime
 * 
 */
public class FormsDataUnit extends PamDataUnit {

	private LoggerForm loggerForm;
	
	public static final int currentXMLFormat = 1;
	
	/**
	 * This Array is based around the inputControlDescriptions ArrayList in formDescriptions. 
	 * is is of same length and order matches 
	 */
	private Object[] formData;
	private FormDescription formDescription;

	private GpsData formOriginLatLong;
	
	/**
	 * Constructor for a form data unit. 
	 * @param loggerForm
	 * @param timeMilliseconds
	 * @param formDescription
	 * @param formData
	 */
	public FormsDataUnit(LoggerForm loggerForm,long timeMilliseconds,FormDescription formDescription, Object[] formData) {
		super(timeMilliseconds);
		this.loggerForm=loggerForm;
		this.formDescription = formDescription;
		this.formData = formData;
	}

	/**
	 * This formData Array is based around the inputControlDescriptions ArrayList in formDescriptions. is is of same length and order matches 
	 * @return the formData
	 */
	public Object[] getFormData() {
		return formData;
	}
	
	public  LoggerForm getLoggerForm() {
		return loggerForm;
	}
	
	
	
	/**
	 * @return the formDescription
	 */
	public FormDescription getFormDescription() {
		return formDescription;
	}

	/**
	 * used for updating data units(editing forms)
	 */
	public void setFormsData(LoggerForm loggerForm,Object[] formData) {
		this.loggerForm=loggerForm;
		this.formData=formData;
		this.updateDataUnit(PamCalendar.getTimeInMillis());
	}

	@Override
	public GpsData getOriginLatLong(boolean recalculate) {
		/**
		 * Need to do something a bit different here since Logger form data is generally 
		 * not associated with a hydrophone (though that may change in the future). 
		 * All we really want is the primary origin method, which is either GPS data 
		 * or static data and then get the value. mostly people will want the GPS position 
		 * for the time of the logger data, though really we should make a much better way 
		 * of doing this, including offsets from GPS, options to use the hydrophones if 
		 * we want to as a reference, etc. 
		 */
		if (recalculate || formOriginLatLong == null) {
			if (formDescription != null) {
				formOriginLatLong = formDescription.getOriginLatLong(this);
			}
		}
		return formOriginLatLong;
	}

	@Override
	public long getTimeMilliseconds() {
		Long time = findTimeValue(PropertyTypes.STARTTIME);
		if (time != null) {
			return time;
		}
		return super.getTimeMilliseconds();
	}

	/**
	 * Find one of the time property controls and get its value. 
	 * @param timeProperty
	 * @return
	 */
	public Long findTimeValue(PropertyTypes timeProperty) {
		if (formData == null) {
			return null;
		}
		PropertyDescription prop = formDescription.findProperty(timeProperty);
		if (prop == null) {
			return null;
		}
		String ctrlTitle = prop.getItemInformation().getStringProperty("Title");
		if (ctrlTitle == null) {
			return null;
		}
		int timeControlIndex = formDescription.findInputControlByName(ctrlTitle);
		if (timeControlIndex < 0 || timeControlIndex >= formData.length) {
			return null;
		}
		Object timeObj = formData[timeControlIndex];
		/*
		 *  this should have found the time contol in the form of a string from the database.
		 *  try to unpack it.  
		 */
		Long timeMillis = SQLTypes.millisFromTimeStamp(timeObj);
		
		return timeMillis;
	}
	
	/**
	 * find a correctly set property value for the end time (if set). 
	 * @return
	 */
	public Long getSetEndTime() {
		return findTimeValue(PropertyTypes.ENDTIME);
	}

	@Override
	public long getEndTimeInMilliseconds() {
		Long time = findTimeValue(PropertyTypes.ENDTIME);
		if (time != null) {
			return time;
		}
		return super.getEndTimeInMilliseconds();
	}
	
	@Override
	public String getSummaryString() {
		String str = String.format("<html><b>%s</b>", formDescription.getFormNiceName());
		Object[] data = getFormData();
		int iDat = 0;
		for (ControlDescription cd:formDescription.getInputControlDescriptions()) {
			if (data[iDat] == null) {
				str += String.format("<p>%s: -", cd.getTitle());
			}
			else {
				str += String.format("<p>%s: %s", cd.getTitle(), data[iDat].toString());
			}
			iDat++;
		}

		str += "</html>";
		return str;
	}



}
