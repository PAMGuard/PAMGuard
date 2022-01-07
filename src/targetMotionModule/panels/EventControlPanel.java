package targetMotionModule.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicArrowButton;

import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.ViewerScrollerManager;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import clickDetector.offlineFuncs.OfflineEventListPanel;
import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamDetection.PamDetection;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetection;
import targetMotionModule.TargetMotionControl;
import targetMotionModule.TargetMotionResult;

@SuppressWarnings("rawtypes") 
public class EventControlPanel extends AbstractControlPanel implements TMDialogComponent, TargetMotionControlPanel{
	
	private TargetMotionControl targetMotionControl;
	
	private ClickControl clickControl; 
	
	private OfflineEventDataBlock offlineClickDataBlock;
	
	private ClickDataBlock clickDataBlock;
	
	OfflineEventListPanel offlineEventListPanel;
	
	ArrayList<OfflineEventDataUnit> selectedEvents;
	
	JPanel mainPanel;
	
	private boolean disableEventTableListener; 

	public static final int SEL_ONE_EVENT = 1;
	public static final int SEL_ALL_EVENTS = 2;
	public static final int SEL_CHANGED_EVENTS = 3;
	
	public EventControlPanel(TargetMotionControl targetMotionControl){
		super(targetMotionControl);
		this.targetMotionControl=targetMotionControl;
		this.targetMotionMainPanel=targetMotionControl.getTargetMotionMainPanel();
		this.offlineClickDataBlock=(OfflineEventDataBlock) targetMotionControl.getCurrentDataBlock();
		this.clickControl=(ClickControl) offlineClickDataBlock.getParentProcess().getPamControlledUnit();
		this.clickDataBlock=clickControl.getClickDataBlock();
		
		clickControl=(ClickControl) targetMotionControl.getCurrentDataBlock().getParentProcess().getPamControlledUnit();
		
		PamPanel eventListPanel = new PamPanel(new BorderLayout());
		offlineEventListPanel = new OfflineEventListPanel(clickControl);
		offlineEventListPanel.getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		offlineEventListPanel.getPanel().setPreferredSize(new Dimension(500,150));
		eventListPanel.add(BorderLayout.CENTER, offlineEventListPanel.getPanel());
		eventListPanel.add(BorderLayout.EAST,offlineEventListPanel.getSelectionPanel());
		offlineEventListPanel.addListSelectionListener(new ListSelection());
		
		offlineEventListPanel.setShowSelection(OfflineEventListPanel.SHOW_SELECTION);
		offlineEventListPanel.tableDataChanged();
		
		PamPanel arrowButtons=new PamPanel(new GridLayout(0,1));
		BasicArrowButton arrowBack=new BasicArrowButton(BasicArrowButton.WEST);
		arrowBack.addActionListener(new ArrowListener(-1));
		BasicArrowButton arrowForward=new BasicArrowButton(BasicArrowButton.EAST);
		arrowForward.addActionListener(new ArrowListener(1));
		arrowForward.setEnabled(true);
		arrowButtons.add(arrowBack);
		arrowButtons.add(arrowForward);
		
				
		mainPanel= new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, eventListPanel);
		mainPanel.add(BorderLayout.WEST,arrowButtons);
		
	}
	
	@Override
	public void setlayerPanelEnabled(boolean enable){
		super.setlayerPanelEnabled(enable);
//		//need this to stop duplicate listenrer 
//		disableEventTableListener=!enable;
		offlineEventListPanel.setEnabled(enable);
	}

	
	
	private class ArrowListener implements ActionListener{

		private int rowMove=1;
		
		public ArrowListener(int rowMove){
			this.rowMove=rowMove;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			if (offlineEventListPanel.getTable().getRowCount()==0) return;
			
			int[] selectedRows=offlineEventListPanel.getTable().getSelectedRows();

			if (selectedRows.length==0 && rowMove>0) {
				offlineEventListPanel.getTable().setRowSelectionInterval(0, 0);
				return;
			};
			if (selectedRows.length==0 && rowMove<0) {
				offlineEventListPanel.getTable().setRowSelectionInterval(offlineEventListPanel.getTable().getRowCount()-1, offlineEventListPanel.getTable().getRowCount()-1);
				return;
			};
		
			if (selectedRows[0]+rowMove>=offlineEventListPanel.getTable().getRowCount() || selectedRows[0]+rowMove<0){
				return;
			}
			offlineEventListPanel.getTable().setRowSelectionInterval(selectedRows[0]+rowMove, selectedRows[0]+rowMove);
			 	
		}
		
	}
	
	
	/**
	 * Listens for which events are selected. Single or multiple events can be selected. These create a list of possible detections
	 * @author Jamie Macaulay
	 *
	 */
	private class ListSelection implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			if (!disableEventTableListener && !arg0.getValueIsAdjusting()){
//				System.out.println("List: SelectionListener: "+	arg0.getValueIsAdjusting());
				loadEvents();
			}
		}
	}
	

	private void loadEvents(){
			disableEventTableListener=true;
			getSelectedEvents();
			ArrayList<PamDataUnit> currentDetections= getCurrentDetections();
			disableEventTableListener=false;
			if (currentDetections.size()==0){ 
				return;
			}
			//FIXME
			//targetMotionControl.calcTMDetectionInfo(currentDetections, offlineClickDataBlock);
	}
	
	private void getSelectedEvents(){
		
			int[] selectedRows=offlineEventListPanel.getTable().getSelectedRows();
			selectedEvents= new ArrayList<OfflineEventDataUnit>();
			for (int i=0; i<selectedRows.length;i++){
				selectedEvents.add(	offlineEventListPanel.getSelectedEvent(selectedRows[i]));
			}
			
	}


	public ArrayList<PamDataUnit> getCurrentDetections() {
		
		ArrayList<PamDataUnit> currentDetections= new ArrayList<PamDataUnit>();
		
		for (int i=0; i<selectedEvents.size(); i++){
			
			///check the load time 
			checkDataLoadTime(selectedEvents.get(i)); 
			
			for (int j=0; j<selectedEvents.get(i).getSubDetectionsCount(); j++){
				currentDetections.add(selectedEvents.get(i).getSubDetection(j));
			}
			
		}
		if (selectedEvents.size()>0) offlineEventListPanel.setSelectedEvents(selectedEvents);
		
		return currentDetections;
		
	}
	
	@Override
	public void refreshData() {
		//refresh the table data to show new events. 
		offlineEventListPanel.tableDataChanged();
	}
	
	@Override
	public JPanel getPanel() {
		//get the panel containing the event table and other components. 
		return mainPanel;
	}

	@Override
	public void enableControls() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean canRun() {
		if (selectedEvents!=null) return true;
		return false;
	}
	
	@Override
	public void update(int flag) {
		super.update(flag);
		
		switch (flag){
		case TargetMotionControl.HYDROPHONE_DATA_CHANGED:
			loadEvents();
		}

	}


	@Override
	public void saveData(TargetMotionResult tmResult) {
		targetMotionControl.getTargetMotionDataBlock().addPamData(tmResult);
		targetMotionControl.getTargetMotionDataBlock().saveViewerData();
	}


	@Override
	public void setNull() {
		System.out.println("Set this localisation to Null");
		
	}
	
	
	public boolean checkDataLoadTime(OfflineEventDataUnit event) {
		if (event == null) {
			return false;
		}

		long evStart = event.getTimeMilliseconds() - 1000;
		long evEnd = event.getEventEndTime() + 1000;
		long gpsStart = evStart - 10*60*1000; // 10 minutes.

		/*
		 * First try to get there with a standard scroll of the whole thing ...
		 * Problem is that the standard scroll manager will load data
		 * asynchronously, so it may not be there !!!!!
		 */
		if (clickDataBlock.getCurrentViewDataStart() > evStart || 
				clickDataBlock.getCurrentViewDataEnd() < evEnd) {
			ViewerScrollerManager scrollManager = (ViewerScrollerManager) AbstractScrollManager.getScrollManager();
			if (scrollManager != null) {
				scrollManager.startDataAt(clickDataBlock, evStart, true);
				PamController.getInstance().notifyModelChanged(PamControllerInterface.DATA_LOAD_COMPLETE);
			}
		}

		/**
		 * There is a chance that not enough data will have been loaded if the standard
		 * display load time is lower than the event length, in which case, take a
		 * more direct approach to data loading...
		 */
		if (clickDataBlock.getCurrentViewDataStart() > evStart || 
				clickDataBlock.getCurrentViewDataEnd() < evEnd) {
			
						Debug.out.println("Loading more data for event " + event.getDatabaseIndex());
						
						clickDataBlock.getUnitsCount();
						
						Debug.out.println("data #= "+offlineClickDataBlock.getUnitsCount());
						
			//			dataBlock.loadViewerData(evStart, evEnd); // don't load the events - they are all in memory anyway. 
						clickDataBlock.loadViewerData(evStart, evEnd, null);

						Debug.out.println("data #= "+offlineClickDataBlock.getUnitsCount());
			
			PamController.getInstance().notifyModelChanged(PamControllerInterface.DATA_LOAD_COMPLETE);
		}

		/**
		 * The GPS data may be needed from some minutes earlier o work out the hydrophone
		 * position
		 */
		GPSDataBlock gpsDataBlock = (GPSDataBlock) PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
		if (gpsDataBlock == null) {
			return false;
		}
		if (gpsDataBlock.getCurrentViewDataStart() > gpsStart || gpsDataBlock.getCurrentViewDataEnd() < evEnd) {
			// need to load GPS data too
			long timeLoad=(gpsDataBlock.getCurrentViewDataEnd()-gpsDataBlock.getCurrentViewDataStart())/2;
			gpsDataBlock.loadViewerData( event.getTimeMilliseconds()-timeLoad,  event.getTimeMilliseconds()+timeLoad, null);
		}

		return true;
	}

	/**
	 * If we want to add a menu item to pop menu in another module then we need to check we can localise the detection, if so then add an option to the menu to localise the selected detection. 
	 */
	@Override
	public void addLocaliserMenuItem(JPopupMenu menu,
			PamDataUnit selectedDetection) {
		//If the click is part of an event and there's a target motion module, then have an option to localise. 
		if (selectedDetection instanceof ClickDetection ){
			if (selectedDetection.getSuperDetection(0) != null) {
				SuperDetection sd = selectedDetection.getSuperDetection(0);
				if (sd.getSubDetectionsCount() > 1) {
					JMenuItem mi = new JMenuItem(String.format("Localise %s id %d TM...", 
							clickDataBlock.getDataName(), selectedDetection.getDatabaseIndex()));
					menu.add(mi);
					mi.addActionListener(new PopMenuLocalise((ClickDetection) selectedDetection));
				}
			}
		}
	}
	
	//TODO-add menu options. 
	public class PopMenuLocalise implements ActionListener{
		
		ClickDetection detection;
		
		public PopMenuLocalise(ClickDetection detection){
			this.detection=detection;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			//TODO-make loc menu work again. 
//			if (detection.getSuperDetection(0) instanceof OfflineEventDataUnit){
//				OfflineEventDataUnit dataUnit=(OfflineEventDataUnit) detection.getSuperDetection(0);
//				selectedEvents=new ArrayList<OfflineEventDataUnit>();
//				selectedEvents.add((OfflineEventDataUnit) dataUnit);
//				
//				ArrayList<PamDetection> currentDetections= getCurrentDetections();
//				if (currentDetections.size()==0){ 
//					targetMotionInfo=null;
//					return;
//				}
//				targetMotionInfo=new AbstractTargetMotionInformation(getCurrentDetections(),clickDataBlock);
//				
//				//disable the table listener
//				disableEventTableListener=true;
//				offlineEventListPanel.setShowSelection(OfflineEventListPanel.SHOW_ALL);
//				offlineEventListPanel.tableDataChanged();
//				offlineEventListPanel.setSelectedEvents(selectedEvents);
//				disableEventTableListener=false;
//				
//				notifyUpdate(TargetMotionControl.CURRENT_DETECTIONS_CHANGED);
//				
//				//now move to this tab
//				targetMotionControl.gotoTab();
//			}
		}
		
	}

	@Override
	public boolean canLocalise(PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<PamDataUnit> getTargetMotionInfo() {
		return getCurrentDetections() ;
	}
	
}


	
//	/**
//	 * Get a menu item to insert into a pop-up menu for a single event
//	 * (i.e. right click on  click train and get this menu).
//	 * Use the default datablock data name in the menu title.  
//	 * @param pamDetection detection to include in menu action. 
//	 * @return menu item or null if action on the detection is not possible
//	 */
//	public JMenuItem getEventMenuItem(T pamDetection) {
//		PamDataBlock dataBlock = pamDetection.getParentDataBlock();
//		if (dataBlock == null) {
//			return null;
//		}
//		return getEventMenuItem(pamDetection, dataBlock.getDataName());
//	}
//	
//
//	/**
//	 * Get a menu item to insert into a pop-up menu for a single event 
//	 * using a specific name for the data (database index will be appended
//	 * to this name)
//	 * @param pamDetection data unit to include in the menu action
//	 * @param dataName data name to include in the meny text
//	 * @return menu item or null if action on the detection is not possible 
//	 */
//	public JMenuItem getEventMenuItem(T pamDetection, String dataName) {
//		if (pamDetection == null) {
//			return null;
//		}
//		JMenuItem mi = new JMenuItem(String.format("Localise %s id %d", 
//				dataName, pamDetection.getDatabaseIndex()));
//		mi.addActionListener(new LocaliseEvent(pamDetection));
//		return mi;
//	}

	
	
	