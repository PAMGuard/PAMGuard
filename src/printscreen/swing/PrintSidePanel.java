package printscreen.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import PamView.PamSidePanel;
import PamView.panel.PamPanel;
import printscreen.PrintScreenControl;

public class PrintSidePanel implements PamSidePanel {

	private PrintScreenControl printScreenControl;
	
	private JPanel mainPanel;
	
	private static final String PRINTSCREEN = "print screen";

	public PrintSidePanel(PrintScreenControl printScreenControl) {
		this.printScreenControl = printScreenControl;
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder(printScreenControl.getUnitName()));
		PrintScreenButton psb = new PrintScreenButton(printScreenControl);
		mainPanel.add(psb.getPrintButton());
		
		// add key binding to CTRL-P
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control P"), PRINTSCREEN);
		mainPanel.getActionMap().put(PRINTSCREEN, new PrintAction());
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String newName) {
		mainPanel.setBorder(new TitledBorder(printScreenControl.getUnitName()));
	}
	
	private class PrintAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			printScreenControl.printScreen();
		}
		
	}

}
