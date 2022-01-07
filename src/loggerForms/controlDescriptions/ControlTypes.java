package loggerForms.controlDescriptions;

public enum ControlTypes {
		CHAR,
		CHECKBOX,
		COUNTER,
		DOUBLE,
//		GPSTIMESTAMP,
		INTEGER,
		HSPACE,
		LATLONG,
//		LATLONGTIME,
		LOOKUP,
		NEWLINE,
		NMEACHAR,
		NMEAFLOAT,
		NMEAINT,
		SHORT,
		SINGLE,
		STATIC,
		TIME,
		TIMESTAMP,
		SUBFORM,
//		ANALOGUE,
//		DIGITAL,
		VSPACE;
	 
		public String getDescription() {
			switch (this) {
			case CHAR:
				return "Creates a field for typing text data";
			case CHECKBOX:
				return "Creates a checkbox for entering boolean or yes / no data";
			case COUNTER:
				return "Creates a counter which increments automatically for each form";
			case DOUBLE:
				return "Creates a field for entering double precision (64 bit) floating point data";
			case HSPACE:
				return "Creates a horizontal space between controls";
			case INTEGER:
				return "Creates a field for entering integer data";
			case LATLONG:
				return "Creates a field for entering Longitudes and Latitudes";
			case LOOKUP:
				return "Creates a Lookup table (drop down list)";
			case NEWLINE:
				return "Forces the next control onto a new line on the form";
			case NMEACHAR:
				return "Creates a field for automatically receiving NMEA text data";
			case NMEAFLOAT:
				return "Creates a field for automatically receiving NMEA floating point data";
			case NMEAINT:
				return "Creates a field for automatically receiving NMEA integer data";
			case SHORT:
				return "Creates a field for entering short integer data (between -32768 and +32767)";
			case SINGLE:
				return "Creates a field for entering single precision (32 bit) floating point data";
			case STATIC:
				return "Creates text on the form";
			case SUBFORM:
				return "Creates a button which is used to pop up another form";
			case TIME:
				return "Creates a field for entering times";
			case TIMESTAMP:
				return "Creates a field for entering dates and times";
			case VSPACE:
				return "Creates a vertical space between controls";
			default:
				break;
			
			}
			return null;
		}
		
		/**
		 * 
		 * @return weather or not the data are numeric
		 */
		public boolean isNumeric() {
			switch (this) {
			case DOUBLE:
			case INTEGER:
			case NMEAFLOAT:
			case NMEAINT:
			case SHORT:
			case SINGLE:
				return true;
			default:
				return false;
			}
		}
}
