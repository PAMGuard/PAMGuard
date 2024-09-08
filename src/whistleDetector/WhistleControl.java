/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package whistleDetector;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamModel.DeprecatedModuleDialog;

/**
 * @author Doug Gillespie
 *         <p>
 *         Implementation of PamControlledUnit for controlling the whistle
 *         detector
 *         <p>
 *         This currently does not have a display since whistle information can
 *         be easily displayed on the standard spectrogram displays. It does
 *         provide a mnu though (which doesn't do anything yet)
 * 
 */
public class WhistleControl extends PamControlledUnit implements PamSettings {

	protected WhistleDetector whistleDetector;
	
	protected WhistleControl THIS;
	
	protected WhistleEventDetector eventDetector;
	
	protected WhistleSidePanel whistleSidePanel;
	
	protected WhistleLocaliser whistleLocaliser;
	
	protected WhistleParameters whistleParameters = new WhistleParameters();

	protected ArrayList<PeakDetectorProvider> peakDetectorProviders;

	public WhistleControl(String name) {
		
		super("Whistle Detector", name);
		
		THIS = this;
		
		PamControlledUnit fftControl = null;
//		
//		DataType type = InputControl.GetPamProcess(0).GetOutputDataBlock(0).GetDataType();
//		if (type == DataType.FFT) {
//			fftControl = InputControl;
//		}
		peakDetectorProviders = new ArrayList<PeakDetectorProvider>();
		peakDetectorProviders.add(new BetterPDProvider());
		peakDetectorProviders.add(new BasicPDProvider());
		
		PamSettingManager.getInstance().registerSettings(this);
		
		if (!whistleParameters.ackOutOfDate) {
			boolean ans = DeprecatedModuleDialog.showDialog(null, 
					"Whistle Detector", "Whistle and Moan Detector", 
			"detectors.whistleMoanHelp.docs.whistleMoan_Overview");
			whistleParameters.ackOutOfDate = ans;
		}
		
		addPamProcess(whistleDetector = new WhistleDetector(this));
		
		addPamProcess(eventDetector = new WhistleEventDetector(this, whistleDetector));
		
		setSidePanel(whistleSidePanel = new WhistleSidePanel(this));
		
		whistleLocaliser = new WhistleLocaliser(this);
		
//		if (whistleParameters.fftDataSource == 0) {
//			
//		}
//		whistleDetector.prepareProcess();
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem;

		menuItem = new JMenuItem("Whistle Settings ...");
		menuItem.addActionListener(new menuDetection(parentFrame));

		return menuItem;
	}

	class menuDetection implements ActionListener {
		Frame parentFrame;
		
		public menuDetection(Frame parentFrame) {
			super();
			// TODO Auto-generated constructor stub
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			WhistleParameters newParams = WhistleParametersDialog.showDialog(parentFrame, THIS, whistleParameters, whistleDetector.getSampleRate());
			if (newParams != null) {
				whistleParameters = newParams.clone();
				whistleDetector.prepareProcess();
				PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
			}
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return whistleParameters;
	}

	@Override
	public long getSettingsVersion() {
		return WhistleParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		whistleParameters = ((WhistleParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			whistleDetector.prepareProcess();
		}
	}

	public WhistleParameters getWhistleParameters() {
		return whistleParameters;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#SetupControlledUnit()
	 */
//	@Override
//	public void setupControlledUnit() {
//		super.setupControlledUnit();
//		whistleDetector.prepareProcess();
//	}

//	@Override
//	public PamSidePanel getSidePanel() {
//		return 
//	}
	

}
