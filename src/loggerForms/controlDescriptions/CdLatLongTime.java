/**
 * 
 */
package loggerForms.controlDescriptions;

import java.sql.Types;




import org.w3c.dom.Document;
import org.w3c.dom.Element;

import loggerForms.FormDescription;
import loggerForms.FormsTableItem;
import loggerForms.ItemInformation;
import loggerForms.LatLongTime;
import loggerForms.LoggerForm;
import loggerForms.controls.LatLongControl;
import loggerForms.controls.LatLongTimeControl;
import loggerForms.controls.LoggerControl;
import PamUtils.LatLong;
import PamUtils.PamCalendar;

/**
 * @author GrahamWeatherup
 *
 */
public class CdLatLongTime extends InputControlDescription{
	/**
	 * @param formDescription
	 */
	public CdLatLongTime(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		
	}
	
	@Override
	public FormsTableItem[] getFormsTableItems() {
		if(formsTableItems==null){
			FormsTableItem[] tis={
			new FormsTableItem(this, getDbTitle()+"_LAT", Types.DOUBLE),
			new FormsTableItem(this, getDbTitle()+"_LON", Types.DOUBLE),
			new FormsTableItem(this, getDbTitle()+"_TIME", Types.TIMESTAMP)};
			formsTableItems=tis;
		}
		return formsTableItems;
		
	};
	@Override
	public Object fromString(String data) {
		if (data == null) {
			return null;
		}
		try {
			LatLong ll = LatLong.valueOfString(data);
			if (ll == null) {
				return null;
			}
			String[] bits = data.split(",");
			long ts = 0;
			if (bits.length >= 3) {
				ts = PamCalendar.millisFromDateTimeString(bits[2], true);
			}
			return new LatLongTime(ll.getLatitude(), ll.getLongitude(), ts);
		}
		catch (Exception e) {
			System.out.println("Unable to unpack cbLatLong item " + data);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new LatLongTimeControl(this, loggerForm);
	}
	
	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.ControlDescription#moveDataToTableItems(java.lang.Object)
	 */
	@Override
	public void moveDataToTableItems(Object data) {//LatLongTime
		
		if (data == null) {
			formsTableItems[0].setValue(null);
			formsTableItems[1].setValue(null);
			formsTableItems[2].setValue(null);
		}
		else {
			LatLongTime latLongTime = (LatLongTime) data;
			formsTableItems[0].setValue(new Double(latLongTime.getLatitude()));
			formsTableItems[1].setValue(new Double(latLongTime.getLongitude()));
			formsTableItems[2].setValue(new Long(latLongTime.getTimeInMillis()));
		}
	}
	
	

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.ControlDescription#moveDataFromTableItems()
	 */
	@Override
	public Object moveDataFromTableItems() {
		Double lat = (Double) formsTableItems[0].getValue();
		Double lon = (Double) formsTableItems[1].getValue();
		Long timeInMillis = (Long) formsTableItems[2].getValue();
		if (lat == null || lon == null || timeInMillis==null) {
			return null;
		}
		return new LatLongTime(lat, lon, timeInMillis) ;
	}

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.ControlDescription#getHint()
	 */
	@Override
	public String getHint() {
		if (super.getHint()==null||super.getHint().length()==0){
			return"<html>Press F1 to update";
		}else{
			return super.getHint();
		}
	}
	@Override
	public void fillXMLDataElement(Document doc, Element el, Object data) {
		if (data == null) {
			return;
		}
		if (LatLongTime.class.isAssignableFrom(data.getClass()) == false) {
			return;
		}
		LatLongTime latLong = (LatLongTime) data;
		
		el.appendChild(createXMLDataItem(doc, formsTableItems[0].getName(), latLong.getLatitude()));
		el.appendChild(createXMLDataItem(doc, formsTableItems[1].getName(), latLong.getLongitude()));
	}

	@Override
	public Object extractXMLElementData(Element el, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
