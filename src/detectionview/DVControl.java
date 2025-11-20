package detectionview;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import annotation.handler.AnnotationHandler;
import clipgenerator.ClipDataUnit;
import clipgenerator.ClipDisplayDataBlock;
import clipgenerator.clipDisplay.ClipDisplayDecorations;
import clipgenerator.clipDisplay.ClipDisplayParent;
import clipgenerator.clipDisplay.ClipDisplayProvider;
import clipgenerator.clipDisplay.ClipDisplayUnit;
import detectionview.swing.DVClipDecorations;
import detectionview.swing.DVClipDisplayProvider;
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
	
	private ArrayList<DVObserver> dvObservers = new ArrayList();

	public DVControl(String unitName) {
		super(unitType, unitName);
		dvProcess = new DVProcess(this);
		addPamProcess(dvProcess);
		
		PamSettingManager.getInstance().registerSettings(this);

		UserDisplayControl.addUserDisplayProvider(new DVClipDisplayProvider(this, getUnitName() + " display"));
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
		return new DVClipDecorations(this, clipDisplayUnit);
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
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(this.getUnitName() + " settings ...");
		JMenuItem menuItem = new JMenuItem("Configuration ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame);
			}
		});
		menu.add(menuItem);
		// see if we've got annotation types to add to the menu.
		PamDataBlock detBlock = dvProcess.getDetectorDataBlock();
		if (detBlock != null) {
			AnnotationHandler annotationHandler = detBlock.getAnnotationHandler();
			if (annotationHandler != null) {
//				menu.add(annotationHandler.)
			}
		}
		return menu;
	}

	protected void showSettingsDialog(Frame parentFrame) {
		DVParameters newSettings = DVDialog.showDialog(this);
		if (newSettings != null) {
			dvParameters = newSettings;
			dvProcess.setupProcess();
			
		}
	}

	/**
	 * Add an observer for data and configuration change notifications
	 * @param dvObserver
	 */
	public void addObserver(DVObserver dvObserver) {
		dvObservers.add(dvObserver);
	}
	
	/**
	 * Remove an observer for data and configuration change notifications
	 * @param dvObserver
	 */
	public boolean removeObserver(DVObserver dvObserver) {
		return dvObservers.remove(dvObserver);
	}
	
	/**
	 * Notify observers that there has been a data update. 
	 * @param updateType
	 */
	public void updateDataObs(int updateType) {
		for (DVObserver obs : dvObservers) {
			obs.updateData(updateType);
		}
	}

	/**
	 * Notify observers that there has been a configuration update
	 * @param updateType
	 */
	public void updateConfigObs() {
		for (DVObserver obs : dvObservers) {
			obs.updateConfig();
		}
	}
	
	public void updateLoadProgressObs(LoadProgress loadProgress) {
		for (DVObserver obs : dvObservers) {
			obs.loadProgress(loadProgress);
		}
	}

	/**
	 * @return the dvProcess
	 */
	public DVProcess getDvProcess() {
		return dvProcess;
	}


}
