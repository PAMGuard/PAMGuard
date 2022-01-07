package clickTrainDetector.layout.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Params;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;

/**
 * Pane which has advanced settings for standard MHT chi^2 calculator
 * 
 * @author Jamie Macaulay 
 *
 */
public class StandardMHTChi2AdvPane extends PamBorderPane {

	private Spinner<Double> newTrackPenalty;
	private PamSpinner<Double> coastpenatly;
	private PamSpinner<Double> longTrackBonus;
	private PamSpinner<Double> lowICIBonus;
	private PamSpinner<Integer> newTrackN;


	public StandardMHTChi2AdvPane() {
		super(); 
		this.setCenter(createAdvPane()); 
	}


	private Pane createAdvPane() {

		newTrackPenalty = new PamSpinner<Double>(0,Double.MAX_VALUE,5,50);
		newTrackPenalty.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL); 
		newTrackPenalty.setEditable(true);
		newTrackPenalty.setPrefWidth(80);
		String tooltipText = 
				"Add a penalty to a new track's  X\u00b2 value. This ensures \n "
						+ "the MHT algorithm does not minmise X\u00b2 by simply creating\n"
						+ "lots of fragmentated tracks. The new track penalty is added to\n "
						+ "the X\u00b2 of every new track."; 

		newTrackPenalty.setTooltip(new Tooltip(tooltipText) );
		Label newTrackPenaltyLabel = new Label("New Track Penalty");
		newTrackPenaltyLabel.setTooltip(new Tooltip(tooltipText) );
		
		newTrackN = new PamSpinner<Integer>(0,Integer.MAX_VALUE,1,1);
		newTrackN.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL); 
		newTrackN.setEditable(true);
		newTrackN.setPrefWidth(80);
		tooltipText = 
				"Add a penalty to a new track's  X\u00b2 value. This ensures \n "
						+ "the MHT algorithm does not minmise X\u00b2 by simply having\n"
						+ "lots of fragmentated tracks. The new track penalty is added to\n "
						+ "the X\u00b2 of every new track."; 

		newTrackN.setTooltip(new Tooltip(tooltipText) );
		Label newTrackNLabel = new Label("No. New Track Clicks");
		newTrackNLabel.setTooltip(new Tooltip(tooltipText) );


		coastpenatly = new PamSpinner<Double>(0,Double.MAX_VALUE,5,1);
		coastpenatly.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL); 
		coastpenatly.setEditable(true);
		coastpenatly.setPrefWidth(80);
		tooltipText = 
				"Add a penalty for each coast of a track. This discourages the algorithm \n"
						+ "from coasting to attempt to minimise X\u00b2. The coast value is added to \n "
						+ "the X\u00b2 for every track coast."; 

		coastpenatly.setTooltip(new Tooltip(tooltipText) );

		Label coastPenatlyLabel = new Label("Coast Penalty");
		coastPenatlyLabel.setTooltip(new Tooltip(tooltipText) );

		longTrackBonus = new PamSpinner<Double>(0,Double.MAX_VALUE,5,1);
		longTrackBonus.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL); 
		longTrackBonus.setEditable(true);
		longTrackBonus.setPrefWidth(80);
		tooltipText = "The X\u00b2 value for a track is divided by the number of data units and then multiplied \n"
				+ "by the long track bonus The long track multplier is the number of clicks in the click train divided \n"
				+ "by the total number of considered clicks.The long track bonus is an exponent of the multplier. \n"
				+ "Thus a value of zero means that longer tracks have no bonus effect, 1 means that the track is multiplied \n"
				+ "directly by the multplier and increasing values then increasingly favour longer tracks: \n\n"
				+ "Recommended value: 2";

		longTrackBonus.setTooltip(new Tooltip(tooltipText) );
		Label longTrackBonusLabel = new Label("Long Track Bonus");
		longTrackBonusLabel.setTooltip(new Tooltip(tooltipText) );

		lowICIBonus = new PamSpinner<Double>(0,Double.MAX_VALUE,5,1);
		lowICIBonus.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL); 
		lowICIBonus.setEditable(true);
		lowICIBonus.setPrefWidth(80);
		tooltipText = "The low ICI multiplier discourages harmonic click trains i.e. when every second or third etc click is \n"
				+ "selected. Higher values favour lower ICI values in click trains. The low ICI track bonus is an exponent of \n"
				+ "the multplier. Thus a value of zero means that longer tracks have no bonus effect, 1 means that the track is \n"
				+ "multiplied directly by the multplier and increasing values then increasingly favour longer tracks: \n\n"
				+ "Recommended value: 2";


		lowICIBonus.setTooltip(new Tooltip(tooltipText) );

		Label lowICIBonusLabel = new Label("Low ICI Bonus");
		lowICIBonusLabel.setTooltip(new Tooltip(tooltipText) );

		PamGridPane gridPaneHolder= new PamGridPane();
		gridPaneHolder.setHgap(5);
		gridPaneHolder.setVgap(5);


		int row =0; 
		gridPaneHolder.add(lowICIBonusLabel, 0,row);
		gridPaneHolder.add(lowICIBonus, 1, row);
		
		row++;
		gridPaneHolder.add(longTrackBonusLabel, 0, row);
		gridPaneHolder.add(longTrackBonus, 1, row);
		
		row++;
		gridPaneHolder.add(coastPenatlyLabel, 0, row);
		gridPaneHolder.add(coastpenatly, 1, row);

		row++; 
		gridPaneHolder.add(newTrackPenaltyLabel, 0, row);
		gridPaneHolder.add(newTrackPenalty, 1, row);
		
		row++; 
		gridPaneHolder.add(newTrackNLabel, 0, row);
		gridPaneHolder.add(newTrackN, 1, row);
		
		return gridPaneHolder; 

	}
	

	public StandardMHTChi2Params getParams(StandardMHTChi2Params currParams) {
		try {
			
			currParams.longTrackExponent = longTrackBonus.getValue(); 
			currParams.lowICIExponent = lowICIBonus.getValue(); 
			
			currParams.coastPenalty = coastpenatly.getValue(); 
			currParams.newTrackPenalty = newTrackPenalty.getValue(); 
			currParams.newTrackN = newTrackN.getValue(); 
			
			return currParams;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null; 
		}
	}

	
	public void setParams(StandardMHTChi2Params currParams) {
		
		longTrackBonus.getValueFactory().setValue(currParams.longTrackExponent);
		lowICIBonus.getValueFactory().setValue(currParams.lowICIExponent);
		
		coastpenatly.getValueFactory().setValue(currParams.coastPenalty);
		newTrackPenalty.getValueFactory().setValue(currParams.newTrackPenalty);
		newTrackN.getValueFactory().setValue(currParams.newTrackN); 

	}

}
