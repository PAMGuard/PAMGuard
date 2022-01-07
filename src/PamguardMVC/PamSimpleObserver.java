package PamguardMVC;

public abstract class PamSimpleObserver extends PamObserverAdapter {

	private PamObservable currentObservable;
	
	public PamSimpleObserver() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Set a single observable. If there is already an observable
	 * object, then delete this observable from it first. 
	 * @param pamObservable
	 */
	public void setObservable(PamObservable pamObservable) {
		if (currentObservable != null) {
			currentObservable.deleteObserver(this);
		}
		currentObservable = pamObservable;
		if (pamObservable != null) {
			pamObservable.addObserver(this);
		}
	}
}
