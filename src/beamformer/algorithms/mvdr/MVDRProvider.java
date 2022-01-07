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



package beamformer.algorithms.mvdr;

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
 * MVDR (minimum variance distortionless response) beamformer provider
 * 
 * @author mo55
 *
 */
public class MVDRProvider implements BeamAlgorithmProvider {

	private StaticAlgoProperties staticProperties;
	
	/**
	 * The parameters to display in the dialog
	 */
	private MVDRParams curParams;
	
	private MVDRParamsPane2 settingsPane;
	
	private BeamFormerBaseControl beamFormerControl;
	
	/**
	 * 
	 */
	public MVDRProvider(BeamFormerBaseControl beamFormerBaseControl) {
		super();
		staticProperties = new StaticAlgoProperties("MVDR Beamformer", "MVDR BF", true);
		this.beamFormerControl = beamFormerBaseControl;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#getStaticProperties()
	 */
	@Override
	public StaticAlgoProperties getStaticProperties() {
		return staticProperties;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#makeAlgorithm(beamformer.BeamFormerProcess, beamformer.BeamAlgorithmParams, int, int)
	 */
	@Override
	public BeamFormerAlgorithm makeAlgorithm(BeamFormerBaseProcess beamFormerProcess, BeamAlgorithmParams parameters,
			int firstSeqNum, int beamogramNum) {
		
		// cast the parameters to the basic frequency domain parameters object
		MVDRParams mvdrParams = (MVDRParams) parameters;
		mvdrParams.setNumBeamogram(1);
		
		// create the algorithm
		return new MVDRalgorithm(this, beamFormerProcess, mvdrParams, firstSeqNum, beamogramNum);
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#createNewParams(java.lang.String, int, int)
	 */
	@Override
	public BeamAlgorithmParams createNewParams(String algorithmName, int groupNumber, int channelMap) {
		return (new MVDRParams(algorithmName, groupNumber, channelMap));
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#getParamsDialog(beamformer.BeamAlgorithmParams)
	 */
	@Override
	public SettingsPane<?> getParamsDialog(BeamFormerParams overallParams, BeamAlgorithmParams params) {
		curParams = (MVDRParams) params;
		if (settingsPane==null){
			settingsPane=new MVDRParamsPane2(beamFormerControl.getGuiFrame(), beamFormerControl);
		}
		settingsPane.setDataSource((AcousticDataBlock) PamController.getInstance().getDataBlockByLongName(overallParams.getDataSource()));
		settingsPane.setParams(curParams);
		return settingsPane;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamAlgorithmProvider#getCurrentParams()
	 */
	@Override
	public BeamAlgorithmParams getCurrentParams() {
		return settingsPane.getParams(curParams);
	}

}
