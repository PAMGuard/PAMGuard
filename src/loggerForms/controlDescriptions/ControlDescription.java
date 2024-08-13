/**
 * 
 */
package loggerForms.controlDescriptions;

import java.sql.Types;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import loggerForms.FormDescription;
import loggerForms.FormsDataUnit;
import loggerForms.FormsTableItem;
import loggerForms.ItemDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;

/**
 * @author GrahamWeatherup
 *
 */
public abstract class ControlDescription extends ItemDescription  {
	
	protected int primarySQLType;
	
	protected FormsTableItem[] formsTableItems;

	/**
	 * @param formDescription
	 */
	protected ControlDescription(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		eType = ControlTypes.valueOf(getType());
	}
	
	
	
	
	
	
	/**
	 * needs overridden for 1-many/many-many/many-a relationship
	 * @return
	 */
	public FormsTableItem[] getFormsTableItems(){
		if (formsTableItems==null){
			
			String  nm = getDbTitle();
			
			int dt = primarySQLType;
			Integer ln = getLength();
			
			Boolean rq = getRequired();
			
			int b = Types.CHAR;
			/*
			 * check to see values make sense then
			 * make table item using FormsTableItem constructor.
			 */
			if (ln == null) ln = 0;
			if (rq==null) rq=false;
			
			if (nm==null){
				getFormDescription().getFormWarnings().add("Title: "+getTitle()+" at Order: "+getOrder()+" in Table: "+getFormDescription().getUdfName()+"has no DbTitle and Title is not suitable so will not be saved");
				
			}else{
				if (dt==1&&getLength()==null){
					ln=10;
					getFormDescription().getFormWarnings().add("Title: "+getTitle()+" at Order: "+getOrder()+" in Table: "+getFormDescription().getUdfName()+" is of char type and no length is specified, this will be set to "+ln);
					
				}
				FormsTableItem[] tis={new FormsTableItem(this, nm, dt, ln, rq)};
				formsTableItems=tis;
				return formsTableItems;
			}
//			FormsTableItem a = new FormsTableItem(this, EmptyTableDefinition.deblankString(getDbTitle()), primarySQLType);
			
//			FormsTableItem[] tis={a};
//			formsTableItems=tis;
		}
		return formsTableItems;
	}
	
	private ControlTypes eType;
	
	/**
	 * @return the eType
	 */
	public ControlTypes getEType() {
		return eType;
	}
	
	
	
