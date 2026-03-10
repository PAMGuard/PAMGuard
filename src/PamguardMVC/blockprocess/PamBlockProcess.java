package PamguardMVC.blockprocess;

import java.util.List;
import java.util.ListIterator;

import Acquisition.AcquisitionProcess;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamModel.PamModel;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.ObservedObject;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.ThreadedObserver;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Modified PamProcess which can block data for analysis in chunks rather than a continuous data stream. <p>
 * There are two or three options with block processes and some messing about to do at PAMGuard starts and stops:<br>
 * 1. Calls into parent process are separated for processing background and processing data<br>
 * 2. Can also handle reverse input buffering<br>
 * 3. Extra things to think about and decide upon at end of a file / run, ie. whether or not to process an incomplete block. <br>
 * Complicated threading so a block can be simultaneously filling and emptying at the same time.  <br>
 * @author dg50
 *
 */
public abstract class PamBlockProcess extends PamProcess {
	
	private PamBlockDataList fillingUnits;
	
	private ThreadedObserver threadedObserver;
	
	private EndObservable endObservable;

	private boolean firstBlock;

	private PamWarning debugWarning;
	
	/**
	 * Last received data unit - used for working out timing offsets. 
	 * This is used to override lastAcouticDataUnit in PamProcess which 
	 * can get seriously messed up in multi-file data whereby data from the
	 * next file may be coming in before the previous file is ended
	 * (this shouldn't happen?). 
	 */
	private PamDataUnit lastSentDataUnit;

	public PamBlockProcess(PamControlledUnit pamControlledUnit, PamDataBlock parentDataBlock) {
		super(pamControlledUnit, parentDataBlock);
		endObservable = new EndObservable();
	}

	@Override
	public void setParentDataBlock(PamDataBlock newParentDataBlock, boolean reThread) {
		/*
		 *  never rethread this as an observer, since there is absolutely no need to since
		 *  we're about to do a shit ton of rethreading anyway. 
		 */
		boolean isNew = newParentDataBlock != getParentDataBlock();
		if (!isNew) {
			return;
		}
		super.setParentDataBlock(newParentDataBlock, false);
		threadedObserver = new PamBlockObserver(newParentDataBlock, endObservable);
	}

	public PamBlockProcess(PamControlledUnit pamControlledUnit, PamDataBlock parentDataBlock, String processName) {
		super(pamControlledUnit, parentDataBlock, processName);
	}

	@Override
	public void pamStart() {
		// these will need to move to daqStart when it exists
		// so that they are in synch with daq status changes
		fillingUnits = new PamBlockDataList();
		firstBlock = true;
		PamBlockParams params = getBlockParams();
		int normalJitter = PamModel.getPamModel().getPamModelSettings().getThreadingJitterMillis();
		if (params.blockMode == BlockMode.NONE) {
			threadedObserver.setMaxJitter(normalJitter);
		}
		else {
			// need 2x the block length I think. Ones to stack the data and a second time to 
			// allow those data to be processed before more can go in ?
			threadedObserver.setMaxJitter(Math.max(normalJitter, params.blockLengthMillis * 2));
		}
	}

	@Override
	public void pamStop() {

	}

