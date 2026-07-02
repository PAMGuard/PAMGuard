package mcc.mccacquisition.layoutFX;

import Acquisition.layoutFX.DAQSettingsPane;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import mcc.mccacquisition.MCCDaqParams;
import mcc.mccacquisition.MCCDaqSystem;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;

/**
 * JavaFX settings pane for the Measurement Computing (MCC) DAQ system.
 * Provides controls for board selection, terminal configuration, and ADC range.
 * 
 * @author Jamie Macaulay
 */
public class MCCDAQPane extends DAQSettingsPane {

	private PamBorderPane mainPane;

	private ComboBox<String> boardList;

	private ComboBox<String> terminalType;

	private MCCDaqSystem mccDaqSystem;

	public MCCDAQPane(MCCDaqSystem mccDaqSystem) {
		this.mccDaqSystem = mccDaqSystem;

		mainPane = new PamBorderPane();
		PamVBox holder = new PamVBox();
		holder.setSpacing(5);

		Label title = new Label("MCC Device");
		PamGuiManagerFX.titleFont2style(title);

		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		int row = 0;

		gridPane.add(new Label("MCC Board"), 0, row);
		boardList = new ComboBox<>();
		boardList.setMaxWidth(Double.MAX_VALUE);
		gridPane.add(boardList, 1, row);

		row++;
		gridPane.add(new Label("Terminal Config"), 0, row);
		terminalType = new ComboBox<>();
		terminalType.getItems().addAll("Single Ended", "Differential");
		terminalType.setMaxWidth(Double.MAX_VALUE);
		gridPane.add(terminalType, 1, row);

		holder.getChildren().addAll(title, gridPane);
		mainPane.setCenter(holder);
	}

	@Override
	public void setParams() {
		MCCDaqParams params = mccDaqSystem.getMccDaqParams();

		boardList.getItems().clear();
		// MCC boards are typically enumerated by the system
		// Add a placeholder if no boards are available
		boardList.getItems().add("Board 0");

		if (params.boardIndex < boardList.getItems().size()) {
			boardList.getSelectionModel().select(params.boardIndex);
		}

		terminalType.getSelectionModel().select(params.differential ? 1 : 0);
	}

	@Override
	public boolean getParams() {
		MCCDaqParams params = mccDaqSystem.getMccDaqParams();
		params.boardIndex = boardList.getSelectionModel().getSelectedIndex();
		params.differential = (terminalType.getSelectionModel().getSelectedIndex() == 1);
		return true;
	}

	@Override
	public Object getParams(Object currParams) {
		return null;
	}

	@Override
	public void setParams(Object input) {
	}

	@Override
	public String getName() {
		return "MCC DAQ settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}
}
