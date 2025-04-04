package group3dlocaliser.algorithm.toadmimplex;

import Localiser.detectionGroupLocaliser.GroupLocResult;
import group3dlocaliser.localisation.LinearLocalisation;


/**
 * Wrapper to make a linear localisation into a group localisation 
 */
public class LinearGroupLocResult extends GroupLocResult {

	public LinearGroupLocResult(LinearLocalisation linearLocalisation) {
		super(linearLocalisation.getReferencePosition(), 0,  linearLocalisation.getChi2());
		
	}

}
