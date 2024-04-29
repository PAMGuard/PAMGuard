package Localiser.controls;

import Localiser.algorithms.genericLocaliser.MCMC.MCMCParams2;
import PamController.SettingsPane;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ComboBox;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;

public class MCMCPane extends SettingsPane<MCMCParams2> {

	private PamBorderPane mainPane;
	
	private double JUMP_SPINNER_WIDTH = 60;

	
	//controls
	private PamSpinner<Integer> numJumps;
	
	
	private PamSpinner<Double> jumpXSpinner;
	private PamSpinner<Double> jumpYSpinner;
	private PamSpinner<Double> jumpZSpinner;
	
	private PamSpinner<Double> startDispersion;
	private PamSpinner<Integer> numChains;
	private ComboBox<String> clustering;
	private PamSpinner<Integer> numkMeans;
	private PamSpinner<Double> maxClustDist;

	public MCMCPane() {
		super(null); 
		mainPane = new PamBorderPane(); 
		mainPane.setTop(createMCMCPane());
		
	}
	
	private Pane createMCMCPane() {
		PamVBox vBox = new 	PamVBox(); 
		
		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		
		int row = 0; 
		int col = 0; 
		
		Label chainTitleLabel = new Label("Markov chain settings"); 
//		PamGuiManagerFX.titleFont2style(chainTitleLabel);
		chainTitleLabel.setFont(Font.font(null,FontWeight.BOLD, 11));

		gridPane.add(chainTitleLabel, col, row);
		GridPane.setColumnSpan(chainTitleLabel, 7);
		row++;
		
	
		col=0; 
		PamHBox chainHolder = new PamHBox();
		chainHolder.setSpacing(5);
		chainHolder.setAlignment(Pos.CENTER_LEFT);
		
		Label label3 = new Label("Start"); 
		label3.setAlignment(Pos.CENTER_RIGHT);
		gridPane.add(label3, col, row);
		col++;
		
		chainHolder.getChildren().add(numChains = new PamSpinner<Integer>(1,Integer.MAX_VALUE, 20, 1));
		numChains.setEditable(true);
		numChains.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		numChains.setMaxWidth(JUMP_SPINNER_WIDTH);
		
		chainHolder.getChildren().add(new Label("chains seperated by"));
		
		chainHolder.getChildren().add(startDispersion = new PamSpinner<Double>(0.,Double.MAX_VALUE, 100., 1.));
		startDispersion.setEditable(true);
		startDispersion.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		startDispersion.setMaxWidth(JUMP_SPINNER_WIDTH+10);

		chainHolder.getChildren().add(new Label("m"));

		gridPane.add(chainHolder, col, row);
		GridPane.setColumnSpan(chainHolder, 7);
		
		row++; 
		col=0; 

		
		//chain propertires
		
		Label label = new Label("No. jumps"); 
		label.setAlignment(Pos.CENTER_RIGHT);
		
		gridPane.add(label, col, row);
		col++;
		gridPane.add(numJumps = new PamSpinner<Integer>(10,Integer.MAX_VALUE, 2500000, 10000), col, row);
		numJumps.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		GridPane.setColumnSpan(numJumps, 3);
		numJumps.setEditable(true);
		//mcmJumpSpinner.setMaxWidth(JUMP_SPINNER_WIDTH*2);

		col+=col+2; 
		Label chainLabel = new Label("per chain");
		gridPane.add(chainLabel, col, row);
		GridPane.setColumnSpan(chainLabel, 3);
	
		row++; 
		col=0; 
		Label label2 = new Label("Jump size x"); 
		label2.setAlignment(Pos.CENTER_RIGHT);
		
		gridPane.add(label2, col, row);
		col++;
		
		
		gridPane.add(jumpXSpinner = new PamSpinner<Double>(0.,Double.MAX_VALUE, 1., 0.5), col, row);
		jumpXSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		jumpXSpinner.setMaxWidth(JUMP_SPINNER_WIDTH);
		jumpXSpinner.setEditable(true);
		col++;
		
		gridPane.add(new Label("y"), col, row);
		col++;
		
		gridPane.add(jumpYSpinner = new PamSpinner<Double>(0.,Double.MAX_VALUE, 1., 0.5), col, row);
		jumpYSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		jumpYSpinner.setMaxWidth(JUMP_SPINNER_WIDTH);
		jumpYSpinner.setEditable(true);
		col++;
		
		gridPane.add(new Label("z"), col, row);
		col++;
		
		gridPane.add(jumpZSpinner = new PamSpinner<Double>(0.,Double.MAX_VALUE, 1., 0.5), col, row);
		jumpZSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		jumpZSpinner.setMaxWidth(JUMP_SPINNER_WIDTH);
		jumpZSpinner.setEditable(true);
		col++;
		
		gridPane.add(new Label("m"), col, row);
		row++;
		col=0; 


	
		//chain clustering
		Label clusterTitleLabel = new Label("Result clustering"); 
		//PamGuiManagerFX.titleFont2style(clusterTitleLabel);
		clusterTitleLabel.setFont(Font.font(null,FontWeight.BOLD, 11));

		gridPane.add(clusterTitleLabel, col, row);
		GridPane.setColumnSpan(clusterTitleLabel, 7);
		row++;
		
		
		row++; 
		col=0; 
		Label label5 = new Label("Clustering"); 
		label5.setAlignment(Pos.CENTER_RIGHT);
		gridPane.add(label5, col, row);
		col++;
		clustering = new ComboBox<String>(); 
		clustering.getItems().addAll("None", "kMeans"); 
		clustering.getSelectionModel().select(1);
		gridPane.add(clustering, col, row);

		GridPane.setColumnSpan(clustering, 3);
		
		clustering.setOnAction((action)->{
			enableControls(); 
		});
		
		
		row++; 
		col=0; 
		
		
		//kmeans settings - TODO - would be better to have a custom pane for each clustering algorithm 
		//but not worth the effort until more clustering algorithms are implemented. 
		Label label6 = new Label("Start"); 
		label6.setAlignment(Pos.CENTER_RIGHT);
		gridPane.add(label6, col, row);
		col++;
		
		
		PamHBox kMeansHolder = new PamHBox();
		kMeansHolder.setSpacing(5);
		kMeansHolder.setAlignment(Pos.CENTER_LEFT);
		
		kMeansHolder.getChildren().add(numkMeans = new PamSpinner<Integer>(1,Integer.MAX_VALUE, 20, 1));
		numkMeans.setEditable(true);
		numkMeans.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		numkMeans.setMaxWidth(JUMP_SPINNER_WIDTH);
		

		kMeansHolder.getChildren().add(new Label("k-means and merge at <"));
		kMeansHolder.getChildren().add(maxClustDist = new PamSpinner<Double>(0.,Double.MAX_VALUE, 5., 1));
		maxClustDist.setEditable(true);
		maxClustDist.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxClustDist.setMaxWidth(JUMP_SPINNER_WIDTH);
		kMeansHolder.getChildren().add(new Label("m"));

		gridPane.add(kMeansHolder, col, row);
		GridPane.setColumnSpan(kMeansHolder, 7);

		vBox.getChildren().add(gridPane); 
		
		enableControls();
		
		return vBox;
	}

