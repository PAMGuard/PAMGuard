package tethys.species.swing;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialogPanel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.PamDataBlock;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesMap;
import tethys.species.SpeciesManagerObserver;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.SpeciesMapItem;

public class DataBlockSpeciesPanel implements PamDialogPanel, SpeciesManagerObserver {
	
	private JPanel mainPanel;
	
	private PamDataBlock dataBlock;
	
	private JPanel speciesPanel;
	
	private ArrayList<SpeciesSubPanel> subPanels = new ArrayList<>();

	private String singleSpecies;

	private DataBlockSpeciesManager speciesManager;

	/**
	 * Panel of info about a species name in PAMGuard relating it to a call type and ITIS 
	 * code for output to Tethys. 
	 * @param dataBlock Datablock with a DataBlockSpeciesManager
	 * @param singleSpecies single species if only one species to be shown. null for all species. 
	 */
	public DataBlockSpeciesPanel(PamDataBlock dataBlock, String singleSpecies) {
		super();
		this.dataBlock = dataBlock;
		this.singleSpecies = singleSpecies;

		speciesManager = dataBlock.getDatablockSpeciesManager();
		
		mainPanel = new JPanel(new BorderLayout());
		if (singleSpecies == null) {
		    // only add additional options if it's the more global use of this dialog. 
			 PamDialogPanel dialogPanel = speciesManager.getDialogPanel(this);
			 if (dialogPanel != null) {
				 JPanel nwPanel = new WestAlignedPanel(dialogPanel.getDialogComponent());
				 mainPanel.add(BorderLayout.NORTH, nwPanel);
			 }
		}
		
		speciesPanel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(speciesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.setBorder(new TitledBorder(dataBlock.getDataName()));
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}
	

	@Override
	public void setParams() {
		speciesPanel.removeAll();
		speciesPanel.setLayout(new BoxLayout(speciesPanel, BoxLayout.Y_AXIS));
		subPanels.clear();
		
//		DataBlockSpeciesCodes speciesTypes = speciesManager.getSpeciesCodes();
		ArrayList<String> speciesNames = speciesManager.getAllSpeciesCodes();
		DataBlockSpeciesMap speciesMap = speciesManager.getDatablockSpeciesMap();
		for (String aSpecies : speciesNames) {
			if (singleSpecies != null && singleSpecies.equals(aSpecies) == false) {
				continue;
			}
			SpeciesSubPanel subPanel = new SpeciesSubPanel(dataBlock, aSpecies);
			subPanels.add(subPanel);
			speciesPanel.add(subPanel.getDialogComponent());
			if (speciesMap != null) {
				SpeciesMapItem speciesInfo = speciesMap.getItem(aSpecies);
				subPanel.setParams(speciesInfo);
			}
		}
	}

	@Override
	public boolean getParams() {
		DataBlockSpeciesManager speciesManager = dataBlock.getDatablockSpeciesManager();
		DataBlockSpeciesMap speciesMap = speciesManager.getDatablockSpeciesMap();
		int errors = 0;
		for (SpeciesSubPanel subPanel : subPanels) {
			SpeciesMapItem mapItem = subPanel.getParams();
			if (mapItem == null) {
				errors++;
			}
			else {
				speciesMap.putItem(mapItem.getPamguardName(), mapItem);
			}
		}
		return errors == 0;
	}

	@Override
	public void update() {
		setParams();
	}

}
