package pamViewFX.fxNodes.table;

import java.text.NumberFormat;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.NumberStringConverter;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Simple table with a check box column that only allows one check box column for 
 * single or multiple selection of table rows. 
 * @author Jamie Macaulay 
 *
 */
public abstract class ChoiceTable<T extends ChoiceTableItem> extends PamBorderPane  {

	/**
	 * Observable list of table items. 
	 */
	private ObservableList<T> tableData;
	
	/**
	 * The table which holds the data 
	 */
	private TableView<T> tableView;

	/**
	 * The button pane with add and remive buttond
	 */
	private PamVBox buttonPane;
	
	/**
	 * Default error colour
	 */
	private Color backGroundError= Color.rgb(200, 0, 0, 0.1);

	
	/**
	 * Only allow a single table item to be selected at any one time. 
	 */
	private boolean singleChoice = true;

	/**
	 * The add button	 
	 **/
	private PamButton addButton;

	/**
	 * The remove button
	 */
	private PamButton removeButton; 

	/**
	 * Create the choice table
	 * @param list - observable data list. 
	 */
	public ChoiceTable(ObservableList<T> list) {
		this.tableData=list;
		this.setTop(createChoiceTablePane());
		tableView.setItems(tableData);
		//this.setStyle("-fx-background-color: blue;");
	}
	


	/**
	 * Create the table. 	
	 */
	private TableView<T> createTable() {

		TableView<T> table = new TableView<T>(); 


		//table.getSelectionModel().cellSelectionEnabledProperty().set(true);
		table.setEditable(true);
		//table.setTableMenuButtonVisible(true);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn< T,Boolean>  loadedColumn  = new TableColumn<T,Boolean>("Current");
		loadedColumn.setCellValueFactory(
				new Callback<CellDataFeatures<T,Boolean>, ObservableValue<Boolean>>(){
					@Override public
					ObservableValue<Boolean> call( CellDataFeatures<T,Boolean> p ){
						if (singleChoice) checkCheckBoxes(p.getValue());
						return p.getValue().selectedProperty;}});
		loadedColumn.setEditable(true);

		loadedColumn.setCellFactory(
				new Callback<TableColumn<T,Boolean>,TableCell<T,Boolean>>(){
					@Override public
					TableCell<T,Boolean> call( TableColumn<T,Boolean> p ){
						CheckBoxTableCell checkBoxTableCell = new CheckBoxTableCell<>();
						//							checkBoxTableCell.selectedStateCallbackProperty().addListener((obsVal, oldVal, newVal)->{
						//								System.out.println("Check box has been checked");
						//							});
						return checkBoxTableCell; 
					}});
		
		table.getColumns().add(loadedColumn);


		return table;

	}


	/**
	 * Check all column values and ensure only one check box at a time is selected
	 * @param rowValue - the last row to trigger a selected listener. 
	 */
	private void checkCheckBoxes(T location) {
		//System.out.println("Checked table box"); 

		int count=0;
		for (int i=0; i<tableView.getItems().size(); i++) {
			if (tableView.getItems().get(i).selectedProperty.get()) count++;
		}

		//must always have one item in table selected. 
		if (count==0) {
			location.selectedProperty.set(true);
			return; 
		}

		if (count>1) {
			for (int i=0; i<tableView.getItems().size(); i++) {
				if (tableView.getItems().get(i) == location) continue; 
				else tableView.getItems().get(i).selectedProperty.set(false);
			}
		}

	}
	
	/**
	 * Create a new data item.
	 * @return new data item. 
	 */
	public abstract T newDataItem(); 


	/**
	 * Create the choice table and controls.
	 * @return apne with table and controls. 
	 */
	private Pane createChoiceTablePane() {
		
		//create the table 
		this.tableView= createTable(); 

		//build a new 
		buttonPane = new PamVBox(); 
		buttonPane.setSpacing(5); 
		buttonPane.setPadding(new Insets(5,5,5,5));
		
		addButton = new PamButton(); 
//		addButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADD, PamGuiManagerFX.iconSize));
		addButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", Color.WHITE, PamGuiManagerFX.iconSize));
		addButton.setOnAction((action)->{
			this.tableData.add(newDataItem());
		});

		removeButton = new PamButton(); 
//		removeButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.DELETE,  PamGuiManagerFX.iconSize));
		removeButton.setGraphic(PamGlyphDude.createPamIcon("mdi2d-delete",  Color.WHITE, PamGuiManagerFX.iconSize));
		removeButton.setOnAction((action)->{
			this.tableData.remove(this.tableView.getSelectionModel().getSelectedItem());
		});
		removeButton.disableProperty().bind(Bindings.isEmpty(tableView.getSelectionModel().getSelectedItems()));

		buttonPane.getChildren().addAll(addButton, removeButton); 
		
		PamHBox holder = new PamHBox();
		
		holder.setSpacing(5);
		holder.getChildren().addAll(tableView, buttonPane);
		HBox.setHgrow(tableView, Priority.ALWAYS);
		
		return holder; 

	}
	
	/**
	 * Get the table data. 
	 * @return the tableData - the table data 
	 */
	public ObservableList<T> getTableData() {
		return tableData;
	}


	/**
	 * Get the tableView. 
	 * @return the tableView
	 */
	public TableView<T> getTableView() {
		return tableView;
	}
	
