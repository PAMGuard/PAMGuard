package PamUtils.worker;

import java.awt.Dimension;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamTextDisplay;

public class PamWorkDialog extends PamDialog {
	
	private JProgressBar progressBar;
	
	private PamTextDisplay[] textRows;

	public PamWorkDialog(Window parentFrame, int nTextRows, String title) {
		super(parentFrame, title, false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new TitledBorder("Task Progress"));
//		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(progressBar = new JProgressBar(0, 100));
		Dimension sz = progressBar.getPreferredSize();
		sz.width = 300;
		progressBar.setPreferredSize(sz);
		textRows = new PamTextDisplay[nTextRows];
		for (int i = 0; i < nTextRows; i++) {
//			c.gridy++;
			textRows[i] = new PamTextDisplay(60);
			mainPanel.add(textRows[i]);
		}
		progressBar.setIndeterminate(true);
		getOkButton().setVisible(false);
		getCancelButton().setVisible(false);
		getButtonPanel().setVisible(false);
		setDialogComponent(mainPanel);
		setResizable(true);
	}
	
	public void update(PamWorkProgressMessage progressMsg) {
		if (progressMsg == null) return;
		Integer progress = progressMsg.progress;
		if (progress != null) {
			progressBar.setIndeterminate(progressMsg.progress < 0);
			if (progressMsg.progress> 0) {
				progressBar.setValue(progressMsg.progress);
			}
		}
		if (progressMsg.textLines == null) {
			return;
		}
		int nT = Math.min(progressMsg.textLines.length, textRows.length);
		for (int i = 0; i <nT; i++) {
			if (progressMsg.textLines[i] != null) {
				textRows[i].setText(progressMsg.textLines[i]);
			}
		}
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
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
