package PamController;

//import java.util.HashMap;

public class pamBuoyGlobals {
//	public static String remoteWavTestFile = null;
//	public static String remoteWavRecDir   = null;
//	public static String remoteDeviceName  = null;
//	public static String deviceDebug  = null;
//	public static Integer alsaBufferSize   = 0;
//	public static Integer alsaBufferFreq   = 0;
	
//	public static Integer remoteListAudio  = 0;
//	public static Integer useGstreamer     = 0;
//	public static boolean useNetworkCont   = false;
	private static Integer networkControlPort = null;
//	private static boolean useDSP = false;

	/**
	 * @return the networkControlPort
	 */
	public static Integer getNetworkControlPort() {
		return networkControlPort;
	}

	/**
	 * @param networkControlPort the networkControlPort to set
	 */
	public static void setNetworkControlPort(Integer networkControlPort) {
		pamBuoyGlobals.networkControlPort = networkControlPort;
	}
	

	//public static HashMap pbHash = new HashMap(10,0.75);
//	
//	public static boolean isUseDSP() {
//		return useDSP;
//	}
//
//	public static void setUseDSP(boolean useDSP) {
//		pamBuoyGlobals.useDSP = useDSP;
//	}
//
//	public static void setGstreamer(){
//		useGstreamer = 1;
//	}
//
//	public static Integer getGstreamer() {
//		return useGstreamer;
//	}
//
//	public static void setNetworkControlPort(int port) {
//		networkControlPort = port;
//	}
//	
//	public static void setWavString(String wavfile) {
//		remoteWavTestFile = wavfile;
//	}
//
//	public static String getWavString() {
//		return remoteWavTestFile;
//	}
//
//	public static void setWavRecDir(String wavdir) {
//		remoteWavRecDir = wavdir;
//	}
//
//	public static String getWavRecDir() {
//		return remoteWavRecDir;
//	}
//
//	public static void setListAudio() {
//		// TODO Auto-generated method stub
//		remoteListAudio = 1;
//	}
//	public static Integer getListAudio() {
//		return remoteListAudio;
//	}
//
//	public static void setNetworkCont() {
//		useNetworkCont = true;
//	}
//	public static boolean getNetworkCont() {
//		return useNetworkCont;
//	}
}
