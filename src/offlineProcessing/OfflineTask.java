package offlineProcessing;

import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import generalDatabase.clauses.FixedClause;
import generalDatabase.clauses.FromClause;
import generalDatabase.clauses.PAMSelectClause;

import java.util.ArrayList;
import java.util.ListIterator;

import PamController.PamControlledUnit;
import PamController.PamViewParameters;
import dataMap.OfflineDataMapPoint;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.superdet.SuperDetection;

/**
 * An offline task, such as click species id. 
 * Generally controlled and operated from within 
 * an OLProcessDialog
 * @author Doug Gillespie
 * @see OLProcessDialog
 *
 */
public abstract class OfflineTask<T extends PamDataUnit> {

	/**
	 * We want this particular task to run ? 
	 * N.B. this is different to canRun !
	 */
	private boolean doRun = true;

	/**
	 * Reference back to a parent offline task group
	 */
	private OfflineTaskGroup offlineTaskGroup;

	/**
	 * primary data block for the task;
	 */
	private PamDataBlock<T> parentDataBlock;

//	/**
//	 * Default constructor. Should no longer be used, but kept in case there are subclasses
//	 * of OfflineTask in other plugins. <br>
//	 * please use constructor 'public OfflineTask(PamDataBlock<T> parentDataBlock)' instead
//	 */
//	@Deprecated
//	public OfflineTask() {
//		super();
//	}
	/**
	 * @param parentDataBlock
	 */
	public OfflineTask(PamDataBlock<T> parentDataBlock) {
		super();
		this.parentDataBlock = parentDataBlock;
		/*
		 *  every task is now going to be centrally registered in the offline task manager, but only
		 *  if it has a PAMControlledUnit. Tasks can be identified in the database from the unit id
		 *  information and the task name. 
		 *  There may be a few teething problems with this if a task is created in the constructor of
		 *  a PamProcess ? though it should by then know it's controlled unit I think. 
		 */
		PamControlledUnit parentControl = getTaskControlledUnit();
		if (parentControl == null) {
			System.out.printf("Offline task %s with datablock %s is not associated with a PAMGuard module\n", getName(), parentDataBlock==null ?  "null": parentDataBlock.getDataName());
		}
		else {
			OfflineTaskManager.getManager().registerTask(this);
		}
		
	}
	
	/**
	 * Get the PAMControlled unit associated with a task. 
	 * @return PAMControlled unit associated with a task. 
	 */
	public PamControlledUnit getTaskControlledUnit() {
		if (parentDataBlock == null) {
			return null;
		}
		PamProcess parentProcess = parentDataBlock.getParentProcess();
		if (parentProcess == null) {
			return null;
		}
		return parentProcess.getPamControlledUnit();
	}

	/**
	 * list of other data blocks also required by this task. 
	 */
	private ArrayList<RequiredDataBlockInfo> requiredDatablocks;

	/**
	 * Data blocks who's data may be affected by this task
	 * (so will need saving or will need their data deleted)
	 */
	private ArrayList<PamDataBlock> affectedDataBlocks;

	/**
	 * Get the parent data block for the task
	 * @return the datablock used by the task. 
	 */
	public PamDataBlock<T> getDataBlock() {
		return parentDataBlock;
	}

	/**
	 * @param dataBlock the dataBlock to set
	 */
	public void setParentDataBlock(PamDataBlock dataBlock) {
		this.parentDataBlock = dataBlock;
		if (offlineTaskGroup != null && offlineTaskGroup.getPrimaryDataBlock() == null) {
			offlineTaskGroup.setPrimaryDataBlock(dataBlock);
		}
	}

	/**
	 * 
	 * @return a name for the task, to be displayed in the dialog. 
	 */
	abstract public String getName();
	
	/**
	 * Get a unit type for the task. This is the unit type of 
	 * the parent PAMGuard module. 
	 * @return module name
	 */
	public String getUnitType() {
		PamControlledUnit parentControl = getTaskControlledUnit();
		if (parentControl == null) {
			return "Unknown ModuleName";
		}
		else {
			return parentControl.getUnitType();
		}
	}
	
	/**
	 * Get a unit name for the task. This is the unit name of 
	 * the parent PAMGuard module. 
	 * @return module name
	 */
	public String getUnitName() {
		PamControlledUnit parentControl = getTaskControlledUnit();
		if (parentControl == null) {
			return "Unknown ModuleName";
		}
		else {
			return parentControl.getUnitName();
		}
	}

	/**
	 * task has settings which can be called
	 * @return true or false
	 */
	public boolean hasSettings() {
		return false;
	}

