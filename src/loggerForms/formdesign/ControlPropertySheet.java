package loggerForms.formdesign;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import loggerForms.ItemInformation;


/**
 * Sheet of control properties for a particular control title. 
 * @author Doug
 *
 */
public class ControlPropertySheet {

	private JPanel sheetComponent;
	
	private FormEditor formEditor;
	
	private ControlTitle controlTitle;

	public ControlPropertySheet(FormEditor formEditor, ControlTitle controlTitle) {
		super();
		this.formEditor = formEditor;
		this.controlTitle = controlTitle;
		sheetComponent = new JPanel();
		sheetComponent.setLayout(new BoxLayout(sheetComponent, BoxLayout.Y_AXIS));
	}
	
	/**
	 * pushes the properties from the item descriptions into 
	 * the individual property panels. 
	 */
	public void pushProperties() {
		
	}
	
	
	/**
	 * Fetches properties from individual propertypanels
	 * back into the ItemDescriptions. 
	 * @return
	 */
	public boolean fetchProperties() {
		
		return false;
	}

	/**
	 * @return the sheetComponent
	 */
	public JPanel getSheetComponent() {
		return sheetComponent;
	}

	/**
	 * @return the formEditor
	 */
	public FormEditor getFormEditor() {
		return formEditor;
	}

	/**
	 * @return the controlTitle
	 */
	public ControlTitle getControlTitle() {
		return controlTitle;
	}

}
