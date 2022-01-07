package alfa.swinggui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamButton;
import PamView.panel.PamPanel;
import alfa.ALFAControl;

public class ALFAOptionsPanel {

	private ALFAControl alfaControl;

	private JPanel mainPanel;
	
	private JButton optsButton;
	
	public ALFAOptionsPanel(ALFAControl alfaControl) {
		this.alfaControl = alfaControl;
		mainPanel = new PamPanel();
		mainPanel.setBorder(new TitledBorder("Options"));
		optsButton = new PamButton("Settings...");
		optsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				optionsButton();
			}

		});
		mainPanel.add(optsButton);
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}
	
	private void optionsButton() {
		alfaControl.showAlfaDialog();
	}

}
