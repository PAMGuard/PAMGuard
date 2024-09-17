package targetMotionOld;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import Localiser.LocalisationAlgorithm;
import Localiser.LocalisationAlgorithmInfo;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.AbstractLocaliser;
import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.debug.Debug;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.ViewerScrollerManager;
import targetMotionOld.algorithms.LeastSquaresNew;
import targetMotionOld.algorithms.Simplex2DNew;
import targetMotionOld.algorithms.Simplex3DNew;
import targetMotionOld.dialog.TargetMotionDialog;
import targetMotionOld.tethys.TMALocalizationCreator;
import tethys.localization.LocalizationCreator;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public class TargetMotionLocaliser<T extends GroupDetection> extends AbstractLocaliser<T> implements LocalisationAlgorithm, LocalisationAlgorithmInfo  {

	public enum Interractive {START, SAVE, BACK, CANCEL, SETNULL, KEEPOLD}
	//	public enum WorkStatus {IDLE, LOADING, WAITING};

	private TargetMotionDialog<T> targetMotionDialog;
	private PamControlledUnit pamControlledUnit;
	private PamDataBlock<T> dataBlock;
	private PamDataBlock subDetectionBlock;
	private ArrayList<TargetMotionModel<T>> models;
	private ArrayList<GroupLocResult> results = new ArrayList<GroupLocResult>();
	private int bestResultIndex = -1;
	private EventLocaliserWorker eventLocaliserWorker;

	//	private Object dataSynchObject = new Object();
	//	/**
	//	 * @return the dataSynchObject
	//	 */
	//	public Object getDataSynchObject() {
	//		return dataSynchObject;
	//	}

	/**
	 * Database index of current event. Not the same as the 
	 * eventListIndex
	 * <p> Use index instead of reference since the data are often reloaded, so references become 
	 * out of date. 
	 */
	public int currentEventIndex;
	//	private T currentEvent;
	//private WorkStatus workStatus;
	private TMALocalizationCreator tmaLocalizationCreator;

	public TargetMotionLocaliser(PamControlledUnit pamControlledUnit, PamDataBlock<T> dataBlock, PamDataBlock subDetectionBlock) {
		super(dataBlock);
		this.pamControlledUnit = pamControlledUnit;
		this.dataBlock = dataBlock;
		this.subDetectionBlock = subDetectionBlock;
		dataBlock.addObserver(new DataObserver(), false);
		models = new ArrayList<TargetMotionModel<T>>();
//		models.add(new Simplex2D<T>(this));
//		models.add(new Simplex3D<T>(this));
		models.add(new LeastSquaresNew<T>(this));
		models.add(new Simplex2DNew<T>(this));
		models.add(new Simplex3DNew<T>(this));
	}

	@Override
	public String getLocaliserName() {
		return "Target Motion Localiser";
	}

	@Override
	public LocalisationAlgorithmInfo getAlgorithmInfo() {
		return this;
	}

	@Override
	public boolean localiseDataUnit(T dataUnit) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Get a menu item to insert into a pop-up menu for a single event
	 * (i.e. right click on  click train and get this menu).
	 * Use the default datablock data name in the menu title.  
	 * @param pamDetection detection to include in menu action. 
	 * @return menu item or null if action on the detection is not possible
	 */
	public JMenuItem getEventMenuItem(T pamDetection) {
		PamDataBlock dataBlock = pamDetection.getParentDataBlock();
		if (dataBlock == null) {
			return null;
		}
		return getEventMenuItem(pamDetection, dataBlock.getDataName());
	}

	/**
	 * Get a menu item to insert into a pop-up menu for a single event 
	 * using a specific name for the data (database index will be appended
	 * to this name)
	 * @param pamDetection data unit to include in the menu action
	 * @param dataName data name to include in the meny text
	 * @return menu item or null if action on the detection is not possible 
	 */
	public JMenuItem getEventMenuItem(T pamDetection, String dataName) {
		if (pamDetection == null) {
			return null;
		}
		JMenuItem mi = new JMenuItem(String.format("Localise %s id %d", 
				dataName, pamDetection.getDatabaseIndex()));
		mi.addActionListener(new LocaliseEvent(pamDetection));
		return mi;
	}

	private class LocaliseEvent implements ActionListener {
		T pamDetection;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			showTMDialog(pamDetection);
		}
		/**
		 * @param pamDetection
		 */
		public LocaliseEvent(T pamDetection) {
			super();
			this.pamDetection = pamDetection;
		}
	}

	public boolean showTMDialog(T dataUnit) {
		if (targetMotionDialog == null) {
			targetMotionDialog = new TargetMotionDialog<T>(pamControlledUnit.getGuiFrame(), this);
		}
		targetMotionDialog.updateEventList();
		targetMotionDialog.setDataUnit(dataUnit);
		targetMotionDialog.setVisible(true);
		return true;
	}

	public int addDetectorMenuItems(Frame parentFrame, JMenu menu) {
		JMenuItem menuItem;
		menuItem = new JMenuItem("Target Motion Analysis ...");
		menuItem.addActionListener(new ShowDialog(parentFrame));
		menu.add(menuItem);
		return 1;
	}

	class ShowDialog implements ActionListener {
		private Frame parentFrame;

		public ShowDialog(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showTMDialog(null);
		}

	}

	public boolean checkDataLoadTime(T event) {
		if (event == null) {
			return false;
		}

		long evStart = event.getTimeMilliseconds() - 1000;
		long evEnd = event.getEventEndTime() + 1000;
		long gpsStart = evStart - 10*60*1000; // 10 minutes.
		long gpsEnd = evEnd;

		/*
		 * First try to get there with a standard scroll of the whole thing ...
		 * Problem is that the standard scroll manager will load data
		 * asynchronously, so it may not be there !!!!!
		 */
		if (subDetectionBlock.getCurrentViewDataStart() > evStart || 
				subDetectionBlock.getCurrentViewDataEnd() < evEnd) {
			ViewerScrollerManager scrollManager = (ViewerScrollerManager) AbstractScrollManager.getScrollManager();
			if (scrollManager != null) {
				scrollManager.startDataAt(subDetectionBlock, evStart, true);
				PamController.getInstance().notifyModelChanged(PamControllerInterface.DATA_LOAD_COMPLETE);
			}
		}

		/**
		 * There is a chance that not enough data will have been loaded if the standard
		 * display load time is lower than the event length, in which case, take a
		 * more direct approach to data loading...
		 */
		if (subDetectionBlock.getCurrentViewDataStart() > evStart || 
				subDetectionBlock.getCurrentViewDataEnd() < evEnd) {
			
			
			Debug.out.println("loading more data for event " + event.getDatabaseIndex());
						
						subDetectionBlock.getUnitsCount();
						
						Debug.out.println("data #= "+subDetectionBlock.getUnitsCount());
						
			//			dataBlock.loadViewerData(evStart, evEnd); // don't load the events - they are all in memory anyway. 
			subDetectionBlock.loadViewerData(evStart, evEnd, null);

			Debug.out.println("data #= "+subDetectionBlock.getUnitsCount());
			
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
		if (gpsDataBlock.getCurrentViewDataStart() > gpsStart || gpsDataBlock.getCurrentViewDataEnd() < gpsEnd) {
			// need to load GPS data too
			gpsDataBlock.loadViewerData(gpsStart, gpsEnd, null);
		}
		
		 // We need at least 1 GPS data unit before the event start, and another one after the event end.  If we don't have that, load more data until we have enough
		if (gpsDataBlock.getUnitsCount()<1 || gpsDataBlock.getFirstUnit().getTimeMilliseconds()>evStart || gpsDataBlock.getLastUnit().getTimeMilliseconds()<evEnd) {
			boolean foundEnough = false;
			while (!foundEnough && (evStart-gpsStart < 60*60*1000) && (gpsEnd-evEnd < 60*60*1000)) { // don't go more than an hour in either direction
				if (gpsDataBlock.getUnitsCount()<1) {
					gpsStart-= 10*60*1000; // another 10 minutes.
					gpsEnd += 10*60*1000; // another 10 minutes.
				} else {
					if (gpsDataBlock.getFirstUnit().getTimeMilliseconds()>evStart) {
						gpsStart-= 10*60*1000; // another 10 minutes.
					}
					if (gpsDataBlock.getLastUnit().getTimeMilliseconds()<evEnd) {
						gpsEnd += 10*60*1000; // another 10 minutes.
					}
				}
				gpsDataBlock.loadViewerData(gpsStart, gpsEnd, null);
				if (gpsDataBlock.getUnitsCount()>0 && gpsDataBlock.getFirstUnit().getTimeMilliseconds()<=evStart && gpsDataBlock.getLastUnit().getTimeMilliseconds()>=evEnd) {
					foundEnough = true;
				}
			}
		}
		

		return true;
	}

	public void clearResults() {
		results.clear();
		setBestResultIndex(-1);
		if (targetMotionDialog != null) {
			targetMotionDialog.notifyNewResults();
		}
	}

	public ArrayList<GroupLocResult> getResults() {
		return results;
	}

	public void addResults(GroupLocResult[] newResults) {
		for (int i = 0; i < newResults.length; i++) {
			if (newResults[i] != null) {
				results.add(newResults[i]);
			}
		}
		//		if (targetMotionDialog != null) {
		//			targetMotionDialog.notifyNewResults();
		//		}
	}

	/**
	 * @return the bestResultIndex
	 */
	public int getBestResultIndex() {
		return bestResultIndex;
	}

	/**
	 * @param bestResultIndex the bestResultIndex to set
	 */
	public void setBestResultIndex(int bestResultIndex) {
		this.bestResultIndex = bestResultIndex;
	}

	/**
	 * @return the dataBlock
	 */
	@Override
	public PamDataBlock<T> getDataBlock() {
		return dataBlock;
	}

	public ArrayList<TargetMotionModel<T>> getModels() {
		return models;
	}

	/**
	 * Find a model by it's name. If shortLength is true, then 
	 * it will accept a match in which the modelName is only partly
	 * included in the full model name. This is required since the names in 
	 * the database may have been truncated and are therefore incomplete. 
	 * @param modelName model name to search for. 
	 * @param shortLength allow short model names (if truncated in the database)
	 * @return reference to a TM model, or null. 
	 */
	public TargetMotionModel findModelByName(String modelName, boolean shortLength) {
		if (modelName == null || modelName.length() == 0) {
			return null;
		}
		String name;
		for (int i = 0; i < models.size(); i++) {
			name = models.get(i).getName();
			if (name.equals(modelName)) {
				return models.get(i);
			}
			if (shortLength) {
				if (name.startsWith(modelName)) {
					return models.get(i);
				}
			}
		}
		return null;
	}

	/**
	 * Called when data in the main source data block are changed
	 * @param pamDetection
	 */
	public void dataChanged(T pamDetection) {
		if (targetMotionDialog != null) {
			targetMotionDialog.dataChanged(pamDetection);
		}
	}

	public void localiseEventList(int[] eventList, TargetMotionModel<T>[] modelList, boolean isSupervised) {
		eventLocaliserWorker = new EventLocaliserWorker(eventList, modelList, isSupervised);
		eventLocaliserWorker.execute();
	}

	public void interractiveCommand(Interractive interractive) {
		if (eventLocaliserWorker != null) {
			eventLocaliserWorker.interractiveCommand(interractive);
		}
	}

	private class EventLocaliserWorker extends SwingWorker<Integer, EventLocalisationProgress> {

		private int[] eventList;
		private TargetMotionModel<T>[] modelList;
		private boolean supervised;
		private int eventListIndex;
		List<Interractive> commandList = Collections.synchronizedList(new LinkedList<Interractive>());

		/**
		 * @param eventList
		 * @param isSupervised
		 */
		public EventLocaliserWorker(int[] eventList, TargetMotionModel<T>[] modelList, boolean isSupervised) {
			super();
			this.eventList = eventList;
			this.modelList = modelList;
			this.supervised = isSupervised;
		}

		/**
		 * Get the next command from a list. 
		 * @param maxWait max wait time in milliseconds (-1 to wait forever) 
		 * @return command
		 */
		public Interractive getCommand(long maxWait) {
			updateGUI(new EventLocalisationProgress(EventLocalisationProgress.WAITING, -1));
			long startTime = System.currentTimeMillis();
			long wait;
			while (commandList.size() == 0) {
				wait = System.currentTimeMillis() - startTime;
				if (maxWait >= 0 && wait >= maxWait) {
					return null;
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (commandList.size() == 0) {
				return null;
			}
			return commandList.remove(0);
		}

		/**
		 * Add a command to a command list. 
		 * @param interractive command
		 */
		public void interractiveCommand(Interractive interractive) {
			commandList.add(interractive);
		}

		@Override
		protected void done() {
			// TODO Auto-generated method stub
			super.done();
			updateGUIAWT(new EventLocalisationProgress(EventLocalisationProgress.DONE, -1));
		}

		@Override
		protected void process(List<EventLocalisationProgress> chunks) {
			//			for (int i = 0; i < chunks.size(); i++) {
			//				if (targetMotionDialog != null) {
			//					targetMotionDialog.progressReport(chunks.get(i));
			//				}
			//				System.out.println(chunks.get(i).toString());
			//			}
		}

		@Override
		protected Integer doInBackground() throws Exception {
			T anEvent;
			eventListIndex = 0;
			Interractive cmd;
			String comment = null;
			for (eventListIndex = 0; eventListIndex < eventList.length; eventListIndex++) {
				anEvent = findEvent(eventList[eventListIndex]);
				if (!processEvent(anEvent, modelList)) {
					return null;
				}
				if (supervised) {
					cmd = getCommand(-1);
					if (targetMotionDialog != null) {
						comment = targetMotionDialog.getUserComment();
					}
				}
				else {
					cmd = getCommand(0);
					if (cmd != null && cmd == Interractive.CANCEL) {
						return null;
					}
					else {
						cmd = Interractive.SAVE;
						comment = "Unsupervised model selection";
					}
				}
				switch(cmd) {
				case CANCEL:
					return null;
				case BACK:
					eventListIndex--;
					break;
				case SETNULL:
					anEvent.setLocalisation(null);
					break;
				case KEEPOLD:
					break; // just leave the old one in place. 
				case SAVE:
					if (getBestResultIndex() >= 0) {
						anEvent = findEvent(currentEventIndex);
						GroupLocResult tmr = results.get(getBestResultIndex());
						tmr.setComment(comment);
						anEvent.setLocalisation(new GroupLocalisation(anEvent, tmr));
						dataBlock.updatePamData(anEvent, anEvent.getTimeMilliseconds());
					}
					break;
				}
			}
			return null;
		}

		/**
		 * Process a single event. Return false if some kind of user input 
		 * indicates that further processing should stop. 
		 * @param anEvent
		 * @return true if processing should continue. 
		 */
		private boolean processEvent(T anEvent, TargetMotionModel<T>[] modelList) {
			if (anEvent.getDatabaseIndex() != currentEventIndex) {
				updateGUI(new EventLocalisationProgress(EventLocalisationProgress.LOADING_EVENT_DATA, anEvent.getDatabaseIndex()));
				checkDataLoadTime(anEvent);
				//				currentEvent = anEvent;
				//				setCurrentEventIndex(anEvent.getDatabaseIndex(), this);
			}
			clearResults();

			updateGUI(new EventLocalisationProgress(EventLocalisationProgress.LOADED_EVENT_DATA, anEvent.getDatabaseIndex()));

			// may need to find that event again since it may have reloaded
			// itself with a different reference
			T wasEvent = anEvent;
			anEvent = findEvent(currentEventIndex);
			//			if (wasEvent != anEvent) {
			//				System.out.println(String.format("Event ref change id %d, %d, %s, %s", wasEvent.getDatabaseIndex(),
			//						anEvent.getDatabaseIndex(), wasEvent.toString(), anEvent.toString()));
			//			}
			GroupLocResult[] results = runModels(anEvent, null, modelList);

			int bestResult = selectBestResult(results);

			setBestResultIndex(bestResult);

			updateGUI(new EventLocalisationProgress(EventLocalisationProgress.GOT_RESULTS, anEvent.getDatabaseIndex()));

			return true;
		}

	}



	//	public int getCurrentEventIndex() {
	//		return currentEventIndex;
	//	}
	private void updateGUI(EventLocalisationProgress elProgress) {
		try {
			SwingUtilities.invokeAndWait(new UpdateGui(elProgress));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	private void updateGUIAWT(EventLocalisationProgress elProgress) {
		if (elProgress != null && elProgress.progressType == EventLocalisationProgress.LOADED_EVENT_DATA) {
			setCurrentEventIndex(elProgress.eventIndex, null);
		}
		if (targetMotionDialog != null) {
			targetMotionDialog.progressReport(elProgress);
			////through to dialogue map
		}
	}
	private class UpdateGui implements Runnable {
		private EventLocalisationProgress elProgress;
		/**
		 * @param elProgress
		 */
		public UpdateGui(EventLocalisationProgress elProgress) {
			super();
			this.elProgress = elProgress;
		}
		@Override
		public void run() {
			updateGUIAWT(elProgress);

		}
	}
	
	public T getCurrentEvent() {
		return findEvent(currentEventIndex);
	}
	
	// returns the event 
	public int getCurrentEventIndex(){
		return currentEventIndex;
	}
	
	

	public void setCurrentEventIndex(int currentEventIndex, Object sender) {
		//		this.currentEventIndex = currentEventIndex;
		this.currentEventIndex = currentEventIndex;
		T anEvent = findEvent(currentEventIndex);
		if (anEvent == null) {
			return;
		}
		checkDataLoadTime(anEvent);
		if (targetMotionDialog != null) {
			targetMotionDialog.setCurrentEventIndex(currentEventIndex, sender);
		}
	}

	public GroupLocResult[] runModels(T anEvent, DetectionGroupOptions detectionGroupOptions, TargetMotionModel<T>[] modelList) {
		if (modelList == null) {
			return null;
		}
		int nModels = modelList.length;
		TargetMotionModel<T> model;
		GroupLocResult[] results;
		GroupLocResult[] allResults = new GroupLocResult[0];
		GroupLocalisation groupLoc; 
		long t1, t2;
		for (int i = 0; i < nModels; i++) {
			model = modelList[i];
			results=null;
			t1 = System.nanoTime();
			try{
				groupLoc = (GroupLocalisation) model.runModel(anEvent, detectionGroupOptions, false);
				if (groupLoc != null) {
					results= groupLoc.getGroupLocResults();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			t2 = System.nanoTime();
			


			if (results != null) {
				for (int m = 0; m < results.length; m++) {
					if (results[m] == null) {
						continue;
					}
					results[m].setRunTimeMillis((t2-t1)/1.e6);
					allResults = Arrays.copyOf(allResults, allResults.length + 1);
					allResults[allResults.length-1] = results[m];
				}
				addResults(results);					
			}
		}
		
		return allResults;
	}

	private class DataObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "Target Motion Analysis";
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			dataChanged((T) arg);
		}
		
	}

	/**
	 * Find an event from it's database index.
	 * @param databaseIndex
	 */
	public T findEvent(int databaseIndex) {
		T dataUnit = dataBlock.findByDatabaseIndex(databaseIndex);
		return dataUnit;
	}

	/**
	 * Work out which is the best result based on Chi2 and AIC. 
	 * @param results array of results to compare. 
	 * @return index of best result, or -1 if there are no results or none with AIC or Chi2 values. 
	 */
	public int selectBestResult(GroupLocResult[] results) {
		if (results == null || results.length < 1) {
			return -1;
		}
		int best = 0;
		GroupLocResult bestRes;
		GroupLocResult aRes;
		// decide on AIC if possible, if not use Chi2.
		for (int i = 1; i < results.length; i++) {
			bestRes = results[best];
			aRes = results[i];
			if (aRes.getAic() != null) {
				if (bestRes.getAic() == null) {		
					best = i;
				}
				else if (aRes.getAic() < bestRes.getAic()) {
					best = i;
				}
				continue;
			}
			else if (aRes.getChi2() != null) {
				if (bestRes.getChi2() == null) {
					best = i;
				}
				else if (aRes.getChi2() < bestRes.getChi2()) {
					best = i;
				}
			}

		}

		bestRes = results[best];
		if (bestRes.getChi2() == null && bestRes.getAic() == null) {
			return -1;
		}
		return best;
	}

	/**
	 * Get the localisation from an event. This will only return 
	 * if the localisation is of the correct type. Can be used
	 * to avoid exceptions if some other sort of localisation 
	 * has been added to an event
	 * @param event
	 * @return localisation or null if there isn't one or it's the wrong type. 
	 */
	public TargetMotionLocalisation getTMLocalisation(T event) {
		AbstractLocalisation l = event.getLocalisation();
		if (l == null) {
			return null;
		}
		if (TargetMotionLocalisation.class.isAssignableFrom(l.getClass())) {
			return (TargetMotionLocalisation) l;
		}
		else {
			return null;
		}
	}

	@Override
	public int getLocalisationContents() {
		return LocContents.HAS_LATLONG | LocContents.HAS_AMBIGUITY | LocContents.HAS_DEPTH;
	}

	@Override
	public String getAlgorithmName() {
		return "Target Motion Localiser";
	}

	@Override
	public LocalizationCreator getTethysCreator() {
		if (tmaLocalizationCreator == null) {
			tmaLocalizationCreator = new TMALocalizationCreator(this);
		}
		return tmaLocalizationCreator;
	}
	

	@Override
	public Serializable getParameters() {
		// TODO Auto-generated method stub
//		return new BearingLocaliserParams();
		return null;
	}
}
