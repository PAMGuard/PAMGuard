package alfa.clickmonitor.eventaggregator;

import PamguardMVC.PamDataUnit;

public interface EventAggregator<T extends PamDataUnit, U extends AggregateDataUnit<T>> {

	public U aggregateData(T dataUnit);
	
	/**
	 * Call when PAMguard starts. 
	 */
	public void reset();
	
}
