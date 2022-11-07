package dataModelFX.connectionNodes;

import java.util.ArrayList;

import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamStackPane;
import pamViewFX.fxNodes.PamVBox;
import javafx.util.Duration;

/**
 * A node which shows the internal structure of a PamControlledUnit
 * 
 * @author Jamie Macaulay
 *
 */
public class ModuleProcessDiagram extends PamBorderPane {
	
	/**
	 * The pane shows the internal data model of this PamControlledUnit. 
	 */
	private PamControlledUnit pamControlledUnit;
	
	/**
	 * Pane which holds main controls. 
	 */
	private PamBorderPane mainPane;
	
	private Color dataBlockColour=Color.YELLOW;
	
	private Color processColour=Color.LIGHTGREEN;
	
	private Color lineColor=Color.LIMEGREEN;

	private int lineWidth=3;
	
	
	private ArrayList<ProcessNode> processNodes;
	private ArrayList<DataBlockNode> dataBlockNodes;
	
	/**
	 * Padding around the chain. 
	 */
	private Insets chainPadding=new Insets(5,10, 5,10);

	/**
	 * Update timeline. 
	 */
	private Timeline timeline;

	
	public ModuleProcessDiagram (PamControlledUnit pamControlledUnit){
		//this.getStylesheets().add(PamController.getInstance().getGuiManagerFX().getPamSettingsCSS());
		this.pamControlledUnit=pamControlledUnit; 
		createUpdateTimer();
		createControlDataModel();
	}
	
	
	private void createControlDataModel(){
		this.setMinWidth(100);
		this.setPrefWidth(USE_COMPUTED_SIZE); 
		//this.setStyle("-fx-background-color: -fx-darkbackground");
		this.populateDataModel();
	}
	
	private void createUpdateTimer(){
		//start a timer
		timeline = new Timeline(new KeyFrame(
				Duration.millis(1000),
				ae -> {
					updateProcessNodes();
					updateDataBlocks();
				}));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	} 
	
	public void populateDataModel(){
		//need to redraw every time we populate the model. 
		timeline.stop();
		this.setCenter(createDataModelNode(calcProcessModel()));
		timeline.play();
	}
	
	/**
	 * FIXME-possible threading issue forseen here
	 * Update CPU in process nodes. 
	 */
	private void updateProcessNodes(){
		if (processNodes!=null){
			for (int i=0; i<processNodes.size(); i++){
				processNodes.get(i).updateCpuUsage();
			}
		}
	}
	
	/**
	 * Update datablock nodes. 
	 */
	private void updateDataBlocks(){
		if (dataBlockNodes!=null){
			for (int i=0; i<dataBlockNodes.size(); i++){
				dataBlockNodes.get(i).updateBlockCount(); 
			}
		}
	}
	

