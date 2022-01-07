package rawDeepLearningClassifier.dlClassification.orcaSpot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import PamController.PamController;
import PamController.SettingsPane;
import PamView.dialog.PamDialog;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;

/**
 * Contains OrcaSpot specific settings fields. 
 * 
 * @author Jamie Macaulay. 
 *
 */
public class OrcaSpotPane extends SettingsPane<OrcaSpotParams2> {

	private Pane mainPane;

	/**
	 * The currently selected directorty. 
	 */
	private File currentSelectedFile;

	/**
	 * The directory chooser. 
	 */
	private DirectoryChooser dirChooser;

	/**
	 * Spinner to set the probability threshold for the call type classification
	 */
	private PamSpinner<Double> classifierSpinner;

	/**
	 * Spinner to set the probability threshold for initial detection stage
	 */
	private PamSpinner<Double> detectionSpinner;

	/**
	 * Check box for enabling the classifier. 
	 */
	private CheckBox enableClassifier;

	/**
	 * Check box for enabling the detector
	 */
	private CheckBox enableDetector;

	/**
	 * The current params
	 */
	private OrcaSpotParams2 currentParams;

	/**
	 * Check box for using CUDA (i.e. running the classifier on a graphics card)
	 */
	private CheckBox useCuda;

	private Label pathLabel;



	public OrcaSpotPane() {
		super(null);
		this.mainPane = createPane(); 

		//the directory chooser. 
		dirChooser = new DirectoryChooser();
		dirChooser.setTitle("OrcaSpot location");
	}

	/**
	 * Create the pane for the OrcaSpot classifier. 
	 */
	private Pane createPane() {
		PamBorderPane mainPane = new PamBorderPane(); 


		Label classiferInfoLabel = new Label("OrcaSpot Classifier Settings"); 
		PamGuiManagerFX.titleFont2style(classiferInfoLabel);

		/**Basic classifier info**/
		Label locationLabel = new Label("Classifier Location:"); 
		PamButton pamButton = new PamButton("Browse..."); 

		pamButton.setOnAction((action)->{

			Path path = currentSelectedFile.toPath();
			if(Files.exists(path, LinkOption.NOFOLLOW_LINKS))
			{
				dirChooser.setInitialDirectory(currentSelectedFile);
			}
			else
			{ 
				dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			}

		});

		PamHBox hBox = new PamHBox(); 
		hBox.setSpacing(5);
		hBox.getChildren().addAll(locationLabel, pamButton); 
		hBox.setAlignment(Pos.CENTER_LEFT);

		//label to show path
		pathLabel = new Label(); 

		useCuda = new CheckBox("CUDA"); 


		/**Classification thresholds etc to set.**/
		Label classiferInfoLabel2 = new Label("OrcaSpot Classifcation Thresholds"); 
		PamGuiManagerFX.titleFont2style(classiferInfoLabel2);

		/**
		 * There are tow classifiers the detector and the classifier
		 */
		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		gridPane.add(new Label("Call detection"), 0, 0);
		gridPane.add(detectionSpinner = new PamSpinner<Double>(0.0, 1.0, 0.9, 0.1), 1, 0);
		detectionSpinner.setPrefWidth(100);
		detectionSpinner.setEditable(true);
		detectionSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		gridPane.add(enableDetector = new CheckBox ("Enable"), 2, 0);
		enableDetector.setOnAction(action->{
			enableControls(); 
		});

		gridPane.add(new Label("Call type classiifcation"), 0, 1);
		gridPane.add(classifierSpinner = new PamSpinner<Double>(0.0, 1.0, 0.9, 0.1), 1, 1);
		classifierSpinner.setPrefWidth(100);
		classifierSpinner.setEditable(true);
		classifierSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);

		gridPane.add(enableClassifier = new CheckBox ("Enable"), 2, 1);
		enableClassifier.setOnAction(action->{
			enableControls(); 
		});

		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);
		vBox.getChildren().addAll(classiferInfoLabel, hBox, pathLabel, useCuda, classiferInfoLabel2, gridPane); 

		mainPane.setCenter(vBox);

		return mainPane; 

	}


	/**
	 * Update the path label and tool tip text; 
	 */
	private void updatePathLabel() {
		pathLabel .setText(this.currentSelectedFile.getPath()); 
		pathLabel.setTooltip(new Tooltip(this.currentSelectedFile.getPath()));
	}

	private void enableControls() {
		detectionSpinner.setDisable(!enableDetector.isSelected());
		classifierSpinner.setDisable(!enableClassifier.isSelected());
	}

	@Override
	public OrcaSpotParams2 getParams(OrcaSpotParams2 currParams) {

		if (currentSelectedFile==null) {
			//uuurgh need to sort this out with FX stuff
			PamDialog.showWarning(PamController.getInstance().getGuiFrameManager().getGuiFrame(), 
					"Path is not set correctly", 
					"The path is null for some reason. You need to browse to the correct OrcaSpot master path"); 
		}
		else {
			currParams.segmenterMasterPath =  currentSelectedFile.getPath(); 
			currParams.updateAllPaths(); 
		}

		currParams.threshold = String.valueOf(detectionSpinner.getValue()); 
		currParams.threshold2 = String.valueOf(classifierSpinner.getValue()); 

		currParams.useDetector = enableDetector.isSelected(); 
		currParams.useClassifier = enableClassifier.isSelected(); 
		currParams.cuda = useCuda.isSelected(); 

		return currParams;
	}

	@Override
	public void setParams(OrcaSpotParams2 currParams) {
		this.currentParams = currParams.clone(); 

		currentSelectedFile = new File(currentParams.segmenterMasterPath);

		updatePathLabel();

		pathLabel .setText(this.currentSelectedFile.getPath()); 

		detectionSpinner.getValueFactory().setValue(Double.valueOf(currParams.threshold));
		classifierSpinner.getValueFactory().setValue(Double.valueOf(currParams.threshold2));

		enableDetector.setSelected(currParams.useDetector);
		enableClassifier.setSelected(currParams.useClassifier);

		useCuda.setSelected(currParams.cuda);

		enableControls();
	}


	@Override
	public String getName() {
		return "OrcaSpotSettings";
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
