package clipgenerator;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Settings for a clip generator - which can be triggered by any AcousticDataUnit. 
 * <p>
 * 
 * @author Doug Gillespie
 *
 */
public class ClipGenSetting implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	/*
	 * Include only the detected channels in the clip
	 */
	public static final int DETECTION_CHANNELS_ONLY = 0;
	/*
	 * Include only the first of the detected channels in the clip
	 */
	public static final int FIRST_DETECTION_CHANNEL_ONLY = 1;
	
	/*
	 * include all channels in the clip
	 */
	public static final int ALL_CHANNELS = 2;

	/**
	 * Types of channel selection. 
	 */
	public static final String[] channelSelTypes = {"Detection channels only", "First detection channel only", "All channels"};

	/**
	 * Data name of the trigger data block. 
	 */
	public String dataName;
	
	/**
	 * Enabled
	 */
	public boolean enable = true;

	/**
	 * Seconds before start of trigger
	 */
	public double preSeconds = 0;

	/**
	 * Seconds after end of trigger. 
	 */
	public double postSeconds = 0;

	/**
	 * Channel selection, all, first, one, etc. 
	 */
	public int channelSelection = DETECTION_CHANNELS_ONLY;
	
	/**
	 * prefix for the clip (ahead of the date string). 
	 * Can be null in which case the default is used. 
	 */
	public String clipPrefix;
	
	/**
	 * If false, then record absolutely everything. 
	 */
	public boolean useDataBudget = true;

	/**
	 * Data budget in kilobytes. 
	 */
	public int dataBudget = 10*1024;

	/**
	 * Budget period in hours. 
	 */
	public double budgetPeriodHours = 24.;

	/**
	 * Length of line to display on map in metres. 
	 */
	public Double mapLineLength;
	private boolean hadMapLine = false;

	/**
	 * @param dataName
	 */
	public ClipGenSetting(String dataName) {
		super();
		this.dataName = dataName;
	}

	@Override
	protected ClipGenSetting clone()  {
		try {
			ClipGenSetting newObj = (ClipGenSetting) super.clone();
			// force map line length to 1000m for old objects. 
			if (!newObj.hadMapLine) {
				newObj.hadMapLine = true;
				newObj.mapLineLength = 1000.;
			}
			return newObj;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("hadMapLine");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return hadMapLine;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
