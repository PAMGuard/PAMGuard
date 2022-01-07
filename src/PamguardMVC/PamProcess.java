/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

/*
 * superclass for all processes which change data within PamGuard.
 * The first PamProcess will take it's data from a file, ADC card, etc and will
 * create a series of PamDataUnits which are held within an array list in the
 * PamDataBlock outputDataBlock. 
 * Subsequent PamProcesses must have a valid parentDataBlock which they
 * subscribe to as observers. TheProcess will then be identified by the PamDataBlock 
 * when new data becomes available and can process it accordingly - generally producing
 * new PamDataUnits to go into the outputDataBlock. 
 */

package PamguardMVC;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import javax.swing.Timer;

import networkTransfer.receive.BuoyStatusDataUnit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.status.ProcessCheck;
import PamModel.PamModel;
import PamModel.PamProfiler;
import PamUtils.PamCalendar;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;

/**
 * @author Doug Gillespie
 *         <p>
 *         PamProcess is an abstract class used as a supertype for the creation
 *         of processes which either convert one type of PamData into a new type
 *         or add existing information to already existing data
 *         <p>
 *         All PamProcesses are Listeners and implement the Observer Class from
 *         java.util
 *         <p>
 *         The first PamProcess to be created will often be something which
 *         generates raw data (either by reading an input device or by reading
 *         chunks of data from a file).
 *         <p>
 *         Each PamProcess will require a source of data, which apart from the
 *         top level processes which acquire data, will be a PamDataBlock. The
 *         PamProcess will generally subscribe to the PamDataBlock so that it is
 *         notified of any changes (i.e. new data) being added to that block.
 *         <p>
 *         The PamController ...
 * 
 */
@SuppressWarnings("rawtypes")
abstract public class PamProcess implements PamObserver, ProcessAnnotator {

	/**
	 * The parent data block is the primary data block for the process. Any generic function which  
	 */
	protected PamDataBlock parentDataBlock;
	
	/**
	 * A process can have multi-plex data blocks. These are (rarely) used to combine data from different PamControlledUnits into
	 * a single process and usually therefore single output data block. 
	 */
	private ArrayList<PamDataBlock> multiPlexDataBlocks;
	
	/**
	 * Allow multiplex data blocks. Multiplex data blocks allow a process to have multiple parents. 
	 */
	private boolean isMultiplex=false; 

	/**
	 * Reference to the PamControlledUnit to which the process belongs. 
	 */
	private PamControlledUnit pamControlledUnit;

	//protected PamProcess parentProcess;

	/**
	 * List of output data blocks from the process. 
	 */
	protected ArrayList<PamDataBlock> outputDataBlocks;
	
	/**
	 * The sample rate of the process. 
	 */
	protected float sampleRate;

	// private PamDataUnit lastUsedUnit;
	protected String processName;

	private ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
	
	/**
	 * Sometimes a process within a module will only accept data from other processes inside that module. 
	 * For example, the whistle and moan detector WhistleToneConnectProcess only accepts noise free FFT data from the 
	 * an internal module process. A problem can occur if potential parent modules try to become the parent of such 
	 * a process. Settings isExternalProcess top true means that external modules can become parents of the process. If 
	 * set to false then only data blocks within the same module can become parents. 
	 */
	boolean isExternalProcess = true; 

	PamProfiler.CPUUsageSnapshot startCPUSnapShot, endCPUSnapShot;
	
	private long cpuUsage;
	private long lastCPUCheckTime = System.currentTimeMillis();
	private double cpuPercent;
	
	/**
	 * Flag for the process to say whether or not it's primary data connection
	 * can be multithreaded. Provides an easy way to disable multithreading in 
	 * any process in which multithreading is not yet stable. 
	 */
	private boolean canMultiThread = true; 
	
	private ProcessCheck processCheck;
	
	/**
	 * Last received data unit - used for working out timing offsets. 
	 */
	private PamDataUnit lastAcousticDataunit;
	// some flags and variables needed to deal with conversion from
	// milliseconds to samples and back
//	private boolean acousticDataSource = false;
//	private long lastUnitMilliseconds = 0;
//	private long lastUnitSamples = 0;
	

