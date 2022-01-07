package clipgenerator.clipDisplay;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clipgenerator.ClipControl;

public class ClipDisplayMarker extends OverlayMarker {

	private ClipDisplayPanel clipDisplayPanel;
	
	private ClipDisplayUnit firstClickedUnit;

	public ClipDisplayMarker(ClipDisplayPanel clipDisplayPanel) {
		super(clipDisplayPanel, 0, null);
		this.clipDisplayPanel = clipDisplayPanel;
	}

	@Override
	public String getMarkerName() {
		/*
		 * The basic panel name is not unique enough. Markers will need to provide
		 * a unique name. Therefore try to also get the name of the 
		 */
		String uniqueName = clipDisplayPanel.getUniqueName();
		String frameTitle =  clipDisplayPanel.getFrameTitle();
		return uniqueName;
	}

	/**
	 * A display unit was clicked. 
	 * @param e
	 * @param clipDisplayUnit
	 */
	public void mouseClicked(MouseEvent e, ClipDisplayUnit clipDisplayUnit) {
		if (clipDisplayUnit == null) {
			return;
		}
		if (!e.isControlDown() && !e.isShiftDown()) {
			// deselect everything else and select just this one. 
			clipDisplayPanel.clearAllHighlights();
			clipDisplayUnit.toggleHighlight();
			firstClickedUnit = clipDisplayUnit.isHighlight() ? clipDisplayUnit : null;
			return;
		}
		if (e.isControlDown()) {
			// toggle the current selected, but nothing else. 
			clipDisplayUnit.toggleHighlight();
			firstClickedUnit = clipDisplayUnit;
			return;
		}
		if (e.isShiftDown()) {
			ArrayList<ClipDisplayUnit> currentHighlights = clipDisplayPanel.getHighlightedUnits();
//			System.out.println("Number of highlighted units = " + currentHighlights.size());
			if (currentHighlights.size() == 0) {
				clipDisplayUnit.setHighlight(true);
				firstClickedUnit = clipDisplayUnit;
			}
			else if (firstClickedUnit != null && e.isShiftDown()) {
				clipDisplayPanel.selectClipRange(firstClickedUnit, clipDisplayUnit);
			}
		}
		
	}

	@Override
	public OverlayMark getCurrentMark() {
		ArrayList<ClipDisplayUnit> currentHighlights = clipDisplayPanel.getHighlightedUnits();
		if (currentHighlights == null || currentHighlights.size() == 0) {
			return null;
		}
		return new ClipDisplayMark(this, currentHighlights);
	}

	@Override
	public List<PamDataUnit> getSelectedMarkedDataUnits(OverlayMark overlayMark, MarkDataSelector markDataSelector) {
		return getHiglightedUnits();
	}

	@Override
	public List<PamDataUnit> getSelectedMarkedDataUnits(OverlayMark overlayMark, MarkDataSelector markDataSelector,
			int minOverlap) {
		return getHiglightedUnits();
	}

	private List<PamDataUnit> getHiglightedUnits() {
		ArrayList<ClipDisplayUnit> currentHighlights = clipDisplayPanel.getHighlightedUnits();
		ArrayList<PamDataUnit> clipDataUnits = new ArrayList<PamDataUnit>();
		if (currentHighlights == null) {
			return null;
		}
		for (ClipDisplayUnit cdu : currentHighlights) {
			clipDataUnits.add(cdu.getClipDataUnit());
		}
		
		return clipDataUnits;
	}


}
