package landMarks;

import java.io.Serializable;

import PamController.masterReference.MasterReferencePoint;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.LatLong;
import PamView.PamSymbol;

public class LandmarkData extends Object implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;

	public LatLong latLong;
	
	public double height;
	
	public PamSymbol symbol;
	
	public String name;

	@Override
	protected LandmarkData clone() {
		try {
			return (LandmarkData) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public double rangeToReference() {
		LatLong refLatLong = MasterReferencePoint.getLatLong();
		if (refLatLong == null) {
			return 0;
		}
		return latLong.distanceToMetres(refLatLong);
	}
	
	public double bearingToReference() {
		LatLong refLatLong = MasterReferencePoint.getLatLong();
		if (refLatLong == null) {
			return 0;
		}
		return latLong.bearingTo(refLatLong);		
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
