package whistleClassifier;

import java.io.Serializable;
import java.lang.reflect.Field;

import Jama.Matrix;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import classifier.ClassifierParams;
import classifier.ClassifierTypes;

/**
 * Parameters class for fragmented whistle classification. 
 * <p>
 * This class contains the basic parameters which could be 
 * applied to any fragment classification method including 
 * how the whistles were fragmented, and the section length
 * as well as basic information about the input data from the 
 * whistle detector.
 * <p>
 * Sine the classification can use a number of different 
 * statistical classifiers, and those classifiers may also 
 * be used to solve other classification problems in PAMGUARD
 * the actual classification parameters are stored in a separate
 * abstract class ClassifierParams which are got directly from 
 * specific classifiers. 
 * 
 * @author Doug Gillespie
 * @see ClassifierParams
 *
 */
public class FragmentClassifierParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 0;
	
	protected int classifierType = ClassifierTypes.CLASSIFIER_LINEAR;
	
	protected int fftLength, fftHop;
	
	protected int fragmentLength, sectionLength;
	
	/**
	 * Min probability for classification. 
	 * <p>In a linear classifier, probabilities are normalised to one, ideally one
	 * output is close to one and the others are small, but often you get several 
	 * candidate values. This should be able to weed these out and create an 
	 * unknown class. 
	 * <p>With the quadratic classifier, output for each species is an absolute 
	 * probability so setting a minimum will easily generate an unknown class. 
	 */
	protected double minimumProbability;
	
	protected double[] frequencyRange;
	
	protected float sampleRate;
	
	protected int nBootstrap;
	
	protected Matrix confusionMatrix;
	
	protected Matrix stdConfusion;
		
	protected ClassifierParams classifierParams;
	
	private String[] speciesList;
	
	protected String fileName; // file name these parameters were stored in. 
	
	public boolean dumpTextFile; // text file output of all bootstrap data.
	
	public int minimumContourLength = 10;
	
	public int getClassifierType() {
		return classifierType;
	}

	public void setClassifierType(int classifierType) {
		this.classifierType = classifierType;
	}

	public FragmentClassifierParams() {
		fragmentLength = 8;
		sectionLength = 150;
		nBootstrap = 30;
	}
	
	/**
	 * @param speciesList the speciesList to set
	 */
	public void setSpeciesList(String[] speciesList) {
		this.speciesList = speciesList;
//		System.out.println("Classifier params species list set to " + speciesList);
		
	}

	/**
	 * @return the speciesList
	 */
	public String[] getSpeciesList() {
		return speciesList;
	}
	
	public Class getClassifierClass() {
		if (classifierParams == null) {
			return null;
		}
		return classifierParams.getClass();
	}


	public int getFftLength() {
		return fftLength;
	}

	public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}

	public int getFftHop() {
		return fftHop;
	}

	public void setFftHop(int fftHop) {
		this.fftHop = fftHop;
	}

	public String getFileName() {
		return fileName;
	}

	public int getFragmentLength() {
		return fragmentLength;
	}

	public void setFragmentLength(int fragmentLength) {
		this.fragmentLength = fragmentLength;
	}

	public int getSectionLength() {
		return sectionLength;
	}

	public void setSectionLength(int sectionLength) {
		this.sectionLength = sectionLength;
	}

	/**
	 * @return the minimumProbability
	 */
	public double getMinimumProbability() {
		return minimumProbability;
	}

	/**
	 * @param minimumProbability the minimumProbability to set
	 */
	public void setMinimumProbability(double minimumProbability) {
		this.minimumProbability = minimumProbability;
	}

	/**
	 * @return the minimumContourLength
	 */
	public int getMinimumContourLength() {
		return minimumContourLength;
	}

	/**
	 * @param minimumContourLength the minimumContourLength to set
	 */
	public void setMinimumContourLength(int minimumContourLength) {
		this.minimumContourLength = minimumContourLength;
	}

	public double[] getFrequencyRange() {
		return frequencyRange;
	}

	public void setFrequencyRange(double[] frequencyRange) {
		this.frequencyRange = frequencyRange;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getNBootstrap() {
		return nBootstrap;
	}

	public void setNBootstrap(int bootstrap) {
		nBootstrap = bootstrap;
	}

	public Matrix getConfusionMatrix() {
		return confusionMatrix;
	}

	public void setConfusionMatrix(Matrix confusionMatrix) {
		this.confusionMatrix = confusionMatrix;
	}

	public Matrix getStdConfusion() {
		return stdConfusion;
	}

	public void setStdConfusion(Matrix stdConfusion) {
		this.stdConfusion = stdConfusion;
	}

	public ClassifierParams getClassifierParams() {
		return classifierParams;
	}
	
	public double getMinFrequency() {
		if (frequencyRange == null) {
			return 0;
		}
		return frequencyRange[0];
	}
	public double getMaxFrequency() {
		if (frequencyRange == null) {
			return 0;
		}
		return frequencyRange[1];
	}

	public void setClassifierParams(ClassifierParams classifierParams) {
		this.classifierParams = classifierParams;
	}

	@Override
	protected FragmentClassifierParams clone()  {
		try {
			return (FragmentClassifierParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Need to override the Matrix fields here so that they return the double[][] array
	 */
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("confusionMatrix");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return confusionMatrix.getArrayCopy();
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("stdConfusion");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return stdConfusion.getArrayCopy();
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}
	

}
