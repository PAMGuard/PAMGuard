package group3dlocaliser.algorithm.toadmcmc;

import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.MCMC.MCMCParams2;
import Localiser.controls.MCMCPane;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.geometry.Insets;


public class MCMCLoclaiserPane extends LocaliserPane<MCMCParams2> {

	private MCMCPane mainPane;

	public MCMCLoclaiserPane() {
		mainPane = new MCMCPane(); 
		((Pane) mainPane.getContentNode()).setPadding(new Insets(5,5,5,5)); 
	}
	

	@Override
	public MCMCParams2 getParams(MCMCParams2 currParams) {
		if (currParams==null) return mainPane.getParams(new MCMCParams2());
		return mainPane.getParams(currParams);
	}

	@Override
	public void setParams(MCMCParams2 input) {
		if (input==null) input= new MCMCParams2(); 
		mainPane.setParams(input);
	}

	@Override
	public String getName() {
		return "MCMC Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane.getContentNode();
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	

}
