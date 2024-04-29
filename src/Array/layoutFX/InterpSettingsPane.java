package Array.layoutFX;

import Array.PamArray;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.control.ToggleGroup;




/**
 * Radio buttons which allow selection of interpolation options. 
 * 
 * @author Jamie Macaulay
 *
 */
public class InterpSettingsPane extends PamBorderPane {
		
	/**
	 * Interp choice. 
	 */
	public static Integer[] interpChoice = new Integer[] {PamArray.ORIGIN_USE_LATEST, 
			PamArray.ORIGIN_INTERPOLATE, PamArray.ORIGIN_USE_PRECEEDING}; 
	
	private RadioButton[] radioButton; 

	protected int allowedValues = 0xFF; // bitmap of banned values !

	public InterpSettingsPane() {
		this.setCenter(createInterpPane());
	}

	
	protected Pane createInterpPane() {	
		PamGridPane gridPane = new PamGridPane(); 

		gridPane.setVgap(5);

		int gridy=0;
		
		radioButton = new RadioButton[interpChoice.length]; 
		ToggleGroup group = new ToggleGroup();

		for (int i=0; i<radioButton.length ; i++) {
			gridPane.add(radioButton[i] = new RadioButton( getInterpString(interpChoice[i])), 0, gridy);
			radioButton[i].setTooltip(new Tooltip( getInterpTip(interpChoice[i])));
			gridy++;
			radioButton[i].setToggleGroup(group);

		}

		return gridPane; 
	}


	/**
	 * Get a description of the interpolation type for an interp-type flag. 
	 * @param interpType - the interpolation type flag. 
	 * @return a description of that interpolation methid. 
	 */
	public String getInterpString(int interpType) {
		String description = null;
		switch ( interpType) {
		case PamArray.ORIGIN_USE_LATEST:
			description = "Use only the latest value";
			break;
		case PamArray.ORIGIN_INTERPOLATE:
			description = "Interpolate between values";

			break;
		case PamArray.ORIGIN_USE_PRECEEDING:
			description = "Use the location for the time preceeding each data unit";
			break;
		}
		return description;
	}

	
	/**
	 * Get a description of the interpolation type for an interp-type flag. 
	 * @param interpType - the interpolation type flag. 
	 * @return a description of that interpolation methid. 
	 */
	public String getInterpTip(int interpType) {
		String description = null;
		switch ( interpType) {
		case PamArray.ORIGIN_USE_LATEST:
			description = "Select this option if you have a simple static array in a single location for the entire data set";
			break;
		case PamArray.ORIGIN_INTERPOLATE:
			description = "Select this option if you are storing multiple locations for slowely moving (i.e. not quite fixed) devices";

			break;
		case PamArray.ORIGIN_USE_PRECEEDING:
			description = "Select this option if you have devices which are periodically moved from one spot to another";
			break;
		}
		return description;
	}


	public void setSelection(int option) {
		for (int i=0; i<interpChoice.length; i++) {
			radioButton[i].setSelected(option == interpChoice[i]);
		}
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


	/**
	 * @return the allowedValues
	 */
	protected int getAllowedValues() {
		return allowedValues;
	}

	/**
	 * @param allowedValues the allowedValues to set
	 */
	protected void setAllowedValues(int allowedValues) {
		this.allowedValues = allowedValues;
		enableControls();
	}

	protected void enableControls() {
		for (int i=0; i<interpChoice.length; i++) {
			radioButton[i].setDisable((allowedValues & (1<<interpChoice[i])) == 0);
		}
	}

	protected int getSelectedInterpType() {
		for (int i=0; i<interpChoice.length; i++) {
			if (radioButton[i].isSelected()) {
				return interpChoice[i];
			}
			radioButton[i].setDisable((allowedValues & (1<<interpChoice[i])) == 0);
		}
		return -1;
	}





}