	// protected long startMilliseconds = -1;
	/**
	 * @param pamControlledUnit
	 *            Reference to the PamControlledUnit containing this process
	 *            (PamProcesses can only exist within PamControlledUnits, but a
	 *            PamControlledUnit can contain multiple PamProcesses)
	 * @param parentDataBlock
	 *            Source data block for this process (can be null for raw data
	 *            input devices)
	 */
	public PamProcess(PamControlledUnit pamControlledUnit,
			PamDataBlock parentDataBlock) {
		makePamProcess(pamControlledUnit, parentDataBlock, pamControlledUnit.getUnitName());
	}
	
	public PamProcess(PamControlledUnit pamControlledUnit,
			PamDataBlock parentDataBlock, String processName) {
		makePamProcess(pamControlledUnit, parentDataBlock, processName);
	}

	@Override
	public PamObserver getObserverObject() {
		return this;
	}
	/**
	 * Called from the PamControlled unit when a PamControlled unit is removed from 
	 * the model. Offers an opportunity to disconnect individual processed from the
	 * rest of the model. 
	 * May be necessary to override to clean up some processes.
	 *
	 */
	public void destroyProcess() {
		setParentDataBlock(null);
		removeAllDataBlocks();
	}

	/**
	 * Remove all data blocks from a process. 
	 */
	public void removeAllDataBlocks() {
		// need to not only delete refs to output data blocks, but also need to tell any downstream
		// processes that the data block no longer exists. 
		for (int i = outputDataBlocks.size()-1; i >= 0; i--) {
			outputDataBlocks.get(i).deleteObservers();
			removeOutputDatablock(outputDataBlocks.get(i));
		}
		
	}
	
	public void makePamProcess(PamControlledUnit pamControlledUnit,
			PamDataBlock parentDataBlock, String processName) {

		setParentDataBlock(parentDataBlock);
		
		this.pamControlledUnit = pamControlledUnit;
		
		this.processName = processName;

		outputDataBlocks = new ArrayList<PamDataBlock>();
		
		//rarely used multi datablock option. 
		multiPlexDataBlocks= new ArrayList<PamDataBlock>();

//		binaryDataSources = new ArrayList<BinaryDataSource>();
		
		t.start();
	}

	
	/**
	 * @return Returns the parentDataBlock.
	 */
	public PamDataBlock getParentDataBlock() {
		return parentDataBlock;
	}
	
	/**
	 * Get a list of the parent datablock and any multiplex datablocks. 
	 * @return a list where the first element is the parent datablock and other elements are multiplex data blocks.
	 */
	public ArrayList<PamDataBlock> getParentDataBlocks() {
		ArrayList<PamDataBlock> dataBlocks=new ArrayList<PamDataBlock>();
		dataBlocks.add(parentDataBlock);
		for (int i=0; i<multiPlexDataBlocks.size(); i++){
			dataBlocks.add(multiPlexDataBlocks.get(i));
		}
		return dataBlocks; 
	}
	
	public PamProcess getParentProcess() {
		if (parentDataBlock != null) return parentDataBlock.getParentProcess();
		return null;
	}
	/**
	 * Useful function to go back through the chain of data blocks
	 * and data units upstream of this process and look for a 
	 * data block which has a particular data type. For instance, you
	 * may want the raw audio data upstream of FFT data. This may not be the 
	 * data input to the FFT source, since you may be looking at some
	 * secondary FFT data source (smoothed, or noise suppressed spectrogram 
	 * data for instance). 
	 * @param unitDataType class type of data in the sought after
	 * data block (e.g. RawDataUnit.class
	 * 
	 * @return reference to data block or null if none found
	 * @see PamDataBlock
	 */
	public PamDataBlock getAncestorDataBlock(Class unitDataType) {
		
		PamDataBlock prevDataBlock = getParentDataBlock();
		PamProcess prevProcess;
		while (prevDataBlock != null) {
			if (prevDataBlock.getUnitClass() == unitDataType) {
				return prevDataBlock;
			}
			//jump back one process up the chain. 
			prevProcess = prevDataBlock.getParentProcess();
			if (prevProcess == null) {
				return null;
			}
			prevDataBlock = prevProcess.getParentDataBlock();
		}
		
		return null;
	}
	
