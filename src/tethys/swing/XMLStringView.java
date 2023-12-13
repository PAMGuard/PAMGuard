package tethys.swing;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import PamView.dialog.PamDialog;

public class XMLStringView extends PamDialog {
	
	private JTextArea textArea;

	private XMLStringView(Window parentFrame, String title, String xmlString) {
		super(parentFrame, title, false);
		
		JTextArea textArea = new JTextArea(50, 100);
		JPanel mainPanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		setDialogComponent(mainPanel);
		setResizable(true);
		textArea.setText(xmlString);
		textArea.setEditable(false);
		textArea.setCaretPosition(0);
		
		getCancelButton().setVisible(false);
	}
	
	public static void showDialog(Window parent, String collection, String documentId, String xmlString) {
		String title = String.format("\"%s\"/\"%s\"", collection, documentId);
		XMLStringView view = new XMLStringView(parent, title, xmlString);
		view.setVisible(true);
	}

	@Override
	public boolean getParams() {
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
