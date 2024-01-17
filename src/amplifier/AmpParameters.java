package amplifier;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamguardMVC.PamConstants;


public class AmpParameters implements Cloneable, Serializable, ManagedParameters {

	static public final long serialVersionUID = 0;
	
	int rawDataSource;
	
	/**
	 * gain is stored as a simple facter (NOT dB) for speed of use. 
	 * The dialog where they are set may well convert to dB values. 
	 */
	public double[] gain = new double[PamConstants.MAX_CHANNELS];

	public AmpParameters () {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			gain[i] = 1.0;
		}
	}
	
	@Override
	public AmpParameters clone() {

		try {
			return (AmpParameters) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("rawDataSource");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return rawDataSource;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}


}
