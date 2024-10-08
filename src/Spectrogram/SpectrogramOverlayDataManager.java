package Spectrogram;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.paneloverlay.OverlayDataInfo;
import PamView.paneloverlay.OverlayDataManager;
import PamguardMVC.PamDataBlock;
import Spectrogram.SpectrogramDisplay.SpectrogramPanel;

public class SpectrogramOverlayDataManager extends OverlayDataManager {

	private SpectrogramDisplay spectrogramDisplay;
	
	private SpectrogramPanel spectrogramPanel;
	
	private static final ParameterType[] axisTypes = {ParameterType.TIME, ParameterType.FREQUENCY};
	private static final ParameterUnits[] axisUnits = {ParameterUnits.SECONDS, ParameterUnits.HZ};

	public SpectrogramOverlayDataManager(SpectrogramDisplay spectrogramDisplay, SpectrogramPanel spectrogramPanel) {
		super(axisTypes, axisUnits);
		this.spectrogramDisplay = spectrogramDisplay;
		this.spectrogramPanel = spectrogramPanel;
	}

	@Override
	public void selectionChanged(PamDataBlock dataBlock, boolean selected) {
		if (spectrogramDisplay.getSpectrogramParameters().applySelectAll) {
			int nPanels = spectrogramDisplay.getSpectrogramParameters().nPanels;
			for (int i = 0; i < nPanels; i++) {
				spectrogramDisplay.getOverlayDataInfo(dataBlock, i).select = selected;
			}
		}
		else {
			spectrogramDisplay.getOverlayDataInfo(dataBlock, spectrogramPanel.panelId).select = selected;
		}
		spectrogramPanel.subscribeDataBlocks();
		spectrogramPanel.repaint();
	}

	@Override
	public String getDataSelectorName() {
		return spectrogramDisplay.getDataSelectorName(spectrogramPanel.panelId);
	}

	@Override
	public OverlayDataInfo getOverlayInfo(PamDataBlock dataBlock) {
		return spectrogramDisplay.getOverlayDataInfo(dataBlock, spectrogramPanel.panelId);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlayDataManager#getProjector()
	 */
	@Override
	protected GeneralProjector getProjector() {
		return spectrogramPanel.getProjector();
	}

	@Override
	public int addSelectionMenuItems(JComponent menu, Window awtWindow, boolean sortAlphabetical, boolean allowScores,
			boolean includeSymbolManagement) {
		int n = super.addSelectionMenuItems(menu, awtWindow, sortAlphabetical, allowScores, includeSymbolManagement);
		if (n > 0) {
			// add an apply to all channels option.
			JCheckBoxMenuItem cbi = new JCheckBoxMenuItem("Apply to all channels");
			cbi.setToolTipText("Selections will apply to all channels in this spectrogram display");
			cbi.setSelected(spectrogramDisplay.getSpectrogramParameters().applySelectAll);
			if (menu instanceof JMenu) {
			 ((JMenu) menu).addSeparator();
			}
			if (menu instanceof JPopupMenu) {
				 ((JPopupMenu) menu).addSeparator();
			}
			menu.add(cbi);
			cbi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					spectrogramDisplay.getSpectrogramParameters().applySelectAll = cbi.isSelected();
				}
			});
		}
		
		return n;
	}
}
