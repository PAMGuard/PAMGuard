package pamViewFX.fxNodes;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.shape.Shape;

/**
 * Allows users to pick a specific shape
 * @author Jamie Macaulay
 *
 */
public class PamShapePicker extends ChoiceBox<Shape> {
	
	ObservableList<Shape> shapeList=FXCollections.observableList(new ArrayList<Shape>());
	
	public PamShapePicker(){
		
	}
	
	
	

}
