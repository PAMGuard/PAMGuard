package loggerForms.formdesign;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import loggerForms.FormDescription;
import loggerForms.FormsControl;
import loggerForms.ItemInformation;
import loggerForms.PropertyDescription;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.formdesign.propertypanels.AutoAlertPanel;
import loggerForms.formdesign.propertypanels.BearingPanel;
import loggerForms.formdesign.propertypanels.BooleanPanel;
import loggerForms.formdesign.propertypanels.ColourPanel;
import loggerForms.formdesign.propertypanels.FontPanel;
import loggerForms.formdesign.propertypanels.HeadingPanel;
import loggerForms.formdesign.propertypanels.HotkeyPanel;
import loggerForms.formdesign.propertypanels.IntegerPanel;
import loggerForms.formdesign.propertypanels.NMEAPanel;
import loggerForms.formdesign.propertypanels.NotImplementedPanel;
import loggerForms.formdesign.propertypanels.OrderPanel;
import loggerForms.formdesign.propertypanels.PropertyPanel;
import loggerForms.formdesign.propertypanels.RangePanel;
import loggerForms.formdesign.propertypanels.SymbolPanel;
import loggerForms.formdesign.propertypanels.TextPanel;
import loggerForms.formdesign.propertypanels.XReferencePanel;

/**
 * Layer that sits between the form description and the actual edit
 * dialog for altering a form. this is so we can separate out the 
 * GUI of the edit dialog from the part doing the actual work
 * serving up options, etc. 
 * @author Doug
 *
 */
public class FormEditor {
	
	private FormsControl formsControl;
	
	private FormDescription formDescription;

	private Window parentFrame;

	private ArrayList<ControlTitle> controlTitles = new ArrayList<>();
	
	public enum EditNotify {CONTROLCHANGE, PROPERTYCHANGE};
	
	/**
	 * @return the controlTitles
	 */
	public ArrayList<ControlTitle> getControlTitles() {
		return controlTitles;
	}
	
	/**
	 * Loads the controlTitles Arraylist with data from the formDescription object.<br><br>  Important: the ControlTitle objects created here
	 * DO NOT hold valid references to the FormEditDialog object.  This method should NOT be used in any sort of way that interacts
	 * with the user, since the FormEditDialog object is used extensively in the parameters GUI.  This method is only used to load
	 * the arrayList in preparation for writing the data to the UDF database table, and clearControlTitles is called immediately
	 * afterwards to make sure that the data isn't accidentally used somewhere else
	 */
	public void populateControlTitles() {
		ArrayList<ItemInformation> formControls = formDescription.getControlsInformationCopy();
		controlTitles.clear();
		for (ItemInformation ds:formControls) {
			controlTitles.add(new ControlTitle(null, ds));
		}
	}
	
	/**
	 * Clears the controlTitles ArrayList
	 */
	public void clearControlTitles() {
		controlTitles.clear();
	}

	/**
	 * @return the propertyInformation
	 */
	public Hashtable<PropertyTypes, ItemInformation> getPropertyInformation() {
		return propertyInformation;
	}

	private Hashtable<PropertyTypes, ItemInformation> propertyInformation;

	public FormEditor(FormsControl formsControl, Window parentFrame, FormDescription formDescription) {
		super();
		this.formsControl = formsControl;
		this.parentFrame = parentFrame;
		this.formDescription = formDescription;
		
		propertyInformation = new Hashtable<>();
		ArrayList<PropertyDescription> propInf = formDescription.getPropertyDescriptions();
		for (PropertyDescription propertyDescription:propInf) {
			propertyInformation.put(propertyDescription.getPropertyType(), propertyDescription.getItemInformation().clone());
		}
	}
	
	/**
	 * Get a specific property. <p>
	 * Since not all properties will be set, null will often be returned
	 * which equivalent to it not being selected. 
	 * @param propertyType Property to fetch
	 * @return Description of the property or null
	 */
	public ItemInformation getFormProperty(PropertyTypes propertyType) {
		return propertyInformation.get(propertyType);
	}
	
	/**
	 * Set a form property. If the description is null, this means removing it
	 * from the list. 
	 * @param propertyType property to set. 
	 * @param itemInformation description of the property or null 
	 */
	public void setFormProperty(PropertyTypes propertyType, ItemInformation itemInformation) {
		if (itemInformation == null) {
			propertyInformation.remove(propertyType);
		}
		else {
			propertyInformation.put(propertyType, itemInformation);
		}
	}

	/**
	 * @return the parentFrame
	 */
	public Window getParentFrame() {
		return parentFrame;
	}

	/**
	 * @return the formsControl
	 */
	public FormsControl getFormsControl() {
		return formsControl;
	}

