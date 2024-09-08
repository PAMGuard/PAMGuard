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
package KernelSmoothing;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;

public class KernelSmoothingControl extends PamControlledUnit implements PamSettings {

	//PamFFTControl fftControl;
	KernelSmoothingProcess smoothingProcess;
	
	KernelSmoothingParameters smoothingParameters = new KernelSmoothingParameters();
	
	public KernelSmoothingControl(String unitName) {
		
		super("Kernel Smoother", unitName);
		
		addPamProcess(smoothingProcess = new KernelSmoothingProcess(this));// (PamFFTProcess) fftControl.GetPamProcess(0)));
		
		PamSettingManager.getInstance().registerSettings(this);
		
		setOutputDataName();
		
		smoothingProcess.setupProcess();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#SetupControlledUnit()
	 */
	@Override
	public void setupControlledUnit() {
		super.setupControlledUnit();
//		have it find it's own data block - for now, just take the first fft block that can be found.
		
	}
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem;

		menuItem = new JMenuItem("Kernel Smoothing Settings ...");
		menuItem.addActionListener(new MenuSmoothingDetection(parentFrame));

		return menuItem;
	}
	
	private void newSettings() {
		PamDataBlock pamDatablock = PamController.getInstance().getFFTDataBlock(smoothingParameters.fftBlockIndex);
		smoothingProcess.setupProcess();
		smoothingProcess.setParentDataBlock(pamDatablock);
	}
	
	class MenuSmoothingDetection implements ActionListener {
		
		Frame parentFrame;
		
		public MenuSmoothingDetection(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			KernelSmoothingParameters newParams = KernelSmoothingDialog.showDialog(parentFrame, smoothingParameters, 
					smoothingProcess.getOutputDataBlock(0));
			if (newParams != null) {
				smoothingParameters = newParams.clone();
				newSettings();
				PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
			}
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return smoothingParameters;
	}

	@Override
	public long getSettingsVersion() {
		return KernelSmoothingParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		smoothingParameters = ((KernelSmoothingParameters) pamControlledUnitSettings.getSettings()).clone();
		newSettings();
		return true;
	}

	private void setOutputDataName() {
		smoothingProcess.outputData.setDataName(getUnitName());
	}

	@Override
	public void rename(String newName) {

		super.rename(newName);

		setOutputDataName();
	}

}
