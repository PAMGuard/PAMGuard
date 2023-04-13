package PamController.fileprocessing;

import java.util.ArrayList;

import PamController.DataInputStore;
import PamController.DataOutputStore;
import PamController.InputStoreInfo;
import PamController.OfflineDataStore;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamGUIManager;
import PamController.RawInputControlledUnit;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import pamguard.GlobalArguments;

/**
 * Set of functions to help decide what to do when reprocessing. 
 * These are probably all called from AcquisitionProcess, but it's easier to have them in their own class. 
 * @author dg50
 *
 */
public class ReprocessManager {
	
	/**
	public ReprocessManager() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 *  run checks on the output data storage system. If data already exist in the output
	 *  we may not want to start again.  
	 */
	public boolean checkOutputDataStatus() {
		
		StoreChoiceSummary choiceSummary = null;
		if (isOfflineFiles()) {
			choiceSummary = checkIOFilesStatus();
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
		
		if (choice == ReprocessStoreChoice.DONTSSTART) {
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
		for (PamControlledUnit aPCU : inputStores) {
			DataInputStore inputStore = (DataInputStore) aPCU;
			OK &= inputStore.setAnalysisStartTime(deleteFrom);
//			System.out.println("Input store info: " + inputInfo);
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
	 * @return true if processing should start. 
	 */
	private StoreChoiceSummary checkIOFilesStatus() {	
		/**
		 * Get information about the input. 
		 * 
		 */
		ArrayList<PamControlledUnit> inputStores = PamController.getInstance().findControlledUnits(DataInputStore.class, true);
		if (inputStores == null || inputStores.size() == 0) {
			return new StoreChoiceSummary(null, ReprocessStoreChoice.STARTNORMAL);
		}
		InputStoreInfo inputInfo = null;
		for (PamControlledUnit aPCU : inputStores) {
			DataInputStore inputStore = (DataInputStore) aPCU;
			inputInfo = inputStore.getStoreInfo(true);
//			System.out.println("Input store info: " + inputInfo);
		}
		StoreChoiceSummary choiceSummary = new StoreChoiceSummary(inputInfo);
		
		if (inputInfo == null || inputInfo.getFileStartTimes() == null) {
			choiceSummary.addChoice(ReprocessStoreChoice.STARTNORMAL);
			return choiceSummary;
		}
		
		ArrayList<PamControlledUnit> outputStores = PamController.getInstance().findControlledUnits(DataOutputStore.class, true);
		boolean partStores = false; 
		int nOutputStores = 0;
		for (PamControlledUnit aPCU : outputStores) {
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
		
		// otherwise we'll need to show a dialog to let the user decide what to do 
		ReprocessStoreChoice choice = ReprocessChoiceDialog.showDialog(PamController.getMainFrame(), choices);
		
		return choice;
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
