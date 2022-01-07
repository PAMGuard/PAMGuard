package dataMap;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;

/**
 * Layout the DataMap giving less space to the hidden components
 * @author Doug Gillespie
 *
 */
public class DataMapLayout implements LayoutManager {

	private ArrayList<DataStreamPanel> dataStreamPanels = new ArrayList<DataStreamPanel>();
	
	private SettingsStrip settingsStrip;
	
	private ScrollingDataPanel scrollingDataPanel;
	
	private static int minShownHeight = 32;
//	private static int preferredShownHeight = 32;
	
	public DataMapLayout(ScrollingDataPanel scrollingDataPanel) {
		super();
		this.scrollingDataPanel = scrollingDataPanel;
	}

	@Override
	public void addLayoutComponent(String arg0, Component component) {
		if (DataStreamPanel.class.isAssignableFrom(component.getClass())) {
			dataStreamPanels.add((DataStreamPanel) component);
		}
		else if (SettingsStrip.class.isAssignableFrom(component.getClass())) {
			settingsStrip = (SettingsStrip) component;
		}
	}

	public SettingsStrip getSettingsStrip() {
		return settingsStrip;
	}

	public void setSettingsStrip(SettingsStrip settingsStrip) {
		this.settingsStrip = settingsStrip;
	}

	@Override
	public void layoutContainer(Container arg0) {
		int shown = 0;
		int hidden = 0;
		int n = dataStreamPanels.size();
		DataStreamPanel dsp;
		int width = arg0.getWidth();
		int height = arg0.getHeight();
		height = Math.max(height,  getMinimumWindowHeight(arg0));
//		height = Math.max(height, 1800);
		int hiddenHeight = 40;
		int totalHiddenHeight = 0;
		int hiddenSpace, shownSpace;
		


		for (int i = 0; i < n; i++) {
			dsp = dataStreamPanels.get(i);
			if (dsp.isGraphVisible()) {
				shown++;
			}
			else {
				hidden++;
				hiddenHeight = dsp.getDataName().getMinimumSize().height;
				dsp.setSize(new Dimension(width, hiddenHeight));
				totalHiddenHeight += hiddenHeight;
			}
		}
//		System.out.println("Panels = " + n + ", shown " + shown + ", hidden " + hidden);
		int visibleSpace = height - totalHiddenHeight;
		if (settingsStrip != null) {
			visibleSpace -= settingsStrip.getStripHeight();
		}
		int y = 0;
		if (settingsStrip != null) {
			settingsStrip.setSize(new Dimension(width, settingsStrip.getStripHeight()));
			settingsStrip.setLocation(0, 0);
			y += settingsStrip.getStripHeight();
		}
		int componentHeight = visibleSpace;
		if (shown > 0) {
			componentHeight = visibleSpace / shown;
		}
		for (int i = 0; i < n; i++) {
			dsp = dataStreamPanels.get(i);
			if (dsp.isGraphVisible()) {
				dsp.setSize(new Dimension(width, componentHeight));
			}
			dsp.setLocation(0,y);
			y += dsp.getSize().height;
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container arg0) {
		return new Dimension(20,getMinimumWindowHeight(arg0));
	}
	
	private int getMinimumWindowHeight(Container container) {
		if (dataStreamPanels == null || dataStreamPanels.size() == 0) {
			return 0;
		}
		int[] winTypes = countWindowTypes();
		int titleHeight = dataStreamPanels.get(0).getDataName().getPreferredSize().height;
		int hiddenHeight =  titleHeight * winTypes[1];
		int shownHeight = winTypes[0] * (minShownHeight + 1 + titleHeight);
		int wantedHeight = hiddenHeight + shownHeight;
		if (settingsStrip != null) {
			wantedHeight += settingsStrip.getStripHeight();
		}
		
		return wantedHeight;
//		int height = scrollingDataPanel.getMainScrollPane().getHeight();
//		
//		if (hiddenHeight + shownHeight <= height) {
//			return hiddenHeight + shownHeight;
//		}
//		else {
//			return hiddenHeight + winTypes[0] * (preferredShownHeight+titleHeight);
//		}
	}
	
	/**
	 * Count the number of hidden and the number of visible windows. 
	 * @return number of visible and hidden windows. 
	 */
	private int[] countWindowTypes() {
		int[] winTypes = new int[2];		
		int n = dataStreamPanels.size();
		for (int i = 0; i < n; i++) {
			DataStreamPanel dsp = dataStreamPanels.get(i);
			if (dsp.isGraphVisible()) {
				winTypes[0] ++;
			}
			else {
				winTypes[1]++;
			}
		}
		return winTypes;
	}

	@Override
	public Dimension preferredLayoutSize(Container arg0) {
		return new Dimension(20,getMinimumWindowHeight(arg0));
	}

	@Override
	public void removeLayoutComponent(Component arg0) {
		dataStreamPanels.remove(arg0);
	}

}
