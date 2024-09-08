package Array.layoutFX;

import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;



/**
 * Choice box which allows selection of interpolation options. 
 * 
 * @author Jamie Macaulay
 *
 */
public class InterpChoicePane extends InterpSettingsPane {

	/**
	 * Interp choice box. 
	 */
	private ChoiceBox<Integer> interpChoiceBox; 
	
	public InterpChoicePane() {

		interpChoiceBox = new ChoiceBox<Integer>(); 
		interpChoiceBox.getItems().addAll(interpChoice); 
		interpChoiceBox.setMaxWidth(Double.MAX_VALUE);
		
		interpChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer item) {
            	if (item ==null) return "null";
                return getInterpString(item); 
            }

            @Override
            public Integer fromString(String unused) {
                throw new UnsupportedOperationException();
            }
        });
		
		this.setCenter(interpChoiceBox);

	}

	public void setSelection(int option) {
		
		System.out.println("Select interp option: "  + option);

		interpChoiceBox.getSelectionModel().select(Integer.valueOf(option));

		//		useLatest.setSelected(option == PamArray.ORIGIN_USE_LATEST);
		//		useInterpolate.setSelected(option == PamArray.ORIGIN_INTERPOLATE);
		//		usePrevious.setSelected(option == PamArray.ORIGIN_USE_PRECEEDING);
	}

	public int getSelection() {
		int sel = getSelectedInterpType();
		if (((1<<sel) & allowedValues) == 0) {
			PamDialogFX.showWarning("The selected interpolation is not available with the selected reference position");
			return -1;
		}
		else {
			return sel;
		}
	}
	
	@Override
	protected void enableControls() {
		//get the current selection
		Integer item = interpChoiceBox.getSelectionModel().getSelectedItem();
		
		//clear items
		interpChoiceBox.getItems().clear();

		//set allowed values
		for (int i=0; i<interpChoice.length ; i++) {
		
			if ((allowedValues & (1<<interpChoice[i])) != 0){
				interpChoiceBox.getItems().add(interpChoice[i]); 
			}
		
		}
		
		//reselect the previously selected item if possible. 
		if (interpChoiceBox.getItems().contains(item)) {
			interpChoiceBox.getSelectionModel().select(item);
		} 
		else {
			interpChoiceBox.getSelectionModel().select(0);
		}
		
	}

	@Override
	protected int getSelectedInterpType() {
		Integer choice =  interpChoiceBox.getSelectionModel().getSelectedItem();
		if (choice == null) {
			return -1;
		}
		else return choice;
	}
	
	
}