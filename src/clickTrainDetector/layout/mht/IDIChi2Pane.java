package clickTrainDetector.layout.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.ResultConverter;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;

/**
 * The IDI Chi2 Pane. 
 * @author au671271
 *
 */
public class IDIChi2Pane extends SimpleMHTVarPane {
	
	
	public IDIChi2Pane(SimpleChi2VarParams simpleChi2Var, ResultConverter resultConverter) {
		super(simpleChi2Var, resultConverter);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Create the advanced settings pane. 
	 * @return the advanced settings pane. 
	 */
	@Override
	public AdvMHTVarPane createAdvMHTVarPane(SimpleChi2VarParams simpleChi2VarParams, ResultConverter resultConverter) {
		return new ICIChi2AdvPane(simpleChi2VarParams, resultConverter); 
	}


}