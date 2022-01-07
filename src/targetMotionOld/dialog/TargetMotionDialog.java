package targetMotionOld.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import targetMotionOld.EventLocalisationProgress;
import targetMotionOld.TargetMotionLocaliser;
import targetMotionOld.TargetMotionModel;
import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import PamDetection.PamDetection;
import PamView.ColorManaged;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamguardMVC.PamDataBlock;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public class TargetMotionDialog<T extends GroupDetection> extends PamDialog implements ColorManaged {
	
	private TargetMotionLocaliser<T> targetMotionLocaliser;
	private TMModelControlPanel modelControlPanel;
	private EventControlPanel eventControlPanel;
	private ModelResultPanel modelResultPanel;
	private DialogMap<T> dialogMap2D, dialogMap3D, currentMap;
	TMDialogComponent[] dialogComponents = new TMDialogComponent[5];
	private JPanel mapBorder;


	public TargetMotionDialog(Window parentFrame, TargetMotionLocaliser<T> targetMotionLocaliser) {
		super(parentFrame, "Target Motion Analysis", false);

		this.targetMotionLocaliser = targetMotionLocaliser;
		
		modelControlPanel = new TMModelControlPanel(targetMotionLocaliser, this);
		eventControlPanel = new EventControlPanel(targetMotionLocaliser, this);
		modelResultPanel = new ModelResultPanel(targetMotionLocaliser, this);
	
	
		mapBorder = new JPanel(new BorderLayout());
		JPanel mapCtrl = new JPanel();
		mapCtrl.setLayout(new FlowLayout());
		
		//Create the map panels. 
		dialogMap2D = new DialogMap2D<T>(targetMotionLocaliser, this);
		//Need to put a try catch here as Java3D is slowly dying 
		try{
			dialogMap3D = new DialogMap3DSwing(targetMotionLocaliser, this);
//			dialogMap3D = new DialogMap3D<T>(targetMotionLocaliser, this);
			setMap(dialogMap3D);
		}
		catch (Exception e){
			System.out.println("Java3D Map failed to initialise.");
			e.printStackTrace();
			setMap(dialogMap2D);
		}
		
		JRadioButton b;
		ButtonGroup bg = new ButtonGroup();
		mapCtrl.add(b = new JRadioButton("2D Map"));
		b.addActionListener(new MapSelAction(dialogMap2D));
		if (dialogMap3D!=null){
			bg.add(b);
			mapCtrl.add(b = new JRadioButton("3D Map"));
			b.addActionListener(new MapSelAction(dialogMap3D));
		}
		b.setSelected(true);
		bg.add(b);
//		JButton s = new JButton("Map Settings");
//		mapCtrl.add(s);
//		s.addActionListener(new MapSettings());
		mapBorder.add(BorderLayout.NORTH, mapCtrl);
		
		
		dialogComponents[0] = modelControlPanel;
		dialogComponents[1] = eventControlPanel;
		dialogComponents[2] = modelResultPanel;
		dialogComponents[3] = dialogMap2D;
		dialogComponents[4] = dialogMap3D;
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, eventControlPanel.getPanel());
		mainPanel.add(BorderLayout.WEST, modelControlPanel.getPanel());
		mainPanel.add(BorderLayout.SOUTH, modelResultPanel.getPanel());
		mainPanel.add(BorderLayout.CENTER, mapBorder);
		
		getOkButton().setVisible(false);
		getCancelButton().setText("Close");
		setDialogComponent(mainPanel);
		setModal(false);
		setResizable(true);		

		setHelpPoint("localisation.targetmotion.docs.targetmotion_overview");
		
		enableControls();
	}
	
	private int visCount = 0;
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
//		if (visible) {
//			if (visCount == 0) {
//				setMap(dialogMap3D);
//				setMap(dialogMap2D);
//			}
//			visCount++;
//		}
	}
	
	private static int mapErrorCount = 0;
	void setMap(DialogMap<T> maptype){
		if (currentMap != null) {
			currentMap.showMap(false);
			mapBorder.remove(currentMap.getPanel());
			
		}
		try {
			mapBorder.add(BorderLayout.CENTER, maptype.getPanel());
		}
		catch (IllegalArgumentException e) {
			System.out.println("Map graphics error: " + e.getMessage());
			if (maptype.getClass() == DialogMap3DSwing.class) {
				dialogMap3D = new DialogMap3DSwing<T>(targetMotionLocaliser, this);
				if (++mapErrorCount < 2) {
					setMap(dialogMap3D);
				}
				return;
			}
			if (maptype.getClass() == DialogMap2D.class) {
				dialogMap2D = new DialogMap2D<T>(targetMotionLocaliser, this);
				if (++mapErrorCount < 2) {
					setMap(dialogMap2D);
				}
				return;
			}
		}
		currentMap=maptype;
		pack();
		maptype.showMap(true);
		mapErrorCount = 0;
	}
	
	
	

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	/**
	 * Select a sepcific data unit in the drop down menu. 
	 * @param dataUnit
	 * @return
	 */
	public boolean setDataUnit(T dataUnit) {
		if (dataUnit != null) {
			targetMotionLocaliser.setCurrentEventIndex(dataUnit.getDatabaseIndex(), null);
		}
		return false;
	}
	
	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Called when data in the main source data block are changed
	 * @param pamDetection
	 */
	public void dataChanged(T pamDetection) {
		if (pamDetection == null) {
			return;
		}
//		if (pamDetection.getDatabaseIndex() == currentEventIndex) {
////			setCurrentEventIndex(pamDetection.getDatabaseIndex(), null);
//		}
	}
	
	public void updateEventList() {
		eventControlPanel.updateEventList();
	}

	public void setCurrentEventIndex(int currentEventIndex, Object sender) {
		targetMotionLocaliser.clearResults();
//		if (sender != targetMotionLocaliser) {
//			targetMotionLocaliser.setCurrentEventIndex(currentEventIndex, sender);
//		}
		for (int i = 0; i < dialogComponents.length; i++) {
			if (dialogComponents[i] == null) continue;
			if (dialogComponents[i] == sender) continue;
			dialogComponents[i].setCurrentEventIndex(currentEventIndex, sender);
		}
	}
	
	public void enableControls() {
		for (int i = 0; i < dialogComponents.length; i++) {
			if (dialogComponents[i] == null) continue;
			dialogComponents[i].enableControls();
		}
		
	}
	public boolean canRun() {
		for (int i = 0; i < dialogComponents.length; i++) {
			if (dialogComponents[i] == null) continue;
			if (dialogComponents[i].canRun() == false) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Start a run. Will need to get options from various sub panels to 
	 * work out what to actually do.
	 * <p>
	 * Currently just do it all in this thread. In future, will try to rethread 
	 * so multiple models can run concurrently.  
	 */
	public void start(boolean runAll) {
		int[] eventList = createEventList(runAll);
		if (eventList == null) {
			return;
		}
		TargetMotionModel<T>[] modelList = new TargetMotionModel[0];

		ArrayList<TargetMotionModel<T>> models = targetMotionLocaliser.getModels();
		int nModels = models.size();
		TargetMotionModel<T> model;
		GroupLocResult[] results;
		GroupLocResult[] allResults = new GroupLocResult[0];
		long t1, t2;
		for (int i = 0; i < nModels; i++) {
			model = models.get(i);
			if (modelControlPanel.isEnabled(i)) {
				modelList = Arrays.copyOf(modelList, modelList.length+1);
				modelList[modelList.length-1] = model;
			}
		}
		
		targetMotionLocaliser.localiseEventList(eventList, modelList, eventControlPanel.isSupervised());		
	}
	
	/**
	 * Create an event list for the target motion analysis based on selection 
	 * options ...
	 * <p> List by event number since events are likely to be re-loaded as 
	 * we scroll through data, so references to objects will become out of date. 
	 * @param runAll 
	 * @return list of events to localise, or null if no events. 
	 */
	private int[] createEventList(boolean runAll) {
		int[] eventList = new int[0];
		PamDataBlock<T> dataBlock;
		T dataUnit;
		ListIterator<T> it;
//		switch(selType) {
//		case EventControlPanel.SEL_ONE_EVENT:
////			T currentEvent = targetMotionLocaliser.findEvent(targetMotionLocaliser.getCurrentEventIndex());
//			T currentEvent = targetMotionLocaliser.getCurrentEvent();
//			if (currentEvent != null) {
//				eventList = new int[1];
//				eventList[0] = currentEvent.getDatabaseIndex();
//			}
//			break;
//		case EventControlPanel.SEL_ALL_EVENTS:
//			dataBlock = targetMotionLocaliser.getDataBlock();
//			 it = dataBlock.getListIterator(0);
//			while (it.hasNext()) {
//				dataUnit = it.next();
//				eventList = Arrays.copyOf(eventList, eventList.length+1);
//				eventList[eventList.length-1] = dataUnit.getDatabaseIndex();;
//			}
//			break;
//		case EventControlPanel.SEL_CHANGED_EVENTS:
//			dataBlock = targetMotionLocaliser.getDataBlock();
//			it = dataBlock.getListIterator(0);
//			while (it.hasNext()) {
//				dataUnit = it.next();
//				if (dataUnit.getUpdateCount() > 0) {
//					eventList = Arrays.copyOf(eventList, eventList.length+1);
//					eventList[eventList.length-1] = dataUnit.getDatabaseIndex();
//				}
//			}
//			break;
//		}
		if (runAll) {
			dataBlock = targetMotionLocaliser.getDataBlock();
			it = dataBlock.getListIterator(0);
			while (it.hasNext()) {
				dataUnit = it.next();
				eventList = Arrays.copyOf(eventList, eventList.length+1);
				eventList[eventList.length-1] = dataUnit.getDatabaseIndex();;
			}
		}
		else {
			T currentEvent = targetMotionLocaliser.getCurrentEvent();
			if (currentEvent != null) {
				eventList = new int[1];
				eventList[0] = currentEvent.getDatabaseIndex();
			}
		}

		return eventList;
	}

	

	public void notifyNewResults() {
		currentMap.notifyNewResults();
		modelResultPanel.notifyNewResults();
		eventControlPanel.notifyNewResults();
	}

	/**
	 * Called from the localisation worker thread each time an event is loaded, started, 
	 * completed, etc. 
	 * @param eventLocalisationProgress
	 */
	public void progressReport(EventLocalisationProgress eventLocalisationProgress) {
		eventControlPanel.progressReport(eventLocalisationProgress);
		switch (eventLocalisationProgress.progressType) {
		case EventLocalisationProgress.GOT_RESULTS:
			notifyNewResults();
			break;
		case EventLocalisationProgress.LOADED_EVENT_DATA:
			notifyNewResults();
			break;
		case EventLocalisationProgress.LOADING_EVENT_DATA:
			
			break;

		default:
			notifyNewResults();
			break;
		}
	}

	public String getUserComment() {
		return eventControlPanel.getComment();
	}

	class MapSelAction implements ActionListener {

		private DialogMap dialogMap;
		public MapSelAction(DialogMap dialogMap) {
			super();
			this.dialogMap = dialogMap;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			setMap(dialogMap);
			
			
		}
		
	}

	class MapSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			currentMap.settings();
		}
	}

	@Override
	public PamColor getColorId() {
		return PamColor.BORDER;
	}
}