	/**
	 * Set a parent data block with the default options of rethreading
	 * if set 
	 * @param newParentDataBlock  source data block. 
	 */
	public void setParentDataBlock(PamDataBlock newParentDataBlock) {
		setParentDataBlock(newParentDataBlock, canMultiThread);
	}
	/**
	 * 
	 * Set a parent data block for the process with the option to rethread
	 * the data exchange process. This is overridden to false if the main
	 * PAMGUARD option to rethread is off. 
	 * @param newParentDataBlock source data block
	 * @param reThread rethread if multithreading is enabled. 
	 */
	public void setParentDataBlock(PamDataBlock newParentDataBlock, boolean reThread) {
//		if (newParentDataBlock == parentDataBlock) return;
		/*
		 * Only do this if the parentDataBlock is different to the new
		 * one. Otherwise Pamguard tends to get stuck in a loop of model change
		 * notifications and setting of data blocks. 
		 */
		if (parentDataBlock == newParentDataBlock) {
			return;
		}
		
		if (parentDataBlock != null) {
			parentDataBlock.deleteObserver(this);
		}
		parentDataBlock = newParentDataBlock;
		if (parentDataBlock != null) {
			parentDataBlock.addObserver(this, PamModel.getPamModel().isMultiThread() & reThread);
//			acousticDataSource = AcousticDataUnit.class.isAssignableFrom(parentDataBlock.getUnitClass());
//			parentProcess = parentDataBlock.getParentProcess();
			PamProcess pp = parentDataBlock.getParentProcess();
			setSampleRate(pp.getSampleRate(), true);
		}
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
		createAnnotations(true);
	}
	
	final public void createAnnotations(boolean notifyDownstream) {
		PamDataBlock pdb;
		if (outputDataBlocks == null) {
			return;
		}
		for (int i = 0; i < outputDataBlocks.size(); i++) {
			pdb = outputDataBlocks.get(i);
			pdb.createProcessAnnotations(parentDataBlock, this, notifyDownstream);
		}
	}
	
	/**
	 * Function that gets called in every process 
	 * (or all processes that are listed in PamControlledUnits)
	 * and re-sets up the data source using the correct threading model.
	 * All this actually involves is resubscribing to the same data since
	 * everything else is handled in setParentDataBlock;
	 */
	public void changedThreading() {
		PamDataBlock currentDataSource = parentDataBlock;
		if (currentDataSource != null) {
			setParentDataBlock(null);
			setParentDataBlock(currentDataSource, canMultiThread);
		}
	}
	
	/**
	 * @param processName
	 *            Sets the name of the process
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	/**
	 * 
	 * @return Name of the PamProcess
	 */
	public String getProcessName() {
		return processName;
	}

	public String getObserverName() {
		return "Process: " + getProcessName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamObserver#SetSampleRate(float, boolean)
	 */
	public void setSampleRate(float sampleRate, boolean notify) {
		// notify all output data blocks that there is a new sample rate
		this.sampleRate = sampleRate;
		if (notify && outputDataBlocks != null) {
			for (int i = 0; i < outputDataBlocks.size(); i++) {
				outputDataBlocks.get(i).setSampleRate(sampleRate, notify);
			}
		}
	}
	
	@Override
	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
		if (outputDataBlocks != null) {
			for (int i = 0; i < outputDataBlocks.size(); i++) {
				/**
				 * Somehow these three lines crept in, presumably to stop some sort of inter thread lock
				 * up - but they are wrong since all output data blocks of a pam process
				 * will have this peocess as their parent, so the clockupdate was simply 
				 * never getting through. one noticable place where this mattered was the clock 
				 * update on the noise displays. Lines removed, but may cause problems !
				 * Doug 18 Feb, 2014. 
				 */
//				if (outputDataBlocks.get(i).getParentProcess() == this) {
//					continue;
//				}
				outputDataBlocks.get(i).masterClockUpdate(timeMilliseconds, sampleNumber);
			}
		}
	}

	/**
	 * Convert a number of samples into a number of milliseconds
	 * @param samples Number of ADC samples
	 * @return equivalent number of milliseconds
	 */
	public long relSamplesToMilliseconds(long samples) {
		return (long) (samples * 1000 / sampleRate);
	}
	
