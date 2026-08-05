package hfDaqCard.layoutFX;

import Acquisition.layoutFX.DAQSettingsPane;
import hfDaqCard.SmruDaqParameters;
import hfDaqCard.SmruDaqSystem;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;

/**
 * JavaFX settings pane for the SAIL/SMRU DAQ Card system.
 * Provides controls for sample rate, per-channel enable, gain, and filter settings.
 * 
 * @author Jamie Macaulay
 */
public class SmruDaqPane extends DAQSettingsPane {

	private PamBorderPane mainPane;

	private ComboBox<String> sampleRate;

	private CheckBox[] channelEnable;

	private ComboBox<String>[] gains;

	private ComboBox<String>[] filters;

	private Label libVersion;

	private SmruDaqSystem smruDaqSystem;

	private int nShownBoards;

	@SuppressWarnings("unchecked")
	public SmruDaqPane(SmruDaqSystem smruDaqSystem) {
		this.smruDaqSystem = smruDaqSystem;

		int nBoards = smruDaqSystem.getSmruDaqJNI().getnDevices();
		nShownBoards = Math.max(nBoards, 1);
		int totalChannels = nShownBoards * SmruDaqParameters.NCHANNELS;

		channelEnable = new CheckBox[totalChannels];
		gains = new ComboBox[totalChannels];
		filters = new ComboBox[totalChannels];

		mainPane = new PamBorderPane();
		PamVBox holder = new PamVBox();
		holder.setSpacing(5);

		Label title = new Label("DAQ Card Configuration");
		PamGuiManagerFX.titleFont2style(title);

		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(3);

		int row = 0;

		// Library version
		gridPane.add(new Label("Library version:"), 0, row);
		libVersion = new Label(smruDaqSystem.getJNILibInfo());
		gridPane.add(libVersion, 1, row, 3, 1);

		// Sample rate
		row++;
		gridPane.add(new Label("Sample Rate"), 0, row);
		sampleRate = new ComboBox<>();
		for (int i = 0; i < SmruDaqParameters.sampleRates.length; i++) {
			sampleRate.getItems().add(String.format("%3.1f kHz", SmruDaqParameters.sampleRates[i] / 1000.));
		}
		sampleRate.setMaxWidth(Double.MAX_VALUE);
		gridPane.add(sampleRate, 1, row, 3, 1);

		// Per-board channel controls
		int iBoard = 0;
		for (int b = 0; b < nShownBoards; b++) {
			row++;
			Label boardLabel;
			if (b < nBoards) {
				long sn = smruDaqSystem.getSmruDaqJNI().getSerialNumber(b);
				boardLabel = new Label(String.format("Board %d s/n %X", b, sn));
			} else {
				boardLabel = new Label("*** No board installed ***");
				boardLabel.setStyle("-fx-text-fill: red;");
			}
			gridPane.add(boardLabel, 0, row, 4, 1);

			for (int i = 0; i < SmruDaqParameters.NCHANNELS; i++) {
				row++;
				channelEnable[iBoard] = new CheckBox("Ch " + iBoard);
				gridPane.add(channelEnable[iBoard], 0, row);

				gridPane.add(new Label("Gain"), 1, row);
				gains[iBoard] = new ComboBox<>();
				double[] gainVals = SmruDaqParameters.getGains();
				for (int g = 0; g < gainVals.length; g++) {
					gains[iBoard].getItems().add(String.format("%3.1f dB", gainVals[g]));
				}
				gridPane.add(gains[iBoard], 2, row);

				gridPane.add(new Label("Filter"), 3, row);
				filters[iBoard] = new ComboBox<>();
				for (int f = 0; f < SmruDaqParameters.filters.length; f++) {
					if (SmruDaqParameters.filters[f] == 0) {
						filters[iBoard].getItems().add("Off");
					} else {
						filters[iBoard].getItems().add(String.format("%.0f Hz", SmruDaqParameters.filters[f]));
					}
				}
				gridPane.add(filters[iBoard], 4, row);

				iBoard++;
			}
		}

		holder.getChildren().addAll(title, gridPane);
		mainPane.setCenter(holder);
	}

	@Override
	public void setParams() {
		SmruDaqParameters params = smruDaqSystem.getSmruDaqParameters();
		if (params == null) return;

		if (params.systemType == null)
			params.systemType = smruDaqSystem.getSystemType();

		sampleRate.getSelectionModel().select(params.sampleRateIndex);

		int totalChannels = nShownBoards * SmruDaqParameters.NCHANNELS;
		for (int i = 0; i < totalChannels; i++) {
			if (channelEnable[i] != null) {
				channelEnable[i].setSelected((params.channelMask & (1 << i)) != 0);
			}
			if (gains[i] != null && params.channelGainIndex[i] < gains[i].getItems().size()) {
				gains[i].getSelectionModel().select(params.channelGainIndex[i]);
			}
			if (filters[i] != null && params.channelFilterIndex[i] < filters[i].getItems().size()) {
				filters[i].getSelectionModel().select(params.channelFilterIndex[i]);
			}
		}
	}

	@Override
	public boolean getParams() {
		SmruDaqParameters params = smruDaqSystem.getSmruDaqParameters();
		if (params == null) return false;

		params.sampleRateIndex = sampleRate.getSelectionModel().getSelectedIndex();
		if (params.sampleRateIndex < 0) return false;

		int totalChannels = nShownBoards * SmruDaqParameters.NCHANNELS;
		int channelMask = 0;
		for (int i = 0; i < totalChannels; i++) {
			if (channelEnable[i] != null && channelEnable[i].isSelected()) {
				channelMask |= (1 << i);
			}
			if (gains[i] != null) {
				params.channelGainIndex[i] = gains[i].getSelectionModel().getSelectedIndex();
			}
			if (filters[i] != null) {
				params.channelFilterIndex[i] = filters[i].getSelectionModel().getSelectedIndex();
			}
		}
		params.channelMask = channelMask;

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
		return "SAIL DAQ Card settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}
}