	/**
	 * @return the formDescription
	 */
	public FormDescription getFormDescription() {
		return formDescription;
	}
	
 
	/**
	 * Create the right type of panel for the given property
	 * @param propertyType
	 * @return a strip panel to go in the dialog. 
	 */
	public PropertyPanel makePropertyPanel(PropertyTypes propertyType) {
		
		switch (propertyType) {
		case STARTTIME:
			return new XReferencePanel(this, propertyType, ControlTypes.TIMESTAMP);
		case ENDTIME:
			return new XReferencePanel(this, propertyType, ControlTypes.TIMESTAMP);
		case AUTOALERT:
			return new IntegerPanel(this, propertyType, UDColName.AutoUpdate, "Alert operator every", "minutes");
		case AUTORECORD:
			return new NotImplementedPanel(this, propertyType);
		case BEARING:
			return new BearingPanel(this, propertyType);
//		case BLOBSIZE:
//			return new IntegerPanel(this, propertyType, UDColName.Length, "Default size for map symbols", "pixels");
		case DBTABLENAME:
			return new TextPanel(this, propertyType, UDColName.Title, null, "If blank, defaults to form name");
		case FONT:
			return new FontPanel(this, propertyType);
		case FORMCOLOR:
			return null;
		case FORMCOLOUR:
			return new ColourPanel(this, propertyType);
		case HEADING:
			return new HeadingPanel(this, propertyType);
		case HIDDEN:
			return new BooleanPanel(this, propertyType, "Form is hidden (for forms which complete automatically)");
		case HOTKEY:
			return new HotkeyPanel(this, propertyType);
		case NOCANCEL:
			return new BooleanPanel(this, propertyType, "Disables the forms Cancel button");
		case NOCLEAR:
			return new BooleanPanel(this, propertyType, "Disables the forms Clear button");
		case NOTOFFLINE:
			return new BooleanPanel(this, propertyType, "Prevents the form from displaying in Viewer mode");
		case NOTONLINE:
			return new BooleanPanel(this, propertyType, "Prevents the form from displaying in Normal mode");
		case ORDER:
			return new OrderPanel(this);
		case PLOT:
			return new BooleanPanel(this, propertyType, "Form can be plotted on the map");
		case POPUP:
			return new BooleanPanel(this, propertyType, "Makes the form appear in a separate window");
		case RANGE:
			return new RangePanel(this, propertyType);
		case READONGPS:
			return new BooleanPanel(this, propertyType, "Save whenever new GPS data are read from NMEA");
		case READONNMEA:
			return new NMEAPanel(this, propertyType);
		case READONTIMER:
			return new IntegerPanel(this, propertyType, UDColName.AutoUpdate, "Save automatically every", "seconds");
		case SUBTABS:
			return new BooleanPanel(this, propertyType, "Multiple forms can appear simultaneously on separate subtabs");
		case SYMBOLTYPE:
			return new SymbolPanel(this, propertyType);
		default:
			break;
		
		}
		
		return new BooleanPanel(this, propertyType, null);
	}

	/**
	 * Create a temporary preview of a form during the design phase. 
	 */
	public void previewForm() {
		/**
		 * Make a master copy of all item infos from both the properties and
		 * from the controls ...
		 */
		ArrayList<ItemInformation> itemInfos = getAllFormInformations();
		
		FormDescription newDescription = new FormDescription(formDescription, itemInfos);
		
		PreviewDialog previewDialog = new PreviewDialog(formsControl.getGuiFrame(), newDescription);
		previewDialog.setVisible(true);
		
	}
	
	/**
	 * Extracts a fresh list of all item informations from the dialog. 
	 * <p>This is put pack into a single list since that's the way 
	 * that the FormDescription originally received it. Note entirely 
	 * efficient, but 
	 * @return
	 */
	public ArrayList<ItemInformation> getAllFormInformations() {
		ArrayList<ItemInformation> itemInfos = new ArrayList<>();
		Enumeration<ItemInformation> propertyEls = propertyInformation.elements();
		ItemInformation itemInfo;
		while (propertyEls.hasMoreElements()) {
			itemInfos.add(itemInfo = propertyEls.nextElement());
//			System.out.println("Adding form property " + itemInfo.getStringProperty(UDColName.Type.toString()));
		}
		
		// if the controlTitles list is empty, try to populate it.  This can happen if the form has not been loaded into the form editor yet, but something is
		// trying to operate on it (for example, changing the order of a form)
		if (controlTitles.isEmpty()) {
			populateControlTitles();
		}
		for (ControlTitle ctrlTit:controlTitles) {
			itemInfos.add(itemInfo = ctrlTit.getItemInformation());
		}
		
		/**
		 * Now put order information into the item infos 
		 */
		int orderId = 10;
		int orderStep = 10;
		for (ItemInformation it:itemInfos) {
			it.setProperty("Order", orderId);
			orderId += orderStep;
		}
		
		return itemInfos;
	}

}
