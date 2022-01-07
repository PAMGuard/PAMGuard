package clipgenerator.clipDisplay;

import java.util.List;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.paneloverlay.overlaymark.MarkExtraInfo;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarker;

public class ClipDisplayMark extends OverlayMark {

	public ClipDisplayMark(ClipDisplayMarker clipDisplayMarker, List<ClipDisplayUnit> clipDisplayUnits) {
		super(clipDisplayMarker, null, null, 0, null, null);
		if (clipDisplayUnits != null) {
			addClipDisplayUnits(clipDisplayUnits);
		}
	}

	private void addClipDisplayUnits(List<ClipDisplayUnit> clipDisplayUnits) {
		// TODO Auto-generated method stub
		
	}

}
