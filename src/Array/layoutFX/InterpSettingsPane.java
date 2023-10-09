package Array.layoutFX;

import Array.PamArray;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleGroup;



/**
 * Dialog component used by both the streamer and the hydrophone dialogs
 * 
 * @author Doug Gillespie
 *
 */
public class InterpSettingsPane extends PamBorderPane {
	
	private RadioButton useLatest, usePrevious, useInterpolate;
	
	
	private int allowedValues = 0xFF; // bitmap of banned values !
	
	ChoiceBox<String> interpBox; 

	public InterpSettingsPane() {
		
		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setVgap(5);
		
		int gridy=0;
		
		gridPane.add(useLatest = new RadioButton("Use only the latest value"), 0, gridy);
		gridy++;
		gridPane.add(useInterpolate = new RadioButton("Interpolate between values"), 0, gridy);
		gridy++;
		gridPane.add(usePrevious = new RadioButton("Use the location for the time preceeding each data unit"), 0, gridy);
		useLatest.setTooltip(new Tooltip(
				"Select this option if you have a simple static array in a single location for the entire data set"));
		useInterpolate.setTooltip(new Tooltip(
				"Select this option if you are storing multiple locations for slowely moving (i.e. not quite fixed) devices"));
		usePrevious.setTooltip(new Tooltip(
				"Select this option if you have devices which are periodically moved from one spot to another"));
		
		ToggleGroup group = new ToggleGroup();
		useLatest.setToggleGroup(group);
		useInterpolate.setToggleGroup(group);
		usePrevious.setToggleGroup(group);
		
		this.setCenter(gridPane);

	}
	


	public void setSelection(int option) {
		useLatest.setSelected(option == PamArray.ORIGIN_USE_LATEST);
		useInterpolate.setSelected(option == PamArray.ORIGIN_INTERPOLATE);
		usePrevious.setSelected(option == PamArray.ORIGIN_USE_PRECEEDING);
	}
	
	public int getSelection() {
		int sel = getSelectedButton();
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

	private void enableControls() {
		useLatest.setDisable((allowedValues & (1<<PamArray.ORIGIN_USE_LATEST)) == 0);
		useInterpolate.setDisable((allowedValues & (1<<PamArray.ORIGIN_INTERPOLATE)) == 0);
		usePrevious.setDisable((allowedValues & (1<<PamArray.ORIGIN_USE_PRECEEDING)) == 0);
	}

	private int getSelectedButton() {
		if (useLatest.isSelected()) {
			return PamArray.ORIGIN_USE_LATEST;
		}
		else if (useInterpolate.isSelected()) {
			return PamArray.ORIGIN_INTERPOLATE;
		}
		else if (usePrevious.isSelected()) {
			return PamArray.ORIGIN_USE_PRECEEDING;
		}
		return -1;
	}
	



	
}