	/**
	 * Override the PamProcess new data call. It's here that we're going to 
	 * stack up all the input data into blocks. Then pass on to two separate functions
	 * blockBackground and blockData.
	 */
	@Override
	public final void newData(PamObservable o, PamDataUnit dataUnit) {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			return;
		}
		// one or both of these should be true. 
		if (sendSingles()) {
			threadedObserver.addData(o, dataUnit); // put the single data unit into the thread queue
		}
		if (sendBlocks()) {
			// then add the data unit to the growing list
			fillingUnits.add(dataUnit);
			if (isLastChannel(o, dataUnit) && isFullBlock(fillingUnits)) {
				// and add that full list to the thread queue. 
				threadedObserver.addData(getParentDataBlock(), fillingUnits);
				fillingUnits = new PamBlockDataList();
			}
		}
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
		if (getSampleRate() == 0) {
			return PamCalendar.getTimeInMillis();
		}
		if (lastSentDataUnit != null) {
			// just do a relative jump of the last data unit. this way, it should pick up the corrected millisecond time off the 
			// original raw data unit. 
			return lastSentDataUnit.getTimeMilliseconds() + relSamplesToMilliseconds(samples-lastSentDataUnit.getStartSample());
		}
		else {
			return (long) (samples * 1000. / getSampleRate()) + PamCalendar.getSessionStartTime();
		}
	}
	
	@Override
	public void receiveSourceNotification(int type, Object object) {
		
		// if the daq source has finished sending data, just send whatever is currently in
		// the fillingUnits list to the observer for processing, then send on the notification
		if (type==AcquisitionProcess.LASTDATA && fillingUnits != null && fillingUnits.getNumDataUnits()>0) {
			threadedObserver.addData(getParentDataBlock(), fillingUnits);
			threadedObserver.receiveSourceNotification(type, object);
			long mtTime = threadedObserver.waitToEmpty(getBlockParams().blockLengthMillis); // wait for it to empty before returning here. 
			if (mtTime < 0) {
							System.out.printf("Warning code %d from block observer in %s\n", mtTime, getProcessName());
			}
		}
		else {
			threadedObserver.receiveSourceNotification(type, object);
		}
	}

	private boolean sendSingles() {
		PamBlockParams params = getBlockParams();
		switch (params.blockMode) {
		case BLOCKED:
			return true;
		case BLOCKFIRST:
			return true;
		case NONE:
			return true;
		case REVERSEFIRST:
			return !firstBlock;
		default:
			break;
		}
		return false;
	}
	
	private boolean sendBlocks() {
		PamBlockParams params = getBlockParams();
		switch (params.blockMode) {
		case BLOCKED:
			return true;
		case BLOCKFIRST:
			return firstBlock;
		case NONE:
			return false;
		case REVERSEFIRST:
			return firstBlock;
		default:
			break;
		}
		return false;
	}
	
	private boolean isFullBlock(PamBlockDataList fillingUnits) {
//		if (fillingUnits.getNumDataUnits() == 465) {
//			// bodge while debugging to force exact same length as Matlab code
//			return true;
//		}
		return fillingUnits.getDurationMillis() >= getBlockParams().blockLengthMillis;
	}

	private void processBlock(PamBlockDataList fillingUnits) {
		threadedObserver.addData(getParentDataBlock(), fillingUnits);
	}

	/**
	 * Is this the last channel in a group from that datablock ? 
	 * @param channelMap
	 * @return
	 */
	public boolean isLastChannel(PamObservable obs, PamDataUnit dataUnit) {
		int unitMap = dataUnit.getSequenceBitmap();
		int blockMap;
		if (obs instanceof PamDataBlock) {
			blockMap = ((PamDataBlock) obs).getSequenceMap();
		}
		else {
			blockMap = dataUnit.getParentDataBlock().getSequenceMap();
		}
		int highChan = PamUtils.getHighestChannel(blockMap);
		return (((1<<highChan) & unitMap) != 0);
	}
	
	abstract public PamBlockParams getBlockParams();
	/**
	 * Set the block state - tell it what to expect next so that it can clean up
	 * at the start middle and end of a block of data. 
	 * @param state
	 */
	abstract public void setBlockState(BlockState state);
	
	/**
	 * Split a data list into individual channels. 
	 * @param dataList data list with multiple channels of data. Assuming it's a type of data
	 * which only has one channel per data unit (e.g. raw or fft) 
	 * @return list of data separated by channel. If channels were not continuous, then there may be some nulls. 
	 */
	public PamBlockDataList[] channelSplitList(PamBlockDataList dataList) {
		int chanMap = getParentDataBlock().getSequenceMap();
		int highChan = PamUtils.getHighestChannel(chanMap);
		int loChan = PamUtils.getLowestChannel(chanMap);
		if (loChan == highChan) {
			PamBlockDataList[] onlyOne = {dataList};
			return onlyOne;
		}
		PamBlockDataList[] blockLists = new PamBlockDataList[highChan-loChan+1];
		
		List<PamDataUnit> unitList = dataList.getList();
		ListIterator<PamDataUnit> it = unitList.listIterator();
		while (it.hasNext()) {
			PamDataUnit unit = it.next();
			int chanInd = PamUtils.getSingleChannel(unit.getSequenceBitmap())-loChan;
			if (blockLists[chanInd] == null) {
				blockLists[chanInd] = new PamBlockDataList();
			}
			blockLists[chanInd].add(unit);
		}
		return blockLists;
	}

	/**
	 * Reverse a data list into a new list. Swap channel orders, so channels are
	 * in correct order, but time is reversed. 
	 * @param blockDataList
	 * @return
	 */
	public PamBlockDataList reverseDataList(PamBlockDataList dataList) {
		/*
		 *  iterate backwards through data, but need to send data in correct channel 
		 *  order, so need to make a temp store of the data and send each time we 
		 *  find the first channel.  
		 */
		PamBlockDataList reverseList = new PamBlockDataList();
		int chanMap = getParentDataBlock().getSequenceMap();
		int highChan = PamUtils.getHighestChannel(chanMap);
		int loChan = PamUtils.getLowestChannel(chanMap);
		/*
		 * Data had better be correctly interleaved, or this is going to go badly wrong. 
		 * and it's also going to go wrong if a data unit has > 1 channel in it, in which case we
		 * probably just want to send the units in whatever order they are in. Need to think on this
		 * a bit ! Will always be OK for single getSequenceBitmap data. 
		 */
		PamDataUnit[] channelUnits = new PamDataUnit[highChan+1];
		List<PamDataUnit> unitList = dataList.getList();
		int haveChan = 0;
		ListIterator<PamDataUnit> it = unitList.listIterator(unitList.size()-1);
		while (it.hasPrevious()) {
			PamDataUnit du = it.previous();
			int chan = PamUtils.getSingleChannel(du.getSequenceBitmap());
			channelUnits[chan] = du;
			haveChan |= du.getSequenceBitmap();
			if (haveChan == chanMap) {
				for (int i = loChan; i <= highChan; i++) {
					if (channelUnits[i] != null) {
						reverseList.add(channelUnits[i]);
					}
				}
			}
			haveChan = 0;
		}
		return reverseList;
	}

	/**
	 * Called in sequence for data in a block so it can make background measurements
	 * @param blockDataList blocked list of data units (possibly interleaved by channel)
	 */
	abstract public void addBlockData(PamBlockDataList blockDataList); 

	/**
	 * Called in sequence for data in a block so it can run detectors
	 * @param o
	 * @param dataUnit
	 */
	abstract public void addSingleData(PamObservable o, PamDataUnit dataUnit); 
	
	protected void debugMessage(String message) {
		if (message == null) {
			if (debugWarning != null) {
				WarningSystem.getWarningSystem().removeWarning(debugWarning);
			}
			return;
		}
		if (debugWarning == null) {
			debugWarning = new PamWarning(getPamControlledUnit().getUnitName(), message, 1);
			WarningSystem.getWarningSystem().addWarning(debugWarning);
		}
		else {
			debugWarning.setWarningMessage(message);
		}
	}
	
	private class EndObservable implements PamObserver {

		@Override
		public long getRequiredDataHistory(PamObservable observable, Object arg) {
			// TODO Auto-generated method stub
			return 0;
		}


		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			/**
			 * To keep things simple (flexible), this now gets all the data up to two times, once as single
			 * data units and once as blocked lists. All will be synchronized, with blocked lists always containing data 
			 * that have already been sent. 
			 */
			if (pamDataUnit instanceof PamBlockDataList) {
				PamBlockDataList blockList = (PamBlockDataList) pamDataUnit;
				lastSentDataUnit = blockList.getList().get(0);
				addBlockData(blockList);
				firstBlock = false;
			}
			else {
				addSingleData(observable, pamDataUnit);
				lastSentDataUnit = pamDataUnit;
			}
		}
		
		

