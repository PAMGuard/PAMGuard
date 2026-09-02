package annotationMark.spectrogram;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.AcousticSQLLogging;
import PamUtils.PamUtils;
import PamView.GeneralProjector.ParameterType;
import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.MarkRelationships;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarkObservers;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import Spectrogram.SpectrogramDisplay;
import Spectrogram.SpectrogramMarkObserver;
import annotation.AnnotationDialog;
import annotation.DataAnnotationType;
import annotation.calcs.snr.SNRAnnotationType;
import annotation.calcs.spl.SPLAnnotationType;
import annotation.handler.AnnotationChoices;
import annotation.string.StringAnnotationType;
import annotation.userforms.UserFormAnnotationType;
import annotationMark.MarkAnnotationDialog;
import annotationMark.MarkDataBlock;
import annotationMark.MarkDataUnit;
import annotationMark.MarkModule;
import annotationMark.MarkSQLLogging;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.overlaymark.StandardOverlayMarker;
import dataPlotsFX.projector.TDProjectorFX;
import detectiongrouplocaliser.DetectionGroupSummary;
import generalDatabase.DBControlUnit;
import detectionPlotFX.data.DDPlotRegister;
import javafx.application.Platform;
import javafx.scene.input.MouseButton;

public class SpectrogramAnnotationModule extends MarkModule implements PamSettings {

	protected StringAnnotationType stringAnnotationType;
	protected SNRAnnotationType snrAnnotationType;
	protected StringAnnotationType labelAnnotationType;
	protected SPLAnnotationType splAnnotationType;

	//	private SpecMarkObserver spectrogramMarkObserver;

	private SpectrogramMarkParams specMarkParams = new SpectrogramMarkParams();
	protected SpectrogramMarkAnnotationHandler annotationHandler;
	protected DisplayObserver displayObserver;
	private SpectrogramAnnotationDDPlotProvider ddPlotProvider;

	public SpectrogramAnnotationModule(String unitName) {
		super(unitName);
		annotationHandler = new SpectrogramMarkAnnotationHandler(this, getAnnotationDataBlock());
		getAnnotationDataBlock().setAnnotationHandler(annotationHandler);
		annotationHandler.addAnnotationType(snrAnnotationType = new SNRAnnotationType());
		annotationHandler.addAnnotationType(splAnnotationType =new SPLAnnotationType());
		annotationHandler.addAnnotationType(stringAnnotationType = new StringAnnotationType("Note", 50));
		annotationHandler.addAnnotationType(labelAnnotationType = new StringAnnotationType("Label", 50));
		annotationHandler.addAnnotationType(new UserFormAnnotationType(getAnnotationProcess().getMarkDataBlock()));
		//		spectrogramMarkObserver = new SpecMarkObserver();
		//		SpectrogramMarkObservers.addSpectrogramMarkObserver(spectrogramMarkObserver);

		PamSettingManager.getInstance().registerSettings(this);
		OverlayMarkObservers.singleInstance().addObserver(displayObserver = new DisplayObserver());

		//register a detection display provider so the advanced overlay mark pop up menu can
		//show a spectrogram of the raw data underneath the annotation.
		DDPlotRegister.getInstance().registerDataInfo(
				ddPlotProvider = new SpectrogramAnnotationDDPlotProvider(getAnnotationDataBlock()));

		annotationHandler.loadAnnotationChoices();
		sortSQLLogging();
		subscribeMarkObserver();
	}

	/**
	 * Sort out the SQLLogging, this requires the creation of a new base 
	 * table definition, then adding the SQLLogging to it. 
	 */
	protected void sortSQLLogging() {
		MarkDataBlock dataBlock = this.getAnnotationDataBlock();
		//in this case the base logging is a straight up acouistic table
		AcousticSQLLogging acousticSQLLogging = new MarkSQLLogging(dataBlock, getUnitName());
		dataBlock.SetLogging(acousticSQLLogging);
		if (annotationHandler.addAnnotationSqlAddons(acousticSQLLogging) > 0) {
			DBControlUnit dbc = DBControlUnit.findDatabaseControl();
			if (dbc != null) {
				dbc.getDbProcess().checkTable(acousticSQLLogging.getTableDefinition());
			}
		}
	}