	/**
	 * Get the pane which holds the add and delete buttons. 
	 * @return pane which holds table control buttons. 
	 */
	public Pane getButtonPane() {
		return this.buttonPane; 
	}
	
	
	/**
	 * DSet
	 * 	 * @param singleChoice
	 */
	public void setSingleChoice(boolean singleChoice) {
		this.singleChoice=singleChoice; 
	}
	
	
	/**
	 * Get the row which is currently selected. If multiple choice
	 * selections are allowed then return the first selected row. 
	 * @return the row with the choice box selected.-1 if no row is selected. 
	 */
	public int getSelectedChoice() {
		for (int i=0; i< this.getTableView().getItems().size(); i++) {
			if (this.getTableView().getItems().get(i).selectedProperty.get()){ 
				//System.out.println("Selected Choice: " + i );
				return i; 
			}
		}
		//System.out.println("Selected Choice: -1" );
		return -1;
	}
	
	/**
	 * Set the selected single choice on the table. If mutliple 
	 * choices are allowed simply sets the choice to true. 
	 * @param currentHeightIndex - the index of the selected choice to set. 
	 */
	public void setSelectedChoice(int currentHeightIndex) {
		if (currentHeightIndex<getTableView().getItems().size()) {
			this.getTableView().getItems().get(currentHeightIndex).selectedProperty.set(true);
		}
	}

	

	/**
	 * Set the table colour based on whether the ChoiceTable item has an OK flag. 
	 * @param tableCell - the table cell to set colour for 
	 * @param backGroundError - the colour of the background ot set if there is an error. 
	 */
	private static void setTableCellColour(TableCell<?,?> tableCell, Color backGroundError) {
		
		ChoiceTableItem landMark = (ChoiceTableItem) tableCell.getTableView().getItems().get(tableCell.getIndex());

		if (landMark.checkItem(tableCell.getTableColumn().getCellObservableValue(tableCell.getIndex()))) {
			//Here I see if the row of this cell is selected or not
			if(tableCell.getTableView().getSelectionModel().getSelectedItems().contains(landMark)) {
				tableCell.setTextFill(Color.BLACK);
				tableCell.setStyle(""); //reset back to default colour
				//setStyle("-fx-background-color:"+PamUtilsFX.color2Hex(backGroundError.brighter()));
			}
			else {
				tableCell.setTextFill(Color.WHITE);
				tableCell.setStyle("");
			}
		
		}
		else {
			tableCell.setTextFill(Color.BLACK); //The text in red
			if(tableCell.getTableView().getSelectionModel().getSelectedItems().contains(landMark)) {
				tableCell.setStyle("-fx-background-color:"+PamUtilsFX.color2Hex(backGroundError.brighter()));
			}
			else {
				tableCell.setStyle("-fx-background-color:"+PamUtilsFX.color2Hex(backGroundError));
			}
		}
	}


	/**
	 * 
	 * Number cell which shows an error depending on whether checkItem(ObservableValue value) within 
	 * ChoiceTableItem returns true or false. 
	 * @author Jamie Macaulay
	 *
	 * @param <T> - the table item. Must extend ChoiceTableItem
	 */
	public class ErrNumberFieldCell<T extends ChoiceTableItem> extends AcceptOnExitTableCell<T, Number> {

		public ErrNumberFieldCell(){
			this.setConverter(new NumberStringConverter());
		}
		
		public ErrNumberFieldCell(NumberFormat converter){
			this.setConverter(new NumberStringConverter(converter));
		}

		@Override
		public void updateItem(Number item, boolean empty) {
			super.updateItem(item, empty); //This is mandatory
			//System.out.println("Update: " + item); 
			if (getTableView().getItems().size()>0 && getIndex()<getTableView().getItems().size() &&
					getIndex()>=0) {
				//set the cell colour.
				setTableCellColour(this, backGroundError);
			}
			
		}
		
		@Override 
		public void commitEdit(Number newValue) {
			super.commitEdit(newValue);
			//must refresh in order to repaint if this cell alters the error state of other cells. 
			if (!super.getTabButtonPressed()) getTableView().refresh();
		}

	}


	/**
	 * 
	 * Text cell which shows an error depending on whether checkItem(ObservableValue value) within ChoiceTableItem returns true or false. 
	 * @author Jamie Macaulay
	 *
	 * @param <T> - the table item. Must extend ChoiceTableItem
	 */
	public class ErrTextFieldCell<T extends ChoiceTableItem> extends AcceptOnExitTableCell<T, String> {

		public ErrTextFieldCell(){
			this.setConverter(new DefaultStringConverter());
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty); //This is mandatory
			
			if (getTableView().getItems().size()>0 && getIndex()<getTableView().getItems().size() &&
					getIndex()>=0) {
				//set the cell colour
				setTableCellColour(this, backGroundError);
				
			}
		}

		@Override 
		public void commitEdit(String newValue) {
			super.commitEdit(newValue);
			//must refresh in order to repaint if this cell alters the error state of other cells. 
			if (!super.getTabButtonPressed()) getTableView().refresh();
		}
			

	}
	

	/**
	 * @return the addButton
	 */
	public PamButton getAddButton() {
		return addButton;
	}



	/**
	 * @return the removeButton
	 */
	public PamButton getRemoveButton() {
		return removeButton;
	}




}
