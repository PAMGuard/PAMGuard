package ArrayAccelerometer;

import java.io.Serializable;
import java.util.Arrays;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import mcc.MccJniInterface;
import mcc.mccjna.MCCConstants;

public class ArrayAccelParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public static final int NDIMENSIONS = 3;
	
	public static final String[] DIMENSIONNAME = {"X", "Y", "Z"};
	
	public static final double defaultZero = 1.5;
	public static final double defaultScale = 0.1;

	public double[] zeroVolts = new double[NDIMENSIONS];
	public double[] voltsPerG = new double[NDIMENSIONS];
	/**
	 * Really the board index - need to handle this in the code. 
	 */
	public int boardNumber = 0;
	public int boardRange = MCCConstants.BIP2VOLTS;
	public double readInterval = 1.0;
	public int[] dimChannel = new int[NDIMENSIONS];
	public int streamerIndex = 0;
	/**
	 * Roll offset is added to the measured roll
	 */
	public double rollOffset = 0.0;
	/**
	 * Tilt offset is added to the measured roll. 
	 */
	public double pitchOffset = 0.0;
	
	
	public ArrayAccelParams() {
		for (int i = 0; i < NDIMENSIONS; i++) {
			zeroVolts[i] = defaultZero;
			voltsPerG[i] = defaultScale;
			dimChannel[i] = i;
		}
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ArrayAccelParams clone()  {
		try {
			ArrayAccelParams np =  (ArrayAccelParams) super.clone();
			np.voltsPerG = voltsPerG.clone();
			np.zeroVolts = zeroVolts.clone();
			return np;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
