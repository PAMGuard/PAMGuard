package rockBlock;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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

/**
 * @author mo55
 *
 */
public class RockBlockControl extends PamControlledUnit implements PamSettings {

	/** Unit type, for id purposes */
	public static final String unitType = "RockBlock";
	
	/** parameters object */
	private RockBlockParams rockBlockParams = new RockBlockParams();
	
	/** process object */
	private RockBlockProcess2 rockBlockProcess;
	
	/** side panel */
	private RockBlockSidePanel rbsp;
	

	/**
	 * @param defUnitType
	 * @param unitName
	 */
	public RockBlockControl(String unitName) {
        super(unitType, unitName);
        PamSettingManager.getInstance().registerSettings(this);
        addPamProcess(rockBlockProcess = new RockBlockProcess2(this));
        this.setSidePanel(rbsp = new RockBlockSidePanel(this));
	}
	
	/**
	 * Transmit a String object from the RockBlock to the Iridium Network
	 *  
	 * @param outgoingMessage
	 */
	public void sendText(String outgoingMessage) {
		rockBlockProcess.addOutgoingMessageToQueue(outgoingMessage);
	}
	
	/**
	 * Tell the RockBlock+ to check the signal strength
	 */
	public void checkSignalStrength() {
		rockBlockProcess.checkSignal();
	}
	
	/**
	 * Get the RockBlockStatus object
	 * @return
	 */
	public RockBlockStatus getRockBlockStatus() {
		return rockBlockProcess.getRbStatus();
	}
	


	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Parameters");
		menuItem.addActionListener(new SetParameters(parentFrame));
		return menuItem;
	}


	/** get reference to the parameters */
	public RockBlockParams getParams() {
		return rockBlockParams;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsReference()
	 */
	@Override
	public Serializable getSettingsReference() {
		return rockBlockParams;
	}


	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsVersion()
	 */
	@Override
	public long getSettingsVersion() {
		return RockBlockParams.getSerialVersionUID();
	}


	/* (non-Javadoc)
	 * @see PamController.PamSettings#restoreSettings(PamController.PamControlledUnitSettings)
	 */
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.rockBlockParams = ((RockBlockParams) pamControlledUnitSettings.getSettings()).clone();
 		return true;
	}
	
	/**
	 * 
	 * @author mo55
	 *
	 */
	class SetParameters implements ActionListener {

		Frame parentFrame;

		public SetParameters(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RockBlockParams newParams = RockBlockParamsDialog.showDialog(parentFrame, rockBlockParams);
			/*
			 * The dialog returns null if the cancel button was set. If it's
			 * not null, then clone the parameters onto the main parameters reference
			 * and call prepareProcess to make sure they get used !
			 */
			if (newParams != null) {
				rockBlockParams = newParams.clone();
				rockBlockProcess.prepareProcess();
				rockBlockProcess.resetRockBlock();
			}
		}
	}

	/**
	 * @return the rockBlockProcess
	 */
	public RockBlockProcess2 getRockBlockProcess() {
		return rockBlockProcess;
	}
	
	public void resetRockBlock() {
		rockBlockProcess.resetRockBlock();
	}




}