	/**
	 * 
	 * @return FormsTableItems related to this controlDescription
	 */
//	public abstract FormsTableItem[] getFormsTableItems();
	
	
	
	
	
//	int getDataType(){
//		switch (eType) {
//		case CHAR		: return Types.CHAR;	//1
//		case CHECKBOX	: return Types.BIT;	//16
//		case COUNTER	:	if(getLength()==null||getLength()==0){
//								setLength(4);
//							}
//							return Types.CHAR;	//1
//		case DOUBLE		: return Types.DOUBLE;	//8
//		case GPSSTAMP	: return Types.DOUBLE;	//8
//		
//		case INTEGER	: return Types.INTEGER;	//4
//		case LOOKUP		:	if(getLength()==null||getLength()==0){
//								setLength(5);
//							}
//							return Types.CHAR;	//1
//		case NMEACHAR	: return Types.CHAR;	//1
//		case NMEAFLOAT	: return Types.REAL;	//7 // real seems to work with Access. Single and Float won't support null data. 
//		case NMEAINT	: return Types.INTEGER;	//4
//		case SHORT		: return Types.INTEGER;	//4
//		case SINGLE		: return Types.REAL;	//7
//		case TIME		: return Types.TIME;	//92
//		case TIMESTAMP	: return Types.TIMESTAMP;//93                   !!!!!Attention timestamp will only go up to 2038, but
//																		//Types.DATETIME does not appear to be supported
//																		//ref http://www.tizag.com/mysqlTutorial/mysql-date.php
//																		//appears to work ok with access. perhaps datetime conversion is automatic if required
//																		//should be tested with mySQL database.
//		default			: return Types.CHAR;	//1,4,6,8,16,92
//		}
//	}


	
	public static ControlDescription makeCd(FormDescription formDescription, ItemInformation itemInformation){
		ControlTypes ctrlType = itemInformation.getControlType();
		switch(ctrlType){
			case CHAR : 	return new CdChar(formDescription, itemInformation);
			case CHECKBOX : return new CdCheckbox(formDescription, itemInformation);
			case COUNTER : 	return new CdCounter(formDescription, itemInformation);
			case DOUBLE : 	return new CdDouble(formDescription, itemInformation);
	//		GPSTIMESTAMP,
			case INTEGER : 	return new CdInteger(formDescription, itemInformation);
			case LOOKUP : 	return new CdLookup(formDescription, itemInformation);
			case LATLONG : 	return new CdLatLong(formDescription, itemInformation);
//			case LATLONGTIME:return new CdLatLongTime(formDescription);
			case NMEACHAR : return new CdNMEAChar(formDescription, itemInformation);
			case NMEAFLOAT : return new CdNMEAFloat(formDescription, itemInformation);
			case NMEAINT : 	return new CdNMEAInt(formDescription, itemInformation);
			case SHORT : 	return new CdShort(formDescription, itemInformation);
			case SINGLE : 	return new CdSingle(formDescription, itemInformation);
			case TIME : 	return new CdTime(formDescription, itemInformation);
			case TIMESTAMP : return new CdTimestamp(formDescription, itemInformation);
	//		ANALOGUE,
	//		DIGITAL,
			case HSPACE : 	return new CdxHSpace(formDescription, itemInformation);
			case NEWLINE : 	return new CdxNewline(formDescription, itemInformation);
			case STATIC : 	return new CdxStatic(formDescription, itemInformation);
			case VSPACE : 	return new CdxVSpace(formDescription, itemInformation);
			case SUBFORM: return new CdSubForm(formDescription, itemInformation);
		}
		return null;
	}
	
//	abstract Class getDataClasses();
	
//	
//	public boolean isInput(){
////		System.out.println(getType());
//		if (/**/eType==ControlTypes.HSPACE||
//				eType==ControlTypes.STATIC||
//				eType==ControlTypes.VSPACE||
//				eType==ControlTypes.NEWLINE){
//			return false;
//		}
//		return true;
//	}
	
