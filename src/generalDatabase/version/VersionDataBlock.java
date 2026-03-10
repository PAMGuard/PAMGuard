package generalDatabase.version;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import generalDatabase.DBProcess;

public class VersionDataBlock extends PamDataBlock {

	public VersionDataBlock(DBProcess parentProcess) {
		super(VersionDataUnit.class, "Version Data", parentProcess, 0);
	}

}
