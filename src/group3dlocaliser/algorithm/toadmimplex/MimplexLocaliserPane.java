package group3dlocaliser.algorithm.toadmimplex;

import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.MCMC.MCMCParams2;
import Localiser.controls.MCMCPane;
import group3dlocaliser.algorithm.toadmcmc.MCMCLoclaiserPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;

/**
 * An MCMC pane with a few extra options for Mimplex loclaisation. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MimplexLocaliserPane extends LocaliserPane<MimplexParams> {

	private MCMCPane mcmcPane;
	
	private PamBorderPane mainPane; 

	public MimplexLocaliserPane() {
		
				
		mcmcPane = new MCMCPane(); 
		((Pane) mcmcPane.getContentNode()).setPadding(new Insets(5,5,5,5)); 
		
		mainPane.setTop( createMimplexPane());
		mainPane.setCenter(mcmcPane.getContentNode()); 
	}
	
	private Pane createMimplexPane() {
		
		PamVBox holder = new PamVBox();
		
		PamToggleSwitch toggleSwitch = new PamToggleSwitch("Always loclaiser first combination"); 
		
		return holder; 
	}

	@Override
	public MimplexParams getParams(MimplexParams currParams) {
		
		MimplexParams params; 
		if (currParams==null) params = new MimplexParams();  
		else params = currParams; 
		
		
		params =  (MimplexParams) mcmcPane.getParams(params);
		
		return params;
	}
	

	@Override
	public void setParams(MimplexParams input) {
		if (input==null) input= new MimplexParams(); 
		mcmcPane.setParams(input);
	}

	@Override
	public String getName() {
		return "Mimplex Settings";
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
