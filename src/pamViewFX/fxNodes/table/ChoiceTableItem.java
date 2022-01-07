package pamViewFX.fxNodes.table;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

/**
 * Used in conjuction with the Choice table. A table item which can be selected 
 * using a check box cell. This represents one row of Choice table data. 
 * 
 * @author Jamie Macaulay 
 */
public abstract class ChoiceTableItem {

	/**
	 * Property on whether the check box int he table row is selected.
	 */
	public BooleanProperty selectedProperty = new SimpleBooleanProperty(); 
	
	
	/**
	 * Check the params for the table row. Can be overriden if using ErrTable cells
	 * @return
	 */
	public boolean checkParams() {
		return true; 
	}


	/**
	 * Check the params. Can be overriden if using ErrTable cells
	 * @return true if the param is OK 
	 */
	public boolean checkItem(ObservableValue<?> value) {
		return false;
	}

		
}