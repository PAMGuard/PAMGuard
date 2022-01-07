package classifier;

import Jama.Matrix;

public class LinearClassifierParams extends ClassifierParams {

	private static final long serialVersionUID = 1L;

	protected GroupMeans groupMeans;
	
	protected Matrix rMatrix;
	
	protected double logDetSigma;
	
	public LinearClassifierParams() {
		super(LinearClassifier.class);
	}

}
