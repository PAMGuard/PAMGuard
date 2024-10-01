package tethys.swing.export;

import PamView.dialog.PamDialogPanel;

public interface LocalizationOptionsPanel extends PamDialogPanel {

	/**
	 * Is the panel big enough that it needs opening in a separate dialog, 
	 * or is it small enough to squish into the export panel ? <p>
	 * Caller may chose to ignore this information.   
	 * @return true if it should open in it's own window. 
	 */
	public boolean isBig();

}
