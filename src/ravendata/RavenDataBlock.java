package ravendata;

import PamguardMVC.PamDataBlock;

public class RavenDataBlock extends PamDataBlock<RavenDataUnit> {

	private RavenProcess ravenProcess;

	public RavenDataBlock(RavenProcess parentProcess, int channelMap) {
		super(RavenDataUnit.class, "Selection", parentProcess, channelMap);
		this.ravenProcess = parentProcess;
	}


}
