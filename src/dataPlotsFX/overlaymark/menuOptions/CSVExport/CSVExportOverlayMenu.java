package dataPlotsFX.overlaymark.menuOptions.CSVExport;

import PamView.paneloverlay.overlaymark.OverlayMark;
import dataPlotsFX.overlaymark.menuOptions.ExportOverlayMenu;
import dataPlotsFX.overlaymark.menuOptions.OverlayMenuItem;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;

/***
 * Export data units as .csv file
 * @author Jamie Macaulay
 *
 */
public class CSVExportOverlayMenu extends ExportOverlayMenu {

	private Text csvFileGlyph;
	private PamButton button;

	public CSVExportOverlayMenu() {
//		csvFileGlyph=PamGlyphDude.createPamGlyph(MaterialDesignIcon.FILE_EXCEL, standardIconSize);
		csvFileGlyph=PamGlyphDude.createPamIcon("mdi2f-file-excel", standardIconSize);
		button = new PamButton();
		button.setGraphic(csvFileGlyph);
	}


	@Override
	public Labeled menuAction(DetectionGroupSummary foundDataUnits, int index, OverlayMark mark) {
		// TODO Auto-generated method stub
		return button; 
	}

	@Override
	public boolean canBeUsed(DetectionGroupSummary foundDataUnits, int index, OverlayMark mark) {
		if (foundDataUnits!=null && foundDataUnits.getDataList().size()>0) return true; 
		return false;
	}

	@Override
	public int getFlag() {
		return OverlayMenuItem.EXPORT_GROUP;
	}

	@Override
	public Tooltip getNodeToolTip() {
		return new Tooltip("Export the current data unit(s) to a .csv file. This can be imported into R");
	}
	
	@Override
	public int getSubMenuGroup() {
		return -1;
	}

}
