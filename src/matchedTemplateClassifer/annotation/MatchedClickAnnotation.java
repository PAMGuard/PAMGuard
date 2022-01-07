package matchedTemplateClassifer.annotation;

import java.util.ArrayList;
import java.util.List;

import PamView.symbol.SymbolData;
import annotation.DataAnnotation;
import matchedTemplateClassifer.MatchedTemplateResult;

/**
 * Annotation for the matched click classifier. Holds data on the threshold value, 
 * the match correlation and reject correlation. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class MatchedClickAnnotation extends DataAnnotation<MatchedClickAnnotationType> {
	
	/**
	 * The classifiers that the click passed. e.g. [1 3 5] would mean that
	 * the click was classified by classifiers returning types 1 3 and 5. 
	 */
	private List<MatchedTemplateResult> matchedTemplateResult;
	
	private MatchedClickAnnotationType matchedClickType; 
	
	/**
	 * 
	 * Get the matched template classifier result for the data annotation
	 * @param matchedClickType - the annotation type. 
 	 * @param matchedTemplateResult the matched template result. 
	 */
	public MatchedClickAnnotation(MatchedClickAnnotationType matchedClickType, 
			MatchedTemplateResult matchedTemplateResult) {
		super(matchedClickType);
		this.matchedClickType=matchedClickType; 
		this.matchedTemplateResult = new ArrayList<MatchedTemplateResult>();
		this.matchedTemplateResult.add(matchedTemplateResult);
	}	
	
	
	/**
	 * 
	 * Get the matched template classifier result for the data annotation
	 * @param matchedClickType - the annotation type. 
 	 * @param matchedTemplateResult the matched template result. 
	 */
	public MatchedClickAnnotation(MatchedClickAnnotationType matchedClickType, 
			List<MatchedTemplateResult> matchedTemplateResult) {
		super(matchedClickType);
		this.matchedClickType=matchedClickType; 
		this.matchedTemplateResult= matchedTemplateResult;
	}	


	public List<MatchedTemplateResult> getMatchedTemplateResult() {
		return matchedTemplateResult;
	}

	/**
	 * Set the matched template classifier result for the annotation.
	 * @param matchedTemplateResult - the matched template results
	 */
	public void setMatchedTemplateResult(List<MatchedTemplateResult> matchedTemplateResult) {
		this.matchedTemplateResult = matchedTemplateResult;
	}
	
	/**
	 * Set the matched template classifier result for the annotation.
	 * @param matchedTemplateResult - the matched template result. 
	 */
	public void setMatchedTemplateResult(MatchedTemplateResult matchedTemplateResult) {
		this.matchedTemplateResult = new ArrayList<MatchedTemplateResult>();
		this.matchedTemplateResult.add(matchedTemplateResult);
	}

	/**
	 * Convenience class to grab symbol data from match click classifier. 
	 * @param clickType - the click type
	 * @return
	 */
	public SymbolData getSymbolData(byte clickType) {
		//System.out.println("Hello colour my click: "  + matchedClickType.getMTControl());
		return matchedClickType.getMTControl().getSymbolData(clickType);
	}
	
	/**
	 * Get the type flag for the matched click classifier. 
	 * @return the matched click classifier type. 
	 */
	public byte getClickType() {
		return matchedClickType.getMTControl().getMTParams().type; 
	}
	
	@Override
	public String toString() {
		String results = "<html><p> "; 
		for (int i=0; i<this.matchedTemplateResult.size(); i++) {
			results += String.format("Template %d: Correlation Threshold: %.4f Match Corr: %.4f Reject Corr: %.4f\n <p>", i, matchedTemplateResult.get(i).threshold,
				matchedTemplateResult.get(i).matchCorr, matchedTemplateResult.get(i).rejectCorr);
		}
		results += "<html>"; 
		return results;
	}


	

}
