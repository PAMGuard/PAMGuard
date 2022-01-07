package PamUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

import javax.swing.Timer;

public class TimedObservable extends Observable {

	private Timer t;
	
	private int delay = 1000;
	
	private long lastNotification;
	
	public TimedObservable() {
		
		super();

		t = new Timer(delay, new ObserverTimerAction());
	}
	
	class ObserverTimerAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			notifyObservers();
		}
		
	}
	
	/**
	 * Always send out a notification immediately. 
	 * Not possible to wait 
	 * @param immediate Notify immediately ignoring the delay
	 */
	public void notifyObservers(boolean immediate) {
		if (immediate) {
			lastNotification = 0;
		}
		notifyObservers();
	}

	@Override
	public void notifyObservers() {
		/**
		 * IF a notification has been sent out in the last delay interval, then
		 * start a timer and the notification will get sent when the timer
		 * returns. Don't do anything if the timer is already running or it
		 * might continually restart and never come back. 
		 * IF the delay interval has passed, then get on with it and send
		 * the notification. 
		 */
		long now = getTime();
		if (now - lastNotification >= delay) {	
			lastNotification = now;
			t.stop();
			setChanged();
			super.notifyObservers();
		}
		else {
			if (t.isRunning() == false) {
				t.start();
			}
		}
	}

	@Override
	public void notifyObservers(Object arg) {
		super.notifyObservers(arg);
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	/**
	 * Just do this - may want to do something more sophisitcated for offline. 
	 * @return
	 */
	private long getTime() {
		return System.currentTimeMillis();
	}

}
