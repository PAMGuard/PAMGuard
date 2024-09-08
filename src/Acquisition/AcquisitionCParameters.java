package Acquisition;

/**
 * Very simplified version of the Acquisition parameters
 * to be passed over the JNI to the C backend.
 * @author Doug
 *
 */
public class AcquisitionCParameters {

	public static final int MAXCHAN = 4;
	public static final int SOUNDCARD = 1;
	public static final int WAVFILE = 2;
	public static final int DAQCARD = 3;

	public	int deviceType;
	public	int deviceNumber;
	public	int sampleRate;
	public	int nChan;
	public	int spareInt1=0;
	public	int spareInt2=0;
	public String spareString1 = null;
	public String spareString2 = null;
	public	int[] channelList = new int[MAXCHAN];
	public String soundFile;

	public int setDaqType(String daqType) {
		deviceType = -1;
		if (daqType.equalsIgnoreCase("Sound Card")) {
			deviceType = SOUNDCARD;
		}
		else if (daqType.equalsIgnoreCase("Audio File")) {
			deviceType = WAVFILE;
		}
		else if (daqType.equalsIgnoreCase("National Instruments DAQ Cards")) {
			deviceType = DAQCARD;
		}
		return deviceType;
	}

}
