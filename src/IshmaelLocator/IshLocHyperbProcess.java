package IshmaelLocator;


import fftManager.FFT;
import Localiser.*;
import Localiser.algorithms.Correlations;
import PamUtils.CoordUtils;
import PamguardMVC.PamDataUnit;
import IshmaelLocator.IshLocProcess;
import IshmaelLocator.IshLocalisation;
import IshmaelLocator.LM;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;


/** Two-hydrophone bearing calculation.
 * @author Dave Mellinger
 */
public class IshLocHyperbProcess extends IshLocProcess {
	double[] freqRange = new double[2];
	double deltaTime[][];	//3 cols: phone1, phone2, corr_lag_in_sec
	int nPairs;				//index into deltaDist
	int nDim;				//2 or 3
	Correlations correlations = new Correlations();
	private DelayMeasurementParams delayParams = new DelayMeasurementParams();

	public IshLocHyperbProcess(IshLocControl ishLocControl) {
		super(ishLocControl);
	}
	
	@Override
	public String getName() {
		return "Hyperbolic location";
	}
	
	/** Calculate the location of the sound source.  this.arraygeom is
	 * set upon entry to the hydrophone positions.
	 */
	//TODO: normalized freqs should not be ignored as they are now
	@Override
	IshLocalisation calcData(PamDataUnit outputUnit, int referencePhones, 
			double[][] selectionSams, double rawSampleRate, double f0, double f1)
		throws noLocationFoundException
	{
		final double tol = 1e-10;
		IshLocHyperbParams p = (IshLocHyperbParams)ishLocControl.ishLocParams;
		nDim = p.nDimensions;
		int nPhones = selectionSams.length;

		//Re-get the array geometry. This is done at the time of the loc in case
		//the geometry has changed since this IshLocHyperbProcess was created.
		//PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//		for (int i = 0; i < nPhones; i++)
//			arraygeom[i] = array.getHydrophone(hydlist[i]).getCoordinate();

		//arraygeom = array.getLocalGeometry(outputUnit.getTimeMilliseconds());

/* I'm pretty sure all this arraygeom stuff is obsolete now. arraygeom should be 
 * set upon entry to this routine.
		HydrophoneLocator hydrophoneLocator = array.getHydrophoneLocator();
//		hydrophoneLocator.getPhoneLatLong(outputUnit.getTimeMilliseconds(), i)
		//}
		LatLong originLatLong = hydrophoneLocator.getPhoneLatLong(outputUnit.getTimeMilliseconds(), 0);
		LatLong phoneLL;
		outputUnit.setOriginLatLong(originLatLong);
		for (int i = 0; i < nPhones; i++) {
			phoneLL = hydrophoneLocator.getPhoneLatLong(outputUnit.getTimeMilliseconds(), i);
			arraygeom[i][0] = originLatLong.distanceToMetresX(phoneLL);
			arraygeom[i][1] = originLatLong.distanceToMetresY(phoneLL);
			arraygeom[i][2] = hydrophoneLocator.getPhoneDepth(outputUnit.getTimeMilliseconds(), i);
		}
*/
		
		//Calculate the lag (delay) between every pair of channels.  Results are
		//put in deltaDist: col0 is phone1 index, col1 is phone2 index, and col3
		//is lag in seconds.
		int nFFT = 2 * FFT.nextBinaryExp(selectionSams[0].length);
		deltaTime = new double[nPhones*(nPhones-1)/2][3];
		int nPairs = 0;
		//		nPairs = 0;			//index into deltaDist
		for (int i = 0; i < nPhones; i++) {
			for (int j = i+1; j < nPhones; j++) {
				//Cross-correlate phones i and j, use corr peak to get lag.
				double lagSams = correlations.getDelay(selectionSams[i], 
						selectionSams[j], delayParams, getSampleRate(), nFFT).getDelay();
				double lagSec = lagSams / rawSampleRate;
				double dist = CoordUtils.dist(arraygeom[i], arraygeom[j], nDim);
				double maxDelaySec = dist / c;

				if (Math.abs(lagSec) > maxDelaySec) {
					//Physically impossible.  But sometimes the lag is close to 
					//the limit and just got thrown off a bit by noise, roundoff,
					//etc.  If so, make the lag equal to the max possible delay.
					if (Math.abs(lagSec * 1.05) <= maxDelaySec)
						lagSec = maxDelaySec * Math.signum(lagSec);
					else {
						//Throw out this lag, instead of throwing out whole loc.
						continue;
					}
				}
				deltaTime[nPairs][0] = i;
				deltaTime[nPairs][1] = j;
				deltaTime[nPairs][2] = lagSec;
				nPairs++;
			}
		}
		this.nPairs = nPairs;
		TimeDiff_LM lm = new TimeDiff_LM();
		double initGuess[] = initialGuess(lm); 
		lm.calc_simple(true, nPairs, nDim, tol, initGuess); 

		IshLocalisation iLoc = new IshLocalisation(outputUnit, LocContents.HAS_LATLONG,
				referencePhones);
		iLoc.getLocContents().setLocContent((nDim == 2) ? LocContents.HAS_XY : LocContents.HAS_XYZ);
		iLoc.addLocContents(LocContents.HAS_LATLONG);
		
		iLoc.x = lm.x[0];
		iLoc.y = lm.x[1];
		if (nDim == 3)
			iLoc.z = lm.x[2];
		
		return iLoc;
	}
	