	/**
	 * Create a diagram of the data model of a PamControlledUnit. The JavaFX process is relatively simple. Each link in the process->datablock 
	 * chain has it's own VBox, either filled with process or data block nodes. Each VBox is than added to  StackPane with an overlayed transparent pane. 
	 * The transparent pane is used to draw line son- no layout rules apply to Pane and the Pane is the same size of the VBox thereofore line positions 
	 * can be bound to process and data block nodes. 
	 * @param controlsProcessModel - diagram data.
	 * @return a node containing a diagram of the datamodel within a PamControlledUnit.
	 */
	private Node createDataModelNode(ArrayList<ProcessTierData> controlsProcessModel){
		

		PamVBox vBoxHolder=new PamVBox();

		for (int i=0; i<controlsProcessModel.size(); i++){
			
//			printProcessList(controlsProcessModel.get(i).processChain, 
//					controlsProcessModel.get(i).dataBlockChain);
			
			PamHBox processChain=new PamHBox(); 
			processChain.setPadding(new Insets(5,10, 5,10));
			//processChain.setSpacing(30);
			processChain.setAlignment(Pos.CENTER);
			ProcessNode processNode;
			DataBlockNode dataBlockNode;
			ArrayList<DataBlockNode> lastdataBlockNodes=null;
			Pane lastdataBlockLinePane=null;

			for (int j=0; j<controlsProcessModel.get(i).processChain.size(); j++){
								
				if (controlsProcessModel.get(i).processChain.get(j).size()<1) continue;
				
				processNodes=new ArrayList<ProcessNode>();
				dataBlockNodes=new ArrayList<DataBlockNode>();

				//create VBox for single position of processes in chain
				PamVBox processVBox=new PamVBox(); 
			    HBox.setHgrow(processVBox, Priority.ALWAYS);
				processVBox.setAlignment(Pos.CENTER);
				processVBox.setSpacing(5);

				for (int k=0; k<controlsProcessModel.get(i).processChain.get(j).size(); k++){
					processNode=new ProcessNode(controlsProcessModel.get(i).processChain.get(j).get(k)); 
					processNode.setPadding(new Insets(0,20,0,20));
					processVBox.getChildren().add(processNode);
					processNodes.add(processNode);
				}
				
				//create a Pane for drawing lines (don't want any layout restrictions)
				Pane processLinePane=new Pane(); 
				processLinePane.prefWidthProperty().bind(processVBox.widthProperty());
				processLinePane.prefHeightProperty().bind(processVBox.heightProperty());
				
				//add all to a stackPane
				PamStackPane processStackPane=new PamStackPane(); 
				processStackPane.getChildren().addAll(processLinePane, processVBox);
				
				processChain.getChildren().add(processStackPane);
				//processVBox.setStyle("-fx-background-color: red");


				//data block node. 
				PamVBox dataBlockVBox=new PamVBox(); 
			    HBox.setHgrow(dataBlockVBox, Priority.ALWAYS);
				dataBlockVBox.setAlignment(Pos.CENTER);
				dataBlockVBox.setSpacing(5);
				
				for (int k=0; k<controlsProcessModel.get(i).dataBlockChain.get(j).size(); k++){
					dataBlockNode=new DataBlockNode(controlsProcessModel.get(i).dataBlockChain.get(j).get(k));
					dataBlockNode.setPadding(new Insets(0,20,0,20));
					dataBlockVBox.getChildren().add(dataBlockNode);
					dataBlockNodes.add(dataBlockNode);
				}
				
				//create a Pane for drawing lines (don't want any layout restrictions)
				Pane dataBlockLinePane=new Pane(); 
				dataBlockLinePane.prefWidthProperty().bind(dataBlockVBox.widthProperty());
				dataBlockLinePane.prefHeightProperty().bind(dataBlockVBox.heightProperty());
				//dataBlockVBox.setStyle("-fx-background-color: green");
				
				PamStackPane dataBlockStackPane=new PamStackPane(); 
				dataBlockStackPane.getChildren().addAll(dataBlockLinePane, dataBlockVBox);
				
				processChain.getChildren().add(dataBlockStackPane);
				
				//draw connection lines. 
				drawConnectionLines(lastdataBlockLinePane, processLinePane, dataBlockLinePane, lastdataBlockNodes, processNodes, dataBlockNodes);
				
				//draw outputs on data block
				drawDataBlockObsvOutput(dataBlockLinePane, dataBlockNodes, (j==controlsProcessModel.get(i).processChain.size()-1));

				lastdataBlockNodes=dataBlockNodes;
				lastdataBlockLinePane=dataBlockLinePane;
			}
			
			vBoxHolder.getChildren().add(processChain);
		}	
			
		return vBoxHolder;
		
	}
	
