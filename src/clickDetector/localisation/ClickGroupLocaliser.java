package clickDetector.localisation;

import java.util.ArrayList;

import Localiser.LocaliserModel;
import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.leastSquares.LeastSquares;
import Localiser.algorithms.genericLocaliser.simplex.Simplex;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser2;
import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * A localiser implemented for groups of clicks. Usually this will be used with target motion loclisation but is also
 * suitable for dispersed set of buoys. 
 *
 */
public class ClickGroupLocaliser  extends GeneralGroupLocaliser {
	
	
	/**
	 * Reference to the click control. 
	 */
	private ClickControl clickControl;
	
	/**
	 * List of possible localiser (do not serialise)
	 */
	public transient ArrayList<LocaliserModel> locAlgorithmList;

	public ClickGroupLocaliser(ClickControl clickControl){
		super();
		this.clickControl=clickControl; 
	}

	@Override
	public String getName() {
		return clickControl.getUnitName();
	}

	@Override
	public ClickLocParams getClickLocParams() {
		return clickControl.getClickParameters().getLocalisationParams();
	}

}
