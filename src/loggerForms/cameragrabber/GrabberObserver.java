package loggerForms.cameragrabber;

public interface GrabberObserver {

	/**
	 * Notify message from main module. 
	 * @param grabberNotification
	 */
	void notify(GrabberNotification grabberNotification);

}
