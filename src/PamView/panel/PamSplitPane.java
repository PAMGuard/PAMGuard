package PamView.panel;

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * Implementation of the split pane that can remember it's position. 
 * @author dg50
 *
 */
public class PamSplitPane extends JSplitPane implements PamSettings {

	private static final long serialVersionUID = 1L;
	private String settingsName;
	private PamSplitPaneParams pamSplitPaneParams = new PamSplitPaneParams();

	/**
	 * @param newOrientation
	 * @param newLeftComponent
	 * @param newRightComponent
	 * @param settingsName Name to be used in PamSettings for resetting position. 
	 */
	public PamSplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent, String settingsName) {
		super(newOrientation, newLeftComponent, newRightComponent);
		this.settingsName = settingsName;
		if (settingsName != null) {
			PamSettingManager.getInstance().registerSettings(this);
		}
	}

	@Override
	public String getUnitName() {
		return settingsName;
	}

	@Override
	public String getUnitType() {
		return "Split Pane Position";
	}

	@Override
	public Serializable getSettingsReference() {
		pamSplitPaneParams.dividerLocation = this.getDividerLocation();
		return pamSplitPaneParams;
	}

	@Override
	public long getSettingsVersion() {
		return PamSplitPaneParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		pamSplitPaneParams = ((PamSplitPaneParams) pamControlledUnitSettings.getSettings()).clone();
		/**
		 * for some stupid reason if you set the location of the divider here, the component has zero height, so
		 * it can't do anything. Seems OK if we queue the command on the swing thread though ...
		 */
		setLocationLater();
		return true;
	}
	
	private void setLocationLater() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setLocation();
			}
		});
	}
	
	private void setLocation() {
		if (pamSplitPaneParams.dividerLocation != null) {
			setDividerLocation(pamSplitPaneParams.dividerLocation);
		}
	}


}
