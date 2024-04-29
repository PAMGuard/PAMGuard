package rawDeepLearningClassifier.dlClassification.delphinID;

import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pamViewFX.fxNodes.PamVBox;

/**
 * Settings pane for delphin ID. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DelphinIDPane  extends SettingsPane<DelphinIDParams> {
	
	/**
	 * The main pane. 
	 */
	private Pane mainPane;
	
	/**
	 * Reference to the delphinID classifier 
	 */
	private DelphinIDClassifier delphinUIClassifier;

	public DelphinIDPane(DelphinIDClassifier delphinUIClassifier) {
		super(null);
		this.delphinUIClassifier = delphinUIClassifier; 
		mainPane =  createPane();
	}
	
	private Pane createPane() {
		

		//font to use for title labels. 
		Font font= Font.font(null, FontWeight.BOLD, 11);

		Node classifierIcon;
		classifierIcon = delphinUIClassifier.getModelUI().getIcon();
	
		
		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5.);
		
		/**Classification thresholds etc to set.**/
		Label classiferInfoLabel2 = new Label("Decision Threshold"); 
		classiferInfoLabel2.setTooltip(new Tooltip("Set the minimum prediciton value for selected classes. If a prediction exceeds this value "
				+ "a detection will be saved."));
		classiferInfoLabel2.setFont(font);
		
		
		vBox.getChildren().addAll(classifierIcon, classiferInfoLabel2);
	
		return vBox;
	}

	@Override
	public DelphinIDParams getParams(DelphinIDParams currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(DelphinIDParams input) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getName() {
		return "delphinIDParams";
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
