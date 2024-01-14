package clickDetector;

import java.io.Serializable;
import java.lang.reflect.Field;

import clickDetector.IDI_Display.IDIHistogramImage;

import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class ClickDisplayManagerParameters2 implements Cloneable, Serializable, ManagedParameters {

	static public final long serialVersionUID = 2;
	
	static private final int NMODES = 6;
	
	private int[] nBTDisplays = new int[NMODES];
	
	private int[] nWaveDisplays = new int[NMODES];
	
	private int[] nSpectrumDisplays = new int[NMODES];
	
	private int[] nTriggerDisplays = new int[NMODES];
	
	private int[] nWignerDisplays = new int[NMODES];
	
	private int[] nConcatSpecDisplays = new int[NMODES];
	
	private int[] nIDIHistogramDisplays = new int[NMODES];
	
	private int lastMode;
	
	private boolean initialised = false;
	
	public ClickDisplayManagerParameters2() {
		setDefaults();
	}

	/**
	 * @return the nBTDisplays
	 */
	public int getnBTDisplays() {
		int mode = PamController.getInstance().getRunMode();
		if (mode >= NMODES) mode = 0;
		if (mode < 0) mode = 0;
		if (nBTDisplays == null) {
			return 0;
		}
		return nBTDisplays[mode];
	}

	/**
	 * @return the nWaveDisplaye
	 */
	public int getnWaveDisplays() {
		int mode = PamController.getInstance().getRunMode();
		if (mode >= NMODES) mode = 0;
		if (mode < 0) mode = 0;
		if (nWaveDisplays == null) {
			return 0;
		}
		return nWaveDisplays[mode];
	}

	/**
	 * @return the nSpectrumDisplays
	 */
	public int getnSpectrumDisplays() {
		int mode = PamController.getInstance().getRunMode();
		if (mode >= NMODES) mode = 0;
		if (mode < 0) mode = 0;
		if (nSpectrumDisplays == null) {
			return 0;
		}
		return nSpectrumDisplays[mode];
	}

	/**
	 * @return the nTriggerDisplays
	 */
	public int getnTriggerDisplays() {
		int mode = PamController.getInstance().getRunMode();
		if (mode >= NMODES) mode = 0;
		if (mode < 0) mode = 0;
		if (nTriggerDisplays == null) {
			return 0;
		}
		return nTriggerDisplays[mode];
	}

	/**
	 * @return the nWignerDisplays
	 */
	public int getnWignerDisplays() {
		int mode = PamController.getInstance().getRunMode();
		if (mode >= NMODES) mode = 0;
		if (mode < 0) mode = 0;
		if (nWignerDisplays == null) {
			return 0;
		}
		return nWignerDisplays[mode];
	}

	/**
	 * @return the nConcatSpecDisplays
	 */
	public int getnConcatSpecDisplays() {
		int mode = PamController.getInstance().getRunMode();
		if (mode >= NMODES) mode = 0;
		if (mode < 0) mode = 0;
		if (nConcatSpecDisplays == null) {
			return 0;
		}
		return nConcatSpecDisplays[mode];
	}

	/**
	 * @return the nConcatSpecDisplays
	 */
	public int getnIDIHistogramDisplays() {
		int mode = PamController.getInstance().getRunMode();
		if (mode >= NMODES) mode = 0;
		if (mode < 0) mode = 0;
		if (nIDIHistogramDisplays == null) {
			return 0;
		}
		return nIDIHistogramDisplays[mode];
	}
	void setDefaults() {
		int mode = PamController.getInstance().getRunMode();
		for (int i = 0; i < NMODES; i++) {
			nBTDisplays[i] = 1;
			nWaveDisplays[i] = 1;
			nSpectrumDisplays[i] = 1;
			nTriggerDisplays[i] = (mode == PamController.RUN_NORMAL ? 1: 0);
			nWignerDisplays[i] = 0;
			nConcatSpecDisplays[i] = 0;
			nIDIHistogramDisplays[i] = 0;
		}
		nWignerDisplays[PamController.RUN_PAMVIEW] = 1;
		nWignerDisplays[PamController.RUN_NETWORKRECEIVER] = 1;
		initialised = true;
	}

	@Override
	public ClickDisplayManagerParameters2 clone() {
		try {
			ClickDisplayManagerParameters2 np =  (ClickDisplayManagerParameters2) super.clone();
			if (np.initialised == false) {
				setDefaults();
			}
			return np;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	public void countEverything(ClickDisplayManager clickDisplayManager) {
		lastMode = PamController.getInstance().getRunMode();
		if (lastMode >= NMODES) lastMode = 0;
		
		if (nBTDisplays==null)			nBTDisplays=new int[NMODES];
		if (nWaveDisplays==null)		nWaveDisplays=new int[NMODES];
		if (nSpectrumDisplays==null)	nSpectrumDisplays=new int[NMODES];
		if (nTriggerDisplays==null)		nTriggerDisplays=new int[NMODES];
		if (nWignerDisplays==null)		nWignerDisplays=new int[NMODES];
		if (nConcatSpecDisplays==null)	nConcatSpecDisplays=new int[NMODES];
		if (nIDIHistogramDisplays==null)nIDIHistogramDisplays=new int[NMODES];
		
		nBTDisplays[lastMode] = clickDisplayManager.countDisplays(ClickBTDisplay.class);
		nWaveDisplays[lastMode] = clickDisplayManager.countDisplays(ClickWaveform.class);
		nSpectrumDisplays[lastMode] = clickDisplayManager.countDisplays(ClickSpectrum.class);
		nTriggerDisplays[lastMode] = clickDisplayManager.countDisplays(ClickTrigger.class);
		nWignerDisplays[lastMode] = clickDisplayManager.countDisplays(WignerPlot.class);
		nConcatSpecDisplays[lastMode] = clickDisplayManager.countDisplays(ConcatenatedSpectrogram.class);
		nIDIHistogramDisplays[lastMode] = clickDisplayManager.countDisplays(IDI_Display.class);
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("initialised");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return initialised;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("lastMode");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return lastMode;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
