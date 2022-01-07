package alfa.help.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;

import javax.help.JHelpContentViewer;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import PamView.panel.PamPanel;
import alfa.ALFAControl;

public class ALFAHelpPanel {

	private ALFAControl alfaControl;
	
	private PamPanel mainPanel;
	
	private JEditorPane htmlPane;
	
	private static final String helpFile = "alfa/help/doc/ALFAOnePageHelp.html";
//	private static final String helpFile = "alfa/help/doc/TestRTF.rtf";

	public ALFAHelpPanel(ALFAControl alfaControl) {
		this.alfaControl = alfaControl;
		mainPanel = new PamPanel(new BorderLayout());
		htmlPane = new JEditorPane();
//		htmlPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(htmlPane);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		/*
		 * Stick the single html file into it ...
		 */
		try {
			URL help = ClassLoader.getSystemResource(helpFile);
			htmlPane.setPage(help);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

}