	protected void subscribeMarkObserver() {
		MarkRelationships markRelationships = MarkRelationships.getInstance();
		markRelationships.subcribeToMarkers(displayObserver);		
	}
	//	public SNRAnnotationType getSnrAnnotationType() {
	//		return snrAnnotationType;
	//	}


	@Override
	public boolean removeUnit() {
		//		SpectrogramMarkObservers.removeSpectrogramMarkObserver(spectrogramMarkObserver);
		OverlayMarkObservers.singleInstance().removeObserver(displayObserver);
		if (ddPlotProvider != null) {
			DDPlotRegister.getInstance().unRegisterDataInfo(ddPlotProvider);
		}
		return super.removeUnit();
	}
	
	/**
	 * Show the modal annotation dialog. This MUST be called on the Swing EDT (see
	 * {@link #showAnnotationDialogAndStore}) - never on the JavaFX thread.
	 * <p>
	 * If the user has switched off {@link SpectrogramMarkParams#isShowDialogOnNewMark()}
	 * the dialog is skipped and the annotation is kept with whatever the automatic
	 * annotation types have already measured.
	 */
	protected boolean manualAnnotate(MarkDataUnit adu, Point locOnScreen) {
		if (!specMarkParams.isShowDialogOnNewMark()) {
			return true;
		}
		return MarkAnnotationDialog.showDialog(getGuiFrame(), this, adu, locOnScreen);
	}

	/**
	 * Is the "show annotation dialog" option offered in this module's settings
	 * dialog? Sub classes which control the popup themselves (the Quick Annotation
	 * module has its own control in its settings dialog and side panel) return
	 * false so that the same option isn't presented twice.
	 *
	 * @return true if the option should be shown in {@link SpectrogramMarkDialog}
	 */
	public boolean hasShowDialogOption() {
		return true;
	}

	/**
	 * @return the mark parameters, which hold the annotation choices and the
	 *         automatic dialog option.
	 */
	public SpectrogramMarkParams getSpecMarkParams() {
		return specMarkParams;
	}

	/**
	 * Show the manual annotation dialog and, if the user accepts, store the new annotation.
	 * <p>
	 * {@code markUpdate(...)} is called on the JavaFX Application Thread when the mark is made
	 * on a TimeDisplayFX, which lives inside a {@code JFXPanel}. {@link MarkAnnotationDialog}
	 * is a <b>modal</b> Swing dialog. Blocking the FX thread to wait for a modal Swing dialog
	 * (e.g. with {@code invokeAndWait}) deadlocks the two toolkits and hangs PAMGuard, so this
	 * is fire-and-forget: the dialog is shown on the Swing EDT and the annotation is stored
	 * from the dialog's own thread once the user is done. The mark repaint is bounced back to
	 * the FX thread because {@code repaintOwner()} drives a JavaFX {@code Timeline}.
	 *
	 * @param adu         the new annotation data unit
	 * @param locOnScreen screen location for the dialog
	 * @param overlayMark the mark to repaint once done (may be null)
	 */
	private void showAnnotationDialogAndStore(MarkDataUnit adu, Point locOnScreen, OverlayMark overlayMark) {
		if (SwingUtilities.isEventDispatchThread()) {
			// Swing display: already on the EDT. Show the modal dialog inline (it blocks the
			// EDT as a modal dialog normally would) and repaint the Swing mark source directly.
			doAnnotationDialog(adu, locOnScreen, overlayMark, false);
		}
		else {
			// FX display (mark made on a JFXPanel-hosted TimeDisplayFX): never block the FX
			// thread. Show the dialog on the EDT and bounce the FX repaint back to the FX thread.
			SwingUtilities.invokeLater(() -> doAnnotationDialog(adu, locOnScreen, overlayMark, true));
		}
	}

	/**
	 * Show the modal annotation dialog (must be on the EDT), store the annotation if accepted
	 * and repaint the mark.
	 *
	 * @param repaintOnFXThread true if the mark is owned by an FX display, so its repaint
	 *            (which drives a JavaFX {@code Timeline}) must be bounced to the FX thread;
	 *            false for a Swing mark source, whose {@code repaint()} is safe on the EDT.
	 */
	private void doAnnotationDialog(MarkDataUnit adu, Point locOnScreen, OverlayMark overlayMark, boolean repaintOnFXThread) {
		boolean store = manualAnnotate(adu, locOnScreen);
		if (store) {
			getAnnotationDataBlock().addPamData(adu);
			getAnnotationDataBlock().sortData();
		}
		if (overlayMark != null) {
			if (repaintOnFXThread) {
				Platform.runLater(() -> overlayMark.repaintOwner());
			}
			else {
				overlayMark.repaintOwner();
			}
		}
	}

