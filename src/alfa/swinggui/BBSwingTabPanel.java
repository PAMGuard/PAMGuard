package alfa.swinggui;

import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import Map.MapController;
import Map.SimpleMap;
import PamView.PamTabPanel;
import alfa.ALFAControl;
import alfa.help.gui.ALFAHelpPanel;
import alfa.status.swing.StatusPanel;

public class BBSwingTabPanel implements PamTabPanel {

	private ALFAControl alfaControl;
	
//	private PamPanel mainPanel;
	
	private StatusPanel statusPanel;
	
//	private PamPanel mapSpace;

//	private SimpleMap simpleMap;
	
	private ALFALayout alfaLayout;

	public BBSwingTabPanel(ALFAControl alfaControl) {
		this.alfaControl = alfaControl;
		alfaLayout = new ALFALayout1();
		statusPanel = new StatusPanel(alfaControl.getStatusMonitor());
		alfaLayout.setWestStatusComponent(statusPanel.getPanel());
		ALFAOptionsPanel alfaOptionsPanel = new ALFAOptionsPanel(alfaControl);
		alfaLayout.setOptionsComponent(alfaOptionsPanel.getComponent());
		alfaLayout.setHelpcomponent(new ALFAHelpPanel(alfaControl).getComponent());
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getPanel() {
		return alfaLayout.getComponent();
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Called when units are added or removed. 
	 */
	public void updateUnits() {
//		if (mapSpace.getComponentCount() == 0) {
//			MapController mapCon = bigBrotherControl.findMapController();
//			if (mapCon != null) {
//				simpleMap = new SimpleMap(mapCon, false);
//				mapSpace.setLayout(new BorderLayout());
//				mapSpace.add(simpleMap, BorderLayout.CENTER);
//			}
////			mapCon.

//		MapController mapCon = alfaControl.findMapController();
//		if (mapCon != null) {
//			simpleMap = new SimpleMap(mapCon, false);
//			alfaLayout.setMapComponent(simpleMap);
//		}
//		alfaLayout.setSpermSummaryComponents(bigBrotherControl.getClickMonitorProcess().getSwingComponent());
		IntervalTablePanel intervalTablePanel = new IntervalTablePanel(alfaControl,alfaControl.getEffortMonitor().getIntervalDataBlock());
		alfaLayout.setSpermSummaryComponents(intervalTablePanel.getComponent());
		
		JComponent commsPanel = alfaControl.getMessageProcess().getSwingCommsPanel(SwingMessagePanel.SHOW_OUTGOING);
		alfaLayout.setCommsComponent(commsPanel);
	}

}
