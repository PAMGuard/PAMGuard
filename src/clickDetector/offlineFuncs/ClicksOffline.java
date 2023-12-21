package clickDetector.offlineFuncs;

import generalDatabase.PamTableItem;
import generalDatabase.dataExport.DataExportDialog;
import generalDatabase.dataExport.IntValueParams;
import generalDatabase.dataExport.LookupFilter;
import generalDatabase.dataExport.TimeValueParams;
import generalDatabase.dataExport.ValueFilter;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupList;

import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JMenuItem;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import offlineProcessing.OfflineTaskManager;
import offlineProcessing.superdet.OfflineSuperDetFilter;
import performanceTests.PamguardInfo;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryStore;
import clickDetector.ClickBTDisplay;
import clickDetector.ClickBinaryDataSource;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import dataPlotsFX.JamieDev;
import PamController.PamController;
import PamView.CtrlKeyManager;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Functions for handling offline data display
 * and processing. 
 * <p>
 * Basically, this is an add on the ClickController, but
 * split off into a separate class since the main
 * ClickController has got a bit over cumbersome. 
 *  
 * @author Doug Gillespie
 *
 */
public class ClicksOffline {

	private ClickControl clickControl;

	private OfflineParameters offlineParameters = new OfflineParameters();

	private OLProcessDialog clickOfflineDialog;
	
	public static final String ClickTypeLookupName = "OfflineRCEvents";

	/**
	 * Constructor, called from ClickControl. 
	 * @param clickControl
	 */
	public ClicksOffline(ClickControl clickControl) {
		super();
		this.clickControl = clickControl;
	}

	protected void runClickClassification() {

	}

	/**
	 * Called when the click store has closed
	 * Will need to delete all data from the module. 
	 * will be called from the offlineStore
	 */
	protected void storageClosed() {

	}

	/** 
	 * Called from when data have changed (eg from re doing click id). 
	 * Needs to notify the display and maybe some other classes. 
	 */
	protected void offlineDataChanged() {
		clickControl.offlineDataChanged();
	}

	/**
	 * Add offline functions to the top of the main Detector menu
	 * when operating in viewer mode. 
	 * @param menu menu to add items to 
	 * @return number of items added. 
	 */
	public int addDetectorMenuItems(Frame owner, Container menu) {
		JMenuItem menuItem;
		menuItem = new JMenuItem("Show Events ...");
		menuItem.addActionListener(new ShowEvents(owner));
		menu.add(menuItem);
		menuItem = new JMenuItem("Reanalyse clicks ...");
		menuItem.addActionListener(new ReanalyseClicks());
		menu.add(menuItem);

		return 2;
	}

	/**
	 * Add menu items associated with right mouse actions on bearing time
	 * display 
	 * @param menu menu to add items to
	 * @param hasZoom whether or not the display has a zoomed area. 
	 * @param isOnClick whether or not the mouse is on a click.
	 * @return number of items added to the menu
	 */
	public int addBTMenuItems(Container menu, OverlayMark overlayMark, ClickBTDisplay btDisplay, boolean hasZoom, ClickDetection clickedClick) {
		return addBTMenuItems(menu, overlayMark, btDisplay.getMarkedClicks(), hasZoom, clickedClick);
	}
	
	public int addBTMenuItems(Container menu, OverlayMark overlayMark, List<PamDataUnit> markedClicks, boolean hasZoom, PamDataUnit singleDataUnit) {
		JMenuItem menuItem;
		OfflineEventDataUnit autoEvent = null;
		OfflineEventDataUnit anEvent;
		Color col;
		OfflineEventDataUnit[] markedEvents = findMarkedEvents(markedClicks);
		int nMenuItems = 0;
		if (markedEvents == null) {
			autoEvent = clickControl.getLatestOfflineEvent();
		}
		else if (markedEvents.length == 1) {
			autoEvent = markedEvents[0];
		}
		PamSymbol eventSymbol = null;
		int eventNumber = 0;
		if (autoEvent != null) {
			eventNumber = autoEvent.getDatabaseIndex();
			col = PamColors.getInstance().getWhaleColor(autoEvent.getColourIndex());
			eventSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, true, col, col);
		}
		//		ActionListener al;
		//		CtrlKeyManager cm = btDisplay.getCtrlKeyManager();
		
