package matchedTemplateClassifer;

/**
 * Class to hold results from the matched click classifier
 * @author Jamie Macaulay 
 *
 */
public class  MatchedTemplateResult{

	/**
	 * The threshold of the match and reject template correlation. 
	 */
	public double threshold; 
	
	/**
	 * The threshold of the match and reject template correlation. 
	 */
	public double matchCorr; 

	/**
	 * The threshold of the match and reject template correlation. 
	 */
	public double rejectCorr; 
	
	public MatchedTemplateResult(Double threshold) {
		this.threshold=threshold; 
	}


	public MatchedTemplateResult() {
		// TODO Auto-generated constructor stub
	}

}
