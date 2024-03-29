package loggerForms;

import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.pamCursor.PamCursor;
import generalDatabase.pamCursor.PamCursorManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import Array.streamerOrigin.GPSOriginMethod;
import Array.streamerOrigin.GPSOriginSystem;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.OriginIterator;
import Array.streamerOrigin.StaticOriginSystem;
import GPS.GpsData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//import com.sun.org.apache.xerces.internal.dom.DocumentImpl;

import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.ScrollPaneAddon;
import PamView.PamTabPanel;
import PamView.panel.PamPanel;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.XMLUtils;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.controlDescriptions.InputControlDescription;
import loggerForms.PropertyTypes;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.formdesign.FormEditDialog;
import loggerForms.formdesign.FormEditor;
import loggerForms.formdesign.FormList;
import loggerForms.propertyInfos.BEARINGinfo;
import loggerForms.propertyInfos.HEADINGinfo;
import loggerForms.propertyInfos.RANGEinfo;
/**
 * 
 * @author Graham Weatherup - SMRU
 * Holds a description of the Form to be created
 */
public class FormDescription implements Cloneable, Comparable<FormDescription> {

	private UDFTableDefinition udfTableDefinition;
	private FormsControl formsControl;
	private DBControlUnit dbControl;
	private boolean udfTableOK;
	private ArrayList<PropertyDescription> propertyDescriptions = new ArrayList<PropertyDescription>();
	private FormList<ControlDescription> controlDescriptions = new FormList<ControlDescription>();
	private ArrayList<InputControlDescription> inputControlDescriptions = new ArrayList<InputControlDescription>();
	private PamTableDefinition outputTableDef;
	private FormPlotOptions formPlotOptions = new FormPlotOptions();
	private char counterSuffix;
	private String strippedName;
	private Long timeOfLastSave;
	private Long timeOfNextSave;
	private FormsDataBlock formsDataBlock;
	private UDFErrors formErrors = new UDFErrors(this);
	private UDFErrors formWarnings = new UDFErrors(this);
	private boolean outputTableOK;

	//	private static enum formTypes{Normal,Hidden,Subtabs,Subform,Popup}
	private boolean needsUDFSave = false;


	/**
	 * Tab number for this forms items on the main logger tab
	 */
	private int tabNumber = -1;
	private JComponent tabComponent;
	private FormsDataDisplayTable formsDataDisplayTable;
	private LoggerSubTabbedPane subTabPane;
	private LoggerForm hiddenForm;
	private ArrayList<LoggerForm> subtabForms;
	private PamCursor outputCursor;

	// indexes of special controls used in plotting and stuff like that on the map
	//	int bearingControlIndex = -1;
	//	int rangeControlIndex = -1;
	/*
	 * Type of bearing - TURE, MAGNETIC,RELATIVE1, RELATIVE2
	 */
	private BearingTypes bearingType;
	private RangeUnitTypes rangeType;
	private Integer fixedRange;
	private Integer headingLength;
	private BearingTypes headingType;
	private RangeUnitTypes headingRangeUnit;
	private LoggerForm normalForm;

	private FormSettingsControl formSettingsControl;
	private JSplitPane splitPane;
	private BEARINGinfo bearingInfo;
	private RANGEinfo rangeInfo;
	private HEADINGinfo headingInfo;
	private String udfName;


	public static final int LOGGER_FORMS_JSON = 1;
	public static final int LOGGER_FORMS_COMMA = 2;
	public static final int LOGGER_FORMS_XML = 3;

	private static Font defaultFont = (new JLabel()).getFont();

	/**
	 * Constructor for building a demo form during the design phase. 
	 * @param existingDescription
	 * @param itemInfos
	 */
	public FormDescription(FormDescription existingDescription, ArrayList<ItemInformation> itemInfos) {
		this.formsControl = existingDescription.formsControl;
		this.dbControl = existingDescription.dbControl;		
		this.udfName = existingDescription.udfName;
		udfTableDefinition = new UDFTableDefinition(existingDescription.udfTableDefinition.getTableName());


		createControlDescriptions(itemInfos);

		//		setTimeOfNextSave();

		findSpecialControls();
	}

	/**
	 * Main constructor used when reading data from a UDF table
	 * @param formsControl
	 * @param udfName
	 */
	public FormDescription(FormsControl formsControl, String udfName){

		this.formsControl = formsControl;
		this.udfName = udfName;
		dbControl = DBControlUnit.findDatabaseControl();
		formSettingsControl = new FormSettingsControl(this, udfName);

		udfTableDefinition = new UDFTableDefinition(udfName);

		udfTableOK = dbControl.getDbProcess().checkTable( udfTableDefinition);

		strippedName = udfName.substring(4);

		ArrayList<ItemInformation> itemInfos = readUDFTable();

		buildForm(itemInfos);
	}

	private void reBuildForm(ArrayList<ItemInformation> itemInfos) {


		/*
		 * first clear out existing build information
		 */
		formErrors.clear();
		formWarnings.clear();
		propertyDescriptions.clear();
		controlDescriptions.clear();
		inputControlDescriptions.clear();

		//		formPlotOptions
		/*
		 * if there is a form on a tabcontrol, remove it but leave the tab 
		 * in place. 
		 * If it's a subform anywaym then it probably doens't matter. 
		 */

		buildForm(itemInfos);

		//		formsControl.getTab
	}

	/**
	 * This stuff has been broken off from the original constructor
	 * so that it can be used with a new list of item infos during 
	 * Interactive form design. 
	 * @param itemInfos list either read from the UDF or from the design system. 
	 */
	private void buildForm(ArrayList<ItemInformation> itemInfos) {
		createControlDescriptions(itemInfos);

		createOutputTableDef();

		//		tableComponant=new FormsDataDisplayTable(this);

		/*
		 * report UDF errors
		 */
		PamTabPanel tabPanel = formsControl.getTabPanel();
		Component c = null;
		if (tabPanel != null) {
			c = tabPanel.getPanel();
		}
		if (formErrors.popupAll(c)){

		}

		outputCursor=PamCursorManager.createCursor(outputTableDef);

		outputTableOK = dbControl.getDbProcess().checkTable(outputTableDef);


		formsDataBlock = new FormsDataBlock(this, getFormName(), formsControl.getFormsProcess(), 0);
		formsDataBlock.SetLogging(new FormsLogging(this,formsDataBlock));
		formsDataBlock.setOverlayDraw(new LoggerFormGraphics(formsControl, this));
		formsDataBlock.setPamSymbolManager(new StandardSymbolManager(formsDataBlock, LoggerFormGraphics.defaultSymbol, false));


		setTimeOfNextSave();

		findSpecialControls();

		PamSettingManager.getInstance().registerSettings(new FormPlotOptionsStore());
	}




	/**
	 * @return the formErrors
	 */
	public UDFErrors getFormErrors() {
		return formErrors;
	}




	/**
	 * @param formErrors the formErrors to set
	 */
	public void setFormErrors(UDFErrors formErrors) {
		this.formErrors = formErrors;
	}




	/**
	 * @return the formWarnings
	 */
	public UDFErrors getFormWarnings() {
		return formWarnings;
	}




	/**
	 * @param formWarnings the formWarnings to set
	 */
	public void setFormWarnings(UDFErrors formWarnings) {
		this.formWarnings = formWarnings;
	}




