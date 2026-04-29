package loggerForms;

import java.sql.Types;

import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;

public class UDBTableDefinition extends EmptyTableDefinition {
	private PamTableItem order;				//Integer
	private PamTableItem type;				//String 50
	private PamTableItem title;				//String 50
	private PamTableItem postTitle;			//String 50
	private PamTableItem dbTitle;			//String 50
	private PamTableItem length;			//Integer
	private PamTableItem topic;				//String 50
//	private PamTableItem nmeaModule;		//String 50		//Spaces to deal with
//	private PamTableItem nmeaString;		//String 50		//Spaces to deal with
//	private PamTableItem nmeaPosition;		//Integer		//Spaces to deal with
//	private PamTableItem required;			//Boolean
//	private PamTableItem autoUpdate;		//Integer
//	private PamTableItem autoclear;			//Boolean
//	private PamTableItem forceGps;			//Boolean
	private PamTableItem hint;				//String 100
//	private PamTableItem adcChannel;		//Integer		//Spaces to deal with
//	private PamTableItem adcGain;			//Integer		//Spaces to deal with
//	private PamTableItem analogueMultiply;	//Single(What)	//Spaces to deal with
//	private PamTableItem analogueAdd;		//Single(What)	//Spaces to deal with
//	private PamTableItem plot;				//Boolean
//	private PamTableItem height;			//Integer
//	private PamTableItem colour;			//String 20
//	private PamTableItem minValue;			//Single(What)
//	private PamTableItem maxValue;			//Single(What)
//	private PamTableItem readOnly;			//Boolean
//	private PamTableItem sendControlName;	//String 50		//Spaces to deal with
	private PamTableItem controlOnSubform;	//String 50		//Spaces to deal with
//	private PamTableItem getControlData;	//String 50		//Spaces to deal with
//	private PamTableItem defaultValue;		//String 50		//Needs suffixed with 'Value'
	public UDBTableDefinition(String tableName) {
		super(tableName);
		addTableItem(order				= new PamTableItem("Order"				, Types.INTEGER)); // problems in MySQL
		addTableItem(type				= new PamTableItem("Type"				, Types.CHAR, 50));
		addTableItem(title				= new PamTableItem("Title"				, Types.CHAR, 50));
		addTableItem(postTitle			= new PamTableItem("PostTitle"			, Types.CHAR, 50));
		addTableItem(dbTitle			= new PamTableItem("DbTitle"			, Types.CHAR, 50));
		addTableItem(length				= new PamTableItem("Length"				, Types.INTEGER));
		addTableItem(topic				= new PamTableItem("Topic"				, Types.CHAR,50));
//		addTableItem(nmeaModule			= new PamTableItem("NMEA_Module"		, Types.CHAR,50));	//Space here
//		addTableItem(nmeaString			= new PamTableItem("NMEA_String"		, Types.CHAR,50));	//Space here
//		addTableItem(nmeaPosition		= new PamTableItem("NMEA_Position"		, Types.INTEGER));	//Space here
//		addTableItem(required			= new PamTableItem("Required"			, Types.BOOLEAN));
//		addTableItem(autoUpdate			= new PamTableItem("AutoUpdate"			, Types.INTEGER));
//		addTableItem(autoclear			= new PamTableItem("Autoclear"			, Types.BOOLEAN));
//		addTableItem(forceGps			= new PamTableItem("ForceGps"			, Types.BOOLEAN));
		addTableItem(hint				= new PamTableItem("Hint"				, Types.CHAR, 100));
//		addTableItem(adcChannel			= new PamTableItem("ADC_Channel"		, Types.INTEGER));	//Space here
//		addTableItem(adcGain			= new PamTableItem("ADC_Gain"			, Types.REAL));	//Space here
//		addTableItem(analogueMultiply	= new PamTableItem("Analog_Multiply"	, Types.REAL));		//Space here
//		addTableItem(analogueAdd		= new PamTableItem("Analog_Add"			, Types.REAL));		//Space here
//		addTableItem(plot				= new PamTableItem("Plot"				, Types.BOOLEAN));
//		addTableItem(height				= new PamTableItem("Height"				, Types.INTEGER));
//		addTableItem(colour				= new PamTableItem("Colour"				, Types.CHAR, 20));
//		addTableItem(minValue			= new PamTableItem("MinValue"			, Types.REAL));
//		addTableItem(maxValue			= new PamTableItem("MaxValue"			, Types.REAL));   // problems with MySQL
//		addTableItem(readOnly			= new PamTableItem("ReadOnly"			, Types.BOOLEAN));
//		addTableItem(sendControlName	= new PamTableItem("Send_Control_Name"	, Types.CHAR,50));	//Space here
		addTableItem(controlOnSubform	= new PamTableItem("Control_on_Subform"	, Types.CHAR,50));	//Space here
//		addTableItem(getControlData		= new PamTableItem("Get_Control_Data"	, Types.CHAR,50));	//Space here
//		addTableItem(defaultValue		= new PamTableItem("Default"			, Types.CHAR,50));
	}

}
