package loggerForms;

import generalDatabase.PamTableItem;

public abstract class ItemDescription {

	private FormDescription formDescription;
	
	private ItemInformation itemInformation;

	/**
	 * @return the itemInformation
	 */
	public ItemInformation getItemInformation() {
		return itemInformation;
	}

	private int itemErrors = 0;

	public ItemDescription(FormDescription formDescription, ItemInformation itemInformation){
		this.formDescription = formDescription;
		this.itemInformation = itemInformation;
	}

	/**
	 * @param itemErrors the itemErrors to set
	 */
	public void setItemErrors(int itemErrors) {
		this.itemErrors = itemErrors;
	}

	/**
	 * Add one to the item error count. 
	 */
	public void addItemError() {
		this.itemErrors++;
	}
	
	/**
	 * @return the itemErrors
	 */
	public int getItemErrors() {
		return itemErrors;
	}

	/**
	 * To be used by control Description for to check necessary fields exist
	 * @return null if ok otherwise warning string
	 */
	public String getItemWarning() {
		return "";
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return itemInformation.getIntegerProperty(UDColName.Id.toString());
	}



	/**
	 * @return the order
	 */
	public Integer getOrder() {
		return itemInformation.getIntegerProperty(UDColName.Order.toString());
	}



	/**
	 * @return the type
	 */
	public String getType() {
		return itemInformation.getStringProperty(UDColName.Type.toString());
	}



	/**
	 * @return the title
	 */
	public String getTitle() {
		return itemInformation.getStringProperty(UDColName.Title.toString());
	}



	/**
	 * @return the postTitle
	 */
	public String getPostTitle() {
		return itemInformation.getStringProperty(UDColName.PostTitle.toString());
	}


	public int getNumDBColumns() {
		return 1;
	}

	/**
	 * Return the database title. It's perfectly acceptable for
	 * this not to have been filled in in which case
	 * return the main title of the control. If that is also null, 
	 * then it will cause an error.  
	 * @return the dbTitle
	 */
	public String getDbTitle() {
		String dbTitle = itemInformation.getStringProperty(UDColName.DbTitle.toString());
		if (dbTitle != null && dbTitle.length() > 0) {
			return dbTitle;
		}
		return getTitle();
	}



	/**
	 * @return the length
	 */
	public Integer getLength() {
		return itemInformation.getIntegerProperty(UDColName.Length.toString());
	}



	/**
	 * @return the topic
	 */
	public String getTopic() {		
		
		String topic = itemInformation.getStringProperty(UDColName.Topic.toString());
		if (topic != null && topic.length() > 0) {
			//TODO FIXME this maybe isn't great - means for heading if its null it tries to check the if title is a bearing type rather than default to True/etc
			return topic;
		}
		return getTitle();
	}



	/**
	 * @return the nmeaString
	 */
	public String getNmeaModule() {
		return itemInformation.getStringProperty(UDColName.NMEA_Module.toString());
	}


	/**
	 * @return the nmeaString
	 */
	public String getNmeaString() {
		return itemInformation.getStringProperty(UDColName.NMEA_String.toString());
	}



	/**
	 * @return the nmeaPosition
	 */
	public Integer getNmeaPosition() {
		return itemInformation.getIntegerProperty(UDColName.NMEA_Position.toString());
	}



	/**
	 * @return the required
	 */
	public Boolean getRequired() {
		return itemInformation.getBooleanProperty(UDColName.Required.toString());
	}



	/**
	 * @return the autoUpdate
	 */
	public Integer getAutoUpdate() {
		return itemInformation.getIntegerProperty(UDColName.AutoUpdate.toString());
	}

	/**
	 * @param the autoUpdate
	 */
	public void setAutoUpdate(Integer autoUpdate) {
		itemInformation.setProperty(UDColName.AutoUpdate.toString(), autoUpdate);
	}



	/**
	 * @return the autoclear
	 */
	public Boolean getAutoclear() {
		return itemInformation.getBooleanProperty(UDColName.Autoclear.toString());
	}



	/**
	 * @return the forceGps
	 */
	public Boolean getForceGps() {
		return itemInformation.getBooleanProperty(UDColName.ForceGps.toString());
	}



	/**
	 * @return the hint
	 */
	public String getHint() {
		return itemInformation.getStringProperty(UDColName.Hint.toString());
	}



	/**
	 * @return the adcChannel
	 */
	public Integer getAdcChannel() {
		return itemInformation.getIntegerProperty(UDColName.ADC_Channel.toString());
	}



	/**
	 * @return the adcGain
	 */
	public Integer getAdcGain() {
		return itemInformation.getIntegerProperty(UDColName.ADC_Gain.toString());
	}



	/**
	 * @return the analogueMultiply
	 */
	public Float getAnalogueMultiply() {
		return itemInformation.getFloatProperty(UDColName.Analog_Multiply.toString());
	}



	/**
	 * @return the analogueAdd
	 */
	public Float getAnalogueAdd() {
		return itemInformation.getFloatProperty(UDColName.Analog_Add.toString());
	}



	/**
	 * @return the plot
	 */
	public Boolean getPlot() {
		return itemInformation.getBooleanProperty(UDColName.Plot.toString());
	}



	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return itemInformation.getIntegerProperty(UDColName.Height.toString());
	}



	/**
	 * @return the colour
	 */
	public String getColour() {
		return itemInformation.getStringProperty(UDColName.Colour.toString());
	}



	/**
	 * @return the minValue
	 */
	public Float getMinValue() {
		return itemInformation.getFloatProperty(UDColName.MinValue.toString());
	}



	/**
	 * @return the maxValue
	 */
	public Float getMaxValue() {
		return itemInformation.getFloatProperty(UDColName.MaxValue.toString());
	}



	/**
	 * @return the readOnly
	 */
	public Boolean getReadOnly() {
		return itemInformation.getBooleanProperty(UDColName.ReadOnly.toString());
	}



	/**
	 * @return the sendControlName
	 */
	public String getSendControlName() {
		return itemInformation.getStringProperty(UDColName.Send_Control_Name.toString());
	}



	/**
	 * @return the controlOnSubform
	 */
	public String getControlOnSubform() {
		return itemInformation.getStringProperty(UDColName.Control_on_Subform.toString());
	}



	/**
	 * @return the getControlData
	 */
	public String getGetControlData() {
		return itemInformation.getStringProperty(UDColName.Get_Control_Data.toString());
	}



	/**
	 * @return the defaultField
	 */
	public String getDefaultValue() {
		return itemInformation.getStringProperty(UDColName.Default.toString());
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(Integer length) {
		itemInformation.setProperty(UDColName.Length.toString(), length);
	}

	/**
	 * @return the motherFormDescription
	 */
	public FormDescription getFormDescription() {
		return formDescription;
	}

	
}
