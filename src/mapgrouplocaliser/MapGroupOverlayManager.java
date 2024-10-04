/**
 * 
 */
package mapgrouplocaliser;

import java.awt.Window;

import Map.MapProjector;
import PamView.paneloverlay.OverlayDataManager;
import PamView.paneloverlay.OverlayMarkSwingPanel;
import PamView.paneloverlay.OverlaySwingPanel;
import PamView.paneloverlay.overlaymark.OverlayMarkDataInfo;
import PamguardMVC.PamDataBlock;

/**
 * @author dg50
 *
 */
public class MapGroupOverlayManager extends OverlayDataManager<OverlayMarkDataInfo> {

	private MapGroupLocaliserControl mapGroupLocaliserControl;

	/**
	 * @param generalProjector
	 */
	public MapGroupOverlayManager(MapGroupLocaliserControl mapGroupLocaliserControl) {
		super(MapProjector.parameterTypes, MapProjector.parameterUnits);
		this.mapGroupLocaliserControl = mapGroupLocaliserControl;
		
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlayDataObserver#selectionChanged(PamguardMVC.PamDataBlock, boolean)
	 */
	@Override
	public void selectionChanged(PamDataBlock dataBlock, boolean selected) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlayDataManager#getDataSelectorName()
	 */
	@Override
	public String getDataSelectorName() {
		return mapGroupLocaliserControl.getUnitName();
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlayDataManager#getOverlayInfo(java.lang.String)
	 */
	@Override
	public OverlayMarkDataInfo getOverlayInfo(PamDataBlock dataBlock) {
		return mapGroupLocaliserControl.getMapGrouperSettings().getOverlayInfo(dataBlock);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlayDataManager#getSwingPanel(java.awt.Window)
	 */
	@Override
	public OverlaySwingPanel getSwingPanel(Window parentWindow) {
		return new OverlayMarkSwingPanel(this, parentWindow);
	}

}