	/**
	 * find special controls for range, bearing and heading information on the map and
	 * also collate a few other things about each of them. 
	 */
	private void findSpecialControls() {
		bearingInfo = findBEARINGInfo();
		rangeInfo = findRANGEInfo();
		headingInfo = findHEADINGInfo();		
	}


	/**
	 * Reads information from the UDF table into a list of ItemInformation objects. 
	 * These are later turned into a list of control descriptions, but not now !
	 */
	private ArrayList<ItemInformation> readUDFTable() {
		ArrayList<ItemInformation> itemInfos = new ArrayList<>();
		PamCursor udfCursor = PamCursorManager.createCursor(udfTableDefinition);
		try {
			udfCursor.openScrollableCursor(dbControl.getConnection(), 
					true, true, "ORDER By "+dbControl.getDatabaseSystem().getSqlTypes().formatColumnName("Order"));

			//			udfCursor.openReadOnlyCursor(dbControl.getConnection(), "WHERE Id > 0");
			udfCursor.beforeFirst();

			while (udfCursor.next()) {

				udfCursor.moveDataToTableDef(true);

				ItemInformation itemInfo = new ItemInformation(this);
				itemInfo.readTableDefRecord();
				itemInfos.add(itemInfo);
			}



		} catch (SQLException e) {
			System.out.println("UDF_"+strippedName+" table not read properly");
			e.printStackTrace();
		}

		udfCursor.close();

		return itemInfos;
	}

	public boolean writeCompleteUDFTable() {
		FormEditor formEditor = new FormEditor(formsControl, null, this);
		formEditor.populateControlTitles();
		ArrayList<ItemInformation> newFormItems = formEditor.getAllFormInformations();
		formEditor.clearControlTitles();
		for (ItemInformation itemInfo:newFormItems) {
			itemInfo.setProperty(UDColName.Id.toString(), 0);
		}
		return writeUDFTable(newFormItems);
	}

	/**
	 * Write a totally new set of form data from the from design dialog back down 
	 * into the UDF table. To do this, the UDF table must be cleared first. 
	 * @param newFormItems array list of new form properties and controls. 
	 * @return true if the operation was successful. 
	 */
	public boolean writeUDFTable(ArrayList<ItemInformation> newFormItems) {
		if (newFormItems == null) {
			FormEditor formEditor = new FormEditor(formsControl, null, this);
			newFormItems = formEditor.getAllFormInformations();
		}


		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return false;
		}
		/**
		 * Table rewriting process needs to happen in three distinct stages
		 * 1. Update existing records, 
		 * 2. Delete remove records
		 * 3. Add new records. 
		 * All needs working out by seeing which newFormItems Id fields match with
		 * Ids in the database table. 
		 */
		PamCursor udfCursor = PamCursorManager.createCursor(udfTableDefinition);
		udfCursor.openScrollableCursor(con, true, true, "");
		// go through cursor and for each item, match back to the items 
		// and either update or delete
		while (udfCursor.next()) {
			int iD = udfCursor.getInt(1);
			ItemInformation existingInfo = findItemById(newFormItems, iD);
			if (existingInfo != null) {
				// move the data from the item info to the cursor. 
				existingInfo.writeTableDefRecord();
				try {
					udfCursor.moveDataToCursor(true);
					udfCursor.updateRow();
				} catch (SQLException e) {
					System.err.println(String.format("Error writing for %d for Logger form %s", iD, getUdfName()));
					System.err.println(e.getMessage());
				}
			}
			else {
				udfCursor.deleteRow();
			}
		}	
		udfCursor.updateDatabase();	
		/*
		 * Now write all the new data, forcing the 
		 */
		udfCursor.moveToInsertRow();
		for (ItemInformation itemInfo:newFormItems) {
			Integer Id = itemInfo.getIntegerProperty(UDColName.Id.toString());
			if (Id != null && Id > 0) {
				continue; // record will have already been written. 
			}
			itemInfo.writeTableDefRecord();
			udfCursor.immediateInsert(con);
		}

		udfCursor.close();

		/*
		 * Commit changes to database since it's likely that user will 
		 * want to look into the db at this point to see what's going on 
		 * and will get very confused if changes are not committed
		 */
		if (DBControlUnit.findDatabaseControl().commitChanges() == false) {
			System.out.println("Error Commiting logger form form changes to database");
		}

		//		// old way which deleted and rewrote everything. 
		//		/** 
		//		 * Start by deleting everything....
		//		 */
		//		PamCursor udfCursor = PamCursorManager.createCursor(udfTableDefinition);
		//		udfCursor.openScrollableCursor(con, true, true, "");
		////		udfCursor.beforeFirst();
		//		while (udfCursor.next()) {
		//			udfCursor.deleteRow();
		//		}
		//		udfCursor.updateDatabase();
		//		/*
		//		 * Now write all the new data, forcing the 
		//		 */
		//			for (ItemInformation itemInfo:newFormItems) {
		//				itemInfo.writeTableDefRecord();
		//				udfCursor.immediateInsert(con);
		//			}
		//
		//		udfCursor.close();

