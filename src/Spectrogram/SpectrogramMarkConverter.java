package Spectrogram;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import PamUtils.PamCoordinate;
import PamUtils.PamUtils;
import PamView.GeneralProjector.ParameterType;
import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMark.OverlayMarkType;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import Spectrogram.SpectrogramDisplay.SpectrogramPanel;
import dataPlotsFX.layout.TDGraphFX;
import detectiongrouplocaliser.DetectionGroupSummary;

/**
 * Class to convert the new style OverlayMark's into old style spectrogram marks.<p>
 * This can be used until people get round to rewriting receivers of spectrogram marks 
 * to the point where they can use the new system.
 * SpectrogramMarkObservers. 
 * @author dg50
 *
 */
public class SpectrogramMarkConverter implements OverlayMarkObserver {

	private SpectrogramMarkObserver spectrogramMarkObserver;
	
	private static final ParameterType[] parameterTypes = {ParameterType.TIME, ParameterType.FREQUENCY};
	
	/**
	 * @param spectrogramMarkObserver
	 */
	public SpectrogramMarkConverter(SpectrogramMarkObserver spectrogramMarkObserver) {
		this.spectrogramMarkObserver = spectrogramMarkObserver;
	}

	@Override
	public boolean markUpdate(int markStatus, javafx.scene.input.MouseEvent mouseEvent, OverlayMarker overlayMarker, OverlayMark overlayMark) {
		
		// first check the status
		int downUp = 0;
		switch (markStatus) {
		case OverlayMarkObserver.MARK_START:
			downUp = SpectrogramMarkObserver.MOUSE_DOWN;
			break;
		case OverlayMarkObserver.MARK_END:
			downUp = SpectrogramMarkObserver.MOUSE_UP;
			break;
		case OverlayMarkObserver.MARK_UPDATE:
			downUp = SpectrogramMarkObserver.MOUSE_DRAG;
			break;
		case OverlayMarkObserver.MARK_CANCELLED:
			return false;
		}
		
		/**
		 * Call through to 
		 * 
	public boolean spectrogramNotification(SpectrogramDisplay display, MouseEvent mouseEvent, int downUp, int channel, 
			long startMilliseconds, long duration, double f1, double f2);
		 */
		if (overlayMark.getMarkType() != OverlayMarkType.RECTANGLE) {
			return false;
		}
		
		SpectrogramDisplay spec = null;
		TDGraphFX tdDisp = null;
		if (overlayMark.getMarkSource() instanceof SpectrogramDisplay.SpectrogramPanel) {
//		if (SpectrogramDisplay.SpectrogramPanel.class.isAssignableFrom(overlayMark.getMarkSource().getClass())) {
			spec = ((SpectrogramPanel) overlayMark.getMarkSource()).getSpectrogramDisplay();
		}
		if (overlayMark.getMarkSource() instanceof TDGraphFX) {
			tdDisp = (TDGraphFX) overlayMark.getMarkSource();
		}
		/**
		 * 
	public static final int MARK_START = 0;
	public static final int MARK_END = 1;
	public static final int MARK_UPDATE = 2;
	public static final int MARK_CANCELLED = 3;
		 */
		
		PamCoordinate currentMouse = overlayMark.getCurrentMouse();
		PamCoordinate c0 = overlayMark.getCoordinate(0);
		PamCoordinate c1 = overlayMark.getLastCoordinate(); // avoid crash when only one coordinate exists. 
		double tStart = Math.min(c1.getCoordinate(0), c0.getCoordinate(0));
		double tDiff = Math.abs(c1.getCoordinate(0) - c0.getCoordinate(0));
		double f1 = c0.getCoordinate(1);
		double f2 = c1.getCoordinate(1);
//		System.out.printf("Convert mark start %s len %3.1f fRange %3.1f to %3.1f\n",
//				PamCalendar.formatDateTime((long) tStart), tDiff, f1, f2);
		
		int channel = PamUtils.getSingleChannel(overlayMark.getMarkChannels()); 
		
		MouseEvent swingMouse = ExtMouseAdapter.swingMouse(mouseEvent);
		return spectrogramMarkObserver.spectrogramNotification(spec, swingMouse, downUp, channel, (long) tStart, (long) tDiff, f1, f2, tdDisp);
	}

	@Override
	public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData) {
		return null;
	}

	@Override
	public ParameterType[] getRequiredParameterTypes() {
		return parameterTypes;
	}

	@Override
	public String getObserverName() {
		return spectrogramMarkObserver.getMarkObserverName();
	}

	/**
	 * @return the spectrogramMarkObserver
	 */
	protected SpectrogramMarkObserver getSpectrogramMarkObserver() {
		return spectrogramMarkObserver;
	}


	@Override
	public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMarkName() {
		
		return spectrogramMarkObserver.getMarkName();
	}

}
