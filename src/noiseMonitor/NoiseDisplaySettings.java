package noiseMonitor;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

public class NoiseDisplaySettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	/*
	 * display parameters
	 */
	long displayLengthSeconds = 900;
	
	double levelMin = 70;
	
	double levelMax = 170;
	
	boolean[] selectedChannels;
	
	int selectedStats = 0x1;
	
	private boolean[] selectedData;

	public boolean autoScale = false;
	
	public boolean showGrid = false;
	
	public boolean showKey = false;
	
	/**
	 * Band option - 0 = band energy, 1 = convert to spectrum level. 
	 */
	public int bandOption;
	
	public static final int BAND_ENERGY = 0;
	public static final int SPECTRUM_LEVEL = 1;
	
	public boolean isSelectData(int iBand) {
		if (selectedData == null || selectedData.length <= iBand) {
			return false;
		}
		return selectedData[iBand];
	}
	
	public void setSelectData(int iBand, boolean state) {
		if (selectedData == null) {
			selectedData = new boolean[iBand+1];
		}
		else if (selectedData.length <= iBand) {
			selectedData = Arrays.copyOf(selectedData, iBand+1);
		}
		selectedData[iBand] = state;
	}


	@Override
	protected NoiseDisplaySettings clone() {
		try {
			return (NoiseDisplaySettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("displayLengthSeconds");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return displayLengthSeconds;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("levelMin");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return levelMin;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("levelMax");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return levelMax;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("selectedChannels");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return selectedChannels;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("selectedStats");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return selectedStats;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("selectedData");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return selectedData;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}
}
