package PamView.wizard;

import javax.swing.JPanel;


/**
 * Base class for PAMGuard wizard cards. 
 * @author dg50
 *
 * @param <T> class type for parameters to set and get. 
 */
abstract public class PamWizardCard<T extends Object> extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private String title;

	private PamWizard pamWizard;

	/**
	 * @param title
	 */
	public PamWizardCard(PamWizard pamWizard, String title) {
		this.pamWizard = pamWizard;
		this.title = title;
	}

	public abstract boolean getParams(T cardParams);
	
	public abstract void setParams(T cardParams);

	public String getTitle() {
		return title;
	}

	/**
	 * @return the pamWizard
	 */
	public PamWizard getPamWizard() {
		return pamWizard;
	}
	
}