	public class DisplayObserver implements OverlayMarkObserver {

		private final ParameterType[] parameterTypes = {ParameterType.TIME, ParameterType.FREQUENCY};
		private MarkDataUnit existingUnit;
		private MouseButton startButton;

		// Grab state for interactive resize / move of an existing box. moveMode moves
		// the whole box; the dragTx / dragFx flags mark which edges are being dragged
		// (a corner sets one time and one frequency edge). The orig* fields hold the
		// box geometry when the grab started, so drags are computed relative to it.
		private boolean moveMode;
		private boolean dragT0, dragT1, dragF0, dragF1;
		private long origTime;
		private double origDur;
		private double origF0, origF1;
		private long grabTime;
		private double grabFreq;

		@Override
		public boolean markUpdate(int markStatus, javafx.scene.input.MouseEvent mouseEvent, OverlayMarker overlayMarker,
				OverlayMark overlayMark) {
			if (markStatus == MARK_CANCELLED) {
				// mark abandoned (overlayMark may be null) - clear any live measurement text.
				if (isGrabbing()) {
					AnnotationHandleState.clearEditing();
					resetGrab();
					if (overlayMark != null) {
						overlayMark.repaintOwner();
					}
				}
				return false;
			}
			if (overlayMark == null) {
				return false;
			}
			int markChannels = overlayMark.getMarkChannels();
			int markChannel = PamUtils.getLowestChannel(markChannels);
			long t0 = (long) overlayMark.getCoordinate(0).getCoordinate(0);
			double f0 =  overlayMark.getCoordinate(0).getCoordinate(1);
			long t2 = (long) overlayMark.getLastCoordinate().getCoordinate(0);
			double f2 = overlayMark.getLastCoordinate().getCoordinate(1);
			MouseButton mouseButton = mouseEvent.getButton();
			MouseEvent swingMouse = ExtMouseAdapter.swingMouse(mouseEvent);
			if (mouseEvent.isPopupTrigger() && existingUnit != null) {
				// markUpdate may be on the JavaFX thread; show the Swing popup on the EDT.
				final MarkDataUnit popupUnit = existingUnit;
				SwingUtilities.invokeLater(() -> {
					JPopupMenu pop = getPopupMenu(swingMouse, popupUnit);
					pop.show(swingMouse.getComponent(), swingMouse.getX(), swingMouse.getY());
				});
				return true;
			}
			if (markStatus == MARK_START) {
				startButton = mouseButton;
				resetGrab();
				existingUnit = findAnnotationUnit(markChannel, t0, f0);
				if (existingUnit == null) {
					// the press may be on a resize handle that sits just outside the box outline.
					existingUnit = findAnnotationUnitNear(markChannel, t0, f0, overlayMarker);
				}
				if (existingUnit != null && mouseButton == MouseButton.PRIMARY){
					determineGrab(existingUnit, t0, f0, overlayMarker);
					if (isGrabbing()) {
						// remember the starting geometry so drags are relative to it.
						origTime = existingUnit.getTimeMilliseconds();
						origDur = existingUnit.getDurationInMilliseconds();
						double[] fr = existingUnit.getFrequency();
						origF0 = fr[0];
						origF1 = fr[1];
						grabTime = t0;
						grabFreq = f0;
						overlayMark.setHidden(true);
					}
				}
			}
			else if (markStatus == MARK_UPDATE && isGrabbing() && existingUnit != null
					&& startButton == MouseButton.PRIMARY) {
				overlayMark.setHidden(true);
				applyDrag(existingUnit, t2, f2);
				// show live measurements inside the box while it is being changed.
				AnnotationHandleState.setEditing(existingUnit,
						computeMeasurementText(existingUnit));
				overlayMark.repaintOwner();
				return true;
			}
			else if (markStatus == MARK_END) {
				if (isGrabbing() && existingUnit != null) {
					// recompute measurements now the box is in its final position.
					snrAnnotationType.autoAnnotate(existingUnit);
					splAnnotationType.autoAnnotate(existingUnit);
					getAnnotationDataBlock().updatePamData(existingUnit, System.currentTimeMillis());
					// drag finished - remove the live measurement text.
					AnnotationHandleState.clearEditing();
					resetGrab();
					overlayMark.repaintOwner();
					return true;
				}
				resetGrab();
			}
			if (existingUnit == null && markStatus == MARK_END && startButton == MouseButton.PRIMARY) {
				if (t2-t0 == 0 || f2 == f0) return false;
				//				SpectrogramAnnotation an = new SpectrogramAnnotation(SpectrogramAnnotationType.this);
				final MarkDataUnit adu = new MarkDataUnit(Math.min(t0, t2), 1<<markChannel, Math.abs(t2-t0));
				// set the datablock now since it's needed in some calculations.
				adu.setParentDataBlock(getAnnotationDataBlock());
				double[] fRange = new double[] {Math.min(f0, f2), Math.max(f0,  f2)};
				adu.setFrequency(fRange);
				adu.setChannelBitmap(1<<markChannel);
				List<DataAnnotationType<?>> anTypes = annotationHandler.getUsedAnnotationTypes();
				boolean manualAnnotationNeeded = false;
				Point locOnScreen = null;
				for (DataAnnotationType anType:anTypes) {
					if (anType.canAutoAnnotate()) {
						anType.autoAnnotate(adu);
					}
					else {
						manualAnnotationNeeded  = true;
						locOnScreen = new Point((int)mouseEvent.getScreenX(), (int)mouseEvent.getScreenY());
					}
				}
				if (manualAnnotationNeeded) {
					// Needs a modal Swing dialog: show it on the EDT and store from there, so
					// we never block the JavaFX thread (which would deadlock - see method doc).
					showAnnotationDialogAndStore(adu, locOnScreen, overlayMark);
					return true;
				}
				getAnnotationDataBlock().addPamData(adu);
				getAnnotationDataBlock().sortData();
				overlayMark.repaintOwner();
//				if (swingDisplay != null) {
//					swingDisplay.repaint(0);
//				}
				return true;
			}

			return false;
		}

