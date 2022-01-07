/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package Stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import PamguardMVC.debug.Debug;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Performs a multinomial logistic regression using WEKA library
 * 
 * @author mo55
 *
 */
public class LogRegWeka {
	
	/** the training data set */
	private Instances dataRaw = null;
	
	/** the logistic regression classifier */
	private Logistic logRegClassifier = null;
	
	/** the number of attributes used to train the classifier */
	private int numAtt = 0;
	
	/** the number of classes */
	private int numClass = 0;

	private String modelError;

	/**
	 * Main Constructor - create a Multinomial Logistic Regression Model based on the passed training data, using the WEKA library
	 * 
	 */
	public LogRegWeka() {
	}
	
	/**
	 * Logistic regression where y variable i spassed in as a single array (e.g. array 
	 * of ranges) and y Variable may not be 0's or 1's. 
	 * @param xVar
	 * @param yVar
	 * @return
	 */
	public boolean setTrainingData(double[] xVar, int[] yVar) {
		double[][] xV2 = new double[xVar.length][1];
		double[] yV2 = new double[xVar.length];
		for (int i = 0;i < xVar.length; i++) {
			xV2[i][0] = xVar[i];
			yV2[i] = yVar[i] > 0 ? 1 : 0;
		}
		return setTrainingData(xV2, yV2);
	}
	
	/**
	 * Logistic regression where y variable i spassed in as a single array (e.g. array 
	 * of ranges) and y Variable may not be 0's or 1's. 
	 * @param xVar
	 * @param yVar
	 * @return
	 */
	public boolean setTrainingData(double[] xVar, double[] yVar) {
		double[][] xV2 = new double[xVar.length][1];
		double[] yV2 = new double[xVar.length];
		for (int i = 0;i < xVar.length; i++) {
			xV2[i][0] = xVar[i];
			yV2[i] = yVar[i] > 0 ? 1 : 0;
		}
		return setTrainingData(xV2, yV2);
	}
	
	/**
	 * Take the training data passed in, convert to something that WEKA understands and then create a logistic regression model
	 * 
	 * @param x a 2d array of training data.  Columns are any number of input variables (x1, x2, x3... aka attributes) and rows are data points
	 * @param y the output variable.  Length of array should match number of rows in x parameter.  Since this is a logistic
	 * regression, the output is considered 'nominal' and not numeric - a distinct classification, and not a continuous variable.  It's odd that
	 * nominal values should be passed as doubles, but that's what WEKA wants.  For best results, use continuous integers starting at 0
	 *  - e.g. 0, 1, 2, 3 etc.  
	 * <br>Also, there can't be any gaps in the output of the training dataset - you can't have 0, 1, 2, 4.  WEKA will throw an error.  
	 * <br>Keep track in your own code
	 * of what each value represents (e.g. for a binomial problem, 0=yes and 1=no; for a weather problem, 0=cold, 1=warm, 2=hot, etc).  
	 * @return true=successful, false=unsuccessful
	 * 
	 */
	public boolean setTrainingData(double[][] x, double[] y) {
		return (this.convertArrayData(x, y));
	}
		
	/**
	 * Takes the passed data arrays and converts to an Instances object - something that WEKA can understand.  Once the Instances object is
	 * created, a new regression model is made.
	 *  
	 * @param x a 2d array of training data.  Columns are any number of input variables (x1, x2, x3... aka attributes) and rows are data points
	 * @param y the output variables.  Length of array should match number of rows in x parameter.  Since this is a logistic
	 * regression, the output is considered 'nominal' and not numeric - a distinct classification, and not a continuous variable.  It's odd that
	 * nominal values should be passed as doubles, but that's what WEKA wants.  For best results, use continuous integers starting at 0
	 *  - e.g. 0, 1, 2, 3 etc.  
	 * <br>Also, there can't be any gaps in the output of the training dataset - you can't have 0, 1, 2, 4.  WEKA will throw an error.  
	 * <br>Keep track in your own code
	 * of what each value represents (e.g. for a binomial problem, 0=yes and 1=no; for a weather problem, 0=cold, 1=warm, 2=hot, etc).  
	 */
	private boolean convertArrayData(double[][] x, double[] y) {
		
		// create a list of attributes - one attribute for each input variable, and the last attribute for the class (output)
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		if (x == null || x.length == 0) {
			return false;
		}
		int numData = x.length;
		numAtt = x[0].length;
		for (int i=0; i<numAtt; i++) {
			String attName = "x" + i;
			attributes.add(new Attribute(attName));
		}
		Set<String> uniqY = new TreeSet<String>();	// create a TreeSet to hold the unique values in y as Strings, and sort them in order right away
		for (int i=0; i<numData; i++) {
			uniqY.add(String.valueOf(y[i]));	// add each value to the Set (duplicates will be ignored)
		}
		List<String> predValues = new ArrayList<String>(uniqY);	// convert the set back to a List
		attributes.add(new Attribute("y",predValues));
		numClass = predValues.size();
		dataRaw = new Instances("LogisticRegression", attributes , numData);
		dataRaw.setClassIndex(dataRaw.numAttributes() - 1); 

		// once the attributes have been defined, create an Instance out of each data point and load it into the dataRaw variable
		for (int i=0; i<numData; i++) {
			Instance instance = new DenseInstance(numAtt+1);
			for (int j=0; j<numAtt; j++) {
				instance.setValue(j, x[i][j]);
			}
			instance.setValue(numAtt, y[i]);
		    dataRaw.add(instance);
		}
		return (this.createModel());
	}
	
