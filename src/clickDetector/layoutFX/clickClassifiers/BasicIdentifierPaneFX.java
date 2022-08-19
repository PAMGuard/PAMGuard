package clickDetector.layoutFX.clickClassifiers;

import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.flipPane.PamFlipPane;
import pamViewFX.fxNodes.table.TableSettingsPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import clickDetector.BasicClickIdParameters;
import clickDetector.ClickControl;
import clickDetector.ClickTypeParams;
import clickDetector.ClickClassifiers.ClickIdentifier;
import clickDetector.ClickClassifiers.basic.BasicClickIdentifier;

/**
 * Pane for the basic click classifier. 
 * @author Jamie Macaulay	
 *
 */
public class BasicIdentifierPaneFX implements ClassifyPaneFX {
	
	/**
	 * Reference to the basicClickIdentifier. 
	 */
	private ClickIdentifier basicClickIdentifier;
	
	/**
	 * Reference to the click control. 
	 */
	protected ClickControl clickControl;
	
	/**
	 * Pane which holds table data.
	 */
	private TableSettingsPane<ClickTypeProperty> settingsPane;
	
//	/**
//	 * Hiding pane which slides out to allow users to change click type settings. 
//	 */
//	protected HidingPane hidingPane; 
	
	/**
	 * Holds click classifier controls inside hiding pane. 
	 */
	protected PamBorderPane clickTypeHolder;

	
	/**
	 * A list of click classifiers currently shown in the table.It Would have been much easier to have this in params
	 * but didn't want to add any FX related as these should be GUI independent classes. 
	 */
	private ObservableList<ClickTypeProperty> clickClassifiers=FXCollections.observableArrayList();

	/**
	 * Holds the table. 
	 */
	protected PamBorderPane mainHolderPane;
	
//	/**
//	 * The width of the hiding pane
//	 */
//	private double hidingPaneWidth=900;

	/**
	 * Cloned copy of BasicClickIdParameters. 
	 */
	private BasicClickIdParameters basicClickIdParameters;
	
	private PamBorderPane mainPane; 
	
	private PamFlipPane flipPane;

	/**
	 * Create a BasicClickIdParameters pane which allows users to add multiple basic click identifiers to the PAMGuard click classifier. 
	 * @param basicClickIdentifier - the ClickIdentifier. ob  
	 * @param clickControl -a reference to the ClickControl the classifier is associated with. 
	 */
	public BasicIdentifierPaneFX(ClickIdentifier basicClickIdentifier, 
			ClickControl clickControl){
		this.basicClickIdentifier= basicClickIdentifier;
		this.clickControl=clickControl; 
		mainPane= new PamBorderPane(); 
		
		flipPane = new PamFlipPane(); 
		flipPane.getFrontPane().setCenter(createSettingsPane()); 
		flipPane.getBackPane().setCenter(clickTypeHolder); 

		mainPane.setCenter(flipPane);

	}
	
	
	
	/**
	 * Create the controls for the basic click identifier pane. 
	 * @return node with all controls for basic click classifier. 
	 */
	protected Node createSettingsPane(){
		
		mainHolderPane=new PamBorderPane();
		mainHolderPane.setCenter(settingsPane=new ClickClassifierTable(clickClassifiers));
		
		clickTypeHolder=new PamBorderPane();
		//clickTypeHolder.setPrefWidth(hidingPaneWidth);
				
		return mainHolderPane;
		
	}
	
//	/**
//	 * Added purely so can be override and hiding pane set in different location if required
//	 */
//	public void createHidingPane(){
//		hidingPane=new HidingPane(Side.RIGHT, clickTypeHolder, mainPane, false);
//		//hidingPane.showHidePane(false);		
//		mainHolderPane.setRight(hidingPane);
//		hidingPane.showHidePane(false);
//	}
	


	@Override
	public Node getNode() {
		return mainPane;
	}

	@Override
	public void setParams() {
		basicClickIdParameters = ((BasicClickIdentifier) basicClickIdentifier).getIdParameters().clone();
		//change the observable list. 
		
	}

	@Override
	public boolean getParams() {
		((BasicClickIdentifier) basicClickIdentifier).setIdParameters(basicClickIdParameters);
		return true;
	}