		private boolean isGrabbing() {
			return moveMode || dragT0 || dragT1 || dragF0 || dragF1;
		}

		private void resetGrab() {
			moveMode = false;
			dragT0 = dragT1 = dragF0 = dragF1 = false;
		}

		/**
		 * Find an annotation unit whose box - grown by the resize-handle tolerance -
		 * contains the press point. This lets the user grab the little handle squares
		 * that sit just outside the annotation box outline (where the box itself does
		 * not cover the press point).
		 */
		private MarkDataUnit findAnnotationUnitNear(int channel, long t0, double f0, OverlayMarker overlayMarker) {
			if (!(overlayMarker instanceof StandardOverlayMarker)) {
				return null;
			}
			TDGraphFX graph = ((StandardOverlayMarker) overlayMarker).getTdGraphFX();
			TDProjectorFX proj = graph.getGraphProjector();
			double ss = graph.getScrollStart();
			double px = proj.getTimePix(t0 - ss);
			double py = proj.getYPix(f0);
			double tol = 8;
			MarkDataUnit found = null;
			ListIterator<MarkDataUnit> it = getAnnotationDataBlock().getListIterator(0);
			while (it.hasNext()) {
				MarkDataUnit unit = it.next();
				if ((unit.getChannelBitmap() & 1 << channel) == 0) {
					continue;
				}
				double[] fr = unit.getFrequency();
				double xa = proj.getTimePix(unit.getTimeMilliseconds() - ss);
				double xb = proj.getTimePix(unit.getEndTimeInMilliseconds() - ss);
				double xL = Math.min(xa, xb), xR = Math.max(xa, xb);
				double yc = proj.getYPix(fr[0]);
				double yd = proj.getYPix(fr[1]);
				double yT = Math.min(yc, yd), yBot = Math.max(yc, yd);
				if (px >= xL - tol && px <= xR + tol && py >= yT - tol && py <= yBot + tol) {
					found = unit; // keep scanning: later units are drawn on top.
				}
			}
			return found;
		}

