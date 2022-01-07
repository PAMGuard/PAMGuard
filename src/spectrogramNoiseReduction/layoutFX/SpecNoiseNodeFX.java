package spectrogramNoiseReduction.layoutFX;

import javafx.scene.Node;

public interface SpecNoiseNodeFX {

	/**
	 * Get the fx based GUI. 
	 * @return
	 */
	public Node getNode();

	public boolean getParams();

	public void setParams();

	/**
	 * Called when the "Run this method" is checked or unchecked.
	 * @param selected true if selected
	 */
	public void setSelected(boolean selected);

}
