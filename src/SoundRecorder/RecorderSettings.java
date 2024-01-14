package SoundRecorder;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import PamController.PamController;
import PamController.PamFolders;
import PamModel.SMRUEnable;
import PamModel.parametermanager.FieldNotFoundException;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterData;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.PamUtils;
import PamguardMVC.PamRawDataBlock;
import SoundRecorder.trigger.RecorderTrigger;
import SoundRecorder.trigger.RecorderTriggerData;

/**
 * Control parameters for sound recorders.
 * @author Doug Gillespie
 *
 */
public class RecorderSettings implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 3;

	/**
	 * Allowable bit depths. 
	 */
	static public final int[] BITDEPTHS = {8, 16, 24};
	/**
	 * Name of the raw data source
	 */
	public String rawDataSource;

	/**
	 * Bitmap of channels to be saved (need not be all available channels)
	 */
	private int channelBitmap = 3;

	/**
	 * bit depth (NOT Byte depth) of the recording format.
	 */
	int bitDepth = 16;

	/**
	 * Buffer data so that it can be added to the start of a file
	 */
	public boolean enableBuffer = false;

	/**
	 * Length of the buffered data to store
	 */
	public int bufferLength = 30; //seconds.

	/**
	 * Output folder for recording files
	 */
	public String outputFolder;

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	/**
	 * Initials to add to the start of a file name, the rest
	 * of which is made up from the date. 
	 */
	public String fileInitials = "PAM";

	/**
	 * Unfortunately AudioFileFormat.Type is not serialized, so store
	 * as a string and have getters and setters to sort out the mess
	 */
	private String fileType = "WAVE";

	/**
	 * Number of seconds between automatic recordings 
	 */
	public int autoInterval = 300;

	/**
	 * Duration of automatic recordings in seconds
	 */
	public int autoDuration = 10;

	/**
	 * Maximum length of a single file in seconds
	 */
	public int maxLengthSeconds = 3600;

	/**
	 * Limit the maximum length of a single file in seconds
	 */
	boolean limitLengthSeconds = true;
	
	/**
	 * Oppostie of what we want so new default is really true. 
	 */
	private boolean notRoundFileStarts = false;
	
	/**
	 * 
	 * @return if file start times should be rounded to 'nice' times. 
	 */
	public boolean isRoundFileStarts() {
		return !notRoundFileStarts;
	}
	
	/**
	 * 
	 * @param roundFileStarts File start times will be rounded to 'nice' times. 
	 */
	public void setRoundFileStarts(boolean roundFileStarts) {
		notRoundFileStarts = !roundFileStarts;
	}

	/**
	 * Maximum length of a single file in Mega bytes
	 */
	public long maxLengthMegaBytes = 640;

	/**
	 * Limit the maximum length of a single file in Mega bytes
	 */
	boolean limitLengthMegaBytes = true;

	/**
	 * Store data in subfolders organised by date. 
	 */
	boolean datedSubFolders = true;

	/**
	 * Enable triggers (from detectors). It is possible that the
	 * number of triggers will increase, in which case, this array
	 * will get extended before it is next saved.
	 */
	private ArrayList<RecorderTriggerData> recorderTriggerDatas = new ArrayList<RecorderTriggerData>();

	RecorderSettings() {
		/*
		 * By default set the output directory as the current directory 
		 * and the data source as the first data source. The second time
		 * PG runs, these will be overwritten by whatever they've been
		 * set to from data in the set file. 
		 */
		outputFolder = getDefaultOutputFolder();

		PamRawDataBlock prd = PamController.getInstance().getRawDataBlock(0);
		if (prd != null) {
			rawDataSource = prd.getDataName();
		}
	}

	//	/**
	//	 * Get the trigger state for a trigger
	//	 * @param iTrigger trigger index
	//	 * @return trigger state
	//	 */
	//	public boolean getEnableTrigger(int iTrigger) {
	//		if (enableTrigger == null || enableTrigger.length <= iTrigger) {
	//			return false;
	//		}
	//		return enableTrigger[iTrigger];
	//	}
	//	
	//	/**
	//	 * Set the trigger state for a trigger
	//	 * @param iTrigger trigger index
	//	 * @param state trigger state
	//	 */
	//	public void setEnableTrigger(int iTrigger, boolean state) {
	//		if (recorderTriggerDatas == null || iTrigger >= recorderTriggerDatas.size()) {
	//			return;
	//		}
	//		recorderTriggerDatas.get(iTrigger).setEnabled(true);
	//	}
	/**
	 * Check that everything in the recorderTriggers list is also represented 
	 * in the triggerDataList. <p>
	 * Each recorder trigger can provide a set of default data, which is basically 
	 * what the programmer has put in to give an idea of suitable data budgets and 
	 * trigger conditions. These default parameters then get modified
	 * by the user to suit their own requirements. 
	 */
	protected void createTriggerDataList(ArrayList<RecorderTrigger> recorderTriggers) {
		for (RecorderTrigger rt:recorderTriggers) {
			if (findTriggerData(rt.getDefaultTriggerData().getTriggerName()) == null) {
				recorderTriggerDatas.add(rt.getDefaultTriggerData().clone());
			}
		}
	}

	/**
	 * Called before settings are saved to remove settings for any module no longer present. 
	 * @param recorderTriggers
	 */
	protected void cleanTriggerDataList(ArrayList<RecorderTrigger> recorderTriggers) {
		boolean[] present = new boolean[recorderTriggerDatas.size()];
		RecorderTriggerData rtData;
		for (RecorderTrigger rt:recorderTriggers) {
			rtData = findTriggerData(rt.getDefaultTriggerData().getTriggerName());
			if (rtData != null) {
				present[recorderTriggerDatas.indexOf(rtData)] = true;
			}
		}
		for (int i = present.length - 1; i >= 0; i--) {
			if (present[i] == false) {
				recorderTriggerDatas.remove(i);
			}
		}
	}

	/**
	 * Find the active trigger data for a trigger of a given name. 
	 * <p>If the trigger data cannot be found, add the default set. 
	 * @param recorderTrigger
	 * @return Active trigger data (started as the default, then got modified by the user)
	 */
	public RecorderTriggerData findTriggerData(RecorderTrigger recorderTrigger) {
		if (findTriggerData(recorderTrigger.getName()) == null) {
			recorderTriggerDatas.add(recorderTrigger.getDefaultTriggerData().clone());
		}
		return findTriggerData(recorderTrigger.getName());
	}

	/**
	 * find a set of trigger data by name. 
	 * @param triggerName trigger name
	 * @return Active trigger data. 
	 */
	public RecorderTriggerData findTriggerData(String triggerName) {
		for (RecorderTriggerData td:recorderTriggerDatas) {
			if (td.getTriggerName().equals(triggerName)) {
				return td;
			}
		}
		return null;
	}

	/**
	 * Get the largest (enabled) pre trigger time
	 * @return longest time in seconds. 
	 */
	public double getLongestHistory() {
		double t = 0;
		for (RecorderTriggerData td:recorderTriggerDatas) {
			if (td.isEnabled() == false) {
				continue;
			}
			t = Math.max(t, td.getSecondsBeforeTrigger());
		}
		return t;
	}

	/**
	 * If PAMGAURD stops and starts, automatically put the recorder back into
	 * the same mode it was in when acquisition stopped. <br>
	 * This works with startStatus to optionally automatically start the recorder. It's left in 
	 * for config compatibility reasons, most of the work now being done by startStatus. 
	 */
	boolean autoStart = false;

	/**
	 * Memorised status for autoStart (i.e. the last buttone that got pressed).
	 * @see autoStart
	 */
	int oldStatus;
	
	/**
	 * New start status option to make it easier to automatically start recording.<br>Tells 
	 * PAMGuard whether or not to start the recorder when PAMGuard starts.  
	 */
	public int startStatus = 0;


	@Override
	public RecorderSettings clone(){
		try {
			RecorderSettings newSettings = (RecorderSettings) super.clone();
			if (newSettings.recorderTriggerDatas == null) {
				newSettings.recorderTriggerDatas = new ArrayList<RecorderTriggerData>();
			}
			if (badOutputFolder(newSettings.outputFolder)) {
				newSettings.outputFolder = getDefaultOutputFolder();
			}
			return newSettings;
		}
		catch (CloneNotSupportedException Ex) {
			return null;
		}
	}

	/**
	 * Get a default output folder, ideally in My Documents. 
	 * @return default output folder. 
	 */
	private String getDefaultOutputFolder() {
		String defLoc = PamFolders.getDefaultProjectFolder();
		defLoc += File.separator + "PAMRecordings";
		return defLoc;
	}

	/**
	 * Check the output folder isn't null or the program files folder 
	 * since on Windows there isn't write access to these folders. 
	 * @param outputFolder
	 * @return
	 */
	private boolean badOutputFolder(String folderName) {
		/*
		 *  it's possible that it's writing to it's defult folder from earlier
		 *  versions which is 
		 *	outputFolder = new File(".").getCanonicalPath(); so check against 
		 *	this first, to help old configurations even though we've altered
		 *	the default in the constructor.  
		 */
		if (folderName == null) {
			return true;
		}

		String oldDefault = null;
		try {
			oldDefault = new File(".").getCanonicalPath();
		} catch (IOException e) {
		}
		if (oldDefault != null && oldDefault.equals(folderName)) {
			return true; // yes, it's the bad old defaul tfolder !
		}

		// check to see if it's in a read only folder of any kind. 
		File outFolder = new File(folderName);
		if (outFolder.exists() && outFolder.canWrite() == false) {
			return true;
		}
		
		// crude check to see if it's anywhere near the program files folder
		if (folderName.startsWith("C:\\Program Files")) {
			return true;
		}


		return false;
	}

	/**
	 * Since AudioFileFormat.Type is not serialized, fileType
	 * is stored as a sting. The getter therefore needs to search
	 * available file types and return the appropriate one.
	 * @return Format type for the audio file
	 * @see javax.sound.sampled.AudioFormat
	 */
	public AudioFileFormat.Type getFileType() {
		
		// if the decimus flag is set, first check for X3 file type
		if (SMRUEnable.isEnableDecimus()) {
			if (RecorderControl.X3.toString().equals(fileType)) {
				return RecorderControl.X3;
			}
		}

		AudioFileFormat.Type types[] = AudioSystem.getAudioFileTypes();
		for (int i = 0; i < types.length; i++) {
			if (types[i].toString().equals(fileType)) {
				return types[i];
			}
		}
		return null;
	}

	//transient AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
	public void setFileType(AudioFileFormat.Type fileType) {
		this.fileType = fileType.toString();
	}

	/**
	 * Find a trigger data object with the same name and replace it. 
	 * @param newData
	 */
	public void replaceTriggerData(RecorderTriggerData newData) {
		RecorderTriggerData td = findTriggerData(newData.getTriggerName());
		int ind = recorderTriggerDatas.indexOf(td);
		recorderTriggerDatas.remove(ind);
		recorderTriggerDatas.add(ind, newData);
	}

	/**
	 * get the channel map, but tell it what channels are available !
	 * @param availableChannels available cahnnels (channel map of parent process)
	 * @return channel bitmap
	 */
	public synchronized int getChannelBitmap(int availableChannels) {
		return (channelBitmap & availableChannels);
	}
	
	/**
	 * @return The number of channels to be recorded. 
	 */
	public int getNumChannels() {
		return PamUtils.getNumChannels(channelBitmap);
	}
	
	/**
	 * @param channelBitmap the channelBitmap to set
	 */
	public synchronized void setChannelBitmap(int channelBitmap) {
		this.channelBitmap = channelBitmap;
	}

	/**
	 * Set the bitmap for a given channel. 
	 * @param iChannel channel number
	 * @param state on or of (true or false)
	 * @return channel bitmap
	 */
	public int setChannelBitmap(int iChannel, boolean state) {
		if (state) {
			channelBitmap |= (1<<iChannel);
		}
		else {
			channelBitmap &= ~(1<<iChannel);
		}

		return channelBitmap;
	}

	/**
	 * Get the state of a single channel. 
	 * @param availableChannels available channels
	 * @param iChannel channel number
	 * @return true or false.
	 */
	public boolean getChannelBitmap(int availableChannels, int iChannel) {
		channelBitmap &= availableChannels;
		return ((channelBitmap & (1<<iChannel)) != 0);
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("channelBitmap");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return channelBitmap;
				}
			});
			field = this.getClass().getDeclaredField("bitDepth");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return bitDepth;
				}
			});
			field = this.getClass().getDeclaredField("notRoundFileStarts");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return notRoundFileStarts;
				}
			});
			field = this.getClass().getDeclaredField("limitLengthSeconds");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return limitLengthSeconds;
				}
			});
			field = this.getClass().getDeclaredField("limitLengthMegaBytes");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return limitLengthMegaBytes;
				}
			});
			field = this.getClass().getDeclaredField("datedSubFolders");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return datedSubFolders;
				}
			});
			field = this.getClass().getDeclaredField("recorderTriggerDatas");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return recorderTriggerDatas;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			PamParameterData param = ps.findParameterData("fileType");
			param.setShortName(fileType);	// add in the actual file type, because getter returns an AudioFileFormat object
		}
		catch (FieldNotFoundException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