	/**
	 * Convert am ADC sample number to a millisecond time.
	 * This function was re-written on 11/11/08 to deal with problems of 
	 * sound card clocks not running at same speed as PC clock, so milliseconds
	 * from PC clock and milliseconds based on samples would drift apart. 
	 * This new system bases the calculation on the times of the most recently
	 * received data unit. 
	 * @param samples sample number (from start of run)
	 * @return Millisecond time 
	 * (UTC milliseconds from the epoch - the standard Java way)
	 */
	public long absSamplesToMilliseconds(long samples) {
		if (sampleRate == 0) {
			return PamCalendar.getTimeInMillis();
		}
		if (lastAcousticDataunit != null) {
			/**
			 * just do a relative jump of the last data unit. this way, it should pick up the corrected millisecond time off the 
			 * original raw data unit. 
			 * It shouldn't matter if this updates, just not in the middle of the next line or it will seriously mess up
			 * so copy the reference so it can't change. 
			 */
			PamDataUnit theLast = lastAcousticDataunit;
			return theLast.getTimeMilliseconds() + relSamplesToMilliseconds(samples-theLast.getStartSample());
		}
		else {
			return (long) (samples * 1000. / sampleRate) + PamCalendar.getSessionStartTime();
		}
	}
	
	/**
	 * Convert a time in milliseconds to a number of samples.
	 * @param millis Current time in milliseconds 
	 * (UTC millseconds from the epoch - the standard Java way)
	 * @return ADC Samples since the run start. 
	 * @see PamProcess#relMillisecondsToSamples(double)
	 */
	public long absMillisecondsToSamples(long millis) {
		//return (millis - startMilliseconds) * (long)  sampleRate / 1000;
		return (millis - PamCalendar.getSessionStartTime()) * (long)  sampleRate / 1000;
	}
	
	/**
	 * Convert a number of milliseconds to a number of samples. 
	 * @param millis number of milliseconds
	 * @return number of ADC samples corresponding to millis milliseconds.
	 * @see PamProcess#absMillisecondsToSamples(long)
	 */
	public long relMillisecondsToSamples(double millis) {
		return (long) (millis * sampleRate / 1000);
	}

	public void noteNewSettings() {
		for (int i = 0; i < outputDataBlocks.size(); i++) {
			outputDataBlocks.get(i).noteNewSettings();
		}
	}

	/**
	 * @return The sample rate in the process
	 */
	public float getSampleRate() {
		return sampleRate;
	}
	
	/**
	 * Get the range of frequencies over which the data in this process are likely 
	 * to be present. Note that this is pretty crude and may not reflect the true 
	 * range, for example, the click detector will return the limits of it's trigger
	 * filter, and there are plenty of sounds outside of that range which may have 
	 * most of their energy well outside of the trigger range of the detector. 
	 * @return Nominal frequency range for data in this block. 
	 */
	public double[] getFrequencyRange() {
		double[] fr = {0., getSampleRate()/2.};
		return fr;
	}

	/**
	 * @return The original data source process, i.e. the one reading the
	 *         soundcard or the file, the one that doesn't have a
	 *         parentDataBlock
	 */
	public PamProcess getSourceProcess() {
		 if (parentDataBlock == null) return this;
		 if (getParentProcess() == this) {
			 System.out.println("PamProcess warning: Process " + getProcessName() + " is it's own parent - not allowed ! " + toString());
			 return null; // should not happen - would imply a block with no process.
		 }

//		 otehrwise, look around anyway, just in case sourceDatablock wasn't set. 
		 if (getParentProcess() == null){
			 System.out.println("PamProcess warning: Data block with no parentprocess in " + toString());
			 return null; // should not happen - would imply a block with no process.
		 }
		 return getParentProcess().getSourceProcess();

	 }

	/**
	 * find the absolute source of data, if one exists.
	 * i.e. the data output of a process that has no 
	 * data input. <p> 
	 * If this is called from within acquisition, it will
	 * return null <p>
	 * It there is no raw data source, i.e. if the type is 
	 * not PamRawDataBlock, then return null. 
	 * @return a PamRawDataBlock or null
	 */
	public PamDataBlock<PamDataUnit> getSourceDataBlock() {
		PamProcess prevProcess;
		PamDataBlock<PamDataUnit> prevDataBlock = getParentDataBlock();
		while (prevDataBlock != null) {
			prevProcess = prevDataBlock.getParentProcess();
			if (prevProcess == null) {
				break;
			}
			if (prevProcess.getParentDataBlock() == null) {
				break;
			}
			prevDataBlock = prevProcess.getParentDataBlock();
		}
		return prevDataBlock;
	}
	
