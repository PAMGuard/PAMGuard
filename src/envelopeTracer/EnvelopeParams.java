package envelopeTracer;

import java.io.Serializable;

import Filters.FilterParams;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class EnvelopeParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public String dataSourceName;
	
	public int channelMap = 3;
	
	public float outputSampleRate = 48000;
	
	public boolean logScale;
	
	public FilterParams filterSelect = new FilterParams();
	
	public FilterParams postFilterParams = new FilterParams();

	@Override
	protected EnvelopeParams clone() {
		try {
			return (EnvelopeParams) super.clone();
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
