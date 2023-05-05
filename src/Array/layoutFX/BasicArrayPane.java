package Array.layoutFX;

import clickDetector.layoutFX.clickClassifiers.ClickTypeProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.table.TableSettingsPane;

/**
 *  A pane for setting up hydrophones. Note that this is entirely separate from PAMGuard so can be used in
 *  other projects. 
 *  
 * @author Jamie Macaulay
 *
 */
public class BasicArrayPane extends PamBorderPane {
	
	/**
	 * Class which extends TableSettingsPane and creates a sliding pane instead of a dialog when an item is added. 
	 * @author Jamie Macaulay 
	 *
	 */
	class ClickClassifierTable extends TableSettingsPane<ClickTypeProperty> {

		public ClickClassifierTable(ObservableList<ClickTypeProperty> data) {
			super(data);
			//need to set up all the rows.

			TableColumn<ClickTypeProperty,String>  icon = new TableColumn<ClickTypeProperty,String>("Name");
			icon.setCellValueFactory(cellData -> cellData.getValue().name);
			icon.setEditable(false);

			icon.setCellFactory((tableColumn) -> {
				TableCell<ClickTypeProperty, String> tableCell = new TableCell<ClickTypeProperty, String>(); 
				return tableCell;
			});

			//			TableColumn<ClickTypeProperty,String>  name = new TableColumn<ClickTypeProperty,String>("Name");
			//			name.setCellValueFactory(cellData -> cellData.getValue().name);
			//			name.setEditable(true);


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

			getTableView().getColumns().addAll(checkCol, icon, code, discard);

		}

		@Override
		public void dialogClosed(ClickTypeProperty data) {
			// TODO Auto-generated method stub	
		}

		@Override
		public Dialog<ClickTypeProperty> createSettingsDialog(ClickTypeProperty data) {
			//we do not use dialogs here- sliding pane instead. 
//			setClassifierPane(data);
//			showFlipPane(true);		
			return null;
		}

		@Override
		public void editData(ClickTypeProperty data){
//			setClassifierPane(data);
//			showFlipPane(true);		
		}

		@Override
		public void createNewData(){
			//create a new classifier. 
//			clickClassifiers.add(createClickTypeProperty()); 
		}

	}


}
