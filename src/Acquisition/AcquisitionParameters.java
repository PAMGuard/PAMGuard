package Acquisition;

import hfDaqCard.SmruDaqSystem;

import java.io.Serializable;
import java.lang.reflect.Field;

import Acquisition.gpstiming.PPSParameters;
import Array.Preamplifier;
import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamguardMVC.PamConstants;

public class AcquisitionParameters implements Serializable, Cloneable, ManagedParameters {

	static final long serialVersionUID = 2;
	
	String daqSystemType = "Sound Card";
	
	public float sampleRate = 48000;
	
	public int nChannels = 2;
	
	public double voltsPeak2Peak = 5;
	
	private transient boolean isNetReceive;
	
	/**
	 * List of channels data are acquired from (not necessarily 0,1,2, etc.) 
	 * With NI boards, this has become a pain since if multiple boards are
	 * used, there may be repeats within this list.  
	 */
	private int channelList[];// = new int[PamConstants.MAX_CHANNELS]; //Xiao Yan Deng
	
	public Preamplifier preamplifier = new Preamplifier(0, new double[] {0, 20000});

	/**
	 * Hydrophone list is a short list of length equal to the number of channels, so if
	 * your channel list does not start at zero, you have to first use the 
	 * channelListIndexes before using this lookup table. 
	 */
	private int[] hydrophoneList;
	
	/**
	 * list of indexes for each hardware channel in channelList (i.e. opposite LUT)
	 */
	transient int[] channelListIndexes; 
	
	private PPSParameters ppsParameters;

	public boolean subtractDC;

	public double dcTimeConstant; // time constant for DC subtraction in seconds. 
	

	public AcquisitionParameters() {
		getHardwareChannelList(); // automatically create a channellist.
		/*
		 * this won't work since the constructor is only ever called for a new config, so if an old
		 * config is loaded, this will never be called. 
		 */
		isNetReceive = PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER;
	}
	
