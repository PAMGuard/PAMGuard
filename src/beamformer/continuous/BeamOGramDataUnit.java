package beamformer.continuous;

import java.util.Iterator;
import java.util.List;

import Acquisition.AcquisitionProcess;
import PamUtils.PamUtils;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.DataUnit2D;
import PamguardMVC.PamDataUnit;

/**
 * Data output for the BaemOGram. This is beam data with a high angular resolution 
 * which is used for display purposes and for fine scale angle calculation. <p>
 * The data may have 1, 2 or 3 dimensions, so are always stored in a 3D array, though
 * often the size of the first two dimensions will be 1:
 * <br>Dimension 1: Frequency
 * <br>Dimension 2: Slant angle (strictly angle relative to the perpendicular to the array axis)
 * <br>Dimension 3: Main angle - the angle relative to the main array axis.  
 * <p>Angle 1 - The main angle and the only angle for a linear array is last since this is the 
 * most common case and the data can be easily accessed as an array with a reference to data[0][0]. 
 * <p>So for the simple case of a continuous beamformer, using a linear array, the data will 
 * already be averaged over frequency, so the dimension will be [1][1][nAngle1]. A 3D array
 * beamformer may have both angles present, so will be [1][nAngle2][nAngle1]. 
 * <p>Frequency information is only retained when doing detect-then-localise beam forming 
 * so that the information can be easily displayed on plots of frequency vs angle. 
 * 
 * @author Doug Gillespie
 *
 */
public class BeamOGramDataUnit extends DataUnit2D implements AcousticDataUnit {

	/**
	 * Three dimensional beam data [frequency][angle2][angle1]
	 */
	private double[][][] beamOGramData;
	
	/**
	 * corresponding FFT Slice number
	 */
	private int fftSlice;
	
