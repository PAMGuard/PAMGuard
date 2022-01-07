package loggerForms;

import java.sql.Types;

import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;



/**
 * 
 * @author Graham Weatherup
 * @extends EmptyTableDefinition
 * 
 */
public class UDFTableDefinition extends EmptyTableDefinition {
	
	private PamTableItem order;				//Integer
	private PamTableItem type;				//String 50
	private PamTableItem title;				//String 50
	private PamTableItem postTitle;			//String 50
	private PamTableItem dbTitle;			//String 50
	private PamTableItem length;			//Integer
	private PamTableItem topic;				//String 50
	private PamTableItem nmeaModule;		//String 50		//Spaces to deal with
	private PamTableItem nmeaString;		//String 50		//Spaces to deal with
	private PamTableItem nmeaPosition;		//Integer		//Spaces to deal with
	private PamTableItem required;			//Boolean
	private PamTableItem autoUpdate;		//Integer
	private PamTableItem autoclear;			//Boolean
	private PamTableItem forceGps;			//Boolean
	private PamTableItem hint;				//String 100
	private PamTableItem adcChannel;		//Integer		//Spaces to deal with
	private PamTableItem adcGain;			//Integer		//Spaces to deal with
	private PamTableItem analogueMultiply;	//Single(What)	//Spaces to deal with
	private PamTableItem analogueAdd;		//Single(What)	//Spaces to deal with
	private PamTableItem plot;				//Boolean
	private PamTableItem height;			//Integer
	private PamTableItem colour;			//String 20
	private PamTableItem minValue;			//Single(What)
	private PamTableItem maxValue;			//Single(What)
	private PamTableItem readOnly;			//Boolean
	private PamTableItem sendControlName;	//String 50		//Spaces to deal with
	private PamTableItem controlOnSubform;	//String 50		//Spaces to deal with
	private PamTableItem getControlData;	//String 50		//Spaces to deal with
	private PamTableItem defaultValue;		//String 50		//Needs suffixed with 'Value'

	public UDFTableDefinition(String tableName) {
		super(tableName);
		//getColumnNames(tableName);
		// no need to Id item - it's already in the EmptyTableDefinition.
		addTableItem(order				= new PamTableItem("Order"				, Types.INTEGER)); // problems in MySQL
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
		addTableItem(maxValue			= new PamTableItem("MaxValue"			, Types.REAL));   // problems with MySQL
		addTableItem(readOnly			= new PamTableItem("ReadOnly"			, Types.BOOLEAN));
		addTableItem(sendControlName	= new PamTableItem("Send_Control_Name"	, Types.CHAR,50));	//Space here
		addTableItem(controlOnSubform	= new PamTableItem("Control_on_Subform"	, Types.CHAR,50));	//Space here
		addTableItem(getControlData		= new PamTableItem("Get_Control_Data"	, Types.CHAR,50));	//Space here
		addTableItem(defaultValue		= new PamTableItem("Default"			, Types.CHAR,50));
		
	}
/*
	private void getColumnNames(String tableName) {
		// TODO Auto-generated method stub
		
		DatabaseMetaData dbmd = dbCon.getMetaData();
		String[]types = {"TABLE"};
		ResultSet resultSet = dbmd.getTables(null, null, "%", types);
	
		
		
		//Loop through database tables
		while (resultSet.next()){
			String tableName = resultSet.getString(3);
			//If starts with 'UDF_' create form description from it.
			if(  tableName.startsWith(this.tableName)){
				udfTableNameList.add(tableName);
			}
		}
		
		
	} */	


	/**
	 * @return the order
	 */
	public PamTableItem getOrder() {
		return order;
	}

	/**
	 * @return the type
	 */
	public PamTableItem getType() {
		return type;
	}

	/**
	 * @return the title
	 */
	public PamTableItem getTitle() {
		return title;
	}

	/**
	 * @return the postTitle
	 */
	public PamTableItem getPostTitle() {
		return postTitle;
	}

	/**
	 * @return the dbTitle
	 */
	public PamTableItem getDbTitle() {
		return dbTitle;
	}

	/**
	 * @return the length
	 */
	public PamTableItem getLength() {
		return length;
	}

	/**
	 * @return the topic
	 */
	public PamTableItem getTopic() {
		return topic;
	}

	/**
	 * @return the nmeaString
	 */
	public PamTableItem getNmeaModule() {
		return nmeaModule;
	}

	/**
	 * @return the nmeaString
	 */
	public PamTableItem getNmeaString() {
		return nmeaString;
	}

	/**
	 * @return the nmeaPosition
	 */
	public PamTableItem getNmeaPosition() {
		return nmeaPosition;
	}

	/**
	 * @return the required
	 */
	public PamTableItem getRequired() {
		return required;
	}

	/**
	 * @return the autoUpdate
	 */
	public PamTableItem getAutoUpdate() {
		return autoUpdate;
	}

	/**
	 * @return the autoclear
	 */
	public PamTableItem getAutoclear() {
		return autoclear;
	}

	/**
	 * @return the forceGps
	 */
	public PamTableItem getForceGps() {
		return forceGps;
	}

	/**
	 * @return the hint
	 */
	public PamTableItem getHint() {
		return hint;
	}

	/**
	 * @return the adcChannel
	 */
	public PamTableItem getAdcChannel() {
		return adcChannel;
	}

	/**
	 * @return the adcGain
	 */
	public PamTableItem getAdcGain() {
		return adcGain;
	}

	/**
	 * @return the analogueMultiply
	 */
	public PamTableItem getAnalogueMultiply() {
		return analogueMultiply;
	}

	/**
	 * @return the analogueAdd
	 */
	public PamTableItem getAnalogueAdd() {
		return analogueAdd;
	}

	/**
	 * @return the plot
	 */
	public PamTableItem getPlot() {
		return plot;
	}

	/**
	 * @return the height
	 */
	public PamTableItem getHeight() {
		return height;
	}

	/**
	 * @return the colour
	 */
	public PamTableItem getColour() {
		return colour;
	}

	/**
	 * @return the minValue
	 */
	public PamTableItem getMinValue() {
		return minValue;
	}

	/**
	 * @return the maxValue
	 */
	public PamTableItem getMaxValue() {
		return maxValue;
	}

	/**
	 * @return the readOnly
	 */
	public PamTableItem getReadOnly() {
		return readOnly;
	}

	/**
	 * @return the sendControlName
	 */
	public PamTableItem getSendControlName() {
		return sendControlName;
	}

	/**
	 * @return the controlOnSubform
	 */
	public PamTableItem getControlOnSubform() {
		return controlOnSubform;
	}

	/**
	 * @return the getControlData
	 */
	public PamTableItem getGetControlData() {
		return getControlData;
	}

	/**
	 * @return the defaultValue
	 */
	public PamTableItem getDefaultValue() {
		return defaultValue;
	}
	
	
	
}
