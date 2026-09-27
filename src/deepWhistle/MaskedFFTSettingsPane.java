package deepWhistle;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.popOver.PamPopOver;
import pamViewFX.fxNodes.utilityPanes.FreqResolutionPane;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import rawDeepLearningClassifier.DLDownloadManager;
import rawDeepLearningClassifier.DLStatus;

/**
 * Simple JavaFX settings pane for DeepWhistleParameters.
 * <p>
 * The pane allows the user to select which {@link PamFFTMask} to use. Each mask
 * has its own settings pane (see {@link FFTMaskSettingsPane}) which is shown
 * when the mask is selected. When a new mask is selected its model is
 * downloaded - a progress indicator is shown and the controls are disabled
 * until the download completes.
 */
public class MaskedFFTSettingsPane extends DynamicSettingsPane<MaskedFFTParamters> {

    private MaskedFFTParamters params;
    private PamBorderPane main;
	private SourcePaneFX sourcePane;
	private FreqResolutionPane resolutionPane;

	/**
	 * Selection control for the type of mask to use.
	 */
	private ChoiceBox<FFTMaskType> maskChoiceBox;

	/**
	 * Holds the settings pane for the currently selected mask.
	 */
	private PamBorderPane maskPaneHolder;

	/**
	 * The settings pane for the currently selected mask.
	 */
	private FFTMaskSettingsPane currentMaskPane;

	/**
	 * Progress indicator shown whilst a model is downloading.
	 */
	private ProgressIndicator modelLoadIndicator;

	/**
	 * Label showing the model download status / model file name.
	 */
	private Label modelPathLabel;

	/**
	 * Button which lets the user import a custom (e.g. retrained) model file,
	 * replacing the downloaded online model.
	 */
	private PamButton importButton;

	/**
	 * Button which shows a description of the selected model and a link to its paper.
	 */
	private PamButton infoButton;

	/**
	 * Manages downloading (and unzipping) of models.
	 */
	private DLDownloadManager downloadManager = new DLDownloadManager();

	/**
	 * The local path to the currently downloaded model.
	 */
	private String currentModelPath;

	/**
	 * Flag set whilst parameters are being set programmatically so that the mask
	 * selection listener does not trigger a download.
	 */
	private boolean isSettingParams = false;

	/**
	 * Flag set whilst a model download is running so overlapping downloads are
	 * not started.
	 */
	private boolean downloadInProgress = false;

    public MaskedFFTSettingsPane(Object owner) {
        super(owner);
        createContent();
    }

    private void createContent() {

    	PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);

		sourcePane = new SourcePaneFX("Raw data source for FFT", FFTDataUnit.class, true, true);
		PamGuiManagerFX.titleFont2style(sourcePane.getTitleLabel());

		resolutionPane=new FreqResolutionPane();

		sourcePane.addSelectionListener((obsVal, newVal, oldVal) -> {
			//update the frequency resolution pane.
			resolutionPane.setParams((FFTDataBlock) sourcePane.getSource());
			//let the mask pane re-validate against the new source.
			if (currentMaskPane != null) {
				currentMaskPane.setFFTSource(getSelectedFFTSource());
			}
		});

		vBox.getChildren().add(sourcePane);

		vBox.getChildren().add(resolutionPane);

		vBox.getChildren().add(createMaskPane());

