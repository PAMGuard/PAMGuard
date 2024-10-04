package depthReadout;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import mcc.mccjna.MCCConstants;

public class MccDepthParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;
		
	/**
	 * board index, not MCC Number. 
	 */
	public int iBoard;
	
	public int range = MCCConstants.BIP10VOLTS;
	
	MccSensorParameters[] mccSensorParameters;
	
	public MccSensorParameters getSensorParameters(int iSensor) {
		if (mccSensorParameters == null || iSensor >= mccSensorParameters.length) {
			return null;
		}
		return mccSensorParameters[iSensor];
	}
	
	public class MccSensorParameters implements Serializable, Cloneable, ManagedParameters {

		static public final long serialVersionUID = 0;
		
		public int iChan;
		
		/**
		 * Deth = scaleA * voltage + scaleB;
		 */		
		public double scaleA = 1;
		
		public double scaleB = 0;
		
		@Override
		public PamParameterSet getParameterSet() {
			PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
			return ps;
		}
	}

	@Override
	public MccDepthParameters clone() {
		try {
			return (MccDepthParameters) super.clone();
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
			Field field = this.getClass().getDeclaredField("mccSensorParameters");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return mccSensorParameters;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	
}
