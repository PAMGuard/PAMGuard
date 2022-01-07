package printscreen.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import PamView.dialog.PamButton;
import printscreen.PrintScreenControl;

public class PrintScreenButton {

	private PrintScreenControl printScreenControl;
	
	private JButton printButton;	

	public PrintScreenButton(PrintScreenControl printScreenControl) {
		this.printScreenControl = printScreenControl;
		printButton = new PamButton("Capture Screen(s)");
		printButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				printAction(e);
			}
		});
		printButton.setToolTipText(PrintScreenControl.getToolTip());
	}

	private void printAction(ActionEvent e) {
		printScreenControl.printScreen();
	}
	
	/**
	 * @return the printButton
	 */
	public JButton getPrintButton() {
		return printButton;
	}
	
}
