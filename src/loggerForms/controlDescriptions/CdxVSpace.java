/**
 * 
 */
package loggerForms.controlDescriptions;

import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import loggerForms.FormDescription;
import loggerForms.FormsTableItem;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.LoggerFormPanel;
import loggerForms.controls.LoggerControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdxVSpace extends ControlDescription {

	/**
	 * @param formDescription
	 */
	public CdxVSpace(FormDescription formDescription, ItemInformation itemInformation) {
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
		tmpPanel.add(new JSeparator(SwingConstants.VERTICAL));
		return tmpPanel;
	}
}
