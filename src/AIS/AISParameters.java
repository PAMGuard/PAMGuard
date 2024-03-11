package AIS;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class AISParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 0;
	
	protected String nmeaSource;
	
	/**
	 * Only record AIS data within 
	 * maxRange_kn of the vessel
	 */
	public boolean limitRange = false;
	
	/**
	 * Maximum range for AIS data. Data further off than 
	 * this will be ignored.
	 */
	public double maxRange_km = 10.;
	
	public boolean showTail;
	/**
	 * Tail length in minutes. 
	 */
	public int tailLength = 30;
	
	public boolean showPredictionArrow;
	
	/**
	 * Prediction length in seconds
	 */
	public int predictionLength = 600;
	
	@Override
	protected AISParameters clone()  {
		try {
			return (AISParameters) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("nmeaSource");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return nmeaSource;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
