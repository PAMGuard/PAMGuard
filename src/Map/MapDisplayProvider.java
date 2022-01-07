package Map;

import java.awt.Component;

import pamScrollSystem.PamScrollSlider;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class MapDisplayProvider implements UserDisplayProvider {

	private MapController mapControl;
	
	public MapDisplayProvider(MapController mapControl) {
		super();
		this.mapControl = mapControl;
	}

	@Override
	public String getName() {
		return mapControl.getUnitName();
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		SimpleMap simpleMap = new SimpleMap(mapControl, false);
		PamScrollSlider viewerScroller = simpleMap.getViewerScroller();
		if (viewerScroller != null) {
			viewerScroller.coupleScroller(userDisplayControl.getUnitName());
		}
		return simpleMap;
	}

	@Override
	public Class getComponentClass() {
		return SimpleMap.class;
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub

	}

}
