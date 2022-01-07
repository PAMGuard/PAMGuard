package PamView.symbol;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import PamController.PamController;
import PamView.GeneralProjector;
import PamView.dialog.GenericSwingDialog;
import PamguardMVC.PamDataBlock;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.superdet.SuperDetection;
import generalDatabase.SQLLogging;
import generalDatabase.SuperDetLogging;
import offlineProcessing.superdet.OfflineSuperDetFilter;

/**
 * Some global management of PAM symbols, in particular needed for superdetection
 * symbol options when a supdet symbol manager says to use colour by superdetection.  
 * @author dg50
 *
 */
public class GlobalSymbolManager {

	private static GlobalSymbolManager singleInstance;
	
	private GlobalSymbolManager() {
		
	}
	
	public static GlobalSymbolManager getInstance() {
		if (singleInstance == null) {
			synchronized (GlobalSymbolManager.class) {
				if (singleInstance == null) {
					singleInstance = new GlobalSymbolManager();
				}
			}
		}
		return singleInstance;
	}
	
	public List<JMenuItem> getSuperDetMenuItems(PamDataBlock subDataBlock, String displayName, GeneralProjector projector, SymbolUpdateMonitor updateMonitor) {
		ArrayList<SuperDetDataBlock> superBlocks = OfflineSuperDetFilter.findPossibleSuperDetections(subDataBlock);
//		ArrayList<PamDataBlock> superBlocks = PamController.getInstance().getDataBlocks(SuperDetection.class, true);
		if (superBlocks == null) {
			return null;
		}
		/**
		 * Only interested in 'real' superdetections, which have subtables, not those
		 * which just temporarily hold subdet data as a convenience. 
		 */
		ArrayList<JMenuItem> menuItems = new ArrayList<JMenuItem>();
		for (PamDataBlock dataBlock : superBlocks) {
			SQLLogging logging = dataBlock.getLogging();
			if (logging == null || logging instanceof SuperDetLogging == false) {
				continue;
			}
			SuperDetLogging superDetLogging = (SuperDetLogging) logging;
			if (superDetLogging.getSubLogging() == null) {
				continue;
			}
			PamSymbolManager symbolManager = dataBlock.getPamSymbolManager();
			if (symbolManager == null) {
				continue;
			}
			JMenuItem item = new JMenuItem(dataBlock.getDataName());
			item.addActionListener(new SymbolModifierAction(symbolManager, displayName, projector, updateMonitor));
			menuItems.add(item);
		}
		
		return menuItems;
	}
	
	private class SymbolModifierAction implements ActionListener {

		private String displayName;
		private GeneralProjector projector;
		private PamSymbolManager symbolManager;
		private SymbolUpdateMonitor updateMonitor;

		public SymbolModifierAction(PamSymbolManager symbolManager, String displayName, GeneralProjector projector, SymbolUpdateMonitor updateMonitor) {
			this.symbolManager = symbolManager;
			this.displayName = displayName;
			this.projector = projector;
			this.updateMonitor = updateMonitor;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingSymbolOptionsPanel optsPanel = symbolManager.getSwingOptionsPanel(displayName, projector);
			if (optsPanel == null) {
				return;
			}
			GenericSwingDialog.showDialog(PamController.getMainFrame(), displayName, optsPanel);
			if (updateMonitor != null) {
				updateMonitor.symbolUpdate();
			}
		}
		
	}
}
