package clipgenerator;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * This is just a dummy datablock to fool the Clips dialog into showing
 * options for the clips from spectrogram marks. The data units themselves
 * are never used for anything, but will exist very temporarily so that
 * clips can be generated. The block is not registered in the ClipProcess 
 * so it will be invisible to the rest of PAMGuard. 
 * @author Doug Gillespie
 *
 */
public class ClipSpectrogramMarkDataBlock extends PamDataBlock {

	public ClipSpectrogramMarkDataBlock(PamProcess parentProcess, int channelMap) {
		super(ClipSpectrogramMark.class, parentProcess.getPamControlledUnit().getUnitName() + " Spectrogram Marks", 
				parentProcess, channelMap);
		setCanClipGenerate(true);
	}


}
