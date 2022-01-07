package classifier;

import java.awt.Window;

import Jama.Matrix;

/**
 * Interface for fragment classification. Hopefully, a number of different
 * classifiers will be incorporated and each will work under this general interface.
 * <p>
 * Assume that each classifier will be able to return 0 - n-1 different species classifications. 
 * They will provide a list of which species correspond to those numbers. -1 (for nothing I recognise) 
 * will also be a valid classification result. 
 * <p>
 * these classifiers are not thread safe. e.g. When running classifiers, it's likely 
 * that you will call one of the runClassification functions which will return the
 * most likely result, but you may follow this up with subsequent calls to getLogLikelyhood()
 * and getProbability() to get more information about individual group probabilities. 
 * 
 * @author Douglas Gillespie
 *
 */
abstract public class Classifier {
	
	/**
	 * Type of probability that will be returned by this classifier.
	 * @author Doug Gillespie
	 *
	 */
	public enum ProbabilityType {UNAVAILABLE, ABSOLUTE, NORMALISED};
	
	/**
	 * Minimum acceptance probability. <p>
	 * any output with a p of less than this will 
	 * be classed as unclassified (-1). 
	 */
	protected double minimumProbability;

	/**
	 * Will get called AFTER classifier training to get
	 * classifier specific parameters. 
	 * @return classifier specific parameters. 
	 */
	abstract public ClassifierParams getClassifierParams();
	
	/**
	 * Will be called to load stored parameters into a 
	 * classifier. 
	 * @param classifierParams classifier parameters.
	 * @return Return true if parameters loaded OK. Reasons for not
	 * loading include the classifier being incompatible with the
	 * classifier parameters or the parameters not being present (e.t. null matrixes) 
	 */
	abstract public boolean setClassifierParams(ClassifierParams classifierParams);
	
	public boolean hasParamsDialog() {
		return false;
	}
	
	public boolean showParamsDialog(Window parent) {
		return false;
	}

	/**
	 * Gets the name of a specific species 
	 * (generally equivalent to getSpeciesList()[iSpecies] 
	 * @param iSpecies species index 
	 * @return Species Name
	 */
//	public String getSpeciesName(int iSpecies) {
//		if (fragmentClassifierParams == null) {
//			return null;
//		}
//		return fragmentClassifierParams.getSpeciesList()[iSpecies];		
//	}

	/**
	 * Run the classification on  single parameter value. 
	 * <p>
	 * Return true if the classification
	 * completed OK, false otherwise. The classification results can 
	 * be obtained through calls to getClassLikelyhoods() and getBestSpeciesBet();
	 * 
	 * @param params array of input parameters
	 * @return true if classification completed successfully.
	 */
	public int runClassification(double[] params) {
		/**
		 * Pack it up as though it's a 2D array of data for
		 * compatibility with the rest of the code. 
		 */
		double[][] p = new double[1][];
		p[0] = params;
		int[] result = runClassification(p);
		if (result == null) {
			return -1;
		}
		return result[0];
	}

	/**
	 * Run classification on multiple parameter values. 
	 * @param params array of input parameters
	 * @return true if completed successfully
	 */
	public int[] runClassification(double[][] params) {
		if (params == null || params.length == 0 || params[0].length == 0) {
			return null;
		}
		Matrix data = new Matrix(params);
		return runClassification(data);
	}
	
	public abstract int[] runClassification(Matrix data);

	/**
	 * Get an array of log likelihoods from the most
	 * recent call to runClassification(double[] );
	 * <p>
	 * This version will return a single row of data, 
	 * which should be from a single classification 
	 * @return array of likelihoods
	 */
	public double[] getLogLikelihoods1() {
		double[][] ans2D = getLogLikelihoods2();
		if (ans2D == null) {
			return null;
		}
		return ans2D[0];
	}
	
