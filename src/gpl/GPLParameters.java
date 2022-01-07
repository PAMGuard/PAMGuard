package gpl;

import java.io.File;
import java.io.Serializable;

import PamModel.parametermanager.FieldNotFoundException;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamguardMVC.blockprocess.PamBlockParams;
import gpl.contour.ContourMerge;

public class GPLParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public enum ConnectType  {CONNECT4, CONNECT8};

	/**
	 * Generic description for this parameter set used
	 * by the default parameters list. . 
	 */
	public String description; 
	
	/**
	 * Name of a file settings were imported from. Easiest to keep
	 * this separate from the freeform text. 
	 */
	public String importFile;
	
	/**
	 * Data source name (FFT or beam former data)
	 */
	public String fftSourceName;
	
	/**
	 * Channel / sequence map for processing 
	 * (might replace this with full detection grouping ?)
	 */
	public int sequenceMap = 1;
	
	/**
	 * Minimum frequency for analysis. Currently used both in whitener and 
	 * contour detector. If allowing multiple bands, may have to separate 
	 * this for the different bands. 
	 */
	public double minFreq = 20;
	
	/**
	 * As minFreq above. 
	 */
	public double maxFreq = 100;
	
	/**
	 * Time constant for background whitener and also for 
	 * blocking of data. 
	 */
	public double backgroundTimeSecs = 60;

	/**
	 * Whitening factor for first whitening over backgroundTimeSecs duration 
	 * of data. Used in Base3xWhitener and is a scaling factor used to increase the mean 
	 * subtracted from the spectrogram, i.e. if X is the spectrogram and mu is the mean 
	 * value of the spectrogram, then the white spectrogram is X-white_fac_x*mu. In Matlab 
	 * code this is the 'fac' passed to whiten_matrix in GPL_whiten. <br>
	 * So far as I can see, this doesn't appear in H et al. If it did, it would scale the mu 
	 * term in equations 17 and 18. 
	 */
	public double white_fac_x = 1.0;
	
	/**
	 * This is nu1 in H et al. eq 6. Used as xp1 in Matlab GPL_Quiet. 
	 * This is the scale factor that will emphasise tonal signals. <br>Note that 
	 * in code the spec is raised to this power to calculate bas, but then it all get's squared
	 * again to make baseline0, so this is nu1, not 2nu1.  
	 */
	public double xp1 = 2.5;

	/**
	 * This is nu2 in H et al. eq 6. Used as xp1 in Matlab GPL_Quiet. 
	 * This is the scale factor that will emphasise broad band transients. <br>Note that 
	 * in code the spec is raised to this power to calculate bas, but then it all get's squared
	 * again to make baseline0, so this is nu1, not 2nu1.  
	 */
	public double xp2 = .5; 
	
	/**
	 * This is possibly better thought of as a 'low threshold', final detector 
	 * finds regions that go above this, but only accepts them as detections if 
	 * at least a bin goes above the higher 'thresh' value. 
	 */
	public double noise_ceiling = 50;

	/**
	 * See above. Main detection threshold .
	 */
	public double thresh = 200;

	/**
	 * Minimum gap between peaks in the detector. 
	 */
	public int minPeakGap = 10;
	
	/**
	 * Used to select a peak from the detector. Would this be better used 
	 * on individual contours ? 
	 */
	public double minCallLengthSeconds = 1.;

	/**
	 * Used to select a peak from the detector. Would this be better used 
	 * on individual contours ? 
	 */
	public double maxCallLengthSeconds = 20.;
	
	/**
	 * cut in HT. Threshold for contour detection. 
	 */
	public double contourCut = 6;
	
	/**
	 * Type of contour connector: 4 = sides only, 8 = sides and diagonals. 
	 */
	public ConnectType connectType = ConnectType.CONNECT4;
	
	/**
	 * Min contour area to be kept. fixed value of 10 in HT
	 */
	public int minContourArea = 10;
	
	/**
	 * What to do with multiple contour patches within a single 'detection'
	 */
	public ContourMerge contourMerge = ContourMerge.SEPARATE;
	
	/**
	 * Params controlling blocking of data
	 */
	public PamBlockParams blockParams = new PamBlockParams();

	/**
	 * Keep GPL detections even if they don't have any detected contours. 
	 */
	public boolean keepNullContours = true;

	
	public GPLParameters(String name, double minFreq, double maxFreq, double backgroundTimeSecs, double white_fac_x,
			double xp1, double xp2, double noise_ceiling, double thresh, int minPeakGap, double minCallLengthSeconds,
			double maxCallLengthSeconds) {
		super();
		this.description = name;
		this.minFreq = minFreq;
		this.maxFreq = maxFreq;
		this.backgroundTimeSecs = backgroundTimeSecs;
		this.white_fac_x = white_fac_x;
		this.xp1 = xp1;
		this.xp2 = xp2;
		this.noise_ceiling = noise_ceiling;
		this.thresh = thresh;
		this.minPeakGap = minPeakGap;
		this.minCallLengthSeconds = minCallLengthSeconds;
		this.maxCallLengthSeconds = maxCallLengthSeconds;
		
	}

	public GPLParameters() {
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public GPLParameters clone()  {
		
		// add a check for the newly added PamBlockParams - if it's null, create a new one
		if (blockParams==null) {
			blockParams = new PamBlockParams();
		}
		try {
			GPLParameters newP = (GPLParameters) super.clone();
			if (newP.contourCut == 0) {
				newP.contourCut = 6;
			}
			if (minContourArea == 0) {
				newP.minContourArea = 10;
			}
			if (newP.connectType == null) {
				newP.connectType = ConnectType.CONNECT4;
			}
			if (newP.contourMerge == null) {
				newP.contourMerge = ContourMerge.SEPARATE;
			}
			return newP;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			ps.findParameterData("minPeakGap").setInfo("Minimum gap", "bins", "Minimum gap between peaks (FFT time bins)");
			ps.findParameterData("minCallLengthSeconds").setInfo("Minimum length", "bins", "Minimum length of a detection in seconds");
			ps.findParameterData("maxCallLengthSeconds").setInfo("Maximum length", "bins", "Maximum length of a detection in seconds");
			
		} catch (FieldNotFoundException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
