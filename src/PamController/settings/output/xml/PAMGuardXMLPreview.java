package PamController.settings.output.xml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;

public class PAMGuardXMLPreview extends PamDialog {

	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	private JLabel summary;
	
	private String xmlString;
	
	public PAMGuardXMLPreview(Window parentFrame, String title, String xmlDoc) {
		super(parentFrame, title, false);
		xmlString = xmlDoc;
		getCancelButton().setVisible(false);
		textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(900, 600));
		
		
		JPanel borderPane = new JPanel(new BorderLayout());
		borderPane.add(BorderLayout.CENTER, scrollPane);
		borderPane.add(BorderLayout.NORTH, summary = new JLabel(" "));
		summary.setBorder(new EmptyBorder(3, 5, 3, 0));
		
		setDialogComponent(borderPane);
		
		this.setResizable(true);
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				textArea.setText(xmlDoc);
				int nChar = xmlDoc.length();
				int nLines = textArea.getLineCount();
				summary.setText(String.format("  XML doc contains %d characters in %d lines", nChar, nLines));
			}
		});
		
		setVisible(true);
	}

	@Override
	public boolean getParams() {
		return true;
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
