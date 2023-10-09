package group3dlocaliser.algorithm.toadmimplex;

import java.util.ArrayList;

import Localiser.LocaliserModel;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import group3dlocaliser.Group3DDataUnit;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.algorithm.hyperbolic.HyperbolicLocaliser;
import group3dlocaliser.algorithm.toadmcmc.ToadMCMCLocaliser;
import group3dlocaliser.algorithm.toadsimplex.ToadSimplexLocaliser;
import group3dlocaliser.grouper.DetectionGroupedSet;


/**
 * The Mimplex localiser is similar to MCMC but it localises multiple possible combinations using fast algorithms then uses MCMC to localiser the final combination. 
 * @author Jamie Macaulay
 *
 */
public class ToadMimplexLocaliser extends ToadMCMCLocaliser {


	/**
	 * The simplex localiser for pre-filtering detections. 
	 */
	ToadSimplexLocaliser simplexLocaliser; 


	/**
	 * The hyperbolic localiser for pre-filtering detections. 
	 */
	HyperbolicLocaliser hyperbolicLoclaiser;

	ArrayList<LocaliserModel> preLocaliserModels = new ArrayList<LocaliserModel>(); 



	public ToadMimplexLocaliser(Group3DLocaliserControl group3dLocaliser) {
		super(group3dLocaliser);

		//note that Mimplex only supports 3D localisations. 
		simplexLocaliser =  new ToadSimplexLocaliser(group3dLocaliser, 3);

		hyperbolicLoclaiser =  new HyperbolicLocaliser(group3dLocaliser);

		preLocaliserModels.add(hyperbolicLoclaiser);
		preLocaliserModels.add(simplexLocaliser);

	}


	/**
	 * Option to pre-filter the localisation results. This can be useful when using algorithms that 
	 * internally handle detection match uncertainty. 
	 * @param - the initial set of detection matches to filter. 
	 */
	public DetectionGroupedSet preFilterLoc(DetectionGroupedSet preGroups) {

		//loclaiser using both hyperbolic and the

		// will have to make a data unit for each group now...
		Group3DDataUnit[] group3dDataUnits = new Group3DDataUnit[preGroups.getNumGroups()];

		ArrayList<GroupLocResult> preLocalisations = new ArrayList<GroupLocResult>();


		for (int i=0; i<preGroups.getNumGroups(); i++) {
			group3dDataUnits[i] = new Group3DDataUnit(preGroups.getGroup(i));


			GroupLocalisation preAbstractLocalisation; 
			double minChi2 = Double.MAX_VALUE;

			GroupLocResult locResult = null;
			for (LocaliserModel model: preLocaliserModels) {

				locResult = null; 
				minChi2=Double.MAX_VALUE;

				preAbstractLocalisation = (GroupLocalisation) model.runModel(group3dDataUnits[i], null, false);

				//now iterate through the potential ambiguities (this is a bit redunadant with Simplex and Hyperbolic)
				for (GroupLocResult groupLocResult: preAbstractLocalisation.getGroupLocResults()) {
					if (groupLocResult.getChi2()<minChi2) {
						locResult = groupLocResult; 
					}
				}
			}

			//add the pre loclaisation to the array - note we may add a null here. 
			preLocalisations.add(locResult); 

		}
		
		//now we have a list of best loclaisations from different models. Now pick the ones we want to localise with MCMC



		return preGroups; 
	}

}
