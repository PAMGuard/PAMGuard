package tethys.species;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class FixedSpeciesManager<T extends PamDataUnit> extends DataBlockSpeciesManager<T> {
	

	public FixedSpeciesManager(PamDataBlock dataBlock, int itisCode, String name, String callType) {
		super(dataBlock);
		setDefaultDefaultSpecies(new SpeciesMapItem(itisCode, name, callType));
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		return null;
	}

	@Override
	public String getSpeciesCode(T dataunit) {
		return getDefaultSpeciesCode();
	}
}