	private void drawDataBlockObsvOutput(Pane dataBlockLinePane,
			ArrayList<DataBlockNode> dataBlockNodes, boolean last) {
		
		PamDataBlock dataBlock;
		List<PamObserver> observers;
		for (int i=0; i<dataBlockNodes.size(); i++){
			dataBlock=dataBlockNodes.get(i).getDataBlock();
			boolean hasExternal=hasExternalObserver(dataBlock);
			if (hasExternal){
				
				Line line1=new Line();
				styleLine(line1);
				line1.startXProperty().bind(dataBlockNodes.get(i).getPamButton().layoutXProperty().add(dataBlockNodes.get(i).getPamButton().widthProperty().
						add(dataBlockNodes.get(i).getNUnitLabel().widthProperty())));
				line1.startYProperty().bind(dataBlockNodes.get(i).layoutYProperty().add(dataBlockNodes.get(i).heightProperty().divide(2)));
				line1.endXProperty().bind(line1.startXProperty().add(5));
				line1.endYProperty().bind(dataBlockNodes.get(i).layoutYProperty().add(dataBlockNodes.get(i).heightProperty().divide(2)));
				dataBlockLinePane.getChildren().add(line1);
				
				Line line2=new Line();
				styleLine(line2);
				line2.startXProperty().bind(line1.endXProperty());
				line2.startYProperty().bind(line1.endYProperty());
				//now need to figure out which side to the line one
				
				//if last then we have end data blocks- so connection should go to end. 
				if (last){
					line2.endXProperty().bind(dataBlockLinePane.widthProperty().add(chainPadding.getRight()));
					line2.endYProperty().bind(line2.startYProperty());
				}
				else {
					//need to find closest side, top or bottom?
					if (dataBlockLinePane.getHeight()-dataBlockNodes.get(i).getLayoutY()<dataBlockNodes.get(i).getLayoutY()){
						//bottom
						line2.endXProperty().bind(line1.endXProperty());
						line2.endYProperty().bind(dataBlockLinePane.heightProperty().add(chainPadding.getBottom()));
					}
					else {
						//top
						line2.endXProperty().bind(line1.endXProperty());
						line2.endYProperty().setValue(0-chainPadding.getTop());
					}
				}
				dataBlockLinePane.getChildren().add(line2);

				Circle outputCircle=new Circle();
				outputCircle.centerXProperty().bind(line2.endXProperty());
				outputCircle.centerYProperty().bind(line2.endYProperty());
				outputCircle.setRadius(5);
				outputCircle.setFill(lineColor);

				dataBlockLinePane.getChildren().add(outputCircle);

			}
		}
	}
	
	/**
	 * Style line coneecting processes to datablocks and vice versa. 
	 * @param line - the line to style. 
	 */
	private void styleLine(Line line){
		line.setStroke(lineColor);
		line.setStrokeWidth(lineWidth);
	}
	
	/**
	 * Check whether a data block has an observer which is NOT a process within the PamControlledUnit it belongs
	 * to.
	 * @param dataBlock - the data block to test.
	 * @return - true if the data block has at least one observer which is not within it's own PamControlledUnit. 
	 */
	private boolean hasExternalObserver(PamDataBlock dataBlock){
		List<PamObserver> observers;
		boolean hasExternal=false;
//		observers=dataBlock.getPamObservers();
		int nObservers = dataBlock.countObservers();
		for (int j=0; j<nObservers; j++){
			boolean externalObserver=true;
			//is the observer within the pamControlledUnit
			for (int n=0; n<pamControlledUnit.getNumPamProcesses(); n++){
				//NOTE:  must use the observer object for comparisons. 
				if (dataBlock.getPamObserver(j).getObserverObject() == pamControlledUnit.getPamProcess(n)){
					externalObserver=false; 
					break; 
				}
			}
//			System.out.println(" The data block: "+dataBlock.getDataName()+" has observer "+
//					observers.get(j).getObserverName() + " external " +externalObserver);
			if (externalObserver) {
				hasExternal=true; 
				break;
			}
		}
		return hasExternal; 
	}

