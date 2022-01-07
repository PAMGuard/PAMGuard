package Localiser.detectionGroupLocaliser;

public interface LocalisationChi2 {
	
	/**
	 * 
	 * @return A Chi2 value for the localisation
	 */
	public Double getChi2();
	
	/**
	 * 
	 * @return The number of degrees of freedom in the model used
	 */
	public Integer getNDF();
	
	/**
	 * 
	 * @return the AIC. 
	 */
	public Double getAic();
	
}
