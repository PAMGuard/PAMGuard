package ravendata;

import PamDetection.AcousticSQLLogging;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class RavenLogging extends AcousticSQLLogging {

	private RavenDataBlock ravenDataBlock;
	private RavenControl ravenControl;
	
	private PamTableItem f1;

	public RavenLogging(RavenControl ravenControl, RavenDataBlock pamDataBlock) {
		super(pamDataBlock, ravenControl.getUnitName());
		this.ravenControl = ravenControl;
		this.ravenDataBlock = pamDataBlock;
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int chanMap, long duration,
			double[] f) {
		return new RavenDataUnit(timeMilliseconds, chanMap, duration, f[0], f[1]);
	}


}
