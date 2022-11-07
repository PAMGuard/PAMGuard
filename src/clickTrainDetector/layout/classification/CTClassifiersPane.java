package clickTrainDetector.layout.classification;

import java.util.ArrayList;

import PamUtils.PamArrayUtils;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainParams;
import clickTrainDetector.classification.CTClassifierParams;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamTabPane;
import warnings.PamWarning;

/**
 * 
 * Pane which allows users to create and change settings for multiple click train classifiers. 
 * 
 * @author Jamie Macaulay
 *
 */
public class CTClassifiersPane extends PamBorderPane {

	/**
	 * Enable the classifier box
	 */
	private CheckBox enableClassifierBox;
	
	
	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl;

	
	/**
	 * Tab pane where each tab is a different classifier. 
	 */
	private PamTabPane pamTabPane;


	public CTClassifiersPane(ClickTrainControl clickTrainControl) {
		this.clickTrainControl = clickTrainControl; 
		this.setCenter(createClassifierPane()); 
	}


	/**
	 * Create the template classifier pane. 
	 * @return the template classifier pane. 
	 */
	private Pane createClassifierPane() {

		// enable the classifier. 
		enableClassifierBox = new CheckBox("Enable Click Train Classification"); 
		enableClassifierBox.setOnAction(action ->{
			enableClassifierPane(enableClassifierBox.isSelected()); 
		});
		
		// enable the classifier. 
		enableClassifierBox = new CheckBox("Enable Click Train Classification"); 
		enableClassifierBox.setOnAction(action ->{
			enableClassifierPane(enableClassifierBox.isSelected()); 
		});
				
		//with just one classifier.
		pamTabPane = new PamTabPane(); 
		pamTabPane.setAddTabButton(true);
//		pamTabPane.getAddTabButton().setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADD, PamGuiManagerFX.iconSize));
		pamTabPane.getAddTabButton().setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", PamGuiManagerFX.iconSize));
		pamTabPane.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		pamTabPane.getAddTabButton().setTooltip(new Tooltip(
				"Add a new classifier."));

		pamTabPane.getAddTabButton().setOnAction((action)->{
			CTClassifierPane clssfrPane = new CTClassifierPane(clickTrainControl);
			clssfrPane.setDefaultSpeciesID(pamTabPane.getTabs().size()+1);
			ClassifierTab tab = new ClassifierTab(clssfrPane, pamTabPane.getTabs().size());
					
			pamTabPane.getTabs().add(tab); 
		});
		
		pamTabPane.setTabMinWidth(70);

		//list change listener to make sure cannot close the last tab (otherwise pane disappears)
		pamTabPane.getTabs().addListener((ListChangeListener<? super Tab>) c->{
			if (pamTabPane.getTabs().size()==1) {
				pamTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
			}
			else {
				pamTabPane.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
			}
		});
		
		// create the first classifier pane. 		
		ClassifierTab tab = new ClassifierTab(new CTClassifierPane(clickTrainControl), 0); 
		pamTabPane.getTabs().add(tab); 

		PamBorderPane holder = new PamBorderPane(); 
		holder.setTop(enableClassifierBox);
		holder.setCenter(pamTabPane);

		enableClassifierPane(enableClassifierBox.isSelected()); 

		enableClassifierPane(enableClassifierBox.isSelected()); 
		
		return holder; 
	}

	/**
	 * Enable or disable classifier pane
	 * @param enable - true for enabled pane
	 */
	private void enableClassifierPane(boolean enable) {
		pamTabPane.setDisable(!enable);
	}



	/**

	 * Class to hold a classifier pane inside a tab. 
	 * 
	 * @author Jamie Macaulay 
	 *
	 */
	private class ClassifierTab extends Tab {

		/**
		 * 	Reference to the ct classifier pane.
		 */
		private CTClassifierPane ctClassifierPane;

