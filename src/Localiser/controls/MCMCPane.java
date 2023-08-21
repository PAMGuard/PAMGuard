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
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;

public class MCMCPane extends SettingsPane<MCMCParams2> {

	private PamBorderPane mainPane;
	private PamSpinner<Integer> mcmJumpSpinner;
	
	
	private PamSpinner<Double> jumpXSpinner;
	private PamSpinner<Double> jumpYSpinner;
	private PamSpinner<Double> jumpZSpinner;
	
	private double JUMP_SPINNER_WIDTH = 60;
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
		
	
		
		PamHBox holder = new PamHBox();
		holder.setSpacing(5);
		holder.setAlignment(Pos.CENTER_LEFT);
		
		Label label3 = new Label("Start"); 
		label3.setAlignment(Pos.CENTER_RIGHT);
		gridPane.add(label3, col, row);
		col++;
		
		holder.getChildren().add(numChains = new PamSpinner<Integer>(1,Integer.MAX_VALUE, 20, 1));
		numChains.setEditable(true);
		numChains.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		numChains.setMaxWidth(JUMP_SPINNER_WIDTH);
		
		holder.getChildren().add(new Label("chains"));
		
		holder.getChildren().add(startDispersion = new PamSpinner<Double>(0.,Double.MAX_VALUE, 100., 1.));
		startDispersion.setEditable(true);
		startDispersion.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		startDispersion.setMaxWidth(JUMP_SPINNER_WIDTH+10);

		holder.getChildren().add(new Label("m apart"));

		gridPane.add(holder, col, row);
		GridPane.setColumnSpan(holder, 6);
		
		row++; 
		col=0; 

		
		//chain propertires
		
		Label label = new Label("No. jumps"); 
		label.setAlignment(Pos.CENTER_RIGHT);
		
		gridPane.add(label, col, row);
		col++;
		gridPane.add(mcmJumpSpinner = new PamSpinner<Integer>(10,Integer.MAX_VALUE, 2500000, 10000), col, row);
		mcmJumpSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		GridPane.setColumnSpan(mcmJumpSpinner, 3);
		mcmJumpSpinner.setEditable(true);
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
		
		//initial conditions
		


	
		//chain clustering
		
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

		GridPane.setColumnSpan(clustering, 2);
		
		
		row++; 
		col=0; 
		
		Label label6 = new Label("No. k-means"); 
		label6.setAlignment(Pos.CENTER_RIGHT);
		gridPane.add(label6, col, row);
		col++;
		gridPane.add(numkMeans = new PamSpinner<Integer>(1,Integer.MAX_VALUE, 20, 1), col, row);
		numkMeans.setEditable(true);
		numkMeans.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		numkMeans.setMaxWidth(JUMP_SPINNER_WIDTH);
		

		Label label7 = new Label("with max distance"); 
		label7.setAlignment(Pos.CENTER_RIGHT);
		gridPane.add(label7, col, row);
		col++;
		gridPane.add(maxClustDist = new PamSpinner<Double>(0.,Double.MAX_VALUE, 5., 1), col, row);
		maxClustDist.setEditable(true);
		maxClustDist.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxClustDist.setMaxWidth(JUMP_SPINNER_WIDTH);
		GridPane.setColumnSpan(startDispersion, 2);
		col=col+2;
		gridPane.add(new Label("m"), col, row);
		
		


		
		mcmJumpSpinner.setEditable(true);		
		
		vBox.getChildren().add(gridPane); 
		
		return vBox;
	}

	@Override
	public MCMCParams2 getParams(MCMCParams2 currParams) {
		// TODO Auto-generated method stub
		return currParams;
	}

	@Override
	public void setParams(MCMCParams2 input) {
		// TODO Auto-generated method stub
		
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
