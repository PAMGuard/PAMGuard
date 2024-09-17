package soundtrap;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.dialog.warn.WarnOnce;

public class STToolsControl extends PamControlledUnit implements PamSettings {

	private STToolsParams stToolsParams = new STToolsParams();

	public static final String xmlFileEnd = ".log.xml";
	public static final String dwvFileEnd = ".dwv";
	public static final String bclFileEnd = ".bcl";
	
	private String warn = "<html>Versions 2.02.06 of PAMGuard and later can read SoundTrap SUD files directly, removing the need " +
	" to decompress them and then use this module to import the SoundTrap click detector data into PAMGuard.<p><br>" + 
			"Instead you should process the SUD files in 'normal mode' and include a SoundTrap Click Detector in "
			+ "your configuration. Click data will then be automatically unpacked into the correct binary files"
			+ " as you process the SUD file audio data.<p><br>"
			+ "You can remove this module which is now obsolete. See the online help for details.";
	
	public STToolsControl(String unitName) {
		super("Sound Trap Tools", unitName);
		PamSettingManager.getInstance().registerSettings(this);
		WarnOnce.showWarning("Importing SoundTrap data", warn, WarnOnce.WARNING_MESSAGE);
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu stMenu = new JMenu(getUnitName());
		JMenuItem mi = new JMenuItem("Import bcl and dwv data");
		stMenu.add(mi);
		mi.addActionListener(new ImportBCL(parentFrame));
		return stMenu;
	}

	private class ImportBCL implements ActionListener {
		private Frame parentFrame;

		public ImportBCL(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			importBCL(parentFrame);
		}
	}
	
	/**
	 * Import data from sound trap detector files. 
	 * @param parentFrame
	 */
	protected void importBCL(Frame parentFrame) {
		ImportBCLDialog.showDialog(parentFrame, this);
	}

	@Override
	public Serializable getSettingsReference() {
		return stToolsParams;
	}

	@Override
	public long getSettingsVersion() {
		return STToolsParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		stToolsParams = ((STToolsParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * @return the stToolsParams
	 */
	public STToolsParams getStToolsParams() {
		return stToolsParams;
	}

}