	/**	  
	 * Get a double array of log likelihoods from the most
	 * recent call to runClassification(double[][] );
	 * <p>
	 * This version will return a 2D array of data, 
	 * which should be from a set of classifications 
	 * @return array of likelihoods
	 */
	public double[][] getLogLikelihoods2() {
		Matrix m = getLogLikelihoodsM();
		if (m == null) {
			return null;
		}
		return m.getArray();
	}
	
	/**
	 * Get a matrix of log likelihoods from 
	 * the most recent call to RunClassification(Matrix )
	 * <p>
	 * This version will return a matrix of data, which 
	 * should be from a set of classifications. 
	 * @return log likelihoods matrix. 
	 */
	public abstract Matrix getLogLikelihoodsM();

	/**
	 * Get an array of probabilities from the most
	 * recent call to runClassification(double[] );
	 * <p>
	 * This version will return a single row of data, 
	 * which should be from a single classification 
	 * @return array of probabilities
	 */
	public double[] getProbabilities1() {
		double[][] ans2D = getProbabilities2();
		if (ans2D == null) {
			return null;
		}
		return ans2D[0];
	}

	/**	  
	 * Get a double array of probabilities from the most
	 * recent call to runClassification(double[][] );
	 * <p>
	 * This version will return a 2D array of data, 
	 * which should be from a set of classifications 
	 * @return array of probabilities
	 */
	public double[][] getProbabilities2() {
		Matrix m = getProbabilitiesM();
		if (m == null) {
			return null;
		}
		return m.getArray();
	}

	/**
	 * Get a matrix of probabilities from 
	 * the most recent call to RunClassification(Matrix )
	 * <p>
	 * This version will return a matrix of data, which 
	 * should be from a set of classifications. 
	 * @return probabilities matrix. 
	 */
	public abstract Matrix getProbabilitiesM();
	
	/**
	 * Get the type of probability returned by a classifier.
	 * this will either be NORMALISED, ABSOLUTE or UNAVAILABLE
	 * in which case the classifier should still return data
	 * (to stop things crashing) but should return arrays / Matrixes
	 * with zeros in all columns except the selected item, which should
	 * be 1. 
	 * @return type of probability returned. 
	 */
	public abstract ProbabilityType getProbabilityType();
		
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
	 * Train the classifier. 
	 * @param params double array of input data, each row representing one
	 * training value, and each column one parameter value. 
	 * @param truth
	 * @return null if OK, or error string
	 */
	public String trainClassification(double[][]params, int[] group) {
		if (params == null || params.length < 1 || params[0].length < 1) {
			return "FragemntClassifier.trainClassification Null data";
		}
		/**
		 * in jama, m is n rows, n is numb columns
		 */
		Matrix m = new Matrix(params);

		return trainClassification(m, group);
	}

	/** 
	 * 	/**
	 * Train the classifier. 
	 * @param params double array of input data, each row representing one
	 * training value, and each column one parameter value. 
	 * @param matrix matrix of training data (each row one training point, each col one parameter
	 * @param group truth
	 * @return null if OK or error string
	 */
	abstract public String trainClassification(Matrix matrix, int[] group);

	@Override
	public String toString() {
		return getClassifierName();
	}
	
	/**
	 *
	 * @return the classifier name, e.g. Linear Discriminant Analysis
	 */
	public abstract String getClassifierName();

	

	//	/**
	//	 * 
	//	 * @return a double array of class likelihoods. 
	//	 */
	//	public double[] getSpeciesLogLikelyhoods();

	//	/**
	//	 * Gets a list of probabilities for each species. 
	//	 * <p> Generally, these should be normalised so that the
	//	 * total probability comes to 1.
	//	 * 
	//	 * @return a list of probabilities for each species
	//	 */
	//	public double[] getSpeciesProbabilities();
	//	
	//	/**
	//	 * 
	//	 * @return Index of the best species (or -1 if nothing at all recognised)
	//	 */
	//	public int getBestSpeciesBet();


}
