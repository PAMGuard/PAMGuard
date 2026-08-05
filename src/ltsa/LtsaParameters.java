package ltsa;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class LtsaParameters implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public String dataSource;
	
	public int channelMap;
	
	/**
	 * Interval for basic measurements. 
	 */
	public int intervalSeconds = 60;
	
	/**
	 * Factor for longer term averages. 
	 */
	public int longerFactor = 10;

	@Override
	protected LtsaParameters clone() {
		try {
			LtsaParameters params = (LtsaParameters) super.clone();
			params.intervalSeconds = this.intervalSeconds;
			params.longerFactor = this.longerFactor;
			params.dataSource = this.dataSource;
			params.channelMap = this.channelMap;
			return params;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}
}
