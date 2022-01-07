package generalDatabase;

import javax.swing.JPanel;

/**
 * Different db systems can proide a system specific dialog panel
 * by implementing this interface
 * @author Doug
 *
 */
public interface SystemDialogPanel {

	JPanel getPanel();
	
	boolean getParams();
	
	void setParams();
}