		return true;
	}

	/**
	 * Find an item information based on it's Id. 
	 * Function used when rewriting the database table. 
	 * @param itemInfos list of item informations
	 * @param iD Id searched for
	 * @return ItemInformation matching that Id, or null
	 */
	private ItemInformation findItemById(ArrayList<ItemInformation> itemInfos, int iD) {
		for (ItemInformation anItem:itemInfos) {
			Integer thisId = anItem.getIntegerProperty(UDColName.Id.toString());
			if (thisId != null && thisId == iD) {
				return anItem;
			}
		}
		return null;
	}

	private void createControlDescriptions(ArrayList<ItemInformation> itemInformations) {
		for (ItemInformation itemInformation:itemInformations) {

			String	type = itemInformation.getStringProperty(UDColName.Type.toString());

			/* decide what type, control or property.
			 * new item description(udfTableDefinition)/.
			 */
			if (PropertyDescription.isProperty(type)){
				propertyDescriptions.add( new PropertyDescription(this, itemInformation));
			}
			else if (itemInformation.getControlType() != null){
				ControlDescription ctrlDesc = ControlDescription.makeCd(this,itemInformation);
				if (ctrlDesc == null) {
					continue;
				}
				controlDescriptions.add(ctrlDesc);
				//				controlDescriptions.add(ctrlDesc = Cd.makeCd(this, type));
				if (ctrlDesc instanceof InputControlDescription){
					String dbTitle = ctrlDesc.getDbTitle();

					if (dbTitle == null) {
						formErrors.add(String.format("Table item ID %d, ORDER %d does not have a valid \"Title\" or \"Dbtitle\"",
								ctrlDesc.getId(), ctrlDesc.getOrder()));
						ctrlDesc.addItemError();
						continue;
					}

					for (String rT:FormsControl.restrictedTitles){
						if (dbTitle.equalsIgnoreCase(rT)){
							formErrors.add(String.format("%s has a resticted DbTitle of \"%s\"",ctrlDesc.getTitle(),ctrlDesc.getDbTitle()));
						}
					}

					for (ControlDescription cd:inputControlDescriptions){
						if (dbTitle==cd.getDbTitle()){
							formErrors.add(String.format("%s at %s has the same DbTitle as %s at %s",ctrlDesc.getTitle(),ctrlDesc.getId(),cd.getTitle(),cd.getId()));
						}

					}


					inputControlDescriptions.add((InputControlDescription) ctrlDesc);
				}
			}
			else {
				formErrors.add(type+" in "+getUdfName()+" is not a recognised control or property type");
			}

			//			udfCursor.
			//			if (controlDescriptions.size() > 20) break;

		}
	}

	/**
	 * @return the propertyDescriptions
	 */
	public ArrayList<PropertyDescription> getPropertyDescriptions() {
		return propertyDescriptions;
	}




	@Override
	public String toString(){
		return getFormNiceName();
	}

	private void createOutputTableDef(){
		outputTableDef = new PamTableDefinition(getDBTABLENAME(), SQLLogging.UPDATE_POLICY_OVERWRITE);

		for (ControlDescription ctrlDesc:inputControlDescriptions){

			for (int i=0;i<ctrlDesc.getFormsTableItems().length;i++){
				//this index can be used in creating index lookups
				int index = outputTableDef.addTableItem(ctrlDesc.getFormsTableItems()[i]);
			}

		}
	}



	/**
	 * @return the controlDescriptions
	 */
	public FormList<ControlDescription> getControlDescriptions() {
		return controlDescriptions;
	}


	public PropertyDescription findProperty(PropertyTypes propertyType) {
		for(PropertyDescription p:propertyDescriptions){
			if (p.getPropertyType() == propertyType) {
				return p;
			}
		}
		return null;
	}

	/**
	 * Find an input control by name and return it's index
	 * @param name Input control name
	 * @return index in table, or -1 if nothing found. 
	 */
	public int findInputControlByName(String name) {
		if (name == null) {
			return -1;
		}
		for (int i = 0; i < inputControlDescriptions.size(); i++) {
			if (name.equals(inputControlDescriptions.get(i).getTitle())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the index of the control in the list of input controls, this will 
	 * help find it's data. 
	 * @param controlDescription
	 * @return control index, or -1 if not found. 
	 */
	public int getControlIndex(ControlDescription controlDescription) {
		for (int i = 0; i < inputControlDescriptions.size(); i++) {
			if (inputControlDescriptions.get(i) == controlDescription) {
				return i;
			}
		}
		return -1;
	}


	public String getFormName() {
		return strippedName;
	}

	String getFormNiceName(){
		return EmptyTableDefinition.reblankString(strippedName);
	}

	String getFormTabName() {
		if (isSubTabs()) {
			int nSubTabs = 0;
			if (subTabPane != null) {
				nSubTabs = subTabPane.getTabCount();
			}
			return String.format("%s (%d)", getFormNiceName(), nSubTabs);
		}
		else {
			return getFormNiceName();
		}
	}

	/**
	 * Function to say whether or not the forms should be displayed as sub-tabs. 
	 * <p> This should replace all direct references to the SUBTABS property 
	 * since in viewer mode, sub tabs are NOT used. 
	 * @return
	 */
	boolean isSubTabs() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			return false;
		}
		return (findProperty(PropertyTypes.SUBTABS) != null);
	}
	/**
	 * 
	 */
	//	private void createOutputTableDef() {
	//		outputTableDef = new EmptyTableDefinition(getDBTABLENAME());
	//		ArrayList<String> outputTableFieldNames = new ArrayList<String>();
	//		for(ControlDescription c:inputControlDescriptions){
	//			String newField = c.getDbTitle();
	//			if (outputTableFieldNames.contains(newField)){
	//				System.out.println("Table Already has this field: "+newField);
	//			}else{
	//				outputTableFieldNames.add(newField);
	//				outputTableDef.addTableItem(new FormsTableItem(c, newField, c.getDataType()));
	//			}
	//		}
	//	}

	public Integer getAUTOALERT(){
		PropertyDescription p = findProperty(PropertyTypes.AUTOALERT);
		if (p == null) {
			//			formWarnings.add("The AUTOALERT Property was not found in "+getUdfName());
			return null;
		}
		Integer alert = p.getAutoUpdate();
		if (alert == null || alert == 0) {
			formErrors.add("The AUTOALERT AutoUpdate field is not filled in correctly, in "+getUdfName());
			return null;
		}

		return alert;

	}

	private int getRelatedDescription(PropertyDescription p){
		int cSelect = -1;
		Boolean titleExists=false;
		int count = 0;
		//Check the specified control exists and select the first one if it does.
		int iCtrol = 0;
		if (p == null || p.getTitle() == null) {
			return -1;
		}
		for (ControlDescription c:inputControlDescriptions){
			if (p.getTitle().equals(c.getTitle())){
				if (!titleExists){
					cSelect=iCtrol;
				}
				titleExists=true;
				count++;
			}
			iCtrol++;
		}
		if (titleExists){
			if (count==0){
				//This should never happen
				formErrors.add("Doesn't really exist");
				return -1;
			}else if (count==1){
				/*
				 * Normal situation no warning necessary
				 */
			}else if (count>1){
				//Two titles exist, Warn User and notify first will be used.
				formErrors.add(count+" \""+p.getTitle()+"\"rows with \""+p.getType()+"\" information exist in "+getUdfName()+". The first one will be used.");
				//FUTURE IMPROVEMENT:Ask User which to use and modify Title of other so is ignored in future
			}
			return cSelect;
		}else{
			formErrors.add("The row \""+p.getTitle()+"\" with \""+p.getType()+"\" information does not exist in "+getUdfName());
			return -1;
		}
	}


	public Font getFONT(){
		Font font = defaultFont;
		PropertyDescription p = findProperty(PropertyTypes.FONT);
		if (!(p==null)){
			Integer fontSize = p.getLength();
			if (fontSize == null || fontSize == 0) {
				fontSize = defaultFont.getSize();
			}
			String fontName = p.getTitle();
			if (fontName == null || fontName.equalsIgnoreCase("default")) {
				fontName = font.getFontName();
			}

			font = new Font(fontName, Font.PLAIN, fontSize);
		}
		if (font == null) {
			font = defaultFont;
		}
		return font;
	}

	public String getDBTABLENAME() {
		PropertyDescription p = findProperty(PropertyTypes.DBTABLENAME);
		if (p == null) {
			return strippedName;
		}
		String name = p.getTitle();
		if (name == null || name.length() == 0) {
			formErrors.add("The DBTABLENAME Title field is not filled in correctly in "+getUdfName());
			return strippedName;
		}
		return name;
	}

	/**
	 * 
	 * @return BEARINGinfo 
	 * 
	 * @containing (
	 * @ControlDescription based on Title field,
	 * @PropertyDescription.bearingTypes based on Topic field,
	 * @boolean (primitive) based on Plot field)
	 * 
	 */
	public BEARINGinfo findBEARINGInfo(){
		ControlDescription relatedControl;
		BearingTypes type;
		boolean plot;

		//		Object[] bearingObject = new Object[3];
		PropertyDescription p = findProperty(PropertyTypes.BEARING);
		if (p == null) {
			//kill if not existant
			return null;
		}
		int ctrolIndex = getRelatedDescription(p);
		if (ctrolIndex < 0){
			return null;
		}else{
			relatedControl=inputControlDescriptions.get(ctrolIndex);
		}

		//Check topic for type of bearing to save set to TRUE if not correct.
		String topic = p.getTopic();
		if (BearingTypes.getValue(topic) == null){
			formErrors.add("The information \""+topic+"\" in topic field of \""+p.getType()+"\" is not correct in "+getUdfName()+". As default it will be saved as TRUE");
			type=BearingTypes.getValue("TRUE");
		}else {
			type=BearingTypes.getValue(topic);
		}

		Boolean plotB = p.getPlot();
		if (plotB==null){
			plot=false;
			formErrors.add("Plot Field is null, BEARING will not be plotted for "+getUdfName());
		}else {
			plot=plotB;
		}

		return new BEARINGinfo( relatedControl, ctrolIndex, type, plot);
	}




	/**
	 * 
	 * @return RANGEinfo
	 * 
	 * @containing (
	 * @ControlDescription relatedControl,
	 * @rangeUnitTypes unitType,
	 * @rangeTypes type,
	 * @int fixedLength)
	 */
	public RANGEinfo findRANGEInfo(){
		/*
		 * check if RANGE exists
		 */	
		PropertyDescription p = findProperty(PropertyTypes.RANGE);
		//No heading Property
		if (p == null) {
			return null;
		}


		/**
		 * get dataInput field(ControlDescription)
		 */	
		int ctrolIndex = getRelatedDescription(p);
		ControlDescription c;
		if (ctrolIndex < 0){
			return null;
		}else{
			c=inputControlDescriptions.get(ctrolIndex);
		}
		/**
		 * get/set-to-default rangeUnit field(enum)
		 */	
		String rangeUnitType = p.getPostTitle();
		RangeUnitTypes unitType;
		if (RangeUnitTypes.getValue(rangeUnitType) == null){
			formErrors.add("The information \""+rangeUnitType+"\" in PostTitle field of \""+p.getType()+"\" is not correct in "+getUdfName()+". As default it will be saved as M");
			unitType=RangeUnitTypes.getValue("M");
		}else {
			unitType=RangeUnitTypes.getValue(rangeUnitType);
		}
		/**
		 * get/set-to-default rangeType field(enum)
		 */	
		String rangeType = p.getTopic();
		RangeTypes type;
		if (RangeTypes.getValue(rangeType) == null){
			formErrors.add("The information \""+rangeType+"\" in PostTitle field of \""+p.getType()+"\" is not correct in "+getUdfName()+". As default it will not be FIXED");
			type=RangeTypes.VARIABLE;
		}else {
			type=RangeTypes.getValue(rangeType);
		}

		/**
		 * get/set-to-default rangeUnit field(enum)
		 */	
		int fixedLength = 1;
		if (type==(RangeTypes.getValue("FIXED"))){
			Integer range = p.getLength();
			if (range==null||range==0){
				formErrors.add("The FIXED range in the Length Field of RANGE in "+getUdfName()+" is null or 0. As default it will be 50m, 1km or 1nmi depending on units specified");
				if ((type)==(RangeTypes.getValue("m"))){
					fixedLength=50;
				}
				fixedLength=1;
			}else{
				fixedLength=range;
			}
		}
		RANGEinfo info=new RANGEinfo(c, ctrolIndex, unitType, type, fixedLength);
		return info;
	}




	/**
	 * 
	 * @return HEADINGinfo
	 * 
	 * 
	 * @containing (
	 * @ControlDescription relatedControl;
	 * @PropertyDescription.headingUnitTypes unitType;
	 * @int arrowLength;
	 * @int arrowHeadSize;
	 * @PropertyDescription.headingTypes type;
	 * @boolean fillHead;
	 * @Color colour;)
	 */
	public HEADINGinfo findHEADINGInfo(){
		RangeUnitTypes unitType;
		int arrowLength;
		int arrowHeadSize;
		HeadingTypes type;
		boolean fillHead;
		Color colour;


		/**
		 * check if HEADING exists
		 */	
		PropertyDescription p = findProperty(PropertyTypes.HEADING);
		//No heading Property
		if (p == null) {
			return null;
		}

		Object[] headingObject = new Object[7];
		/**
		 * get dataInput field(ControlDescription)
		 */	
		ControlDescription relatedControl = null;
		int ctrolIndex = getRelatedDescription(p);
		if (ctrolIndex < 0){
			return null;
		}else{
			relatedControl=inputControlDescriptions.get(ctrolIndex);
		}

		/**
		 * get/set-to-default headingUnit field(enum)
		 */	
		String headingUnitType = p.getPostTitle();
		if (headingUnitType == null){
			formErrors.add("The information \""+headingUnitType+"\" in PostTitle field of \""+p.getType()+"\" is not correct in "+getUdfName()+". As default it will be saved as M");
			unitType=RangeUnitTypes.m;
		}else {
			unitType=RangeUnitTypes.getValue(headingUnitType);
		}
		/**
		 * get/set-to-default arrowLength field(int)
		 */	
		Integer arrowLengthI = p.getLength();
		if (arrowLengthI==null||arrowLengthI==0){
			formErrors.add("The Length of the Heading arrow in \""+p.getType()+"\" has not been entered or is 0. A default value of 10 will be used.");
			arrowLengthI=10;
		}
		arrowLength=arrowLengthI;

		/**
		 * get/set-to-default arrowHeadSize field(int)
		 */	
		Integer arrowHeadSizeI = p.getLength();
		if (arrowHeadSizeI==null||arrowHeadSizeI==0){
			formErrors.add("The Length of the Heading arrow in \""+p.getType()+"\" has not been entered or is 0. A default value of 1 will be used.");
			arrowHeadSizeI=1;
		}
		arrowHeadSize=arrowHeadSizeI;


		/**
		 * get/set-to-default headingType field(enum)
		 */	
		String headingType = p.getTopic();
		//		TODO
		//		this seems to be broken and is getting title instead of topic
		//		Illegal heading type argument: Direction of travel S
		//		The information "Direction of travel S" in Topic field of "HEADING" is not correct in UDF_Sightings. As default it will be saved as TRUE
		//		At least if that topic should check then comment should say topic/title isnt correct			
		if (HeadingTypes.getValue(headingType) == null){
			formErrors.add("The information \""+headingType+"\" in Topic field of \""+p.getType()+"\" is not correct in "+getUdfName()+". As default it will be saved as TRUE");
			type=HeadingTypes.getValue("TRUE");
		}else {
			type=HeadingTypes.getValue(headingType);
		}

		/**
		 * get/set-to-default fillHead field(boolean)
		 */
		Boolean fillHeadB = p.getPlot();
		if (fillHeadB==null){
			fillHeadB=false;
		}
		fillHead=fillHeadB;

		/**
		 * get/set-to-default Colour field(Color)
		 */
		colour=Color.getColor(p.getColour(), Color.black);

		//		return headingObject;

		//		ControlDescription relatedControl,PropertyDescription.headingUnitTypes unitType,int arrowLength,
		//		int arrowHeadSize,PropertyDescription.headingTypes type,boolean fillHead, Color colour

		return new HEADINGinfo(relatedControl,ctrolIndex, unitType,arrowLength,
				arrowHeadSize,type, fillHead,  colour);
	}




	public void getHIDDEN(){
		//		PropertyDescription p = findProperty(propertyTypes.HIDDEN);
		//		
		//		
		//		//No hidden Property
		//		if (!(p == null)) {
		//			return false;
		//		}
		//		
		//		boolean hidden = false;
		//		for (ControlDescription c:controlDescriptions){
		//			if (c.
		//		}
		//		
		//		
		//		return hidden;
		//		
	}

	public String getHOTKEY(){
		/**
		 * check if HOTKEY-property exists
		 */	
		PropertyDescription p = findProperty(PropertyTypes.HOTKEY);
		//No Property
		if (p == null) {
			return null;
		}

		/**
		 * check if HOTKEY-value exists and is valid (F5-F24 for now)
		 */
		String keyName = p.getTitle();
		if (keyName == null || keyName.length() == 0) {
			formErrors.add("The HOTKEY "+keyName+" in Title field is not filled in correctly");
			return null;
		}

		return keyName;
	}

	public String getUdfName() {
		return udfTableDefinition.getTableName();
	}

	/**
	 * Get a component to go into the main tab panel for the Logger forms
	 * If it's a POPUP form, return null since these don't go into the tab panel. 
	 * If it's SUBTABS, then return an empty tab panel, but be prepared to add stuff to it
	 * IF it's a normal form, then return a normal form.
	 * @return
	 */
	public JComponent getTabComponent() {
		boolean isSubTabs = isSubTabs();
		//		boolean isSubForm = (findProperty(PropertyTypes.SUBFORM) != null);
		boolean isPopup = (findProperty(PropertyTypes.POPUP) != null);
		boolean isHidden = (findProperty(PropertyTypes.HIDDEN) != null);
		if (isPopup) {
			return null;
		}
		if (isHidden) {
			if (hiddenForm == null) {
				hiddenForm = createForm();
			}
			return null;
		}
		if (tabComponent == null) {
			JComponent formComponent;
			if (isSubTabs) {
				/*
				 * Create a sub tab panel which will be able to hold multiple forms
				 */
				subTabPane = new LoggerSubTabbedPane(this);
				formComponent = subTabPane.getComponent();
			}
			else { 
				/*
				 * Create a normal form.
				 */
				normalForm = createForm();
				formComponent = normalForm.getComponent();
			}
			formsDataDisplayTable = new FormsDataDisplayTable(this);

			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formComponent, formsDataDisplayTable.getMainPanel());
			if (formSettingsControl.getFormSettings().splitPanelPosition != null) {
				splitPane.setDividerLocation(formSettingsControl.getFormSettings().splitPanelPosition);
			}
			else {
				splitPane.setDividerLocation(400);
			}

			if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
				ScrollPaneAddon sco = new ScrollPaneAddon(formsDataDisplayTable.getScrollPane(), getFormNiceName(),
						AbstractPamScrollerAWT.HORIZONTAL, 1000, 2*24*3600*1000, true);
				sco.addDataBlock(getFormsDataBlock());
				if (normalForm != null) {
					// try to incorporate the scrollers into the bottom of the main form by the save button
					normalForm.getLastRow().add(sco.getButtonPanel());
					tabComponent = splitPane;
				}
				else {
					// otherwise put them near the top. 
					JPanel bPanel = new PamPanel();
					bPanel.setLayout(new BorderLayout());
					JPanel blPanel = new PamPanel();
					blPanel.setLayout(new BorderLayout());
					blPanel.add(BorderLayout.EAST, sco.getButtonPanel());
					bPanel.add(BorderLayout.NORTH, blPanel);
					bPanel.add(BorderLayout.CENTER, splitPane);
					tabComponent = bPanel;
				}
			}
			else {	
				tabComponent = splitPane;
			}

			//			splitPane.setAutoscrolls(true);
			splitPane.setBorder(BorderFactory.createEmptyBorder());
		}
		return tabComponent;
	}


	private int getFormNLines(){

		int newLines=0;
		for (ControlDescription cD:controlDescriptions){
			if(cD.getEType()==ControlTypes.NEWLINE){
				newLines+=1;
			}
		}
		return newLines;
	}

	public LoggerForm createForm() {
		return new LoggerForm(this,LoggerForm.NewDataForm);
	}


	/**
	 * @return the udfTableDefinition
	 */
	public UDFTableDefinition getUdfTableDefinition() {
		return udfTableDefinition;
	}


	/**
	 * @return the formsDataBlock
	 */
	public FormsDataBlock getFormsDataBlock() {
		return formsDataBlock;
	}


	/**
	 * @return the inputControlDescriptions
	 */
	public ArrayList<InputControlDescription> getInputControlDescriptions() {
		return inputControlDescriptions;
	}


	/**
	 * @return the outputTableDef
	 */
	public PamTableDefinition getOutputTableDef() {
		if (outputTableDef==null)createOutputTableDef();
		return outputTableDef;
	}


	/**
	 * Process an event created by a mouse action on the tab associated with this form. 
	 * <p>This will include all button presses and mouse enter / exit. Does not capture mousemove. 
	 * @param evt
	 */
	public void processTabMouseEvent(LoggerTabbedPane loggerTabbedPane, MouseEvent evt) {
		switch (evt.getID()) {
		//		case MouseEvent.MOUSE_ENTERED:
		////			setTabToolTip(loggerTabbedPane);
		//			break;
		case MouseEvent.MOUSE_CLICKED:
			if (evt.getClickCount() == 2 && isSubTabs()) {
				createSubtabForm();
			}
		}

		if (evt.isPopupTrigger()) {
			formPopupMenu(evt);
		}

	}

	/**
	 * Run some code when a hotkey is pressed.
	 * For a normal form this should switch the focus to the form.
	 * For a subtab form, this should create a new subtab and switch focus to it
	 * For a popup, this should create a new popup -- though not sure that 
	 *    popups are actually implemented...
	 */
	public void processHotkeyPress() {
		if (isSubTabs()) { // This works
			createSubtabForm(); 
		}
			
	}
	
	public void formPopupMenu(MouseEvent evt) {
		JPopupMenu popMenu = new JPopupMenu(this.getFormName());
		JMenuItem menuItem;
		menuItem = new JMenuItem("Edit " + this.getFormName() + " form");
		menuItem.addActionListener(new EditForm(formsControl.getGuiFrame()));
		popMenu.add(menuItem);
		menuItem = new JMenuItem("Remove " + this.getFormName() + " form");
		menuItem.addActionListener(new RemoveForm(formsControl.getGuiFrame()));
		popMenu.add(menuItem);
		popMenu.show(evt.getComponent(), evt.getX(), evt.getY());
	}

	private class EditForm implements ActionListener {
		Window parentFrame;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			editForm(parentFrame);
		}
		public EditForm(Window parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}
	}

	private class RemoveForm implements ActionListener {
		Window parentFrame;
		public RemoveForm(Window parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			removeForm(parentFrame);
		}
	}
	/**
	 * Create a new subtab form on the appropriate sub tab panel. 
	 */
	private void createSubtabForm() {
		LoggerForm newForm = createForm();
		String subtabName = getFormNiceName();

		if (newForm.hasCounter()){
			subtabName+=" ("+newForm.getCounter().getData().toString()+")";
		}

		subTabPane.addTab(subtabName, newForm.getComponent());

		subTabPane.setSelectedComponent(newForm.getComponent());
		formsControl.getFormsTabPanel().setTabName(this);
		if (subtabForms == null) {
			subtabForms = new ArrayList<LoggerForm>();
		}
		subtabForms.add(newForm);
	}

	/**
	 * Edit the form. Will eventually pop up 
	 * a large dialog with a whole load of options for adding
	 * and altering controls. 
	 */
	public void editForm(Window parentFrame) {
		FormEditor formEditor = new FormEditor(formsControl, parentFrame, this);
		ArrayList<ItemInformation> newFormItems = FormEditDialog.showDialog(formEditor);
		if (newFormItems == null) {
			return;
		}
		// if it get's here, rebuild the form. 
		writeUDFTable(newFormItems);
		setNeedsUDFSave(false); // this one is now done, so don't redo !
		/*
		 *  and rewrite all the other forms which may also have been altered
		 *  during the writing of this form. e.g. if ordering was changed
		 */
		formsControl.rewriteChangedUDFTables();

		/*
		 * for now, just do a total rebuild of all forms. 
		 */
		formsControl.regenerateForms(parentFrame);
		//		this.reBuildForm(newFormItems);
	}

	/**
	 * Remove the form. Will involve dropping the UDF table from the 
	 * database, which is a pretty serious thing to want to do ! Warn
	 * first and suggest hiding or just renaming the udf. 
	 */
	public void removeForm(Window parentFrame) {
		// TODO Auto-generated method stub

	}




	/**
	 *
	 * removes the Subtabform that the loggerForm calling it is on
	 */
	void removeSubtabform(LoggerForm loggerForm){

		//removes the Subtabform that is currently selected(it will be the one this command is called from)

		//		subTabs.removeTabAt(subTabs.getSelectedIndex());
		subTabPane.removeTabAt(subTabPane.indexOfComponent(loggerForm.getComponent()));
		subtabForms.remove(loggerForm);
		formsControl.getFormsTabPanel().setTabName(this);
	}

	/**
	 * 
	 * @param loggerForm
	 * @return 
	 * @return the subtab which the specified logger form is on
	 */
	public Component getSubtab(LoggerForm loggerForm){

		//		Component tab = subTabPane.getTabComponentAt(subTabPane.indexOfComponent(loggerForm.getComponent()));

		return subTabPane.getTabComponentAt(subTabPane.indexOfComponent(loggerForm.getComponent()));
	}



	/**
	 * Set an appropriate tool tip for the tab panel. 
	 * @param loggerTabbedPane
	 */
	public String getTabToolTip() {
		String tipText = null;
		if (isSubTabs()) {
			tipText = String.format("Double click to create a new %s form", getFormNiceName());
		}
		else {
			tipText = String.format("Click to view %s form", getFormNiceName());			
		}
		return tipText;
	}

	/**
	 * 
	 * @return true if the form data can be plotted on the PAMGUARD map. 
	 * May adapt this later on so it can draw on other projections as well. 
	 */
	public boolean canDrawOnMap() {
		if (findProperty(PropertyTypes.PLOT) != null) {
			return true;
		}
		for (ControlDescription aControl:controlDescriptions) {
			if (aControl.getPlot() != null && aControl.getPlot()) {
				return true;
			}
		}
		return false;
	}


	/**
	 * @param formPlotOptions the formPlotOptions to set
	 */
	public void setFormPlotOptions(FormPlotOptions formPlotOptions) {
		this.formPlotOptions = formPlotOptions;
	}


	/**
	 * @return the formPlotOptions
	 */
	public FormPlotOptions getFormPlotOptions() {
		return formPlotOptions;
	}


	/**
	 * @return the timeOfNextSave
	 */
	public long getTimeOfNextSave() {
		return timeOfNextSave;
	}


	/**
	 * 
	 */
	public void setTimeOfNextSave() {
		if (getAUTOALERT()==null){
			timeOfNextSave= null;
			return;
		}


		/*
		 * find the last save time of the form
		 */
		Long timeOfLastSave;
		if (formsDataBlock.getUnitsCount()>0){
			timeOfLastSave=formsDataBlock.getLastUnit().getTimeMilliseconds();
		}else{
			timeOfLastSave=getLastSaveTimeFromDB();

		}


		/*
		 * set time of next save based on time of last save if one exists if not sets to now
		 */
		if (timeOfLastSave==null){
			timeOfNextSave=PamCalendar.getTimeInMillis();
		}else{
			timeOfNextSave=timeOfLastSave+getAUTOALERT()*60*1000;
		}


		//		this.secondsUntilNextSave = secondsUntilNextSave;
	}


	//	private Long getLastSaveTime() {
	//		
	//	}


	private Long getLastSaveTimeFromDB() {
		try {

			long timestampNewest = 0;
			outputCursor.openScrollableCursor(dbControl.getConnection(), 
					true, true, "ORDER By \"UTC\" DESC");
			outputCursor.beforeFirst();
			/*
			 * if a record exists
			 */
			SQLTypes sqlTypes = DBControlUnit.findConnection().getSqlTypes();
			if (outputCursor.next()){

				//			while (outputCursor.next()) {

				outputCursor.moveDataToTableDef(true);


				Object timeObject = outputTableDef.getTimeStampItem().getValue();
				Long timestamp = sqlTypes.millisFromTimeStamp(timeObject);
				if (timestamp != null && timestamp > timestampNewest) {
					timestampNewest=timestamp;
				}

				//				timestamp.toString();

				//			}
				return timestampNewest;
			}else{
				//				System.out.println("No last saved data for "+getFormName()+" was found in "+outputTableDef.getTableName());
				return null;
			}



		} catch (SQLException e) {
			System.out.println("No table "+outputTableDef.getTableName()+" was found in the database");
			e.printStackTrace();
			return null;
		}

	}


	/**
	 * @return the formsControl
	 */
	public FormsControl getFormsControl() {
		return formsControl;
	}



	//	/**
	//	 * @return the counter and increment it
	//	 */
	//	public int getCounter() {
	//		
	//		
	//		
	//		
	//		return counter++;
	//	}


	/**
	 * @return the counterSuffix
	 */
	public char getCounterSuffix() {
		return counterSuffix;
	}

	/**
	 * @return the bearingControlIndex or -1 if none found
	 */
	//	public int getBearingControlIndex() {
	//		return bearingControlIndex;
	//	}
	//
	//	/**
	//	 * @return the rangeControlIndex or -1 if none found
	//	 */
	//	public int getRangeControlIndex() {
	//		return rangeControlIndex;
	//	}
	//
	//	/**
	//	 * @return the headingControlIndex or -1 if none found
	//	 */
	//	public int getHeadingControlIndex() {
	//		return headingControlIndex;
	//	}

	/**
	 * @return the bearingType
	 * <p>This can be one of RELATIVE1, RELATIVE2, TRUE, MAGNETIC;
	 */
	public BearingTypes getBearingType() {
		return bearingType;
	}

	/**
	 * @return the rangeType
	 * <p>This can be one of nmi, km, m, FIXED;
	 */
	public RangeUnitTypes getRangeType() {
		return rangeType;
	}

	/**
	 * @return the range value to be used when getRangeType returns FIXED
	 */
	public Integer getFixedRange() {
		return fixedRange;
	}

	/**
	 * @return the length of the heading arrow in units given in getHEadingRangeUnit()
	 * 
	 */
	public Integer getHeadingLength() {
		return headingLength;
	}

	/**
	 * @return the headingType
	 * This can be one of RELATIVE1, RELATIVE2, TRUE, MAGNETIC;
	 */
	public BearingTypes getHeadingType() {
		return headingType;
	}

	/**
	 * @return the headingRangeUnit
	 * <p>This can be one of nmi, km, m, pix;
	 */
	public RangeUnitTypes getHeadingRangeUnit() {
		return headingRangeUnit;
	}

	class FormPlotOptionsStore implements PamSettings {

		@Override
		public Serializable getSettingsReference() {
			return formPlotOptions;
		}

		@Override
		public long getSettingsVersion() {
			return FormPlotOptions.serialVersionUID;
		}

		@Override
		public String getUnitName() {
			return getFormName();
		}

		@Override
		public String getUnitType() {
			return "UDF Plot Options";
		}

		@Override
		public boolean restoreSettings(
				PamControlledUnitSettings pamControlledUnitSettings) {
			formPlotOptions = ((FormPlotOptions) pamControlledUnitSettings.getSettings()).clone();
			return formPlotOptions != null;
		}

	}

	/**
	 * Destroy any open forms. This includes disconnecting any observer
	 * of NMEA data. 
	 */
	public void destroyForms() {
		if (normalForm != null) {
			normalForm.destroyForm();
		}
		if (hiddenForm != null) {
			if (normalForm != null) {
				normalForm.destroyForm();
			}
		}
		if (subtabForms != null) {
			for (LoggerForm aForm:subtabForms) {
				aForm.destroyForm();
			}
		}
	}


	/**
	 * Called when data are added to or removed from the 
	 * datablock. 
	 * <p> goes on to notify the history table that things have changed. 
	 */
	public void dataBlockChanged() {
		if (formsDataDisplayTable != null) {
			formsDataDisplayTable.dataChanged();
		}
	}


	/**
	 * Called from FormSettingsControl just before PAMGuard exits (or settings
	 * are saved for some other reason).
	 * <p> Populate appropriate data into the formSettings as provided. 
	 * @param formSettings
	 */
	public void getFormSettingsData(FormSettings formSettings) {
		if (splitPane != null) {
			formSettings.splitPanelPosition = splitPane.getDividerLocation();
		}
	}


	/**
	 * Called only in viewer mode when the selection of a row in the summary table 
	 * changes. The contents of the data unit will be displayed in the form (which 
	 * cannot be edited !), or the form will be cleared if the data unit is null
	 * @param formsDataUnit Data unit to display. 
	 */
	public void viewDataUnit(FormsDataUnit formsDataUnit) {
		if (normalForm != null) {
			normalForm.restoreData(formsDataUnit);
		}
	}

	/**
	 * Get a count of open sub tab forms. 
	 * @return the number of open sub tab forms. 
	 */
	public int getSubformCount() {
		if (subtabForms == null) {
			return 0;
		}
		else {
			return subtabForms.size();
		}
	}




	public BEARINGinfo getBearingInfo() {
		return bearingInfo;
	}




	public RANGEinfo getRangeInfo() {
		return rangeInfo;
	}




	public HEADINGinfo getHeadingInfo() {
		return headingInfo;
	}

	/**
	 * 
	 * @return cloned copies of ItemInformation in all controls. 
	 */
	public ArrayList<ItemInformation> getControlsInformationCopy() {
		ArrayList<ItemInformation> itemInfos = new ArrayList<ItemInformation>();
		for (ControlDescription cd:controlDescriptions) {
			itemInfos.add(cd.getItemInformation().clone());
		}
		return itemInfos;
	}

	public PamDataUnit createDataFromXML(Document doc) {
		int nErrors = 0;
		/*
		 * Get the version number. 
		 */
		Element root = doc.getDocumentElement();
		if (root == null) {
			return null;
		}
		//		root.get
		//		NodeList info = root.getElementsByTagName("INFO");
		//		if (info == null || info.getLength() == 0 ){
		//			return -2;
		//		}
		Integer v = XMLUtils.getIntegerValue(root, "Version");
		String formName = root.getAttribute("FormName");
		String time = root.getAttribute("TimeMillis");
		long timeMillis = 0;
		try {
			timeMillis = Long.valueOf(time);
		}
		catch (NumberFormatException e) {
			System.out.println("Invalid time string in Logger form " + formName);
			return null;
		}

		if (formName.equals(getFormName()) == false) {
			return null;
		}
		Object[] formData = new Object[inputControlDescriptions.size()];
		NodeList info = root.getElementsByTagName("ControlData");
		for (int i = 0; i < info.getLength(); i++) {
			Element el = (Element) info.item(i);
			String ctrlName = el.getAttribute("Name");
			String ctrlType = el.getAttribute("Type");
			/**
			 * And find the Data element of that control. 
			 */
			NodeList dataNodes = el.getElementsByTagName("Data");
			Element dataElement = el;
			if (dataNodes != null && dataNodes.getLength() == 1) {
				dataElement = (Element) dataNodes.item(0);
			}

			int itemIndex = findInputControlByName(ctrlName);
			if (itemIndex < 0) {
				nErrors++;
				continue;
			}
			ControlDescription cd = getInputControlDescriptions().get(itemIndex);
			if (cd.getType().equals(ctrlType) == false) {
				continue;
			}
			Object data = cd.extractXMLElementData(dataElement, dataElement.getAttribute("Value"));
			if (data != null) {
				System.out.println(data.toString());
			}
			formData[itemIndex] = data;
			//			System.out.println("Control " + ctrlName + " type " + ctrlType);
		}
		FormsDataUnit dataUnit = new FormsDataUnit(null, timeMillis, this, formData);


		return dataUnit;
	}

	public String getStringData(FormsDataUnit formsDataUnit, int stringStyle) {
		switch (stringStyle) {
		case LOGGER_FORMS_JSON:
			return getJSONData(formsDataUnit);
		case LOGGER_FORMS_COMMA:
			return getCommaData(formsDataUnit);
		case LOGGER_FORMS_XML:
			return getXMLData(formsDataUnit);
		}
		return null;
	}
	
	/**
	 * Get Comma delimited data with no meta data whatsoever. 
	 * @param formsDataUnit
	 * @return comma delimited data string
	 */
	private String getCommaData(FormsDataUnit formsDataUnit) {
		String str = getFormName();
		ArrayList<InputControlDescription> inputCtrls = getInputControlDescriptions();
		ControlDescription cd;
		Object[] formData = formsDataUnit.getFormData();
		for (int i = 0; i < inputCtrls.size(); i++) {
			cd = inputCtrls.get(i);
			if (formData != null && formData.length > i) {
				if (formData[i] == null) {
					str += ",";
				}
				else {
					str += "," + formData[i].toString();
				}
			}
		}
		return str;
	}

	/**
	 * Get the contents of the data unit as xml.
	 * @return an XML String summarising the form data. 
	 */
	public String getXMLData(FormsDataUnit formsDataUnit) {
		Document doc = XMLUtils.createBlankDoc();
		Element root = doc.createElement("INFO");
		root.setAttribute("Version", String.format("%d", FormsDataUnit.currentXMLFormat));
		doc.appendChild(root);
		//		Element formInfo = doc.createElement("FORM");
		root.setAttribute("FormName", getFormName());
		root.setAttribute("TimeMillis", new Long(formsDataUnit.getTimeMilliseconds()).toString());
		//		root.appendChild(formInfo);
		Element ctrls = doc.createElement("Controls");
		ArrayList<InputControlDescription> inputCtrls = getInputControlDescriptions();
		ControlDescription cd;
		Object[] formData = formsDataUnit.getFormData();
		for (int i = 0; i < inputCtrls.size(); i++) {
			cd = inputCtrls.get(i);
			if (formData != null && formData.length > i) {
				Element el = cd.createXMLDataElement(doc, formsDataUnit, formData[i]);
				ctrls.appendChild(el);					
			}
		}

		root.appendChild(ctrls);


		String xmlString = PamUtils.XMLUtils.getStringFromDocument(doc);
		//		int errors = setXMLData(xmlString);
		return xmlString;
	}
	
	/**
	 * Create logger form data from JASON data. 
	 * @param jsonString
	 * @return array of data objects matching those in the form definition
	 */
	public Object[] fromJSONString(String jsonString) {
		JsonFactory jf = new JsonFactory();
		String formName = null;
		String dataName = "";
		HashMap<String, String> dataPairs = null;
		try {
			JsonParser parser = jf.createParser(jsonString);
			// example {"Form":"Sound_Type","Data":{"Type":"Other","Comment":"Test annotation"}}
			while (parser.nextToken() != JsonToken.END_OBJECT) {
				JsonToken token = parser.getCurrentToken();
				if (token == JsonToken.END_OBJECT) {
					break;
				}
				if (token == JsonToken.START_OBJECT) {
					continue;
				}
				if (token == JsonToken.FIELD_NAME) {
					String currName = token.name(); // this is the type of the token, not of interest
					String currValue = parser.getText(); // this should be the name of the data
					if (token == JsonToken.FIELD_NAME) {
						switch (currValue) {
						case "Form":
							JsonToken dataToken = parser.nextToken();
							Object dataValue = parser.getText();
							formName = dataValue.toString();
							break;
						case "Data":
							dataPairs = readJSONDataPairs(parser);
							break;
						}
					}
				}
//				if (dataToken == JsonToken.END_OBJECT) {
//					break;
//				}
			}
		} catch (IOException e) {
			System.out.printf("Unable to parse data in Logger form %s: \"%s\"\nError: %s", getFormName(), jsonString, e.getMessage());
			return null;
		}
		if (dataPairs == null) {
			return null;
		}
		//now try to match these up with all the field data for the form type. 
		ArrayList<InputControlDescription> inputCtrls = getInputControlDescriptions();
		int nF = inputCtrls.size();
		Object[] data = new Object[nF];
		for (int i = 0; i < nF; i++) {
			InputControlDescription cd = inputCtrls.get(i);
			String strData = dataPairs.get(cd.getDbTitle());
			data[i] = cd.fromString(strData);
		}
		
		return data;
	}

	private HashMap<String, String> readJSONDataPairs(JsonParser parser) throws IOException {
		String nextField = null;
		String nextObject = null;
		HashMap<String,String> dataMap = new HashMap<String, String>();
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			JsonToken token = parser.getCurrentToken();
			if (token == JsonToken.END_OBJECT) {
				break;
			}
			if (token == JsonToken.START_OBJECT) {
				continue;
			}
			if (token == JsonToken.FIELD_NAME) {
				nextField = parser.getText();
				token = parser.nextToken();
				nextObject = parser.getText();
				dataMap.put(nextField, nextObject);
			}
			
			
		}
		return dataMap;
	}

	/**
	 * Create a JSON string from a forms data unit 
	 * @param formsDataUnit forms data unit
	 * @return JSON string
	 */
	public String getJSONData(FormsDataUnit formsDataUnit) {
		Object[] formData = formsDataUnit.getFormData();
		return getJSONData(formData);
	}

	/**
	 * Create a JSON string from a forms data unit 
	 * @param formsDataUnit forms data
	 * @return JSON string
	 */
	public String getJSONData(Object[] formData) {
		if (formData == null) {
			return null;
		}
		JsonFactory jf = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ArrayList<InputControlDescription> inputCtrls = getInputControlDescriptions();
		ControlDescription cd;
		try {
			JsonGenerator jg = jf.createGenerator(os, JsonEncoding.UTF8);
			jg.writeStartObject();
			jg.writeStringField("Form", getFormName());
			jg.writeFieldName("Data");
			jg.writeStartObject();
			for (int i = 0; i < inputCtrls.size(); i++) {
				cd = inputCtrls.get(i);
				if (formData != null && formData.length > i) {
					if (formData[i] == null) {
						jg.writeStringField(cd.getDbTitle(), "null");
					}
					else {
						jg.writeStringField(cd.getDbTitle(), formData[i].toString());
					}
				}
			}
			jg.writeEndObject();
			jg.writeEndObject();
			jg.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		String jsonString = os.toString();
		return jsonString;
	}
	
	@Override
	public int compareTo(FormDescription otherForm) {
		Integer thisOrder = null, otherOrder = null;
		String thisName = null, otherName = null;
		ItemDescription t = this.findProperty(PropertyTypes.ORDER);
		if (t != null) {
			thisOrder = t.getLength();
		}
		t = otherForm.findProperty(PropertyTypes.ORDER);
		if (t != null) {
			otherOrder = t.getLength();
		}
		if (otherOrder != null && thisOrder != null) {
			return thisOrder - otherOrder;
		}
		if (otherOrder != null && thisOrder == null) {
			return -1;
		}
		if (otherOrder == null && thisOrder != null) {
			return 1;
		}
		// both order values are null, so compare the form names instead.
		thisName = getFormName();
		otherName = otherForm.getFormName();
		if (thisName == null) thisName = " ";
		if (otherName == null) otherName = " ";
		return thisName.compareTo(otherName);
	}
	/**
	 * Get the order property for the form
	 * @return order property or null if it isn't set. 
	 */
	public Integer getFormOrderProperty() {
		PropertyDescription formProperty = findProperty(PropertyTypes.ORDER);
		if (formProperty == null) {
			return null;
		}
		return formProperty.getLength();
	}

	/**
	 * Set the form order property. 
	 * @param order order in list of forms. 
	 */
	public void setFormOrderProperty(Integer order) {
		PropertyDescription formProperty = findProperty(PropertyTypes.ORDER);
		if (formProperty == null) {
			ItemInformation iif = new ItemInformation(this);
			iif.setProperty(UDColName.Type.toString(), PropertyTypes.ORDER.toString());
			formProperty = new PropertyDescription(this, iif);
			getPropertyDescriptions().add(formProperty);
		}
		formProperty.setLength(order);
	}

	/**
	 * @return true if the forms UDF table needs rewriting. 
	 */
	public boolean isNeedsUDFSave() {
		return needsUDFSave;
	}

	/**
	 * @param needsUDFSave Set true if the forms UDF is going to need re-saving 
	 */
	public void setNeedsUDFSave(boolean needsUDFSave) {
		this.needsUDFSave = needsUDFSave;
	}

	public GpsData getOriginLatLong(FormsDataUnit formsDataUnit) {
		GpsData gps = getOrigin(GPSOriginSystem.class, formsDataUnit);
		if (gps != null) {
			return gps;
		}
		gps = getOrigin(StaticOriginSystem.class, formsDataUnit);
		return gps;
	}
	
	private GpsData getOrigin(Class originClass, FormsDataUnit formsDataUnit) {
		HydrophoneOriginMethods origins = HydrophoneOriginMethods.getInstance();
		HydrophoneOriginMethod origin = origins.getMethod(GPSOriginMethod.class, null, null);
		if (origin == null) {
			return null;
		}
		OriginIterator gpsIter = origin.getGpsDataIterator(PamDataBlock.ITERATOR_END);
		GpsData prev = null;
		while (gpsIter.hasPrevious()) {
			prev = gpsIter.previous();
			if (prev.getTimeInMillis() < formsDataUnit.getTimeMilliseconds()) {
				break;
			}
		}
		return prev;		
	}
	

}
