package annotationMark.spectrogram;

import java.awt.Component;
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
import PamguardMVC.PamDataUnit;
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
import detectiongrouplocaliser.DetectionGroupSummary;
import generalDatabase.DBControlUnit;
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

	public SpectrogramAnnotationModule(String unitName) {
		super(unitName);
		annotationHandler = new SpectrogramMarkAnnotationHandler(this, getAnnotationDataBlock());
		getAnnotationDataBlock().setAnnotationHandler(annotationHandler);
		annotationHandler.addAnnotationType(snrAnnotationType = new SNRAnnotationType());
		annotationHandler.addAnnotationType(splAnnotationType =new SPLAnnotationType());
		annotationHandler.addAnnotationType(stringAnnotationType = new StringAnnotationType("Note", 50));
		annotationHandler.addAnnotationType(labelAnnotationType = new StringAnnotationType("Label", 50));
		annotationHandler.addAnnotationType(new UserFormAnnotationType());
		//		spectrogramMarkObserver = new SpecMarkObserver();
		//		SpectrogramMarkObservers.addSpectrogramMarkObserver(spectrogramMarkObserver);

		PamSettingManager.getInstance().registerSettings(this);
		OverlayMarkObservers.singleInstance().addObserver(displayObserver = new DisplayObserver());

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
		return super.removeUnit();
	}
	
	protected boolean manualAnnotate(MarkDataUnit adu, Point locOnScreen) {
		return MarkAnnotationDialog.showDialog(getGuiFrame(), this, adu, locOnScreen);
	}

	public class DisplayObserver implements OverlayMarkObserver {

		private final ParameterType[] parameterTypes = {ParameterType.TIME, ParameterType.FREQUENCY};
		private MarkDataUnit existingUnit;
		private int dragEdge;
		private double dragStartFreq;
		private long dragStartTime;
		private MouseButton startButton;

		@Override
		public boolean markUpdate(int markStatus, javafx.scene.input.MouseEvent mouseEvent, OverlayMarker overlayMarker,
				OverlayMark overlayMark) {
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
			Component swingDisplay = swingMouse.getComponent();
//			System.out.printf("Mark status %d, chan %d, t %s f%3.1f, unit %s\n", 
//					markStatus, markChannel, PamCalendar.formatDateTime(t0), f0, existingUnit);
			if (mouseEvent.isPopupTrigger() && existingUnit != null) {
				JPopupMenu pop = getPopupMenu(swingMouse, existingUnit);
				//* Must find a way of doing this as fx or swing - there is one somewhere !
				pop.show(swingMouse.getComponent(), swingMouse.getX(), swingMouse.getY());
				return true;
			}
			if (markStatus == MARK_START) {
				startButton = mouseButton;
				existingUnit = findAnnotationUnit(markChannel, t0, f0);
				if (existingUnit != null && mouseButton == MouseButton.PRIMARY){
					dragEdge = findDragEdge(existingUnit, t0, f0);
					if (dragEdge >= 0) {
						if (swingDisplay != null) {
							swingDisplay.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
						}
						dragStartFreq = f0;
						dragStartTime = t0;
//						System.out.println("Found draggable edge " + dragEdge);
//						System.out.println("Hide mark " + overlayMark.toString());
						overlayMark.setHidden(true);
					}
				}
				else {
					dragEdge = -1;
				}
			}
			else if (markStatus == MARK_END) {
				if (dragEdge >= 0 && existingUnit != null) {
					// update the SNR measurement
					snrAnnotationType.autoAnnotate(existingUnit);
					splAnnotationType.autoAnnotate(existingUnit);
					getAnnotationDataBlock().updatePamData(existingUnit, System.currentTimeMillis());
				}
				dragEdge = -1;
				overlayMark.repaintOwner();
//				if (swingDisplay != null) {
//					swingDisplay.repaint(0);
//				}
				if (swingDisplay != null) {
					swingDisplay.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
			if (existingUnit != null && dragEdge >= 0 && markStatus == MARK_UPDATE && startButton == MouseButton.PRIMARY) {
				overlayMark.setHidden(true);
				// work out which time and freq have changed. 
				long dragT = t2;
				if (dragT == dragStartTime) dragT = t0;
				double dragF = f2;
				if (dragF == dragStartFreq) dragF = f0;
				//				SpectrogramAnnotation dA = (SpectrogramAnnotation) existingUnit.findDataAnnotation(SpectrogramAnnotation.class);
				switch (dragEdge) {
				case 0:
					existingUnit.setDurationInMilliseconds((float) (existingUnit.getDurationInMilliseconds() - (dragT-existingUnit.getTimeMilliseconds())));
					existingUnit.setTimeMilliseconds(dragT);
//					System.out.println("Set start to " + PamCalendar.formatDateTime(dragT));
					break;
				case 1:
					existingUnit.setDurationInMilliseconds(dragT-existingUnit.getTimeMilliseconds());
//					System.out.println("Set Duration to " + existingUnit.getDurationInMilliseconds());
					break;
				case 2:
					existingUnit.getFrequency()[0] = dragF;
//					System.out.println("Set f0 to " + FrequencyFormat.formatFrequency(dragF, true));
					break;
				case 3:
					existingUnit.getFrequency()[1] = dragF;
//					System.out.println("Set f1 to " + FrequencyFormat.formatFrequency(dragF, true));
					break;
				}
				overlayMark.repaintOwner();
//				if (swingDisplay != null) {
//					swingDisplay.repaint(100);
//				}
//				dragEdge(existingUnit, t0, t2, f0, f2);
				return true;
			}
			if (existingUnit == null && markStatus == MARK_END && startButton == MouseButton.PRIMARY) {
				if (t2-t0 == 0 || f2 == f0) return false;
				//				SpectrogramAnnotation an = new SpectrogramAnnotation(SpectrogramAnnotationType.this);
				MarkDataUnit adu = new MarkDataUnit(Math.min(t0, t2), 1<<markChannel, Math.abs(t2-t0));
				// set the datablock now since it's needed in some calculations. 
				adu.setParentDataBlock(getAnnotationDataBlock());
				double[] fRange = new double[] {Math.min(f0, f2), Math.max(f0,  f2)};
				adu.setFrequency(fRange);
				adu.setChannelBitmap(1<<markChannel);
				boolean store = true;
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
					store = manualAnnotate(adu, locOnScreen);
				}
				if (store == true) {
					getAnnotationDataBlock().addPamData(adu);
					getAnnotationDataBlock().sortData();
				}
				overlayMark.repaintOwner();
//				if (swingDisplay != null) {
//					swingDisplay.repaint(0);
//				}
				return true;
			}

			return false;
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
	class SpecMarkObserver implements SpectrogramMarkObserver {

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
