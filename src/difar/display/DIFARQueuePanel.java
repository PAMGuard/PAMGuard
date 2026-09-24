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

	private final ClipDisplayDataBlock clipDataBlock;

	private final boolean saved;

	/**
	 * A strip of DIFAR clips.
	 * @param difarControl the DIFAR module.
	 * @param queueName name of the strip.
	 * @param clipDataBlock the clips to show.
	 * @param saved true if the clips are saved ones, which can be looked at
	 * again but not saved a second time; false for clips waiting to be worked.
	 */
	public DIFARQueuePanel(DifarControl difarControl, String queueName,
			ClipDisplayDataBlock clipDataBlock, boolean saved) {
		super();
		this.difarControl = difarControl;
		this.queueName = queueName;
		this.clipDataBlock = clipDataBlock;
		this.saved = saved;

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
		return new DifarClipDecorations(difarControl, clipDisplayUnit, saved);
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
		switch(difarMessage.message) {
		case DIFARMessage.NewDifarUnit:
		case DIFARMessage.SaveDatagramUnit:
		case DIFARMessage.SaveDatagramUnitWithoutRange:
			// the clip strip only lays itself out again for new clips in normal
			// mode, so in the viewer a new or newly saved clip is there but not
			// shown until something else redraws the strip. Later, so the clip
			// has joined the strip before it is laid out.
			clipDisplayPanel.updatePanelLater();
			break;
		case DIFARMessage.DeleteFromQueue:
		case DIFARMessage.ProcessFromQueue:
			// a clip leaves the queue strip once it is taken to be worked or
			// deleted. Saved clips stay where they are.
			if (!saved) {
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
		return clipDataBlock;
	}

	/**
	 * @return true if this strip shows saved clips.
	 */
	public boolean isSaved() {
		return saved;
	}	
	
	/**
	 * @return the clipDisplayPanel
	 */
	public ClipDisplayPanel getClipDisplayPanel() {
		return clipDisplayPanel;
	}

	@Override
	public String getDisplayName() {
		// the queue keeps the module's name, so its existing display settings
		// are kept; the saved strip needs a name of its own for its settings.
		return saved ? difarControl.getUnitName() + " saved clips" : difarControl.getUnitName();
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