	private void enableControls() {
		//clustering
		numkMeans.setDisable(true); 		
		maxClustDist.setDisable(true); 	
		switch (clustering.getSelectionModel().getSelectedIndex()) {
		case MCMCParams2.K_MEANS:
			numkMeans.setDisable(false); 		
			maxClustDist.setDisable(false); 	
			break;
		case MCMCParams2.NONE:
			break;
		}
	}

	@Override
	public MCMCParams2 getParams(MCMCParams2 currParams) {
		
		//chain settings. 
		currParams.numberOfChains = numChains.getValue();
		currParams.numberOfJumps = numJumps.getValue();

		double[] jumpSize = new double[3]; 
		
		jumpSize[0] = jumpXSpinner.getValue();
		jumpSize[1] = jumpYSpinner.getValue();
		jumpSize[2] = jumpZSpinner.getValue();
		
		currParams.jumpSize = jumpSize;

		//bit messy but works...
		currParams.setChainDispersion(startDispersion.getValue(), 3);
		
		//cluster settings
		currParams.clusterAnalysis = clustering.getSelectionModel().getSelectedIndex(); 
		currParams.kmeanAttempts = numkMeans.getValue();
		currParams.maxClusterSize = maxClustDist.getValue();
		
		return currParams;
	}

	@Override
	public void setParams(MCMCParams2 currParams) {
		
		//chain settings. 
		numJumps.getValueFactory().setValue(currParams.numberOfJumps);
		 
		jumpXSpinner.getValueFactory().setValue(currParams.jumpSize[0]);
		jumpYSpinner.getValueFactory().setValue(currParams.jumpSize[1]);
		jumpZSpinner.getValueFactory().setValue(currParams.jumpSize[2]);
		
		numChains.getValueFactory().setValue(currParams.numberOfChains);
		
		//bit messy but works...
		startDispersion.getValueFactory().setValue(Math.abs(currParams.chainStartDispersion[0][0]));
		
		
		//cluster settings
		clustering.getSelectionModel().select(currParams.clusterAnalysis);
		numkMeans.getValueFactory().setValue(currParams.kmeanAttempts);
		maxClustDist.getValueFactory().setValue(currParams.maxClusterSize);

		//enable the controls.
		enableControls();
	}

	@Override
	public String getName() {
		return "MCMC Settings";
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