	/**
	 * Draw connection lines between different data blocks and processes.
	 * @param dataBlockLinePane1
	 * @param processLinePane
	 * @param dataBlockLinePane2
	 * @param dataBlockNodes1
	 * @param processNodes
	 * @param dataBlockNodes2
	 */
	private void drawConnectionLines(Pane dataBlockLinePane1, Pane processLinePane, Pane dataBlockLinePane2,
			ArrayList<DataBlockNode> dataBlockNodes1, ArrayList<ProcessNode> processNodes, ArrayList<DataBlockNode> dataBlockNodes2){
		
		Line line; 
		/**parent data blocks(s) to child process(s)**/
		//not all datablocks are parents of the proccess. 
		
		if (dataBlockLinePane1!=null){
			for (int i=0; i<dataBlockNodes1.size(); i++){
				for (int j=0; j<processNodes.size(); j++){
					if (processNodes.get(j).getProcess().getParentDataBlock()==dataBlockNodes1.get(i).getDataBlock()){
						//now join data block to process. 
						line=new Line();
						styleLine(line);
						line.startXProperty().bind(dataBlockNodes1.get(i).getPamButton().layoutXProperty().add(dataBlockNodes1.get(i).getPamButton().widthProperty().
								add(dataBlockNodes1.get(i).getNUnitLabel().widthProperty())));
						line.startYProperty().bind(dataBlockNodes1.get(i).layoutYProperty().add(dataBlockNodes1.get(i).heightProperty().divide(2)));
						line.endXProperty().bind(dataBlockLinePane1.widthProperty());
						line.endYProperty().bind(dataBlockNodes1.get(i).layoutYProperty().add(dataBlockNodes1.get(i).heightProperty().divide(2)));
						dataBlockLinePane1.getChildren().add(line);
						
						line=new Line();
						styleLine(line);
						line.startXProperty().set(0);
						line.startYProperty().bind(dataBlockNodes1.get(i).layoutYProperty().add(dataBlockNodes1.get(i).heightProperty().divide(2)));
						line.endXProperty().set(0);
						line.endYProperty().bind(processNodes.get(j).layoutYProperty().add(processNodes.get(j).heightProperty().divide(2)));
						processLinePane.getChildren().add(line);
						
						line=new Line();
						styleLine(line);
						line.startXProperty().set(0);
						line.startYProperty().bind(processNodes.get(j).layoutYProperty().add(processNodes.get(j).heightProperty().divide(2)));
						line.endXProperty().bind(processNodes.get(j).layoutXProperty().add(processNodes.get(j).getPadding().getLeft()));
						line.endYProperty().bind(processNodes.get(j).layoutYProperty().add(processNodes.get(j).heightProperty().divide(2)));
						processLinePane.getChildren().add(line);
					}
				}
			}
		}
		
		/**parent process(s) to data block(s)***/
		//all datablocks must be children of one of the processes. 
		
		//draw line from process to end of process pane.
		for (int i=0; i<processNodes.size(); i++){
			line=new Line();
			line.setStroke(lineColor);
			line.setStrokeWidth(lineWidth);
			line.startXProperty().bind(processNodes.get(i).getPamButton().layoutXProperty().add(processNodes.get(i).getPamButton().widthProperty())
					.add(processNodes.get(i).getProcessLabel().widthProperty()));
			line.startYProperty().bind(processNodes.get(i).layoutYProperty().add(processNodes.get(i).heightProperty().divide(2)));
			line.endXProperty().bind(processLinePane.widthProperty());
			line.endYProperty().bind(processNodes.get(i).layoutYProperty().add(processNodes.get(i).heightProperty().divide(2)));
			processLinePane.getChildren().add(line);
		}
		
		for (int i=0; i<dataBlockNodes2.size(); i++){
			//find process node for the data block
			for (int j=0; j<processNodes.size(); j++){
				if (dataBlockNodes2.get(i).getDataBlock().getParentProcess()==processNodes.get(j).getProcess()){
					line=new Line();
					styleLine(line);
					line.startXProperty().set(0);
					line.startYProperty().bind(processNodes.get(j).layoutYProperty().add(processNodes.get(j).heightProperty().divide(2)));
					line.endXProperty().set(0);
					line.endYProperty().bind(dataBlockNodes2.get(i).layoutYProperty().add(dataBlockNodes2.get(i).heightProperty().divide(2)));
					dataBlockLinePane2.getChildren().add(line);
					

					line=new Line();
					styleLine(line);
					line.startXProperty().set(0);
					line.startYProperty().bind(dataBlockNodes2.get(i).layoutYProperty().add(dataBlockNodes2.get(i).heightProperty().divide(2)));
					line.endXProperty().bind(dataBlockNodes2.get(i).layoutXProperty().add(dataBlockNodes2.get(i).getPadding().getLeft()));
					line.endYProperty().bind(dataBlockNodes2.get(i).layoutYProperty().add(dataBlockNodes2.get(i).heightProperty().divide(2)));
					dataBlockLinePane2.getChildren().add(line);
				}
			}
			
		}
		
		
		
	}

