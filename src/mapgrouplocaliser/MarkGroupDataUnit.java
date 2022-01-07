package mapgrouplocaliser;

import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class MarkGroupDataUnit extends SuperDetection {

	private OverlayMark overlayMark;

	public MarkGroupDataUnit(long timeMilliseconds, OverlayMark overlayMark) {
		super(timeMilliseconds);
		this.overlayMark = overlayMark;
	}

	public OverlayMark getOverlayMark() {
		return overlayMark;
	}

}
