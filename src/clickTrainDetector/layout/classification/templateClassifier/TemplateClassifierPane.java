package clickTrainDetector.layout.classification.templateClassifier;

import PamController.SettingsPane;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdParams;
import clickTrainDetector.classification.templateClassifier.CTTemplateClassifier;
import clickTrainDetector.classification.templateClassifier.DefualtSpectrumTemplates;
import clickTrainDetector.classification.templateClassifier.DefualtSpectrumTemplates.SpectrumTemplateType;
import clickTrainDetector.classification.templateClassifier.TemplateClassifierParams;
import clickTrainDetector.layout.classification.idiClassifier.IDIPane;
import clickTrainDetector.layout.classification.simplechi2classifier.SimpleCTClassifierPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilsFX.ControlField;

/**
 * Pane which allows users set classification parameters for a template classifier. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class TemplateClassifierPane extends SettingsPane<TemplateClassifierParams> {

	/**
	 * The main pane. 
	 */
	private PamBorderPane mainPane; 

	/**
	 * Simple classifier pane 
	 */
	//private SimpleCTClassifierPane simpleCTClassifierPane;

	/**
	 * Reference to the CT classifer. 
	 */
	private CTTemplateClassifier cTTemplateClassifier;

	/**
	 * The inter-detection settings pane. 
	 */
	//private IDIPane idiPane;

	private TemplateSpectrumPane spectrumTemplatePane;

	/**
	 * The spectrum threshold. 
	 */
	private ControlField<Double> spectrumthreshold;


	public TemplateClassifierPane(CTTemplateClassifier cTTemplateClassifier) {
		super(null);
		this.cTTemplateClassifier=cTTemplateClassifier; 
		
		mainPane = new PamBorderPane();
		
//		ScrollPane scrollPane = new  ScrollPane(createTemplatePane());
//		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		
		mainPane.setCenter(createTemplatePane()); 
		//TEMP
		this.setParams(new TemplateClassifierParams());
		
		
	}

	private Node createTemplatePane() {

//		//create the simple classifier pane. 
//		Label label1 = new Label("X\u00b2 Threshold"); 
////		label1.setFont(PamGuiManagerFX.titleFontSize2);
//		PamGuiManagerFX.titleFont2style(label1);
//
//		simpleCTClassifierPane = new SimpleCTClassifierPane(cTTemplateClassifier.getSimpleCTClassifier()); 
//		
//		//the IDI settings
//		Label label2 = new Label("IDI Settings"); 
////		label2.setFont(PamGuiManagerFX.titleFontSize2);
//		PamGuiManagerFX.titleFont2style(label2);
//
//		idiPane = new IDIPane(); 
		
		//the spectrum template pane.
		//Label label3 = new Label("Spectrum Template"); 
//		label3.setFont(PamGuiManagerFX.titleFontSize2);
		//PamGuiManagerFX.titleFont2style(label3);
		
		// create spinner. 		
		spectrumthreshold = new ControlField<Double>("Spectrum Correlation Threshold", "", 0.0, Double.MAX_VALUE, 0.2);
		spectrumthreshold.getSpinner().getValueFactory().setConverter(PamSpinner.createStringConverter(3));		
		spectrumthreshold.setTooltip(new Tooltip("The threshold for correlation of the spectrum and average click train template"));
		spectrumthreshold.setPadding(new Insets(0,0,10,0));

		spectrumTemplatePane = new TemplateSpectrumPane(); 
		spectrumTemplatePane.setPadding(new Insets(5,5,5,5));
		spectrumTemplatePane.setMaxWidth(Double.POSITIVE_INFINITY);
		spectrumTemplatePane.setMinWidth(50);

//		spectrumTemplatePane.setMouseTransparent(true);

		//make general and spectrum correlation pane. 
		PamVBox generalPane = new PamVBox(); 
		generalPane.setSpacing(5);
//		generalPane.getChildren().addAll(label1, simpleCTClassifierPane.getContentNode(), label2,
//				idiPane); 
		generalPane.setPadding(new Insets(5,5,5,5));

		PamVBox spectrumPane = new PamVBox(); 
		spectrumPane.setSpacing(5);
		spectrumPane.getChildren().addAll(spectrumthreshold, spectrumTemplatePane); 
		spectrumPane.setPadding(new Insets(5,5,5,5));
		
		
//		tabPane.setSide(Side.LEFT);
		
//		//tab pane to hold everything
//		Tab tab1 = new Tab();
//		tab1.setText("General");
//		tab1.setContent(generalPane);
//		tab1.setClosable(false);
//		
//		Tab tab2 = new Tab();
//		tab2.setText("Template");
//		tab2.setContent(spectrumPane);
//		tab2.setClosable(false);
//		
//		TabPane tabPane = new TabPane();
//		tabPane.minWidthProperty().bind(generalPane.widthProperty());
//		tabPane.getTabs().addAll(tab1, tab2);
		
		return new PamVBox(generalPane, spectrumPane); 
	}


	@Override
	public TemplateClassifierParams getParams(TemplateClassifierParams currParams) {

//		currParams.chi2ThresholdParams = simpleCTClassifierPane.getParams(currParams.chi2ThresholdParams); 
//		
//		currParams = idiPane.getParams(currParams); 
		
		currParams.spectrumTemplate = spectrumTemplatePane.getSpectrum(); 
		
		currParams.corrThreshold = this.spectrumthreshold.getValue();
	
		return currParams;
	}

	@Override
	public void setParams(TemplateClassifierParams input) {		
		//TEMP- testing settings classes. 
		//if (input.chi2ThresholdParams==null) input.chi2ThresholdParams=new Chi2ThresholdParams(); 
		if (input.spectrumTemplate==null) input.spectrumTemplate=DefualtSpectrumTemplates.getTemplate(SpectrumTemplateType.DOLPHIN); 

//		simpleCTClassifierPane.setParams(input.chi2ThresholdParams);
//		idiPane.setParams(input);
		spectrumTemplatePane.setSpectrum(input.spectrumTemplate); 
		spectrumthreshold.setValue(input.corrThreshold);
	}

	@Override
	public String getName() {
		return "Template Classifier Pane";
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
