package whistleClassifier.training;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import whistleClassifier.WhistleContour;
import whistleClassifier.WhistleFragmenter;

/**
 * Training contours from a single file, each contour representing
 * one whistle. 
 * <p>
 * For each species, several of these will probably be held
 * in a TrainingDataGroup
 * 
 * @author Doug Gillespie
 *	@see TrainingDataGroup
 */
public class TrainingDataSet implements Serializable, ManagedParameters {
	
	static public final long serialVersionUID = 0;

	private String species;
	
	private int fftLength;
	
	private float sampleRate;
	
	private int ffthop;
	
	private int nContours;
	
	private long trainingTime;
	
	private String sourceDataType;
	
	private String storageSource;
	
	private ArrayList<TrainingContour> trainingContours;

	public TrainingDataSet(Class sourceDataClass, String species, float sampleRate, int fftLength, int ffthop) {
		super();
		this.sourceDataType = sourceDataClass.getName();
		this.species = species;
		this.fftLength = fftLength;
		this.ffthop = ffthop;
		this.sampleRate = sampleRate;
		trainingContours = new ArrayList<TrainingContour>();
		nContours = 0;
		trainingTime = System.currentTimeMillis();
	}
	
	@Override
	synchronized public String toString() {
		String ans = String.format("%d contours, %d kHz, FFT Length %d, Hop %d",
				getNumContours(), (int) sampleRate/1000, fftLength, ffthop); 
		if (storageSource != null) {
			ans = String.format("%s %s", storageSource, ans);
		}
		return ans;
	}

	synchronized public int addContour(WhistleContour contour) {
		return addContour(contour.getTimesInSeconds(), contour.getFreqsHz());
	}
	
	synchronized public int addContour(double[] t, double[] f) {
//		return addContour(new TrainingContour)
		trainingContours.add(new TrainingContour(t, f));
		nContours++;
		return trainingContours.size();
	}

	synchronized public String getSpecies() {
		return species;
	}

	synchronized public long getTrainingTime() {
		return trainingTime;
	}

	synchronized public String getSourceDataType() {
		return sourceDataType;
	}

	synchronized public ArrayList<TrainingContour> getTrainingContours() {
		return trainingContours;
	}

	/**
	 * Get a specific training contour
	 * @param iContour contour index
	 * @return contour
	 */
	synchronized public TrainingContour getTrainingContour(int iContour) {
		return trainingContours.get(iContour);
	}

	synchronized public void setSpecies(String species) {
		this.species = species;
	}

	synchronized public int getFftLength() {
		return fftLength;
	}

	synchronized public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}

	synchronized public float getSampleRate() {
		return sampleRate;
	}

	synchronized public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	synchronized public int getFfthop() {
		return ffthop;
	}

	synchronized public void setFfthop(int ffthop) {
		this.ffthop = ffthop;
	}

	/**
	 * Get the number of contours in the data set. 
	 * @return number of contours. 
	 */
	synchronized public int getNumContours() {
		if (nContours == 0 && trainingContours != null) {
			return trainingContours.size();
		}
		return nContours;
	}
	
	/**
	 * This method was only added so that nContours would be included in the
	 * PamParameterSet when getParameterSet is called
	 * 
	 * @return
	 */
	synchronized public int getNContours() {
		return getNumContours();
	}
	
	/**
	 * Get the number of fragments based on the fragment length
	 * @param fragLen length of each fragment
	 * @return number of fragments. 
	 */
	synchronized public int getNumFragments(WhistleFragmenter fragmenter, double minFreq, double maxFreq,
			int minContourLength) {
		int nFragments = 0;
		int[] fragStarts;
		TrainingContour tc;
		ListIterator<TrainingContour> tci = trainingContours.listIterator();
		double f;
		while(tci.hasNext()) {
			tc = tci.next();
			if (tc.getLength() < minContourLength) {
				continue;
			}
			fragStarts = fragmenter.getFragmentStarts(tc.getLength());
			if (fragStarts == null) {
				continue;
			}
			for (int i = 0; i < fragStarts.length; i++) {
				f = tc.getFreqsHz()[fragStarts[i]];
				if (minFreq > 0 && f < minFreq) {
					continue;
				}
				if (maxFreq > 0 && f > maxFreq) {
					continue;
				}
				nFragments++;
			}
//			if (fragStarts != null) {
//				nFragments += fragStarts.length;
//			}
		}
		return nFragments;
	}

	synchronized public String getStorageSource() {
		return storageSource;
	}

	synchronized public void setStorageSource(String storageSource) {
		this.storageSource = storageSource;
	}

//	public void setNContours(int contours) {
//		nContours = contours;
//	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