	/**
	 * Calculate the maximum number of either process or data blocks in a single tier. 
	 * @param controlModelData - tier data
	 * @return the maximum number of elements in a single tier
	 */
	private int maxTierElements(ProcessTierData controlModelData){
		int maxVal=Integer.MAX_VALUE;
		for (int i=0; i>controlModelData.processChain.size(); i++){
			if (controlModelData.processChain.get(i).size()>maxVal) maxVal=controlModelData.processChain.get(i).size();
		}
		for (int i=0; i>controlModelData.dataBlockChain.size(); i++){
			if (controlModelData.dataBlockChain.get(i).size()>maxVal) maxVal=controlModelData.dataBlockChain.get(i).size();
		}
		return maxVal;
	}

	/**
	 * Figure out the process chains. This is a list of process chains i.e. parent process->child process 1->child process 2 etc.  
	 * @return a list representing process chains
	 */
	public ArrayList<ProcessTierData> calcProcessModel(){
		
		ArrayList<ProcessTierData> processTierDataList=new ArrayList<ProcessTierData>();
		
		/**
		 * First need to order the processes into tiers i.e. tier 1 are processes which external data blocks, tier 2 are
		 * processes which accept data blocks from tier 1 processes etc.
		 **/ 
		ArrayList<PamProcess> parentProcesses=new ArrayList<PamProcess>();  
		for (int i=0; i<pamControlledUnit.getNumPamProcesses();i++){
			if (pamControlledUnit.getPamProcess(i).getParentProcess()==null || 
					pamControlledUnit.getPamProcess(i).getParentProcess().getPamControlledUnit()!=pamControlledUnit ){
				parentProcesses.add(pamControlledUnit.getPamProcess(i)); 
			}
		}
		
		/**
		 * Now we have the number of process chains. In order to layout properly need to get an idea of the number of datablocks 
		 * and processes in each tier of the chain. Need to make some arrays to get those numbers
		 */
		PamProcess parentProcess;
		ArrayList<PamProcess> currentProcessLink; 
		ArrayList<PamDataBlock> currentDataBlockLink; 
		ArrayList<ArrayList<PamProcess>> processChain;
		ArrayList<ArrayList<PamDataBlock>> dataBlockChain;
		for (int i=0; i<parentProcesses.size(); i++){
			
			parentProcess=parentProcesses.get(i); 
			processChain=new ArrayList<ArrayList<PamProcess>>(); 
			dataBlockChain=new ArrayList<ArrayList<PamDataBlock>>(); 
			
			//start first link
			currentProcessLink=new ArrayList<PamProcess>(); 
			currentProcessLink.add(parentProcess); 
			processChain.add(currentProcessLink); 

			//start first data block link
			currentDataBlockLink=new ArrayList<PamDataBlock>();
			currentDataBlockLink.addAll(parentProcess.getOutputDataBlocks()); 
			dataBlockChain.add(currentDataBlockLink); 

			boolean chainOk=true; 
			while (chainOk){
				ArrayList<PamProcess> newProcessList=new ArrayList<PamProcess>();
				for (int j=0; j<currentDataBlockLink.size(); j++){
					for (int k=0; k<pamControlledUnit.getNumPamProcesses();k++){
						if (pamControlledUnit.getPamProcess(k).getParentDataBlock()==currentDataBlockLink.get(j)){
							newProcessList.add(pamControlledUnit.getPamProcess(k)); 
						}
					}
				}
				ArrayList<PamDataBlock> newDataBlockList =new ArrayList<PamDataBlock>();
				for (int j=0; j<newProcessList.size(); j++){
					newDataBlockList.addAll(newProcessList.get(j).getOutputDataBlocks()); 
				}
				
				//no processes left then exit the loop; 
				if (newProcessList.size()<1) {
					chainOk=false; 
				}

				currentProcessLink=newProcessList;
				currentDataBlockLink=newDataBlockList; 
				//add to main arrays
				if (newProcessList.size()>0){
					processChain.add(newProcessList); 
					dataBlockChain.add(newDataBlockList);
				}
			}
			
			ProcessTierData processDataTier=new ProcessTierData();
			processDataTier.processChain=processChain;
			processDataTier.dataBlockChain=dataBlockChain;
			processTierDataList.add(processDataTier);
			//printProcessList(processChain,dataBlockChain);
		}
		
		return processTierDataList;
				
	} 
	
