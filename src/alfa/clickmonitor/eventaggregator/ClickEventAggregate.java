package alfa.clickmonitor.eventaggregator;

import PamguardMVC.DataUnitBaseData;
import detectiongrouplocaliser.DetectionGroupDataUnit;

public class ClickEventAggregate extends AggregateDataUnit<DetectionGroupDataUnit> {

	public ClickEventAggregate(DataUnitBaseData basicData) {
		super(basicData);
	}

	public ClickEventAggregate(long timeMilliseconds, DetectionGroupDataUnit firstSubDetection) {
		super(timeMilliseconds, firstSubDetection);
	}

	public ClickEventAggregate(DetectionGroupDataUnit dataUnit) {
		this(dataUnit.getTimeMilliseconds(), dataUnit);
	}

}
