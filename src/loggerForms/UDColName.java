package loggerForms;

import java.sql.Types;

public enum UDColName {
	/*
	 * 
		addTableItem(order				= new PamTableItem("Order"				, Types.INTEGER));
		addTableItem(type				= new PamTableItem("Type"				, Types.CHAR, 50));
		addTableItem(title				= new PamTableItem("Title"				, Types.CHAR, 50));
		addTableItem(postTitle			= new PamTableItem("PostTitle"			, Types.CHAR, 50));
		addTableItem(dbTitle			= new PamTableItem("DbTitle"			, Types.CHAR, 50));
		addTableItem(length				= new PamTableItem("Length"				, Types.INTEGER));
		addTableItem(topic				= new PamTableItem("Topic"				, Types.CHAR,50));
		addTableItem(nmeaModule			= new PamTableItem("NMEA_Module"		, Types.CHAR,50));	//Space here
		addTableItem(nmeaString			= new PamTableItem("NMEA_String"		, Types.CHAR,50));	//Space here
		addTableItem(nmeaPosition		= new PamTableItem("NMEA_Position"		, Types.INTEGER));	//Space here
		addTableItem(required			= new PamTableItem("Required"			, Types.BOOLEAN));
		addTableItem(autoUpdate			= new PamTableItem("AutoUpdate"			, Types.INTEGER));
		addTableItem(autoclear			= new PamTableItem("Autoclear"			, Types.BOOLEAN));
		addTableItem(forceGps			= new PamTableItem("ForceGps"			, Types.BOOLEAN));
		addTableItem(hint				= new PamTableItem("Hint"				, Types.CHAR, 100));
		addTableItem(adcChannel			= new PamTableItem("ADC_Channel"		, Types.INTEGER));	//Space here
		addTableItem(adcGain			= new PamTableItem("ADC_Gain"			, Types.REAL));	//Space here
		addTableItem(analogueMultiply	= new PamTableItem("Analog_Multiply"	, Types.REAL));		//Space here
		addTableItem(analogueAdd		= new PamTableItem("Analog_Add"			, Types.REAL));		//Space here
		addTableItem(plot				= new PamTableItem("Plot"				, Types.BOOLEAN));
		addTableItem(height				= new PamTableItem("Height"				, Types.INTEGER));
		addTableItem(colour				= new PamTableItem("Colour"				, Types.CHAR, 20));
		addTableItem(minValue			= new PamTableItem("MinValue"			, Types.REAL));
		addTableItem(maxValue			= new PamTableItem("MaxValue"			, Types.REAL));
		addTableItem(readOnly			= new PamTableItem("ReadOnly"			, Types.BOOLEAN));
		addTableItem(sendControlName	= new PamTableItem("Send_Control_Name"	, Types.CHAR,50));	//Space here
		addTableItem(controlOnSubform	= new PamTableItem("Control_on_Subform"	, Types.CHAR,50));	//Space here
		addTableItem(getControlData		= new PamTableItem("Get_Control_Data"	, Types.CHAR,50));	//Space here
		addTableItem(defaultValue		= new PamTableItem("Default"			, Types.CHAR,50));
	 */
	Id (Types.INTEGER), 
	Order (Types.INTEGER), 
	Type(Types.CHAR, 50), 
	Title(Types.CHAR, 50), 
	PostTitle(Types.CHAR, 50), 
	DbTitle(Types.CHAR, 50), 
	Length(Types.INTEGER), 
	Topic(Types.CHAR, 50),
	NMEA_Module(Types.CHAR, 50),
	NMEA_String(Types.CHAR, 50),
	NMEA_Position(Types.INTEGER),
	Required(Types.BOOLEAN),
	AutoUpdate(Types.INTEGER),
	Autoclear(Types.BOOLEAN),
	ForceGps(Types.BOOLEAN),
	Hint(Types.CHAR, 100),
	ADC_Channel(Types.INTEGER),
	ADC_Gain(Types.REAL),
	Analog_Multiply(Types.REAL),
	Analog_Add(Types.REAL),
	Plot(Types.BOOLEAN),
	Height(Types.INTEGER),
	Colour(Types.CHAR, 20),
	MinValue(Types.REAL),
	MaxValue(Types.REAL),
	ReadOnly(Types.BOOLEAN),
	Send_Control_Name(Types.CHAR, 50),
	Control_on_Subform(Types.CHAR, 50),
	Get_Control_Data(Types.CHAR, 50),
	Default(Types.CHAR, 50);

	private final int sqlType;
	
	private final int stringLength;

	UDColName(int sqlType) {
		this.sqlType = sqlType;
		stringLength = 0;
	}
	
	UDColName(int sqlType, int stringLength) {
		this.sqlType = sqlType;
		this.stringLength = stringLength;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * @return the stringLength
	 */
	public int getStringLength() {
		return stringLength;
	}

	/**
	 * @return the type
	 */
	public int getSqlType() {
		return sqlType;
	}


}
