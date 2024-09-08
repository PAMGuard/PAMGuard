package effort.swing;

import java.awt.Window;
import java.util.ArrayList;

import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import effort.EffortManager;

public class EffortSourcePanel extends SourcePanel {

	public EffortSourcePanel(Window ownerWindow) {
		super(ownerWindow, PamDataBlock.class, false, false);
	}

	public EffortSourcePanel(Window ownerWindow, String borderTitle) {
		super(ownerWindow, borderTitle, PamDataBlock.class, false, false);
	}

	@Override
	public ArrayList<PamDataBlock> getCompatibleDataBlocks() {
		return EffortManager.getEffortManager().getEffortDataBlocks();
	}




}
