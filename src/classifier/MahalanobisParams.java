package classifier;

import Jama.Matrix;

public class MahalanobisParams extends ClassifierParams {

	private static final long serialVersionUID = 1L;

	protected GroupMeans groupMeans;
	
//	protected Matrix[] rMatrix;
	
	protected Matrix[] rInverse;
	
	protected double[] logDetSigma;
	
	public MahalanobisParams() {
		super(MahalanobisClassifier.class);
	}

}
