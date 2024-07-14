package rawDeepLearningClassifier.dataSelector;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 *  Settings pane for filtering deep learning results by class prediciton. 
 */
public class DLPredictonPane extends DynamicSettingsPane<DLPredictionFilterParams>{
	
	private DLPredictionFilter predicitonFilter;
	
	private PamVBox mainPane;

	public DLPredictonPane(DLPredictionFilter predicitonFilter) {
		super(null);
		this.predicitonFilter=predicitonFilter;
		
		createPane();
	}

	
	private void createPane() {
		mainPane = new PamVBox();
		mainPane.setSpacing(5);
	}
	
	class ClassDataSelector extends PamHBox {
		
		PamSpinner<Double> spinner;
		
		CheckBox enable;
		
		ClassDataSelector(String classType, int index) {
		
			enable = new CheckBox(classType);
			spinner = new PamSpinner<Double>(0., 1., 0.7, 0.1);
			spinner.setEditable(true);
			
			this.getChildren().addAll(getChildrenUnmodifiable());
		}
	}
	
	
	@Override
	public DLPredictionFilterParams getParams(DLPredictionFilterParams currParams) {
		// TODO Auto-generated method stub
		return currParams;
	}

	@Override
	public void setParams(DLPredictionFilterParams input) {

		
	}

	@Override
	public String getName() {
		return "Deep learning prediciton filter";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
