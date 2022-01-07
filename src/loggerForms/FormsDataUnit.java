package loggerForms;

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


}