	/** Find a starting point (initial guess) for the LM algorithm.
	 * Try a 20x20 grid of points centered on the middle of the phone 
	 * array and 3 times as wide as the array.  The gridpoint with the
	 * lowest sum-squared error becomes the initial guess.
	 *
	 * @return a 2- or 3-D coordinate vector with the starting point for the
	 * LM algorithm.
	 * @author Dave Mellinger
	 */
	double[] initialGuess(TimeDiff_LM lm) {
		final int nGrid2 = 20, nGrid3 = 8;
		final double span = 3.0;		//initial grid size is arrayWidth*span
		
		//First find the bounds of the phone array in lo[] and hi[].  Also get
		//arrayWidth, the largest size of the phone array in any dimension.
		double lo[] = new double[nDim], hi[] = new double[nDim];
		double arrayWidth = Double.MIN_VALUE;
		for (int j = 0; j < nDim; j++) {
			lo[j] = Double.MAX_VALUE; hi[j] = Double.MIN_VALUE;
			for (int i = 0; i < arraygeom.length; i++) {
				lo[j] = Math.min(lo[j],arraygeom[i][j]);
				hi[j] = Math.max(hi[j],arraygeom[i][j]);
			}
			arrayWidth = Math.max(arrayWidth, hi[j] - lo[j]);
		}
		//Set gridMin to be the lowest value in the grid for each dim.
		double gridMin[] = new double[nDim];
		for (int dim = 0; dim < nDim; dim++)
			gridMin[dim] = (lo[dim] + hi[dim])/2 - arrayWidth*span/2;

		//Now set up the grid of guesses, calculate error for each one, 
		//take sum squared.  If any sum-squared error is better than the 
		//extant sum-squared, make it the new initial guess.
		double guess[] = new double[nDim];				//this guess
		double initGuess[] = new double[nDim];			//initial (best) guess
		double minSumSq = Double.MAX_VALUE;
		int nGrid = (nDim == 2) ? nGrid2 : nGrid3;		//for x and y
		int nGridZ = (nDim == 2) ? 0 : nGrid3;
		for (int xi = 0; xi <= nGrid; xi++) {			//note '<='
			for (int yi = 0; yi <= nGrid; yi++) {		//note '<='
				for (int zi = 0; zi <= nGridZ; zi++) {	//note '<=', nGridZ
					guess[0] = gridMin[0] + (double)xi / nGrid * arrayWidth * 3;
					guess[1] = gridMin[1] + (double)yi / nGrid * arrayWidth * 3;
					if (nDim == 3)
						guess[2] = gridMin[2] + (double)zi / nGridZ * arrayWidth * 3;

					double result[] = new double[nPairs];
					lm.fcn(guess, result, false);
					
					double sumSq = CoordUtils.sumSquared(result);
					if (sumSq < minSumSq) {
						//Found a better one.  Set this up as initGuess.
						minSumSq = sumSq;
						for (int dim = 0; dim < nDim; dim++)
							initGuess[dim] = guess[dim];
					}
				}
			}
		}
		return initGuess;
	}
	
	/** Functions for running the LM algorithm.
	 * <p>
	 * fcn() calculates the difference between the time delay for point x
	 * and the time delay measured from the audio signal.  
	 * <p>
	 * jacFcn is the Jacobian of fcn, i.e., the partial derivative of each
	 * time-difference function in each dimension.
	 * 
	 * @author Dave Mellinger
	 */
	class TimeDiff_LM extends LM {
		@Override
		boolean fcn(double x[], double y[], boolean printFlag) {
			for (int i = 0; i < nPairs; i++) {
				double d1 = CoordUtils.dist(x, arraygeom[(int)deltaTime[i][0]], nDim); 
				double d2 = CoordUtils.dist(x, arraygeom[(int)deltaTime[i][1]], nDim);

				

				double dt = (d2 - d1) / c;
				
				
				
				
				y[i] = dt - deltaTime[i][2];
			}
			return true;
		}
		@Override
		boolean jacFcn(double x[], double jac[][], boolean printFlag) {
			double d1[] = new double[nPairs];
			double d2[] = new double[nPairs];
			double d;
			double dxy1[][] = new double[nPairs][nDim], dxy2[][] = new double[nPairs][nDim];
			for (int pr = 0; pr < nPairs; pr++) {
				//Beware that phone1 & 2 have 3 elements even for 2-D locs.
				double phone1[] = arraygeom[(int)deltaTime[pr][0]]; 
				double phone2[] = arraygeom[(int)deltaTime[pr][1]];
				d1[pr] = d2[pr] = 0.0;
				for (int dim = 0; dim < nDim; dim++) {
					dxy1[pr][dim] = d = x[dim] - phone1[dim];
					d1[pr] = d1[pr] + d * d;
					dxy2[pr][dim] = d = x[dim] - phone2[dim];
					d2[pr] = d2[pr] + d * d;
				}
				d1[pr] = Math.sqrt(d1[pr]);
				d2[pr] = Math.sqrt(d2[pr]);
			}

			//Calculate the return value jac.
			for (int dim = 0; dim < nDim; dim++) {
				for (int pr = 0; pr < nPairs; pr++) {
					double a1 = (d1[pr] == 0) ? 0.0 : dxy1[pr][dim] / d1[pr];
					double a2 = (d2[pr] == 0) ? 0.0 : dxy2[pr][dim] / d2[pr];
					jac[pr][dim] = (a2 - a1) / c;
				}
			}
			return true;
		}
	}

	@Override
	public String getMarkName() {
		// TODO Auto-generated method stub
		return null;
	}
}
