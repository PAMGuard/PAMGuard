package alfa.clickmonitor.eventaggregator;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;;

public abstract class AggregateDataUnit<T extends PamDataUnit> extends SuperDetection<T> {

	public AggregateDataUnit(DataUnitBaseData basicData) {
		super(basicData);
	}


	/**
	 * @param timeMilliseconds
	 */
	public AggregateDataUnit(long timeMilliseconds, T firstSubDetection) {
		super(timeMilliseconds);
		if (firstSubDetection != null) {
			setChannelBitmap(firstSubDetection.getChannelBitmap());
			addSubDetection(firstSubDetection);
		}
	}

}
