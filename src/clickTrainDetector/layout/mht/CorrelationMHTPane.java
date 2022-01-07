package clickTrainDetector.layout.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.ResultConverter;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;

public class CorrelationMHTPane extends SimpleMHTVarPane {

	
	public CorrelationMHTPane(SimpleChi2VarParams simpleChi2Var) {
		super(simpleChi2Var);
	}
	
	/**
	 * Create the advanced settings pane. 
	 * @return the advanced settings pane. 
	 */
	@Override
	public AdvMHTVarPane createAdvMHTVarPane(SimpleChi2VarParams simpleChi2VarParams, ResultConverter resultConverter) {
		return new CorrelationAdvMHTPane(simpleChi2VarParams); 
	}
	
//	@Override
//	public void setParams(SimpleChi2VarParams simpleChi2VarParams) {
//		
//		this.simpleChi2VarParams = simpleChi2VarParams; 
//
//		currentInput = simpleChi2VarParams.clone(); 
//	
//		this.slider.setValue(error2Slider(simpleChi2VarParams.error, simpleChi2VarParams.errorScaleValue));
//		
//		System.out.println("AdvCorrPane: "  + getMhtAlgorithm());
//		
//		this.advPane.setParams(simpleChi2VarParams.clone());
//		
//	}
	


}
