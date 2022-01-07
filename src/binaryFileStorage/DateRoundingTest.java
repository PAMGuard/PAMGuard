package binaryFileStorage;

import PamUtils.PamCalendar;

public class DateRoundingTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long now = System.currentTimeMillis();
		System.out.println(" Now: " + PamCalendar.formatDateTime(now));
		long round = 3600*1000;
		long then = (now/round)*round;
		System.out.println("Then: " + PamCalendar.formatDateTime(then));
		System.out.println("Next: " + PamCalendar.formatDateTime(then+round));
	}

}
