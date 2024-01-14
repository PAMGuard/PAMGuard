package noiseOneBand;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class OneBandDisplayParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 2L;
	
	public double minAmplitude = 0;
	
	public double maxAmplitude = 100;
	
	public long timeRange = 600;
	
	public boolean autoScale = true;
	
	public int symbolSize = 10;
	
	public boolean drawLine;
	
	public int showWhat = 0xF;
	
	public boolean showGrid;
	
	public boolean colourByChannel = true;
	
	private Integer displayChannels = 0xFFFF;

	@Override
	protected OneBandDisplayParams clone(){
		try {
			OneBandDisplayParams newParams = (OneBandDisplayParams) super.clone();
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the displayChannels
	 */
	public int getDisplayChannels(int availableChannels) {
		if (displayChannels == null) {
			displayChannels = availableChannels;
		}
		return (displayChannels & availableChannels);
	}

	/**
	 * @param displayChannels the displayChannels to set
	 */
	public void setDisplayChannels(int displayChannels) {
		this.displayChannels = displayChannels;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("displayChannels");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return displayChannels;
				}
			});
		} catch (NoSuchFieldException | SecurityException e2) {
			e2.printStackTrace();
		}
		return ps;
	}

}
