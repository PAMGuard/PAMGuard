package clickDetector.layoutFX.clickClassifiers;


import clickDetector.ClickControl;
import clickDetector.ClickClassifiers.basicSweep.SweepClassifier;
import clickDetector.ClickClassifiers.basicSweep.SweepClassifierParameters;
import clickDetector.ClickClassifiers.basicSweep.SweepClassifierSet;

/**
 * Slightly different pane for the sweep classifier.  
 * 
 * @author Jamie Macaulay
 */
public class SweepClassifierPaneFX extends BasicIdentifierPaneFX {

	/** 
	 * Reference to the sweep classifier.
	 */
	SweepClassifier sweepClickClassifier;
	
	/**
	 * Reference to the sweep classifier params.
	 */
	private SweepClassifierParameters sweepIdParameters;

	
	public SweepClassifierPaneFX(SweepClassifier sweepClickClassifier,
			ClickControl clickControl) {
		super(sweepClickClassifier, clickControl);
		this.sweepClickClassifier=sweepClickClassifier; 
	}
	
	
	/**
	 * Set classifier pane within hiding pane.
	 * @param clickTypeProperty - the click type property. 
	 */
	@Override
	public void setClassifierPane(ClickTypeProperty clickTypeProperty){
		SweepClassifierSetPaneFX sweepPane=new SweepClassifierSetPaneFX(sweepClickClassifier);
	
		
		//make it so the title of the pane is the same as the name as the classifier
		getFlipPane().getAdvLabel().textProperty().unbind();
		getFlipPane().getAdvLabel().textProperty().bind(	sweepPane.getNameTextProperty());
		
		sweepPane.classifierItemRow = sweepClickClassifier.getSweepClassifierParams().getSetRow((SweepClassifierSet) clickTypeProperty.getClickType());
		
		sweepPane.setParams(clickTypeProperty);
		super.getClickTypeHolder().setCenter(sweepPane.getContentNode());
		
		//now need to make sure on closing the pane that settings are saved. Need to 
		//remove the old click type from the list and add new one in the same position. 
		getFlipPaneCloseButton().setOnAction((action)->{
			showFlipPane(false);
			sweepPane.getParams(clickTypeProperty);
		});
	}
	
	
	@Override
	public void setParams() {
		
		sweepIdParameters = sweepClickClassifier.getSweepClassifierParams().clone();
		

		//change the table
		tableDataChanged();
	}

	@Override
	public boolean getParams() {
//		System.out.println("Sweep classifier getParams: " + sweepIdParameters); 
		if (sweepIdParameters==null) sweepIdParameters = new SweepClassifierParameters(); 
		
		sweepClickClassifier.setSweepClassifierParams(sweepIdParameters);
		
		//remove all classifiers and add whatever is in the table. 
		sweepClickClassifier.getSweepClassifierParams().removeAll();
		for (int i=0; i<getClickClassifiers().size() ;i++){
			sweepClickClassifier.getSweepClassifierParams().addSet((SweepClassifierSet) getClickClassifiers().get(i).getClickType());
		}
		

		return true;
	}
	
	
	/**
	 * Create click classifier. 
	 */
	@Override
	public ClickTypeProperty createClickTypeProperty(){
		return new ClickTypeProperty(new SweepClassifierSet());
	}
	
	
	/**
	 * Change data on the table.
	 */
	public void tableDataChanged(){
		//clear the table
		getClickClassifiers().removeAll(getClickClassifiers());
		//now add all the classifiers.
		for (int i=0; i<sweepIdParameters.getNumSets() ; i++){
			ClickTypeProperty sweepTypeProperty=new ClickTypeProperty(sweepIdParameters.getSet(i));
			getClickClassifiers().add(sweepTypeProperty);
		}
	}


}
