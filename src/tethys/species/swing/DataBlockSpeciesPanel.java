package tethys.species.swing;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import tethys.species.DataBlockSpeciesManager;
import tethys.species.DataBlockSpeciesMap;
import tethys.species.DataBlockSpeciesTypes;
import tethys.species.SpeciesMapItem;

public class DataBlockSpeciesPanel implements PamDialogPanel {
	
	private JPanel mainPanel;
	
	private PamDataBlock dataBlock;
	
	private JPanel speciesPanel;
	
	private ArrayList<SpeciesSubPanel> subPanels = new ArrayList<>();

	public DataBlockSpeciesPanel(PamDataBlock dataBlock) {
		super();
		this.dataBlock = dataBlock;
		mainPanel = new JPanel(new BorderLayout());
		speciesPanel = new JPanel();
		mainPanel.add(speciesPanel, BorderLayout.CENTER);
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
		
		DataBlockSpeciesManager speciesManager = dataBlock.getDatablockSpeciesManager();
		DataBlockSpeciesTypes speciesTypes = speciesManager.getSpeciesTypes();
		ArrayList<String> speciesNames = speciesTypes.getSpeciesNames();
		DataBlockSpeciesMap speciesMap = speciesManager.getDatablockSpeciesMap();
		for (String aSpecies : speciesNames) {
			SpeciesSubPanel subPanel = new SpeciesSubPanel(aSpecies);
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

}
