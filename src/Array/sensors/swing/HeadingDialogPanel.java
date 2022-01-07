package Array.sensors.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import Array.sensors.ArrayDisplayParameters;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;

public class HeadingDialogPanel implements PamDialogPanel {
	
	private JPanel mainPanel;
	private ArrayDisplayParamsDialog arrayDisplayParamsDialog;
	private JRadioButton[] headButtons = new JRadioButton[2];

	public HeadingDialogPanel(ArrayDisplayParamsDialog arrayDisplayParamsDialog) {
		super();
		this.arrayDisplayParamsDialog = arrayDisplayParamsDialog;
		JPanel headPanel = new JPanel();
		headPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		ButtonGroup bg = new ButtonGroup();
		headPanel.add(new JLabel("Heading value range: "), c);
		for (int i = 0; i < 2; i ++) {
			c.gridx++;
			headPanel.add(headButtons[i] = new JRadioButton(ArrayDisplayParameters.HEADNAMES[i]));
			bg.add(headButtons[i]);
		}
		mainPanel = new PamAlignmentPanel(headPanel, BorderLayout.WEST);
		mainPanel.setBorder(new TitledBorder("Heading"));
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		ArrayDisplayParameters params = arrayDisplayParamsDialog.currentParams;
		for (int i = 0; i < 2; i ++) {
			headButtons[i].setSelected(params.getHeadRange() == i);
		}
	}

	@Override
	public boolean getParams() {
		ArrayDisplayParameters params = arrayDisplayParamsDialog.currentParams;
		for (int i = 0; i < 2; i ++) {
			if (headButtons[i].isSelected()) {
				params.setHeadRange(i);
				return true;
			}
		}
		return arrayDisplayParamsDialog.showWarning("No heading angle range selected");
	}

}
