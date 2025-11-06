package clipgenerator.clipDisplay;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import userDisplay.UserDisplayComponentAdapter;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;
import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import PamView.paneloverlay.overlaymark.OverlayMarkProviders;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.SymbolUpdateMonitor;
import PamView.symbol.modifier.SymbolModType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorChangeListener;
import clipgenerator.ClipDataUnit;
import clipgenerator.ClipDisplayDataBlock;
import clipgenerator.ClipProcess;

/**
 * Clip display panel. Can be incorporated into a tab panel or stand alone in 
 * a general display. 
 * @author Doug Gillespie
 *
 */
public class ClipDisplayPanel extends UserDisplayComponentAdapter implements PamSettings, SymbolUpdateMonitor {

	private ClipDisplayParent clipDisplayParent;

	private JPanel displayPanel;

	private JPanel unitsPanel;

	protected ClipDisplayParameters clipDisplayParameters = new ClipDisplayParameters();
	
	private ClipDisplayProjector clipDisplayProjector;
	
	private ClipDisplayMarker clipDisplayMarker;

	private DisplayControlPanel displayControlPanel;

	private float sampleRate;

	private ColourArray colourArray;

	protected Font clipFont;

	private JScrollPane scrollPane;

	private ClipLayout clipLayout;

	private ColourArrayType currentColours = null;

	private ClipDataProjector clipDataProjector;

	private boolean isViewer;

	private HidingPanel hidingPanel;
	
//	private ArrayList<ClipDisplayUnit> selectedClips;
	
	private UnitsMouse unitsMouse;