	/**
	 * Holds data on the structure of the control data model.
	 * @author Jamie Macaulay
	 *
	 */
	class ProcessTierData{

		public ArrayList<ArrayList<PamDataBlock>> dataBlockChain;
		
		
		public ArrayList<ArrayList<PamProcess>> processChain;
		
	}
	
	
	/** 
	 * Print out a process chain. 
	 * @param processChain - process chain
	 * @param dataBlockChain - corresponding datablock chain. 
	 */
	private void printProcessList(ArrayList<ArrayList<PamProcess>> processChain, 
			ArrayList<ArrayList<PamDataBlock>> dataBlockChain){
		for (int i=0; i<processChain.size(); i++){
			if (i==0) System.out.println("Parent Process is: "+processChain.get(i).get(0).getProcessName()); 
			else {
				for (int j=0; j<processChain.get(i).size(); j++){
					System.out.print(processChain.get(i).get(j).getProcessName()+"     ");
				}
				System.out.println("");
			}
			System.out.println("/****Tier "+i+"****/"); 

		}
	}
	
	/**
	 * Shows a process name and the amount of
	 * @author Jamie Macaulay 
	 *
	 */
	private class ProcessNode extends PamHBox{
		
		
		private PamProcess pamProcess;
		
		/**
		 * Button to show process name
		 */
		private PamButton pamButton;
		
		private TextField processPwrLabel;


		public ProcessNode(PamProcess pamProcess){
			this.pamProcess=pamProcess; 
			createProcessNode();
			//this.setStyle("-fx-background-color: yellow");
		}
		
		public PamProcess getProcess() {
			return pamProcess;
		}
		
		public PamButton getPamButton() {
			return pamButton;
		}
		
		/**
		 * Get the processing power label. 
		 * @return the processinfg power label
		 */
		public TextField getProcessLabel() {
			return processPwrLabel;
		}

		private void createProcessNode(){
			pamButton=new PamButton(pamProcess.getProcessName()); 
			pamButton.setStyle("-fx-border-radius: 5 0 0 5;");
			pamButton.setOnAction((action)->{
				System.out.println(" ProcessButton layout:  X: "+pamButton.getLayoutX()+"  Y: " +pamButton.getLayoutY()); 
				System.out.println(" ProcessNode layout:  X: "+this.getLayoutX()+"  Y: " +this.getLayoutY()); 

			});
			//pamButton.getStyleClass().add("square-button");
			this.getChildren().add(pamButton);
			
			processPwrLabel=new TextField("0%");
			processPwrLabel.setEditable(false);
			processPwrLabel.setPrefColumnCount(3);
			processPwrLabel.setStyle("-fx-border-radius: 0 5 5 0;");
			pamButton.maxHeightProperty().bind(processPwrLabel.heightProperty());
			this.getChildren().add(processPwrLabel);
		}
		
