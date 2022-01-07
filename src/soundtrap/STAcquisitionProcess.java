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



package soundtrap;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;

/**
 * @author SCANS
 *
 */
public class STAcquisitionProcess extends AcquisitionProcess {

	private PamRawDataBlock stClickDataBlock;

	/**
	 * @param acquisitionControl
	 */
	public STAcquisitionProcess(AcquisitionControl acquisitionControl) {
		super(acquisitionControl);
		
		// remove the PamRawDataBlock that super() just created, and replace it with another referencing this process.  Don't add it to
		// the output data block list either, since we don't want the user to subscribe to it.
		this.removeOutputDatablock(rawDataBlock);
		String name = String.format("Soundtrap Click Only data from %s", acquisitionControl.getUnitName());
		addOutputDataBlock(rawDataBlock = new PamRawDataBlock(name, this,
				PamUtils.makeChannelMap(acquisitionControl.acquisitionParameters.nChannels,acquisitionControl.acquisitionParameters.getHardwareChannelList()),
				acquisitionControl.acquisitionParameters.sampleRate));
//		rawDataBlock = new PamRawDataBlock(name, this,
//				PamUtils.makeChannelMap(acquisitionControl.acquisitionParameters.nChannels,acquisitionControl.acquisitionParameters.getHardwareChannelList()),
//				acquisitionControl.acquisitionParameters.sampleRate);
		rawDataBlock.setParentProcess(this);
//		removeOutputDatablock(getDaqStatusDataBlock());

	}

	@Override
	/**
	 * Override this, because AcquisitionProcess.setupDataBlock sets the channel map and 
	 * sample rate to match AcquisitionControl.AcquisitionParameters, but those values
	 * refer to the wav files and not the click data
	 */
	public void setupDataBlock() {
	}

	@Override
	/**
	 * Override the AcquisitionProcess version of this, because it sets the 
	 * acquisitionParameters sample rate to the passed variable and we don't
	 * want to do that.  However, AcquisitionProcess DOES also call
	 * super.setSampleRate, and we DO want to do that.  So, since we can't call
	 * super.super.setSampleRate, we'll just copy the PamProcess.setSampleRate
	 * code here.  Yes, it's ugly.
	 */
	public void setSampleRate(float sampleRate, boolean notify) {
		// notify all output data blocks that there is a new sample rate
		this.sampleRate = sampleRate;
		if (notify && outputDataBlocks != null) {
			for (int i = 0; i < outputDataBlocks.size(); i++) {
				outputDataBlocks.get(i).setSampleRate(sampleRate, notify);
			}
		}
	}

	@Override
	/**
	 * Override the AcquisitionProcess version of this, because it sets the 
	 * acquisitionParameters nChannels and we don't want to do that.
	 */
	public void setNumChannels(int numChannels) {
		rawDataBlock.setChannelMap(PamUtils.makeChannelMap(numChannels));
	}

	@Override
	/**
	 * Override the AcquisitionProcess version of this, because it sets the 
	 * acquisitionParameters nChannels and channelList fields and we don't want to do that.
	 */
	public void setNumChannels(int numChannels, int[] channelList) {
		setNumChannels(numChannels);
	}
	
	@Override
	/**
	 * Override - nothing should happen here when Pamguard starts
	 */
	public void pamStart() {
	}

	@Override
	/**
	 * Override - nothing should happen here when Pamguard stops
	 */
	public void pamStop() {
	}

	@Override
	/**
	 * Override - nothing should happen here when Pamguard stops
	 */
	public void pamStop(String reason) {
	}

	@Override
	/**
	 * Override - nothing should happen here when Pamguard prepares to process
	 */
	public void prepareProcess() {
	}

	@Override
	/**
	 * Override - we're not getting offline data with this process
	 */
	public int getOfflineData(OfflineDataLoadInfo offlineLoadDataInfo) {
		return PamDataBlock.REQUEST_NO_DATA;
	}

	@Override
	public void acquisitionStopped() {
	}

	@Override
	public boolean isStalled() {
		return false;
	}
	
	
	
	


}
