package Array;

import javax.swing.JPanel;

public interface LocatorDialogPanel {

	public JPanel getDialogPanel();
	
	public void setParams(PamArray currentArray);
	
	public boolean getParams(PamArray currentArray);
}
