package pamViewFX.fxNodes;

import javafx.scene.control.TextField;

public class PamTextField extends TextField {

	/**
	 * Construct a basic text field
	 */
	public PamTextField() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Construct a text field and initialise it with the given text
	 * @param text initial text
	 */
	public PamTextField(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Construct a text field with a preferred number of columns
	 * @param nColumns number of columns
	 */
	public PamTextField(int nColumns) {
		super();
		setPrefColumnCount(nColumns);
	}
	
	/**
	 * Construct a text field with initial text and a preferred number of columns
	 * @param text initial text
	 * @param nColumns number of columns
	 */
	public PamTextField(String text, int nColumns) {
		super(text);
		setPrefColumnCount(nColumns);
	}
	
}
