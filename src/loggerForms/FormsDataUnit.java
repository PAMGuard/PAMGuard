package loggerForms;

import GPS.GpsData;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
/**
 * 
 * @author Graham Weatherup
 * 
 * ID
 * UTC
 * UTCmillisecond
 * PCLocalTime
 * 
 * 
 * 
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
			formOriginLatLong = loggerForm.getOriginLatLong(this);
		}
		return formOriginLatLong;
	}



}
