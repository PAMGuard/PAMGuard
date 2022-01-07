package dataPlotsFX.overlaymark.menuOptions.copyImage;

import PamView.paneloverlay.overlaymark.OverlayMark;
import dataPlotsFX.overlaymark.menuOptions.OverlayMenuItem;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;

public class CopyTDDataPlot implements OverlayMenuItem {

	@Override
	public Tooltip getNodeToolTip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Labeled menuAction(DetectionGroupSummary foundDataUnits, int index, OverlayMark overlayMark) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canBeUsed(DetectionGroupSummary foundDataUnits, int index, OverlayMark overlayMark) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getFlag() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getSubMenuGroup() {
		return -1;
	}


}