	/**
	 * Get a process check object. This indicates the current status of a process. 
	 * @return the process check object.
	 */
	public ProcessCheck getProcessCheck() {
		return processCheck;
	}

	/**
	 * Set a process check object. This indicates the current status of a process. 
	 * @return the process check objects. 
	 */
	public void setProcessCheck(ProcessCheck processCheck) {
		this.processCheck = processCheck;
	}
	
	
	/**
	 * Find the absolute source of raw audio data
	 * if one exists<p>
	 * @return a PamRawDataBlock or null
	 */
	public PamRawDataBlock getRawSourceDataBlock() {
		PamDataBlock sourceDataBlock = getSourceDataBlock();
		if (sourceDataBlock == null) return null;
		if (PamRawDataBlock.class.isAssignableFrom(sourceDataBlock.getClass())) {
			return (PamRawDataBlock) sourceDataBlock;
		}
		return null;
	}

	/**
	 * Find the absolute source of raw audio data
	 * if one exists<p>
	 * @return a PamRawDataBlock or null
	 */
	@Deprecated
	public PamRawDataBlock getRawSourceDataBlock(float sampleRate) {
		return  getRawSourceDataBlock();
	}
//	public PamProcess getSourceProcess() {
//		if (parentDataBlock == null) return this;
//		// otehrwise, look around anyway, just in case sourceDatablock wasn't set. 
//		if (getParentProcess() == null){
//			System.out.println("PamProcess warning: Data block with no parentprocess in " + toString());
//			return null; // shoul not happen - would imply a block with no process.
//		}
//		return getParentProcess().getSourceProcess();
//	}
	
	public int getChainPosition() {
		// work out how many processes data went through before they got to this one
		int chainPos = 0;
		PamProcess parentProcess = this;
		PamProcess nextProcess;
		while (parentProcess != null) {
			nextProcess = parentProcess.getParentProcess();
			if (nextProcess == parentProcess) {
				break;
			}
			parentProcess = nextProcess;
			chainPos++;
		}
		return chainPos;
	}	

	/**
	 * Each process may produce multiple data blocks. This returns the reference
	 * to a specific block
	 * 
	 * @param block
	 *            Index of the required block
	 * @return PamDataBlock
	 */
	public PamDataBlock getOutputDataBlock(int block) {
		if (block < outputDataBlocks.size()) {
			return outputDataBlocks.get(block);
		} else
			return null;
	}

	/**
	 * @return Total number of PamDataBlocks created by this process
	 */
	public int getNumOutputDataBlocks() {
		return outputDataBlocks.size();
	}

	/**
	 * Adds an additional PamDataBlock to the process
	 * 
	 * @param outputDataBlock
	 *            Reference to a PamDataBlock
	 */
	public void addOutputDataBlock(PamDataBlock outputDataBlock) {
		if (outputDataBlocks.contains(outputDataBlock) == false){
			outputDataBlocks.add(outputDataBlock);
			PamController.getInstance().notifyModelChanged(PamControllerInterface.ADD_DATABLOCK);
		}
	}
	
//	/**
//	 * Add a new binary data source / sink. 
//	 * @param binaryDataSource
//	 */
//	public void addBinaryDataSource(BinaryDataSource binaryDataSource) {
//		if (binaryDataSources.contains(binaryDataSource) == false) {
//			binaryDataSources.add(binaryDataSource);
//		}
//	}
//
//	/**
//	 * 
//	 * @return an array list of available binary data sources
//	 */
//	public ArrayList<BinaryDataSource> getBinaryDataSources() {
//		return binaryDataSources;
//	}
	public void removeOutputDatablock(PamDataBlock outputDataBlock) {
		while (outputDataBlocks.remove(outputDataBlock)) {
			outputDataBlock.clearAll();
			outputDataBlock.remove();
			PamController.getInstance().notifyModelChanged(PamControllerInterface.REMOVE_DATABLOCK);
		}
	}
	
	/**
	 * Test to see if an output datablock exists.
	 * @param outputDataBlock Output datablock
	 * @return true if the output datablock exists. 
	 */
	public boolean hasOutputDatablock(PamDataBlock outputDataBlock) {
		return outputDataBlocks.contains(outputDataBlock);
	}

