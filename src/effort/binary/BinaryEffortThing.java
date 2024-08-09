package effort.binary;

import PamguardMVC.PamDataBlock;
import effort.EffortDataUnit;

public class BinaryEffortThing extends EffortDataUnit {

	
	private PamDataBlock parentDatablock;

	public BinaryEffortThing(PamDataBlock parentDatablock, long effortStart, long effortEnd) {
		super(null, effortStart, effortEnd);
		this.parentDatablock = parentDatablock;
	}

	@Override
	public String getEffortDescription() {
		String str = String.format("%s", parentDatablock.getDataName());
		return str;
	}
}
