package nidaqdev.layoutFX;

import java.util.ArrayList;

import Acquisition.layoutFX.DAQSettingsPane;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nidaqdev.NIConstants;
import nidaqdev.NIDAQProcess;
import nidaqdev.NIDeviceInfo;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;

/**
 * JavaFX settings pane for the National Instruments DAQ system.
 * Provides controls for device selection, terminal configuration, 
 * and multi-board operation.
 * 
 * @author Jamie Macaulay
 */
public class NIDAQPane extends DAQSettingsPane {

	private PamBorderPane mainPane;

	private ComboBox<String> audioDevices;

	private ComboBox<String> inputType;

	private CheckBox allowMultiBoard;

	private Label warningText;

	private NIDAQProcess nidaqProcess;

	public NIDAQPane(NIDAQProcess nidaqProcess) {
		this.nidaqProcess = nidaqProcess;

		mainPane = new PamBorderPane();
		PamVBox holder = new PamVBox();
		holder.setSpacing(5);

		Label title = new Label("Select NI Device");
		PamGuiManagerFX.titleFont2style(title);

		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		int row = 0;

		gridPane.add(new Label("Master Device"), 0, row);
		audioDevices = new ComboBox<>();
		audioDevices.setMaxWidth(Double.MAX_VALUE);
		gridPane.add(audioDevices, 1, row);

		row++;
		warningText = new Label(" ");
		warningText.setStyle("-fx-text-fill: red;");
		gridPane.add(warningText, 0, row, 2, 1);

		row++;
		allowMultiBoard = new CheckBox("Use multiple DAQ boards");
		gridPane.add(allowMultiBoard, 0, row, 2, 1);

		row++;
		gridPane.add(new Label("Terminal Config"), 0, row);
		inputType = new ComboBox<>();
		inputType.getItems().addAll(
				"Referenced single ended",
				"Non-Referenced single ended",
				"Differential",
				"Pseudo Differential"
		);
		inputType.setMaxWidth(Double.MAX_VALUE);
		gridPane.add(inputType, 1, row);

		holder.getChildren().addAll(title, gridPane);
		mainPane.setCenter(holder);
	}

	@Override
	public void setParams() {
		if (nidaqProcess.getNiParameters().systemType == null)
			nidaqProcess.getNiParameters().systemType = nidaqProcess.getSystemType();

		audioDevices.getItems().clear();
		ArrayList<NIDeviceInfo> devices = nidaqProcess.getNiDevices();
		if (devices != null) {
			for (int i = 0; i < devices.size(); i++) {
				audioDevices.getItems().add(devices.get(i).toString());
			}
		}
		if (nidaqProcess.getNiParameters().deviceNumber < audioDevices.getItems().size()) {
			audioDevices.getSelectionModel().select(nidaqProcess.getNiParameters().deviceNumber);
		}

		switch (nidaqProcess.getNiParameters().terminalConfiguration) {
		case NIConstants.DAQmx_Val_RSE:
			inputType.getSelectionModel().select(0);
			break;
		case NIConstants.DAQmx_Val_NRSE:
			inputType.getSelectionModel().select(1);
			break;
		case NIConstants.DAQmx_Val_Diff:
			inputType.getSelectionModel().select(2);
			break;
		case NIConstants.DAQmx_Val_PseudoDiff:
			inputType.getSelectionModel().select(3);
			break;
		}

		allowMultiBoard.setSelected(nidaqProcess.getNiParameters().enableMultiBoard);
	}

	@Override
	public boolean getParams() {
		nidaqProcess.getNiParameters().deviceNumber = audioDevices.getSelectionModel().getSelectedIndex();
		if (nidaqProcess.getNiParameters().deviceNumber < 0) return false;

		switch (inputType.getSelectionModel().getSelectedIndex()) {
		case 0:
			nidaqProcess.getNiParameters().terminalConfiguration = NIConstants.DAQmx_Val_RSE;
			break;
		case 1:
			nidaqProcess.getNiParameters().terminalConfiguration = NIConstants.DAQmx_Val_NRSE;
			break;
		case 2:
			nidaqProcess.getNiParameters().terminalConfiguration = NIConstants.DAQmx_Val_Diff;
			break;
		case 3:
			nidaqProcess.getNiParameters().terminalConfiguration = NIConstants.DAQmx_Val_PseudoDiff;
			break;
		default:
			nidaqProcess.getNiParameters().terminalConfiguration = NIConstants.DAQmx_Val_NRSE;
		}

		nidaqProcess.getNiParameters().enableMultiBoard = allowMultiBoard.isSelected();
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
		return "NI DAQ settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}
}
