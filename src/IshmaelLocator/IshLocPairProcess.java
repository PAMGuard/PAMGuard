package IshmaelLocator;



import Localiser.DelayMeasurementParams;
import Localiser.algorithms.Correlations;
import PamDetection.LocContents;
import PamUtils.CoordUtils;
import PamguardMVC.PamDataUnit;
import fftManager.FFT;


// import IshmaelDetector.OutputDataBlkIntfc;
// import IshmaelDetector.OutDataBlk;

/** Two-hydrophone bearing calculation.
 * @author Dave Mellinger
 */
public class IshLocPairProcess extends IshLocProcess {
	double[] freqRange = new double[2];
	Correlations correlations = new Correlations();
	private DelayMeasurementParams delayParams = new DelayMeasurementParams();

	public IshLocPairProcess(IshLocControl ishLocControl) {
		super(ishLocControl);
	}
	
	@Override
	public String getName() {
		return "Phone pair location";
	}
	
	//TODO: normalized freqs should not be ignored as they are now
	@Override
	IshLocalisation calcData(PamDataUnit outputUnit, int referencePhones,
			double[][] selectionSams, double rawSampleRate, double f0, double f1)
	throws noLocationFoundException
	{
		//double normF0 = f0 / (rawSampleRate / 2);
		//double normF1 = f1 / (rawSampleRate / 2);
		
		int nPhones = selectionSams.length;

		//For now, just cross-correlate first (0th) and last (n-1st) phones.
		int i0 = 0;
		int i1 = nPhones - 1;
		
		//Calculate the lag between the two channels.
		int nFFT = 2 * FFT.nextBinaryExp(selectionSams[0].length);
		double lagSams = correlations.getDelay(selectionSams[i0], 
				selectionSams[i1], delayParams, getSampleRate(), nFFT).getDelay();
		double lagSec = lagSams / rawSampleRate;
		
		double dist = CoordUtils.dist(arraygeom[i0], arraygeom[i1]);
		double maxDelaySec = dist / c;

		if (Math.abs(lagSec) > maxDelaySec) {
			//Physically impossible.  But sometimes the lag is close to 
			//the limit and just got thrown off a bit by noise, roundoff,
			//etc.  If so, make the lag equal to the max possible delay.
			if (Math.abs(lagSec * 1.05) <= maxDelaySec)
				lagSec = maxDelaySec * Math.signum(lagSec);
			else
				throw new noLocationFoundException();	//give up
		}
		
		//Get the angle of the phone pair (does not use lag).
		double pairAngle = Math.atan2(arraygeom[i0][1] - arraygeom[i1][1],
				arraygeom[i0][0] - arraygeom[i1][0]);

		//Convert the lag and pair angle into an array-relative angle. Angle
		//here is 0 along positive X-axis, increasing anticlockwise, radians.
		double frac = lagSec / maxDelaySec;
		IshLocalisation iLoc = new IshLocalisation(outputUnit, 
				LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY,
				referencePhones);
		//iLoc.setLocContents(AbstractLocalisation.HAS_BEARING);
		iLoc.theta0 = (pairAngle + Math.acos(frac) + Math.PI*2) % (Math.PI*2);
		iLoc.theta1 = (pairAngle - Math.acos(frac) + Math.PI*2) % (Math.PI*2);
		iLoc.theta0deg = iLoc.theta0 * 180/Math.PI;	//debugging-friendly version
		iLoc.theta1deg = iLoc.theta1 * 180/Math.PI;	//debugging-friendly version
		
		return iLoc;
	}

	@Override
	public String getMarkName() {
		// TODO Auto-generated method stub
		return null;
	}
}