	@Override
	public String toString() {
		return processName;
	}


	/**
	 * Clears all data from all output data blocks of this process.
	 * <br>This gets called from the main controller at the 
	 * start of operations. Can be overridden in some classes
	 * which don't want to delete existing data or they can set the 
	 * clearAtStart flag in any data block. 
	 */
	public void clearOldData() {
		for (int i = 0; i < outputDataBlocks.size(); i++) {
			if (outputDataBlocks.get(i).isClearAtStart()) {
				outputDataBlocks.get(i).clearAll();
			}
		}
	}
	
	/**
	 * reset datablocks. Mostly concerned with setting timing counts in 
	 * row data which may need a bit of an extra kick when handling
	 * networked data. 
	 */
	public void resetDataBlocks() {
		for (int i = 0; i < outputDataBlocks.size(); i++) {
			outputDataBlocks.get(i).reset();
		}
	}

	/**
	 * New version of prepareProcess which get's called from 
	 * PAMController prior to PAMGuard starting up. If a single 
	 * process returns false from this function, startup will be
	 * aborted. <p>
	 * For backwards compatibility (to save the need to modify every
	 * process) there is a default function which simply calls the 
	 * older prepareProcess() function, then return true, but 
	 * processes which might fail can override this and return false
	 * instead if they so wish. <p>
	 * Initial motivation for this function was to stop millions of empty
	 * files being created when the watchdog is running but the DAQ can't 
	 * start. 
	 * @return true if it's looking highly likely that the process 
	 * is going to start OK. 
	 */
	public boolean prepareProcessOK() {
		prepareProcess();
		return true;
	}
	/**
	 * Called for each process before any of them receive the PamStart command
	 */
	public void prepareProcess() {
		if (getParentProcess() != null) {
			sampleRate = getParentProcess().getSampleRate();
		}
		//startMilliseconds = PamCalendar.getTimeInMillis();
	}
	
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 0;
	}

	@Override
	public final void addData(PamObservable o, PamDataUnit arg) {
		if (o == parentDataBlock && arg instanceof AcousticDataUnit) {
			lastAcousticDataunit = arg;
		}
		if (arg == null) {
			return;
		}
//		long cpuStart = SystemTiming.getProcessCPUTime();
		long threadId = Thread.currentThread().getId();
		long cpuStart = tmxb.getThreadCpuTime(threadId);
			newData(o, arg);
			if (processCheck != null) {
				processCheck.newInput(o, arg);
			}
//		long cpuEnd = SystemTiming.getProcessCPUTime();
		long cpuEnd = tmxb.getThreadCpuTime(threadId);
		cpuUsage += (cpuEnd - cpuStart);
	}
	
	Timer t = new Timer(1000, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			long now = System.currentTimeMillis();
			if (lastCPUCheckTime == now) return;
			/**
			 * cpuUsage is in ns intervals, now is in milliseconds, 
			 * so divide cpuUsage by 10^9, divide now by 10^3 and * all by 100 to 
			 * get percent. Total is -9+3+2 = /!0^4 !  
			 */
			cpuPercent = (double) cpuUsage / (now - lastCPUCheckTime) / 10000.;
			lastCPUCheckTime = now;
			cpuUsage = 0;
		}
	});

	private int lastSourceNotificationType;

	private Object lastSourceNotificationObject;
	
	public void newData(PamObservable o, PamDataUnit arg) {
		
	}
	
	public void updateData(PamObservable o, PamDataUnit arg) {
		
	}

	/**
	 * called for every process once the systemmodel has been created. 
	 * this is a good time to check out and find input data blocks and
	 * similar tasks. 
	 *
	 */
	public void setupProcess() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_MIXEDMODE) {
			autoSetDataBlockMixMode();
		}
	}
	
	private void autoSetDataBlockMixMode() {
		/*
		 * mixMode is set automatically from the constructor of each datablock.
		 * good to get it done so that it can be overridden when settings are loaded later on.
		 */ 
//		if (outputDataBlocks == null) return;
//		for (int i = 0; i < outputDataBlocks.size(); i++) {
//			outputDataBlocks.get(i).autoSetDataBlockMixMode();
//		}
	}
	

	public double getCpuPercent() {
		return cpuPercent;
	}
	
	/**
	 * Called for each process to tell it to start (may not be necessary for
	 * processes which are listening for data anyway).
	 */
	abstract public void pamStart();

	/**
	 * Stops the process.
	 */
	abstract public void pamStop();
	
	/**
	 * Called when a PamDataBlock observed by this PamProcess is
	 * removed. 
	 */
	public void removeObservable(PamObservable observable) {
		// TODO Auto-generated method stub
		// called when the source data block has been removed. 
		if (parentDataBlock == observable) {
			setParentDataBlock(null);
		}
	}
	public PamControlledUnit getPamControlledUnit() {
		return pamControlledUnit;
	}
	
	@Override
	public ProcessAnnotation getAnnotation(PamDataBlock pamDataBlock, int annotation) {
		// return a single default annotation
		return new ProcessAnnotation(this, this, pamControlledUnit.getUnitType(), getProcessName());
	}
	
	@Override
	public int getNumAnnotations(PamDataBlock pamDataBlock) {
		// return a single default annotation 
		return 1;
	}
	
	public void notifyModelChanged(int changeType) {
		int nB = getNumOutputDataBlocks();
		for (int i = 0; i < nB; i++) {
			getOutputDataBlock(i).notifyModelChanged(changeType);
		}
	}
	/**
	 * @param canMultiThread the canMultiThread to set
	 */
	public void setCanMultiThread(boolean canMultiThread) {
		this.canMultiThread = canMultiThread;
	}
	/**
	 * @return the canMultiThread
	 */
	public boolean isCanMultiThread() {
		return canMultiThread;
	}
		
	/**
	 * Save data (to binary files and to database)
	 * in viewer mode. 
	 * <p>
	 * This gets called automatically on system exit and can 
	 * also be called from the file menu. 
	 */
	public void saveViewerData() {
		int n = getNumOutputDataBlocks();
		for (int i = 0; i < n; i++) {
			getOutputDataBlock(i).saveViewerData();
		}
	}
	
	/**
	 * Request offline data.<p>
	 * This will be called from a PamDatablock in offline viewer mode
	 * from requestOfflineData(PamObserver observer, long startMillis, long endMillis).
	 * <p>this is used to request data from upstream processes, e.g. o get raw data
	 * to turn into FFT data units to go to the spectrogram display (possible going
	 * via decimators and any other processes before spitting out the right data. 
	 * @param dataBlock data block making the request. 
	 * @param endUser observer which made the original data request. 
	 * @param startMillis start time in milliseconds
	 * @param endMillis end time in milliseconds. 
	 * @param loadKeepLayers 
	 * @param cancellationObject 
	 * @return true if request can be satisfied (or partially satisfied). 
	 */
	public int getOfflineData(PamDataBlock dataBlock, PamObserver endUser, 
			long startMillis, long endMillis, int loadKeepLayers) {
//		if (getParentDataBlock() == null) {
//			return PamDataBlock.REQUEST_NO_DATA;
//		}
		return getOfflineData(new OfflineDataLoadInfo(this, endUser, startMillis, endMillis, loadKeepLayers-1, true));
	}
	
	
	public int getOfflineData(OfflineDataLoadInfo offlineLoadInfo) {
		if (getParentDataBlock() == null) {
			return PamDataBlock.REQUEST_NO_DATA;
		}
		
		//need to go up a layer.
		offlineLoadInfo.setLoadKeepLayers(offlineLoadInfo.getLoadKeepLayers()-1); 
		offlineLoadInfo.setCurrentObserver(this); 
		
		return getParentDataBlock().getOfflineData(offlineLoadInfo);
	}
	
	
	/**
	 * Work through all the output datablocks and wait for their
	 * internal buffers to flush through.<p>
	 * This is used when stopping PAMGUARD to ensure that all 
	 * data complete processing before anything else happens. 
	 * @param maxWait maxWait time in milliseconds. 
	 * @return true if successful, or false if there was a timeout. 
	 */
	public boolean flushDataBlockBuffers(long maxWait) {
		int errors = 0;
		int nDB = getNumOutputDataBlocks();
		for (int i = 0; i < nDB; i++) {
			if (getOutputDataBlock(i).waitForThreadedObservers(maxWait) == false) {
				errors++;
			}
		}
		return (errors == 0);
	}
	
	// do anything necessary to new data arriving from a buoy 
	public void processNewBuoyData(BuoyStatusDataUnit dataUnit, PamDataUnit dataUnit2) {
		
	}
	/**
	 * Get the full list of output datablocks - used in logger forms so they 
	 * can all be mapped easily once created. 
	 * @return the outputDataBlocks
	 */
	public ArrayList<PamDataBlock> getOutputDataBlocks() {
		return outputDataBlocks;
	}
	
	/**
	 * A list of data block class types which are compatible as parent data blocks for the PamProcess. This can return null, e.g. in the case of 
	 * Acquisition process. 
	 * @return a list of PamDataBlock sub class types which can be used as parent data blocks for the process. 
	 */
	public ArrayList<Class<? extends PamDataUnit>> getCompatibleDataUnits(){
		return null; 
	}
	
	/**
	 * Get whether  multiplex data blocks are allowed. 
	 * @return true if multiplex data blocks is enabled 
	 */
	public boolean isMultiplex() {
		return isMultiplex;
	}

	/**
	 * Set whether  multiplex data blocks are allowed. 
	 * @param true if multiplex data blocks is enabled 
	 */
	public void setMultiplex(boolean isMultiplex) {
		this.isMultiplex = isMultiplex;
	}
	
	/**
	 * Check whether the process can accept parent data blocks from other PamControlledUnits
	 * @return true to accept external data blocks from other PamControlledUnits. 
	 */
	public boolean isExternalProcess() {
		return isExternalProcess;
	}

	/**
	 * Set whether the process can accept parent data blocks from other PamControlledUnits
	 * @return true to accept external data blocks from other PamControlledUnits. 
	 */
	public void setExternalProcess(boolean isExternalProcess) {
		this.isExternalProcess = isExternalProcess;
	}
	
	/**
	 * Get a multiplex data block.
	 * @param block - datablock index
	 * @return a multiplex data block. 
	 */
	public PamDataBlock getMuiltiplexDataBlock(int block){
		if (multiPlexDataBlocks.size()<=block) return null; 
		return multiPlexDataBlocks.get(block); 
	}
	
	/**
	 * Get the number of multiplex datablocks. 
	 * @return the number of multiplex data blocks. 
	 */
	public int getNumMuiltiplexDataBlocks(){
		return this.multiPlexDataBlocks.size();
	}
	

	/**
	 * Adds the process as an observer to the data block. 
	 * @param pamDataBlock - the datablock to add.
	 */
	public void addMultiPlexDataBlock(PamDataBlock pamDataBlock){
		pamDataBlock.addObserver(this, PamModel.getPamModel().isMultiThread() & canMultiThread);
		this.multiPlexDataBlocks.add(pamDataBlock);
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
	}
	
	/**
	 * Remove the process from a data block and remove datablock from list of multiplex datablocks. 
	 * @param pamDataBlock - the data block to remove. 
	 */
	public void removeMultiPlexDataBlock(PamDataBlock pamDataBlock){
		pamDataBlock.deleteObserver(this);
		this.multiPlexDataBlocks.remove(pamDataBlock);
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
	}
	
	/**
	 * Remove a list of multiplex data blocks.
	 * @param pamDataBlocks -data blocks to remove. 
	 */
	public void removeAllMultiPlexDataBlocks(ArrayList<PamDataBlock> pamDataBlocks){
		for (int i=0; i<pamDataBlocks.size(); i++){
			pamDataBlocks.get(i).deleteObserver(this);
		}
		this.multiPlexDataBlocks.removeAll(pamDataBlocks);
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		lastSourceNotificationType = type;
		lastSourceNotificationObject = object;
		// cascade the notification down through all of the output data blocks for this process as well
		for (PamDataBlock anOutputBlock : outputDataBlocks) {
			for (int i=0; i<anOutputBlock.countObservers(); i++) {
				anOutputBlock.getPamObserver(i).receiveSourceNotification(type, object);
			}
		}
	}

	/**
	 * @return the lastSourceNotificationType
	 */
	public int getLastSourceNotificationType() {
		return lastSourceNotificationType;
	}

	/**
	 * @return the lastSourceNotificationObject
	 */
	public Object getLastSourceNotificationObject() {
		return lastSourceNotificationObject;
	}

}
