package PamView.paneloverlay;

import java.awt.GridBagConstraints;
import java.awt.Window;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamGridBagContraints;
import PamView.paneloverlay.overlaymark.OverlayMarkDataInfo;
import PamguardMVC.PamDataBlock;

public class OverlayMarkSwingPanel extends OverlaySwingPanel {

	private JCheckBox[] partialBoxes;

	public OverlayMarkSwingPanel(OverlayDataManager overlayDataManager, Window parentWindow) {
		super(overlayDataManager, parentWindow);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlaySwingPanel#createCheckboxes()
	 */
	@Override
	protected void createCheckboxes() {
		super.createCheckboxes();

		// add additional controls for partial overlaps and set their values. 
		int firstCol = getFirstFreeColumn();
		List<PamDataBlock> dataList = getDataList();
		partialBoxes = new JCheckBox[dataList.size()];
		JPanel p = getSwingPanel();
		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		c.gridx = firstCol;
		p.add(new JLabel(" Partial ", JLabel.CENTER));
		for (int i = 0; i < dataList.size(); i++) {
			c.gridy++;
			partialBoxes[i] = new JCheckBox();
			p.add(partialBoxes[i], c);
			partialBoxes[i].setToolTipText("Allow partially overlapping data (e.g. bearing lines)");
		}
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlaySwingPanel#setParams()
	 */
	@Override
	public void setParams() {
		super.setParams();
		List<PamDataBlock> dataList = getDataList();
		OverlayDataManager overlayDataManager = getOverlayDataManager();
		for (int i = 0; i < dataList.size(); i++) {
			OverlayDataInfo dataInfo = overlayDataManager.getOverlayInfo(dataList.get(i));
			if (dataInfo != null) {
				if (OverlayMarkDataInfo.class.isAssignableFrom(dataInfo.getClass())) {
					OverlayMarkDataInfo omdi = (OverlayMarkDataInfo) dataInfo;
					partialBoxes[i].setSelected(omdi.acceptOverlapping);
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlaySwingPanel#getParams()
	 */
	@Override
	public boolean getParams() {
		if (!super.getParams()) {
			return false;
		};
		List<PamDataBlock> dataList = getDataList();
		OverlayDataManager overlayDataManager = getOverlayDataManager();
		for (int i = 0; i < dataList.size(); i++) {
			OverlayDataInfo dataInfo = overlayDataManager.getOverlayInfo(dataList.get(i));
			if (dataInfo != null) {
				if (OverlayMarkDataInfo.class.isAssignableFrom(dataInfo.getClass())) {
					OverlayMarkDataInfo omdi = (OverlayMarkDataInfo) dataInfo;
					omdi.acceptOverlapping = partialBoxes[i].isSelected();
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlaySwingPanel#enableControls()
	 */
	@Override
	public void enableControls() {
		super.enableControls();
		for (int i = 0; i < selCheckBoxes.length; i++) {
			partialBoxes[i].setEnabled(selCheckBoxes[i].isSelected());
			if (selCheckBoxes[i].isSelected() == false) {
				partialBoxes[i].setSelected(false);
			}
		}
	}

}
