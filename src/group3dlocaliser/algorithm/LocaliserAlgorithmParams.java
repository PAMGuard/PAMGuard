package group3dlocaliser.algorithm;

import java.io.Serializable;

/**
 * This is a wrapper around any type of algorithm parameters so that
 * they can be easily serialised into a single hash table in the main 
 * group localiser parameters section. 
 * <br> the algorithm provider class will have to check that these
 * are of the right type before casting. 
 * @author dg50
 *
 */
public class LocaliserAlgorithmParams implements Serializable {

	public static final long serialVersionUID = 1L;
		
	private Serializable algorithmParameters;
	
	private Serializable xtraSourceParameters;

	/**
	 * @param algorithmParameters
	 */
	public LocaliserAlgorithmParams(Serializable algorithmParameters) {
		super();
		this.algorithmParameters = algorithmParameters;
	}

	public LocaliserAlgorithmParams() {
		super();
	}
	
	/**
	 * @return the algorithmParameters
	 */
	public Serializable getAlgorithmParameters() {
		return algorithmParameters;
	}

	/**
	 * @param algorithmParameters the algorithmParameters to set
	 */
	public void setAlgorithmParameters(Serializable algorithmParameters) {
		this.algorithmParameters = algorithmParameters;
	}

	/**
	 * @return the xtraSourceParameters
	 */
	public Serializable getXtraSourceParameters() {
		return xtraSourceParameters;
	}

	/**
	 * @param xtraSourceParameters the xtraSourceParameters to set
	 */
	public void setXtraSourceParameters(Serializable xtraSourceParameters) {
		this.xtraSourceParameters = xtraSourceParameters;
	}

}
