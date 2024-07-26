package tethys.localization;

import PamguardMVC.PamDataBlock;
import nilus.Localize;
import tethys.niluswraps.NilusDataWrapper;
import tethys.niluswraps.PDeployment;

public class PLocalization extends NilusDataWrapper<Localize> {

	public PLocalization(Localize nilusObject, PamDataBlock dataBlock, PDeployment deployment, Integer count) {
		super(nilusObject, dataBlock, deployment, count);
	}

}
