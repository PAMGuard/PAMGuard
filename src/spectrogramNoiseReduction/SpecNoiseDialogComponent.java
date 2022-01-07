package spectrogramNoiseReduction;

import javax.swing.JComponent;

public interface SpecNoiseDialogComponent {

	public JComponent getSwingComponent();
	
	public boolean getParams();
	
	public void setParams();
	
	/**
	 * Called when the "Run this method" is checked or unchecked.
	 * @param selected true if selected
	 */
	public void setSelected(boolean selected);
	
}
