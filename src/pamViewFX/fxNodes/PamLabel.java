package pamViewFX.fxNodes;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class PamLabel extends Label {


	/**
	 * 
	 */
	public PamLabel() {
		super();
	}

	/**
	 * @param text
	 * @param graphic
	 */
	public PamLabel(String text, Node graphic) {
		super(text, graphic);
	}

	/**
	 * @param text
	 */
	public PamLabel(String text) {
		super(text);
	}
	
	/**
	 * Crate a label with the text aligned in a specific manner
	 * @param text text for label
	 * @param position alignment option
	 */
	public PamLabel(String text, Pos position) {
		super(text);
		setAlignment(position);
	}

}
