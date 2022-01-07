package Map;

import java.awt.Frame;
import java.awt.event.MouseAdapter;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import PamView.PamTabPanel;

public class MapTabPanelControl implements PamTabPanel  {

	SimpleMap simpleMap;
	
	MapController mapController;
	
	MapTabPanelControl(MapController mapController) {
		this.mapController = mapController;
		simpleMap = new SimpleMap(mapController, true);
	}
	
	@Override
	public JMenu createMenu(Frame parentFrame) {
		return null;
	}

	@Override
	public JComponent getPanel() {
		return simpleMap.getPanel();
	}

	@Override
	public JToolBar getToolBar() {
		return null;
	}

	public SimpleMap getSimpleMap() {
		return simpleMap;
	}

	/**
	 * @param mouseAdapter
	 */
	public void addMouseAdapterToMapPanel(MouseAdapter mouseAdapter) {
		simpleMap.addMouseAdapterToMapPanel(mouseAdapter);
	}

	public void mapCanScroll(boolean b) {	
		simpleMap.mapCanScroll(b);
	}

	/**
	 * 
	 */
	public void refreshDetectorList() {
		simpleMap.refreshDetectorList();
	}
	
}