	@Override
	public AcquisitionParameters clone() {
		try {
			AcquisitionParameters ap = (AcquisitionParameters) super.clone();
			if (ap.preamplifier != null) {
				ap.preamplifier = ap.preamplifier.clone();
			}
			/*
			 * Automatically convert the old SMRU daq system name into the new SAIL name
			 */
			if (ap.daqSystemType != null && SmruDaqSystem.oldCardName.equals(ap.daqSystemType)) {
				ap.daqSystemType = SmruDaqSystem.newCardName;
			}
			ap.isNetReceive = PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER;
			if (ap.ppsParameters == null) {
				ap.ppsParameters = new PPSParameters();
			}
			else {
				ap.ppsParameters = ap.ppsParameters.clone();
			}
			if (ap.dcTimeConstant <= 0) {
				ap.dcTimeConstant = 1.;
			}
			return ap;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	public String getDaqSystemType() {
		return daqSystemType;
	}

	public void setDaqSystemType(String daqSystemType) {
		this.daqSystemType = daqSystemType;
	}

	/**
	 * Gets a list of hydrophones from channel Indexes (not channel numbers)
	 * @return list of hydrophones. 
	 */
	public int[] getHydrophoneList() {
		if ((hydrophoneList == null || hydrophoneList.length < nChannels) && nChannels > 0) {
			hydrophoneList = new int[nChannels];
			for (int i = 0; i < nChannels; i++) {
				hydrophoneList[i] = i;
			}
		}
		return hydrophoneList;
	}

	/**
	 * Set a hydrophone list. 
	 * @param hydrophoneList
	 */
	public void setHydrophoneList(int[] hydrophoneList) {
		this.hydrophoneList = hydrophoneList;
	}

	/** 
	 * Gets a hydrophone number from a channel number (not channel index) 
	 * @param channel software channel number
	 * @return a specific hydrophone number from the selected array
	 */
	public int getHydrophone(int channel) {
		if (isNetReceive) {
			return channel;
		}
		// first convert the software channel to a channel index (i.e.
		// if we're reading ch 4 and 5, convert to 0 or 1.
		if (hydrophoneList == null) {
			hydrophoneList = getHydrophoneList();
		}
		if (channelListIndexes == null) {
			sortChannelListIndexes();
		}
		if (channel < 0) {
			return channel;
		}
		if (channelListIndexes.length <= channel) return -1;
		// this line no longer needed now that software channels are alwasy 0,1,2,3
		// even if hardware channels are more random. 
//		channel = this.channelListIndexes[channel];
		//channel = this.channelListIndexes[channel];
		if (channel < 0) {
			return -1;
		}
		if (hydrophoneList.length <= channel) return -1;
		return hydrophoneList[channel];
	}
	
	public int getNChannels() {
		return nChannels;
	}

	public void setNChannels(int channels) {
		nChannels = channels;
	}
	
	public int[] getNChannelList(){
		return channelList;
	}

	public Preamplifier getPreamplifier() {
		return preamplifier;
	}

	public void setPreamplifier(Preamplifier preamplifier) {
		this.preamplifier = preamplifier;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	public double getVoltsPeak2Peak() {
		return voltsPeak2Peak;
	}

	public void setVoltsPeak2Peak(double voltsPeak2Peak) {
		this.voltsPeak2Peak = voltsPeak2Peak;
	}

	/**
	 * Gets / creates a list of hardware channels used. <p>
	 * i.e. converts from channel indexes to channel numbers. 
	 * @return List of channel numbers
	 */
	public int[] getHardwareChannelList() {
		if (channelList == null || ((channelList.length < nChannels) && nChannels > 0)) {
			int[] newChannelList = new int[PamConstants.MAX_CHANNELS];
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
				newChannelList[i] = i;
			}
			// use old list numbers if they exist.
			if (channelList != null) {
				for (int i = 0; i < Math.min(nChannels, channelList.length); i++) {
					newChannelList[i] = channelList[i];
				}
			}
			// now replace the old with the new.
			channelList = newChannelList;
		}
		return channelList;
	}

	public void setChannelList(int[] channelList) {
		this.channelList = channelList;
		sortChannelListIndexes();
	}
	
	public void setChannelList(int index, int channelNumber) {
		getHardwareChannelList();
		if (index >= channelList.length) {
			return;
		}
		channelList[index] = channelNumber;
		sortChannelListIndexes();
	}
	
	/**
	 * Creates a default channel list 0,1,2,3,4 etc.
	 */
	public void setDefaultChannelList() {
		channelList = new int[PamConstants.MAX_CHANNELS];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			channelList[i] = i;
		}
		sortChannelListIndexes();
	}
	
	/**
	 * Gets the channel number for a particular channel index. 
	 * @param index channel index
	 * @return channel number
	 */
	public int getChannelList(int index) {
		return getHardwareChannelList()[index];
	}
	
	/**
	 * Gets the complete list of channel indexes. 
	 * @return list of channel indexes. 
	 */
	public int[] getChannelListIndexes() {
		if (channelListIndexes == null) {
			sortChannelListIndexes();
		}
		return channelListIndexes;
	}
	
	/**
	 * Sets the channel list indeces
	 * @param channelListIndexes
	 */
	public void setChannelListIndexes(int[] channelListIndexes) {
		this.channelListIndexes = channelListIndexes;
	}
	
	/**
	 * Gets the channel index for a particular hardware channel 
	 * @param channel
	 * @return channel index or -1 if it doesn't exist. 
	 */
	public int getChannelListIndexes(int channel) {
		if (channelListIndexes == null) {
			sortChannelListIndexes();
		}
		if (channel >= 0 && channel < channelListIndexes.length) {
			return channelListIndexes[channel];
		}
		else return -1;
	}
	
	/**
	 * Creates a set of easily accessible channel indexes
	 * which can be used to convert from channel numbers to 
	 * channel index e.g. used channel numbers might be 3 and 4
	 * so the listIndexes will be {-1 -1 -1 0 1]
	 */
	private void sortChannelListIndexes() {
		int max = 0;
		getHardwareChannelList();
		for (int i = 0; i < nChannels; i++) {
			max = Math.max(max, channelList[i]);
		}
		
		channelListIndexes = new int[max+1];
		for (int i = 0; i < channelListIndexes.length; i++) {
			channelListIndexes[i] = -1;
		}
		for (int i = 0; i < nChannels; i++) {
			channelListIndexes[channelList[i]] = i;
		}
	}
//	public void setChannelListIndexes(int[] channelListIndexes) {
//		this.channelListIndexes = channelListIndexes;
//	}
	/**
	 * @return the ppsParameters
	 */
	public PPSParameters getPpsParameters() {
		if (ppsParameters == null) {
			ppsParameters = new PPSParameters();
		}
		return ppsParameters;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("channelList");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return channelList;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
