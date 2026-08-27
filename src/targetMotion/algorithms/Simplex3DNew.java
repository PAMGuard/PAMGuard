package targetMotion.algorithms;

import java.awt.Color;

import targetMotion.TargetMotionLocaliser;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser2;
import PamguardMVC.superdet.DetectionGroup;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.LocContents;

public class Simplex3DNew<T extends DetectionGroup> extends Simplex2DNew<T>{
	

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
		return new LocContents(LocContents.HAS_LATLONG | LocContents.HAS_DEPTH | LocContents.HAS_DEPTHERROR | LocContents.HAS_PERPENDICULARERRORS);
	}

}
