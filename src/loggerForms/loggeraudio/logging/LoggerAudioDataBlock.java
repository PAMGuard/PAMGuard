package loggerForms.loggeraudio.logging;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class LoggerAudioDataBlock extends PamDataBlock<LoggerAudioDataUnit>{

	public LoggerAudioDataBlock(PamProcess parentProcess) {
		super(LoggerAudioDataUnit.class, "Logger Audio", parentProcess, 0);
		// TODO Auto-generated constructor stub
	}

}
