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

public class TOADOptionsPane extends SettingsPane<TOADBaseParams>{
	
	private BorderPane mainPane;
	
	private TextField minCorrelation;
	
	private TextField minTimeDelays, minGroups;

	public TOADOptionsPane(Object ownerWindow) {
		super(ownerWindow);
		mainPane = new BorderPane();
		GridPane gridPane = new PamGridPane();
		int x = 0, y = 0;
		gridPane.add(new PamLabel("Min correlation ", Pos.CENTER_RIGHT), x++, y);
		gridPane.add(minCorrelation = new TextField("1234"), x, y);
		x = 0;
		y++;
		gridPane.add(new PamLabel("Min TOAD measurements ", Pos.CENTER_RIGHT), x++, y);
		gridPane.add(minTimeDelays = new TextField("1234"), x, y);
		x = 0;
		y++;
		gridPane.add(new PamLabel("Min groups ", Pos.CENTER_RIGHT), x++, y);
		gridPane.add(minGroups = new TextField("1234"), x, y);
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
