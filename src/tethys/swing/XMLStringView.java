package tethys.swing;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
		
		JPopupMenu popMenu = new JPopupMenu();
		textArea.setComponentPopupMenu(popMenu);
		JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.copy();
			}
		});
		JMenuItem selItem = new JMenuItem("Select All");
		selItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.selectAll();
			}
		});
		
		popMenu.add(copyItem);
		popMenu.addSeparator();
		popMenu.add(selItem);
		
		getCancelButton().setVisible(false);
		setModal(false);
		
		getOkButton().setText("Close");
		getOkButton().setToolTipText("Close window");
	}
	
	public static void showDialog(Window parent, String collection, String documentId, String xmlString) {
		String title = String.format("\"%s\"/\"%s\"", collection, documentId);
		XMLStringView view = new XMLStringView(parent, title, xmlString);
		view.setVisible(true);
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
