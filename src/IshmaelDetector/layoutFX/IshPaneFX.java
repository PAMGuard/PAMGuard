package IshmaelDetector.layoutFX;

import IshmaelDetector.IshDetControl;
import IshmaelDetector.IshDetParams;
import PamController.SettingsPane;
import fftManager.FFTDataUnit;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;

/**
 * FX implementation of the Ishmael detector. 
 * 
 * @author Jamie Macaulay 
 *
 */
public abstract class IshPaneFX extends SettingsPane<IshDetParams> {

	/**
	 * The grouped source pane. Holds channels etc.  
	 */
	private GroupedSourcePaneFX groupedSourcePaneFX;
	
	/**
	 * The main pane. 
	 */
	private Pane mainPane;

	/**
	 * The generic peak picking pane. 
	 */
	private PeakPickingPane peakPickingPane;

	/**
	 * Reference to the control. 
	 */
	protected IshDetControl ishDetControl; 


	/**
	 * Constructor for Ishmael pane. Since different Ishmael detector share a lot of params 
	 * the pane is abstract and sub-classed by different Ishamel detectors.
	 * @param ownerWindow - the ownder window
	 */
	public IshPaneFX(IshDetControl ishDetControl) {
		super(null);
		this.ishDetControl=ishDetControl; 
		mainPane= createIshPane(); 
		mainPane.setPadding(new Insets(5,5,5,5));
	}

	/****
	 * Create the Ishmael Pane 
	 * @return controls for Ishmael settings
	 */
	private Pane createIshPane() {

		groupedSourcePaneFX = new GroupedSourcePaneFX("Raw Data Source for Ishmael Detector", FFTDataUnit.class, true, true, true);
		
		peakPickingPane = new PeakPickingPane(); 

		PamVBox mainPane = new PamVBox();
		mainPane.setSpacing(5);
		mainPane.getChildren().addAll(groupedSourcePaneFX, getDetectorPane().getContentNode(), peakPickingPane.getContentNode());
	
		return mainPane;
	}
	
	
	/**
	 * Get the detector specific pane. 
	 * @return pane with detector specific controls. 
	 */
	public abstract SettingsPane<IshDetParams> getDetectorPane(); 
	
	@Override
	public IshDetParams getParams(IshDetParams currParams) {
		
		//sets params inside function
		groupedSourcePaneFX.getParams(currParams.groupedSourceParmas);

		currParams = getDetectorPane().getParams(currParams); 

		currParams = peakPickingPane.getParams(currParams);

		return currParams;
	}

	@Override
	public void setParams(IshDetParams input) {
		groupedSourcePaneFX.setParams(input.groupedSourceParmas);
		peakPickingPane.setParams(input);
		getDetectorPane().setParams(input);
	}

	@Override
	public String getName() {
		return "Ishmael Parameters";
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
