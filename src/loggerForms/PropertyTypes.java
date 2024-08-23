package loggerForms;

public enum PropertyTypes {
	STARTTIME, ENDTIME, ORDER, AUTOALERT,SUBTABS,POPUP,HIDDEN,AUTORECORD,BEARING,
	RANGE,HEADING,FONT,DBTABLENAME,
	FORMCOLOUR,FORMCOLOR,
	HOTKEY,NOCLEAR,NOCANCEL,NOTOFFLINE,NOTONLINE,
	PLOT,READONTIMER,READONGPS,READONNMEA,
	SYMBOLTYPE;
	//,BLOBSIZE;
	
	/**
	 * Properties from Logger which are not currently used. 
	 * DDEENABLE,NOTSHOWDATA,GPSDATASET,
	 */

	public String getDescription() {
		switch (this) {
		case STARTTIME:
			return "Form start time. Will be used as main data UTC. If undefined, form time is save time";
		case ENDTIME:
			return "Form end time. Will be used data end time in period data";
		case AUTOALERT:
			return "A warning will be issued if the form has not been completed for a set time";
		case AUTORECORD:
			return "An audio recording will be made whenever the form is completed";
		case BEARING:
			return "Set a control who's content will be interpreted as bearing information";
//		case BLOBSIZE:
//			return "Sets the default size of symbols displayed on the map";
		case DBTABLENAME:
			return "The name of the output database table name (default is the form's name)";
		case FONT:
			return "The font to use for controls on the form";
		case FORMCOLOR:
			return "The color of the form";
		case FORMCOLOUR:
			return "The colour of the form";
		case HEADING:
			return "Set a control who's content will be interpreted as heading information";
		case HIDDEN:
			return "Hide the form (generally used with forms which can auto complete, e.g. with NMEA data)";
		case HOTKEY:
			return "A function key which will open the form";
		case NOCANCEL:
			return "Once opened the form cannot be cancelled";
		case NOCLEAR:
			return "The form cannot be cleared";
		case NOTOFFLINE:
			return "The form is not displayed in Viewer mode";
		case NOTONLINE:
			return "The form is not displayed during normal Pamguard operation";
		case PLOT:
			return "The form can be plotted on the map";
		case RANGE:
			return "Set a control who's content will be interpreted as range information";
		case READONGPS:
			return "The form will be automatically read every time new GPS data arrive in PAMGuard";
		case READONNMEA:
			return "The form will be automatically read every time new NMEA data arrive in PAMGuard";
		case READONTIMER:
			return "The form will be automatically read on a timer";
//		case SUBFORM:
//			return "The form is a subform, launched from a button within another form";
		case SUBTABS:
			return "Multiple instances of the form can be opened in separate tabs";
		case SYMBOLTYPE:
			return "The symbol time for the map display";
		case POPUP:
			return "The form opens in a separate window (not currently implemented)";
		case ORDER:
			return "The order forms will be displayed in on the display";
		default:
			break;
		
		}
		return null;
	}
}
