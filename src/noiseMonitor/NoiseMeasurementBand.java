package noiseMonitor;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class NoiseMeasurementBand implements Serializable, Cloneable, ManagedParameters {

	public static final int TYPE_USER = 0;
	public static final int TYPE_THIRDOCTAVE = 1;
	public static final int TYPE_DECIDECADE = 2;
	
	public static final long serialVersionUID = 1L;

	public double f1, f2;
	
	public String name;
	
	private boolean canEdit;
	
	private boolean canRemove;
	
	private int type;

	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}


	public NoiseMeasurementBand(int type, String name, double f1, double f2) {
		super();
		this.type = type;
		this.name = name;
		this.f1 = f1;
		this.f2 = f2;
		autoEnable();
	}


	public NoiseMeasurementBand(int type) {
		this.type = type;
		autoEnable();
	}
	
	private void autoEnable() {
		canEdit = canRemove = (type==TYPE_USER);
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
}