		if (singleDataUnit != null) {
			if (autoEvent != null) {
				menuItem = new JMenuItem(String.format("Add click to event %d (Ctrl+A)", 
						eventNumber), eventSymbol);
				menuItem.addActionListener(new QuickAddClicks(autoEvent, singleDataUnit, overlayMark));
				//				cm.addCtrlKeyListener('a', al);
				menu.add(menuItem);
				nMenuItems++;
			}
			String unitTypeName = "click";
			if (ClickDetection.class.isAssignableFrom(singleDataUnit.getClass()) == false) {
				PamDataBlock pdb = singleDataUnit.getParentDataBlock();
				unitTypeName = pdb.getDataName();
				if (unitTypeName.endsWith("s")) {
					unitTypeName = unitTypeName.substring(0, unitTypeName.length()-2);
				}
			}
			
			menuItem  = new JMenuItem("Label click ...");
			menuItem.addActionListener(new LabelClicks(overlayMark, singleDataUnit));
			menu.add(menuItem);
			nMenuItems++;
			anEvent = (OfflineEventDataUnit) singleDataUnit.getSuperDetection(OfflineEventDataUnit.class);
			if (anEvent != null) {
				menuItem = new JMenuItem(String.format("Remove %s from event %d", unitTypeName, anEvent.getEventNumber()));
				menuItem.addActionListener(new UnlabelClick(overlayMark, singleDataUnit));
				menu.add(menuItem);
				nMenuItems++;
			}
		}
		else if (markedClicks != null && markedClicks.size() > 0){
			if (autoEvent != null) {
				menuItem = new JMenuItem(String.format("Add %d clicks to event %d (Ctrl+A)", 
						markedClicks.size(), eventNumber), eventSymbol);
				menuItem.addActionListener(new QuickAddClicks(autoEvent, markedClicks,overlayMark));
				menu.add(menuItem);
				nMenuItems++;
			}
			menuItem  = new JMenuItem("Label clicks (Ctrl+L) ...");
			menuItem.addActionListener(new LabelClicks(overlayMark, markedClicks));
			menu.add(menuItem);
			nMenuItems++;
			menuItem = new JMenuItem("Create new event (Ctrl+N) ...");
			menuItem.addActionListener(new NewEvent(overlayMark, markedClicks));
			menu.add(menuItem);
			nMenuItems++;
			if (markedEvents != null && markedEvents.length > 0) {
				if (markedEvents.length > 1) {
					menuItem = new JMenuItem("Remove all marked clicks from multiple events");
					menuItem.addActionListener(new UnlabelMarkedClicks(overlayMark, markedClicks, -1));
					menu.add(menuItem);
					nMenuItems++;
				}
				for (int i = 0; i < markedEvents.length; i++) {
					anEvent = markedEvents[i];
					col = PamColors.getInstance().getWhaleColor(anEvent.getColourIndex());
					eventSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, true, col, col);
					menuItem = new JMenuItem(String.format("Remove marked clicks from event %d", 
							anEvent.getEventNumber()), eventSymbol);
					menuItem.addActionListener(new UnlabelMarkedClicks(overlayMark, markedClicks,
							anEvent.getEventNumber()));
					menu.add(menuItem);
					nMenuItems++;
				}
			}

		}
		return nMenuItems;
	}

	/**
	 * Automatically work out if there is an obvious event to add clicks to.
	 * <p>this will either be the last event anything was added to, or 
	 * a unique event already used with the clicks in the marked list. 
	 * @param btDisplay Bearing time display
	 * @return existing event, or null. 
	 */
	private OfflineEventDataUnit[] findMarkedEvents(List<PamDataUnit> markedClicks) {
		if (markedClicks == null) {
			return null;
		}
		OfflineEventDataUnit[] existingEvents = null;
		int nEvents = 0;
		OfflineEventDataUnit anEvent;
		PamDataUnit aClick;
		boolean haveEvent;
		if (markedClicks != null) {
			for (int i = 0; i < markedClicks.size(); i++) {
				aClick = markedClicks.get(i);
				anEvent = (OfflineEventDataUnit) aClick.getSuperDetection(OfflineEventDataUnit.class);
				if (anEvent == null) {
					continue; // nothing doing
				}
				if (existingEvents == null) {
					existingEvents = new OfflineEventDataUnit[1];
					existingEvents[0] = anEvent;
					nEvents = 1;
				}
				else {
					haveEvent = false;
					for (int e = 0; e < nEvents; e++) {
						if (anEvent == existingEvents[e]) {
							haveEvent = true;
							break;
						}
					}
					if (!haveEvent) {
						existingEvents = Arrays.copyOf(existingEvents, nEvents+1);
						existingEvents[nEvents++] = anEvent;
					}
				}
			}
		}
		return existingEvents;
	}

	private class ShowEvents implements ActionListener{

		private Frame frame;

		public ShowEvents(Frame frame) {
			super();
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			clickControl.showOfflineEvents(frame);
		}

	}

	private class QuickAddClicks implements ActionListener {
		private List<PamDataUnit> markedClicks;
		private PamDataUnit singleClick;
		private OfflineEventDataUnit event;
		private OverlayMark overlayMark;

		public QuickAddClicks(OfflineEventDataUnit event, List<PamDataUnit> markedClicks2, OverlayMark overlayMark) {
			super();
			this.event = event;
			this.markedClicks = markedClicks2;
			this.overlayMark=overlayMark;
		}

		public QuickAddClicks(OfflineEventDataUnit event, PamDataUnit singleClick, OverlayMark overlayMark) {
			super();
			this.event = event;
			this.singleClick = singleClick;
			this.overlayMark=overlayMark;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (event == null) {
				return;
			}
			if (singleClick != null) {
				event.addSubDetection(singleClick);
				if (overlayMark!=null) overlayMark.repaintOwner();

			}
			else if (markedClicks != null) {
				event.addSubDetections(markedClicks);
				if (overlayMark!=null) overlayMark.repaintOwner();

			}

			clickControl.getClickDetector().getOfflineEventDataBlock().
			updatePamData(event, System.currentTimeMillis());
			notifyEventChanges(event);
			clickControl.setLatestOfflineEvent(event);
			//must repaint or click does not change colour
			clickControl.getDisplayManager().findFirstBTDisplay().repaintTotal();
		}

	}
	//	private class LabelClick implements ActionListener {
	//		private OverlayMark overlayMark;
	//		private ClickDetection singleClick;
	//		/**
	//		 * @param overlayMark
	//		 */
	//		public LabelClick(OverlayMark overlayMark, ClickDetection singleClick) {
	//			super();
	//			
	//			this.overlayMark = overlayMark;
	//			this.singleClick = singleClick;
	//		}
	//		@Override
	//		public void actionPerformed(ActionEvent arg0) {
	//			labelClick(overlayMark, singleClick);
	//		}
	//	}
		private class UnlabelClick implements ActionListener {
			private OverlayMark overlayMark;
			private List<PamDataUnit> unitsList;;
			/**
			 * @param btDisplay
			 */
			public UnlabelClick(OverlayMark overlayMark, PamDataUnit singleClick) {
				super();
				
				this.overlayMark = overlayMark;
				this.unitsList = new ArrayList<>();
				unitsList.add(singleClick);
			}
			
			/**
			 * @param overlayMark
			 * @param unitsList
			 */
			public UnlabelClick(OverlayMark overlayMark, List<PamDataUnit> unitsList) {
				super();
				this.overlayMark = overlayMark;
				this.unitsList = unitsList;
			}

			@Override
			public void actionPerformed(ActionEvent arg0) {
				unLabelClicks(overlayMark, unitsList);
				//must repaint or click does not change colour
				clickControl.getDisplayManager().findFirstBTDisplay().repaintTotal();
			}
		}

	private class LabelClicks implements ActionListener {
		private OverlayMark overlayMark;
		private List<PamDataUnit> dataList;
		/**
		 * @param btDisplay
		 */
		public LabelClicks(OverlayMark overlayMark, List<PamDataUnit> markedClicks) {
			super();
			this.overlayMark = overlayMark;
			this.dataList = markedClicks;
		}

		public LabelClicks(OverlayMark overlayMark, PamDataUnit singleClick) {
			super();
			this.overlayMark = overlayMark;
			dataList = new ArrayList<>();
			dataList.add(singleClick);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			labelClicks(overlayMark, dataList);
			//must repaint or click does not change colour
			clickControl.getDisplayManager().findFirstBTDisplay().repaintTotal();
		}
	}


	private class NewEvent implements ActionListener {

		private OverlayMark overlayMark;
		private List<PamDataUnit> dataList;
		
		/**
		 * @param overlayMark
		 * @param markedClicks
		 */
		public NewEvent(OverlayMark overlayMark, List<PamDataUnit> markedClicks) {
			super();
			this.overlayMark = overlayMark;
			this.dataList = markedClicks;
		}

		/**
		 * @param btDisplay
		 */
		public NewEvent(OverlayMark overlayMark, PamDataUnit singleClick) {
			this.overlayMark = overlayMark;
			this.dataList = new ArrayList<>();
			dataList.add(singleClick);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			newEvent(overlayMark, dataList);
			//must repaint or click does not change colour
			clickControl.getDisplayManager().findFirstBTDisplay().repaintTotal();
		}
	}

	private class UnlabelMarkedClicks implements ActionListener {
		private int eventNumber;
		private OverlayMark overlayMark;
		private List<PamDataUnit> markedClicks;

		/**
		 * @param eventNumber
		 */
		public UnlabelMarkedClicks(OverlayMark overlayMark, List<PamDataUnit> markedClicks, int eventNumber) {
			super();
			this.overlayMark = overlayMark;
			this.markedClicks = markedClicks;
			this.eventNumber = eventNumber;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			unLabelClicks(overlayMark, markedClicks, eventNumber);
			//must repaint or click does not change colour
			clickControl.getDisplayManager().findFirstBTDisplay().repaintTotal();
		}

	}

	private class ReanalyseClicks implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			reAnalyseClicks();
		}
	}

	private class ExportEventData implements ActionListener {

		private Frame frame;

		/**
		 * @param parentFrame
		 */
		public ExportEventData(Frame parentFrame) {
			super();
			this.frame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			exportEventData(frame);
		}
	}


	abstract class ShowEventAnalysisDialog implements ActionListener{

		OfflineEventDataUnit event; 
		Window frame;
		ClickControl clickControl; 


		public ShowEventAnalysisDialog(Window frame, OfflineEventDataUnit event, ClickControl clickControl){
			this.frame=frame;
			this.event= event; 
			this.clickControl=clickControl;
		}
	}
	private class CheckEventDatabase implements ActionListener {

		private Frame frame;

		/**
		 * @param parentFrame
		 */
		public CheckEventDatabase(Frame parentFrame) {
			super();
			this.frame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			checkEventDatabase(frame);
		}
	}


	/**
	 * Create a menu item for exporting click event data. 
	 * @param parentFrame parent frame (for any created dialog)
	 * @return menu item.
	 */
	public JMenuItem getExportMenuItem(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem("Export click event data ...");
		menuItem.addActionListener(new ExportEventData(parentFrame));
		return menuItem;
	}

	public void checkEventDatabase(Frame frame) {
		DatabaseCheckDialog.showDialog(frame, clickControl);

	}

	public JMenuItem getDatabaseCheckItem(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem("Check Offline Event Database ...");
		menuItem.addActionListener(new CheckEventDatabase(parentFrame));
		return menuItem;
	}

	/**
	 * Go through clicks and do things like re classify to species, look for trains, etc. 
	 * This may eventually need to be done within each offlineStore if it's not possible
	 * to find a common way of scrolling through all clicks. 
	 */
	public void reAnalyseClicks() {
		if (clickOfflineDialog == null) {
			clickOfflineDialog = new OLProcessDialog(clickControl.getGuiFrame(), 
					getOfflineTaskGroup(clickControl), "Click Reprocessing");
		}
		clickOfflineDialog.setVisible(true);
	}

	public void exportEventData(Frame frame) {
		OfflineEventLogging offlineEventLogging = clickControl.getClickDetector().getOfflineEventLogging();
		DataExportDialog exportDialog = new DataExportDialog(frame, offlineEventLogging.getTableDefinition(), 
				"Export Click Events");
		exportDialog.excludeColumn("UTCMilliseconds");
		exportDialog.excludeColumn("colour");

		PamTableItem eventTableItem = offlineEventLogging.getTableDefinition().findTableItem("eventType");
		LookupList lutList = LookUpTables.getLookUpTables().getLookupList(ClicksOffline.ClickTypeLookupName);
		exportDialog.addDataFilter(new LookupFilter(exportDialog, lutList, eventTableItem));

		PamTableItem tableItem;
		tableItem = offlineEventLogging.getTableDefinition().findTableItem("UTC");
		exportDialog.addDataFilter(new ValueFilter<TimeValueParams>(exportDialog, new TimeValueParams(), tableItem));

		tableItem = offlineEventLogging.getTableDefinition().findTableItem("nClicks");
		exportDialog.addDataFilter(new ValueFilter<IntValueParams>(exportDialog, new IntValueParams(), tableItem)); 

		exportDialog.showDialog();
	}

	/**
	 * Get / Create an offline task group for click re-processing. 
	 * @return offline task group. Create if necessary
	 */
	public static OfflineTaskGroup getOfflineTaskGroup(ClickControl clickControl) {
		
		OfflineTaskGroup offlineTaskGroup = new OfflineTaskGroup(clickControl, "Click Reprocessing");
		
		/**
		 * These tasks are not registered - gets too complicated since some of them 
		 * need references to things that may not be set or created when main constructors are 
		 * called. 
		 */
		offlineTaskGroup.addTask(new ClickReClassifyTask(clickControl));
		offlineTaskGroup.addTask(new EchoDetectionTask(clickControl));
		offlineTaskGroup.addTask(new ClickDelayTask(clickControl));
		offlineTaskGroup.addTask(new ClickBearingTask(clickControl));
//		if (JamieDev.isEnabled()) {
//			//re import waveform data from raw wave files. 
//			offlineTaskGroup.addTask(new ClickWaveTask(clickControl));
//		}
		
		/*
		 * Add the click detector tasks first since these will need to operate
		 * before anything else we find, which may be from a localiser that will 
		 * need click id and initial localisation information. 
		 */
//		offlineTaskGroup.addTasks(clickControl.getOfflineTasks());
		OfflineTaskManager.getManager().addAvailableTasks(offlineTaskGroup, clickControl.getClickDataBlock());
		OfflineSuperDetFilter sdf = OfflineSuperDetFilter.makeSuperDetFilter(clickControl.getClickDataBlock(), clickControl.getUnitName()+"SuperDetFilter");
		offlineTaskGroup.setSuperDetectionFilter(sdf);
//		offlineTaskGroup.addTask(new ClickTrainClass(clickControl)); // bad idea !
		return offlineTaskGroup;
	}

	public void labelClicks(OverlayMark overlayMark, List<PamDataUnit> dataList) {
		Window win = clickControl.getGuiFrame();
		OfflineEventDataUnit event = LabelClicksDialog.showDialog(win, clickControl, overlayMark, dataList);
		if (event != null) {
			notifyEventChanges(event);
		}
	}
	
	/**
	 * Notify observers that an event has changed (will update maps, etc and also 
	 * the event display panel. 
	 * @param event
	 */
	private void notifyEventChanges(OfflineEventDataUnit event) {
		OfflineEventDataBlock offlineEventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
		offlineEventDataBlock.notifyObservers(event);
	}

	public void newEvent(OverlayMark overlayMark, List<PamDataUnit> markedClicks) {
		Window win = clickControl.getGuiFrame();
		OfflineEventDataBlock offlineEventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
		if (markedClicks == null) {
			return;
		}
		OfflineEventDataUnit newUnit = new OfflineEventDataUnit(null, getNextEventColourIndex(), null);
		newUnit = OfflineEventDialog.showDialog(win, clickControl, newUnit);
		if (newUnit != null) {
			newUnit.addSubDetections(markedClicks);
			offlineEventDataBlock.addPamData(newUnit);
			clickControl.setLatestOfflineEvent(newUnit);
			notifyEventChanges(newUnit);
		}
	}

	/**
	 * Get the most likely next colour index. 
	 * @return next colour index.
	 */
	public int getNextEventColourIndex() {
		return getHighestEventIndex() + 1;
	}

	private int getHighestEventIndex() {
		OfflineEventDataBlock offlineEventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
		ListIterator<OfflineEventDataUnit> evs = offlineEventDataBlock.getListIterator(0);
		int highestIndex = 0;
		while (evs.hasNext()) {
			OfflineEventDataUnit anEv = evs.next();
			highestIndex = Math.max(highestIndex, anEv.getDatabaseIndex());
		}
		return highestIndex;
	}

	/**
	 * Remove event labels from data units. 
	 * @param btDisplay
	 * @param markedClicks
	 * @param eventNumber
	 */
	public void unLabelClicks(OverlayMark overlayMark, List<PamDataUnit> markedClicks, int eventNumber) {
		if (markedClicks == null) {
			return;
		}
		OfflineEventDataBlock eventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
		ClickDataBlock clickDataBlock = clickControl.getClickDataBlock();
		int n = markedClicks.size();
		PamDataUnit aClick;
		OfflineEventDataUnit clickEvent;
		long now = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			aClick = markedClicks.get(i);
			clickEvent = (OfflineEventDataUnit) aClick.getSuperDetection(OfflineEventDataUnit.class);
			if (clickEvent == null) {
				continue;
			}
			if (eventNumber < 0 || clickEvent.getEventNumber() == eventNumber) {
				clickEvent.removeSubDetection(aClick);
				eventDataBlock.updatePamData(clickEvent, now);
				aClick.getParentDataBlock().updatePamData(aClick, now);
			}
			clickEvent.getParentDataBlock().notifyObservers(clickEvent);
		}
		overlayMark.repaintOwner();
	}

	public void labelClick(OverlayMark overlayMark, PamDataUnit singleClick) {
		Window win = clickControl.getGuiFrame();
		OfflineEventDataUnit event = LabelClicksDialog.showDialog(win, clickControl, overlayMark, singleClick);
		if (event != null) {
			notifyEventChanges(event);
		}
	}


	public void unLabelClicks(OverlayMark overlayMark, List<PamDataUnit> unitsList) {
		for (PamDataUnit dataUnit:unitsList) {
			unLabelClick(overlayMark, dataUnit);
		}
	}
	public void unLabelClick(OverlayMark overlayMark, PamDataUnit dataUnit) {
		OfflineEventDataUnit anEvent = (OfflineEventDataUnit) dataUnit.getSuperDetection(OfflineEventDataUnit.class);
		if (anEvent == null) {
			return;
		}
		anEvent.removeSubDetection(dataUnit);
		anEvent.updateDataUnit(System.currentTimeMillis());
		if (overlayMark!=null) overlayMark.repaintOwner();
	}

	//	private void processClick(ClickDetection click) {
	//		ClickIdInformation idInfo = clickControl.getClickIdentifier().identify(click);
	//		if (idInfo.clickType != click.getClickType()) {
	//			click.setClickType((byte) idInfo.clickType);
	//			click.getDataUnitFileInformation().setNeedsUpdate(true);
	//			//			click.setUpdated(true);
	//		}
	//	}

	public ClickControl getClickControl() {
		return clickControl;
	}

	/**
	 * @param offlineParameters the offlineParameters to set
	 */
	public void setOfflineParameters(OfflineParameters offlineParameters) {
		this.offlineParameters = offlineParameters;
	}

	/**
	 * @return the offlineParameters
	 */
	public OfflineParameters getOfflineParameters() {
		return offlineParameters;
	}

	public ClickBinaryDataSource findBinaryDataSource() {
		return (ClickBinaryDataSource) clickControl.getClickDetector().
				getClickDataBlock().getBinaryDataSource();
	}

	private BinaryStore findBinaryStore() {
		return (BinaryStore) PamController.getInstance().findControlledUnit(BinaryStore.defUnitType);
	}

	public BinaryOfflineDataMap findOfflineDataMap() {
		BinaryStore bs = findBinaryStore();
		return (BinaryOfflineDataMap) clickControl.getClickDetector().getClickDataBlock().getOfflineDataMap(bs);
	}

	public boolean saveClicks() {
		BinaryStore bs = findBinaryStore();
		if (bs == null) {
			return true;
		}
		return bs.saveData(clickControl.getClickDataBlock());
	}
	
	
	/**
	 * Called whenever a single click is selected with no overlay mark to add key shortcuts. 
	 * @param overlayMark - the overlay mark (should be null if single keys are going to be added)
	 * @param singleUnit - the single data unit
	 * @param btDisplay - the bearing time display. 
	 */
	public void newMarkedClick(OverlayMark overlayMark, PamDataUnit singleUnit, ClickBTDisplay btDisplay) {

		CtrlKeyManager cm = null;

		if (btDisplay != null) {
			cm = btDisplay.getCtrlKeyManager();
			//need 
			if (overlayMark != null) {
				return;
			}
			cm.clearAll(); //<- always clear the key listeners
		}
		
		OfflineEventDataUnit[] markedEvents = findMarkedEvents(null);
		OfflineEventDataUnit autoEvent = null;
		if (markedEvents == null) {
			autoEvent = clickControl.getLatestOfflineEvent();
		}
		else if (markedEvents.length == 1) {
			autoEvent = markedEvents[0];
		}
		
		if (btDisplay != null) {
			cm = btDisplay.getCtrlKeyManager();
			
			//only add this ability of there is no overlay mark
			if (overlayMark == null) {
//				Debug.out.println("Hello: new Marked Click 2: " + singleUnit.getUID()); 

				//add marked clicks automatically
				ActionListener al = new QuickAddClicks(autoEvent, singleUnit, overlayMark);
				cm.addCtrlKeyListener('A', al);
			}
		}
	}

	/**
	 * Called whenever a new marked list to add shortcut keys
	 * @param overlayMark - overlay mark
	 * @param markedClicks - marked click list.
	 * @param btDisplay - the bearing time display. 
	 */
	public void newMarkedClickList(OverlayMark overlayMark, List<PamDataUnit> markedClicks, ClickBTDisplay btDisplay) {

		/*
		 *  A new zoom mark has been created, so set up the shortcuts
		 *  for event creation.  
		 */
		CtrlKeyManager cm = null;

		if (btDisplay != null) {
			cm = btDisplay.getCtrlKeyManager();
			//need 
			if (overlayMark == null) {
				return;
			}
			cm.clearAll(); //<- always clear the key listeners
		}

		OfflineEventDataUnit autoEvent = null;
		OfflineEventDataUnit anEvent;
		Color col;
		OfflineEventDataUnit[] markedEvents = findMarkedEvents(markedClicks);
		int nMenuItems = 0;
		if (markedEvents == null) {
			autoEvent = clickControl.getLatestOfflineEvent();
		}
		else if (markedEvents.length == 1) {
			autoEvent = markedEvents[0];
		}

		ActionListener al;
		al = new LabelClicks(overlayMark, markedClicks);

		if (cm != null) {
			cm.addCtrlKeyListener('L', al);

			cm.addCtrlKeyListener('N', new NewEvent(overlayMark, markedClicks));

			if (markedClicks != null) {
				//add marked clicks automatically
				al = new QuickAddClicks(autoEvent, markedClicks, overlayMark);
				cm.addCtrlKeyListener('A', al);
			}
		}
	}

}