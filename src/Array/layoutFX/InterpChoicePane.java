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
		interpChoiceBox.getItems().clear();
	
		for (int i=0; i<interpChoice.length ; i++) {
		
			if ((allowedValues & (1<<interpChoice[i])) != 0){
				interpChoiceBox.getItems().add(interpChoice[i]); 
			}
		
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