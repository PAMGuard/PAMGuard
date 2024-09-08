package targetMotionModule;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import Localiser.algorithms.timeDelayLocalisers.bearingLoc.AbstractLocaliser;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import targetMotionModule.algorithms.LeastSquares;
import targetMotionModule.algorithms.MarkovChain;
import targetMotionModule.algorithms.Simplex2D;
import targetMotionModule.algorithms.Simplex3D;
import targetMotionModule.algorithms.Simplex3DTD;
import targetMotionModule.algorithms.TargetMotionModel;

public class TargetMotionLocaliser<T extends PamDataUnit> extends AbstractLocaliser {

	public enum Interractive {START, SAVE, BACK, CANCEL, SETNULL, SAVE_ALL}
	//	public enum WorkStatus {IDLE, LOADING, WAITING};

	private TargetMotionControl targetMotionControl;
	private ArrayList<TargetMotionModel> models;
	private ArrayList<TargetMotionResult> results = new ArrayList<TargetMotionResult>();
	private int bestResultIndex = -1;
	private TMLocaliserWorker eventLocaliserWorker;
	private int currentRunState=TargetMotionControl.LOCALISATION_DONE;

	/**
	 * Database index of current event. Not the same as the 
	 * eventListIndex
	 * <p> Use index instead of reference since the data are often reloaded, so references become 
	 * out of date. 
	 */
	public int currentEventIndex;
	private ArrayList<TargetMotionInformation> currentTargetMotionInfo;
	//	private T currentEvent;
	//private WorkStatus workStatus;

	public TargetMotionLocaliser(TargetMotionControl targetMotionControl, PamDataBlock<T> dataBlock) {
		super(dataBlock);
		this.targetMotionControl = targetMotionControl;
		models = new ArrayList<TargetMotionModel>();
		models.add(new LeastSquares(this));
		models.add(new Simplex2D(this));
		models.add(new Simplex3D(this));
		models.add(new Simplex3DTD(this));
		models.add(new MarkovChain(this));
	}

	@Override
	public String getLocaliserName() {
		return "Target Motion Localiser";
	}

//	public boolean showTMDialog(T dataUnit) {
//		if (targetMotionMainPanel == null) {
////targetMotionDialog = new TargetMotionMainPanel<T>(pamControlledUnit.getGuiFrame(), this);
//		}
//		targetMotionMainPanel.updateCurrentControlPanel();
//		return true;
//	}

	public int addDetectorMenuItems(Frame parentFrame, JMenu menu) {
		JMenuItem menuItem;
		menuItem = new JMenuItem("Target Motion Analysis ...");
		menuItem.addActionListener(new MoveToTargetMotion(parentFrame));
		menu.add(menuItem);
		return 1;
	}

	class MoveToTargetMotion implements ActionListener {
		private Frame parentFrame;

		public MoveToTargetMotion(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			 moveToTargetMotionTab();
		}

	}
	
	public void moveToTargetMotionTab(){
		
	}

	public void clearResults() {
		results.clear();
		setBestResultIndex(-1);
		if (targetMotionControl.getTargetMotionMainPanel()!=null){
		targetMotionControl.getTargetMotionMainPanel().getMap3D().update(TargetMotionControl.LOCALISATION_STARTED);
		targetMotionControl.getTargetMotionMainPanel().getMap2D().update(TargetMotionControl.LOCALISATION_STARTED);
		}
	
	}

	public ArrayList<TargetMotionResult> getResults() {
		return results;
	}