		/**
		 * Work out what part of an existing box the user has grabbed: an edge, a corner
		 * (two edges at once) or - if not near any edge - the middle for a move. The
		 * press point has already been confirmed to be inside the box (via
		 * {@link #findAnnotationUnit}) so this always results in a grab; if no edge is
		 * hit the whole box is moved.
		 */
		private void determineGrab(MarkDataUnit unit, long t0, double f0, OverlayMarker overlayMarker) {
			resetGrab();
			double[] fr = unit.getFrequency();
			long start = unit.getTimeMilliseconds();
			long end = unit.getEndTimeInMilliseconds();

			if (overlayMarker instanceof StandardOverlayMarker) {
				// pixel-accurate edge detection so the grab zones match the drawn handles.
				TDGraphFX graph = ((StandardOverlayMarker) overlayMarker).getTdGraphFX();
				TDProjectorFX proj = graph.getGraphProjector();
				double ss = graph.getScrollStart();
				double xL = proj.getTimePix(start - ss);
				double xR = proj.getTimePix(end - ss);
				double yLow = proj.getYPix(fr[0]);
				double yHigh = proj.getYPix(fr[1]);
				double px = proj.getTimePix(t0 - ss);
				double py = proj.getYPix(f0);
				double tol = 8;
				dragT0 = Math.abs(px - xL) <= tol; // left edge = start time
				dragT1 = Math.abs(px - xR) <= tol; // right edge = end time
				dragF0 = Math.abs(py - yLow) <= tol; // low-frequency edge
				dragF1 = Math.abs(py - yHigh) <= tol; // high-frequency edge
			}
			else {
				// fallback (no FX projector available): detect edges in data coordinates.
				double dur = Math.max(1, end - start);
				double df = Math.max(1e-9, fr[1] - fr[0]);
				double frac = 0.15;
				dragT0 = (t0 - start) / dur < frac;
				dragT1 = (end - t0) / dur < frac;
				dragF0 = (f0 - fr[0]) / df < frac;
				dragF1 = (fr[1] - f0) / df < frac;
			}

			if (!dragT0 && !dragT1 && !dragF0 && !dragF1) {
				moveMode = true; // not near an edge - move the whole box.
			}
		}

		/**
		 * Apply the current drag to the box, computing the new geometry relative to the
		 * geometry saved when the grab started.
		 */
		private void applyDrag(MarkDataUnit unit, long t2, double f2) {
			long origEnd = origTime + (long) origDur;
			long newStart = origTime;
			long newEnd = origEnd;
			double newF0 = origF0;
			double newF1 = origF1;
			if (moveMode) {
				long dt = t2 - grabTime;
				double df = f2 - grabFreq;
				newStart = origTime + dt;
				newEnd = origEnd + dt;
				newF0 = origF0 + df;
				newF1 = origF1 + df;
			}
			else {
				if (dragT0) newStart = t2;
				if (dragT1) newEnd = t2;
				if (dragF0) newF0 = f2;
				if (dragF1) newF1 = f2;
			}
			if (newEnd < newStart) {
				long tmp = newStart; newStart = newEnd; newEnd = tmp;
			}
			if (newF1 < newF0) {
				double tmp = newF0; newF0 = newF1; newF1 = tmp;
			}
			unit.setTimeMilliseconds(newStart);
			unit.setDurationInMilliseconds((float) (newEnd - newStart));
			unit.setFrequency(new double[] { newF0, newF1 });
		}

		/**
		 * Build the live measurement text shown inside a box while it is being resized
		 * or moved: duration, frequency range and - if the raw audio is available -
		 * RMS and peak-to-peak amplitude.
		 */
		private String[] computeMeasurementText(MarkDataUnit unit) {
			double[] fr = unit.getFrequency();
			List<String> lines = new java.util.ArrayList<>();
			lines.add(String.format("Dur: %.3f s", unit.getDurationInMilliseconds() / 1000.0));
			lines.add(String.format("Freq: %.0f-%.0f Hz", fr[0], fr[1]));
			double[] rmsPP = measureRawData(unit);
			if (rmsPP != null) {
				lines.add(String.format("RMS: %.4g", rmsPP[0]));
				lines.add(String.format("Pk-Pk: %.4g", rmsPP[1]));
			}
			return lines.toArray(new String[0]);
		}

