/**
 * 
 */
package gpl;

import java.util.ArrayList;

import PamguardMVC.PamDataBlock;
import beamformer.continuous.BestBeamSelector;

/**
 * @author Doug Gillespie
 *
 */
public class GPLBeamSelector extends BestBeamSelector<GPLDetection> {

	private GPLDetectionBlock gplDetectionBlock;

	public GPLBeamSelector(PamDataBlock sourceDataBlock, GPLDetectionBlock gplDetectionBlock) {
		super(sourceDataBlock, gplDetectionBlock);
		this.gplDetectionBlock = gplDetectionBlock;
	}

	@Override
	public GPLDetection getBestDataUnit(ArrayList<GPLDetection> heldDataUnits) {
		GPLDetection bestDetection = null;
		double topScore = 0;
		for (GPLDetection gplDet:heldDataUnits) {
			if (gplDet.getPeakValue() > topScore) {
				topScore = gplDet.getPeakValue();
				bestDetection = gplDet;
			}
		}
		return bestDetection;
	}

}