	public void addResults(TargetMotionResult[] newResults) {
		for (int i = 0; i < newResults.length; i++) {
			if (newResults[i] != null) {
				results.add(newResults[i]);
			}
		}
		//now update the maps
		targetMotionControl.getTargetMotionMainPanel().notifyNewResults();
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

	public ArrayList<TargetMotionModel> getModels() {
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


	public void localiseEventList(ArrayList<TargetMotionInformation> targetMotionInfo, TargetMotionModel[] modelList, boolean isSupervised) {
		this.currentTargetMotionInfo=targetMotionInfo;
		eventLocaliserWorker = new TMLocaliserWorker(targetMotionInfo, modelList, isSupervised);
		eventLocaliserWorker.execute();
	}

	private class TMLocaliserWorker extends SwingWorker<Integer, EventLocalisationProgress> {

		private TargetMotionModel[] modelList;
		private ArrayList<TargetMotionInformation> targetMotionList;
		private boolean supervised;
		List<Interractive> commandList = Collections.synchronizedList(new LinkedList<Interractive>());

		/**
		 * @param eventList
		 * @param isSupervised
		 */
		public TMLocaliserWorker(ArrayList<TargetMotionInformation> targetMotionList, TargetMotionModel[] modelList, boolean isSupervised) {
			super();
			this.targetMotionList=targetMotionList;
			this.modelList = modelList;
			this.supervised = isSupervised;
		}

		@Override
		protected void done() {
//			System.out.println("We're all done here");
			super.done();
			currentRunState=TargetMotionControl.LOCALISATION_DONE;
			targetMotionControl.getTargetMotionMainPanel().update(TargetMotionControl.LOCALISATION_DONE);
		}

		@Override
		protected Integer doInBackground() throws Exception {
			
			currentRunState=TargetMotionControl.LOCALISATION_STARTED;
			
			if (targetMotionList==null) return null;
			
			for(int i=0; i<targetMotionList.size(); i++){
				if (!localiseTM(targetMotionList.get(i), modelList)) {
					return null;
				}
			}
			
			currentRunState=TargetMotionControl.LOCALISATION_DONE;
			targetMotionControl.getTargetMotionMainPanel().update(TargetMotionControl.LOCALISATION_DONE);
			

			return null;
		}

		/**
		 * Process a single event. Return false if some kind of user input 
		 * indicates that further processing should stop. 
		 * @param anEvent
		 * @return true if processing should continue. 
		 */
		private boolean localiseTM(TargetMotionInformation targetMotionInformation, TargetMotionModel[] modelList) {

			clearResults();
			
			TargetMotionResult[] results = runModels(targetMotionInformation, modelList);
			
			int bestResult = selectBestResult(results);
			
//			System.out.println("TargteMotionLocaliser.Best Result: "+bestResult);

			setBestResultIndex(bestResult);
			
//			System.out.println("Results:  "+  results.toString());

			return true;
		}

	}



//	//	public int getCurrentEventIndex() {
//	//		return currentEventIndex;
//	//	}
//	private void updateGUI(EventLocalisationProgress elProgress) {
//		try {
//			SwingUtilities.invokeAndWait(new UpdateGui(elProgress));
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//	}
	

	
//	private class UpdateGui implements Runnable {
//		private EventLocalisationProgress elProgress;
//		/**
//		 * @param elProgress
//		 */
//		public UpdateGui(EventLocalisationProgress elProgress) {
//			super();
//			this.elProgress = elProgress;
//		}
//		@Override
//		public void run() {
//			updateGUIAWT(elProgress);
//
//		}
//	}
	// returns the event 
	public int getCurrentEventIndex(){
		return currentEventIndex;
	}
	
	
	public TargetMotionResult[] runModels(TargetMotionInformation targetMotionInformation, TargetMotionModel[] modelList) {
		
		if (modelList == null) {
			return null;
		}
		
		int nModels = modelList.length;
		TargetMotionModel model;
		TargetMotionResult[] results;
		TargetMotionResult[] allResults = new TargetMotionResult[0];
		long t1, t2;
		for (int i = 0; i < nModels; i++) {
			model = modelList[i];
			t1 = System.nanoTime();
			results = model.runModel(targetMotionInformation);
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

	/**
	 * Work out which is the best result based on Chi2 and AIC. 
	 * @param results array of results to compare. 
	 * @return index of best result, or -1 if there are no results or none with AIC or Chi2 values. 
	 */
	public int selectBestResult(TargetMotionResult[] results) {
		if (results == null || results.length < 1) {
			return -1;
		}
		int best = 0;
		TargetMotionResult bestRes;
		TargetMotionResult aRes;
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

	public TargetMotionControl getTargetMotionControl() {
		return targetMotionControl;
	}
	
	public int getCurrentRunState(){
		return currentRunState;
	}

	@Override
	public boolean localiseDataUnit(PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
