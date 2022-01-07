package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.BooleanCtrlColPanel;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.IntegerCtlrColPanel;
import loggerForms.formdesign.itempanels.TextCtrlColPanel;
import loggerForms.formdesign.itempanels.TitleCtrlColPanel;

/**
 * Property set for all 
 * @author Doug
 *
 */
public class BasePropertySet {


	protected FormDescription formDescription;
	
	protected ControlTitle controlTitle;
	
	public BasePropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super();
		this.formDescription = formDescription;
		this.controlTitle = controlTitle;
	}
	
	public String getPanelTitle() {
		return "Properties for " + controlTitle.getType().toString() + " Control type";
	}

	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case ADC_Channel:
			break;
		case ADC_Gain:
			break;
		case Analog_Add:
			break;
		case Analog_Multiply:
			break;
		case AutoUpdate:
			break;
		case Autoclear:
			return new BooleanCtrlColPanel(controlTitle, UDColName.Autoclear);
		case Colour:
			break;
		case Control_on_Subform:
			break;
		case DbTitle:
			return new TextCtrlColPanel(controlTitle, UDColName.DbTitle, UDColName.DbTitle.getStringLength());
		case Default:
			break;
		case ForceGps:
			break;
		case Get_Control_Data:
			break;
		case Height:
			break;
		case Hint:
			return new TextCtrlColPanel(controlTitle, UDColName.Hint, UDColName.Hint.getStringLength());
		case Id:
			break;
		case Length:
			return new IntegerCtlrColPanel(controlTitle, UDColName.Length, "Length of field to display on form");
		case MaxValue:
			break;
		case MinValue:
			break;
		case NMEA_Module:
			break;
		case NMEA_Position:
			break;
		case NMEA_String:
			break;
		case Order:
			break;
		case Plot:
			return new BooleanCtrlColPanel(controlTitle, UDColName.Plot);
		case PostTitle:
			return new TextCtrlColPanel(controlTitle, UDColName.PostTitle, UDColName.PostTitle.getStringLength());
		case ReadOnly:
			return new BooleanCtrlColPanel(controlTitle, UDColName.ReadOnly);
		case Required:
			return new BooleanCtrlColPanel(controlTitle, UDColName.Required);
		case Send_Control_Name:
			break;
		case Title:
			return new TitleCtrlColPanel(controlTitle, UDColName.Title, UDColName.Title.getStringLength());
		case Topic:
			break;
		case Type:
			return null;
		default:
			break;
		
		}
		return null;
	}

}
