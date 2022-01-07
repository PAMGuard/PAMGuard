package pamViewFX.fxNodes.utilityPanes;

import org.controlsfx.control.ToggleSwitch;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import pamViewFX.fxNodes.PamHBox;


/**
 * Toggle switch with a label to the right of the switch, not the default left.
 *  
 * @author Jamie Macaulay
 *
 */
public class PamToggleSwitch extends PamHBox{
	
	public static double MAX_TOGGLE_SWITCH_WIDTH = 20.0; 

	private ToggleSwitch toggleSwitch;

	private Label label;
	

	public PamToggleSwitch(String text) {
		toggleSwitch = new ToggleSwitch(); 
		toggleSwitch.setMaxWidth(20); //bug fix
		label = new Label(text);
		this.setSpacing(5);
		this.setAlignment(Pos.CENTER_LEFT);
			
		this.getChildren().addAll(toggleSwitch, label ); 
	}
	
	public BooleanProperty selectedProperty() {
		return toggleSwitch.selectedProperty(); 
	}
	
	public boolean isSelected() {
		return toggleSwitch.isSelected(); 
	}
	
	public void setSelected(boolean select) {
		 toggleSwitch.setSelected(select);
	}
	
	public ToggleSwitch getToggleSwitch() {
		return toggleSwitch;
	}

	public void setToggleSwitch(ToggleSwitch toggleSwitch) {
		this.toggleSwitch = toggleSwitch;
	}
	
	public void setTooltip(Tooltip tooltip) {
		this.toggleSwitch.setTooltip(tooltip);
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

}
