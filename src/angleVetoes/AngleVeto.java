package angleVetoes;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Data for a single angle veto. Note that the channel
 * number information is not currently used. 
 * 
 * @author Douglas Gillespie
 *
 */
public class AngleVeto implements Serializable, Cloneable, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7457498080588517537L;

	protected int channels = 0;

	protected double startAngle = 0;

	protected double endAngle = 0;

	public AngleVeto() {
		super();
	}

	public AngleVeto(int channels, double startAngle, double endAngle) {
		super();
		this.channels = channels;
		this.startAngle = startAngle;
		this.endAngle = endAngle;
	}

	public int getChannels() {
		return channels;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public double getEndAngle() {
		return endAngle;
	}

	public void setEndAngle(double endAngle) {
		this.endAngle = endAngle;
	}

	public double getStartAngle() {
		return startAngle;
	}

	public void setStartAngle(double startAngle) {
		this.startAngle = startAngle;
	}

	@Override
	public AngleVeto clone() {
		try {
			return (AngleVeto) super.clone();
		}
		catch (CloneNotSupportedException e) {
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
