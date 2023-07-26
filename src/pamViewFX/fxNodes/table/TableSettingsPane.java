package pamViewFX.fxNodes.table;

import pamViewFX.fxNodes.PamBorderPane;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.geometry.Insets;


/**
 * Pane which holds data within a table  and has add, settings and delete buttons. 
 * @author Jamie Macaulay
 *
 * @param <T> - type of data unit
 */
public abstract class TableSettingsPane<T> extends PamBorderPane {

	/**
	 * The table to hold data of type T. 
	 */
    private TableView<T> table;
    
    /**
   	 * Holds all data units.
   	 */
   	ObservableList<T> data;

	private TableButtonPane buttonPane;
   
	public TableSettingsPane(ObservableList<T> data){
   		this.data=data; 
		table = new TableView<T>();
        this.setCenter(createPane());
	}
	
	/**
	 * Create the pane with table and add, settings and delete buttons. 
	 * @return pane with table and control buttons. 
	 */
	protected Pane createPane(){

		BorderPane arrayPane=new BorderPane();

		//enable double clicking on table row. 
		table.setRowFactory( tv -> {
		    TableRow<T> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
		            T rowData = row.getItem();
		            editData(rowData);
		        }
		    });
		    return row ;
		});
		
		//add listener to disable settings and delete buttons when there's nothing in the table. 
		data.addListener((ListChangeListener.Change<? extends T> c) ->{
			getButtonPane().getSettingsButton().setDisable(table.getItems().size()<=0);
			getButtonPane().getDeleteButton().setDisable(table.getItems().size()<=0);
		});
	
		
		//set table data 
		table.setItems(data);
		
		

	    //create pane holding add, edit and remove controls
        buttonPane=new TableButtonPane(Orientation.VERTICAL); 
        buttonPane.getAddButton().setOnAction((event)->{
        	createNewData();
        });
        
        buttonPane.getSettingsButton().setOnAction((event)->{
        	editData(table.getSelectionModel().getSelectedItem());
        });
        
        buttonPane.getDeleteButton().setOnAction((event)->{
        	deleteData(table.getSelectionModel().getSelectedItem());
        });
        
        arrayPane.setCenter(table);
        arrayPane.setRight(buttonPane);
        
    	
        //make sure table resized with pane to stop blank column
        getTableView().setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                
    	getButtonPane().getSettingsButton().setDisable(table.getItems().size()<=0);
		getButtonPane().getDeleteButton().setDisable(table.getItems().size()<=0);

		return arrayPane;
	}
	
	public void createNewData(){
		Dialog<T> arrayDialog=createSettingsDialog(null); 
		arrayDialog.showAndWait();
		//add new result to table 
		if (arrayDialog.getResult()!=null){
			data.add(arrayDialog.getResult()); 
			dialogClosed(arrayDialog.getResult());
		}
	
	}
	
	public void editData(T data){
		Dialog<T> arrayDialog=createSettingsDialog(data); 
		arrayDialog.showAndWait();
		dialogClosed( data);
	}
	
	public void deleteData(T data){
		table.getItems().remove(data);
	}
	
	/**
	 * Called whenever dialog is closed and new data has been either created or edited. 
	 * @param data
	 */
	public abstract void dialogClosed(T data);
		
	

	/**
	 * Create a dialog to change data settings for each row in table. 
	 * @param data - data to edit. null to  create a new instance of data
	 * @return dialog to change data settigns. 
	 */
	public abstract Dialog<T> createSettingsDialog(T data);

	/**
	 * Get the table
	 * @return the table. 
	 */
	public TableView<T> getTableView() {
		return table;
	}	
	
	/**
	 * Get the pane which holds, add, edit and delete buttons for the table. 
	 * @return pane containing control buttons for the pane. 
	 */
	public TableButtonPane getButtonPane() {
		return buttonPane;
	}

		

}