	/**
	 * Create a regression model based on the current dataRow Instances object
	 */
	private boolean createModel() {
		if (dataRaw == null) {
			System.out.println("Cannot create Logistic Regression model - no data available");
			return false;
		}
		logRegClassifier = new Logistic();
		try {
			logRegClassifier.buildClassifier(dataRaw);
		} catch (Exception e) {
			Debug.out.println("Error building Logistic Regression classifier: " + e.getMessage());
			modelError = e.getMessage();
			logRegClassifier = null;
			return false;
		}
		modelError = null;
		double[][] coeffs = logRegClassifier.coefficients();
		for (int i = 0; i < coeffs.length; i++) {
			Debug.out.println("Model coefficients "+ i + " = " + Arrays.toString(coeffs[i]));
		}
		return true;
	}

	/**
	 * Get a prediction (output variable) based on the passed input array.  The order of the elements in
	 * the x array must match the order that was used in the training data.  The Double output references
	 * the unique values that were used in the training data (0, 1, 2, etc).
	 * 
	 * @param x an array containing the input variables to use in the regression
	 * @return
	 */
	public Double getPrediction(double[] x) {
		if (logRegClassifier == null) {
			System.out.println("Cannot predict value - no Logistic Regression model available");
			return null;
		}
		if (x.length != numAtt) {
			System.out.println(String.format("Incorrect number of attributes for logistic regression - expecting %d but given %d", numAtt, x.length));
			return null;
		}
		Double prediction = null;
		Instance toClassify = convertArrayToInstance(x);
		try {
			double[] distr = logRegClassifier.distributionForInstance(toClassify);
			prediction = distr[1];
		} catch (Exception e) {
			e.printStackTrace();
			prediction = null;
		}
//		logRegClassifier.
//		String[] options = logRegClassifier.getOptions();
//		double ridge = logRegClassifier.getRidge();
		return prediction;
	}
	
	
	
	/**
	 * Passing the input array into the current regression model, a double array is
	 * passed back which contains the percentage values for each of the possible output classifications.
	 * Thus, if there are 3 potential classes (0, 1 and 2) then the method will return a 3-element array
	 * with a percentage in each index corresponding to the probability of the input variable falling into
	 * the corresponding category.
	 * 
	 * @param x an array containing the input variables to use in the regression
	 * @return
	 */
	public double[] getDistribution(double[] x) {
		if (logRegClassifier == null) {
			System.out.println("Cannot predict value - no Logistic Regression model available");
			return null;
		}
		if (x.length != numAtt) {
			System.out.println(String.format("Incorrect number of attributes for logistic regression - expecting %d but given %d", numAtt, x.length));
			return null;
		}
		double[] distribution = null;
		Instance toClassify = convertArrayToInstance(x);
		try {
			distribution = logRegClassifier.distributionForInstance(toClassify);
		} catch (Exception e) {
			e.printStackTrace();
			distribution = null;
		}
		return distribution;
	}
	
	/**
	 * Create an instance from the passed double array.
	 * 
	 * @param x an array containing the input variables to use in the regression
	 * @return
	 */
	private Instance convertArrayToInstance(double[] x) {
		if (x.length != numAtt) {
			System.out.println(String.format("Incorrect number of attributes for logistic regression - expecting %d but given %d", numAtt, x.length));
			return null;
		}
		Instance toClassify = new DenseInstance(numAtt+1);
		toClassify.setDataset(dataRaw);
		for (int i=0; i<numAtt; i++) {
			toClassify.setValue(i, x[i]);
		}
		return toClassify;
	}

	/**
	 * Return the coefficients of the classifier
	 * 
	 * @return
	 */
	public double[][] getCoefficients() {
		if (logRegClassifier == null) {
			return null;
		}
		return logRegClassifier.coefficients();
	}
	
	public double[][] getCoeffUncertainty() {
		if (logRegClassifier == null) {
			return null;
		}
//		logRegClassifier.
		return null;
	}

