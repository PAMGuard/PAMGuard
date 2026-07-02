package asiojni.layoutFX;

import Acquisition.layoutFX.DAQSettingsPane;
import asiojni.ASIOSoundSystem;
import asiojni.AsioDriverInfo;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;

import java.util.ArrayList;

/**
 * JavaFX settings pane for the ASIOSoundSystem.
 * Provides a combo box to select the ASIO audio device.
 * 
 * @author Jamie Macaulay
 */
public class ASIOSoundDAQPane extends DAQSettingsPane {

	private PamBorderPane mainPane;

	private ComboBox<String> audioDevices;

	private ASIOSoundSystem asioSoundSystem;

	public ASIOSoundDAQPane(ASIOSoundSystem asioSoundSystem) {
		this.asioSoundSystem = asioSoundSystem;

		mainPane = new PamBorderPane();
		PamVBox holder = new PamVBox();
		holder.setSpacing(5);

		Label title = new Label("Select ASIO audio line");
		PamGuiManagerFX.titleFont2style(title);

		audioDevices = new ComboBox<>();
		audioDevices.setMaxWidth(Double.MAX_VALUE);

		holder.getChildren().addAll(title, audioDevices);
		mainPane.setCenter(holder);
	}

	@Override
	public void setParams() {
		if (asioSoundSystem.getSoundCardParameters().systemType == null)
			asioSoundSystem.getSoundCardParameters().systemType = asioSoundSystem.getSystemType();

		ArrayList<AsioDriverInfo> devices = asioSoundSystem.getDevicesList();
		audioDevices.getItems().clear();
		for (int i = 0; i < devices.size(); i++) {
			audioDevices.getItems().add(devices.get(i).driverName);
		}
		if (asioSoundSystem.getSoundCardParameters().deviceNumber < devices.size()) {
			audioDevices.getSelectionModel().select(asioSoundSystem.getSoundCardParameters().deviceNumber);
		}
	}

	@Override
	public boolean getParams() {
		if (audioDevices != null) {
			asioSoundSystem.getSoundCardParameters().deviceNumber = audioDevices.getSelectionModel().getSelectedIndex();
		}
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
		return "ASIO sound card settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}
}
