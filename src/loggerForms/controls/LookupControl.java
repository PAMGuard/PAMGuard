package loggerForms.controls;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import generalDatabase.lookupTables.LookupComponent;
import generalDatabase.lookupTables.LookupItem;
import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.CdLookup;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controls.LoggerControl.ComponentFocusListener;

public class LookupControl extends LoggerControl{

	private LookupComponent lookup;
	
	public LookupControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		
		lookup=new LookupComponent(controlDescription.getTopic(),((CdLookup)controlDescription).getLookupList());
//		lookup.setToolTipText(controlDescription.getHint());
		
		component.add(new PamLabel(controlDescription.getTitle()));
		component.add(lookup.getComponent());
		component.add(new PamLabel(controlDescription.getPostTitle()));
		addFocusListenerToAllSubComponants(new ComponentFocusListener(), component);
		setToolTipToAllSubJComponants(component);
		setDefault();
		
//		textField.addFocusListener(new ComponentFocusListener());
	}

	@Override
	public Object getData() {
		// TODO Auto-generated method stub
		LookupItem lutItem = lookup.getSelectedItem();
		if (lutItem == null) {
//			System.out.println("lut null");
			return null;
		}
		else {
//			System.out.println("lut "+lutItem.getCode());
			return lutItem.getCode();
		}
	}

	@Override
	public void setData(Object data) {
		lookup.setSelectedCode(String.valueOf(data));
		
	}

	@Override
	public void setDefault() {
		setData(getControlDescription().getDefaultValue());
	}

	@Override
	public String getDataError(){
		if (getData()==null){
			String text = (String) getData();
			if (text==null||text.length()==0){
				Boolean req = controlDescription.getRequired();
				if (req==true){
					return controlDescription.getTitle()+" is a required field.";
				}
				return null;
			}else{
				return dataError;
			}
			
		}
		return dataError;
	}

	@Override
	public int fillNMEAControlData(NMEADataUnit dataUnit) {
		String subStr = NMEADataBlock.getSubString(dataUnit.getCharData(), 
				controlDescription.getNmeaPosition());
		lookup.setSelectedCode(subStr);
		return AUTO_UPDATE_SUCCESS;
	}
	
}
