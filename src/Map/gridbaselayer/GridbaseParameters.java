package Map.gridbaselayer;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class GridbaseParameters implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public String netCDFFile;
//	= "C:\\ALFA2019\\GEBCO_2019_-159.8938_64.9599_-126.546_49.8462.nc"; // huge
//	public String netCDFFile = "C:\\ALFA2019\\GEBCO_2019_-136.75_58.3687_-134.5208_55.9937.nc";

	@Override
	public GridbaseParameters clone() {
		try {
			return (GridbaseParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
	

}
