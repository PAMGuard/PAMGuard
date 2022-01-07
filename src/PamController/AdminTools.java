package PamController;

import java.io.PrintStream;
import java.util.prefs.Preferences;

public class AdminTools {

	private static Boolean isRunAdministrator = false;

	/**
	 * @return true if PAMGuard is running as administrator under Windows. 
	 * Doesn't seem to work - seem to test if logge in as administrator. 
	 */
	public synchronized static boolean isAdmin() {
		// from http://stackoverflow.com/questions/4350356/detect-if-java-application-was-run-as-a-windows-admin
		//		try {
		//			String groups[] = (new com.sun.security.auth.module.NTSystem()).getGroupIDs();
		//			for (String group : groups) {
		//				if (group.equals("S-1-5-32-544"))
		//					return true;
		//			}
		//		}
		//		catch (Exception e) {
		//			return false;
		//		}
		//	    return false;
		if (isRunAdministrator == null) {
			Preferences prefs = Preferences.systemRoot();
			PrintStream systemErr = System.err;
			synchronized(systemErr){    // better synchroize to avoid problems with other threads that access System.err
				System.setErr(null);
				try{
					prefs.put("foo", "bar"); // SecurityException on Windows
					prefs.remove("foo");
					prefs.flush(); // BackingStoreException on Linux
					isRunAdministrator = true;
				}catch(Exception e){
					isRunAdministrator = false;
				}finally{
					//		            System.setErr(systemErr);
				}
			}
		}
		return true;

	}

}