		protected void updateCpuUsage() {
			if (processPwrLabel != null) {
//				System.out.println("Process "+ pamProcess.getProcessName() + " CPU percent: "+ pamProcess
//						.getCpuPercent());
				processPwrLabel.setText(String.format(("%3.1f %%"), pamProcess
						.getCpuPercent()));
			}
		}
	}
	
	/**
	 * Shows a process name and the amount of
	 * @author Jamie Macaulay 
	 */
	private class DataBlockNode extends PamHBox{
		
		private PamDataBlock pamDataBlock;
		
		/**
		 * SHows the name of the data block and allows users to click to bring up stats. 
		 */
		private PamButton pamButton;
		
		/**
		 * Shows number of data units.
		 * 
		 */
		private TextField nUnitLabel;

		public DataBlockNode(PamDataBlock pamDataBlock){
			this.pamDataBlock=pamDataBlock; 
			createDataBlockNode();
		}
		
		public PamButton getPamButton() {
			return pamButton;
		}

		private void createDataBlockNode(){
			pamButton=new PamButton(pamDataBlock.getDataName()); 
			pamButton.setStyle("-fx-border-radius: 5 0 0 5;");
			pamButton.setOnAction((action)->{
				System.out.println(" DATABlockButton layout:  X: "+pamButton.getLayoutX()+"  Y: "
			+pamButton.getLayoutY()+" width: "+pamButton.getWidth()); 
			});
			//pamButton.getStyleClass().add("square-button");
			this.getChildren().add(pamButton);
			nUnitLabel=new TextField("0");
			nUnitLabel.setEditable(false);
			nUnitLabel.setPrefColumnCount(4);
			nUnitLabel.setStyle("-fx-border-radius: 0 5 5 0;");
			pamButton.maxHeightProperty().bind(nUnitLabel.heightProperty());
			this.getChildren().add(nUnitLabel);

		}
		
		/**
		 * Label which displays number of units in the data block
		 * @return text field showing number of units. 
		 */
		public TextField getNUnitLabel() {
			return nUnitLabel;
		}
		
		/**
		 * Get the data block associated with the data block node. 
		 * @return the data block associated with the node. 
		 */
		public PamDataBlock getDataBlock(){
			return pamDataBlock;
		}
		
		/**
		 * UYpdate data block text field 
		 */
		protected void updateBlockCount() {
			nUnitLabel.setText(String.format("%d", pamDataBlock
					.getUnitsCount()));
		}
	}
	
	//old mathod- very simple but not that informative. 

//	/**
//	 * Populate process list. 
//	 */
//	private void populateProcessList(){
//		if (pamControlledUnit==null) return; 
//		PamButton pamButton; 
//		for (int i=0; i<pamControlledUnit.getNumPamProcesses();i++){
//			pamButton=new PamButton(pamControlledUnit.getPamProcess(i).getProcessName());
//			pamButton.getStyleClass().add("square-button");
//			pamButton.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
//			lowerHidingBox.getChildren().add(pamButton); 
//			//add any data blocks associated with this process
//			for (int j=0; j<pamControlledUnit.getPamProcess(i).getNumOutputDataBlocks(); j++){
//				pamButton=new PamButton(pamControlledUnit.getPamProcess(i).getOutputDataBlock(j).getDataName());
//				pamButton.getStyleClass().add("square-button");
//				pamButton.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
//				lowerHidingBox.getChildren().add(pamButton); 
//			}
//			pamButton.getStyleClass().add("square-button");
//			pamButton.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
//		}
//	}

}
