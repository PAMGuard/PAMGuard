/**
 * 
 */
package beamformer.continuous;

import java.util.ArrayList;
import java.util.Hashtable;

import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * 
 * This class can be used within a detector that may or may not be 
 * attached to a beam former (alternative is probably raw fft data). If
 * it's a beam former, then when multiple beams detect the same sound, it
 * will select only the loudest or some other 'best' from the available
 * detections. <p>
 * The class is abstract so that bespoke behaviour to select the best one of different
 * types of data unit can be implemented. 
 * @author Doug Gillespie
 *
 */
public abstract class BestBeamSelector<T extends PamDataUnit> {

	private PamDataBlock<T> outputDataBlock;
	
	private PamDataBlock sourceDataBlock;

	private boolean isBeamData;
	
	private Hashtable<Integer, GroupData> heldData = new Hashtable();
//	private ArrayList<T> heldDataUnits = new ArrayList<>();
	
	
	/**
	 * Needs the source data (to see if it's beam data or not) and the output data block. 
	 * @param sourceDataBlock source data block (FFT Data or beam former data)
	 * @param outputDataBlock output datablock from a detector
	 */
	public BestBeamSelector(PamDataBlock sourceDataBlock, PamDataBlock<T> outputDataBlock) {
		super();
		this.sourceDataBlock = sourceDataBlock;
		this.outputDataBlock = outputDataBlock;
		if (BeamFormerDataBlock.class == sourceDataBlock.getClass()) {
			isBeamData = true;
		}
		else {
			isBeamData = false;
		}
	}

	/**
	 * Add new pam data unit's here instead of to the output datablock. 
	 * they will get held in a list, then when a set of channels have 
	 * completed their new data units, it will select the best and pass it on 
	 * to the actual data block, discarding the rest. 
	 * @param newDataUnit New data unit
	 * @param channel 
	 * @return true if it was added directly to the output datablock. 
	 */
	public synchronized boolean addPamData(T newDataUnit) {
		if (!isBeamData) {
			outputDataBlock.addPamData(newDataUnit);
			return true;
		}
		else {
			GroupData groupData = findGroupData(newDataUnit.getChannelBitmap());
			groupData.heldDataUnits.add(newDataUnit);
			return false;
		}
	}
	
	private GroupData findGroupData(int channelMap) {
		GroupData h = heldData.get(channelMap);
		if (h == null) {
			h = new GroupData(channelMap);
			heldData.put(channelMap, h);
		}
		return h;
	}
	/**
	 * Set the detection status of a channel detector. This should be put to true for a 
	 * channel as soon as there is a putative detection. When all flags are zero it will 
	 * look to see which is best and send it on to the output data block. <p>
	 * This should always
	 * be called after a detection has been added to this selector with state 0. 
	 * @param seqNo sequence (channel) number 0 to 31 
	 * @param state statue of detection = 1 forming, 0 formed or below threshold. 
	 */
	public synchronized void setSequenceState(int channelMap, int seqNo, boolean state){
		GroupData groupData = findGroupData(channelMap);
		int oldState = groupData.sequenceState;
		groupData.sequenceState = PamUtils.SetBit(groupData.sequenceState, seqNo, state);
		if (oldState != 0 && groupData.sequenceState == 0) {
			T bestDataUnit = getBestDataUnit(groupData.heldDataUnits);
			if (bestDataUnit != null) {
				outputDataBlock.addPamData(bestDataUnit);
			}
			groupData.heldDataUnits.clear();
		}
	}
	
	private class GroupData {
		private int channelMap;
		public int sequenceState;
		public ArrayList<T> heldDataUnits = new ArrayList<>();
		public GroupData(int channelMap) {
			super();
			this.channelMap = channelMap;
		}
	}

	/**
	 * Select the best data unit from all channels / sequences. 
	 * @param heldDataUnits list of held data units
	 * @return best data unit or null (if there were no data units or if none of them were worth keeping)
	 */
	public abstract T getBestDataUnit(ArrayList<T> heldDataUnits);

	
}
