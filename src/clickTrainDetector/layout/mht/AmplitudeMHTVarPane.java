package clickTrainDetector.layout.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.ResultConverter;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;

public class AmplitudeMHTVarPane extends SimpleMHTVarPane {

		public AmplitudeMHTVarPane(SimpleChi2VarParams simpleChi2Var, ResultConverter resultsConverter) {
			super(simpleChi2Var, resultsConverter);
			// TODO Auto-generated constructor stub
		}
		
		/**
		 * Create the advanced settings pane. 
		 * @return the advanced settings pane. 
		 */
		@Override
		public AdvMHTVarPane createAdvMHTVarPane(SimpleChi2VarParams simpleChi2VarParams, ResultConverter resultConverter) {
			return new AmplitudeChi2AdvPane(simpleChi2VarParams, resultConverter); 
		}

}
