package clipgenerator.clipDisplay;

import java.util.List;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.paneloverlay.overlaymark.MarkExtraInfo;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarker;

public class ClipDisplayMark extends OverlayMark {

	/**
	 * not currently used. If used, would need to make a new method in OverlayMarker to abstract out
	 * the construction of marks, so that markers can make bespoke types such as this. Otherwise it
	 * ends up as a mess of a mark made by the marker and one of these !
	 * @param clipDisplayMarker
	 * @param clipDisplayUnits
	 */
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