	/**
	 * Call any task specific settings
	 * @return true if settings may have changed. 
	 */
	public boolean callSettings() {
		return false;
	}

	/**
	 * can the task be run ? This will generally 
	 * be true, but may be false if the task is dependent on 
	 * some other module which may not be present.  
	 * @return true if it's possible to run the task. 
	 */
	public boolean canRun() {
		boolean can = getDataBlock() != null; 
		return can;
	}

	/**
	 * Process a single data unit. 
	 * @return true if the data unit has changed in some
	 * way so that it will need re-writing to it's binary file 
	 * or database. 
	 */
	abstract public boolean processDataUnit(T dataUnit);

	/**
	 * Called when new data are loaded for offline processing 
	 * (or once at the start of processing loaded data). 
	 * @param startTime start time of loaded data
	 * @param endTime end time of loaded data
	 */
	abstract public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint);

	/**
	 * Called when processing of loaded data, or each map point worth of data,
	 * is complete. 
	 */
	abstract public void loadedDataComplete();

	/**
	 * Add a required data block. 
	 * These are data blocks apart from the main one which 
	 * are required before this task can complete. 
	 * Data for these block will be loaded automatically. 
	 * @param dataBlockInfo required data block with pre and post load times. 
	 */
	public void addRequiredDataBlock(RequiredDataBlockInfo dataBlockInfo) {
		if (requiredDatablocks == null) {
			requiredDatablocks = new ArrayList<RequiredDataBlockInfo>();
		}
		requiredDatablocks.add(dataBlockInfo);
	}

	/**
	 * Add a required data block. 
	 * These are data blocks apart from the main one which 
	 * are required before this task can complete. 
	 * Data for these block will be loaded automatically. 
	 * @param dataBlock required data block. 
	 */
	public void addRequiredDataBlock(PamDataBlock pamDataBlock) {
		addRequiredDataBlock(new RequiredDataBlockInfo(pamDataBlock, 0, 0));
	}

	/**
	 * Add a required data block. 
	 * These are data blocks apart from the main one which 
	 * are required before this task can complete. 
	 * Data for these block will be loaded automatically. 
	 * @param dataBlock required data block. 
	 * @param preLoadMillis time required before main data used in offline task
	 * @param postLoadMillis time required after main data used in offline task
	 */
	public void addRequiredDataBlock(PamDataBlock pamDataBlock, long preLoadMillis, long postLoadMillis) {
		addRequiredDataBlock(new RequiredDataBlockInfo(pamDataBlock, preLoadMillis, postLoadMillis));
	}

	/**
	 * @return the number of data blocks required to run 
	 * this task. 
	 */
	public int getNumRequiredDataBlocks() {
		if (requiredDatablocks == null) {
			return 0;
		}
		return requiredDatablocks.size();
	}


	/**
	 * A data block required to run this task. 
	 * @param iBlock block index
	 * @return data block .
	 */
	public RequiredDataBlockInfo getRequiredDataBlock(int iBlock) {
		return requiredDatablocks.get(iBlock);
	}

	/**
	 * Add an affected data block. 
	 * These are data blocks apart from the main one which 
	 * will have their contents changed by the task and will 
	 * require saving / updating as the task progresses. 
	 * @param dataBlock affected data block. 
	 */
	public void addAffectedDataBlock(PamDataBlock dataBlock) {
		if (affectedDataBlocks == null) {
			affectedDataBlocks = new ArrayList<PamDataBlock>();
		}
		if (!affectedDataBlocks.contains(dataBlock)) {
			affectedDataBlocks.add(dataBlock);
		}
	}
	/**
	 * @return the number of data blocks required to run 
	 * this task. 
	 */
	public int getNumAffectedDataBlocks() {
		if (affectedDataBlocks == null) {
			return 0;
		}
		return affectedDataBlocks.size();
	}

	/**
	 * A data block required to run this task. 
	 * @param iBlock block index
	 * @return data block .
	 */
	public PamDataBlock getAffectedDataBlock(int iBlock) {
		return affectedDataBlocks.get(iBlock);
	}

	/**
	 * Return whether or not the task SHOULD be run - i.e. if it is selected in 
	 * the dialog, etc...
	 * @return true to run. 
	 */
	public boolean isDoRun() {
		if (canRun() == false) {
			return false;
		}
		return doRun;
	}

	/**
	 * Set whether or not this task within a taskGroup should be run. 
	 * @param doRun the doRun to set
	 */
	public void setDoRun(boolean doRun) {
		this.doRun = doRun;
	}

	/**
	 * @return the offlineTaskGroup
	 */
	public OfflineTaskGroup getOfflineTaskGroup() {
		return offlineTaskGroup;
	}

	/**
	 * @param offlineTaskGroup the offlineTaskGroup to set
	 */
	public void setOfflineTaskGroup(OfflineTaskGroup offlineTaskGroup) {
		this.offlineTaskGroup = offlineTaskGroup;
	}

	/**
	 * Delete database outptut data in the list of output datablocks. 
	 * All data in the time range of data read into the primary 
	 * source data block will be deleted. 
	 * @param currentViewDataStart
	 * @param currentViewDataEnd
	 * @param mapPoint
	 */
	@Deprecated
	public void deleteOldOutput(long currentViewDataStart,
			long currentViewDataEnd, OfflineDataMapPoint mapPoint) {
		if (affectedDataBlocks == null || parentDataBlock == null) {
			return;
		}
		SQLLogging sqlLogging;
		for (int i = 0; i < affectedDataBlocks.size(); i++) {
			affectedDataBlocks.get(i).clearAll();
			sqlLogging = affectedDataBlocks.get(i).getLogging();
			if (sqlLogging == null) {
				continue;
			}
			sqlLogging.deleteData(parentDataBlock.getCurrentViewDataStart(), 
					parentDataBlock.getCurrentViewDataEnd());
		}
	}

	/**
	 * Called at the start of the thread which executes this task. 
	 */
	public void prepareTask() {	}

	/**
	 * Called at the end of the thread which executes this task. 
	 */
	public void completeTask() { }

	/**
	 * Gets called at the start of data processing, called oncer per run, i.e. 
	 * not called again each time more data are loaded. 
	 * @param taskGroupParams
	 */
	public void deleteOldData(TaskGroupParams taskGroupParams) {
		if (affectedDataBlocks == null) {
			return;
		}

		for (PamDataBlock aBlock:affectedDataBlocks) {
			deleteOldData(aBlock, taskGroupParams);
		}
	}

	/**
	 * Delete the old data before re-analysis. Any sub detections are removed from the 
	 * data unit and their super detection references deleted. 
	 * @param aBlock - the data block from which to remove data units. 
	 * @param taskGroupParams - the task group parameters. 
	 */
	private void deleteOldData(PamDataBlock aBlock, TaskGroupParams taskGroupParams) {

		//delete the old data- also need to get rid of super detection references. 
		synchronized (aBlock.getSynchLock()) {
			ListIterator<PamDataUnit> ctIterator = aBlock.getListIterator(0);
			while (ctIterator.hasNext()) {
				PamDataUnit dataUnit = ctIterator.next();
				if (dataUnit instanceof SuperDetection) { 
					SuperDetection ct = (SuperDetection) dataUnit;
					//				Debug.out.println("Clear the data unit: " + ct);
					//must clear both sub detections from super detection and 
					//super detection from sub detection!
					for (int i=0; i<ct.getSubDetectionsCount(); i++) {
						if (ct.getSubDetection(i)!=null) ct.getSubDetection(i).removeSuperDetection(ct);
					}
					if (ct.getSubDetections()!=null) {
						ct.getSubDetections().clear();
						ct.clearSubdetectionsRemoved();
					}
				}
			}
		}
		aBlock.clearAll(); 

		

		SQLLogging logging = aBlock.getLogging();
		if (logging == null) {
			return; // data block doesn't have database i/o. 
		}
		PamConnection con = DBControlUnit.findConnection();
		if (con == null) {
			return; // no database connection
		}
		SQLTypes sqlTypes = con.getSqlTypes(); // cannot be null (I hope!)
		/*
		 * Make up a delete clause based on which data are being analysed. 
		 */
		PAMSelectClause clause;
		switch (taskGroupParams.dataChoice) {
		case TaskGroupParams.PROCESS_ALL:
			clause = new FixedClause("");
			break;
		case TaskGroupParams.PROCESS_LOADED:
			clause = new PamViewParameters(getDataBlock().getCurrentViewDataStart(), getDataBlock().getCurrentViewDataEnd());
			break;
		case TaskGroupParams.PROCESS_NEW:
			clause = new FromClause(taskGroupParams.startRedoDataTime);
			break;
		default:
			System.out.println("Unknown data selection option in OfflineTask.deleteOldData: " + taskGroupParams.dataChoice);
			return;
		}
		while (logging != null) {
			logging.deleteData(clause);
			if (logging instanceof SuperDetLogging) {
				logging = ((SuperDetLogging) logging).getSubLogging();
			}
			else {
				break;
			}
		}
	}


}
