package group3dlocaliser.algorithm.toadmimplex;

import java.util.ArrayList;

import Localiser.LocaliserModel;
import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.MCMC.MCMCParams2;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamguardMVC.PamDataBlock;
import group3dlocaliser.Group3DDataUnit;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.algorithm.hyperbolic.HyperbolicLocaliser;
import group3dlocaliser.algorithm.toadbase.TOADBaseAlgorithm;
import group3dlocaliser.algorithm.toadmcmc.MCMCLocaliserPane;
import group3dlocaliser.algorithm.toadmcmc.ToadMCMCLocaliser;
import group3dlocaliser.algorithm.toadsimplex.ToadSimplexLocaliser;
import group3dlocaliser.grouper.DetectionGroupedSet;
import group3dlocaliser.localisation.LinearLocalisation;


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

	ArrayList<TOADBaseAlgorithm> preLocaliserModels = new ArrayList<TOADBaseAlgorithm>(); 

	/**
	 * A settings pane for the Mimplex localiser
	 */
	private MimplexLocaliserPane mimplexSettingsPane; 


	public ToadMimplexLocaliser(Group3DLocaliserControl group3dLocaliser) {
		super(group3dLocaliser);

		//note that Mimplex only supports 3D localisations. 
		simplexLocaliser =  new ToadSimplexLocaliser(group3dLocaliser, 3);

		hyperbolicLoclaiser =  new HyperbolicLocaliser(group3dLocaliser);

		preLocaliserModels.add(hyperbolicLoclaiser);
		preLocaliserModels.add(simplexLocaliser);

	}
	
	@Override
	public String getName() {
		return "Mimplex";
	}
	
	
	@Override
	public String getToolTipText() {
		return "Uses a combination of faster and slower algorithms to localise. Useful if there is match uncertainty between detections"; 
	}

	@Override	
	public boolean prepare(PamDataBlock sourceBlock) {
		//need to prep our pre-localiser models.
		for (TOADBaseAlgorithm model: preLocaliserModels) {
			model.prepare(sourceBlock);
			
			//important to set the toad params here or nothing will work...
			model.getToadBaseParams().setChannelBitmap(this.getToadBaseParams().getChannelBitmap());			
			model.getToadBaseParams().setMinCorrelatedGroups(this.getToadBaseParams().getMinCorrelatedGroups());
			model.getToadBaseParams().setMinCorrelation(this.getToadBaseParams().getMinCorrelation());
			model.getToadBaseParams().setMinCorrelatedGroups(this.getToadBaseParams().getMinCorrelatedGroups());

		}
		
		return super.prepare(sourceBlock);
	}

	/**
	 * Option to pre-filter the localisation results. This can be useful when using algorithms that 
	 * internally handle detection match uncertainty. 
	 * @param - the initial set of detection matches to filter. 
	 */
	public DetectionGroupedSet preFilterLoc(DetectionGroupedSet preGroups) {
		
		//System.out.println("Pre filter groups: " + preGroups.getNumGroups());
		
		MimplexParams params = (MimplexParams) group3dLocaliser.getLocaliserAlgorithmParams(this).getAlgorithmParameters();
		
		if (params==null) params = new MimplexParams();
		
		//no need to do any more processing
		if (preGroups.getNumGroups()<=1) {
			return preGroups;
		}
	
		//no need to do a any more processing. 
		if (preGroups.getNumGroups()<=2 && params.useFirstCombination) {
			return preGroups;
		}

		//localiser using both hyperbolic and simplex the
		// will have to make a data unit for each group now...
		Group3DDataUnit[] group3dDataUnits = new Group3DDataUnit[preGroups.getNumGroups()];

		ArrayList<GroupLocResult> preLocalisations = new ArrayList<GroupLocResult>();


		for (int i=0; i<preGroups.getNumGroups(); i++) {
			
			group3dDataUnits[i] = new Group3DDataUnit(preGroups.getGroup(i));

			//System.out.println("ToadMImplex. group3dDataUnits[i]: " + group3dDataUnits[i] );

			AbstractLocalisation preAbstractLocalisation = null;
			double minChi2 = Double.MAX_VALUE;

			GroupLocResult locResult = null;
			for (LocaliserModel model: preLocaliserModels) {
				
				//System.out.println("ToadMImplex. model: " + model );


				locResult = null; 
				minChi2=Double.MAX_VALUE;
				 preAbstractLocalisation = null; //must reset this. 
				try {
					preAbstractLocalisation = model.runModel(group3dDataUnits[i], null, false);
				}
				catch (Exception e) {
					System.out.println("Mimplex pre filter loclaisation failed"); 
					e.printStackTrace();
				}
				
//				System.out.println("Pre-localisation result: " + locResult + "  " + model.getName() + "N units: " + preGroups.getGroup(i).size());
				
				if (preAbstractLocalisation!=null) {
					
					if (preAbstractLocalisation instanceof GroupLocalisation) {
					//now iterate through the potential ambiguities (this is a bit redunadant with Simplex and Hyperbolic)
					for (GroupLocResult groupLocResult: ((GroupLocalisation) preAbstractLocalisation).getGroupLocResults()) {
						if (groupLocResult.getChi2()<minChi2) {
							locResult = groupLocResult; 
						}
					}
					}
					if (preAbstractLocalisation instanceof LinearLocalisation) {
						//if a linear vertical array (exactly) then will return a linear localisation. 	Need to make this into
						//a group localisation to satisfy the mimplex localiser. 
						if (((LinearLocalisation) preAbstractLocalisation).getChi2()<minChi2) {
							locResult = new LinearGroupLocResult(((LinearLocalisation) preAbstractLocalisation)); 
						}
					}
					
				}
			}
			
//			System.out.println("Pre-localisation: " + locResult);

			//add the pre loclaisation to the array - note we may add a null here. 
			preLocalisations.add(locResult); 

		}
		
		int bestLocIndex = -1;
		double minAIC = Double.MAX_VALUE;
		
		for (int i=0; i<preLocalisations.size(); i++) {
			
			if (preLocalisations.get(i)!=null && preLocalisations.get(i).getAic()<minAIC) {
				bestLocIndex = i;
			}
		}
		
		//create a new detection group set. 
		DetectionGroupedSet groupedSet = new DetectionGroupedSet();
		
		if ((params.useFirstCombination && bestLocIndex!=0) || bestLocIndex==-1) {
			groupedSet.addGroup(preGroups.getGroup(0));			
		}
		
		if (bestLocIndex>=0) {
			groupedSet.addGroup(preGroups.getGroup(bestLocIndex));
		}
		
		System.out.println("Number of groups out: " + groupedSet.getNumGroups());

		return groupedSet; 
	}
	
	@Override
	public LocaliserPane getAlgorithmSettingsPane() {
		if (mimplexSettingsPane==null) {
			mimplexSettingsPane = new MimplexLocaliserPane(); 
		}
		return mimplexSettingsPane;
	}


}
