/**
 * 
 */
package loggerForms.controlDescriptions;

import javax.swing.JPanel;

import loggerForms.FormDescription;
import loggerForms.FormsTableItem;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.LoggerFormPanel;
import loggerForms.controls.LoggerControl;
import loggerForms.controls.LoggerFormLabel;

/**
 * @author GrahamWeatherup
 *
 */
public class CdxStatic extends ControlDescription {

	/**
	 * @param formDescription
	 */
	public CdxStatic(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
	}
	
	@Override
	public FormsTableItem[] getFormsTableItems() {return null;};

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return null;
	}

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeComponent(loggerForms.LoggerForm)
	 */
	@Override
	public JPanel makeComponent(LoggerForm loggerForm) {
		JPanel tmpPanel=new LoggerFormPanel(loggerForm);
		tmpPanel.add(new LoggerFormLabel(loggerForm, getTitle()));
		return tmpPanel;
	}
}
