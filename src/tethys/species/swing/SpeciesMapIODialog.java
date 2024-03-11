package tethys.species.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import tethys.species.DataBlockSpeciesMap;
import tethys.species.GlobalSpeciesMap;

/**
 * dialog to select which species maps to import / export. 
 * @author dg50
 *
 */
public class SpeciesMapIODialog extends PamDialog {

	private static SpeciesMapIODialog singleInstance = null;
	private GlobalSpeciesMap speciesMap;
	
	private JCheckBox everything;
	private JCheckBox[] blockBoxes;
	
	private JPanel boxesPanel;
	
	/**
	 * @param parentFrame
	 * @param title
	 * @param hasDefault
	 */
	private SpeciesMapIODialog(Window parentFrame) {
		super(parentFrame, "Map IO", true);

		boxesPanel = new JPanel();
		boxesPanel.setBorder(new TitledBorder("Select datablocks"));
		
		setDialogComponent(boxesPanel);
	}
	
	public static GlobalSpeciesMap showDialog(Window parentFrame, GlobalSpeciesMap speciesMap, boolean export) {
		if (singleInstance == null) {
			singleInstance = new SpeciesMapIODialog(parentFrame);
		}
		if (speciesMap.getDatablockMaps().size() == 0) {
			singleInstance.showWarning("No Data block species maps are defined");
			return speciesMap;
		}
		singleInstance.setTitle(export ? "Export species maps" : "Import species maps");
		singleInstance.setParams(speciesMap);
		singleInstance.setVisible(true);
		
		return singleInstance.speciesMap;
	}

	private void setParams(GlobalSpeciesMap speciesMap) {
		this.speciesMap = speciesMap.clone();
		boxesPanel.removeAll();
		HashMap<String, DataBlockSpeciesMap> blockMaps = speciesMap.getDatablockMaps();
		Set<Entry<String, DataBlockSpeciesMap>> mapSet = blockMaps.entrySet();
		Iterator<Entry<String, DataBlockSpeciesMap>> iter = mapSet.iterator();		
		
		boxesPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		boxesPanel.add(everything = new JCheckBox("Select All"), c);
		everything.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		blockBoxes = new JCheckBox[mapSet.size()];
		int iBox = 0;
		while (iter.hasNext()) {
			Entry<String, DataBlockSpeciesMap> item = iter.next();
			c.gridy++;
			blockBoxes[iBox] = new JCheckBox(item.getKey()); 
			boxesPanel.add(blockBoxes[iBox], c);	
			iBox++;
		}
		
		enableControls();
	}

	protected void enableControls() {
		if (blockBoxes == null) {
			return;
		}
		boolean selAll = everything.isSelected();
		for (int i = 0; i < blockBoxes.length; i++) {
			blockBoxes[i].setEnabled(!selAll);
			if (selAll) {
				blockBoxes[i].setSelected(true);
			}
		}
	}

	@Override
	public boolean getParams() {
		if (everything.isSelected()) {
			return true;
		}
		HashMap<String, DataBlockSpeciesMap> blockMaps = speciesMap.getDatablockMaps();
		Set<Entry<String, DataBlockSpeciesMap>> mapSet = blockMaps.entrySet();
		Iterator<Entry<String, DataBlockSpeciesMap>> iter = mapSet.iterator();	
		for(int i = 0; i < blockBoxes.length; i++) {
			if (blockBoxes[i].isSelected() == false) {
				String name = blockBoxes[i].getText();
				blockMaps.remove(name);
			}
		}
//		int iBox = 0;
//		while (iter.hasNext()) {
//			if (blockBoxes[iBox].isSelected() == false) {
//				iter.remove();
//			}
//			iBox++;
//		}
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		speciesMap = null;
	}

	@Override
	public void restoreDefaultSettings() {
		everything.setSelected(true);
		enableControls();
	}

}
