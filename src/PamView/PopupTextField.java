package PamView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JTextField;

/*
 * Quick popup window to allow quick entry of text
 */
public class PopupTextField {
	
	private JTextField textField;
	
	private JDialog frame;
	
	private PopupTextField(String title) {
//		frame = new JFrame();
		frame = new JDialog();
		frame.setIconImage(new ImageIcon(ClassLoader
				.getSystemResource("Resources/pamguardIcon.png")).getImage());
		frame.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		frame.setIconImage(null);
		frame.setLayout(new BorderLayout());
		frame.add(BorderLayout.CENTER, textField = new JTextField(6));
		frame.setTitle(title);
		textField.addActionListener(new EnterAction());
		textField.addFocusListener(new TextFocusListener());
		frame.pack();
		
	}

	public static Double getValue(Component parent, String title, Point pt, Double range) {
		PopupTextField ptf = new PopupTextField(title);
		ptf.textField.setText(range.toString());
		ptf.frame.setLocation(pt);
		ptf.frame.setVisible(true);

		// blocks after setVisible. 
		Double newVal = null;
		try {
			newVal = Double.valueOf(ptf.textField.getText());
		}
		catch (NumberFormatException e){
			return null;
		}
		
		return newVal;
	}
	
	public void enterAction() {
		frame.setVisible(false);
	}
	
	public void closeAction() {
		textField.setText(null);
		frame.setVisible(false);
	}

	private class EnterAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			enterAction();
		}
		
	}

	private class TextFocusListener extends FocusAdapter {

		@Override
		public void focusLost(FocusEvent e) {
			closeAction();
		}
		
	}
}
