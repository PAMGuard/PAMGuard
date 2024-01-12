package matchedTemplateClassifer;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataTransforms;

/**
 * Stores information for a click template
 * @author Jamie Macaulay
 *
 */
public class MatchTemplate implements RawDataHolder, Serializable, Cloneable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public float sR;
	
	public double[] waveform;
	
	public String name; 

	public MatchTemplate(String name, double[] waveform, float sR){
		this.name=name; 
		this.waveform=waveform;
		this.sR= sR; 
	}

	@Override
	public MatchTemplate clone() {
		MatchTemplate newParams = null;
		try {
			newParams = (MatchTemplate) super.clone();
		}
		catch(CloneNotSupportedException Ex) {
			Ex.printStackTrace(); 
			return null;
		}
		return newParams;
	}
	
	@Override
	 public String toString() {
        return super.toString() +  " sR: " + sR + " waveform length: " + waveform == null ? "null waveform" : waveform.length  + " " + name;
    }
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

	@Override
	public double[][] getWaveData() {
		return new double[][] {waveform};
	}

	@Override
	public RawDataTransforms getDataTransforms() {
		// TODO Auto-generated method stub
		return null;
	}
}