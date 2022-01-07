package nmeaEmulator;

import javax.swing.JTextField;
import javax.swing.text.Document;

public class SerialTextField extends JTextField {

	public SerialTextField() {
		super();
		setEditable(false);
	}

	public SerialTextField(Document arg0, String arg1, int arg2) {
		super(arg0, arg1, arg2);
		setEditable(false);
	}

	public SerialTextField(int arg0) {
		super(arg0);
		setEditable(false);
	}

	public SerialTextField(String arg0, int arg1) {
		super(arg0, arg1);
		setEditable(false);
	}

	public SerialTextField(String arg0) {
		super(arg0);
		setEditable(false);
	}

}
