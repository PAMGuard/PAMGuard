/**
 * 
 */
/**
 * 
 */
package IshmaelLocator;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import IshmaelDetector.IshDetection;
import IshmaelDetector.IshLogger;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;

/** This implements the two locators from Ishmael.  One, namely
 * IshLocPairProcess, calculates a bearing from 2 phones.  The other
 * calculates a 2- or 3-D position from N phones using a 
 * least-squares minimization process.
 * 
 * @author Dave Mellinger and Hisham Qayum
 */
public class IshLocControl extends PamControlledUnit implements PamSettings {
	IshLocProcess ishLocProcessPr, ishLocProcessHy;
	IshLocParams ishLocParams;		//is actually a subclass of ishLocParams
	
	public IshLocControl(String unitName) {
		super("Ishmael Locator", unitName);
		this.ishLocParams = new IshLocHyperbParams();
		
		//Include/exclude localizers by enabling/commenting out lines here.
		//addPamProcess(ishLocProcessPr = new IshLocPairProcess(this));
		addPamProcess(ishLocProcessHy = new IshLocHyperbProcess(this));
		
		//(PamFFTProcess) fftControl.GetPamProcess(0)));
		PamSettingManager.getInstance().registerSettings(this);
		//newSettings();      Doug says leave this out
		
		//new IshLocGraphics(this);
				
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#SetupControlledUnit()
	 */
	@Override
	public void setupControlledUnit() {
		super.setupControlledUnit();
		//have it find it's own data block - for now, just take the first fft block
		//that can be found.
		
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem("Ishmael Location Settings ...");
		menuItem.addActionListener(new LocationActionListener(parentFrame));
		return menuItem;
	}

	/** This is called after a settings file is read.  The subclass should 
	 * get newParams and clone it as ishDetParams before calling here.
	 */
	public boolean restoreSettings(PamControlledUnitSettings settings) {
		IshLocParams newParams = (IshLocParams)settings.getSettings();
		//I can't figure out why inputDataSource is not in the newParams
		//returned by getSettings, but sometimes it's not. 
		if (newParams.inputDataSource == null)
			newParams.inputDataSource = ishLocParams.inputDataSource;
		ishLocParams = newParams.clone();
		
		if (ishLocProcessHy != null) ishLocProcessHy.setupConnections();
		if (ishLocProcessPr != null) ishLocProcessPr.setupConnections();
		return true;
	}
	
	private void newSettings() {
		//Here is where you set which data source it uses - currently 
		//set to 0. May crash if there are none at all !
		PamDataBlock pamDatablock = PamController.getInstance().getDetectorDataBlock(0);
		if (ishLocProcessPr != null) ishLocProcessPr.setParentDataBlock(pamDatablock);
		if (ishLocProcessHy != null) ishLocProcessHy.setParentDataBlock(pamDatablock);
	}
	
	class LocationActionListener implements ActionListener {
		Frame parentFrame;
		public LocationActionListener(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}
		public void actionPerformed(ActionEvent e) {
			PamDataBlock b = (ishLocProcessHy == null) ? null
					: ishLocProcessHy.outputDataBlock;
			IshLocParams p = IshLocHyperbParamsDialog.showDialog2(parentFrame, 
					(IshLocHyperbParams)ishLocParams, b);
			installNewParams(parentFrame, p);
		}
	}
	//public void showParamsDialog1(Frame parentFrame) {}

	protected void installNewParams(Frame parentFrame, IshLocParams newParams) {
		if (newParams != null) {
			ishLocParams = newParams.clone();      //makes a new IshLocParams
			if (ishLocProcessHy != null) {
				ishLocProcessHy.setupConnections();
				PamController.getInstance().notifyModelChanged(
						PamControllerInterface.CHANGED_PROCESS_SETTINGS);
			}
			if (ishLocProcessPr != null)
				ishLocProcessPr.setupConnections();
		}
	}
	
	public Serializable getSettingsReference() {
		return ishLocParams;
	}
	
	/**
	 * @return An integer version number for the settings
	 */
	public long getSettingsVersion() {
		return IshLocParams.serialVersionUID;
	}

	/**
	 * @return a PamProcess (hyperb or pair loc) for this control
	 */
	public IshLocProcess getProcess() 
	{
		return (ishLocProcessHy != null) ? ishLocProcessHy : ishLocProcessPr;
	}
}

