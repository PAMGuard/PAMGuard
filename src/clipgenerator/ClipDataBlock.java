package clipgenerator;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import PamguardMVC.datamenus.DataMenuParent;

public class ClipDataBlock extends ClipDisplayDataBlock<ClipDataUnit> {

	private ClipProcess clipProcess;

	public ClipDataBlock(String dataName,
			ClipProcess clipProcess, int channelMap) {
		super(ClipDataUnit.class, dataName, clipProcess, channelMap);
		this.clipProcess = clipProcess;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsT(long)
	 */
	@Override
	protected synchronized int removeOldUnitsT(long currentTimeMS) {
		// TODO Auto-generated method stub
		return super.removeOldUnitsT(currentTimeMS);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsS(long)
	 */
	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		// TODO Auto-generated method stub
		return super.removeOldUnitsS(mastrClockSample);
	}

	@Override
	public List<JMenuItem> getDataUnitMenuItems(DataMenuParent menuParent, Point mousePosition,
			PamDataUnit... dataUnits) {
		List<JMenuItem> standItems = super.getDataUnitMenuItems(menuParent, mousePosition, dataUnits);
		if (standItems == null) {
			standItems = new ArrayList<JMenuItem>();
		}
		for (int i = 0; i < Math.min(3,  dataUnits.length); i++) {
			if (dataUnits[i] instanceof ClipDataUnit) {
				ClipDataUnit clipDataUnit = (ClipDataUnit) dataUnits[i];
				JMenuItem menuItem = new JMenuItem("Play clip UID " + dataUnits[i].getUID());
				standItems.add(i, menuItem);
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						clipProcess.playClip(clipDataUnit);
					}
				});
			}
		}
		return standItems;
	}

	@Override
	public DataSelectorCreator getDataSelectCreator() {
		// TODO Auto-generated method stub
		return super.getDataSelectCreator();
	}

	public DataSelector getDataSelector(String selectorName, boolean allowScores, String selectorType) {
		DataSelector ds = super.getDataSelector(selectorName, allowScores, selectorType);
//		DataSelector newDS = super.getDataSelector("Randomuiname", allowScores, selectorType);
		return ds;
	}
}
