/**
 * 
 */
package loggerForms.controls;

import javax.swing.JLabel;

import NMEA.NMEADataUnit;
import PamUtils.LatLong;
import loggerForms.LatLongTime;
import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.CdLatLongTime;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controls.LoggerControl.ComponentFocusListener;

/**
 * @author GrahamWeatherup
 *
 */
public class LatLongTimeControl extends LoggerControl {

	
	LatLongControl latLongControl;
	TimestampControl timestampControl;
	/**
	 * @param controlDescription
	 * @param loggerForm
	 */
	public LatLongTimeControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		latLongControl = new LatLongControl(controlDescription, loggerForm);
		timestampControl = new TimestampControl(controlDescription, loggerForm);
		
		component.add(new LoggerFormLabel(loggerForm,controlDescription.getTitle()));
		component.add(new LoggerFormLabel(loggerForm,"Lat"));
		component.add(latLongControl.textFieldLat);
		component.add(new LoggerFormLabel(loggerForm,"Lon"));
		component.add(latLongControl.textFieldLon);
		component.add(new LoggerFormLabel(loggerForm,"Date"));
		component.add(timestampControl.textFieldDate);
		component.add(new LoggerFormLabel(loggerForm,"Time"));
		component.add(timestampControl.textFieldTime);
		component.add(new LoggerFormLabel(loggerForm,controlDescription.getPostTitle()));
		
		
		addFocusListenerToAllSubComponants(new ComponentFocusListener(), component);
		addMouseListenerToAllSubComponants(new HoverListener(), component);
		
		
		setToolTipToAllSubJComponants(component);
		controlMenu.removeAll();
		
		
	}
	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#getDataError()
	 */
	@Override
	public String getDataError() {
		String fullError=null;
		String ll = latLongControl.getDataError();
		String ts = timestampControl.getDataError();
		
		if (ll!=null&&ts!=null){
			fullError=ll+"\n"+ts;
		}else if (ll!=null){
			fullError=ll;
		}else if (ts!=null){
			fullError=ts;
		}
//		else{//both null
//			
//		}
		return fullError;
		
	}
	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#getData()
	 */
	@Override
	public Object getData() {
		LatLong ll = (LatLong) latLongControl.getData();
		Long ts = (Long) timestampControl.getData();
		if (ll==null) return null;
//		if (ll.getLatitude()==null||ll.getLongitude()) return null;
		return new LatLongTime(ll.getLatitude(),ll.getLongitude(),ts);
	}
	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#setData(java.lang.Object)
	 */
	@Override
	public void setData(Object data) {
		LatLongTime llt= (LatLongTime)data;
//		LatLong ll = llt.getLatLong();
		Long ts = llt.getTimeInMillis();
		latLongControl.setData(llt);
		timestampControl.setData(ts);
	}
	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#setDefault()
	 */
	@Override
	public void setDefault() {
		latLongControl.setDefault();
		timestampControl.setDefault();
	}
	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#fillNMEAControlData(NMEA.NMEADataUnit)
	 */
	@Override
	public int fillNMEAControlData(NMEADataUnit dataUnit) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	@Override
	public int autoUpdate() {
		if (loggerForm.getNewOrEdit()==LoggerForm.EditDataForm) return super.autoUpdate();
		int ll = latLongControl.autoUpdate();
		int ts = timestampControl.autoUpdate();
//		AUTO_UPDATE_CANT;
//		AUTO_UPDATE_FAIL;
//		AUTO_UPDATE_SUCCESS;
		if (ll==AUTO_UPDATE_FAIL||ts==AUTO_UPDATE_FAIL){
			return AUTO_UPDATE_FAIL;
		}
		if (ll==AUTO_UPDATE_CANT||ts==AUTO_UPDATE_CANT){
			return AUTO_UPDATE_CANT;
		}
		return AUTO_UPDATE_SUCCESS;
	}
	
}
