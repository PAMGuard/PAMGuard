package difar.demux;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JOptionPane;

import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import Filters.IIRFilterMethod;
import PamController.PamControlledUnit;
import PamUtils.PamUtils;
import difar.DemuxObserver;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarParameters;

/**
 * 
 * @author doug
 *
 */
public class NativeDemux extends GreenridgeDemux {

	boolean libOK;

	private final String libName32 = "GreeneridgeDemux32";
//	private final String libName32 = "DifarDemux";
	private final String libName64 = "GreeneridgeDemux64";

	private PamControlledUnit difarControl;
	
	private final int minJNIVersion = 2;

	private DemuxObserver demuxObserver;

	private int nSamples;

	public NativeDemux(PamControlledUnit difarControl) {
		super(difarControl);
		this.difarControl = difarControl;
		String libName = getDLLName();
		libOK = loadLibrary(libName);
		if (libOK == false) {
			System.out.println("Unable to load demultiplexing library " + libName);
		}
		else {
			System.out.println("Demultiplexing library sucessfully loaded: " + libName);
			//			difar_init("");
		}
		checkVersion();
	}
	

	private String getDLLName() {
		String model = System.getProperty("sun.arch.data.model");
		if (model.contains("32")) {
			return libName32;
		}
		else {
			return libName64;
		}
	}
	
	public void checkVersion() {
		String msg = null;
		if (libOK == false) {
			msg = "The demultiplexing library " + getDLLName() + " is not installed. "
					+ "See the help files for details on how to obtain it "
					+ "or use an alternative demultiplexor.\n";
		}
		else {
			int version = -1;
			try {
				version = jniGetVersion();
			}
			catch (Exception e) {}
			if (version < minJNIVersion) {
				msg = "The demultiplexing library  " + getDLLName() + " is an unsupported version. "
						+ "The minimum required version is currently V" + minJNIVersion 
						+ ". See the help files for details on how to obtain it.\n";
			}
		}
		if (msg != null) {
			System.out.println(msg);
//			JOptionPane.showMessageDialog(null, msg, "Difar demultiplex module", JOptionPane.ERROR_MESSAGE);
		}
	}

	private boolean loadLibrary(String libName) {
		try {
			System.loadLibrary(libName);
			return true;
		}
		catch (UnsatisfiedLinkError e) {
			System.out.println(e.getMessage());
			return false; 
		}
	}

	private native int jniGetVersion();
	
	private native double jniDifarInit(String fileName, String folderName);
	/*
	 * 
(JNIEnv *env, jobject obj, jdoubleArray jRawData, jdoubleArray jom,
		jdoubleArray jew, jdoubleArray jns, jdoubleArray jnco_freq,
		jdoubleArray jphase_diff, jbooleanArray jlock_75,
		jbooleanArray jlock_15,	jbooleanArray jnco_out) {
	 */
	private native int jniDifarDemux(double[] jRawData, double[] jom,
			double[] jew, double[] jns, double[] jnco_freq,
			double[] jphase_diff, boolean[] jlock_75,
			boolean[] jlock_15,	boolean[] jnco_out);

	/**
	 * Set up decimation within the Demux jni code. Doing the decimation as soon 
	 * as the data come back (sample by sample) from the actual demultiplexer 
	 * saves copying back three or more full length arrays of audio data which 
	 * creates a massive memory saving and should also be faster. 
	 * @param decimationFactor decimation factor - must be integer !
	 * @param filterCoefficients filter coefficients for the anti alias filters in biquad pairs (BBAABBAA, etc.)
	 * @param filterGain filter gain. 
	 */
	private native void jniSetDecimation(int decimationFactor, double[] filterCoefficients, double filterGain);
	
	/**
	 * Clear decimation in the native C code - deleted Anti alias filters and 
	 * sets the decimation factor to 1. 
	 */
	private native void jniClearDecimation();
	
