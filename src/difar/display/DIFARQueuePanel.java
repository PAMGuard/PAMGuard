package difar.display;

import generalDatabase.lookupTables.LookupEditDialog;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import PamController.PamController;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.panel.PamPanel;
import PamView.symbol.PamSymbolManager;
import PamView.symbol.StandardSymbolManager;
import clipgenerator.ClipDisplayDataBlock;
import clipgenerator.ClipDataUnit;
import clipgenerator.clipDisplay.ClipDisplayDecorations;
import clipgenerator.clipDisplay.ClipDisplayPanel;
import clipgenerator.clipDisplay.ClipDisplayParameters;
import clipgenerator.clipDisplay.ClipDisplayParent;
import clipgenerator.clipDisplay.ClipDisplayUnit;
import difar.DIFARMessage;
import difar.DifarControl;
import difar.DifarDataUnit;

public class DIFARQueuePanel implements DIFARDisplayUnit, ClipDisplayParent {

	private DifarControl difarControl;

	private String queueName = "DIFAR Queue";

	private JPanel mainPanel;

	private ClipDisplayPanel clipDisplayPanel;

	public DIFARQueuePanel(DifarControl difarControl, String queueName) {
		super();
		this.difarControl = difarControl;
		this.queueName = queueName;

		mainPanel = new JPanel(new BorderLayout());

		clipDisplayPanel = new ClipDisplayPanel(this);
		
		makeSymbolModifier();
		
		mainPanel.add(BorderLayout.CENTER, clipDisplayPanel.getComponent());
	}

	/**
	 * Function to sort out symbol modifier, which will colour the background of the clip
	 * instead of what was previously a call to displayDecorations.getClipBackground()
	 * from the ClipDisplayUnit. 
	 */
	private void makeSymbolModifier() {
		ClipDisplayDataBlock dataBlock = getClipDataBlock();
		PamSymbolManager symbolManager = dataBlock.getPamSymbolManager();
		if (symbolManager == null || symbolManager instanceof StandardSymbolManager == false) {
			return;
		}
//		symbolManager.
	}

	/* (non-Javadoc)
	 * @see clipgenerator.clipDisplay.ClipDisplayParent#getClipDecorations(clipgenerator.clipDisplay.ClipDisplayUnit)
	 */
	@Override
	public ClipDisplayDecorations getClipDecorations(
			ClipDisplayUnit clipDisplayUnit) {
		return new DifarClipDecorations(difarControl, clipDisplayUnit);
	}

	@Override
	public String getName() {
		return queueName ;
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}

	@Override
	public int difarNotification(DIFARMessage difarMessage) {
		boolean isViewer = difarControl.isViewer();
		switch(difarMessage.message) {
		case DIFARMessage.DeleteFromQueue:
			if (!isViewer) {
				clipDisplayPanel.removeClip(difarMessage.difarDataUnit);
				clipDisplayPanel.updatePanel();

			}
			break;
		case DIFARMessage.ProcessFromQueue:
			if (!isViewer) {
				clipDisplayPanel.removeClip(difarMessage.difarDataUnit);
				clipDisplayPanel.updatePanel();
			}
			break;
		}
		// now loop over the clip list and send to their decoratoins. 
		JPanel unitsPanel = getClipDisplayPanel().getUnitsPanel();
		synchronized (unitsPanel.getTreeLock()) {
			int compCount = unitsPanel.getComponentCount();
			ClipDisplayUnit clipDisplayUnit;
			for (int i = compCount-1; i >= 0; i--) {
				clipDisplayUnit = (ClipDisplayUnit) unitsPanel.getComponent(i);
				DifarClipDecorations decoration = (DifarClipDecorations) clipDisplayUnit.getDisplayDecorations();
				if (decoration != null) {
					decoration.enableEnablersAndSelecters();
				}
			}
		}
		return 0;
	}

	@Override
	public ClipDisplayDataBlock getClipDataBlock() {
		/*
		 * for viewer mode there will never be anything in the queue, so display 
		 * the processed data instead. 
		 */
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			return difarControl.getDifarProcess().getProcessedDifarData();
		}
		else {
			return difarControl.getDifarProcess().getQueuedDifarData();
		}
	}	
	
	/**
	 * @return the clipDisplayPanel
	 */
	public ClipDisplayPanel getClipDisplayPanel() {
		return clipDisplayPanel;
	}

	@Override
	public String getDisplayName() {
		return difarControl.getUnitName();
	}

	public void	clearQueuePanel() {
		clipDisplayPanel.removeAllClips();
		boolean shouldClear = difarControl.getDifarParameters().clearQueueAtStart;
		difarControl.getDifarParameters().clearQueueAtStart = true;
		difarControl.getDifarProcess().getQueuedDifarData().clearAll();
		difarControl.getDifarParameters().clearQueueAtStart = shouldClear;
	}


	@Override
	public void displaySettingChange() {
		difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.DisplaySettingsChange, null));
	}

}
