package asiojni.layoutFX;

import java.util.List;

import Acquisition.layoutFX.DAQSettingsPane;
import asiojni.NewAsioSoundSystem;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * JavaFX settings pane for the NewAsioSoundSystem.
 * Provides a combo box to select the ASIO device and a settings button
 * to open the ASIO control panel.
 * 
 * @author Jamie Macaulay
 */
public class NewAsioDAQPane extends DAQSettingsPane {

	private PamBorderPane mainPane;

	private ComboBox<String> deviceList;

	private NewAsioSoundSystem newAsioSoundSystem;

	public NewAsioDAQPane(NewAsioSoundSystem newAsioSoundSystem) {
		this.newAsioSoundSystem = newAsioSoundSystem;

		mainPane = new PamBorderPane();
		PamVBox holder = new PamVBox();
		holder.setSpacing(5);

		Label title = new Label("Select Device");
		PamGuiManagerFX.titleFont2style(title);

		PamHBox deviceRow = new PamHBox();
		deviceRow.setSpacing(10);

		deviceList = new ComboBox<>();
		deviceList.setMaxWidth(Double.MAX_VALUE);
		javafx.scene.layout.HBox.setHgrow(deviceList, javafx.scene.layout.Priority.ALWAYS);

		PamButton settingsButton = new PamButton("Settings...");
		settingsButton.setOnAction(e -> {
			newAsioSoundSystem.showASIOControl();
		});

		deviceRow.getChildren().addAll(deviceList, settingsButton);

		holder.getChildren().addAll(title, deviceRow);
		mainPane.setCenter(holder);
	}

	@Override
	public void setParams() {
		if (newAsioSoundSystem.getSoundCardParameters().systemType == null)
			newAsioSoundSystem.getSoundCardParameters().systemType = newAsioSoundSystem.getSystemType();

		deviceList.getItems().clear();
		try {
			List<String> driverNames = newAsioSoundSystem.getDriverNames();
			if (driverNames != null) {
				for (String name : driverNames) {
					deviceList.getItems().add(name);
				}
			}
		} catch (Exception e) {
			System.out.println("Error getting ASIO driver names: " + e.getLocalizedMessage());
		}
		if (newAsioSoundSystem.getSoundCardParameters().deviceNumber < deviceList.getItems().size()) {
			deviceList.getSelectionModel().select(newAsioSoundSystem.getSoundCardParameters().deviceNumber);
		}
	}

	@Override
	public boolean getParams() {
		newAsioSoundSystem.getSoundCardParameters().deviceNumber = deviceList.getSelectionModel().getSelectedIndex();
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
		return "New ASIO settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}
}