	public abstract LoggerControl makeControl(LoggerForm loggerForm);
	
	
	
//	/**can maybe be moved to the controls themselves with Overrides of a getControl function on Logger Control which would be passed the LoggerForm and ControlDescription
//	 * 
//	 * @param loggerForm
//	 * @return
//	 */
//	public  LoggerControl makeControl(LoggerForm loggerForm){
//		
////		System.out.println(getOrder()+"|"+getType()+"|"+getTitle());
//		
//		if (loggerForm.getNewOrEdit()==LoggerForm.NewDataForm){
//			switch (eType) {
//				case CHAR		: setDataClass = String.class;		return new CharControl(this,loggerForm);
//				case CHECKBOX	: setDataClass = Boolean.class;		return new CheckboxControl(this,loggerForm);
//				case COUNTER	: setDataClass = String.class;		return new CounterControl(this,loggerForm);
//				case DOUBLE		: setDataClass = Double.class;		return new DoubleControl(this,loggerForm);
//				case INTEGER	: setDataClass = Integer.class;		return new IntegerControl(this,loggerForm);
//				case GPSSTAMP	: setDataClass = LatLong.class;		return new LatLongControl(this,loggerForm);
//				case LOOKUP		: setDataClass = String.class;		return new LookupControl(this,loggerForm);
//				case NMEACHAR	: setDataClass = String.class;		return new NMEACharControl(this, loggerForm);
//				case NMEAFLOAT	: setDataClass = String.class;		return new NMEAFloatControl(this, loggerForm);
//				case NMEAINT	: setDataClass = String.class;		return new NMEAIntegerControl(this, loggerForm);
//				case SHORT		: setDataClass = String.class;		return new IntegerControl(this,loggerForm);
//				case SINGLE		: setDataClass = String.class;		return new FloatControl(this,loggerForm);
//				case TIME		: setDataClass = Long.class;		return new TimeControl(this,loggerForm);
//				case TIMESTAMP	: setDataClass = Long.class;		return new TimestampControl(this, loggerForm);
//				
//				default			: System.out.println(getOrder()+getType()+getTitle()+" not correct"); return null;
//			}
//		
//		
//		}else if (loggerForm.getNewOrEdit()==LoggerForm.EditDataForm){
//			switch (eType) {
//				case CHAR		: setDataClass = String.class;		return new CharControl(this,loggerForm);
//				case CHECKBOX	: setDataClass = Boolean.class;		return new CheckboxControl(this,loggerForm);
//				case COUNTER	: setDataClass = String.class;		//return new CharControl(this, loggerForm);
//																	return new CounterControl(this,loggerForm){
//					
//					/* overridden so counter is not set and update not called to "waste" counter numbers
//					 * also in this case a formsdataunit will be held so can get counter from it
//					 * (non-Javadoc)
//					 * @see loggerForms.controls.CounterControl#updateCounter()
//					 */
//					@Override
//					public void updateCounter() {
////						int index = loggerForm.getFormDescription().getInputControlDescriptions().indexOf(this);
////						
////						Object[] formsData = loggerForm.getFormsDataUnit().getFormData();
////						Object counterData = formsData[index];
////						System.out.println(counterData);
////						setData(counterData);
//					}
//				};
//				case DOUBLE		: setDataClass = Double.class;		return new DoubleControl(this,loggerForm);
//				case INTEGER	: setDataClass = Integer.class;		return new IntegerControl(this,loggerForm);
//				case GPSSTAMP	: setDataClass = LatLong.class;		return new LatLongControl(this,loggerForm);
//				case LOOKUP		: setDataClass = String.class;		return new LookupControl(this,loggerForm);
//				case NMEACHAR	: setDataClass = String.class;		return new NMEACharControl(this, loggerForm);
//				case NMEAFLOAT	: setDataClass = String.class;		return new NMEAFloatControl(this, loggerForm);
//				case NMEAINT	: setDataClass = String.class;		return new NMEAIntegerControl(this, loggerForm);
//				case SHORT		: setDataClass = String.class;		return new IntegerControl(this,loggerForm);
//				case SINGLE		: setDataClass = String.class;		return new FloatControl(this,loggerForm);
//				case TIME		: setDataClass = Long.class;		return new TimeControl(this,loggerForm){
//					@Override
//					public int autoUpdate() {
//						//Can't because not 
//						return AUTO_UPDATE_CANT;
//					}
//				};
//				case TIMESTAMP	: setDataClass = Long.class;		return new TimestampControl(this, loggerForm){
//					@Override
//					public int autoUpdate() {
//						//Can't because not 
//						return AUTO_UPDATE_CANT;
//					}
//				};
//				
//				default			: System.out.println(getOrder()+getType()+getTitle()+" not correct"); return null;
//			}
//		}
//		System.out.printf("Form %s is neither of type New nor Edit! \n", loggerForm.getFormDescription().getFormNiceName());//shouldn't happen
//		return null;
//		
//	}
	

//	/**
//	 * 
//	 * @return class used to set data in the setData method of the control
//	 */
//	Class getSetDataClass(){
//		return setDataClass;
//	}
	
	/**
	 * can now be incorporated in make control if control descriptions hold Class/data arrays of 0 size
	 * @param loggerForm
	 * @return
	 */
	public JPanel makeComponent(LoggerForm loggerForm){
		return null;
	}
	
//	public JPanel makeComponent(LoggerForm loggerForm){
////		System.out.println(getOrder()+"|"+getType()+"|"+getTitle()+"|"+getLength());
//		JPanel tmpPanel=new LoggerFormPanel(loggerForm);
//		switch (eType) {
//			case HSPACE		: tmpPanel.add(Box.createRigidArea(new Dimension(this.getLength()*9,0)));
//			break;
////			case NEWLINE	: //this wont be called as NEWLINE is dealt with in LoggerForm.
//			case STATIC		: tmpPanel.add(new LoggerFormLabel(loggerForm, getTitle()));
//			break;
//			case VSPACE		: tmpPanel.add(new JSeparator(SwingConstants.VERTICAL));
//			break;
//		}
//		return tmpPanel;
//	}
	
	
	public String getHint(){
		String hint = super.getHint();
		if (hint == null) return null;
		return hint.trim();
	}
	
