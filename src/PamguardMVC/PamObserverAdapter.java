package PamguardMVC;

/**
 * Adapter class for PamObserver so not necessary to implement
 * absolutely everything. 
 * @author Doug Gillespie
 *
 */
public abstract class PamObserverAdapter implements PamObserver {

	@Override
	public PamObserver getObserverObject() {
		return this;
	}

	@Override
	public long getRequiredDataHistory(PamObservable observable, Object arg) {
		return 0;
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {

	}

	@Override
	public void noteNewSettings() {

	}

	@Override
	public void removeObservable(PamObservable observable) {

	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {

	}

	@Override
	public void addData(PamObservable observable, PamDataUnit pamDataUnit) {

	}

	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		
	}
	
	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}



}