	@Override
	public String getHelpPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActive(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Class which extends TableSettingsPane and creates a sliding pane instead of a dialog when an item is added. 
	 * @author Jamie Macaulay 
	 *
	 */
	class ClickClassifierTable extends TableSettingsPane<ClickTypeProperty> {

		public ClickClassifierTable(ObservableList<ClickTypeProperty> data) {
			super(data);
			//need to set up all the rows.
			
			TableColumn<ClickTypeProperty,String>  name = new TableColumn<ClickTypeProperty,String>("Name");
			name.setCellValueFactory(cellData -> cellData.getValue().name);
			name.setEditable(true);
			
			TableColumn<ClickTypeProperty,Number>  code = new TableColumn<ClickTypeProperty,Number>("Species Code");
			code.setCellValueFactory(cellData -> cellData.getValue().code);

			
			TableColumn<ClickTypeProperty,Boolean>  checkCol = new TableColumn<>("Enable");
			checkCol.setCellValueFactory( cellData -> cellData.getValue().enableClassifier);
			checkCol.setCellFactory(CheckBoxTableCell.forTableColumn(checkCol));
			checkCol.setEditable(true);
			checkCol.setMaxWidth( 100 );
			checkCol.setMinWidth( 100 );
			
			
			TableColumn<ClickTypeProperty,Boolean>  discard = new TableColumn<>("Discard");
			discard.setCellValueFactory( cellData -> cellData.getValue().discardClassifier);
			discard.setCellFactory(CheckBoxTableCell.forTableColumn(checkCol));
			discard.setEditable(true);
			discard.setMaxWidth( 100 );
			discard.setMinWidth( 100 );
			
			getTableView().setEditable(true);
			
			getTableView().getColumns().addAll(checkCol, name, code, discard);

		}

		@Override
		public void dialogClosed(ClickTypeProperty data) {
			// TODO Auto-generated method stub	
		}

		@Override
		public Dialog<ClickTypeProperty> createSettingsDialog(ClickTypeProperty data) {
			//we do not use dialogs here- sliding pane instead. 
			setClassifierPane(data);
			showFlipPane(true);		
			return null;
		}
		
		@Override
		public void editData(ClickTypeProperty data){
			setClassifierPane(data);
			showFlipPane(true);		
		}
		
		@Override
		public void createNewData(){
			//create a new classifier. 
			clickClassifiers.add(createClickTypeProperty()); 
		}
		
	}
	
//	/**
//	 * Show the hiding pane which contains classifier settings
//	 * NOTE: needed to add this to stop a stack overflow error in BasicClickIdentifier 06/09/2016
//	 * @param show - true to show pane. 
//	 */
//	public void showHidingPane(boolean show){
//		if (hidingPane==null){
//			this.createHidingPane();
//		}
//		hidingPane.showHidePane(true);		
//	}
	
	/**
	 * Show the flip pane. 
	 * NOTE: needed to add this to stop a stack overflow error in BasicClickIdentifier 06/09/2016
	 * @param show - true to show pane. 
	 */
	public void showFlipPane(boolean show){
		if (show) {
			//System.out.println("Show flip pane: " + show);
			flipPane.flipToBack();
		}
		else {
			flipPane.flipToFront();
		}
	}
	
	/**
	 * Create click classifier. 
	 */
	public ClickTypeProperty createClickTypeProperty(){
		return new ClickTypeProperty(new ClickTypeParams(clickClassifiers.size()));
	}
	
	/**
	 * Set classifier pane within hiding pane.
	 * @param clickTypeProperty
	 */
	public void setClassifierPane(ClickTypeProperty clickTypeProperty){
		
		
		ClickTypePaneFX clickTypePane=new ClickTypePaneFX();
		clickTypePane.setParams(clickTypeProperty);
		
		clickTypeHolder.setCenter(clickTypePane.getContentNode());
		
		//now need to make sure on closing the pane that settings are saved. Need to 
		//remove the old click type from the list and add new one in the same position. 
		getFlipPaneCloseButton().setOnAction((action)->{
			//System.out.println("CLOSE FLIP PANE");
			showFlipPane(false);
			//this should update the click type property in the observable list thus changing the table
			clickTypePane.getParams(clickTypeProperty);
		});
	}

	/**
	 * Get the button which closes the hiding pane. 
	 * @return button which closes the hiding pane. 
	 */
	public Button getFlipPaneCloseButton() {
		return flipPane.getBackButton();
	}

//	/**
//	 * Get the hiding pane which holds settings for different click types. 
//	 * @return the HidingPane for click classifiers. 
//	 */
//	public HidingPane getClickTypeHidingPane() {
//		if (hidingPane==null) {
//			this.createHidingPane();
//		}
//		return hidingPane;
//	}

	/**
	 * Get the pane which holds the ClickTypePaneFX.
	 * @return a BorderPane which holds selected ClickTypePaneFX.
	 */
	public PamBorderPane getClickTypeHolder() {
		return clickTypeHolder;
	};
	
	/**
	 * Get the table which holds a list of classifier
	 * @return table which holds a list of classifiers
	 */
	public TableSettingsPane<ClickTypeProperty>  getTablePane() {
		return this.settingsPane;
	};
	
	/**
	 * Get list of click classifiers
	 * @return list of click classifiers
	 */
	public ObservableList<ClickTypeProperty> getClickClassifiers() {
		return clickClassifiers;
	}

}