		/**
		 * Measure the RMS and peak-to-peak amplitude of the raw audio under a box, from
		 * the already-loaded data. Returns {rms, peakToPeak}, or null if the raw data is
		 * not available.
		 */
		private double[] measureRawData(MarkDataUnit unit) {
			try {
				PamRawDataBlock raw = getRawDataBlock();
				if (raw == null) {
					return null;
				}
				long dur = unit.getDurationInMilliseconds().longValue();
				if (dur <= 0) {
					return null;
				}
				int channel = PamUtils.getLowestChannel(unit.getChannelBitmap());
				double[][] samples = raw.getSamplesForMillis(unit.getTimeMilliseconds(), dur, 1 << channel);
				if (samples == null || samples.length == 0 || samples[0].length == 0) {
					return null;
				}
				double[] x = samples[0];
				double sum2 = 0, min = x[0], max = x[0];
				for (double v : x) {
					sum2 += v * v;
					if (v < min) min = v;
					if (v > max) max = v;
				}
				return new double[] { Math.sqrt(sum2 / x.length), max - min };
			}
			catch (Exception e) {
				return null; // raw data not available for this window - just skip the measurement.
			}
		}

		/**
		 * @return the first raw (acquisition) data block, or null.
		 */
		private PamRawDataBlock getRawDataBlock() {
			java.util.ArrayList<PamDataBlock> raws = PamController.getInstance().getRawDataBlocks();
			if (raws == null || raws.isEmpty()) {
				return null;
			}
			PamDataBlock block = raws.get(0);
			return (block instanceof PamRawDataBlock) ? (PamRawDataBlock) block : null;
		}

		@Override
		public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData) {
			MarkDataUnit markDataUnit = getMarkDataUnit(markSummaryData);
			if (markDataUnit == null) {
				return null;
			}
			else {
				MouseEvent swingMouse = ExtMouseAdapter.swingMouse(markSummaryData.getMouseEvent());
				return getPopupMenu(swingMouse, markDataUnit);
			}
		}

		public MarkDataUnit getMarkDataUnit(DetectionGroupSummary markSummaryData) {
			if (markSummaryData == null) {
				return null;
			}
			List<PamDataUnit> dataList = markSummaryData.getDataList();
			if (dataList == null) {
				return null;
			}
			for (PamDataUnit dataUnit:dataList) {
				if (dataUnit instanceof MarkDataUnit) {
					return (MarkDataUnit) dataUnit;
				}
			}
			return null;
		}

		@Override
		public ParameterType[] getRequiredParameterTypes() {
			return parameterTypes;
		}

		@Override
		public String getObserverName() {
			return getUnitName();
		}