		main = new PamBorderPane();
		main.setPadding(new Insets(5,5,5,5));
		main.setCenter(vBox);


    }

    /**
     * Create the pane which allows the user to select a mask, shows the model
     * download status and holds the mask specific settings.
     *
     * @return the mask selection pane.
     */
    private Node createMaskPane() {

    	PamVBox maskBox = new PamVBox();
    	maskBox.setSpacing(5);

    	Label titleLabel = new Label("FFT Mask");
    	PamGuiManagerFX.titleFont2style(titleLabel);

    	//the mask selection control
    	maskChoiceBox = new ChoiceBox<FFTMaskType>();
    	maskChoiceBox.getItems().addAll(FFTMaskType.values());
    	maskChoiceBox.setTooltip(new Tooltip("Select the type of FFT mask to use to remove noise from the spectrogram"));

    	maskChoiceBox.getSelectionModel().selectedItemProperty().addListener((obsVal, oldVal, newVal) -> {
    		if (newVal == null) return;
    		setMaskPane(newVal);
    		if (!isSettingParams) {
    			//the user changed the mask - download the model for it.
    			downloadModel(newVal);
    		}
    	});

    	//the model download status row
    	modelLoadIndicator = new ProgressIndicator(-1);
    	modelLoadIndicator.setVisible(false);
    	modelLoadIndicator.setPrefSize(20, 20);

    	modelPathLabel = new Label("No model downloaded");
    	modelPathLabel.setMaxWidth(Double.MAX_VALUE);

    	//lets the user swap in a custom (retrained) model file in place of the online one.
    	importButton = new PamButton();
    	importButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-folder-open", PamGuiManagerFX.iconSize));
    	importButton.setTooltip(new Tooltip("Import a custom (retrained) model file. This replaces the "
    			+ "downloaded online model for the selected mask."));
    	importButton.setOnAction(e -> importCustomModel());
    	importButton.getStyleClass().add("icon-button"); //keeps the button small
    	maskChoiceBox.prefHeightProperty().bind(importButton.heightProperty());

    	
    	PamHBox selectBox = new PamHBox();
    	selectBox.setSpacing(5);
    	selectBox.setAlignment(Pos.CENTER_LEFT);
    	selectBox.setMaxWidth(Double.MAX_VALUE);
    	selectBox.getChildren().addAll(maskChoiceBox, importButton);
    	maskChoiceBox.setMaxWidth(Double.MAX_VALUE);
    	PamHBox.setHgrow(maskChoiceBox, Priority.ALWAYS);
    	selectBox.prefWidthProperty().bind(maskBox.widthProperty());
    	
    	//information button - shows a description of the selected model and a link to its paper.
    	infoButton = new PamButton();
    	infoButton.setGraphic(PamGlyphDude.createPamIcon("mdi2i-information-outline", PamGuiManagerFX.iconSize));
    	infoButton.setTooltip(new Tooltip("Show information about the selected model"));
    	infoButton.setOnAction(e -> showModelInfo());
    	infoButton.getStyleClass().add("icon-button"); //keeps the button small

    	PamHBox statusBox = new PamHBox();
    	statusBox.setSpacing(5);
    	statusBox.setAlignment(Pos.CENTER_LEFT);
    	statusBox.getChildren().addAll(modelLoadIndicator, modelPathLabel);
    	
    	PamBorderPane statusHolder = new PamBorderPane(); 
    	statusHolder.setLeft(statusBox);
    	statusHolder.setRight(infoButton);
    	BorderPane.setAlignment(infoButton, Pos.CENTER_RIGHT);
    	
    	//holder for the mask specific settings pane
    	maskPaneHolder = new PamBorderPane();

    	maskBox.getChildren().addAll(titleLabel, selectBox, statusHolder, maskPaneHolder);

    	return maskBox;
    }

    /**
     * Set the mask specific settings pane shown in the holder.
     *
     * @param maskType - the selected mask type.
     */
    private void setMaskPane(FFTMaskType maskType) {
    	currentMaskPane = maskType.createSettingsPane();
    	if (currentMaskPane != null) {
    		maskPaneHolder.setCenter(currentMaskPane.getContentNode());
    		if (params != null) {
    			currentMaskPane.setParams(params);
    		}
    		currentMaskPane.setFFTSource(getSelectedFFTSource());
    	}
    	else {
    		maskPaneHolder.setCenter(null);
    	}
    }

    /**
     * Get the currently selected FFT data source, or null if none / not an FFT block.
     *
     * @return the selected FFT data block.
     */
    private FFTDataBlock getSelectedFFTSource() {
    	if (sourcePane.getSource() instanceof FFTDataBlock) {
    		return (FFTDataBlock) sourcePane.getSource();
    	}
    	return null;
    }

    /**
     * Enable / disable the controls. Used to disable the controls whilst a model
     * is downloading.
     *
     * @param disable - true to disable the controls.
     */
    private void setControlsDisabled(boolean disable) {
    	sourcePane.setDisable(disable);
    	resolutionPane.setDisable(disable);
    	maskChoiceBox.setDisable(disable);
    	maskPaneHolder.setDisable(disable);
    	importButton.setDisable(disable);
    }

    /**
     * Let the user pick a custom (e.g. retrained) model file and use it in place of
     * the online model for the currently selected mask. The custom file simply
     * replaces the online model file: if a model has already been downloaded the
     * downloaded file is overwritten, otherwise the custom file is copied into the
     * same local model folder the online model would use. In both cases the model
     * has the same location and file name as the online model so the mask loads it
     * unchanged.
     */
    private void importCustomModel() {

    	FFTMaskType maskType = maskChoiceBox.getSelectionModel().getSelectedItem();
    	if (maskType == null) {
    		return;
    	}

    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Select custom " + maskType.getName() + " model file");
    	fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PyTorch model (*.pt)", "*.pt"));
    	fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));

    	File selected = fileChooser.showOpenDialog(main.getScene() == null ? null : main.getScene().getWindow());
    	if (selected == null) {
    		return;
    	}

    	try {
    		File target = getCustomModelTarget(maskType);
    		if (target == null) {
    			modelPathLabel.setText("Could not locate model folder");
    			return;
    		}

    		//copy the custom file over the online model file (unless it already is that file).
    		if (!selected.getCanonicalFile().equals(target.getCanonicalFile())) {
    			Files.copy(selected.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    		}

    		currentModelPath = target.getAbsolutePath();
    		if (params != null) {
    			params.modelPath = currentModelPath;
    		}
    		updateModelPathLabel();
    		notifySettingsListeners();

    	} catch (Exception e) {
    		System.err.println("MaskedFFTSettingsPane: failed to import custom model");
    		e.printStackTrace();
    		modelPathLabel.setGraphic(PamGlyphDude.createPamIcon("mdi2c-close-circle-outline", Color.RED, 10));
    		modelPathLabel.setText("Failed to import custom model");
    	}
    }

    /**
     * Show a pop-over describing the currently selected model, with a hyperlink to
     * the paper (a DOI link whose text is the full reference in Harvard format).
     */
    private void showModelInfo() {

    	FFTMaskType maskType = maskChoiceBox.getSelectionModel().getSelectedItem();
    	if (maskType == null) {
    		return;
    	}

    	PamVBox content = new PamVBox();
    	content.setSpacing(8);
    	content.setPadding(new Insets(10));
    	content.setMaxWidth(360);

    	Label title = new Label(maskType.getName() + " model");
    	PamGuiManagerFX.titleFont2style(title);
    	content.getChildren().add(title);

    	if (maskType.getDescription() != null) {
    		Label desc = new Label(maskType.getDescription());
    		desc.setWrapText(true);
    		desc.setMaxWidth(340);
    		content.getChildren().add(desc);
    	}

    	if (maskType.getReference() != null) {
    		//the link text is the full Harvard-format reference; clicking opens the DOI.
    		Hyperlink link = new Hyperlink(maskType.getReference());
    		link.setWrapText(true);
    		link.setMaxWidth(340);
    		String doiURL = maskType.getDoiURL();
    		if (doiURL != null) {
    			link.setTooltip(new Tooltip(doiURL));
    			link.setOnAction(e -> openUrl(doiURL));
    		}
    		content.getChildren().add(link);
    	}

    	PamPopOver popOver = new PamPopOver(content);
    	popOver.setResizeAbility(false);
    	popOver.setDetachable(false);
    	popOver.show(infoButton);
    }

    /**
     * Open a URL in the system default browser (on a background thread so the FX
     * thread is never blocked).
     *
     * @param url - the URL to open.
     */
    private void openUrl(String url) {
    	new Thread(() -> {
    		try {
    			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
    				Desktop.getDesktop().browse(new URI(url));
    			}
    			else {
    				System.err.println("MaskedFFTSettingsPane: opening a browser is not supported on this platform: " + url);
    			}
    		} catch (Exception ex) {
    			System.err.println("MaskedFFTSettingsPane: could not open URL " + url);
    			ex.printStackTrace();
    		}
    	}).start();
    }

    /**
     * Work out where a custom model file should be written so that it replaces the
     * online model for the given mask. If a model has already been downloaded this
     * is the downloaded file's location; otherwise it is the mask's expected model
     * file name inside the local model folder the online download would use.
     *
     * @param maskType - the selected mask.
     * @return the target file, or null if the model folder could not be created.
     */
    private File getCustomModelTarget(FFTMaskType maskType) throws Exception {

    	//if a model has already been downloaded, replace that exact file.
    	if (currentModelPath != null) {
    		return new File(currentModelPath);
    	}

    	//otherwise place the custom file where the online model would be downloaded to.
    	String modelName = FilenameUtils.getBaseName(new URI(maskType.getModelURL()).getPath());
    	String folder = DLDownloadManager.getModelFolder(modelName);
    	if (folder == null) {
    		return null;
    	}

    	String[] fileNames = maskType.getModelFileNames();
    	String fileName = (fileNames != null && fileNames.length > 0) ? fileNames[0] : "custom_model.pt";

    	return new File(folder, fileName);
    }

    /**
     * Download the model for the given mask on a separate thread, showing a
     * progress indicator and disabling the controls whilst the download is in
     * progress. The controls are re-enabled once the download completes.
     *
     * @param maskType - the mask whose model should be downloaded.
     * @return the download task or null if the mask has no model to download.
     */
    public Task<DLStatus> downloadModel(FFTMaskType maskType) {

    	String url = maskType.getModelURL();
    	if (url == null || url.isBlank()) {
    		return null;
    	}

    	//don't start a second download if one is already running.
    	if (downloadInProgress) {
    		return null;
    	}

    	URI uri;
    	try {
    		uri = new URI(url);
    	} catch (Exception e) {
    		System.err.println("MaskedFFTSettingsPane: invalid model URL: " + url);
    		e.printStackTrace();
    		modelPathLabel.setText("Invalid model URL");
    		return null;
    	}

    	//if the model has already been downloaded (and unzipped) on a previous
    	//occasion, use the local copy instead of downloading it again.
    	File existing = downloadManager.getExistingModelFile(uri, maskType.getModelFileNames());
    	if (existing != null && existing.exists()) {
    		System.out.println("MaskedFFTSettingsPane: model already downloaded, using local copy: " + existing.getAbsolutePath());
    		currentModelPath = existing.getAbsolutePath();
    		if (params != null) {
    			params.modelPath = currentModelPath;
    		}
    		updateModelPathLabel();
    		notifySettingsListeners();
    		return null;
    	}

    	downloadInProgress = true;
    	modelLoadIndicator.setVisible(true);
    	setControlsDisabled(true);

    	DownloadTask task = new DownloadTask(uri, maskType.getModelFileNames());

    	modelLoadIndicator.progressProperty().bind(task.progressProperty());
    	modelPathLabel.textProperty().bind(task.messageProperty());

    	Thread th = new Thread(task);
    	th.setDaemon(true);
    	th.start();

    	return task;
    }

    @Override
    public MaskedFFTParamters getParams(MaskedFFTParamters currParams) {
        if (currParams == null) currParams = new MaskedFFTParamters();
			//			fftParameters.rawDataSource = sourceList.getSelectedItem().toString();
        currParams.dataSourceIndex = sourcePane.getSourceIndex();
        currParams.dataSourceName = sourcePane.getSourceLongName();

        currParams.channelMap = sourcePane.getChannelList();

        currParams.maskType = maskChoiceBox.getSelectionModel().getSelectedItem();
        currParams.modelPath = currentModelPath;

        //get the mask specific parameters
        if (currentMaskPane != null) {
        	currentMaskPane.getParams(currParams);
        }

        return currParams;
    }

    @Override
    public void setParams(MaskedFFTParamters input) {
        this.params = input;
        if (input == null) input = new MaskedFFTParamters();

		// and fill in the data source list (may have changed - or might in later versions)
		PamDataBlock  datablock = PamController.getInstance().getFFTDataBlock(params.dataSourceName);
//		System.out.println("Data block to set for FFT source: "+datablock.getDataName() + " FFT PARAMS: "+fftParameters.dataSourceName);
		//fft settings
		sourcePane.setSource(datablock);
		sourcePane.setChannelList(params.channelMap); //set selected channels

		//the currently downloaded model path
		currentModelPath = params.modelPath;

		//set the mask selection without triggering a download
		isSettingParams = true;
		FFTMaskType maskType = params.maskType == null ? FFTMaskType.DEEP_WHISTLE : params.maskType;
		maskChoiceBox.getSelectionModel().select(maskType);
		setMaskPane(maskType);
		isSettingParams = false;

		//update the model status label
		updateModelPathLabel();

		//if the selected mask needs a model but one has not been downloaded (or the
		//downloaded file is missing) then start the download now. The selection listener
		//only fires when the user *changes* the mask, so without this the model for the
		//default mask would never download.
		if (maskType.getModelURL() != null && !isModelAvailable()) {
			downloadModel(maskType);
		}
    }

    /**
     * Check whether the model for the current mask has been downloaded and still
     * exists on disk.
     *
     * @return true if a valid local model file is available.
     */
    private boolean isModelAvailable() {
    	return currentModelPath != null && new File(currentModelPath).exists();
    }

    /**
     * Update the model path label to show the currently downloaded model.
     */
    private void updateModelPathLabel() {
    	modelPathLabel.setGraphic(null);
    	if (currentModelPath == null) {
    		modelPathLabel.setGraphic(PamGlyphDude.createPamIcon("mdi2c-close-circle-outline", Color.ORANGE, 10));
    		modelPathLabel.setText("No model downloaded - select a mask");
    		modelPathLabel.setTooltip(new Tooltip("Select a mask to download its model"));
    	}
    	else {
    		modelPathLabel.setText(new File(currentModelPath).getName());
    		modelPathLabel.setTooltip(new Tooltip(currentModelPath));
    	}
    }

    @Override
    public String getName() {
        return "Deep Whistle Mask Settings";
    }

    @Override
    public Node getContentNode() {
        return main;
    }

    @Override
    public void paneInitialized() {
        // nothing special
    }

    /**
     * Task which downloads (and if necessary unzips) a model on a separate
     * thread so that the GUI can show a progress indicator.
     */
    class DownloadTask extends Task<DLStatus> {

    	private URI uri;
    	private URI modelPath;
    	private String[] modelFileNames;

    	public DownloadTask(URI uri, String[] modelFileNames) {
    		this.uri = uri;
    		this.modelFileNames = modelFileNames;

    		downloadManager.clearDownloadListeners();
    		downloadManager.addDownloadListener((status, bytesDownLoaded) -> updateMessage(status, bytesDownLoaded));
    	}

    	private void updateMessage(DLStatus status, long bytesDownLoaded) {
    		this.updateProgress(-1, 1); //indeterminate
    		switch (status) {
    		case CONNECTION_TO_URL:
    			this.updateMessage("Checking URL");
    			break;
    		case DOWNLOADING:
    			this.updateMessage(String.format("Downloading %.2f MB", ((double) bytesDownLoaded) / 1024. / 1024.));
    			break;
    		case DOWNLOAD_FINISHED:
    			this.updateMessage("Download complete");
    			break;
    		case DOWNLOAD_STARTING:
    			this.updateMessage("Download starting");
    			break;
    		default:
    			this.updateMessage(status.getDescription());
    			break;
    		}
    	}

    	@Override
    	protected DLStatus call() throws Exception {
    		try {
    			this.updateMessage("Connecting to model...");

    			//either downloads the model or instantly returns the path if it's a local file.
    			//If the model is inside an archive, search for the mask's model file name(s).
    			if (modelFileNames != null) {
    				modelPath = downloadManager.downloadModel(uri, modelFileNames);
    			}
    			else {
    				modelPath = downloadManager.downloadModel(uri);
    			}

    			if (modelPath == null) {
    				return DLStatus.MODEL_DOWNLOAD_FAILED;
    			}

    			this.updateMessage("Download complete");
    			Thread.sleep(500); //just so the user sees something happened if the download is rapid.

    			return DLStatus.DOWNLOAD_FINISHED;

    		} catch (Exception e) {
    			System.out.println("-----UNABLE TO DOWNLOAD MODEL-----");
    			e.printStackTrace();
    			return DLStatus.MODEL_DOWNLOAD_FAILED;
    		}
    	}

    	private void finishedLoading() {

    		downloadInProgress = false;

    		//important to stop a bound property exception.
    		modelPathLabel.textProperty().unbind();
    		modelLoadIndicator.progressProperty().unbind();

    		modelLoadIndicator.setVisible(false);
    		setControlsDisabled(false);

    		DLStatus result = this.getValue();

    		if (result != null && !result.isError() && modelPath != null) {
    			currentModelPath = new File(modelPath).getAbsolutePath();
    			if (params != null) {
    				params.modelPath = currentModelPath;
    			}
    		}

    		updateModelPathLabel();

    		if (result != null && result.isError()) {
    			modelPathLabel.setGraphic(PamGlyphDude.createPamIcon("mdi2c-close-circle-outline", Color.RED, 10));
    			modelPathLabel.setText(result.getName());
    			modelPathLabel.setTooltip(new Tooltip(result.getDescription()));
    		}

    		notifySettingsListeners();
    	}

    	@Override
    	protected void succeeded() {
    		super.succeeded();
    		finishedLoading();
    	}

    	@Override
    	protected void cancelled() {
    		super.cancelled();
    		finishedLoading();
    	}

    	@Override
    	protected void failed() {
    		super.failed();
    		finishedLoading();
    	}
    }

}
