package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.wizard.PamWizard;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesMap;
import tethys.species.SpeciesMapItem;

/**
 * Wizard card to select call types (species) to export. Only these names will be added to the 
 * detection effort and only these calls will be exported. Think of it as an additional data filter. 
 * @author dg50
 *
 */
public class CallTypeCard extends ExportWizardCard {

	private DataBlockSpeciesManager speciesManager;
//	private DataBlockSpeciesCodes codes;
	
	private JCheckBox[] speciesSelection;
	private ArrayList<String> names;
	
	private String bitTip = "Select species  / call types to export\n"
			+ "Only selected types will be added to the Document Effort section";

	public CallTypeCard(TethysControl tethysControl, PamWizard pamWizard, String title, PamDataBlock dataBlock) {
		super(tethysControl, pamWizard, title, dataBlock);
		speciesManager = dataBlock.getDatablockSpeciesManager();
		DataBlockSpeciesMap spMap = speciesManager.getDatablockSpeciesMap();
		names = speciesManager.getAllSpeciesCodes();
		speciesSelection = new JCheckBox[names.size()];
		JPanel spPanel = new JPanel();
		spPanel.setBorder(new TitledBorder("Species / Call types to export"));
		spPanel.setLayout(new GridBagLayout());
		spPanel.setToolTipText(bitTip);
		PamGridBagContraints c = new PamGridBagContraints();
		for (int i = 0; i < names.size(); i++) {
			c.gridx = 0;
			speciesSelection[i] = new JCheckBox(names.get(i));
			spPanel.add(speciesSelection[i], c);
			SpeciesMapItem item = spMap.getItem(names.get(i));
			if (item != null) {
				String latin = item.getLatinName();
				int itis = item.getItisCode();
				c.gridx++;
				spPanel.add(new JLabel(String.format("%d", itis), JLabel.RIGHT), c);
				c.gridx++;
				if (latin != null) {
					spPanel.add(new JLabel(latin));
				}
			}
			
			c.gridy++;
		}

		this.setLayout(new BorderLayout());
		this.add(spPanel, BorderLayout.NORTH);
	}

	@Override
	public boolean getParams(StreamExportParams cardParams) {
		int nSel = 0;
		for (int i = 0; i < names.size(); i++) {
			boolean sel = speciesSelection[i].isSelected();
			cardParams.setSpeciesSelection(names.get(i), sel);
			if (sel) {
				nSel++;
			}
		}
		if (nSel == 0) {
			WarnOnce.showWarning("Species / Call type selection", "You must select at least one species or call type for export", WarnOnce.WARNING_MESSAGE);
//			return false;
		}
		return nSel >= 0;
	}

	@Override
	public void setParams(StreamExportParams cardParams) {
		for (int i = 0; i < names.size(); i++) {
			boolean sel = cardParams.getSpeciesSelection(names.get(i));
			speciesSelection[i].setSelected(sel);
		}
		
	}

}
