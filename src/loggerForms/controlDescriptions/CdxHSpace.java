/**
 * 
 */
package loggerForms.controlDescriptions;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JPanel;

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
public class CdxHSpace extends ControlDescription {

	/**
	 * @param formDescription
	 */
	public CdxHSpace(FormDescription formDescription, ItemInformation itemInformation) {
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
		tmpPanel.add(Box.createRigidArea(new Dimension(this.getLength()*9,0)));
		return tmpPanel;
	}

}
