package PamView.panel;

import java.io.Serializable;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * Class that will remember and reset the position of a split pane. Any split pane. 
 * Just call this constructor with a unique name and the splitPane and a default 
 * between 0 and 1 and it will register itself automatically with PamSettings. 
 * @author dg50
 *
 */
public class SplitPanePositioner implements PamSettings {
	
	private static final String unitType = "Split Pane Position";
	
	private String unitName;
	
	private JSplitPane splitPane;

	/**
	 * Constructor for split pane positioner. Just call this constructor for each 
	 * split pane, then forget about it. This will have been registered with 
	 * PamSettings and will handle everything, restoring the split pane position
	 * when PAMGuard is restarted. 
	 * @param unitName A unique name for the split pane. 
	 * @param splitPane reference to an existing split pane. 
	 * @param proportionalDefault default position (0 <  position < 1). 
	 */
	public SplitPanePositioner(String unitName, JSplitPane splitPane, double proportionalDefault) {
		super();
		this.splitPane = splitPane;
		this.unitName = unitName;
		boolean exists = PamSettingManager.getInstance().registerSettings(this);
		if (exists == false) {
			// use the default
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					splitPane.setDividerLocation(proportionalDefault);
				}
			});
		}
	}

	@Override
	public String getUnitName() {
		return unitName;
	}

	@Override
	public String getUnitType() {
		return unitType;
	}

	@Override
	public Serializable getSettingsReference() {
//		System.out.printf("Save split position %s as %d out of %d\n", unitName, splitPane.getDividerLocation(), splitPane.getHeight());
		double propPosition = (double) splitPane.getDividerLocation() / (double) splitPane.getHeight();
		SplitPanePositionData posData = new SplitPanePositionData(splitPane.getDividerLocation(), propPosition, splitPane.getHeight());
		return posData;
	}

	@Override
	public long getSettingsVersion() {
		return SplitPanePositionData.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		
		SplitPanePositionData posData = (SplitPanePositionData) pamControlledUnitSettings.getSettings();
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				int newPos = posData.getPosition() + splitPane.getHeight() - posData.getHeight();
//				System.out.printf("Set split %s position to %d or %3.3f of %d\n", unitName, 
//						posData.getPosition(), posData.getPropPosition(), splitPane.getHeight());
				splitPane.setDividerLocation(posData.getPosition());
			}
		});
		return true;
	}
	
	

}
