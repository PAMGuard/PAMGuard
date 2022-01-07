package loggerForms.propertyInfos;

import java.awt.Color;

import loggerForms.HeadingTypes;
import loggerForms.PropertyDescription;
import loggerForms.RangeUnitTypes;
import loggerForms.controlDescriptions.ControlDescription;

public class HEADINGinfo {
	private ControlDescription relatedControl;
	private int controlIndex;
	private RangeUnitTypes unitType;
	private int arrowLength;
	private int arrowHeadSize;
	private HeadingTypes type;
	private boolean fillHead;
	private Color colour;
	
	public HEADINGinfo(ControlDescription relatedControl, int controlIndex,RangeUnitTypes unitType, int arrowLength,
			int arrowHeadSize,HeadingTypes type,boolean fillHead, Color colour){
		
		this.relatedControl=relatedControl;
		this.controlIndex = controlIndex;
		this.unitType=unitType;
		this.arrowLength=arrowLength;
		this.arrowHeadSize=arrowHeadSize;
		this.type=type;
		this.fillHead=fillHead;
		this.colour=colour;
	}

	/**
	 * @return the relatedControl
	 */
	public ControlDescription getRelatedControl() {
		return relatedControl;
	}

	/**
	 * @return the unitType
	 */
	public RangeUnitTypes getUnitType() {
		return unitType;
	}

	/**
	 * @return the arrowLength
	 */
	public int getArrowLength() {
		return arrowLength;
	}

	/**
	 * @return the arrowHeadSize
	 */
	public int getArrowHeadSize() {
		return arrowHeadSize;
	}

	/**
	 * @return the type
	 */
	public HeadingTypes getType() {
		return type;
	}

	/**
	 * @return the fillHead
	 */
	public boolean isFillHead() {
		return fillHead;
	}

	/**
	 * @return the colour
	 */
	public Color getColour() {
		return colour;
	}

	public int getControlIndex() {
		return controlIndex;
	}
	
	
}
