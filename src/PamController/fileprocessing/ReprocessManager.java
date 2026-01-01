package PamController.fileprocessing;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import PamController.DataInputStore;
import PamController.DataOutputStore;
import PamController.InputStoreInfo;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamGUIManager;
import PamController.RawInputControlledUnit;
import PamUtils.worker.PamWorkDialog;
import PamUtils.worker.PamWorkMonitor;
import PamUtils.worker.PamWorkProgressMessage;
import PamView.dialog.warn.WarnOnce;
import pamViewFX.pamTask.PamTaskUpdate;
import pamViewFX.pamTask.SimplePamTaskUpdate;
import pamguard.GlobalArguments;

/**
 * Set of functions to help decide what to do when reprocessing. 
 * These are probably all called from AcquisitionProcess, but it's easier to have them in their own class. 
 * @author dg50
 *
 */
public class ReprocessManager {

	private volatile PamWorkDialog workDialog;
	
	private Object synch = new Object();
	/**
	 * Start a Swing worker thread to do the checks and to display a progress bar while doing it.<p>
	 * Then when it's done, send the result to the monitor, which is basically telling PamController
	 * whether or not to continue with start up 
	 * @param mainFrame
	 * @param mon monitor for final status message / instruction. 
	 */
	public void startCheckingThread(Frame mainFrame, ReprocessManagerMonitor mon) {
		CheckWorker checkWorker = new CheckWorker(mainFrame, mon);
		checkWorker.execute();	
		
		//TODO - JavaFX GUI crashes here
		if (PamGUIManager.getGUIType() == PamGUIManager.FX) {
			//do nothing - progress messages will be sent to PamController via the monitor interface.
		}
		else {
			synchronized (synch) {
				workDialog = new PamWorkDialog(mainFrame, 1, "Checking input files and existing output data");
				workDialog.setVisible(true);
			}
		}
	}
	
