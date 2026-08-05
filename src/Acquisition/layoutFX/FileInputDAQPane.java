package Acquisition.layoutFX;

import Acquisition.FileInputParameters;
import Acquisition.FileInputSystem;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamComboBox;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * JavaFX settings pane for the FileInputSystem (single file input).
 * 
 * @author Jamie Macaulay
 */
public class FileInputDAQPane extends DAQSettingsPane<FileInputParameters> {

	/**
	 * The main pane.
	 */
	private PamBorderPane mainPane;

	/**
	 * Combo box for recent file names.
	 */
	private PamComboBox<String> fileNameCombo;

	/**
	 * Button to select a file.
	 */
	private PamButton selectFileButton;

	/**
	 * Check box for repeating playback.
	 */
	private CheckBox repeatCheckBox;

	/**
	 * Text field for skipping initial seconds.
	 */
	private TextField skipSecondsField;

	/**
	 * The file input system.
	 */
	private FileInputSystem fileInputSystem;

	public FileInputDAQPane(FileInputSystem fileInputSystem) {
		this.fileInputSystem = fileInputSystem;
		mainPane = new PamBorderPane();

		PamVBox holder = new PamVBox();
		holder.setSpacing(5);

		Label title = new Label("Select sound file");
		PamGuiManagerFX.titleFont2style(title);

		fileNameCombo = new PamComboBox<>();
		fileNameCombo.setMaxWidth(Double.MAX_VALUE);

		PamHBox controlsRow = new PamHBox();
		controlsRow.setSpacing(10);

		repeatCheckBox = new CheckBox("Repeat");

		selectFileButton = new PamButton("Select File");
		selectFileButton.setOnAction(e -> {
			// File selection handled externally via fileInputSystem
		});

		controlsRow.getChildren().addAll(repeatCheckBox, selectFileButton);

		PamHBox skipRow = new PamHBox();
		skipRow.setSpacing(5);
		skipRow.setPadding(new Insets(5, 0, 0, 0));
		skipRow.getChildren().addAll(
				new Label("Skip initial"),
				skipSecondsField = new TextField(),
				new Label("seconds")
		);
		skipSecondsField.setPrefColumnCount(5);

		holder.getChildren().addAll(title, fileNameCombo, controlsRow, skipRow);
		mainPane.setCenter(holder);
	}

	@Override
	public void setParams() {
		FileInputParameters params = fileInputSystem.getFileInputParameters();
		if (params == null) return;

		fileNameCombo.getItems().clear();
		if (params.recentFiles != null) {
			for (String file : params.recentFiles) {
				if (file != null && file.length() > 0) {
					fileNameCombo.getItems().add(file);
				}
			}
		}
		if (fileNameCombo.getItems().size() > 0) {
			fileNameCombo.getSelectionModel().select(0);
		}
		if (repeatCheckBox != null) {
			repeatCheckBox.setSelected(params.repeatLoop);
		}
		if (skipSecondsField != null) {
			skipSecondsField.setText(String.format("%.1f", params.skipStartFileTime / 1000.));
		}
	}

	@Override
	public boolean getParams() {
		FileInputParameters params = fileInputSystem.getFileInputParameters();
		if (params == null) return false;

		params.repeatLoop = repeatCheckBox.isSelected();
		try {
			params.skipStartFileTime = (long) (Double.parseDouble(skipSecondsField.getText()) * 1000.);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public FileInputParameters getParams(FileInputParameters currParams) {
		if (getParams()) return fileInputSystem.getFileInputParameters();
		return null;
	}

	@Override
	public void setParams(FileInputParameters input) {
		setParams();
	}

	@Override
	public String getName() {
		return "File input settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}
}
