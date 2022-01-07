package group3dlocaliser.algorithm.gridsearch;

import Array.ArrayManager;
import Array.PamArray;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataBlock;
import generalDatabase.SQLLoggingAddon;
import group3dlocaliser.algorithm.LocaliserAlgorithm3D;
import group3dlocaliser.grids.Grid3D;
import pamMaths.PamVector;

/**
 * Grid search using Matched field Processing 
 * Not completed - will take far too long and need too much memory 
 * to process at resolution required for NNHF clicks. 
 * @author dg50
 *
 */
public abstract class MFPGridSearch extends LocaliserAlgorithm3D{
	
	public static final String algoName = "MFP Grid Search";
	
//	private Grid3D grid3d;

	private PamArray currentArray;

	private PamVector arrayCenter;
	
	private float[][][][] gridDistances;
	
	private MFPGridSearchParams mfpGridSearchParams = new MFPGridSearchParams();
	
	/**
	 * Factor by which to increase resolution of steering vector calculations. 
	 */
	private int upResFactor = 10;
	
	private ComplexArray steeringVector;

	private float sampleRate;

	public MFPGridSearch() {
//		prepare();
	}

	@Override
	public String getName() {
		return algoName;
	}

	@Override		
	public boolean prepare(PamDataBlock sourceBlock) {
//		this.sampleRate = sourceBlock.getSampleRate();
//		/**
//		 * Make a grid, which is centred around (0,0,0)
//		 * Will need to offset this by centre of hydrophones. 
//		 */
//		grid3d = new StretchedSphericalGrid(null);
//		// get the hydrophone array ... and work out a few things
//		// such as max separations,etc. 
//		currentArray = ArrayManager.getArrayManager().getCurrentArray();
//		int nPhones = currentArray.getHydrophoneCount();
//		int nPairs = (nPhones-1)*nPhones/2;
//		// with 12 hydrophones, there will be 66 pairs - that's a lot !
//		double[][] arrayDims = currentArray.getDimensionLimits();
//		arrayCenter = new PamVector();
//		for (int i = 0; i < 3; i++) {
//			arrayCenter.setElement(i, (arrayDims[i][0]+arrayDims[i][1])/2.);
//		}
//		/**
//		 * Will be setting up time delay rotations for each grid point, but will 
//		 * also need to be able to offset these by the start sample offset of each
//		 * data unit coming into the system, so will need to know the maximum 
//		 * inter group delay so that this can be pre calculated for each frequency. 
//		 */
//		double maxSep = currentArray.getMaxPhoneSeparation(0);
//		double maxSepSeconds = maxSep / currentArray.getSpeedOfSound();
//		double masSepSamples = maxSepSeconds * sampleRate;
//		/*
//		 * For the Meygen array sampled at 500kHz, this could be as much as 4000 samples. 
//		 * We'll need to store an FFT length array of complex numbers for every possible
//		 * delay - so for FFTLen 512, that would be 4000*256*2*8 = about 16MBytes of data - fine.
//		 */
//		
////		System.out.printf("%s has %d points\n", grid3d.getGridName(), grid3d.getTotalPoints());
//		/*
//		 * Will then have to calculate steering vectors for every point in the grid. 
//		 * Current grid has 24605 points. Will need to calculate a steering vector for each of those
//		 * locations for each of the 12 hydrophones (h0 and be unity I guess), so will need
//		 * 24605*12*256*2*8 = 1.2 gigabytes of memory. Bugger !
//		 * Tricks to being this down ...
//		 * Work out which parts of the grid are viable - remove too high and too low in Z, i.e 
//		 * create invalid regions. 
//		 * Use floats instead. 
//		 * Exploit symetry ?
//		 * Don't calculate a grid, but work out the steering vectors on the fly ?
//		 * Only pre calculate distances as floats, so need 24605*12*4 = 1.2MBytes
//		 * Acknowledge that a tiny fraction of a degree difference in steering vectors isn't 
//		 * going to make any bloody difference, so make about 10000 steering vectors once, 
//		 * then for different time delays, work out a simple short[] lookup table to select the 
//		 * appropriate one - or calculate steering index on the fly ... This approach could 
//		 * also remove the need for the general offset LUT as well. 
//		 * So how much phase difference does matter ? at 1.5kHz wavelength = 1m, so 1/1000'th 
//		 * of a circle will be a millimeter. Is there much point in having more of these points 
//		 * than there are samples ? Not really, so if we've a 512pt FFt, we might as well just have
//		 * 512 points in the steering vector - nothing really to gain by having more, but lose if we have less.
//		 * If we were doing vlf work, so at 15Hz, wavelength = 100m, so 1/1000th = .1m still. 
//		 * 
//		 * Full sequence of operations is:
//		 * 1. For Each point in the grid ...
//		 * 2. For each phone calculate the expected delay in samples relative to a reference point
//		 *    (i.e. the centre of the array)
//		 * 3. Subtract off the minimum delay for all phones for that point, so one of them 
//		 *    is zero. Could easily store these as floats or doubles.
//		 * 4. When grouped data arrive, look at the differences in start times of the different
//		 *    channel groups. Subtract these differences off from the expected differences calculated above
//		 * 5. Do a reality check - does the waveform clip still even overlap with the expected delay time ? 
//		 *    If not, then no need to calculate that point.
//		 * 6. If overlap is good, then calculate the phase difference for each frequency, then pick the 
//		 *    closest pre-calculated steering vector and beam form. 
//		 *       
//		 */
//		gridDistances = new float[grid3d.getNumPoints(0)][grid3d.getNumPoints(1)][grid3d.getNumPoints(2)][nPhones];
//		for (int i = 0; i < grid3d.getNumPoints(0); i++) {
//			for (int j = 0; j < grid3d.getNumPoints(1); j++) {
//				for (int k = 0; k < grid3d.getNumPoints(2); k++) {
//					PamVector gridPoint = grid3d.getCartesianVector(i, j, k);
//					for (int p = 0; p < nPhones; p++) {
//						PamVector pV = currentArray.getAbsHydrophoneVector(p, 0).sub(arrayCenter);
//						double d = pV.dist(gridPoint)/currentArray.getSpeedOfSound()*sampleRate;
//						gridDistances[i][j][k][p] = (float) d;
//					}
//				}
//			}
//		}
//		
//		// now the highish resolution steering vector....
//		int nSteer = mfpGridSearchParams.fftLength * upResFactor;
//		steeringVector = new ComplexArray(nSteer);
//		for (int i = 0; i < nSteer; i++) {
//			double phase = Math.PI*2./(double)nSteer*(double)i;
//			steeringVector.set(i, Math.cos(phase), Math.sin(phase));
//		}
		
		return false;
	}


	@Override
	public SQLLoggingAddon getSQLLoggingAddon(int arrayType) {
		// TODO Auto-generated method stub
		return null;
	}

}
