package PamUtils.time.ntp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;

import PamUtils.PamCalendar;

public class NISTTimeTest {
	
	/*
	 * Taken from 
	 *  http://www.avajava.com/tutorials/lessons/how-do-i-query-the-nist-internet-time-service-using-the-network-time-protocol.html
	 *  time.nist.gov
	 */
//	public static final String TIME_SERVER = "time.nist.gov";
	public static final String TIME_SERVER = "time.nist.com";

	public static void main(String[] args)  {
		try {
			NTPUDPClient timeClient = new NTPUDPClient();
			InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
			int n = 0;
			long[] offset1 = new long[n];
			long[] offset2 = new long[n];
			for (int i = 0; i < n; i++) {
				long now = System.currentTimeMillis();
				TimeInfo timeInfo = timeClient.getTime(inetAddress);
				long now2 = System.currentTimeMillis();
				NtpV3Packet message = timeInfo.getMessage();
				long serverTime = message.getTransmitTimeStamp().getTime();
				System.out.printf("Time %d from %s at %s took %d millis returned %s offset %d millis\n", i+1, TIME_SERVER, 
						PamCalendar.formatDateTime(now), now2-now, PamCalendar.formatTime(serverTime, 3, false), serverTime-now2);
				offset1[i] = serverTime - now;
				offset2[i] = serverTime - now2;
				Thread.sleep(5000);
			}
			timeClient.close();
		}
		catch (Exception e) {
			
		}
		
//		test how quickly Systemtime works. 
		long end = System.currentTimeMillis();
		int n = 0;
		int nChanges = 0;
		long now;
		long last = end;
		end += 1000;
		while ((now = System.currentTimeMillis()) < end) {
			n++;
			
			if (now != last) {
				nChanges++;
				last = now;
			}
		}
		System.out.printf("%d calls to System.currentTimeMillis made in 1 second with %d value changes\n", n, nChanges);
		System.out.printf("Mean time per call = %3.3fus\n", (double)n/1.e6);
	}

}
