package loggerForms.loggeraudio;

import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;

/**
 * Had to override PamRawDataBlock to get access to removeOldUnitsT
 * <br>
 * One of these per platform, keeping data separate since they'll never
 * interleave properly - different app's are sending data in different block 
 * sizes and everything is asynchronous chaos !
 */
public class LoggerRawAudioDataBlock  extends PamRawDataBlock {

	public LoggerRawAudioDataBlock(String name, PamProcess parentProcess, int channelMap, float sampleRate) {
		super(name, parentProcess, channelMap, sampleRate);
	}
	
	public void clearold(long start) {
		super.removeOldUnitsT(start);
	}
}