//		private void processNewSingleUnit(PamDataUnit pamDataUnit) {
//			PamBlockParams params = getBlockParams();
//			switch(params.blockMode) {
//			case BLOCKED:
//			case BLOCKBYFILE:
//				blockBackground(getParentDataBlock(), pamDataUnit);
//				break;
//			case BLOCKFIRST:
//				blockBackground(getParentDataBlock(), pamDataUnit);
//				if (!firstBlock) {
//					blockData(getParentDataBlock(), pamDataUnit);
//				}
//				break;
//			case NONE:
//				blockBackground(getParentDataBlock(), pamDataUnit);
//				blockData(getParentDataBlock(), pamDataUnit);
//				break;
//			case REVERSEFIRST:
//				if (!firstBlock) {
//					blockBackground(getParentDataBlock(), pamDataUnit);
//					blockData(getParentDataBlock(), pamDataUnit);
//				}
//				break;			
//			}
//			
//		}

//		private void processNewList(PamBlockDataList dataList) {
//			PamBlockParams params = getBlockParams();
//			ListIterator<PamDataUnit> it;
//			switch(params.blockMode) {
//			case BLOCKED:
//			case BLOCKBYFILE:
//				/**
//				 * background will have been sent directly, so
//				 * no need to do that here. 
//				 */
//				setBlockState(BlockState.DATA);
//				sendBlockData(dataList);
//				setBlockState(BlockState.BACKGROUND);
//				break;
//			case BLOCKFIRST:
//				if (firstBlock) {
//					/*
//					 * For first block data, background already sent, then after this data will get sent directly. 
//					 */
//					setBlockState(BlockState.DATA);
//					sendBlockData(dataList);
//					setBlockState(BlockState.BACKGROUND);
//				}
//				break;
//			case NONE:
//				// nothing to do here. 
//				break;
//			case REVERSEFIRST:
//				if (firstBlock) {
//					/*
//					 * For first block data, do reverse background THEN first data; after this data will get sent directly. 
//					 */
//					setBlockState(BlockState.BACKGROUND);
//					sendReverseBlockBackground(dataList);
//					setBlockState(BlockState.DATA);
//					sendBlockData(dataList);
//					setBlockState(BlockState.BACKGROUND);
//				}
//				break;
//			default:
//				break;
//			
//			}
//			
//		}
		