	public ClipDisplayPanel(ClipDisplayParent clipDisplayParent) {
		super();
		this.clipDisplayParent = clipDisplayParent;

		clipFont = new Font("Arial", Font.PLAIN, 10);
		displayPanel = new ClipMainPanel(new BorderLayout());

		clipDisplayProjector = new ClipDisplayProjector(this);
		
		unitsPanel = new PamPanel(clipLayout = new ClipLayout(FlowLayout.LEFT));
		unitsPanel.addMouseListener(unitsMouse = new UnitsMouse());
		unitsPanel.setToolTipText("Right click for border colour and data selection options");
		clipLayout.setHgap(2);
		clipLayout.setVgap(2);
		//		unitsPanel.setPreferredSize(new Dimension(200, 0));
		//		unitsPanel.setBackground(Color.blue);
		scrollPane = new JScrollPane(unitsPanel, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//		scrollPane.get
		displayPanel.add(BorderLayout.CENTER, scrollPane);

		displayControlPanel = new DisplayControlPanel(clipDisplayParent, this);
		hidingPanel = new HidingPanel(displayPanel, displayControlPanel.getControlPanel(), HidingPanel.HORIZONTAL, true);
		hidingPanel.setTitle("clip viewer controls");
		displayPanel.add(BorderLayout.NORTH, hidingPanel);

		clipDataProjector = new ClipDataProjector(this);

		makeColourArray();

		
		PamDataBlock<ClipDataUnit> dataBlock = clipDisplayParent.getClipDataBlock();
		if (dataBlock != null) {
			dataBlock.addObserver(new ClipObserver());
		}

		PamSettingManager.getInstance().registerSettings(this);
		displayControlPanel.setValues(clipDisplayParameters);
		hidingPanel.showPanel(clipDisplayParameters.showControlPanel);

		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		
//		selectedClips = new ArrayList<>();
		
		clipDisplayMarker = new ClipDisplayMarker(this);
//		clipDisplayMarker.addObserver(new ClipMarkObserver());
		OverlayMarkProviders.singleInstance().addProvider(clipDisplayMarker);
//		OverlayMarkerManager.
	}

	/**
	 * @return the clipDisplayProjector
	 */
	public ClipDisplayProjector getClipDisplayProjector() {
		return clipDisplayProjector;
	}
	
//	private class ClipMarkObserver implements OverlayMarkObserver {
//
//		@Override
//		public boolean markUpdate(int markStatus, javafx.scene.input.MouseEvent mouseEvent, OverlayMarker overlayMarker,
//				OverlayMark overlayMark) {
//			if (markStatus == OverlayM)
//			showAndHideClips();
//			return true;
//		}
//
//		@Override
//		public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public ParameterType[] getRequiredParameterTypes() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public String getObserverName() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public String getMarkName() {
//			return "Select clips";
//		}
//		
//	}

	private class ClipObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return getUniqueName();
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return Math.max(10000, clipDisplayParameters.maxMinutes * 60L * 1000L);
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			newSampleRate(sampleRate);
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
//			
////			TODO FIXME rather than get update count could also check if it's current difar unit. this might have to be done in a difarobserver 
			/**
			 * Add data and update now separated out into two different functions since even with new data, the update count is
			 * sometimes set before the data arrive in every new process. 
			 */
//			if (arg.getUpdateCount() == 0) {
				newDataUnit((ClipDataUnit) arg);
//			}
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			repaintUnits();
			SwingUtilities.invokeLater(new Runnable() {
				// can't do this here or we get a tree lock. 
				@Override
				public void run() {
					showAndHideClips();
				}
			});
		}

	}

	private void newDataUnit(ClipDataUnit clipDataUnit) {
		PamDataUnit triggerDataUnit = findTriggerDataUnit(clipDataUnit);
		clipDataUnit.setTriggerDataUnit(triggerDataUnit);
		ClipDisplayUnit clipDisplayUnit = new ClipDisplayUnit(this, clipDataUnit, triggerDataUnit);
		
		synchronized (unitsPanel.getTreeLock()) {
			//TODO: Add logic to sort by time of (manual) selection, clip start time, and maybe by hydrophone 
			if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
				unitsPanel.add(clipDisplayUnit.getComponent(), -1);
			}
			else {
				unitsPanel.add(clipDisplayUnit.getComponent(), clipDisplayParameters.newClipOrder);
			}
			showAndHideClip(clipDisplayUnit);
		}

		if (!isViewer) {
			removeOldClips();
			updatePanel();
		}
	}

	/**
	 * Remove the display component associated with a specific data unit. 
	 * @param clipDataUnit
	 */
	public void removeClip(ClipDataUnit clipDataUnit) {
		synchronized (unitsPanel.getTreeLock()) {
			int compCount = unitsPanel.getComponentCount();
			ClipDisplayUnit clipDisplayUnit;
			for (int i = compCount-1; i >= 0; i--) {
				clipDisplayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				if (clipDisplayUnit.getClipDataUnit() == clipDataUnit) {
					clipDisplayUnit.removeDisplayDecorations();
					unitsPanel.remove(i);
					updatePanel();
					break;
				}
			}
		}
	}
	
	/**
	 * @return the unitsPanel
	 */
	public JPanel getUnitsPanel() {
		return unitsPanel;
	}

	private void removeOldClips() {
		if (isViewer) {
			return;
		}
		synchronized (unitsPanel.getTreeLock()) {
			int compCount = unitsPanel.getComponentCount();
			ClipDisplayUnit clipDisplayUnit;
			long minTime = PamCalendar.getTimeInMillis() - clipDisplayParameters.maxMinutes * 60000L;
			for (int i = compCount-1; i >= 0; i--) {
				clipDisplayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				if (i > clipDisplayParameters.maxClips || clipDisplayUnit.getClipDataUnit().getTimeMilliseconds() < minTime) {
					clipDisplayUnit.removeDisplayDecorations();
					unitsPanel.remove(i);
				}
			}
		}
	}

	//	public void newViewerDataLoaded() {
	//
	//	}

	public void removeAllClips() {
		synchronized (unitsPanel.getTreeLock()) {
			int compCount = unitsPanel.getComponentCount();
			ClipDisplayUnit clipDisplayUnit;
			for (int i = compCount-1; i >= 0; i--) {
				clipDisplayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				clipDisplayUnit.removeDisplayDecorations();
				unitsPanel.remove(i);
			}
		}
		unitsPanel.removeAll();
	}

	public void newViewerTimes(long start, long end) {
		removeAllClips();
//		ListIterator<ClipDataUnit> it = clipDisplayParent.getClipDataBlock().getListIterator(0);
		ArrayList<ClipDataUnit> clipUnits = clipDisplayParent.getClipDataBlock().getDataCopy();
		for (ClipDataUnit cdu : clipUnits) {
//			ClipDataUnit cdu = it.next();
			long clipTime = cdu.getTimeMilliseconds();
			if (clipTime >= start && clipTime <= end){
				newDataUnit(cdu);
			}
		}
		updatePanel();
	}

	private PamDataUnit findTriggerDataUnit(ClipDataUnit clipDataUnit) {
		return clipDataUnit.findTriggerDataUnit();
//		if (clipDataUnit.getTriggerDataUnit())
//		String trigName = clipDataUnit.triggerName;
//		long trigMillis = clipDataUnit.triggerMilliseconds;
//		long startMillis = clipDataUnit.getTimeMilliseconds();
//		PamDataBlock<PamDataUnit> dataBlock = findTriggerDataBlock(trigName);
//		if (dataBlock == null) {
//			return null;
//		}
////		PamDataUnit trigUnit = dataBlock.findDataUnit(trigMillis, 0);
////		if (trigUnit == null) {
//			PamDataUnit trigUnit = findTriggerDataUnit2(dataBlock, clipDataUnit, 200);
////		}
//		return trigUnit;
	}

