package tethys.swing.export;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import tethys.localization.CoordinateName;

public class CoordinateChoisePanel implements LocalizationOptionsPanel {

	private CoordinateChoice coordinateChoise;
	
	private JPanel mainPanel;
	
	private JRadioButton[] choiceButtons;

	private CoordinateName[] possibles;

	public CoordinateChoisePanel(CoordinateChoice coordinateChoice) {
		this.coordinateChoise = coordinateChoice;
		possibles = coordinateChoice.getPossibleCoordinates();
		choiceButtons = new JRadioButton[possibles.length];
		ButtonGroup bg = new ButtonGroup();
		mainPanel = new WestAlignedPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Coordinate choice"));
		GridBagConstraints c = new PamGridBagContraints();
		for (int i = 0; i < possibles.length; i++) {
			choiceButtons[i] = new JRadioButton(possibles[i].name());
			bg.add(choiceButtons[i]);
			String tip = possibles[i].getDescription();
			if (tip != null) {
				choiceButtons[i].setToolTipText(tip);
			}
			mainPanel.add(choiceButtons[i], c);
			c.gridy++;
		}
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		CoordinateName current = coordinateChoise.getCoordinateName();
		for (int i = 0; i < possibles.length; i++) {
			choiceButtons[i].setSelected(possibles[i] == current);
		}
	}

	@Override
	public boolean getParams() {
		for (int i = 0; i < possibles.length; i++) {
			if (choiceButtons[i].isSelected()) {
				coordinateChoise.setCoordinateName(possibles[i]);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBig() {
		return possibles.length > 1;
	}

}
