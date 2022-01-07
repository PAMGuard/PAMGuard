package loggerForms.formdesign.itempanels;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;

public class TitleCtrlColPanel extends TextCtrlColPanel {

	public TitleCtrlColPanel(ControlTitle controlTitle,
			UDColName propertyName, int length) {
		super(controlTitle, propertyName, length);
		textField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				textChanged();
			}
			
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				textChanged();
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				textChanged();
			}
		});
		
	}

	protected void textChanged() {
		this.controlTitle.setTitle(textField.getText());
	}

}
