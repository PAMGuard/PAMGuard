package difar.calibration;

import java.util.Arrays;

import PamUtils.BearingMean;
import PamUtils.PamUtils;
import difar.DifarControl;
import difar.DifarParameters;
import pamMaths.PamHistogram;

/**
 * Contains data for a Difar calibration histogram. This is a bit of a mod
 * of the standard histogram 
 * @author doug
 *
 */
public class CalibrationHistogram extends PamHistogram {

	private int channel;
	
	private DifarControl difarControl;
	
	private double[] allAngles = new double[0];

	private double maxAngle;

	public CalibrationHistogram(DifarControl difarControl, int channel, double maxAngle) {
		
		super(maxAngle - 360, maxAngle, difarControl.getDifarParameters().findSpeciesParams(DifarParameters.CalibrationClip).getnAngleSections());
//		this.setName("Difar Cal' chan' " + channel);
		this.difarControl = difarControl;
		this.channel = channel;
		this.maxAngle = maxAngle;
	}
	
	/**
	 * Add new data to the histogram
	 * @param newData data to add to histogram. 
	 */
	public void addData(double newData) {
		// check it's in range 0 - 360. 
		double ca = PamUtils.constrainedAngle(newData, maxAngle);
		int n = allAngles.length;
		allAngles = Arrays.copyOf(allAngles, n + 1);
		allAngles[n] = newData;
		super.addData(ca, true);
	}
	
	@Override
	public double getMean() {
		BearingMean bm = new BearingMean(allAngles);
		return PamUtils.constrainedAngle(bm.getBearingMean(), maxAngle);
	}

	/** Clips further than this from the modal bin are left out of the mean near the mode. */
	public static final double MODE_WINDOW_DEGREES = 5.;

	/**
	 * @return the mean of the angles within {@link #MODE_WINDOW_DEGREES} of
	 * the fullest bin, in the histogram's range, or NaN if it is empty.
	 */
	public double getMeanNearMode() {
		double mode = getMode();
		if (Double.isNaN(mode)) {
			return Double.NaN;
		}
		double mean = CalibrationStats.meanNear(allAngles, mode, MODE_WINDOW_DEGREES);
		return Double.isNaN(mean) ? mode : PamUtils.constrainedAngle(mean, maxAngle);
	}

	@Override
	public double getSTD() {
		BearingMean bm = new BearingMean(allAngles);
		return bm.getBearingSTD2();
	}

	@Override
	public void clear() {
		allAngles = new double[0];
		super.clear();
	}

	/**
	 * @return the channel
	 */
	public int getChannel() {
		return channel;
	}
	
}
