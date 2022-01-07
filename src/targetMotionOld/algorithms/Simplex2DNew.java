package targetMotionOld.algorithms;

import java.awt.Color;

import targetMotionOld.TargetMotionLocaliser;
import Localiser.LocaliserPane;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser2;
import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.LocContents;

public class Simplex2DNew<T extends GroupDetection> extends AbstractTargetMotionModel<T> {
	
	/**
	 * The localiser. 
	 */
	public DetectionGroupLocaliser2<T> detectionGroupLocaliser;
	
	public Simplex2DNew(TargetMotionLocaliser<T> targetMotionLocaliser) {
		detectionGroupLocaliser= new DetectionGroupLocaliser2<T>("2D Simplex Optimisation");
	}

	@Override
	public String getName() {
		return detectionGroupLocaliser.getName();
	}

	@Override
	public String getToolTipText() {
		return "Simplex algorithm which searches for localisations in 2D space";
	}

	@Override
	public GroupLocalisation runModel(T pamDetection, DetectionGroupOptions detectionGroupOptions, boolean run) {
//		System.out.println("Simpelx2D: New localiser attempting to localise some data units: " 
//	+ pamDetection.getSubDetectionsCount());
		
		GroupLocalisation groupLoc= (GroupLocalisation) detectionGroupLocaliser.runModel(pamDetection, detectionGroupOptions, false);
		
		if (groupLoc == null || groupLoc.getGroupLocResults()==null) return null; 
		
		//target motion localiser needs to know the model for plot sysmbols etc.
		//Bit of a HACK but a lot of coding to replace?
		for (int i=0; i<groupLoc.getGroupLocResults().length; i++){
			groupLoc.getGroupLocaResult(i).setModel(this);
//			//TEMP bit of error diagnosis. 
//			System.out.println("Simplex2DNew: 45deg parallel: "+groupLoc.getLocError(i).getError(PamVector.fromHeadAndSlant(45, 0))
//					+ " perpindiocular: " +groupLoc.getLocError(i).getError(PamVector.fromHeadAndSlant(45+90, 0))); 
			
		}
		
		
		return groupLoc;
	}

	@Override
	Color getSymbolColour() {
		return Color.ORANGE;
	}

	@Override
	public LocContents getLocContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LocaliserPane<?> getSettingsPane() {
		return detectionGroupLocaliser.getSettingsPane();
	}

	@Override
	public boolean hasParams() {
		return detectionGroupLocaliser.hasParams();
	}

	@Override
	public void notifyModelProgress(double progress) {		
		
	}


}
