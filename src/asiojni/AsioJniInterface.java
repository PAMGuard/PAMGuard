package asiojni;

import java.util.Vector;

import PamDetection.RawDataUnit;
import PamguardMVC.PamConstants;



//javah -jni  asiojni.AsioJniInterface
//javac AsioJniInterface.java


//File: AsioJniInterface.java
public class AsioJniInterface {
	public AsioDriverInfos asioDriverInfos;
	public AsioDriverSettings asioDriverSettings;
	public AsioController asioController;

	public native void getAsioDrivers(AsioDriverInfos asioDrivers);
	public native boolean jniGetAsioControlPanelSettings(String driverName, AsioDriverSettings asioDriverSettings);
	public native void jniAsioStart(AsioController asioController, String driverName,int[] maxChannelList, int[] outputChannels); //Xiao Yan Deng
	public native void jniSetDriverAndSampleRate(int sampleRate, int numChannels, String driverName);
	public native boolean jniPlayData(int iChannel, double[] data);

	boolean	recordActive = true;
//	int maxChannelNumber = 0; // Channels numbered from Channel0.
	int inputChannelList[] = new int[PamConstants.MAX_CHANNELS]; //Xiao Yan Deng
	int outputChannelList[] = null;

	//public Thread asioRunningThread;

//	public Vector<Vector<double[]>> channels;
	private Vector<RawDataUnit> asioDataUnits;

	//public ArrayList <double[]> channel1Buffers;
	ASIOSoundSystem asioSoundSystem;


	public AsioJniInterface(ASIOSoundSystem asioSoundSystem) {

		this.asioSoundSystem = asioSoundSystem;
		asioDriverInfos = new AsioDriverInfos(this);
		asioDriverSettings = new AsioDriverSettings();
		asioController = new AsioController();
	}

	public void createChannelBuffers(){

		int nChannels = getNumInputChannels();
		
//		System.out.println("createChannelBuffers(" + nChannels);
//		System.out.flush();

//		channels = new Vector<Vector<double[]>>();
		asioDataUnits = new Vector<RawDataUnit>();
		//
		for(int i=0; i < nChannels; i++){
//			Vector<double[]> channelBuffer = new Vector<double[]>();
//			channels.add(channelBuffer);	
			//System.out.println("Added channel buffer ArrayList for :" + i);
//			System.out.println("Added channel buffer ArrayList for :" + 
//					inputChannelList[i]);
//			System.out.flush();
		}

	}

	// For this class to work it need to load the asio jni dll
	// Try to load the library as part of making the object.
	static {
		load();
	}

	public void addDriverToList(String driverName, int[] maxChannels, int[]sampleRateInfo){
		asioDriverInfos.addDriverToList(driverName, maxChannels, sampleRateInfo);
	}


	//private static final String SILIB = "lib/pamguardasio";
	private static final String SILIB = "pamguardasio";
	private static final String SILIB2 = "pamguardasio";
	
	private static boolean loadLibraryOK = false;
	private static boolean loadLibraryTried = false;

	public static void load() {
		if (!loadLibraryTried)
			try  {
				System.loadLibrary(SILIB);
				loadLibraryOK = true;
			}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("ASIO error e:"+e.getMessage());
//			e.printStackTrace();
			try  {
				System.loadLibrary(SILIB2);
				loadLibraryOK = true;
			}
			catch (UnsatisfiedLinkError e1)
			{
				loadLibraryOK = false;
				System.out.println("ASIO error e1:"+e1.getMessage());
			}
			loadLibraryOK = false;
		}
		loadLibraryTried = true;
	}





	/*public static void main(String[] args) {
		AsioJniInterface asioInterface = new AsioJniInterface();	

		//asioInterface.testMain();
	}

	public void testMain(){

		//AsioDriverInfos asioDriverInfos = new AsioDriverInfos();
		//AsioDriverSettings asioDriverSettings = new AsioDriverSettings();
		//AsioController asioController = new AsioController();

		//asioDriverInfos.getAsioDriverList();
		//asioDriverSettings.getAsioControlPanelSettings();
		//
		asioController.asioStart();


		//recordTimer();


	}*/

	public void callJniGetAsioDrivers(AsioDriverInfos asioDrivers){
		getAsioDrivers(asioDrivers);
	}

