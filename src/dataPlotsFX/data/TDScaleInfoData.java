package dataPlotsFX.data;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamguardMVC.PamConstants;

/**
 * Data for the TDScaleInfo which can be serialised and saved. 
 * @author Jamie Macaulay
 *
 */
public class TDScaleInfoData implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	/**
	 * The minimum value 
	 */
	public double minVal;

	/**
	 * The maxiumum value
	 */
	public double maxVal;
	
	/**
	 * Unit divisor for the display of units on graphs etc. 
	 */
	protected double unitDivisor=1; 

	/**
	 * String representation of the divisor, typically k (kilo) M (mega) etc.
	 */
	protected String divisorString="";

	/**
	 * Automatically calculate a divisor value for the data e.g. 1000 -> k 1000000 - M 
	 */
	protected boolean autoDivisor=true;
	
	/**
	 * The channels/sequences associated with different plots. If 0 then plot can plot all channels. If any other number then a plot can only plot those channels. 
	 * Most of the time this will be unused, however for some case, e.g. spectrogram overlays it is necessary. as
	 * plotChannels[0] refers to channels for plot pane 0, plotChannels[1] for channels in plot pane 2 etc. It is assumed the user will not have a need for one display showing more than 
	 * PamConstants.MAX_CHANNELS channels of data.  Note that, depending on the source, this may hold either channels or sequence numbers.  It is left to the user to be careful not
	 * to mix the two when selecting which PamDataBlocks to display on the same graph
	 */
	private int[] plotChannels=new int[PamConstants.MAX_CHANNELS];
	
	/**
	 * The visible channels. True to show a channel on the map and false to not show. The visiblePlotChannels is the same size and represents the same channels 
	 * {@link plotChannels}
	 */
	private boolean[] visiblePlotChannels = new boolean[PamConstants.MAX_CHANNELS];

	/**
	 * Set all plot channels visibility
	 * @param visible
	 */
	protected void setAllVisible(boolean visible){
		for (int i=0; i<visiblePlotChannels.length ; i++){
			visiblePlotChannels[i]=visible; 
		}
	}

	/**
	 * @return the plotChannels array
	 */
	public int[] getPlotChannels() {
		return plotChannels;
	}

	/**
	 * @param plotChannels the plotChannels to set
	 */
	public void setPlotChannels(int[] plotChannels) {
		this.plotChannels = plotChannels;
	}

	/**
	 * @param visiblePlotChannels the visiblePlotChannels to set
	 */
	public void setVisiblePlotChannels(boolean[] visiblePlotChannels) {
		this.visiblePlotChannels = visiblePlotChannels;
	}

	/**
	 * @return the visiblePlotChannels
	 */
	public boolean[] getVisiblePlotChannels() {
		return visiblePlotChannels;
	}
	
	/**
	 * Get a string representation of the data: 
	 * @return a string representation of the data. 
	 */
	public String getStringData() {
		return String.format(" min: %.2f max %.2f", minVal, maxVal); 
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("autoDivisor");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return autoDivisor;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("divisorString");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return divisorString;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("unitDivisor");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return unitDivisor;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