	/**
	 * Move data into the database table items
	 * @param data object of data - must be of a suitable type for this control
	 */
	public void moveDataToTableItems(Object data) {
		formsTableItems[0].setValue(data);
	}
	
	/**
	 * Get data from a database table item. 
	 * @return data object - will be in a type suitable for that control. 
	 */
	public Object moveDataFromTableItems() {
		return formsTableItems[0].getValue();
	}

	/**
	 * Create an XML element containging the control information
	 * and data for network communication. <br> These need
	 * a fixed format so this is final, however, you can override
	 * fillXMLDataElement for controls which have > 1 data value (e.g. a LatLong)
	 * @param doc Parent XML document
	 * @return XML element
	 */
	public final Element createXMLDataElement(Document doc, FormsDataUnit formsDataUnit, Object data) {
		Element el = doc.createElement("ControlData");
		el.setAttribute("Name", this.getTitle());
		el.setAttribute("Type", this.eType.toString());
		fillXMLDataElement(doc, el, data);
		return el;
	}

	/**
	 * Write an xml element with an actual data value. 
	 * @param doc
	 * @param el
	 * @param data
	 */
	public void fillXMLDataElement(Document doc, Element el, Object data) {
		el.appendChild(createXMLDataItem(doc, formsTableItems[0].getName(), data));
	}

	/**
	 * Create an xml element with a single data value. 
	 * @param doc
	 * @param name
	 * @param value
	 * @return
	 */
	public final Element createXMLDataItem(Document doc, String name, Object value) {
		Element el = doc.createElement("Data");
		el.setAttribute("Name", name);
		if (value == null) {
			el.setAttribute("Value", "");
		}
		else {
			el.setAttribute("Value", value.toString());
		}
		return el;
	}

	/**
	 * Extract data values from an XML Element. Unlike the function that
	 * writes the elements using toString() this will have to be
	 * more complicated to create the correct type of data. <br>
	 * Controls with non standard data types will have to override this. 
	 * @param el XML Element
	 * @param value Value - quicker and easier for basic controls. Others may 
	 * have to do a more complex extraction from the Element. 
	 * @return Extracted object or null if data were null of there was a mismatch.
	 */
	public Object extractXMLElementData(Element el, String value) {
		return value;
	}






	/**
	 * format the data item, primarily used for the table of data. 
	 * @param data
	 * @return
	 */
	public Object formatDataItem(Object data) {
		if (data == null) {
			return null;
		}
		return data.toString();
	}
//	@Override
//	public String getHint(){
//		if (super.getHint()==null||EmptyTableDefinition.deblankString(super.getHint()).length()==0){
//			switch(eType){
//				case LOOKUP		: return"Use the drop down arrow or press F4 for a list of possible options";
//				case GPSSTAMP	: return"Press F1 to update or right-click to input manually or paste from map";
//				case NMEACHAR	: 
//				case NMEAFLOAT	: 
//				case NMEAINT	: return"Press F1 to update NMEA data";
//				case TIME		: return"<html>Press F1 to update time automatically";
////				+					" Times must be in the format \"hh:mm:ss\"";
//				case TIMESTAMP	: return"<html>Press F1 to update time and date automatically"; 
////				" Dates and times must be in the format \"YYYY-MM-DD hh:mm:ss\"";
//				default:return "";
//			}
//		}
//		return super.getHint();
//	}
	
	

	@Override
	public String getItemWarning() {
		// TODO Auto-generated method stub
		return null;
	}




	
	
	
}