	public BeamOGramDataUnit(long timeMilliseconds, int channelBitmap, int sequenceBitmap, long startSample, 
			long durationSamples, double[][][] beamOGramData, int fftSeqNumber) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
		this.setSequenceBitmap(sequenceBitmap);
		setBeamOGramData(beamOGramData);
		fftSlice = fftSeqNumber;
	}

	/**
	 * @return the beamOGramData
	 */
	public double[][][] getBeamOGramData() {
		return beamOGramData;
	}

	/**
	 * @param beamOGramData the beamOGramData to set
	 */
	public void setBeamOGramData(double[][][] beamOGramData) {
		this.beamOGramData = beamOGramData;
	}

	/**
	 * @return the number of frequency bins
	 */
	public int getNFrequencyBins() {
		if (beamOGramData == null) {
			return 0;
		}
		return beamOGramData.length;
	}

	/**
	 * @return the number of second angle bins
	 */
	public int getNAngle2Bins() {
		if (getNFrequencyBins() < 1) {
			return 0;
		}
		return beamOGramData[0].length;
	}

	/**
	 * @return the number of first angle bins
	 */
	public int getNAngle1Bins() {
		if (getNAngle2Bins() < 1) {
			return 0;
		}
		return beamOGramData[0][0].length;
	}
	
	/**
	 * Collapse the data down to just the two angular dimensions. 
	 * @param average average the data (data are summed if this is false)
	 * @return a two dimensional array of data [angle2][Angle1]
	 */
	public double[][] getAngleAngleData(boolean average) {
		int nFBins = getNFrequencyBins();
		if (nFBins == 0) {
			return null;
		}
		else if (nFBins == 1) {
			return beamOGramData[0];
		}
		else {
			double scale = average ? 1./(double) nFBins : 1.;
			int nA1 = getNAngle1Bins();
			int nA2 = getNAngle2Bins();
			double[][] data = new double[nA2][nA1];
			for (int f = 0; f < nFBins; f++) {
				for (int i2 = 0; i2 < nA2; i2++) {
					for (int i1 = 0; i1 < nA1; i1++) {
						data[i2][i1] += beamOGramData[f][i2][i1] * scale;
					}
				}
			}
			return data;
		}
	}
	
	/**
	 * Collapse the data onto a single main angle array
	 * @param average average the data (data are summed if this is false)
	 * @return one dimensional data along the main angle. 
	 */
	public double[] getAngle1Data(boolean average) {
		int nA1 = getNAngle1Bins();
		if (nA1 < 1) {
			return null;
		}
		double[][] angleAngleData = getAngleAngleData(average);
		int nA2 = angleAngleData.length;
		if (nA2 == 1) {
			return angleAngleData[0];
		}
		else {
			double scale = average ? 1./(double) nA2 : 1;
			double[] data = new double[nA1];
			for (int i2 = 0; i2 < nA2; i2++) {
				for (int i1 = 0; i1 < nA1; i1++) {
					data[i1] += angleAngleData[i2][i1] * scale;
				}
			}
			return data;
		}
	}
	
	/**
	 * Collapse the data onto a 2D array of frequency and main angle
	 * @param average average the data (data are summed if this is false)
	 * @return a two dimensional array of data [frequency][Angle1]
	 */
	public double[][] getFrequencyAngle1Data(boolean average) {
		int nAng1 = getNAngle1Bins();
		if (nAng1 < 1) {
			return null;
		}
		int nAng2 = getNAngle2Bins();
		int nFreq = getNFrequencyBins();
		if (nAng2 == 1) {
			// simple copy of Ang1 arrays to a lower dimension
			double[][] data = new double[nFreq][];
			for (int i = 0; i < nFreq; i++) {
				data[i] = beamOGramData[i][0];
			}
			return data;
		}
		else {
			// average or sum over Ang2
			double[][] data = new double[nFreq][nAng1];
			double scale = average ? 1./(double) nAng2 : 1;
			for (int f = 0; f < nFreq; f++) {
				for (int i2 = 0; i2 < nAng2; i2++) {
					for (int i1 = 0; i1 < nAng1; i1++) {
						data[f][i1] += beamOGramData[f][i2][i1] * scale;
					}
				}				
			}
			return data;
		}
	}

	@Override
	public double[] getMagnitudeData() {
		/**
		 * Always just return the scaled data collapsed onto the main angle. 
		 */
		double[] data = getAngle1Data(false);
		return dataToDB(data);
	}

	/**
	 * Convert a 2D array of magnitude data to a decibel scale
	 * @param beamData beam output data (magnitude squared)
	 * @return data scaled to dB re 1uPa/sqrt(Hz). 
	 */
	public double[][] dataToDB(double[][] beamData) {	
		if (beamData == null) {
			return null;
		}
		int nD1 = beamData.length;
		double[][] magData = new double[nD1][];
		for (int i = 0; i < nD1; i++) {
			magData[i] = dataToDB(beamData[i]);
		}
		return magData;
	}
	
	/**
	 * Convert a 1D array of magnitude data to a decibel scale
	 * @param beamData beam output data (magnitude squared)
	 * @return data scaled to dB re 1uPa/sqrt(Hz). 
	 */
	public double[] dataToDB(double[] beamData) {	
		if (beamData == null) {
			return beamData;
		}
		AcquisitionProcess daqProcess = null;
		BeamOGramDataBlock parentDataBlock = null;
		int iChannel = PamUtils.getSingleChannel(getChannelBitmap());
		// get the acquisition process. 
		try {
			daqProcess = (AcquisitionProcess) (getParentDataBlock().getSourceProcess());
			daqProcess.prepareFastAmplitudeCalculation(iChannel);
			parentDataBlock = (BeamOGramDataBlock) getParentDataBlock();
		}
		catch (ClassCastException e) {
			return beamData;
		}
		int nBins = beamData.length;
		double[] magSqData = new double[nBins];
		for (int i = 0; i < nBins; i++) {
			magSqData[i] = daqProcess.fftAmplitude2dB(beamData[i], iChannel, 
					getParentDataBlock().getSampleRate(), parentDataBlock.getFftLength(), true, true);
		}
		return magSqData;
	}

	/**
	 * @return the fftSlice
	 */
	public int getFftSlice() {
		return fftSlice;
	}

	/**
	 * Average a list of beamogram data units over time , collapsing 
	 * Frequency and also transposing so that angle1 is on x and angle2 on y. 
	 * @param beamOData array list o BeamoGram data. 
	 * @return double array of angle2 vs angle1 data. 
	 */
	public static double[][] averageAngleAngleData(List<BeamOGramDataUnit> beamOData) {
		if (beamOData == null || beamOData.size() == 0) {
			return null;
		}

		int n = beamOData.size();
		double[][] data = beamOData.get(0).getAngleAngleData(true);
		int ni = data.length;
		int nj = data[0].length;
		double[][] aveData = new double[nj][ni];
		double scale = 1./(double) n;
		for (BeamOGramDataUnit bodu:beamOData) {
			data = bodu.getAngleAngleData(true);
			for (int i = 0; i < ni; i++) {
				for (int j = 0; j < nj; j++) {
					double v = data[i][j];
					if (Double.isFinite(v)) {
						aveData[j][i] += v*scale;
					}
				}
			}
		}
		return aveData;
	}

	/**
	 * Average a list of beamogram data units over time , collapsing 
	 * Angle2 and also transposing so that angle1 is on x and frequency on y. 
	 * @param beamOData array list o BeamoGram data. 
	 * @return double array of frequency vs angle1 data. 
	 */
	public static double[][] averageFrequencyAngle1Data(List<BeamOGramDataUnit> beamOData) {
		// need to integrate over time and also transpose. 
		if (beamOData == null || beamOData.size() == 0) {
			return null;
		}
		int n = beamOData.size();
		double[][] data = beamOData.get(0).getFrequencyAngle1Data(true);
		int ni = data.length;
		int nj = data[0].length;
		double[][] aveData = new double[nj][ni];
		double scale = 1./(double) n;
		for (BeamOGramDataUnit bodu:beamOData) {
			data = bodu.getFrequencyAngle1Data(true);
			for (int i = 0; i < ni; i++) {
				for (int j = 0; j < nj; j++) {
					double v = data[i][j];
					if (Double.isFinite(v)) {
						aveData[j][i] += v*scale;
					}
				}
			}
		}
		return aveData;
	}
	
	/**
	 * Average a list of BeamoGram dataunits over time, frequency and angle1 to 
	 * get a single array of amplitude vs angle1. 
	 * @param beamOData list o fBeamOGram data units
	 * @return anlge1 array. 
	 */
	public static double[] getAverageAngle1Data(List<BeamOGramDataUnit> beamOData) {
		if (beamOData == null || beamOData.size() == 0) {
			return null;
		}
		double[] data = beamOData.get(0).getAngle1Data(true);
		Iterator<BeamOGramDataUnit> it = beamOData.iterator();
		it.next();
		while (it.hasNext()) {
			BeamOGramDataUnit bdu = it.next();
			double[] mData = bdu.getAngle1Data(true);
			for (int i = 0; i < mData.length; i++) {
				data[i] += mData[i];
			}
		}
		double scale = 1./beamOData.size();
		if (scale != 1.) for (int i = 0; i < data.length; i++) {
			data[i] *= scale;
		}
		return data;
	}
}