	@Override
	public boolean configDemux(DifarParameters difarParams, double sampleRate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DifarResult processClip(double[] difarClip, double sampleRate, int decimationFactor, DemuxObserver observer, DifarDataUnit difarDataUnit) {		
		if (libOK == false) {
			return null;
		}
		this.demuxObserver = observer;
		this.nSamples = difarClip.length;
		File pFile = createParFile();
		double ans = jniDifarInit(pFile.getAbsolutePath(), demuxParams.getTempDir());
		
		if (decimationFactor <= 1) {
			jniClearDecimation();
			decimationFactor = 1;
		}
		else {
			// need to set up some filters to run in the C code. Calculate the filter
			// coefficients here, and send across to avoid having to do the filter coeff 
			// calcs in C ! Default to a 8 order butterworth with cut off at new Niquist freq. 
			FilterParams filtParams = new FilterParams(); 
			filtParams.filterBand = FilterBand.LOWPASS;
			filtParams.filterOrder = 8;
			filtParams.filterType = FilterType.BUTTERWORTH;
			filtParams.lowPassFreq = (float) (sampleRate/2/decimationFactor);
			FilterMethod iirMethod = FilterMethod.createFilterMethod(sampleRate, filtParams);
			double[] filterCoeffs = iirMethod.getFastFilterCoefficients();
			double filterGain = iirMethod.getFilterGainConstant();
			jniSetDecimation(decimationFactor, filterCoeffs, filterGain);
		}
		/*
		 * 
			(JNIEnv *env, jobject obj, jdoubleArray jRawData, jdoubleArray jom,
					jdoubleArray jew, jdoubleArray jns, jdoubleArray jnco_freq,
					jdoubleArray jphase_diff, jbooleanArray jlock_75,
					jbooleanArray jlock_15,	jbooleanArray jnco_out) {
		 */
		DifarResult nativeResult = new DifarResult(difarClip.length/decimationFactor);
		jniDifarDemux(difarClip, nativeResult.getOm(), nativeResult.getEw(), nativeResult.getNs(), nativeResult.getNco_freq(),
				nativeResult.getPhase_diff(), nativeResult.getLock_75(), nativeResult.getLock_15(), nativeResult.getNco_out());

		return nativeResult;
	}

	
	@Override
	public boolean hasOptions() {
		return false;
	}

	@Override
	public boolean showOptions(Window window, DifarParameters difarParams) {
		return false;
	}

	@Override
	public Component getDisplayComponent() {
		// TODO Auto-generated method stub
		return null;
	}

//	public void testCalls() {
//		if (libOK == false) {
//			return;
//		}
//		File pFile = createParFile();
//		double ans = difarinit(pFile.getAbsolutePath(), demuxParams.tempDir);
//		System.out.println("REturn code from difarinit = " + ans);
//		double[] testData = new double[960000];
//		Random r = new Random();
//		for (int i = 0;i < testData.length; i++) {
//			testData[i] = r.nextGaussian();
//		}	
//		/*
//		 * 
//		(JNIEnv *env, jobject obj, jdoubleArray jRawData, jdoubleArray jom,
//				jdoubleArray jew, jdoubleArray jns, jdoubleArray jnco_freq,
//				jdoubleArray jphase_diff, jbooleanArray jlock_75,
//				jbooleanArray jlock_15,	jbooleanArray jnco_out) {
//		 */
//		DifarResult nativeResult = new DifarResult(testData.length);
//		difardemux(testData, nativeResult.getOm(), nativeResult.getEw(), nativeResult.getNs(), nativeResult.getNco_freq(),
//				nativeResult.getPhase_diff(), nativeResult.getLock_75(), nativeResult.getLock_15(), nativeResult.getNco_out());
//	}

	/**
	 * Used as a callback from the jni to pass back status 
	 * data from the demultiplexing process. 
	 * @param samplesProcessed total number of samples processed. 
	 * @param lock75 lock status for 7.5 kHz carrier
	 * @param lock15 lock status for 15 kHz carrier
	 */
	public void setProgress(int samplesProcessed, boolean lock75, boolean lock15) {
		if (nSamples <= 0 || demuxObserver==null) {
			return;
		}
		double perc = (double) samplesProcessed / (double) nSamples * 100;
		demuxObserver.setStatus(perc, lock75, lock15);
		
//		System.out.println(String.format("Demux progress sample %d 7.5 lock %s 15 lock %s ", samplesProcessed, 
//				new Boolean(lock75).toString(), new Boolean(lock15).toString()));
	}

}
