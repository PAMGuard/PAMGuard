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



package beamformer.algorithms.basicFreqDomain;

import PamController.PamController;
import PamController.SettingsPane;
import PamguardMVC.AcousticDataBlock;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseControl;
import beamformer.BeamFormerBaseProcess;
import beamformer.BeamFormerParams;
import beamformer.algorithms.BeamAlgorithmProvider;
import beamformer.algorithms.BeamFormerAlgorithm;
import beamformer.algorithms.StaticAlgoProperties;
import fftManager.FFTDataBlock;


/**
 * @author mo55
 *
 */
public class BasicFreqDomBeamProvider implements BeamAlgorithmProvider {

	private StaticAlgoProperties staticProperties;
	
	/**
	 * The parameters to display in the dialog
	 */
	private BasicFreqDomParams curParams;
	
	private BasicFreqDomParamsPane2 settingsPane;
	
	private BeamFormerBaseControl beamFormerControl;
	
	/**
	 * Main Constructor
	 * 
	 * @param beamFormerControl
	 */
	public BasicFreqDomBeamProvider(BeamFormerBaseControl beamFormerControl) {
		super();
		staticProperties = new StaticAlgoProperties("Basic Frequency Domain Beamformer", "Basic BF", true);
		this.beamFormerControl = beamFormerControl;
	}

	@Override
	public StaticAlgoProperties getStaticProperties() {
		return staticProperties;
	}

	@Override
	/**
	 * create the beamformer algorithm
	 */
	public BeamFormerAlgorithm makeAlgorithm(BeamFormerBaseProcess beamFormerProcess, BeamAlgorithmParams parameters, int firstSeqNum, int beamogramNum) {
		
		// cast the parameters to the basic frequency domain parameters object
		BasicFreqDomParams basicFreqDomParams = (BasicFreqDomParams) parameters;
//		basicFreqDomParams.setNumBeamogram(1);
		
		// create the algorithm
		return new BasicFreqDomBeamFormer(this, beamFormerProcess, basicFreqDomParams, firstSeqNum, beamogramNum);
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#createNewParams(java.lang.String, int, int)
	 */
	@Override
	public BeamAlgorithmParams createNewParams(String algorithmName, int groupNumber, int channelMap) {
		return (new BasicFreqDomParams(algorithmName, groupNumber, channelMap));
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#getParamsDialog(beamformer.BeamAlgorithmParams)
	 */
	@Override
	public SettingsPane<?> getParamsDialog(BeamFormerParams overallParams, BeamAlgorithmParams algoParams) {
		curParams = (BasicFreqDomParams) algoParams;
		if (settingsPane==null){
			settingsPane=new BasicFreqDomParamsPane2(beamFormerControl.getGuiFrame(), beamFormerControl);
		}
		settingsPane.setDataSource((AcousticDataBlock) PamController.getInstance().getDataBlockByLongName(overallParams.getDataSource()));
		settingsPane.setParams(curParams);
		return settingsPane;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#getCurrentParams()
	 */
	@Override
	public BasicFreqDomParams getCurrentParams() {
		return (BasicFreqDomParams) settingsPane.getParams(curParams);
	}
}
