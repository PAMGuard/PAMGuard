package group3dlocaliser.algorithm.toadmimplex;

import Localiser.LocaliserPane;
import Localiser.controls.MCMCPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;

/**
 * An MCMC pane with a few extra options for Mimplex localisation. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MimplexLocaliserPane extends LocaliserPane<MimplexParams> {

	private MCMCPane mcmcPane;
	
	private PamVBox mainPane;

	private PamToggleSwitch toggleSwitch; 

	public MimplexLocaliserPane() {
		
		mcmcPane = new MCMCPane(); 
		
		mainPane = new PamVBox();
		mainPane.setSpacing(5);
		
		mainPane.getChildren().add( createMimplexPane());
		mainPane.getChildren().add(mcmcPane.getContentNode()); 
		
		mainPane.setPadding(new Insets(5,5,5,5)); 

	}
	
	private Pane createMimplexPane() {
		
		PamVBox holder = new PamVBox();
		
		toggleSwitch = new PamToggleSwitch("Always localise first combination"); 
		
		Label label = new Label("Pre-localisation"); 
		label.setFont(Font.font(null,FontWeight.BOLD, 11));

		
		holder.getChildren().addAll(label, toggleSwitch);
		
		return holder; 
	}

	@Override
	public MimplexParams getParams(MimplexParams currParams) {
		
		MimplexParams params; 
		if (currParams==null) params = new MimplexParams();  
		else params = currParams; 
		
		params.useFirstCombination = toggleSwitch.isSelected();
		
		
		params =  (MimplexParams) mcmcPane.getParams(params);
		
		return params;
	}
	

	@Override
	public void setParams(MimplexParams input) {
		if (input==null) input= new MimplexParams(); 
		
		toggleSwitch.setSelected(input.useFirstCombination);
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
