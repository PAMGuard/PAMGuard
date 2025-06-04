package noiseMonitor;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import noiseBandMonitor.BandType;

public class NoiseMeasurementBand implements Serializable, Cloneable, ManagedParameters {

	private static final int TYPE_USER = 0;
	private static final int TYPE_THIRDOCTAVE = 1;
	private static final int TYPE_DECIDECADE = 2;
	
	public static final long serialVersionUID = 1L;

	public double f1, f2;
	
	public String name;
	
	private boolean canEdit;
	
	private boolean canRemove;
	
	private int type;

	private BandType bandType;

//	public int getType() {
//		return type;
//	}

//
//	public void setType(int type) {
//		this.type = type;
//	}


	public NoiseMeasurementBand(BandType type, double f1, double f2) {
		super();
		this.bandType = type;
		this.name = getAutoName(type);
		this.f1 = f1;
		this.f2 = f2;
		autoEnable();
	}


	public NoiseMeasurementBand(BandType invalid) {
		this.bandType = invalid;
		autoEnable();
	}
	
	private void autoEnable() {
		canEdit = canRemove = (bandType==null);
	}

	@Override
	protected NoiseMeasurementBand clone() {
		try {
			return (NoiseMeasurementBand) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param canEdit the canEdit to set
	 */
	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	/**
	 * @return the canEdit
	 */
	public boolean isCanEdit() {
		return canEdit;
	}

	/**
	 * @param canRemove the canRemove to set
	 */
	public void setCanRemove(boolean canRemove) {
		this.canRemove = canRemove;
	}

	/**
	 * @return the canRemove
	 */
	public boolean isCanRemove() {
		return canRemove;
	}
	
	/**
	 * Have to override the names in this from the 
	 * defaults in the band type enum for compatibility 
	 * with old database logging. 
	 * @param type 
	 * @return
	 */
	private String getAutoName(BandType type) {
		switch (type) {
		case DECADE:
			return "Decade";
		case DECIDECADE:
			return "DeciDecade";
		case OCTAVE:
			return "Octave";
		case THIRDOCTAVE:
			return "ThirdOctave";
		default:
			break;
		
		}
		return null;
	}
	
	public String getLongName() {
		String unit = "Hz";
		double scale = 1.;
		if (f1 > 1000) {
			unit = "kHz";
			scale = 1000.;
		}
		return String.format("%s %3.1f-%3.1f %s", name, f1/scale, f2/scale, unit);
	}


	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}


	/**
	 * @return the bandType
	 */
	public BandType getBandType() {
		if (bandType == null) {
			bandType = oldBandType(type);
		}
		return bandType;
	}


	/**
	 * Convert old integer type to new enum type
	 * @param type2
	 * @return
	 */
	private BandType oldBandType(int oldType) {
		switch (oldType) {
		case TYPE_USER:
			return null;
		case TYPE_THIRDOCTAVE:
			return BandType.THIRDOCTAVE;
		case TYPE_DECIDECADE:
			return BandType.DECIDECADE;
		}
		return null;
	}


	/**
	 * @param bandType the bandType to set
	 */
	public void setBandType(BandType bandType) {
		this.bandType = bandType;
	}
}
