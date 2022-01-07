package mapgrouplocaliser;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamUtils.PamCalendar;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.HoverData;
import PamView.paneloverlay.OverlayDataInfo;
import PamView.paneloverlay.overlaymark.MarkDataMatcher;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkDataInfo;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelector;
import annotation.AnnotationDialog;
import annotation.AnnotationDialogPanel;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationChoiceHandler;
import autecPhones.AutecGraphics;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.scene.input.MouseEvent;

public class MarkGroupProcess extends PamProcess implements OverlayMarkObserver {

	private MapGroupLocaliserControl markGroupLocaliser;
	private MarkGroupDataBlock markGroupDataBlock;
	private MarkGroupSQLLogging markGroupSQLLogging;

	public MarkGroupProcess(MapGroupLocaliserControl markGroupLocaliser) {
		super(markGroupLocaliser, null);
		this.markGroupLocaliser = markGroupLocaliser;
		markGroupDataBlock = new MarkGroupDataBlock(markGroupLocaliser.getUnitName() + " Marks", this);
		addOutputDataBlock(markGroupDataBlock);
		markGroupDataBlock.setOverlayDraw(new MapGroupOverlayDraw(markGroupLocaliser, null));
		markGroupDataBlock.setPamSymbolManager(new StandardSymbolManager(markGroupDataBlock, MapGroupOverlayDraw.defSymbol, false));
		markGroupSQLLogging = new MarkGroupSQLLogging(markGroupLocaliser, markGroupDataBlock);
		markGroupDataBlock.SetLogging(markGroupSQLLogging);
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean markUpdate(int markStatus, MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark) {
		if (markStatus == OverlayMarkObserver.MARK_END) {
			markComplete(overlayMarker, overlayMark);
		}
		return true;
	}

	private void markComplete(OverlayMarker overlayMarker, OverlayMark overlayMark) {
		/*
		 *  find a list of data units plotted within the mark. 
		 */
		GeneralProjector projector = overlayMarker.getProjector();
		ListIterator<HoverData> hoverIt = projector.getHoverDataList().listIterator();
		MarkDataMatcher markMatch = new MarkDataMatcher(overlayMark, projector);

		lastDataBlock = lastDataSelectDataBlock = null;
		int[] sideCount = new int[3];
		MarkGroupDataUnit mapGroupDataUnit = new MarkGroupDataUnit(PamCalendar.getTimeInMillis(), overlayMark);
		int nAdded = 0;
		PamDataUnit lastAddedUnit = null;
		while (hoverIt.hasNext()) {
			HoverData hoverData = hoverIt.next();
			PamDataUnit dataUnit = hoverData.getDataUnit();
			if (dataUnit == null) {
				continue;
			}

			if (wantDataUnit(hoverData, markMatch) == false) {
				continue;
			}

			sideCount[hoverData.getAmbiguity()] ++;
			if (lastAddedUnit != dataUnit) {
				/**
				 * Units may be in hoverdata twice if they had a plot ambiguity. 
				 */
				mapGroupDataUnit.addSubDetection(dataUnit);
				lastAddedUnit = dataUnit;
				nAdded ++;
			}
		}
		/*
		 * Now annotate the dataunit with any selected annotations. 
		 */
		boolean keepData = handleAnnotations(overlayMarker, overlayMark, mapGroupDataUnit);
		/*
		 * And add to the data block
		 */
		if (keepData) {
			markGroupDataBlock.addPamData(mapGroupDataUnit);
//			System.out.printf("%d data units added to mark data unit on side %d,%d,%d\n", nAdded, sideCount[0], sideCount[1], sideCount[2]);
		}
	}

	private boolean handleAnnotations(OverlayMarker overlayMarker, OverlayMark overlayMark,
			MarkGroupDataUnit mapGroupDataUnit) {
		AnnotationChoiceHandler anHandler = markGroupLocaliser.getAnnotationHandler();
		List<DataAnnotationType<?>> anTypes = anHandler.getUsedAnnotationTypes();
		for (DataAnnotationType anType:anTypes) {
			if (!addAnnotation(overlayMarker, overlayMark, anType, mapGroupDataUnit)) {
				return false;
			}
		}
		return true;
	}

	private boolean addAnnotation(OverlayMarker overlayMarker, OverlayMark overlayMark,
			DataAnnotationType anType, MarkGroupDataUnit mapGroupDataUnit) {
		// see if it's an annotation with a dialog. 
		AnnotationDialogPanel dialogPanel = anType.getDialogPanel();
		if (dialogPanel == null) {
			anType.autoAnnotate(mapGroupDataUnit);
			return true;
		}
		else {
			// make and show a dialog. show it close to the current mouse position
			PointerInfo mousePointerInfo = MouseInfo.getPointerInfo();
			Point locOnScreen = mousePointerInfo.getLocation();
			boolean ans = AnnotationDialog.showDialog(markGroupLocaliser.getGuiFrame(), 
					anType, mapGroupDataUnit, locOnScreen);
			//			System.out.println("Data unit has annotation " + ans);
			return ans;
		}
	}

	private boolean wantDataUnit(HoverData hoverData, MarkDataMatcher markMatch) {
		PamDataUnit dataUnit = hoverData.getDataUnit();
		PamDataBlock dataBlock = dataUnit.getParentDataBlock();
		OverlayMarkDataInfo overlayDataInfo = findMarkDataInfo(dataBlock);
		// is it checked that we want this type of data. 
		if (overlayDataInfo == null || overlayDataInfo.select == false) {
			return false;
		}
		// do we want this individual data unit ?
		if (isDataSelected(dataBlock, dataUnit) == false) {
			return false;
		}
		// now see if it's in the marked area...
		// if it gets thsi far we want it if it's in the area. 
		boolean isContained = markMatch.isContained(hoverData);
		if (isContained) {
			return true;
		}
		// may still consider it if it's overlapping. 
		else if (overlayDataInfo.acceptOverlapping) {
			return markMatch.bearingOverlap(hoverData);
		}
		// else
		return false;
	}

	private PamDataBlock lastDataBlock;
	private OverlayMarkDataInfo lastOverlayDataInfo;
	/**
	 * See if this datablock is selected. Speed things up by storing which selector 
	 * was used last on the assumption that consecutive calls are likely to be from the 
	 * same datablock. 
	 * @param dataBlock
	 * @return true  if selected. 
	 */
	private OverlayMarkDataInfo findMarkDataInfo(PamDataBlock dataBlock) {
		if (lastDataBlock != dataBlock || lastOverlayDataInfo == null) {
			lastOverlayDataInfo = markGroupLocaliser.getMapGrouperSettings().getOverlayInfo(dataBlock);
		}
		lastDataBlock = dataBlock;
		return lastOverlayDataInfo;
	}

	private PamDataBlock lastDataSelectDataBlock;
	private DataSelector dataSelector;
	/**
	 * Do we want this actual data unit based on the name of the data selector. <p>
	 * Assume this is likely to be called often for data from the same datablock, so 
	 * don't keep relooking for the data selector.
	 * @param dataBlock
	 * @param dataUnit
	 * @return true if there is no selector, otherwise ask the selector. 
	 */
	private boolean isDataSelected(PamDataBlock dataBlock, PamDataUnit dataUnit) {
		if (lastDataSelectDataBlock != dataBlock) {
			dataSelector = dataBlock.getDataSelector(markGroupLocaliser.getMapGroupOverlayManager().getDataSelectorName(), true);
		}
		if (dataSelector == null) {
			return true;
		}
		else {
			return dataSelector.scoreData(dataUnit) > 0;
		}
	}

	/**
	 * @return the markGroupSQLLogging
	 */
	public MarkGroupSQLLogging getMarkGroupSQLLogging() {
		return markGroupSQLLogging;
	}

	@Override
	public ParameterType[] getRequiredParameterTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMarkName() {
		// TODO Auto-generated method stub
		return null;
	}

}
