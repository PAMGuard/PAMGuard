package classifier;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Enumeration;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import Jama.Matrix;

abstract public class AbstractWekaClassifier extends Classifier {

	private weka.classifiers.AbstractClassifier wekaClassifier;;
	private Instances data;
	//	private Enumeration wekaOptions;

	/**
	 * 
	 */
	public AbstractWekaClassifier() {
		super();
	}

	/**
	 * @return the wekaClassifier
	 */
	public weka.classifiers.AbstractClassifier getWekaClassifier() {
		return wekaClassifier;
	}

	/**
	 * @param wekaClassifier the wekaClassifier to set
	 */
	public void setWekaClassifier(weka.classifiers.AbstractClassifier wekaClassifier) {
		this.wekaClassifier = wekaClassifier;
	}

	@Override
	public Matrix getLogLikelihoodsM() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix getProbabilitiesM() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProbabilityType getProbabilityType() {
		return ProbabilityType.NORMALISED;
	}

	@Override
	public int[] runClassification(Matrix data) {
		return runClassification(data.getArray());
	}

	@Override
	public int runClassification(double[] params) {
		int nCol = params.length;
		Instance instance = new DenseInstance(nCol);
		for (int j = 0; j < nCol; j++) {
			instance.setValue(j, params[j]);
		}
		instance.setDataset(data);
		double[] output;
		try {
			output = wekaClassifier.distributionForInstance(instance);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		int bestInd = -1;
		double bestVal = -1;
		for (int i = 0; i < output.length; i++) {
			if (output[i] > bestVal && output[i] >= minimumProbability) {
				bestVal = output[i];
				bestInd = i;
			}
		}

		return bestInd;
	}


	@Override
	public int[] runClassification(double[][] params) {
		int n = params.length;
		int[] output = new int[n];
		for (int i = 0; i < n; i++) {
			output[i] = runClassification(params[i]);
		}
		return output;
	}

	@Override
	public ClassifierParams getClassifierParams() {
		WekaClassifierParams params = new WekaClassifierParams();
		params.setClassifier(getWekaClassifier());
		//			params.setWekaOptions(getWekaClassifier().getOptions());
		return params;
	}

	@Override
	public boolean setClassifierParams(ClassifierParams classifierParams) {
		if (WekaClassifierParams.class.isAssignableFrom(classifierParams.getClass()) == false) {
			return false;
		}
		WekaClassifierParams wp = (WekaClassifierParams) classifierParams;
		// check it is the right type of classiifer
		if (wp.getClassifier().getClass() != wekaClassifier.getClass()) {
			return false;
		}

		setWekaClassifier(wp.getClassifier());
		return true;
	}

	@Override
	public String trainClassification(Matrix matrix, int[] group) {
		int nCol = matrix.getColumnDimension();
		int nRow = matrix.getRowDimension();
		double[][] matrixData = matrix.getArray();
		Instance instance;

		FastVector wAttrs = new FastVector(nCol+1);
		for (int i = 0; i < nCol; i++) {
			wAttrs.addElement(new Attribute(String.format("Attr%d", i)));
		}  

		/**
		 * Seems we need a list of unique id's. 
		 */
		ArrayList<Integer> uniqueIds = new ArrayList<Integer>();
		for (int i = 0; i < group.length; i++) {
			if (uniqueIds.contains(group[i])) {
				continue;
			}
			uniqueIds.add(group[i]);
		}
		String cl;
		FastVector fvClassVal = new FastVector(uniqueIds.size());
		for (int i = 0; i < uniqueIds.size(); i++) {
			cl = String.format("%d", uniqueIds.get(i));
			fvClassVal.addElement(cl);
		}
		//		fvClassVal.addElement("Class0");
		Attribute ClassAttribute = new Attribute("theClass", fvClassVal);
		wAttrs.addElement(ClassAttribute);		

		data = new Instances("Training data", wAttrs, nRow);

		double[] aRow;
		for (int i = 0; i < nRow; i++) {
			aRow = matrixData[i];
			instance = new DenseInstance(nCol+1);
			instance.setDataset(data);
			//			instance.s
			//			instance.setClassValue(group[i]);
			for (int j = 0; j < nCol; j++) {
				instance.setValue(j, aRow[j]);
			}
			cl = String.format("%d", group[i]);
			instance.setValue(nCol, cl);
			//			instance.setClassValue(cl);
			data.add(instance);
		}
		data.setClassIndex(nCol);
		try {
			wekaClassifier.buildClassifier(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean hasParamsDialog() {
		Enumeration wekaOptions = wekaClassifier.listOptions();
		if (wekaOptions == null) {
			return false;
		}
		return wekaOptions.hasMoreElements();
	}


	@Override
	public boolean showParamsDialog(Window parent) {
		return WekaOptionsDialog.showDialog(parent, this);
	}
}
