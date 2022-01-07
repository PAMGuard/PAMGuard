package clipgenerator.clipDisplay;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.ColourArray.ColourArrayType;

public class ClipDisplayParameters implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/*
	 * Values that get used for controlling the display
	 */
	public double amlitudeMinVal = 50;
	public double amplitudeRangeVal = 70;
	public double imageVScale = 1, imageHScale = 1;
	private int logFFTLength = 9;
	private ColourArrayType colourMap = ColourArrayType.GREY;
	public boolean showTriggerOverlay = true;
	public double frequencyScale = 1.0; // value <= 1. 1 is all data up to niquist. 
	int maxClips = 100;
	int maxMinutes = 10;
	int newClipOrder = 0; // Location of new clips in the clip display. 0 is first (top-left). -1 is last (bottom-right);
	
	boolean showControlPanel = false;

	/**
	 * Values used to set max and min values for the controls. 
	 */
	double amplitudeMinMin = 0;
	double amplitudeMinMax = 200;
	double amplitudeMinStep = 10;
	double amplitudeRangeMin = 40;
	double amplitudeRangeMax = 200;
	double amplitudeRangeStep = 10;

	@Override
	protected ClipDisplayParameters clone() {
		try {
			return (ClipDisplayParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ColourArrayType getColourMap() {
		if (colourMap == null) {
			colourMap = ColourArrayType.GREY;
		}
		return colourMap;
	}

	public void setColourMap(ColourArrayType colourMap) {
		this.colourMap = colourMap;
	}

	/**
	 * @return the logFFTLength
	 */
	public int getLogFFTLength() {
		return logFFTLength;
	}

	public void setLogFFTLength(int logFFTLength) {
		this.logFFTLength = logFFTLength;
	}

	/**
	 * Getter and setter to convert boolean value from the 
	 * DispalyControlPanel checkbox into a meaningful 
	 * layout order (0,-1). and vice versa.
	 * @return
	 */
	public boolean getNewClipOrder() {
		boolean newClipsLast;
		newClipsLast = (newClipOrder==-1) ? true : false;
		return newClipsLast;
	}
	
	public void setNewClipOrder(boolean newClipsLast){
		newClipOrder = newClipsLast ? -1 : 0;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("maxClips");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return maxClips;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("maxMinutes");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return maxMinutes;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("newClipOrder");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return newClipOrder;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("showControlPanel");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return showControlPanel;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("amplitudeMinMin");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return amplitudeMinMin;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("amplitudeMinMax");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return amplitudeMinMax;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("amplitudeMinStep");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return amplitudeMinStep;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("amplitudeRangeMin");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return amplitudeRangeMin;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("amplitudeRangeMax");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return amplitudeRangeMax;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("amplitudeRangeStep");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return amplitudeRangeStep;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
