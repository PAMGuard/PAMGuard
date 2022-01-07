package PamguardMVC;

/**
 * Version of pamdatablock that always keeps one and only one dataUnit.
 * @author Doug
 *
 */
public class SingletonDataBlock<Tunit extends PamDataUnit> extends PamDataBlock<Tunit> {

	public SingletonDataBlock(Class unitClass, String dataName, PamProcess parentProcess, int channelMap) {
		super(unitClass, dataName, parentProcess, channelMap);
		// TODO Auto-generated constructor stub
	}

	@Override
	synchronized public void addPamData(Tunit pamDataUnit) {
		this.clearAll();
		super.addPamData(pamDataUnit);
	}

	@Override
	protected int removeOldUnitsT(long currentTimeMS) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsS(long)
	 */
	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#clearAll()
	 */
	@Override
	public synchronized void clearAll() {
		super.clearAll();
	}

//	/* (non-Javadoc)
//	 * @see PamguardMVC.PamDataBlock#clearAll()
//	 */
//	@Override
//	public synchronized void clearAll() {
//		// TODO Auto-generated method stub
////		super.clearAll();
//	}

}
