package loggerForms;

import java.sql.Types;
import java.util.Hashtable;

import generalDatabase.PamTableItem;
import loggerForms.controlDescriptions.ControlTypes;

/**
 * 
 * @author Graham Weatherup - SMRU
 * Contains all the information about a single line in the UDF database table. 
 *
 */
public  class ItemInformation implements Cloneable {


	protected FormDescription formDescription;

	private Hashtable<String, Object> propertyTable = new Hashtable<>();	

	public ItemInformation(FormDescription formDescription) {
		super();
		this.formDescription = formDescription;
	}

	/**
	 * Move data from the item back into the appropriate columns of the udf table
	 * ready for writing to the database. 
	 */
	public void writeTableDefRecord() {
		UDFTableDefinition udfTableDefinition = formDescription.getUdfTableDefinition();
		int nItems = udfTableDefinition.getTableItemCount();
		for (int i = 0; i <nItems; i++) {
			PamTableItem tableItem = udfTableDefinition.getTableItem(i);
			tableItem.setValue(getProperty(tableItem.getName(), tableItem.getSqlType()));
		}
	}

	/**
	 * Read the data for a single from item (control or command) from the UDF table 
	 * definition. Store in local variables for use as required when forms are created.
	 */
	public void readTableDefRecord() {
		UDFTableDefinition udfTableDefinition = formDescription.getUdfTableDefinition();
		int nItems = udfTableDefinition.getTableItemCount();
		for (int i = 0; i <nItems; i++) {
			PamTableItem tableItem = udfTableDefinition.getTableItem(i);
			setProperty(tableItem.getName(), tableItem.getValue());
		}
		/*
		 * For most of the parameters, use getValue instead of functions like getIntegerValue().
		 * This is because getIntegerValue will convert null's to zeros and it's important that 
		 * the nulls remain nulls for correct control operation. 
		 * The exception is getStringValue since this will still return null if the value is
		 * null, but will trim non null strings - also very important ! 
		 */
		//		id				= (Integer) udfTableDefinition.getIndexItem()		.getValue();
		//		order			= (Integer) udfTableDefinition.getOrder()			.getValue();
		//		type			= udfTableDefinition.getType()						.getStringValue();
		//		title			= udfTableDefinition.getTitle()						.getStringValue();
		//		setProperty(UDColName.Title, property)
		//		postTitle		= udfTableDefinition.getPostTitle()					.getStringValue();
		//		dbTitle			= udfTableDefinition.getDbTitle()					.getStringValue();
		//		length			= (Integer) udfTableDefinition.getLength()			.getValue();
		//		topic			= udfTableDefinition.getTopic()						.getStringValue();
		//		nmeaModule		= udfTableDefinition.getNmeaModule()				.getStringValue();	
		//		nmeaString		= udfTableDefinition.getNmeaString()				.getStringValue();
		//		nmeaPosition	= (Integer) udfTableDefinition.getNmeaPosition()	.getValue();
		//		required		= (Boolean) udfTableDefinition.getRequired()		.getValue();
		//		autoUpdate		= (Integer) udfTableDefinition.getAutoUpdate()		.getValue();
		//		autoclear		= (Boolean) udfTableDefinition.getAutoclear()		.getValue();
		//		forceGps		= (Boolean) udfTableDefinition.getForceGps()		.getValue();
		//		hint			=  udfTableDefinition.getHint()						.getStringValue();
		//		adcChannel		= (Integer) udfTableDefinition.getAdcChannel()		.getValue();
		//		adcGain			= (Integer) udfTableDefinition.getAdcGain()			.getValue();
		//		analogueMultiply= (Float) udfTableDefinition.getAnalogueMultiply()	.getValue();
		//		analogueAdd		= (Float) udfTableDefinition.getAnalogueAdd()		.getValue();
		//		plot			= (Boolean) udfTableDefinition.getPlot()			.getValue();
		//		height			= (Integer) udfTableDefinition.getHeight()			.getValue();
		//		colour			=  udfTableDefinition.getColour()					.getStringValue();
		//		minValue		= (Float) udfTableDefinition.getMinValue()			.getValue();
		//		maxValue		= (Float) udfTableDefinition.getMaxValue()			.getValue();
		//		readOnly		= (Boolean) udfTableDefinition.getReadOnly()		.getValue();
		//		sendControlName	= udfTableDefinition.getSendControlName()			.getStringValue();
		//		controlOnSubform= udfTableDefinition.getControlOnSubform()			.getStringValue();
		//		getControlData	= udfTableDefinition.getGetControlData()			.getStringValue();
		//		defaultValue	= udfTableDefinition.getDefaultValue()				.getStringValue();
	}
	@Override
	public ItemInformation clone() {
		try {
			ItemInformation newDescription = (ItemInformation) super.clone();
			newDescription.propertyTable = (Hashtable<String, Object>) this.propertyTable.clone();
			return newDescription;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * non static this means blank item description has to be created then a
	 * @return
	 */
	/*public JPanel getEditPanel(){

		JPanel editPanel = new PamPanel();
		GridLayout layout;
		editPanel.setLayout(layout=new GridLayout(29,2));

		editPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);


		JTextField id				= new JTextField(); id.setText				(String.valueOf(this.id));						//Integer	id;
		JTextField order			= new JTextField(); order.setText			(String.valueOf(this.order));					//Integer	order;
		JTextField type				= new JTextField(); type.setText			(String.valueOf(this.type));					//String	type;
		JTextField title			= new JTextField(); title.setText			(String.valueOf(this.title));					//String	title;
		JTextField postTitle		= new JTextField(); postTitle.setText		(String.valueOf(this.postTitle));				//String	postTitle;
		JTextField dbTitle			= new JTextField(); dbTitle.setText			(String.valueOf(this.dbTitle));					//String	dbTitle;
		JTextField length			= new JTextField(); length.setText			(String.valueOf(this.length));					//Integer	length;
		JTextField topic			= new JTextField(); topic.setText			(String.valueOf(this.topic));					//String	topic;
		JTextField nmeaModule		= new JTextField(); nmeaModule.setText		(String.valueOf(this.nmeaModule));				//String	nmeaModule;
		JTextField nmeaString		= new JTextField(); nmeaString.setText		(String.valueOf(this.nmeaString));				//String	nmeaString;
		JTextField nmeaPosition		= new JTextField(); nmeaPosition.setText	(String.valueOf(this.nmeaPosition));			//Integer	nmeaPosition;
		JCheckBox  required			= new JCheckBox();	required.setSelected	(this.required);								//Boolean	required;
		JTextField autoUpdate		= new JTextField(); autoUpdate.setText		(String.valueOf(this.autoUpdate));				//Integer autoUpdate;
		JCheckBox  autoclear		= new JCheckBox();	required.setSelected	(this.autoclear);								//Boolean	autoclear;
		JCheckBox  forceGps			= new JCheckBox();	required.setSelected	(this.forceGps);								//Boolean	forceGps;
		JTextField hint				= new JTextField(); hint.setText			(String.valueOf(this.hint));					//String	hint;
		JTextField adcChannel		= new JTextField(); adcChannel.setText		(String.valueOf(this.adcChannel));				//Integer	adcChannel;
		JTextField adcGain			= new JTextField(); adcGain.setText			(String.valueOf(this.adcGain));					//Integer	adcGain;
		JTextField analogueMultiply	= new JTextField(); analogueMultiply.setText(String.valueOf(this.analogueMultiply));		//Float	analogueMultiply;
		JTextField analogueAdd		= new JTextField(); analogueAdd.setText		(String.valueOf(this.analogueAdd));				//Float	analogueAdd;
		JCheckBox  plot				= new JCheckBox();	required.setSelected	(this.plot);									//Boolean	plot;
		JTextField height			= new JTextField(); height.setText			(String.valueOf(this.height));					//Integer	height;
		JTextField colour			= new JTextField(); colour.setText			(String.valueOf(this.colour));					//String	colour;
		JTextField minValue			= new JTextField(); minValue.setText		(String.valueOf(this.minValue));				//Float	minValue;
		JTextField maxValue			= new JTextField(); maxValue.setText		(String.valueOf(this.maxValue));				//Float	maxValue;
		JCheckBox  readOnly			= new JCheckBox();	required.setSelected	(this.readOnly);								//Boolean	readOnly;
		JTextField sendControlName	= new JTextField(); sendControlName.setText	(String.valueOf(this.sendControlName));			//String	sendControlName;
		JTextField controlOnSubform	= new JTextField(); controlOnSubform.setText(String.valueOf(this.controlOnSubform));		//String	controlOnSubform;
		JTextField getControlData	= new JTextField(); getControlData.setText	(String.valueOf(this.getControlData));			//String	getControlData;
		JTextField defaultValue		= new JTextField(); defaultValue.setText	(String.valueOf(this.defaultValue));			//String	defaultValue;

		editPanel.add(new JLabel("id"));				editPanel.add(id);
		editPanel.add(new JLabel("order"));				editPanel.add(order);
		editPanel.add(new JLabel("type"));				editPanel.add(type);
		editPanel.add(new JLabel("title"));				editPanel.add(title);
		editPanel.add(new JLabel("postTitle"));			editPanel.add(postTitle);
		editPanel.add(new JLabel("dbTitle"));			editPanel.add(dbTitle);
		editPanel.add(new JLabel("length"));			editPanel.add(length);
		editPanel.add(new JLabel("topic"));				editPanel.add(topic);
		editPanel.add(new JLabel("nmeaModule"));		editPanel.add(nmeaModule);
		editPanel.add(new JLabel("nmeaString"));		editPanel.add(nmeaString);
		editPanel.add(new JLabel("nmeaPosition"));		editPanel.add(nmeaPosition);
		editPanel.add(new JLabel("required"));			editPanel.add(required);
		editPanel.add(new JLabel("autoUpdate"));		editPanel.add(autoUpdate);
		editPanel.add(new JLabel("autoclear"));			editPanel.add(autoclear);
		editPanel.add(new JLabel("forceGps"));			editPanel.add(forceGps);
		editPanel.add(new JLabel("hint"));				editPanel.add(hint);
		editPanel.add(new JLabel("adcChannel"));		editPanel.add(adcChannel);
		editPanel.add(new JLabel("adcGain"));			editPanel.add(adcGain);
		editPanel.add(new JLabel("analogueMultiply"));	editPanel.add(analogueMultiply);
		editPanel.add(new JLabel("analogueAdd"));		editPanel.add(analogueAdd);
		editPanel.add(new JLabel("plot"));				editPanel.add(plot);
		editPanel.add(new JLabel("height"));			editPanel.add(height);
		editPanel.add(new JLabel("colour"));			editPanel.add(colour);
		editPanel.add(new JLabel("minValue"));			editPanel.add(minValue);
		editPanel.add(new JLabel("maxValue"));			editPanel.add(maxValue);
		editPanel.add(new JLabel("readOnly"));			editPanel.add(readOnly);
		editPanel.add(new JLabel("sendControlName"));	editPanel.add(sendControlName);
		editPanel.add(new JLabel("controlOnSubform"));	editPanel.add(controlOnSubform);
		editPanel.add(new JLabel("getControlData"));	editPanel.add(getControlData);
		editPanel.add(new JLabel("defaultValue"));		editPanel.add(defaultValue);



		return editPanel;
	}/*







	/**
	 * @return the udfTableDefinition
	 */
	public UDFTableDefinition getUdfTableDefinition() {
		return formDescription.getUdfTableDefinition();
	}



	/**
	 * Set a property in the property table. Null values are not allowed, 
	 * so must be removed. Existing values will be overwritten. 
	 * @param propertyKey prperty key name
	 * @param property Property object
	 * @return previous property value (or null)
	 */
	public Object setProperty(String propertyKey, Object property) {
		if (property == null) {
			return propertyTable.remove(propertyKey);
		}
		else if (property.getClass() == String.class) {
			return propertyTable.put(propertyKey, ((String) property).trim());
		}
		else{ 
			return propertyTable.put(propertyKey, property);
		}
	}

	private Object getProperty(String name, int sqlType) {
		switch(sqlType) {
		case Types.REAL:
		case Types.FLOAT:
			return getFloatProperty(name);
		case Types.INTEGER:
			return getIntegerProperty(name);
		case Types.CHAR:
			return getStringProperty(name);
		case Types.BIT:
		case Types.BOOLEAN:
			return getBooleanProperty(name);
		}
		System.err.println("Unknown property type in UDF table re-writing: " + sqlType);
		return null;
	}

	/**
	 * Get a string property from the hash table, 
	 * returning null if the property does not exist. 
	 * @param title property key
	 * @return null or the property cast as a String
	 */
	public String getStringProperty(String title) {
		Object o = propertyTable.get(title);
		if (o == null) {
			return null;
		}
		return ((String) o).trim();
	}

	/**
	 * Get an Integer property from the hash table, 
	 * returning null if the property does not exist. 
	 * @param propertyKey property key
	 * @return null or the property cast as a Integer
	 */
	public Integer getIntegerProperty(String propertyKey) {
		Object o = propertyTable.get(propertyKey);
		if (o == null) {
			return null;
		}
		try {
			return (Integer) o;
		}
		catch (ClassCastException e) {
			System.out.println(o.toString());
			return null;
		}
	}

	/**
	 * Get a Float property from the hash table, 
	 * returning null if the property does not exist. 
	 * @param propertyKey property key
	 * @return null or the property cast as a Float
	 */
	public Float getFloatProperty(String propertyKey) {
		Object o = propertyTable.get(propertyKey);
		if (o == null) {
			return null;
		}
		try {
			return (Float) o;
		}
		catch (ClassCastException e) {
			System.out.printf("Error casting %s of type %s to Float: %s\n", o.toString(), o.getClass().getName(), e.getMessage());
		}
		return null;
	}

	/**
	 * Get a Boolean property from the hash table, 
	 * returning null if the property does not exist. 
	 * @param propertyKey property key
	 * @return null or the property cast as a Boolean
	 */
	public Boolean getBooleanProperty(String propertyKey) {
		Object o = propertyTable.get(propertyKey);
		if (o == null) {
			return false;
		}
		if (Boolean.class == o.getClass()) {
			return (Boolean) o;
		}
		else {
			// need these next lines to deal with SQLite which does not
			// support boolean data and returns an Integer instead. 
			// But assume it's ANY Integer type so convert to a string
			// and try to read back the string !!
			Integer i = 0;
			try {
				i = new Integer(o.toString());
			}
			catch (NumberFormatException e) {
				return false;
			}
			return (i != 0);
		}
	}

	/**
	 * @return the formDescription
	 */
	public FormDescription getFormDescription() {
		return formDescription;
	}

	/**
	 * 
	 * @return the ControlType (INTEGER, LOOKUP, etc.)
	 */
	public ControlTypes getControlType() {
		String type = getStringProperty(UDColName.Type.toString());
		if (type == null) return null;
		try {
			return ControlTypes.valueOf(type);
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}

}
