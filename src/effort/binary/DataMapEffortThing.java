package effort.binary;

import PamguardMVC.PamDataBlock;
import effort.EffortDataUnit;
import effort.EffortProvider;

public class DataMapEffortThing extends EffortDataUnit {

	
	private PamDataBlock parentDatablock;

	public DataMapEffortThing(EffortProvider effortProvider, PamDataBlock parentDatablock, long effortStart, long effortEnd) {
		super(effortProvider, null, effortStart, effortEnd);
		this.parentDatablock = parentDatablock;
	}

	@Override
	public String getEffortDescription() {
		String str = String.format("%s", parentDatablock.getDataName());
		return str;
	}
}
