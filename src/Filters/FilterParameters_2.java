package Filters;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * FilterPArameters for use when the filter is on it's own (within a FilterController)
 * rather than combined into some other detector. 
 */
public class FilterParameters_2 implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1;
	
	public String rawDataSource;
	
	public int channelBitmap;
	
	public FilterParams filterParams = new FilterParams();
	
	@Override
	public FilterParameters_2 clone() {
		try{
//			filterParams = filterParams.clone();
			if (filterParams == null) filterParams = new FilterParams();
			FilterParameters_2 newParams = (FilterParameters_2) super.clone();
			newParams.filterParams = this.filterParams.clone();
			return newParams;
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
