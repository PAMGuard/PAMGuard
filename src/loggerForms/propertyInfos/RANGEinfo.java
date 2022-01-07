package loggerForms.propertyInfos;

import PamUtils.LatLong;
import loggerForms.PropertyDescription;
import loggerForms.RangeTypes;
import loggerForms.RangeUnitTypes;
import loggerForms.controlDescriptions.ControlDescription;

public class RANGEinfo {
	private ControlDescription relatedControl;
	private RangeUnitTypes unitType;
	private RangeTypes type;
	private int fixedLength;
	private int controlIndex;
	
	
	public RANGEinfo(ControlDescription relatedControl,int controlIndex, RangeUnitTypes unitType,
			RangeTypes type,int fixedLength){
		this.relatedControl=relatedControl;
		this.controlIndex = controlIndex;
		this.unitType=unitType;
		this.type=type;
		this.fixedLength=fixedLength;
	}


	/**
	 * @return the relatedControl
	 */
	public ControlDescription getRelatedControl() {
		return relatedControl;
	}



	/**
	 * @return the fixedLength
	 */
	public int getFixedLength() {
		return fixedLength;
	}


	public int getControlIndex() {
		return controlIndex;
	}


	/**
	 * Get the correct range in metres using 
	 * either the given value of the fixed value.  
	 * @param range in form units (m, km, nmi)
	 * @return range in metres
	 */
	public Double getRangeMetres(Double range) {
		if (range == null && type == RangeTypes.FIXED) {
			range = new Double(fixedLength);
		}
		if (range == null) {
			return null;
		}
		switch(unitType) {
		case m:
			return range;
		case km:
			return range * 1000;
		case nmi:
			return range * LatLong.MetersPerMile;		
		}
		return null;
	}


	public RangeUnitTypes getUnitType() {
		return unitType;
	}


	public RangeTypes getType() {
		return type;
	}
	
}