//		private void sendBlockBackground(PamBlockDataList dataList) {
//			ListIterator<PamDataUnit> it = dataList.getList().listIterator();
//			PamDataBlock dataBlock = getParentDataBlock();
//			while (it.hasNext()) {
//				PamDataUnit dataUnit = it.next();
//				blockBackground(dataBlock, dataUnit);
//			}
//			
//		}
//		
//		private void sendBlockData(PamBlockDataList dataList) {
//			ListIterator<PamDataUnit> it = dataList.getList().listIterator();
//			PamDataBlock dataBlock = getParentDataBlock();
//			while (it.hasNext()) {
//				PamDataUnit dataUnit = it.next();
//				blockData(dataBlock, dataUnit);
//			}
//			
//		}
//		private void sendReverseBlockBackground(PamBlockDataList dataList) {
//			/*
//			 *  iterate backwards through data, but need to send data in correct channel 
//			 *  order, so need to make a temp store of the data and send each time we 
//			 *  find the first channel.  
//			 */
//			int chanMap = getParentDataBlock().getChannelMap();
//			int highChan = PamUtils.getHighestChannel(chanMap);
//			int loChan = PamUtils.getLowestChannel(chanMap);
//			/*
//			 * Data had better be correctly interleaved, or this is going to go badly wrong. 
//			 * and it's also going to go wrong if a data unit has > 1 channel in it, in which case we
//			 * probably just want to send the units in whatever order they are in. Need to think on this
//			 * a bit ! Will always be OK for single channel data. 
//			 */
//			PamDataUnit[] channelUnits = new PamDataUnit[highChan+1];
//			List<PamDataUnit> unitList = dataList.getList();
//			int haveChan = 0;
//			ListIterator<PamDataUnit> it = unitList.listIterator(unitList.size()-1);
//			while (it.hasPrevious()) {
//				PamDataUnit du = it.previous();
//				int chan = PamUtils.getSingleChannel(du.getSequenceBitmap());
//				channelUnits[chan] = du;
//				haveChan |= du.getSequenceBitmap();
//				if (haveChan == chanMap) {
//					for (int i = loChan; i <= highChan; i++) {
//						if (channelUnits[i] != null) {
//							blockBackground(parentDataBlock, channelUnits[i]);
//						}
//					}
//				}
//				haveChan = 0;
//			}
//			
//		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			PamBlockProcess.super.updateData(observable, pamDataUnit);			
		}

		@Override
		public void removeObservable(PamObservable observable) {
			PamBlockProcess.super.removeObservable(observable);
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			PamBlockProcess.super.setSampleRate(sampleRate, notify);
		}

		@Override
		public void noteNewSettings() {
			PamBlockProcess.super.noteNewSettings();
			
		}

		@Override
		public String getObserverName() {
			return PamBlockProcess.super.getObserverName();
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			PamBlockProcess.super.masterClockUpdate(milliSeconds, sampleNumber);			
		}

		@Override
		public PamObserver getObserverObject() {
			return PamBlockProcess.this;
		}

		@Override
		public void receiveSourceNotification(int type, Object object) {
			PamBlockProcess.super.receiveSourceNotification(type, object);
		}
	}
	
	/**
	 * Bespoke ThreadedObserver that takes into account the different types of
	 * blocking when deciding how long to wait for the queue to empty before
	 * adding new data objects.
	 * 
	 * @author mo55
	 *
	 */
	private class PamBlockObserver extends ThreadedObserver {

		/**
		 * @param pamObservable
		 * @param singleThreadObserver
		 */
		public PamBlockObserver(PamObservable pamObservable, PamObserver singleThreadObserver) {
			super(pamObservable, singleThreadObserver);
		}

		@Override
		protected void waitForQueueToBeReady(ObservedObject theObject) {
//			super.waitForQueueToBeReady(theObject);

			PamBlockParams params = getBlockParams();
			while (true) {
				long[] queueLimits = getQueueLimits();
				int queueSize = getInterThreadListSize();
				if (queueLimits == null || queueSize == 0) { // nothing in queue. 
					break;
				}
				long dT = theObject.getTimeMillis() - queueLimits[0];
				boolean needSleep = false;
				switch (params.blockMode) {
				case BLOCKBYFILE:
				case BLOCKED:
					needSleep = dT > params.blockLengthMillis;
					break;
				case BLOCKFIRST:
				case REVERSEFIRST:
					if (firstBlock) {
						needSleep = dT > params.blockLengthMillis;
					}
					else {
						/*
						 * This is where we need something a lot more sophisticated for real time operation 
						 * so that 
						 */
						needSleep = dT > getMaxJitter();
					}
					break;
				case NONE:
					needSleep = dT > getMaxJitter();
					break;
				default:
					break;
				}
				if (needSleep) {
					sleepWriter();
				}
				else {
					break;
				}
			}
		}

		
	}

}
