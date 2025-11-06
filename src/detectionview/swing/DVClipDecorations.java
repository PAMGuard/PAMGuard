package detectionview.swing;

import clipgenerator.clipDisplay.ClipDisplayDecorations;
import clipgenerator.clipDisplay.ClipDisplayUnit;
import detectionview.DVControl;

public class DVClipDecorations extends ClipDisplayDecorations {

	private DVControl dvControl;

	public DVClipDecorations(DVControl dvControl, ClipDisplayUnit clipDisplayUnit) {
		super(clipDisplayUnit);
		this.dvControl = dvControl;
	}

}
