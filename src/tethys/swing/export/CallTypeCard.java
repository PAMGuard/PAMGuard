package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamAlignmentPanel;
import PamView.wizard.PamWizard;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesMap;
import tethys.species.SpeciesMapItem;
import tethys.species.SpeciesMapManager;
import tethys.species.swing.DataBlockSpeciesDialog;

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
	private JLabel[] itisLabels;
	private JLabel[] speciesNames;
	private JLabel[] callTypes;
	private JCheckBox commonNames;
	
	private ArrayList<String> names;
	
	private String bitTip = "Select species  / call types to export\n"
			+ "Only selected types will be added to the Document Effort section";

	private DataBlockSpeciesMap speciesMap;

	public CallTypeCard(TethysControl tethysControl, PamWizard pamWizard, String title, PamDataBlock dataBlock) {
		super(tethysControl, pamWizard, title, dataBlock);
		speciesManager = dataBlock.getDatablockSpeciesManager();
		speciesMap = speciesManager.getDatablockSpeciesMap();
		names = speciesManager.getAllSpeciesCodes();
		speciesSelection = new JCheckBox[names.size()];
		itisLabels = new JLabel[names.size()];
		speciesNames = new JLabel[names.size()];
		callTypes = new JLabel[names.size()];
		JPanel spPanel = new JPanel();
		spPanel.setLayout(new GridBagLayout());
		spPanel.setToolTipText(bitTip);
		PamGridBagContraints c = new PamGridBagContraints();
		commonNames = new JCheckBox("Species");
		commonNames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fillAllItemInfo();
			}
		});
		String[] tits = {"Select", "PAMGuard Name", "ITIS Code", "Species", "Call Type" };
		String[] tips = {"Select for export", "Internal PAMGuard Name", "ITIS Species Code", "Species Name (check for common names)", "Call Type (written to Tethys)"};
		for (int i = 0; i < tits.length; i++) {
			JComponent lab;
			if (i == 3) {
				spPanel.add(lab=commonNames, c);
			}
			else {
				spPanel.add(lab = new JLabel(tits[i], JLabel.LEFT), c);
			}
			lab.setToolTipText(tips[i]);
			c.gridx++;
		}
		for (int i = 0; i < names.size(); i++) {
			c.gridx = 0;
			c.gridy++;
			speciesSelection[i] = new JCheckBox();
			spPanel.add(speciesSelection[i], c);
			c.gridx++;
			spPanel.add(new JLabel(names.get(i)), c);
			c.gridx++;
			spPanel.add(itisLabels[i] = new JLabel(), c);
			c.gridx++;
			spPanel.add(speciesNames[i] = new JLabel(), c);
			c.gridx++;
			spPanel.add(callTypes[i] = new JLabel(), c);
			fillItemInfo(i);

			c.gridx++;
			PamSettingsIconButton but = new PamSettingsIconButton();
			but.setToolTipText("Edit species id and call type");
			but.addActionListener(new SpeciesAction(i));
			spPanel.add(but, c);
		}
		JPanel nPanel = new PamAlignmentPanel(spPanel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(nPanel);
		this.setBorder(new TitledBorder("Species / Call types to export"));
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel(new BorderLayout());
//		southPanel.setBorder(new TitledBorder("Controls"));
		JButton importButton = new JButton("Import species mapping");
		importButton.setToolTipText("Import a species map previously saved from another PAMGuard configuration");
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importSpeciesMap(importButton);
			}
		});
		southPanel.add(importButton, BorderLayout.WEST);
		this.add(southPanel, BorderLayout.SOUTH);
	}
	
	private void fillAllItemInfo() {
		for (int i = 0; i < names.size(); i++) {
			fillItemInfo(i);
		}
	}

	private void fillItemInfo(int itemIndex) {
		SpeciesMapItem item = speciesMap.getItem(names.get(itemIndex));
		if (item == null) {
			itisLabels[itemIndex].setText(null);
			speciesNames[itemIndex].setText(null);
			callTypes[itemIndex].setText(null);
		}
		else {
			itisLabels[itemIndex].setText(Integer.valueOf(item.getItisCode()).toString());
			if (commonNames.isSelected()) {
				speciesNames[itemIndex].setText(item.getCommonName());
			}
			else {
				speciesNames[itemIndex].setText(item.getLatinName());
			}
			callTypes[itemIndex].setText(item.getCallType());
		}
		
	}

	protected void importSpeciesMap(JButton importButton) {
		SpeciesMapManager mapManager = SpeciesMapManager.getInstance();
		mapManager.importSpeciesMaps(getPamWizard(), getDataBlock().getLongDataName());
		// has the map changed ? 
		DataBlockSpeciesMap newMap = getDataBlock().getDatablockSpeciesManager().getDatablockSpeciesMap();
		if (newMap != speciesMap) {
//			System.out.println("Map changed");
			speciesMap = newMap;
			fillAllItemInfo();
		}
	}

	private class SpeciesAction implements ActionListener {

		private int itemIndex;

		public SpeciesAction(int itemIndex) {
			this.itemIndex = itemIndex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String name = names.get(itemIndex);
			DataBlockSpeciesDialog.showDialog(PamController.getMainFrame(), getDataBlock(), name);
			fillItemInfo(itemIndex);
		}
		
	}
	@Override
	public boolean getParams(StreamExportParams cardParams) {
		int nSel = 0;
		int nError = 0;
		for (int i = 0; i < names.size(); i++) {
			boolean sel = speciesSelection[i].isSelected();
			cardParams.setSpeciesSelection(names.get(i), sel);
			if (sel) {
				nSel++;
			}
			if (sel == false) {
				continue; // don't check items that are not getting exported. 
			}
			// check that the item has a valid species code and call type
			SpeciesMapItem item = speciesMap.getItem(names.get(i));
			if (item == null) {
				nError++;
				continue;
			}
			if (item.getItisCode() == 0) {
				nError ++;
			}
			if (item.getCallType() == null || item.getCallType().length() == 0) {
				nError ++;
			}
			
		}
		if (nError > 0) {
			WarnOnce.showWarning("Species / Call type selection", "One or more selected items do not have a valid ITIS code or a Call Type", WarnOnce.WARNING_MESSAGE);
			return false;
		}
		
		if (nSel == 0) {
			WarnOnce.showWarning("Species / Call type selection", "You must select at least one species or call type for export", WarnOnce.WARNING_MESSAGE);
			return false;
		}
		return true;
	}

	@Override
	public void setParams(StreamExportParams cardParams) {
		for (int i = 0; i < names.size(); i++) {
			boolean sel = cardParams.getSpeciesSelection(names.get(i));
			speciesSelection[i].setSelected(sel);
		}
		
	}

}
