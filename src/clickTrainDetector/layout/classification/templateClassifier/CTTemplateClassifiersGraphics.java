package clickTrainDetector.layout.classification.templateClassifier;

import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.templateClassifier.CTTemplateClassifier;
import clickTrainDetector.classification.templateClassifier.TemplateClassifierParams;
import clickTrainDetector.layout.classification.CTClassifierGraphics;
import javafx.scene.layout.Pane;

/**
 * The main click train classifier graphics. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CTTemplateClassifiersGraphics implements CTClassifierGraphics {
	
	/**
	 * Reference to the CTTemplate classifier. 
	 */
	private CTTemplateClassifier cTTemplateClassifier;
	
	/**
	 * The main settings pane. 
	 */
	private TemplateClassifierPane ctTemplateClssfrPane;

	public CTTemplateClassifiersGraphics(CTTemplateClassifier cTTemplateClassifier) {
		this.cTTemplateClassifier=cTTemplateClassifier; 
	}

	@Override
	public Pane getCTClassifierPane() {
		if (ctTemplateClssfrPane==null) {
			ctTemplateClssfrPane  = new TemplateClassifierPane(cTTemplateClassifier);
		}
		return (Pane) ctTemplateClssfrPane.getContentNode();
	}

	@Override
	public CTClassifierParams getParams() {
		return ctTemplateClssfrPane.getParams(cTTemplateClassifier.getParams());
	}

	@Override
	public void setParams(CTClassifierParams params) {
		ctTemplateClssfrPane.setParams((TemplateClassifierParams) params);
		
	}
	
	

}
