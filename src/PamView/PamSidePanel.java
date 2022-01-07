package PamView;

import javax.swing.JComponent;

/**
 * PamSidePanels provide support for additional display units for each
 * PamControlledUnit displayed in a vertical column to the left of the main
 * tabbed display. 
 * <p>
 * Side panels should be as small as possible so as not to take space away from
 * the main tab panel. 
 * @author Douglas Gillespie
 *
 */
public interface PamSidePanel {

	/**
	 * @return Reference to a graphics component that can be added to the view.
	 *         This will typically be a JPanel or a JInternalFrame;
	 *         The component will be displayed to the side of the main tab control.
	 */
	JComponent getPanel();
	
	public void rename(String newName);
}