//	/**
//	 * Bespoke search for finding the trig data unit, since the times don't always 
//	 * seem to be matching up correctly. This seems to be more a problem for old data from old file
//	 * format which didn't store the trigger time than it is for newer data. 
//	 * @param dataBlock datablock to search
//	 * @param clipDataUnit clip to match to
//	 * @param timeJitter allowable time jitter (+ or -)
//	 * @return found data unit with overlapping channel map and time close to the clip trigger time. 
//	 */
//	private PamDataUnit findTriggerDataUnit2(PamDataBlock<PamDataUnit> dataBlock, ClipDataUnit clipDataUnit, int timeJitter) {
//		long trigMillis = clipDataUnit.triggerMilliseconds;
//		long t1 = trigMillis - timeJitter;
//		long t2 = trigMillis + timeJitter;
//		int channels = clipDataUnit.getChannelBitmap();
//		synchronized (dataBlock.getSynchLock()) {
//			ListIterator<PamDataUnit> iter = dataBlock.getListIterator(PamDataBlock.ITERATOR_END);
//			while (iter.hasPrevious()) {
//				PamDataUnit trigUnit = iter.previous();
//				long trigTime = trigUnit.getTimeMilliseconds();
//				if (trigTime >= t1 && trigTime <= t2 && (trigUnit.getChannelBitmap() & channels) != 0) {
//					return trigUnit;
//				}
//			}
//			
//		}
//		return null;
//	}

	private String lastFoundName;
	private PamDataBlock<PamDataUnit> lastFoundBlock;

	public boolean mouseDown;
	private PamDataBlock<PamDataUnit> findTriggerDataBlock(String dataName) {
		if (dataName == null) {
			return null;
		}
		if (dataName.equals(lastFoundName)) {
			return lastFoundBlock;
		}
		PamDataBlock<PamDataUnit> dataBlock = PamController.getInstance().getDetectorDataBlock(dataName);
		if (dataBlock == null) {
			return null;
		}
		lastFoundName = new String(dataName);
		lastFoundBlock = dataBlock;
		return dataBlock;
	}

	public void updatePanelLater() {
		SwingUtilities.invokeLater(new UpdateLater());
	}
	
	class UpdateLater implements Runnable {
		@Override
		public void run() {
			updatePanel();
		}
	}
	
	/**
	 * update the panel layout. 
	 */
	public void updatePanel() {
		int space = scrollPane.getWidth() - scrollPane.getVerticalScrollBar().getWidth();
		Insets ins = scrollPane.getInsets();
		if (ins != null) {
			space -= (ins.left + ins.right);
		}
		clipLayout.setPanelWidth(space);
		clipLayout.setClipSizes(this.unitsPanel);
//		displayPanel.invalidate();
		unitsPanel.invalidate();
		unitsPanel.doLayout();
		unitsPanel.repaint();
		displayPanel.repaint();
		repaintUnits();

//		System.out.println(String.format("In update Panel Current display size = %d by %d",
//				unitsPanel.getWidth(), unitsPanel.getHeight()));
	}

	private void repaintUnits() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				repaintInAWT();
			}
		});
	}
	
	private void repaintInAWT() {
		synchronized (unitsPanel.getTreeLock()) {
			int n = unitsPanel.getComponentCount();
			ClipDisplayUnit displayUnit;
			for (int i = 0; i < n; i++) {
				displayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				displayUnit.repaintUnit();
			}
		}
	}

	private void updateAllDisplayUnits(boolean needNewImage) {
//		System.out.println("updateAllDisplayUnits new Image = " + needNewImage);
		synchronized (unitsPanel.getTreeLock()) {
			int n = unitsPanel.getComponentCount();
			ClipDisplayUnit displayUnit;
			for (int i = 0; i < n; i++) {
				displayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				displayUnit.layoutUnit(needNewImage);
			}
		}
		updatePanelLater();
	}

	private void newSampleRate(float sampleRate) {
		this.setSampleRate(sampleRate);
	}

	/**
	 * @return the displayPanel
	 */
	public JPanel getDisplayPanel() {
		return displayPanel;
	}

	/**
	 * @return the colourArray
	 */
	public ColourArray getColourArray() {
		return colourArray;
	}

	/**
	 * @param sampleRate the sampleRate to set
	 */
	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
		displayControlPanel.setValues(clipDisplayParameters);
	}

	/**
	 * @return the sampleRate
	 */
	public float getSampleRate() {
		return sampleRate;
	}
	
	/**
	 * A notification from a ClipDisplayUnit
	 * that the mouse was pressed
	 * @param clipDisplayUnit 
	 * @param e 
	 */
	public void mousePressed(MouseEvent e, ClipDisplayUnit clipDisplayUnit) {
//		unitsMouse.mousePressed(null);
	}
	
	/**
	 * A notification from a ClipDisplayUnit
	 * that the mouse was released
	 */
	public void mouseReleased(MouseEvent e, ClipDisplayUnit clipDisplayUnit) {
//		unitsMouse.mouseReleased(null);
	}
	
	/**
	 * A notification from a ClipDisplayUnit
	 * that the mouse was clicked
	 */
	public void mouseClicked(MouseEvent e, ClipDisplayUnit clipDisplayUnit) {
//		unitsMouse.mouseClicked(null);
		if (clipDisplayMarker == null) {
			return;
		}
		clipDisplayMarker.mouseClicked(e, clipDisplayUnit);
		if (e.getClickCount() == 2) {
			playClip(clipDisplayUnit.getClipDataUnit());
		}
	}
	
	/**
	 * Play the clip (called from mouse double click)
	 * @param clipDataUnit
	 */
	private void playClip(ClipDataUnit clipDataUnit) {
		if (clipDataUnit == null) {
			return;
		}
		PamDataBlock dataBlock = clipDataUnit.getParentDataBlock();
		if (dataBlock == null) return;
		PamProcess process = dataBlock.getParentProcess();
		if (process instanceof ClipProcess) {
			ClipProcess clipProc = (ClipProcess) process;
			clipProc.playClip(clipDataUnit);
		}
	}

	/**
	 * Get a list of highlighted display units. 
	 * @return lit of highlighted units. 
	 */
	public ArrayList<ClipDisplayUnit> getHighlightedUnits() {
		ArrayList<ClipDisplayUnit> units = new ArrayList<ClipDisplayUnit>();

		synchronized (unitsPanel.getTreeLock()) {
			int n = unitsPanel.getComponentCount();
			ClipDisplayUnit displayUnit;
			for (int i = 0; i < n; i++) {
				displayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				if (displayUnit.isHighlight()) {
					units.add(displayUnit);
				}
			}
		}
		return units;
	}
	
	/**
	 * Get the index of a clip display unit. 
	 * @param clipDisplayUnit 
	 * @return index in display. 
	 */
	public int getClipIndex(ClipDisplayUnit clipDisplayUnit) {
		synchronized (unitsPanel.getTreeLock()) {
			int n = unitsPanel.getComponentCount();
			ClipDisplayUnit displayUnit;
			for (int i = 0; i < n; i++) {
				displayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				if (displayUnit == clipDisplayUnit) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public void clearAllHighlights() {
		synchronized (unitsPanel.getTreeLock()) {
			int n = unitsPanel.getComponentCount();
			ClipDisplayUnit displayUnit;
			for (int i = 0; i < n; i++) {
				displayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				displayUnit.setHighlight(false);
			}
		}
	}
	
	/**
	 * Select / highlight a range of units
	 * @param firstClickedUnit2
	 * @param clipDisplayUnit
	 */
	public void selectClipRange(ClipDisplayUnit firstClickedUnit, ClipDisplayUnit clipDisplayUnit) {
		int ind1 = getClipIndex(firstClickedUnit);
		int ind2 = getClipIndex(clipDisplayUnit);
		int minInd = Math.min(ind1,  ind2);
		int maxInd = Math.max(ind1,  ind2);
		if (maxInd < 0 || minInd < 0) {
			minInd = maxInd = -1;
		}
		synchronized (unitsPanel.getTreeLock()) {
			int n = unitsPanel.getComponentCount();
			ClipDisplayUnit displayUnit;
			for (int i = 0; i < n; i++) {
				displayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				displayUnit.setHighlight(i>=minInd && i<=maxInd);
			}
		}
	}
	
//	/**
//	 * Add a ClipDisplayUnit clip to the list of selected clips
//	 * 
//	 * @param theUnit
//	 */
//	public void selectClip(ClipDisplayUnit theUnit) {
//		// if the mouse button is not pressed, return immediately
//		if (!mouseDown) return;
//		
//		// otherwise, add the unit to the list and change it's background
//		selectedClips.add(theUnit);
//		theUnit.changeColor(Color.RED);
//	}

	class UnitsMouse extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			updateAllDisplayUnits(false);
			
			// the next bit of code clears any ClipDisplayUnits that
			// have been selected.  Since we're not pursuing that right
			// now, don't even bother with the code.  Leaving it here
			// though, just in case we get back to it (see notes in
			// ClipDisplayUnit.ImageMouse for more info)
//			for (ClipDisplayUnit aUnit : selectedClips) {
//				aUnit.changeColor(null);
//			}
//			selectedClips.clear();
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			mouseDown = true;
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			mouseDown = false;
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}


		
	}
	class ClipMainPanel extends PamPanel {

		private int lastWidth;

		public ClipMainPanel(LayoutManager layout) {
			super(layout);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (getWidth() != lastWidth) {
				updatePanel();
				lastWidth = getWidth();
			}
		}

	}

	public void displayControlChanged(boolean needNewImage) {
		if (displayControlPanel == null) {
			return;
		}
		boolean ok = displayControlPanel.getValues(clipDisplayParameters);
		if (needNewImage) {
			makeColourArray();
		}
		updateAllDisplayUnits(needNewImage);
	}

	/**
	 * Menu that allows colour chosing
	 * @param e
	 */
	public void showPopupMenu(MouseEvent e) {
		PamSymbolChooser symbolChooser = getSymbolChooser();
		if (symbolChooser instanceof StandardSymbolChooser) {
			StandardSymbolChooser standardChooser = (StandardSymbolChooser) symbolChooser;
			ArrayList<JMenuItem> menuItems = standardChooser.getQuickMenuItems(PamController.getMainFrame(), this, "Border colour:", 
					SymbolModType.LINECOLOUR, true);
			JPopupMenu popMenu = new JPopupMenu();
			for (JMenuItem menuItem : menuItems) {
				popMenu.add(menuItem);
			}
			// how about some dataselector stuff, to see if that's going to work.
			DataSelector ds = getDataSelector();
			if (ds != null) {
				popMenu.addSeparator();
				popMenu.add(ds.getMenuItem(null, new DataSelectorChangeListener() {
					@Override
					public void selectorChange(DataSelector changedSelector) {
						showAndHideClips();
					}
				}));
			}
//			// also add the generic superdetection stuff. 
//			List<JMenuItem> superDetMenuItems = GlobalSymbolManager.getInstance().getSuperDetMenuItems(getUniqueName(), getClipDataProjector(), this);
//			if (superDetMenuItems != null) {
//				JMenu superMen = new JMenu("Super Detection symbols");
//				popMenu.add(superMen);
//				for (JMenuItem item : superDetMenuItems) {
//					superMen.add(item);
//				}
//			}
			popMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	
	/**
	 * Go through all clips and show or hide them according to the 
	 * Really need to call this in the AWT thread or it can cause all sorts 
	 * of trouble, e.g. if called from the arrival of data, it's parent datablock 
	 * is locked, so when it updates, you can get thread lock. 
	 * whims of the data selector
	 */
	private void showAndHideClips() {
		DataSelector dataSelector = getDataSelector();
		synchronized (unitsPanel.getTreeLock()) {
			int compCount = unitsPanel.getComponentCount();
			ClipDisplayUnit clipDisplayUnit;
			for (int i = compCount-1; i >= 0; i--) {
				clipDisplayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				showAndHideClip(clipDisplayUnit, dataSelector);
			}
		}
	}
	
	private void showAndHideClip(ClipDisplayUnit clipDisplayUnit) {
		showAndHideClip(clipDisplayUnit, getDataSelector());
	}

	private void showAndHideClip(ClipDisplayUnit clipDisplayUnit, DataSelector dataSelector) {
		ClipDataUnit clipUnit = clipDisplayUnit.getClipDataUnit();
		boolean vis = true;
		if (dataSelector != null && clipUnit != null) {
			vis = dataSelector.scoreData(clipUnit) > 0;
		}
//		boolean vis = (dataSelector != null && clipUnit != null && dataSelector.scoreData(clipUnit) > 0);
		clipDisplayUnit.setVisible(vis);
	}
	
	
	/**
	 * Get the data selector. 
	 * @return data selector, including options on annotations and super detections. 
	 */
	private DataSelector getDataSelector() {
		ClipDisplayDataBlock<ClipDataUnit> dataBlock = clipDisplayParent.getClipDataBlock();
		return dataBlock.getDataSelector(getUniqueName(), false);
	}

	private void makeColourArray() {
		if (currentColours == clipDisplayParameters.getColourMap()) {
			return;
		}
		currentColours = clipDisplayParameters.getColourMap();

		colourArray = ColourArray.createStandardColourArray(256, currentColours);
	}

	/**
	 * @return the spectrogramProjector
	 */
	public ClipDataProjector getClipDataProjector() {
		return clipDataProjector;
	}

	@Override
	public Serializable getSettingsReference() {
		clipDisplayParameters.showControlPanel = hidingPanel.isExpanded();
		return clipDisplayParameters;
	}

	@Override
	public long getSettingsVersion() {
		return ClipDisplayParameters.serialVersionUID;
	}
	
	public PamSymbolManager getSymbolManager() {
		ClipDisplayDataBlock<ClipDataUnit> dataBlock = clipDisplayParent.getClipDataBlock();
		return dataBlock.getPamSymbolManager();
	}
	
	public PamSymbolChooser getSymbolChooser() {
		PamSymbolManager symbMan = getSymbolManager();
		if (symbMan == null) {
			return null;
		}
		return symbMan.getSymbolChooser(getUniqueName(), getClipDataProjector());
	}

	@Override
	public String getUnitName() {
		return clipDisplayParent.getDisplayName();
	}

	@Override
	public String getUnitType() {
		return "Clip Display Panel";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		this.clipDisplayParameters = ((ClipDisplayParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * @return the scrollPane
	 */
	protected JScrollPane getScrollPane() {
		return scrollPane;
	}

	@Override
	public Component getComponent() {
		return getDisplayPanel();
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the clipDisplayParameters
	 */
	public ClipDisplayParameters getClipDisplayParameters() {
		return clipDisplayParameters;
	}

	/**
	 * @return the clipDisplayParent
	 */
	public ClipDisplayParent getClipDisplayParent() {
		return clipDisplayParent;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			updateAllDisplayUnits(false);
			updatePanelLater();
			break;
		}
		
	}

	@Override
	public String getFrameTitle() {
		return clipDisplayParent.getDisplayName();
	}

	public int addPopupMenuItems(JPopupMenu popupMenu, MouseEvent mouseEvent) {
		ArrayList<ClipDisplayUnit> highlights = getHighlightedUnits();
		int nObs = clipDisplayMarker.getObserverCount();
		if (highlights == null || highlights.size() == 0|| nObs == 0) {
			return 0;
		}
		JPopupMenu pm = clipDisplayMarker.createJPopMenu(ExtMouseAdapter.fxMouse(mouseEvent, null));
		if (pm == null) {
			return 0;
		}
		/*
		 * Copy menu items into a separate list first
		 * otherwise, as they are added to the new menu
		 * they are removed from the old one and all hell
		 * breaks loose. 
		 */
		Component[] menuItems = pm.getComponents();
		int n = menuItems.length;
		if (n > 0 && popupMenu.getComponentCount() > 0) {
			popupMenu.addSeparator();
		}
		for (int i = 0; i < n; i++) {
			popupMenu.add(menuItems[i]);
		}
		return n;
//		List<MenuItem> menuItems = clipDisplayMarker.getPopupMenuItems(ExtMouseAdapter.fxMouse(mouseEvent, null));
//		if (menuItems != null) {
//			for (MenuItem mi : menuItems) {
//				popupMenu.add(PamUtilsFX.fxMenuItemToSwing(mi));
//			}
//			return menuItems.size();
//		}
//		else {
//			return 0;
//		}
	}

	@Override
	public void symbolUpdate() {
		updateBorders();
	}

	private void updateBorders() {
		synchronized (unitsPanel.getTreeLock()) {
			int compCount = unitsPanel.getComponentCount();
			ClipDisplayUnit clipDisplayUnit;
			for (int i = compCount-1; i >= 0; i--) {
				clipDisplayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				clipDisplayUnit.setBorderColour();
			}
		}
	}

	/**
	 * @return the displayControlPanel
	 */
	public DisplayControlPanel getDisplayControlPanel() {
		return displayControlPanel;
	}

}