	private void closeWorkDialog() {
		/**
		 * This will only get called when job has finished - but that might happen before the
		 * dialog is even open, so wait for up to a second for it to appear before closing it anyway. 
		 */
		long t = System.currentTimeMillis();
		while (System.currentTimeMillis()-t < 1000) {
			if (workDialog != null) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		synchronized (synch) {
			if (workDialog != null) {
				workDialog.setVisible(false);
				workDialog.dispose();
				workDialog = null;
			}
		}

	}
	
	private class CheckWorker extends SwingWorker<Boolean, PamWorkProgressMessage> implements PamWorkMonitor {

		private Frame mainFram;
		private ReprocessManagerMonitor mon;
		private volatile boolean result;
		
		public CheckWorker(Frame mainFram, ReprocessManagerMonitor mon) {
			super();
			this.mainFram = mainFram;
			this.mon = mon;
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			try {
				result = checkOutputDataStatus(this);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void process(List<PamWorkProgressMessage> chunks) {
			for (PamWorkProgressMessage message : chunks) {
				synchronized(synch) {
					if (workDialog != null) {
						workDialog.update(message);
					}
					
				}
			}
		}

		@Override
		protected void done() {
			closeWorkDialog();
			mon.done(result);
		}

		@Override
		public void update(PamWorkProgressMessage message) {
			if (PamGUIManager.getGUIType() == PamGUIManager.FX) {
				// in FX mode we just send the message to PamController which will deal with it.
				PamController.getInstance().notifyTaskProgress(
						new SimplePamTaskUpdate(message));
			}
			else {
				//publish normally for SwingWorker
				this.publish(message);
			}
		}
		
	}


	/**
	public ReprocessManager() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 *  run checks on the output data storage system. If data already exist in the output
	 *  we may not want to start again.  
	 */
	public boolean checkOutputDataStatus() {
		return checkOutputDataStatus(null);
	}
	public boolean checkOutputDataStatus(PamWorkMonitor workMonitor) {
		
		StoreChoiceSummary choiceSummary = null;
		if (isOfflineFiles()) {
			choiceSummary = checkIOFilesStatus(workMonitor);
		}
		else {
			/*
			 *  don't really need to do anything for real time processing since adding
			 *  more data to existing stores is normal behaviour. 
			 */
			return true;
		}
		if (choiceSummary == null) {
			return true;
		}
		
		if (choiceSummary.getInputStoreInfo() == null) {
			return true;
		}
		
		// need to decide what to do based on the list of possible choices. 
		ReprocessStoreChoice choice = chosePartStoreAction(choiceSummary);

		/**
		 * Need to call this even though we aren't reprocessing so that
		 * the Folderinput stream reports correctly on how many files have 
		 * been processed. 
		 */
		boolean setupOK = setupInputStream(choiceSummary, choice);
		
		if (choice == null || choice == ReprocessStoreChoice.DONTSSTART) {
			return false;
		}
		
		boolean deleteOK = deleteOldData(choiceSummary, choice);
		
		return true;
		
	}
	

	private boolean setupInputStream(StoreChoiceSummary choiceSummary, ReprocessStoreChoice choice) {
		// work out the first file index and send it to the appropriate input module. 
		long deleteFrom = getDeleteFromTime(choiceSummary, choice);
		ArrayList<PamControlledUnit> inputStores = PamController.getInstance().findControlledUnits(DataInputStore.class, true);
		if (inputStores == null || inputStores.size() == 0) {
			return false;
		}
		InputStoreInfo inputInfo = null;
		boolean OK = true;
		long procStartTime = deleteFrom;
		if (choice == ReprocessStoreChoice.STARTNORMAL) {
			procStartTime = 0;
		}
		for (PamControlledUnit aPCU : inputStores) {
			DataInputStore inputStore = (DataInputStore) aPCU;
			OK &= inputStore.setAnalysisStartTime(procStartTime);
			if (inputInfo != null) {
				System.out.println("Input store info: " + inputInfo);
			}
		}
		return OK;
	}


	/**
	 * Just gets on and does it. The user should already have been asked what they 
	 * want to do, so don't ask again. 
	 * @param choiceSummary
	 * @param choice
	 */
	private boolean deleteOldData(StoreChoiceSummary choiceSummary, ReprocessStoreChoice choice) {
		long deleteFrom = getDeleteFromTime(choiceSummary, choice);
		// go through the data stores and tell them to delete from that time. 
		if (deleteFrom == Long.MAX_VALUE) {
			return false;
		}
		ArrayList<PamControlledUnit> outputStores = PamController.getInstance().findControlledUnits(DataOutputStore.class, true);
		boolean partStores = false; 
		boolean ok = true;
		for (PamControlledUnit aPCU : outputStores) {
			DataOutputStore offlineStore = (DataOutputStore) aPCU;
			ok &= offlineStore.deleteDataFrom(deleteFrom);
		}
		return ok;
	}


	private long getDeleteFromTime(StoreChoiceSummary choiceSummary, ReprocessStoreChoice choice) {
		if (choice == null) {
			return Long.MAX_VALUE; // I don't think this can happen, but you never know. 
		}
		Long t = null;
		switch (choice) {
		case CONTINUECURRENTFILE:
			t = choiceSummary.getInputTimeForIndex(choiceSummary.getFileIndexBefore(choiceSummary.getOutputEndTime()));
			break;
		case CONTINUENEXTFILE:
			t = choiceSummary.getInputTimeForIndex(choiceSummary.getFileIndexAfter(choiceSummary.getOutputEndTime()));
			break;
		case DONTSSTART:  // we should'nt get here with this option. 
			return Long.MAX_VALUE;
		case OVERWRITEALL:
			return 0; // delete from start. (
		case STARTNORMAL: // we should'nt get here with this option. 
			return Long.MAX_VALUE;
		default:
			break;
		
		}
		if (t == null) {
			// shouldn't happen, don't do any deleteing
			return Long.MAX_VALUE;
		}
		else {
			return t;
		}
	}


	/**
	 * Check the output of current files and databases and return a flag to PamController saying whether or
	 * not processing should actually start, possibly overwriting, or if we need to not start to avoid overwriting. 
	 * @param workMonitor 
	 * @return true if processing should start. 
	 */
	private StoreChoiceSummary checkIOFilesStatus(PamWorkMonitor workMonitor) {	
		/**
		 * Get information about the input. 
		 * 
		 */
		ArrayList<PamControlledUnit> inputStores = PamController.getInstance().findControlledUnits(DataInputStore.class, true);
		if (inputStores == null || inputStores.size() == 0) {
			return new StoreChoiceSummary(null, ReprocessStoreChoice.STARTNORMAL);
		}
		StoreChoiceSummary choiceSummary = new StoreChoiceSummary(null);

				
		choiceSummary.addChoice(ReprocessStoreChoice.STARTNORMAL);
		
		ArrayList<PamControlledUnit> outputStores = PamController.getInstance().findControlledUnits(DataOutputStore.class, true);
		boolean partStores = false; 
		int nOutputStores = 0;
		for (PamControlledUnit aPCU : outputStores) {
			if (workMonitor != null) {
				workMonitor.update(new PamWorkProgressMessage(-1, "Checking output data " + aPCU.getUnitName()));
			}
			DataOutputStore offlineStore = (DataOutputStore) aPCU;
			StoreStatus status = offlineStore.getStoreStatus(false);
			nOutputStores++;
			if (status == null) {
				continue;
			}
			if (status.getStoreStatus() == StoreStatus.STATUS_HASDATA) {
				status = offlineStore.getStoreStatus(true); // get more detail.  
				partStores = true;
				System.out.printf("Storage %s already contains some data\n", offlineStore.getDataSourceName());
				choiceSummary.testOutputEndTime(status.getLastDataTime());
				choiceSummary.testOutputStartTime(status.getFirstDataTime());
			}
		}
				
		if (partStores == false)  {
//			choiceSummary.addChoice(ReprocessStoreChoice.STARTNORMAL);
			return null; // no part full stores, so can start without questions
		}		
		
		// now deal with the input data. 
		InputStoreInfo inputInfo = null;
		for (PamControlledUnit aPCU : inputStores) {
			DataInputStore inputStore = (DataInputStore) aPCU;
			if (workMonitor != null) {
				workMonitor.update(new PamWorkProgressMessage(-1, "Checking input data " + aPCU.getUnitName()));
			}
			inputInfo = inputStore.getStoreInfo(workMonitor, true);
//			System.out.println("Input store info: " + inputInfo);
		}
		choiceSummary.setInputStoreInfo(inputInfo);
		
		if (inputInfo == null || inputInfo.getFileStartTimes() == null) {
			choiceSummary.addChoice(ReprocessStoreChoice.STARTNORMAL);
			return choiceSummary;
		}
		
		if (choiceSummary.getInputStartTime() >= choiceSummary.getOutputEndTime()) {
			/*
			 *  looks like it's new data that starts after the end of the current store,
			 *  so there is no need to do anything.  
			 */
			choiceSummary.addChoice(ReprocessStoreChoice.STARTNORMAL);
			return choiceSummary;
		}
		/*
		 * If we land here, it looks like we have overlapping data. so need to make a decision
		 * First, check to see if processing has actually completed which will be the case if 
		 * the data time and the end of the files are the same. 
		 */		
		choiceSummary.addChoice(ReprocessStoreChoice.DONTSSTART);
		choiceSummary.addChoice(ReprocessStoreChoice.OVERWRITEALL);
		if (choiceSummary.isProcessingComplete() == false) {
			choiceSummary.addChoice(ReprocessStoreChoice.CONTINUECURRENTFILE);
			choiceSummary.addChoice(ReprocessStoreChoice.CONTINUENEXTFILE);
		}
		
		return choiceSummary;

	}

	/**
	 * Either opens a dialog to ask the user, or uses a choice entered into the command line for nogui mode. 
	 * Decide what to do with stores that already have data. Can return continue from end or overwrite
	 * in which case stores will be deleted and we'll start again. The chosen action will need to be
	 * communicated to the various inputs. 
	 * @param choices 
	 */
	private ReprocessStoreChoice chosePartStoreAction(StoreChoiceSummary choices) {
		/**
		 * Do we really have to deal with multiple inputs ? Can I envisage a situation where there is
		 * ever more than one input going at any one time ? not really, but should I add code 
		 * to make sure that there really can be only one ? i.e. two daq's would be allowed for real 
		 * time processing, but only one for offline ? could do all I guess by looking at sources of 
		 * all output data blocks and doing it on a case by case basis. All we have to do here though 
		 * is to get an answer about what to do. 
		 */
		// see if we've got a global parameter passed in as an argument
		String arg = GlobalArguments.getParam(ReprocessStoreChoice.paramName);
		if (arg != null) {
			ReprocessStoreChoice choice = ReprocessStoreChoice.valueOf(arg);
			if (choice == null) {
				String warn = String.format("Reprocessing storage input parameter %s value \"%s\" is not a recognised value", ReprocessStoreChoice.paramName, arg);
				WarnOnce.showWarning("Invalid input parameter", warn, WarnOnce.WARNING_MESSAGE);
			}
			if (choice == ReprocessStoreChoice.CONTINUECURRENTFILE || choice == ReprocessStoreChoice.CONTINUENEXTFILE) {
				if (choices.isProcessingComplete()) {
					return ReprocessStoreChoice.DONTSSTART;
				}
			}
			return choice;
		}
		if (PamGUIManager.getGUIType() == PamGUIManager.NOGUI) {
			System.out.println("In Nogui mode you should set a choice as to how to handle existing storage overwrites. Using default of overwriting everything");
			return ReprocessStoreChoice.OVERWRITEALL;			
		}
		else if (PamGUIManager.getGUIType() == PamGUIManager.FX) {
			// otherwise we'll need to show a dialog to let the user decide what to do 
			ReprocessStoreChoice choice = ReprocessChoiceDialogFX.showDialog(PamController.getMainStage(), choices);
			return choice;
		}
		else {
			// otherwise we'll need to show a dialog to let the user decide what to do 
			ReprocessStoreChoice choice = ReprocessChoiceDialog.showDialog(PamController.getMainFrame(), choices);
			
			return choice;
		}
	}

	/**
	 * Return true if we seem to be reprocessing offline files. 
	 * Note that this might be the Tritech data as well as the sound acquisition so 
     * have added an abstract intermediate class on the controlled units so we can check them all. 
	 * @return
	 */
	public boolean isOfflineFiles() {
		ArrayList<PamControlledUnit> sources = PamController.getInstance().findControlledUnits(RawInputControlledUnit.class, true);
		if (sources == null) {
			return false;
		}
		for (PamControlledUnit pcu : sources) {
			RawInputControlledUnit rawPCU = (RawInputControlledUnit) pcu;
			if (rawPCU.getRawInputType() == RawInputControlledUnit.RAW_INPUT_FILEARCHIVE) {
				return true;
			}
		}
		return false;
	}

}
