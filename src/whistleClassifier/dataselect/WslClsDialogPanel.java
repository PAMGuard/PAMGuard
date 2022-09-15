package whistleClassifier.dataselect;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.dataSelector.DataSelectParams;
import whistleClassifier.FragmentClassifierParams;
import whistleClassifier.WhistleClassificationParameters;
import whistleClassifier.WhistleClassifierControl;

/**
 * dialog for whistle classifier data selector
 * @author dg50
 *
 */
public class WslClsDialogPanel implements PamDialogPanel {

	private WhistleClassifierControl wslClassifierControl;
	private WslClsDataSelector wslClsDataSelector;
	
	private JPanel mainPanel;
	
	private JPanel sppPanel;
	
	private JCheckBox[] speciesBoxes;
	
	private JTextField[] speciesScores;

	public WslClsDialogPanel(WhistleClassifierControl wslClassifierControl, WslClsDataSelector wslClsDataSelector) {
		this.wslClassifierControl = wslClassifierControl;
		this.wslClsDataSelector = wslClsDataSelector;
		
		this.mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Select species"));
		
		sppPanel = new JPanel(new GridBagLayout());
		mainPanel.add(BorderLayout.CENTER, sppPanel);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		WhistleClassificationParameters wslParams = wslClassifierControl.getWhistleClassificationParameters();
		WslClsSelectorParams selParams = wslClsDataSelector.getParams();
		if (wslParams == null) {
			return;
		}
		FragmentClassifierParams fragParams = wslParams.fragmentClassifierParams;
		if (fragParams == null) {
			return;
		}
		fillSppPanel(wslParams, selParams);
	}

	private void fillSppPanel(WhistleClassificationParameters wslParams, WslClsSelectorParams selParams) {
		boolean allowScores = wslClsDataSelector.isAllowScores();
		sppPanel.removeAll();
		String[] sppList = wslParams.fragmentClassifierParams.getSpeciesList();
		if (sppList == null) {
			return;
		}
		int nSpp = sppList.length;
		speciesBoxes = new JCheckBox[nSpp];
		speciesScores = new JTextField[nSpp];
		GridBagConstraints c = new PamGridBagContraints();
		sppPanel.add(new JLabel("Species", JLabel.CENTER), c);
		if (allowScores) {
			c.gridx++;
			JLabel lab = new JLabel(" Min score ", JLabel.CENTER);
			lab.setToolTipText("Minimum classification score (between 0 and 1)");
			sppPanel.add(lab , c);
		}
		for (int i = 0; i < nSpp; i++) {
			speciesBoxes[i] = new JCheckBox(sppList[i]);
			speciesScores[i] = new JTextField(3);
			c.gridx = 0;
			c.gridy++;
			sppPanel.add(speciesBoxes[i], c);
			if (allowScores) {
				c.gridx++;
				sppPanel.add(speciesScores[i], c);
			}
			SppClsSelectParams sppSel = selParams.getSppParams(sppList[i]);
			speciesBoxes[i].setSelected(sppSel.selected);
			speciesScores[i].setText(String.format("%3.2f", sppSel.minScore));
		}
	}

	@Override
	public boolean getParams() {
		WslClsSelectorParams selParams = wslClsDataSelector.getParams();
		boolean allowScores = wslClsDataSelector.isAllowScores();
		if (speciesBoxes == null) {
			return false;
		}
		int nSpp = speciesBoxes.length;
		for (int i = 0; i < nSpp; i++) {
			String name = speciesBoxes[i].getText();
			boolean sel = speciesBoxes[i].isSelected();
			double score = 0;
			if (allowScores) {
				try {
					score = Double.valueOf(speciesScores[i].getText());
				}
				catch (NumberFormatException e) {
					score = -1;
				}
			}
			if (score < 0 || score > 1) {
				return PamDialog.showWarning(null, "Invalid score value for " + name, "Score values must be betwween 0 and 1");
			}
			selParams.setSppParams(name, new SppClsSelectParams(name, sel, score));
		}
		return true;
	}

}
