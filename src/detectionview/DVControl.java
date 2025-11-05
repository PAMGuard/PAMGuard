package detectionview;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import clipgenerator.ClipDataUnit;
import clipgenerator.ClipDisplayDataBlock;
import clipgenerator.clipDisplay.ClipDisplayDecorations;
import clipgenerator.clipDisplay.ClipDisplayParent;
import clipgenerator.clipDisplay.ClipDisplayProvider;
import clipgenerator.clipDisplay.ClipDisplayUnit;
import detectionview.swing.DVDialog;
import rawDeepLearningClassifier.swing.DLClipDisplayProvider;
import userDisplay.UserDisplayControl;

/**
 * Detection viewer. Looks a lot like the clip display, but only generates the clips
 * offline from raw data, mostly just in viewer mode. 
 * @author dg50
 *
 */
public class DVControl extends PamControlledUnit implements PamSettings, ClipDisplayParent {

	public static String unitType = "Detections View";
	public static String unitTip = "Creates a display of clips of audio data associated with each detection";
	
	private DVProcess dvProcess;
	
	private DVParameters dvParameters = new DVParameters();

	public DVControl(String unitName) {
		super(unitType, unitName);
		dvProcess = new DVProcess(this);
		addPamProcess(dvProcess);
		
		PamSettingManager.getInstance().registerSettings(this);

		UserDisplayControl.addUserDisplayProvider(new ClipDisplayProvider(this, getUnitName() + " display"));
	}

	@Override
	public Serializable getSettingsReference() {
		return dvParameters;
	}

	@Override
	public long getSettingsVersion() {
		return DVParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		dvParameters = (DVParameters) pamControlledUnitSettings.getSettings();
		return true;
	}

	@Override
	public ClipDisplayDataBlock<ClipDataUnit> getClipDataBlock() {
		return (ClipDisplayDataBlock) dvProcess.getDvDataBlock();
	}

	@Override
	public String getDisplayName() {
		return getUnitName();
	}

	@Override
	public ClipDisplayDecorations getClipDecorations(ClipDisplayUnit clipDisplayUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void displaySettingChange() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the dvParameters
	 */
	public DVParameters getDvParameters() {
		return dvParameters;
	}

	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame);
			}
		});
		return menuItem;
	}

	protected void showSettingsDialog(Frame parentFrame) {
		DVParameters newSettings = DVDialog.showDialog(this);
		if (newSettings != null) {
			dvParameters = newSettings;
			dvProcess.setupProcess();
			
		}
	}


}
