package fftManager;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class FFTDataDisplayOptions implements Serializable, Cloneable, ManagedParameters {
	
	static final long serialVersionUID = 1;
	
	double maxVal = 110;
	double minVal = 70;
	int smoothingFactor = 10;
	boolean useSpecValues = true;
//	//ArrayList<Integer> channelsToPlot = new ArrayList<Integer>();
//	ArrayList<Integer> plottablechannels = new ArrayList<Integer>();
//	ArrayList<Boolean> plottedchannels = new ArrayList<Boolean>();
//	int plottableChannels;
	
	/**
	 * Channel (or Sequence) map to plot
	 */
	int plottedChannels = 0xFFFF;
	
//	int getNumChannels() {
//		return PamUtils.getNumChannels(channelBitmap);
//	}
	//int [] channelId = new int[numChannels];
	//boolean [] plotChannel = new boolean [numChannels];
	
	//PamConstants.MAX_CHANNELS
	
	@Override
	protected FFTDataDisplayOptions clone() {
		try {
			return (FFTDataDisplayOptions) super.clone();
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("maxVal");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return maxVal;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("minVal");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return minVal;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("smoothingFactor");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return smoothingFactor;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("useSpecValues");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return useSpecValues;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("plottedChannels");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return plottedChannels;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
