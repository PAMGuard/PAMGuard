package targetMotionOld.algorithms;

import java.awt.Color;

import targetMotionOld.TargetMotionLocaliser;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser2;
import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.LocContents;

public class Simplex3DNew<T extends GroupDetection> extends Simplex2DNew<T>{
	

	public Simplex3DNew(TargetMotionLocaliser targetMotionLocaliser) {
		super(targetMotionLocaliser);
		detectionGroupLocaliser.setLocDim(3);
	}

	@Override
	public String getName() {
		return "3D Simplex Optimisation";
//		return detectionGroupLocaliser.getName(); 
	}

	@Override
	public String getToolTipText() {
		return "Simplex algorithm which searches for a localisation in 3D space";
	}

	@Override
	Color getSymbolColour() {
		return Color.CYAN;
	}
	
	@Override
	public LocContents getLocContents() {
		// TODO Auto-generated method stub
		return null;
	}

}
