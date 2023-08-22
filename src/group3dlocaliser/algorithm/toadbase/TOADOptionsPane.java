package group3dlocaliser.algorithm.toadbase;

import java.text.DecimalFormat;

import PamController.SettingsPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamLabel;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.validator.PamValidator;

public class TOADOptionsPane extends SettingsPane<TOADBaseParams>{
	
	private BorderPane mainPane;
	
	private TextField minCorrelation;
	
	private TextField minTimeDelays, minGroups;
	
	private PamValidator validator; 

	public TOADOptionsPane(Object ownerWindow) {
		super(ownerWindow);
		
		validator = new PamValidator(); 
		
		mainPane = new BorderPane();
		GridPane gridPane = new PamGridPane();
		int x = 0, y = 0;
		gridPane.add(new PamLabel("Min correlation ", Pos.CENTER_RIGHT), x++, y);
		gridPane.add(minCorrelation = new TextField("1234"), x, y);
		
		
		//create check to show at least some check boxes need to be selected.
		validator.createCheck()
        .dependsOn(("min_corr"), minCorrelation.textProperty())
        .withMethod(c -> {
        	
        	try {
    			double newCorr = Double.valueOf(minCorrelation.getText());
    			if (newCorr>=0.9) {
  	              c.warn("It's very unblikely you will get a correlation this high");
    			}
    			if (newCorr>=1.) {
    	              c.error("Correlation values cannot be higher than 1");
      			}
      
        	}
        	catch (Exception e) {
  	              c.error("Input cannot be read as a number");
        	}    
        })
        .decorates(minCorrelation)
        .immediate();
      
		
		x = 0;
		y++;
		gridPane.add(new PamLabel("Min TOAD measurements ", Pos.CENTER_RIGHT), x++, y);
		gridPane.add(minTimeDelays = new TextField("1234"), x, y);
		
		validator.createCheck()
        .dependsOn(("min_delays"), minTimeDelays.textProperty())
        .withMethod(c -> {
        	
        	try {
    			int minDelays = Integer.valueOf(minTimeDelays.getText());
    			
    			//TODO would be great to add something which catches whether max delays have been reached. 
 
        	}
        	catch (Exception e) {
  	              c.error("Min TOAD measurements cannot be read as a number");
        	}    
        })
        .decorates(minCorrelation)
        .immediate();
		
		x = 0;
		y++;
		gridPane.add(new PamLabel("Min groups ", Pos.CENTER_RIGHT), x++, y);
		gridPane.add(minGroups = new TextField("1234"), x, y);
		
		
		validator.createCheck()
        .dependsOn(("min_groups"), minGroups.textProperty())
        .withMethod(c -> {
        	
        	try {
    			int minGroupsVal = Integer.valueOf(minGroups.getText());
    			
    			//TODO would be great to add something which catches whether max delays have been reached. 
 
        	}
        	catch (Exception e) {
  	              c.error("Minb groups input cannot be read as a number");
        	}    
        })
        .decorates(minCorrelation)
        .immediate();
		
		
		
		
		minCorrelation.setTooltip(new Tooltip("Minimum cross correlation coefficient for each hydrophone pair"));
		minTimeDelays.setTooltip(new Tooltip("Minimum number of time delays with acceptable cross correlation"));
		minGroups.setTooltip(new Tooltip("Minimum number of channel groups included with acceptable cross correlations"));
		minCorrelation.setPrefColumnCount(3);
		minTimeDelays.setPrefColumnCount(3);
		minGroups.setPrefColumnCount(3);
		mainPane.setLeft(gridPane);
	}

	@Override
	public TOADBaseParams getParams(TOADBaseParams currParams) {
		try {
			double newCorr = Double.valueOf(minCorrelation.getText());
			int minT = Integer.valueOf(minTimeDelays.getText());
			int minG = Integer.valueOf(minGroups.getText());
			currParams.setMinCorrelation(newCorr);
			currParams.setMinTimeDelays(minT);
			currParams.setMinCorrelatedGroups(minG);
		}
		catch (NumberFormatException e) {
			PamDialogFX.showWarning("Invalid timing parameter value");
			return null;
		}
		return currParams;
	}

	@Override
	public void setParams(TOADBaseParams input) {
		DecimalFormat df = new DecimalFormat("#.##");
		minCorrelation.setText(df.format(input.getMinCorrelation()));
		minTimeDelays.setText(String.format("%d", input.getMinTimeDelays()));
		minGroups.setText(String.format("%d", input.getMinCorrelatedGroups()));
		
	}

	@Override
	public String getName() {
		return "TDOA Base Params";
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
