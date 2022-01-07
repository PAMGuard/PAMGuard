package clickDetector.ClickClassifiers;

import java.awt.Component;

/**
 * Click Classifiers all now share a common dialog and need to provide
 * a common component to it. 
 * @author Doug gillespie
 *
 */
public interface ClassifyDialogPanel {

	public Component getComponent();
	
	public void setParams();
	
	public boolean getParams();
	
	public String getHelpPoint();

	public void setActive(boolean b);
	
}