		/**
		 * Constructor the Match tab
		 * @param ctClassifierPane
		 * @param listPos
		 */
		public ClassifierTab(CTClassifierPane tmpltClassifierPane, int listPos) {
			super("Tab");
			this.ctClassifierPane = tmpltClassifierPane;
			this.setContent(tmpltClassifierPane);
			tmpltClassifierPane.getNameField().setText("Classifer " + (listPos+1));
			
			this.textProperty().bind(tmpltClassifierPane.getNameField().textProperty());
		}

		/**
		 * Get the matched template classifier pane associated with the tab
		 * @return the match template classifier pane. 
		 */
		public CTClassifierPane getCTClassifierPane() {
			return ctClassifierPane;
		}

	}


	/**
	 * Get parameters from the classifiers pane. 
	 * @param currParams - the parameters to set parameters for.
	 * @return parameters with variables changed. 
	 */
	public ClickTrainParams getParams(ClickTrainParams currParams) {
		//		currParams.runClassifier = this.enableClassifierBox.isSelected();
		//GET all the classifiers form the different tabs!
		ArrayList<CTClassifierParams> ctClassifierParams = new 	ArrayList<CTClassifierParams> (); 

		CTClassifierParams aClassifierParams; 
		
		double[] speciesCodeList = new double[pamTabPane.getTabs().size()]; 
		for (int i=0; i<pamTabPane.getTabs().size(); i++) {
			aClassifierParams = ((ClassifierTab) pamTabPane.getTabs().get(i)).getCTClassifierPane().getParams(); 
			ctClassifierParams.add(aClassifierParams);
		}

		if (!PamArrayUtils.unique(speciesCodeList) && speciesCodeList.length>1) {
			System.err.println("CTClassifiersPane: The species codes are not unique");
			PamArrayUtils.printArray(speciesCodeList);

			this.clickTrainControl.getWarningManager().addWarning(new PamWarning("Click train classifier",
					"There are two species ID's which are the same",2)); 
			return null; 
		}

		CTClassifierParams[] newArray = new CTClassifierParams[ctClassifierParams.size()]; 
		currParams.ctClassifierParams = ctClassifierParams.toArray(newArray); 
		currParams.runClassifier = this.enableClassifierBox.isSelected();

		return currParams;
	}

	/**
	 * Set the parameters. 
	 * @param clickTrainParams - click train parameters 
	 */
	public void setParams(ClickTrainParams clickTrainParams) {

		enableClassifierBox.setSelected(clickTrainParams.runClassifier);

		ClassifierTab tab;
		if (clickTrainParams.ctClassifierParams==null || clickTrainParams.ctClassifierParams.length<1) {
			if (pamTabPane.getTabs().size()<1) {
				tab = new ClassifierTab(new CTClassifierPane(clickTrainControl), pamTabPane.getTabs().size());
				pamTabPane.getTabs().add(tab); 
			}
		}
		else {
			//could just wipe tabs and start again but that means recreating quite a lot of 
			//pane which is a little clunky and takes a little while longer to open. Instead iterate through tabs.

			//if there are more tabs then remove the extra tabs
			if (clickTrainParams.ctClassifierParams.length<pamTabPane.getTabs().size()) {
				pamTabPane.getTabs().remove(clickTrainParams.ctClassifierParams.length, pamTabPane.getTabs().size()-1);
			}

			for (int i=0; i<clickTrainParams.ctClassifierParams.length; i++) {
				if (pamTabPane.getTabs().size()<=i) {
					tab = new ClassifierTab(new CTClassifierPane(clickTrainControl), pamTabPane.getTabs().size());
					pamTabPane.getTabs().add(tab); 
				}

				//set the parameters. 
				tab = (ClassifierTab) pamTabPane.getTabs().get(i); 
				
				//set the parameters for the tab.
				tab.getCTClassifierPane().setParams(clickTrainParams.ctClassifierParams[i]);
			}
		}

		enableClassifierPane(enableClassifierBox.isSelected()); 
	}


}
