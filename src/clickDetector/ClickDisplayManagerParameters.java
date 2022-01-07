package clickDetector;

import java.io.Serializable;

import PamController.PamController;

public class ClickDisplayManagerParameters implements Cloneable, Serializable {

	static public final long serialVersionUID = 2;
	
	static private final int NMODES = 6;
	
	private int nBTDisplays = 1;
	
	private int nWaveDisplays = 1;
	
	private int nSpectrumDisplays = 1;
	
	private int nTriggerDisplays = 1;
	
	private int nWignerDisplays = 1;
	
	private int nConcatSpecDisplays = 1;
	
	private int lastMode;
	
	private boolean initialised = false;
	


	@Override
	public ClickDisplayManagerParameters clone() {
		try {
			ClickDisplayManagerParameters np =  (ClickDisplayManagerParameters) super.clone();
			return np;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

}
