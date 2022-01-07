package PamView.dialog;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class RemoveButton extends JButton {
	
	private static final long serialVersionUID = 1L;

	public RemoveButton() {
		super(makeIcon());
	}
	
	private static ImageIcon makeIcon() {
		return new ImageIcon(ClassLoader.getSystemResource("Resources/delete.png"));
	}

	/**
	 * @param a
	 */
	public RemoveButton(Action a) {
		super(a);
		setIcon(makeIcon());
	}

	/**
	 * @param text
	 */
	public RemoveButton(String text) {
		super(text, makeIcon());
	}

}