	/**
	 * <p><strong>Important: this method ONLY works for binomial (2-class) datasets with
	 * a single attribute x, and will return null if that is not true</strong></p>
	 * 
	 * <p>Given the probability p of classification as the second class, this method returns
	 * the attribute x required.  If interested in the probability of classification as
	 * the first class, pass the value 1-p instead.</p>
	 * 
	 * <p>The equation solved is P = 1 / (1 + e<sup>-(b0 + b1x)</sup>), where P is the probability
	 * desired for the second class, b0 and b1 are the coefficients, and x is the value that
	 * is solved for.  Let's say there are 2 possible classes: undetected (y=0) and detected (y=1), and
	 * there is a single dependent attribute 'range'.  If we want to know the range required for
	 * a 70% probability of classification as detected (y=1), we would call this method and
	 * pass it 0.7.  If we wanted to know the range required for a 70% probability of classification
	 * as undetected (y=0), we would call this method and pass it 0.3.</p>
	 * 
	 * @param p the probability desired
	 * @return a Double value for the attribute, or null if this method fails
	 * @throws ArithmeticException thrown if the attribute calculation returns infinity or NaN
	 */
	public Double getAttFromProb(double p) throws ArithmeticException {
		if (numClass!=2 || numAtt!=1) {
			System.out.println("Error - this method only works on binomial (2-class) datasets with a single attribute");
			return null;
		}
		double[][] coeffs = this.getCoefficients();
		if (coeffs == null) {
			return null;
		}
		double xAtt = (Math.log(p/(1-p))-coeffs[0][0])/coeffs[1][0];	// this calc is why it only works for 2-class 1-attribute classifiers
		if (Double.isNaN(xAtt) || Double.isInfinite(xAtt)) {
			String mess = "Error calculating attribute, value = " + xAtt;
			throw new ArithmeticException(mess);
		}
		return xAtt;
	}
	
	/**
	 * Test model using data from https://machinelearningmastery.com/logistic-regression-tutorial-for-machine-learning/
	 * 
	 * @param args
	 */
	public static void main(String[] args){
//		double[][] xVals = {
//				{2.7810836,	2.550537003},
//				{1.465489372, 2.362125076},
//				{3.396561688, 4.400293529},
//				{1.38807019, 1.850220317},
//				{3.06407232, 3.005305973},
//				{7.627531214, 2.759262235},
//				{5.332441248, 2.088626775},
//				{6.922596716, 1.77106367},
//				{8.675418651, -0.2420686549},
//				{7.673756466, 3.508563011}
//		};
//		double[] yVals = {0,0,0,0,0,1,1,1,1,1};
		// some test data - unfortunately the website doesn't have test data so I'm guessing as to
		// which category each should fall into, but the results seem to make sense if
		// you imagine a diagonal line separating category 0 and category 1
//		double[][] xPred = {
//				{1,	3},
//				{2, 1},
//				{3, 4},
//				{3, -1},
//				{4, 4},
//				{4, -1},
//				{5, 1},
//				{6, 2},
//				{6, 5.5},
//				{7, 10}
//		};
		
		// Test this out with a simple 1D dataset, then try to calculate the coefficients
		double[][] xVals = new double[10][1];
		for (int i=0; i<10; i++) {
			xVals[i][0]=i+1;
		}
		double[] yVals = {0,0,0,0,0,1,1,1,1,1};
		LogRegWeka lrw = new LogRegWeka();
		boolean success = lrw.setTrainingData(xVals, yVals);
		

		
		double[][] xPred = new double[10][1];
		for (int i=0; i<10; i++) {
			xPred[i][0] = 5 + i/10.0;
		}
		
		

//		double[] x_B0 = {0};
//		double[] distB0 = lrw.getDistribution(x_B0);
//		double B0 = Math.log(distB0[1]/(1-distB0[1]));
//		System.out.println("B0 = " + B0);
//		double[] x_B1 = {1};
//		double[] distB1 = lrw.getDistribution(x_B1);
//		double B1 = Math.log(distB1[1]/(1-distB1[1]))-B0;
//		System.out.println("B1 = " + B1);
		
		for (int i=0; i<xPred.length; i++) {
//			System.out.println("Values are " + xPred[i][0] + " and " + xPred[i][1]);
			System.out.print("\nValue is " + xPred[i][0]);
//			Double pred = lrw.getPrediction(xPred[i]);
//			System.out.println("Prediction is " + pred);
			double[] dist = lrw.getDistribution(xPred[i]);
			Debug.out.print(" Distribution is ");
			for (int j=0; j<dist.length; j++) {
				Debug.out.print(dist[j] + " - ");
			}
//			double xCalc = B0+B1*xPred[i][0];
//			double PCalc = 1/(1+Math.exp(-1*xCalc));
		}
		double xDesired = lrw.getAttFromProb(0.5);
		Debug.out.println("\nFor p=0.5 we need x = " + xDesired);
		Debug.out.println("\r\n************************");
		
//		double[] coeffs = lrw.getCoefficients();
//		for (int i=0; i<coeffs.length; i++) {
//			System.out.println("\nCoefficient " + i + " = " + coeffs[i]);
//		}
//		System.out.println("\r\n************************");
		
		double[][] coeffs2 = lrw.getCoefficients();
		for (int i=0; i<coeffs2.length; i++) {
			for (int j=0; j<coeffs2[0].length; j++) {
				Debug.out.println("\nCoefficient " + i + "," + j + " = " + coeffs2[i][j]);
			}
		}
		Debug.out.println("\r\n************************");
	}

	/**
	 * Return exception string from model if it failed to fit. 
	 * @return the modelError
	 */
	public String getModelError() {
		return modelError;
	}
		
		
}