//
//	public int getMaxChannelNumber(){
//		return (maxChannelNumber);
//	}
	public int[] getInputChannelList() {  //Xiao Yan Deng
		return (this.inputChannelList);
	}
	
	public int getNumInputChannels() {
		if (inputChannelList == null) {
			return 0;
		}
		return inputChannelList.length;
	}

	public int getNumOutputChannels() {
		if (outputChannelList == null) {
			return 0;
		}
		return outputChannelList.length;
	}
	

	public void updateJavaBuffers(int channelNumber, double data[]){

		double[] dataCopy;

		dataCopy = new double[data.length];

		for(int i =0; i<data.length; i++){
			dataCopy[i] = data[i];
		}
	
		asioSoundSystem.newASIOData(dataCopy, channelNumber);

		return;
	}

	/**
	 * 
	 */
	private void recordTimer() {
		// TODO Auto-generated method stub

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setRecordActive(false);
	}

	public boolean isRecordActive() {
		//System.out.println("JAVA: recodring? " + recordActive);
		return recordActive;
	}
	public void setRecordActive(boolean recordActive) {
		this.recordActive = recordActive;
	}

	public int getSampleRate(){
		return asioDriverSettings.getSampleRate();
	}

	public class AsioController{
		//AsioThread controlThread;

		public AsioController(){	

		}

		public void asioStart() {

			//channel1Buffers.clear();
			setRecordActive(true);
			AsioThread controlThread = new AsioThread();
			controlThread.ref = this;
//			controlThread.setPriority(Thread.MAX_PRIORITY);
//			if(controlThread.getPriority()==Thread.NORM_PRIORITY)
//				System.out.println("NORM_PRIORITY");
//			if(controlThread.getPriority()==Thread.MIN_PRIORITY)
//				System.out.println("MIN_PRIORITY");
//			if(controlThread.getPriority()==Thread.MAX_PRIORITY)
//				System.out.println("MAX_PRIORITY");
//
//			System.out.println("PRIORITY: " + controlThread.getPriority());
//			System.out.flush();
			controlThread.start();
		}

		public void asioStop(){



		}

		class AsioThread extends Thread {
			AsioThread() {
			}
			AsioController ref;	
			@Override
			public void run() {
//				System.out.println("starting asio with the " + asioDriverSettings.getDriverName() + " diver");
				jniAsioStart(ref,asioDriverSettings.getDriverName(),
						inputChannelList, outputChannelList); 
			}

		}
	}

	public class AsioDriverSettings{
		private int sampleRate = 44100;
		private int maxInputs = 0;
		private boolean asioControlPanelValid = false;
		String driverName;
		AsioDriverSettings(){

		}

		public void displayAsioDriverSettings() {
//			System.out.println();
//			System.out.println("==========================");
//			System.out.println("== ASIO Driver Settings ==");
//			System.out.println();
//			System.out.println("Sample Rate : " + sampleRate);
//			System.out.println("Max Inputs  : " + maxInputs);
//			System.out.println("Ctrl Panel Valid : " + asioControlPanelValid);
//			System.out.println("==========================");
//			System.out.flush();


		}

		public void getAsioControlPanelSettings(){
			//asioControlPanelValid = jniGetAsioControlPanelSettings(driverName,this);
			//System.out.println("Java, asioControlPanelValid = " + asioControlPanelValid);
		}


		public int getSampleRate() {
			return sampleRate;
		}

		public void setSampleRate(int sampleRate) {
			//	System.out.println("Java, ASIO sample rate = " + sampleRate);
			this.sampleRate = sampleRate;
		}

		public boolean isAsioControlPanelValid() {
			return asioControlPanelValid;
		}

		public void setAsioControlPanelValid(boolean asioControlPanelValid) {
			this.asioControlPanelValid = asioControlPanelValid;
		}

		public int getMaxInputs() {
			return maxInputs;
		}

		public void setMaxInputs(int maxInputs) {
			this.maxInputs = maxInputs;
			//System.outprintln("Java, ASIO maximum inputs = " + maxInputs);
		}
		/*public int updateJavaBuffers(int test){

			System.out.println("JAVA:: updateJavaBuffers:: " + test);
			System.out.flush();
			return(999);
		}
		 */

		public String getDriverName() {
			return driverName;
		}

		public void setDriverName(String driverName) {
			this.driverName = driverName;
		}

	}
	public void displayAsioMessage (int Error) {
		System.out.println("ASIO System error " + Error);
	}

	/*public class AsioDriverInfo
	{
		String driverName;
		int maxChannels;
		ArrayList<Integer> sampleRateInfo;

		AsioDriverInfo(String driverName, int maxChannels, ArrayList<Integer> sampleRateInfo){
			this.driverName = driverName;
			this.maxChannels = maxChannels;
			this.sampleRateInfo = sampleRateInfo;
		}


	}*/

	public AsioDriverInfos getAsioDriverInfos() {

		return asioDriverInfos;
	}
	
//	public void setMaxChannelNumber(int maxChannelNumber) {
//		this.maxChannelNumber = maxChannelNumber;
//	}
	public void setInputChannelList(int[] inputChannelList, int nInputChannels) {  //Xiao Yan Deng
		/*
		 * need to strip this down so that it's only containing the 
		 * used channels. 
		 */
		this.inputChannelList = new int[nInputChannels];
		for (int i = 0; i < nInputChannels; i++) {
			this.inputChannelList[i] = inputChannelList[i];
		}
	}
	
	public int[] getPlaybackChannels() {
		return outputChannelList;
	}
	public void setPlaybackChannels(int[] playbackChannels) {
		this.outputChannelList = playbackChannels;
	}
	public Vector<RawDataUnit> getAsioDataUnits() {
		return asioDataUnits;
	}

	public boolean playData(int outputChannel, RawDataUnit rawDataUnit) {
//		int iChan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		return jniPlayData(outputChannel, rawDataUnit.getRawData());
	}


}