		@Override
		public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getMarkName() {
			
			return getMarkType();
		}

	}
	private class SpecMarkObserver implements SpectrogramMarkObserver {

		private MarkDataUnit existingUnit;
		private int dragEdge;
		private double dragStartFreq;
		private long dragStartTime;

		@Override
		public boolean spectrogramNotification(SpectrogramDisplay display, MouseEvent mouseEvent,
				int downUp, int channel, long startMilliseconds, long duration,
				double f1, double f2, TDGraphFX tdDisplay) {
			//			System.out.println(String.format("Spec mark %d chan %d, start %s len %3.1fs, Freq: %s",
			//					downUp, channel, PamCalendar.formatDateTime(startMilliseconds), (double) duration/1000., 
			//					FrequencyFormat.formatFrequencyRange(new double[] {f1,  f2}, true)));
			int event = downUp & 0xFF;
			if (mouseEvent.isPopupTrigger()) {
				existingUnit = findAnnotationUnit(channel, startMilliseconds, f1);
				if (existingUnit != null) {
					showMarkPopup(mouseEvent, existingUnit);
					return true;
				}
			}
			else if (event == SpecMarkObserver.MOUSE_DOWN) {
				existingUnit = findAnnotationUnit(channel, startMilliseconds, f1);
				if (existingUnit != null && mouseEvent.getButton() == MouseEvent.BUTTON1) {
					dragEdge = findDragEdge(existingUnit, startMilliseconds, f1);
					if (dragEdge >= 0) {
						mouseEvent.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
						dragStartFreq = f1;
						dragStartTime = startMilliseconds;
					}
				}
				else {
					dragEdge = -1;
				}
			}
			if (event == SpecMarkObserver.MOUSE_UP) {
				mouseEvent.getComponent().setCursor(Cursor.getDefaultCursor());
				if (dragEdge >= 0 && existingUnit != null) {
					// update the SNR measurement
					snrAnnotationType.autoAnnotate(existingUnit);
					getAnnotationDataBlock().updatePamData(existingUnit, System.currentTimeMillis());
				}
				dragEdge = -1;
				if (display != null) {
					display.repaint(0);
				}
			}
			if (existingUnit != null && dragEdge >= 0 && event == SpecMarkObserver.MOUSE_DRAG) {
				// work out which time and freq have changed. 
				long dragT = startMilliseconds;
				if (dragT == dragStartTime) dragT = startMilliseconds + duration;
				double dragF = f1;
				if (dragF == dragStartFreq) dragF = f2;
				//				SpectrogramAnnotation dA = (SpectrogramAnnotation) existingUnit.findDataAnnotation(SpectrogramAnnotation.class);
				switch (dragEdge) {
				case 0:
					existingUnit.setDurationInMilliseconds((float) (existingUnit.getDurationInMilliseconds() - (dragT-existingUnit.getTimeMilliseconds())));
					existingUnit.setTimeMilliseconds(dragT);
					break;
				case 1:
					existingUnit.setDurationInMilliseconds(dragT-existingUnit.getTimeMilliseconds());
					break;
				case 2:
					existingUnit.getFrequency()[0] = dragF;
					break;
				case 3:
					existingUnit.getFrequency()[1] = dragF;
					break;
				}
				mouseEvent.getComponent().repaint(100);
				//				dragEdge(existingUnit, startMilliseconds, startMilliseconds+duration, f1, f2)
			}
			if (existingUnit == null && downUp == SpecMarkObserver.MOUSE_UP) {
				if (duration == 0 || f2 == f1) return false;
				//				SpectrogramAnnotation an = new SpectrogramAnnotation(SpectrogramAnnotationType.this);
				MarkDataUnit adu = new MarkDataUnit(startMilliseconds, 1<<channel, duration);
				// set the datablock now since it's needed in some calculations. 
				adu.setParentDataBlock(getAnnotationDataBlock());
				double[] fRange = new double[] {Math.min(f1, f2), Math.max(f1,  f2)};
				adu.setFrequency(fRange);
				adu.setChannelBitmap(1<<channel);
				boolean store = true;
				List<DataAnnotationType<?>> anTypes = annotationHandler.getUsedAnnotationTypes();
				for (DataAnnotationType anType:anTypes) {
					if (anType.canAutoAnnotate()) {
						anType.autoAnnotate(adu);
					}
					else {
						Point locOnScreen = mouseEvent.getLocationOnScreen();
						boolean ans = AnnotationDialog.showDialog(getGuiFrame(), anType, adu, locOnScreen);
						if (ans == false) {
							store = false;
							break;
						}
					}
				}
				//				getSnrAnnotationType().autoAnnotate(adu);
				//				boolean ans = MarkAnnotationDialog.showDialog(SwingUtilities.getWindowAncestor(mouseEvent.getComponent()), 
				//						SpectrogramAnnotationModule.this, adu);
				if (store == true) {
					getAnnotationDataBlock().addPamData(adu);
					getAnnotationDataBlock().sortData();
				}
				if (display != null) {
					display.repaint(0);
				}
			}
			return false;
		}

		@Override
		public String getMarkObserverName() {
			return getUnitName();
		}

		@Override
		public boolean canMark() {
			return true;
		}

		private void showMarkPopup(MouseEvent mouseEvent, MarkDataUnit existingUnit) {
			JPopupMenu pop = getPopupMenu(mouseEvent, existingUnit);
			pop.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
		}

		@Override
		public String getMarkName() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private JPopupMenu getPopupMenu(MouseEvent mouseEvent, MarkDataUnit markDataUnit) {
		JPopupMenu pop = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem("Edit Annotation");
		menuItem.addActionListener(new EditUnit(mouseEvent ,markDataUnit));
		pop.add(menuItem);
		menuItem = new JMenuItem("Delete");
		menuItem.addActionListener(new DeleteUnit(mouseEvent, markDataUnit));
		pop.add(menuItem);
		return pop;
	}

	public String getMarkType() {
		return null;
	}

	class DeleteUnit implements ActionListener {
		MarkDataUnit dataUnit;
		private MouseEvent mouseEvent;
		public DeleteUnit(MouseEvent mouseEvent, MarkDataUnit dataUnit) {
			super();
			this.mouseEvent = mouseEvent;
			this.dataUnit = dataUnit;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String msg = dataUnit.getSummaryString();
			int ans = JOptionPane.showConfirmDialog(mouseEvent.getComponent(), 
					msg, "Confirm Delete", JOptionPane.YES_NO_OPTION);
			if (ans == JOptionPane.YES_OPTION) {
				getAnnotationDataBlock().remove(dataUnit, true);
			}
			mouseEvent.getComponent().repaint();
		}
	}	
	class EditUnit implements ActionListener {
		MarkDataUnit dataUnit;
		private MouseEvent mouseEvent;
		public EditUnit(MouseEvent mouseEvent, MarkDataUnit dataUnit) {
			super();
			this.mouseEvent = mouseEvent;
			this.dataUnit = dataUnit;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			boolean ans = MarkAnnotationDialog.showDialog(SwingUtilities.getWindowAncestor(mouseEvent.getComponent()), 
					SpectrogramAnnotationModule.this, dataUnit, null);
			if (ans) {
				getAnnotationDataBlock().updatePamData(dataUnit, System.currentTimeMillis());
			}
			mouseEvent.getComponent().repaint();
		}
	}	


	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsMenu(parentFrame);
			}
		});
		return menuItem;
	}

	public void showSettingsMenu(Frame parentFrame) {
		boolean ans = SpectrogramMarkDialog.showDialog(parentFrame, this);
		if (ans) {
			annotationHandler.loadAnnotationChoices();
			sortSQLLogging();
			subscribeMarkObserver();
		}
	}

	/**
	 * Find an existing annotation data unit. 
	 * @param channel
	 * @param startMilliseconds
	 * @param f1
	 * @return existing unit, or null. 
	 */
	public MarkDataUnit findAnnotationUnit(int channel, long startMilliseconds, double f1) {
		ListIterator<MarkDataUnit> anIt = getAnnotationDataBlock().getListIterator(0);
		while (anIt.hasNext()) {
			MarkDataUnit aUnit = anIt.next();
			if (startMilliseconds < aUnit.getTimeMilliseconds()) continue;
			if ((aUnit.getChannelBitmap() & 1<<channel) == 0) continue;
			//			SpectrogramAnnotation specAnnotation = (SpectrogramAnnotation) aUnit.findDataAnnotation(SpectrogramAnnotation.class);
			//			if (specAnnotation == null) continue;
			if (startMilliseconds > aUnit.getEndTimeInMilliseconds()) continue;
			if (f1 < aUnit.getFrequency()[0] || f1 > aUnit.getFrequency()[1]) continue;
			return aUnit;
		}
		return null;
	}

	private int findDragEdge(MarkDataUnit existingUnit, long startMilliseconds, double f1) {
		//		SpectrogramAnnotation sa = (SpectrogramAnnotation) existingUnit.findDataAnnotation(SpectrogramAnnotation.class);
		double[] edgedist = new double[4];
		edgedist[0] = (startMilliseconds - existingUnit.getTimeMilliseconds()) / 
				(double) (existingUnit.getDurationInMilliseconds()+1);
		edgedist[1] = (existingUnit.getEndTimeInMilliseconds() - startMilliseconds) / 
				(double) (existingUnit.getDurationInMilliseconds()+1);
		double[] fR = existingUnit.getFrequency();
		double df = fR[1]-fR[0];
		edgedist[2] = (f1-fR[0]) / df;
		edgedist[3] = (fR[1]-f1) / df;
		int bestEdge = -1;
		double bestMatch = edgedist[0]+1;
		for (int i = 0; i < 4; i++) {
			if (edgedist[i] < bestMatch && edgedist[i] < 0.2) {
				bestEdge = i;
				bestMatch = edgedist[i];
			}
		}

		return bestEdge;
	}

	public AnnotationChoices getAnnotationChoices() {
		return specMarkParams.getAnnotationChoices(annotationHandler);
	}

	/**
	 * @return the annotationHandler
	 */
	public SpectrogramMarkAnnotationHandler getAnnotationHandler() {
		return annotationHandler;
	}

	@Override
	public Serializable getSettingsReference() {
		return specMarkParams;
	}

	@Override
	public long getSettingsVersion() {
		return SpectrogramMarkParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.specMarkParams = ((SpectrogramMarkParams) pamControlledUnitSettings.getSettings());
		return true;
	}

}
