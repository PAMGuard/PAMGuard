package nidaqdev;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import PamController.PamController;


/**
 * Nidaq is is a wrapper style class, which makes calls to native C++ functions 
 * within in a .dll library. Each of the native C++ methods, corresponds very closely
 * to a library call in NIDAQmx. It is by no means a complete wrapper for NIDAQmx
 * and currently only contains those calls useful to the PAMGUARD project www.pamguard.org
 * <p
 * 
 * @author Paul Redmond, rewritten by Doug Gillespie
 * 
 * To generate correct header file jni c side compiliation :
 * 
 * javac Nidaq.java
 * javah -jni  nidaqdev.Nidaq
 *
 */
public class Nidaq {


	/**
	 * Number of seconds for the main buffer in the NI device(s)
	 */
	static private final int BUFFERSECONDS = 3;

	/**
	 * Number of times NI callback fires per second to read data from device and send 
	 * through to java. 
	 */
	static private final int CALLBACKSPERSECOND = 20;

	private static final float playVoltageRange = 2.f;

	/**
	 * Process for acquisition callbacks when data arrive. 
	 */
	static private volatile NIDAQProcess niDaqProcess;

	public Nidaq() {
		super();
		load();
	}


	public boolean isLoadLibraryOK() {
		return loadLibraryOK;
	}


	/**
	 * Natively calls the NIDAQmx function DAQmxCreateTask.
	 * @param daqmxCreateTaskParams
	 */
	private native int daqmxCreateTask(DAQmxCreateTaskParams daqmxCreateTaskParams);


	/**
	 * Natively calls the NIDAQmx function DAQmxStartTask.
	 * @param daqmxStartTaskParams
	 */
	private native int daqmxStartTask(DAQmxStartTaskParams daqmxStartTaskParams);

	/**
	 * Natively calls the NIDAQmx function DAQmxStopTask.
	 * @param daqmxStopTaskParams
	 */
	private native int daqmxStopTask(DAQmxStopTaskParams daqmxStopTaskParams);

	/**
	 * Natively calls the NIDAQmx function DAQmxClearTask.
	 * @param daqmxClearTaskParams
	 */
	private native int daqmxClearTask(DAQmxClearTaskParams daqmxClearTaskParams);

	/**
	 * Natively calls the NIDAQmx function DAQmxCreateAIVoltageChan.
	 * @param daqmxCreateAIVoltageChanParams
	 */
	private native int daqmxCreateAIVoltageChan(DAQmxCreateAIVoltageChanParams daqmxCreateAIVoltageChanParams);

	/**
	 * Natively calls the NIDAQmx function DAQmxCfgSampClkTiming.
	 * @param daqmxCfgSampClkTimingParams
	 */
	private native int daqmxCfgSampClkTiming(DAQmxCfgSampClkTimingParams daqmxCfgSampClkTimingParams);


	/**
	 * Natively calls the NIDAQmx function DAQmxStartTask.
	 * @param daqmxCreateTaskParams
	 */
	private native int daqmxReadAnalogF64(DAQmxReadAnalogF64Params daqmxReadAnalogF64Params, 
			DaqData DaqData,  
			double dataArray[]); 

	/**
	 * Natively calls the NIDAQmx function DAQmxStartTask.
	 * @param daqmxCreateTaskParams
	 */  
	private native String daqmxGetErrorString(int errorCode);

	/**
	 * Natively calls the NIDAQmx function DAQmxGetDevIsSimulated.
	 * @param daqmxGetDevIsSimulatedParams
	 */
	private native int daqmxGetDevIsSimulated(DAQmxGetDevIsSimulatedParams daqmxGetDevIsSimulatedParams); 

	// ----------------------------------------------------
	// Method based on DAQmx macro
	public boolean javaDAQmxFailed(int error){
		//#define DAQmxSuccess          (0)
		//#define DAQmxFailed(error)    ((error)<0)
		return((error)<0);	
	}

	/**
	 * 
	 * @param deviceNo the device number
	 * @return the device name
	 */
	private native String javaDAQmxGetDeviceName(int deviceNo);  

	private native void jniSetCallBacksPerSecond(int callBacksPerSecond);

	private native int jniGetCallBacksPerSecond();
	/**
	 * 
	 * @param deviceNo the device number
	 * @return the device name
	 */
	public String getDeviceName(int deviceNo) {
		if (loadLibraryOK) {
			return javaDAQmxGetDeviceName(deviceNo);
		}
		else {
			return null;
		}
	}


	/**
	 * 
	 * @param deviceNo the device number
	 * @return the device type
	 */
	private native String javaDAQmxGetDeviceType(int deviceNo);  

	/**
	 * 
	 * @param deviceNo the device number
	 * @return the device name
	 */
	public String getDeviceType(int deviceNo) {
		if (loadLibraryOK) {
			return javaDAQmxGetDeviceType(deviceNo);
		}
		else {
			return null;
		}
	}

	private native int javaDAQmxGetNumDevices();

	/**
	 * Get the Number of NI devices. <p>
	 * N.B. the devices are 1 indexed, so when 
	 * device numbers will range from 1 to getNumDevices()
	 * not 0 to getNumDevices()-1.
	 * @return the number of NI devices
	 */
	public int getNumDevices() {
		if (loadLibraryOK) {
			return javaDAQmxGetNumDevices();
		}
		else {
			return 0;
		}
	}

	/**
	 * Get the NI Major version number
	 * @return major version number
	 */
	public int getMajorVersion() {
		if (loadLibraryOK) {
			return javaDAQmxGetMajorVersion();
		}
		else {
			return -1;
		}
	}

	/**
	 * Get the NI Major version number
	 * @return major version number
	 */
	private native int javaDAQmxGetMajorVersion();

	/**
	 * Get the NI Minor version number
	 * @return major version number
	 */
	public int getMinorVersion() {
		if (loadLibraryOK) {
			return javaDAQmxGetMinorVersion();
		}
		else {
			return -1;
		}
	}

	/**
	 * Set the number of times that NI calls the EveryNCallbacks function 
	 * to read from the internal buffers and send data through to the JAva side. 
	 * <br> Must be called just before jniPrepareDAQ.
	 * @param callBacksPerSecond
	 */
	public void setCallBacksPerSecond(int callBacksPerSecond) {
		if (loadLibraryOK) {
			jniSetCallBacksPerSecond(callBacksPerSecond);
		}
	}

	/**
	 * Get the number of call backs per second. 
	 * @return the number of NI callbacks per second. 
	 */
	public int getCallBacksPerSecond() {
		if (loadLibraryOK) {
			return jniGetCallBacksPerSecond();
		}
		else {
			return -1;
		}

	}
	/**
	 * Get the NI Minor version number
	 * @return major version number
	 */
	private native int javaDAQmxGetMinorVersion();


	private native int jniPreparePlayback(int boardNumber, int sampleRate,
			float voltageRange, int bufferSamples, int[] outputChannelList);

	public int javaPreparePlayback(int boardNumber, int sampleRate,
			int bufferSamples, int[] outputChannelList) {
		if (loadLibraryOK) {
			return jniPreparePlayback(boardNumber, sampleRate, playVoltageRange, bufferSamples, outputChannelList);
		}
		else {
			return 0;
		}
	}

	private native boolean jniStartPlayback();

	public boolean javaStartPlayback() {
		if (loadLibraryOK) {
			return jniStartPlayback();
		}
		else {
			return false;
		}
	}

	private native boolean jniStopPlayback();

	public boolean javaStopPlayback() {
		if (loadLibraryOK) {
			return jniStopPlayback();
		}
		else {
			return false;
		}
	}

	private native int jniPlaybackData(double[] data);

	public int javaPlaybackData(double[] data) {
		if (loadLibraryOK) {
			return jniPlaybackData(data);
		}
		else {
			return -1;
		}
	}

	/**
	 * Hold a static list of devices so that list only needs to be
	 * enumerated once. 
	 */
	private ArrayList<NIDeviceInfo> devices;
	private static Object dlSynchObject = new Object();
	/**
	 * Retrieve information for all NI devices. 
	 * @return Array list of information
	 */
	public ArrayList<NIDeviceInfo> getDevicesList() {
		synchronized (dlSynchObject) {
			if (devices != null)  {
				return devices;
			}
			devices = new ArrayList<NIDeviceInfo>();
			String devName, devType;
			int serialNum;
			boolean isSimulated;
			boolean isSimultaneous;
			int inputChannels, outputChannels;
			double[] aiRanges;
			double[] aoRanges;
			NIDeviceInfo devInfo;
			int nDevices = getNumDevices(); // new function !
			for (int i = 0; i <= nDevices; i++) {
				devName = getDeviceName(i);
				devType = getDeviceType(i);
				serialNum = getSerialNum(i);
				isSimulated = isSimulated(i);
				isSimultaneous = isAISimultaneousSampling(i);
				inputChannels = getMaxInputChannels(i);
				outputChannels = getMaxOutputChannels(i);
				aiRanges = getAIVoltageRanges(i);
				aoRanges = getAOVoltageRanges(i);
				if (devName != null) {
					devices.add(devInfo = new NIDeviceInfo(i, devName, devType, serialNum, isSimulated, 
							isSimultaneous, inputChannels, outputChannels, aiRanges, aoRanges));
					devInfo.setMaxMultiChannelRate(getMaxMultiChannelRate(i));
					devInfo.setMaxSingleChannelRate(getMaxSingleChannelRate(i));
				}
			}
			return devices;
		}
	}
	
	/**
	 * Find information for a specific device number 
	 * @param deviceNumber device number (NOT index !)
	 * @return device information or null. 
	 */
	public NIDeviceInfo findDeviceInfo(int deviceNumber) {
		ArrayList<NIDeviceInfo> deviceList = getDevicesList();
		for (NIDeviceInfo devInfo:deviceList) {
			if (devInfo.getDevNumber() == deviceNumber) {
				return devInfo;
			}
		}
		return null;
	}

	/**
	 * Get the devices serial number
	 * @param deviceNo the device number
	 * @return serial number
	 */
	private native int javaDAQmxGetSerialNum(int deviceNo);  

	/**
	 * Get the devices serial number
	 * @param deviceNo the device number
	 * @return serial number
	 */
	public int getSerialNum(int deviceNo) {
		if (loadLibraryOK) {
			return javaDAQmxGetSerialNum(deviceNo);
		}
		else {
			return 0;
		}
	}

	/**
	 * Set a single voltage channel to a fixed value. 
	 * @param deviceNo device number
	 * @param channel channel number
	 * @param range voltage range
	 * @param voltage voltage
	 * @return true if set successfully
	 */
	private native boolean jniSetAOVoltageChannel(int deviceNo, int channel, double range[], double voltage);

	/**
	 * Set a single voltage channel to a fixed value. 
	 * @param deviceNo device number
	 * @param channel channel number
	 * @param range voltage range
	 * @param voltage voltage
	 * @return true if set successfully
	 */
	public boolean setVoltageChannel(int deviceNo, int channel, double[] range, double voltage) {
		if (!loadLibraryOK) {
			return false;
		}
		return jniSetAOVoltageChannel(deviceNo, channel, range, voltage);
	}

	/**
	 * 
	 */
	public boolean setVoltageChannel(int deviceNo, int channel, double voltage) {
		if (!loadLibraryOK) {
			return false;
		}
		// auto range, by finding the minumum range that is >= the voltage. 
		// return false if one cannot be found. 
		ArrayList<NIDeviceInfo> devList = getDevicesList();
		NIDeviceInfo niDevice = findDeviceInfo(deviceNo);
		if (niDevice == null) {
			System.out.println("Nidaq:setVoltageChannel - unable to find device " + deviceNo);
			return false;
		}
//		if (devList == null || devList.size() <= deviceNo) {
//			System.out.println(String.format("No Dev no %d in device list of size %d", deviceNo, devList.size()));
//			return false;
//		}
		int nRanges = niDevice.getNumAOVoltageRanges();
		double[] range;
		double[] selRange = null;
		for (int i = 0; i < nRanges; i++) {
			range = niDevice.getAOVoltageRange(i);
			if (range[0] <= voltage && range[1] >= voltage) {
				selRange = range;
				break;
			}
		}
		if (selRange == null) {
			System.out.println("Invalid sel range in setVoltageChannel");
			return false;
		}
		return jniSetAOVoltageChannel(deviceNo, channel, selRange, voltage);
	}

	/**
	 * Get if the device is simulated
	 * @param deviceNo the device number
	 * @return is simulated
	 */
	private native boolean javaDAQmxIsSimulated(int deviceNo);  

	/**
	 * Get if the device is simulated
	 * @param deviceNo the device number
	 * @return is simulated
	 */
	public boolean isSimulated(int deviceNo) {
		if (loadLibraryOK) {
			return javaDAQmxIsSimulated(deviceNo);
		}
		else {
			return false;
		}
	}

	/**
	 * Get if the device is simulated
	 * @param deviceNo the device number
	 * @return is simulated
	 */
	private native boolean jniAISimultaneousSampling(int deviceNo);  

	/**
	 * Get if the device is simulated
	 * @param deviceNo the device number
	 * @return is simulated
	 */
	public boolean isAISimultaneousSampling(int deviceNo) {
		if (loadLibraryOK) {
			return jniAISimultaneousSampling(deviceNo);
		}
		else {
			return false;
		}
	}

	private native int jniGetMaxInputChannels(int deviceNo);

	public int getMaxInputChannels(int deviceNo) {
		if (loadLibraryOK) {
			return jniGetMaxInputChannels(deviceNo);
		}
		else {
			return 0;
		}
	}

	private native double jniGetMaxSingleChannelRate(int deviceNo);

	public double getMaxSingleChannelRate(int deviceNo) {
		if (loadLibraryOK) {
			return jniGetMaxSingleChannelRate(deviceNo);
		}
		else {
			return 0;
		}
	}

	private native double jniGetMaxMultiChannelRate(int deviceNo);

	public double getMaxMultiChannelRate(int deviceNo) {
		if (loadLibraryOK) {
			return jniGetMaxMultiChannelRate(deviceNo);
		}
		else {
			return 0;
		}
	}

	private native int jniGetMaxOutputChannels(int deviceNo);

	public int getMaxOutputChannels(int deviceNo){
		if (loadLibraryOK) {
			return jniGetMaxOutputChannels(deviceNo);
		}
		else {
			return 0;
		}
	}

	private native double[] javaGetAIVoltageRanges(int deviceNo);

	public double[] getAIVoltageRanges(int deviceNo) { 
		if (loadLibraryOK) {
			double[] d = javaGetAIVoltageRanges(deviceNo);
			return d;
		}
		else {
			return null;
		}
	}

	private native double[] javaGetAOVoltageRanges(int deviceNo);

	public double[] getAOVoltageRanges(int deviceNo) {
		if (loadLibraryOK) {
			double[] d = javaGetAOVoltageRanges(deviceNo);
			return d;
		}
		else {
			return null;
		}

	}


	//    public native int jnuGetMaxSampleRate(int deviceNo);

	private native void jniSetTerminalConfig(int terminalConfig);

	/**
	 * Basic parameters for setting up NI acquisition
	 * @param deviceNo Master device number
	 * @param sampleRate sample rate
	 * @param bufferSeconds buffer length in seconds
	 * @param inputChannelList channel list
	 * @param rangeList input range list
	 * @param deviceList device list (often same as deviceNo, unless multi board operation is used)
	 * @return
	 */
	private native int jniPrepareDAQ(int deviceNo, int sampleRate, int bufferSeconds, 
			int[] inputChannelList, double[] rangesLo, double[] rangesHi, int[] deviceList);

	private native int jniStartDAQ();

	private native int jniStopDAQ();


	// ----------------------------------------------------
	public String javaDAQmxGetErrorString(int errorCode){
		// #define DAQmxErrorDAQmxVersionNotSupported  (-201076)

		String s = daqmxGetErrorString(-201076);
		System.out.println("j_daqmxGetErrorString: " + s); 

		return(s);
	}


	// ----------------------------------------------------
	public void daqTest() { 
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("---------------------------------------------------- ");
		System.out.println("Operating System: " + System.getProperty("os.name"));   
		//	j_daqmxGetErrorString(0);


		//    daqmxGetDevIsSimulated
		DAQmxGetDevIsSimulatedParams daqmxGetDevIsSimulatedParams = new DAQmxGetDevIsSimulatedParams();
		if(javaDAQmxFailed(daqmxGetDevIsSimulated(daqmxGetDevIsSimulatedParams))){
			System.out.println("daqmxGetDevIsSimulated: Failed.");
		} else {
			System.out.println("daqmxGetDevIsSimulated: Success.");
			System.out.println("Device: " + daqmxGetDevIsSimulatedParams.getDeviceName() + ", Simulatated = " + daqmxGetDevIsSimulatedParams.isDeviceIsSimulated());
		}

		// daqmxCreateTask
		DAQmxCreateTaskParams daqmxCreateTaskParams = new DAQmxCreateTaskParams();

		if(javaDAQmxFailed(daqmxCreateTask(daqmxCreateTaskParams))){
			System.out.println("daqmxCreateTask: Failed.");
		} else {
			System.out.println("daqmxCreateTask: Success.");
		}



		//daqmxCreateAIVoltageChan
		DAQmxCreateAIVoltageChanParams daqmxCreateAIVoltageChanParams = new DAQmxCreateAIVoltageChanParams();
		daqmxCreateAIVoltageChanParams.setTaskHandle(daqmxCreateTaskParams.getTaskHandle());

		if(javaDAQmxFailed(daqmxCreateAIVoltageChan(daqmxCreateAIVoltageChanParams))){
			System.out.println("daqmxCreateAIVoltageChan: Failed.");
		} else {
			System.out.println("daqmxCreateAIVoltageChan: Success.");
		}


		// daqmxCfgSampClkTimingParams
		DAQmxCfgSampClkTimingParams daqmxCfgSampClkTimingParams = new DAQmxCfgSampClkTimingParams();
		daqmxCfgSampClkTimingParams.setTaskHandle(daqmxCreateTaskParams.getTaskHandle());
		if(javaDAQmxFailed(daqmxCfgSampClkTiming(daqmxCfgSampClkTimingParams))){
			System.out.println("daqmxCfgSampClkTiming: Failed.");
		} else {
			System.out.println("daqmxCfgSampClkTiming: Success.");
		}


		// daqmxStartTask
		DAQmxStartTaskParams daqmxStartTaskParams = new DAQmxStartTaskParams();
		daqmxStartTaskParams.setTaskHandle(daqmxCreateTaskParams.getTaskHandle());
		if(javaDAQmxFailed(daqmxStartTask(daqmxStartTaskParams))){
			System.out.println("daqmxStartTask: Failed.");
		} else {
			System.out.println("daqmxStartTask: Success.");
		}



		//DaqConfig dc = new DaqConfig();
		//dc.setTaskHandle(daqmxCreateTaskParams.getTaskHandle());

		//int z = this.daqSetup(dc);
		//System.out.println("DaqStuff: " + z); 
		//System.out.println("dc TaskHandle: " + dc.taskHandle); 
		DaqData daqData;



		DAQmxReadAnalogF64Params daqmxReadAnalogF64Params = new DAQmxReadAnalogF64Params();
		daqmxReadAnalogF64Params.setTaskHandle(daqmxCreateTaskParams.getTaskHandle());

		for(int i=0;i<20;i++){
			daqData = new DaqData();
			daqData.makeNewData(5000);
			if(javaDAQmxFailed(daqmxReadAnalogF64(daqmxReadAnalogF64Params, daqData, daqData.data)))
			{	System.out.println("daqRead: Failed.");
			} else {
				System.out.println("daqRead: Success.");
			}
			System.out.println("JAVA: samples read = " + daqmxReadAnalogF64Params.getSampsPerChanRead()); 


			try{
				// Create file 
				FileWriter fstream = new FileWriter("samples.txt",true);
				PrintWriter print_writer = new PrintWriter (fstream,true);

				for (int ii = 0; ii < daqData.data.length; ii++) {
					print_writer.println(daqData.data[ii]);
				}

				// Close the output stream
				fstream.close();
			}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage() + " , " + e.getLocalizedMessage());
			}
		}

		// daqmxStopTask
		DAQmxStopTaskParams daqmxStopTaskParams = new DAQmxStopTaskParams();
		daqmxStopTaskParams.setTaskHandle(daqmxCreateTaskParams.getTaskHandle());
		if(javaDAQmxFailed(daqmxStopTask(daqmxStopTaskParams))){
			System.out.println("daqmxStopTask: Failed.");
		} else {
			System.out.println("daqmxStopTask: Success.");
		}
		// daqmxClearTask
		DAQmxClearTaskParams daqmxClearTaskParams = new DAQmxClearTaskParams();
		daqmxClearTaskParams.setTaskHandle(daqmxCreateTaskParams.getTaskHandle());
		if(javaDAQmxFailed(daqmxClearTask(daqmxClearTaskParams))){
			System.out.println("daqmxStopTask: Failed.");
		} else {
			System.out.println("daqmxStopTask: Success.");
		}

	}

	// ----------------------------------------------------
	public class DaqConfig 
	{		
		int taskHandle;
		int samplesPerChannel; // Number of samples to acquire per channel

		public int getTaskHandle() {
			return this.taskHandle;
		}

		public void setTaskHandle(int taskHandle) {
			this.taskHandle = taskHandle;
		}

		public int getSamplesPerChannel() {
			return samplesPerChannel;
		}

		public void setSamplesPerChannel(int samplesPerChannel) {
			this.samplesPerChannel = samplesPerChannel;
		}
	}

	public class DAQmxCreateTaskParams 
	{
		//taskName, &taskHandle)
		private int taskHandle; // A reference to the task created in this function.
		DAQmxCreateTaskParams(){
		}
		public int getTaskHandle() {
			return taskHandle;
		}
		public void setTaskHandle(int taskHandle) {
			this.taskHandle = taskHandle;
		}

	}
	// ------------------------------------------ DAQmxStartTaskParams
	public class DAQmxStartTaskParams 
	{
		/* DAQmxStartTask(	
		 * TaskHandle	taskHandle,  // The task to start.
		 */
		private int taskHandle;

		// Constructor
		DAQmxStartTaskParams(){
		}


		// ACCESSORS
		public int getTaskHandle() {
			return taskHandle;
		}
		public void setTaskHandle(int taskHandle) {
			this.taskHandle = taskHandle;
		}
	};

	// ------------------------------------------ DAQmxStopTaskParams
	public class DAQmxStopTaskParams 
	{
		/* DAQmxStopTask(	
		 * TaskHandle	taskHandle,  // The task to stop.
		 */
		private int taskHandle;

		// Constructor
		DAQmxStopTaskParams(){
		}


		// ACCESSORS
		public int getTaskHandle() {
			return taskHandle;
		}
		public void setTaskHandle(int taskHandle) {
			this.taskHandle = taskHandle;
		}
	};
	// ------------------------------------------ DAQmxClearTaskParams
	public class DAQmxClearTaskParams 
	{
		/* DAQmxClearTask(	
		 * TaskHandle	taskHandle,  // The task to clear.
		 *                           // Stops task and frees task resources
		 */
		private int taskHandle;

		// Constructor
		DAQmxClearTaskParams(){
		}


		// ACCESSORS
		public int getTaskHandle() {
			return taskHandle;
		}
		public void setTaskHandle(int taskHandle) {
			this.taskHandle = taskHandle;
		}
	};

	// ------------------------------------------ DAQmxCreateAIVoltageChanParams
	public class DAQmxCreateAIVoltageChanParams 
	{
		/* Native function and parameter types
		 * See NI NI-DAQmx C Reference Help for more info
		 * 
		 * int32 DAQmxCreateAIVoltageChan (
		 * TaskHandle taskHandle, //The task to which to add the channels that this function creates.
		 * const char physicalChannel[], // The names of the physical channels to create virtual chans from e.g "Dev1/ai0,Dev1/ai1"
		 * const char nameToAssignToChannel[], // The name(s) to assign to the created virtual channel(s). If you do not specify a name defaults to phys chan name.
		 * int32 terminalConfig, // The input terminal configuration for the channel.
		 * float64 minVal, //The minimum value, in units, that you expect to measure. 
		 * float64 maxVal, //The maximum value, in units, that you expect to measure. 
		 * int32 units, //The units to use to return the voltage measurements. 
		 * const char customScaleName[]); // Must be NULL if not a customscale above	
		 */	

		//	DAQmxErrChk(DAQmxCreateAIVoltageChan(taskHandle, physicalChannel0, channelName0, DAQmx_Val_Cfg_Default, minVolVal, maxVolVal,DAQmx_Val_Volts, NULL) );


		private int taskHandle;
		// const char physicalChannel[], //(const char []) The names of the physical channels to create virtual chans from e.g "Dev1/ai0,Dev1/ai1"
		// const char nameToAssignToChannel[], // The name(s) to assign to the created virtual channel(s). If you do not specify a name defaults to phys chan name.
		int terminalConfig;
		double minVal;
		double maxVal;
		int units; //int32 The units to use to return the voltage measurements. 
		//* const char customScaleName[]);   


		// Constructor
		DAQmxCreateAIVoltageChanParams(){
			terminalConfig = NIConstants.DAQmx_Val_Cfg_Default;		
			minVal = -1.0;
			maxVal = 1.0;
			units = NIConstants.DAQmx_Val_Voltage;
		}

		public double getMaxVal() {
			return maxVal;
		}

		public void setMaxVal(double maxVal) {
			this.maxVal = maxVal;
		}

		public double getMinVal() {
			return minVal;
		}

		public void setMinVal(double minVal) {
			this.minVal = minVal;
		}

		public int getTaskHandle() {
			return taskHandle;
		}

		public void setTaskHandle(int taskHandle) {
			this.taskHandle = taskHandle;
		}

		public int getTerminalConfig() {
			return terminalConfig;
		}

		public void setTerminalConfig(int terminalConfig) {
			this.terminalConfig = terminalConfig;
		}

		public int getUnits() {
			return units;
		}

		public void setUnits(int units) {
			this.units = units;
		}
	}




	//	 ------------------------------------------ DAQmxCreateAIVoltageChanParams	
	public class DAQmxCfgSampClkTimingParams
	{
		/*	
	    DAQmxCfgSampClkTiming(
	    TaskHandle	taskHandle, //The task used in this function.
	    const char [] 	source, // The source terminal of the Sample Clock. internal = OnboardClock
	    float64 rate, // The sampling rate in samples per second per channel. 
	    int32 activeEdge, // which clock edge (rising vs falling) to use i.e. DAQmx_Val_Rising, 
	    int32 sampleMode, // e.g. continous or finite i.e. DAQmx_Val_ContSamps, 
	    uInt64 sampsPerChanToAcquire, // The number of samples to acquire or generate for each channel. samplesPerChan) );
		 */

		private int taskHandle;
		//  const char [] 	source,
		private double rate;
		private int activeEdge;
		private int sampleMode;
		private long sampsPerChanToAcquire;

		DAQmxCfgSampClkTimingParams(){
			rate = 500000.0;
			activeEdge = NIConstants.DAQmx_Val_Rising;
			sampleMode = NIConstants.DAQmx_Val_ContSamps;
			sampsPerChanToAcquire = 0;	// use 0 when DAQmx_Val_ContSamps lets NI-DAQ use default buffer size.
		}

		public int getActiveEdge() {
			return activeEdge;
		}

		public void setActiveEdge(int activeEdge) {
			this.activeEdge = activeEdge;
		}

		public double getRate() {
			return rate;
		}

		public void setRate(double rate) {
			this.rate = rate;
		}

		public int getSampleMode() {
			return sampleMode;
		}

		public void setSampleMode(int sampleMode) {
			this.sampleMode = sampleMode;
		}

		public long getSampsPerChanToAcquire() {
			return sampsPerChanToAcquire;
		}

		public void setSampsPerChanToAcquire(long sampsPerChanToAcquire) {
			this.sampsPerChanToAcquire = sampsPerChanToAcquire;
		}

		public int getTaskHandle() {
			return taskHandle;
		}

		public void setTaskHandle(int taskHandle) {
			this.taskHandle = taskHandle;
		}
	}

	// ------------------------------------------ DAQmxGetDevIsSimulatedParams
	public class DAQmxGetDevIsSimulatedParams
	{
		// 
		private String deviceName = null;
		private boolean deviceIsSimulated;  //natively a bool32 which Indicates if the device is a simulated device. 

		DAQmxGetDevIsSimulatedParams(){
			deviceName = "Dev1";
		}



		public String getDeviceName() {
			return deviceName;
		}

		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}



		public boolean isDeviceIsSimulated() {
			return deviceIsSimulated;
		}



		public void setDeviceIsSimulated(boolean deviceIsSimulated) {
			this.deviceIsSimulated = deviceIsSimulated;
		};
	}

	// ------------------------------------------ DAQmxReadAnalogF64Params
	/**
	 * @author Paul Redmond
	 *         <p>
	 *         Class for holding the input and output parameters of the 
	 *		   call to DAQmxReadAnalogF64Params
	 *         <p>      
	 */
	public class DAQmxReadAnalogF64Params 
	{
		/*	DAQmxErrChk (DAQmxReadAnalogF64(	
		 * taskHandle,  // (TaskHadle) The task to read samples from 
			int32 		samplesPerChan,  	// Number of samples to acquire per channel.
			float64		timeout = 10.0;		// The amount of time, in seconds, to wait for the function to read the sample(s).
			int32		fillMode = DAQmx_Val_GroupByChannel; // (bool32)Specifies whether or not the samples are interleaved.
			buffer[writeBufIndex], // (float64[]) The array to read samples into, organised as per fill mode.
			uInt32 arraySizeInSamps, 	// The size (in samples) of the array to read samples into.
			int32* &sampsPerChanRead, 	// The actually number of sample read from each channel.
			NULL));
		 */

		private int taskHandle;
		private int samplesPerChannel; 
		private double timeout;	
		private int fillMode ;  
		private int arraySizeInSamps; // *note* this is a uint natively
		private int sampsPerChanRead; 


		// CONSTRUCTOR
		DAQmxReadAnalogF64Params(){
			timeout = 10.0;
			samplesPerChannel = 5000;
			fillMode = NIConstants.DAQmx_Val_GroupByChannel; 
			arraySizeInSamps = samplesPerChannel;
		}




		// ==============================
		// ACCESSOR METHODS

		public int getTaskHandle() {
			return this.taskHandle;
		}

		public void setTaskHandle(int taskHandle) {
			this.taskHandle = taskHandle;
		}

		public int getSamplesPerChannel() {
			return samplesPerChannel;
		}

		public void setSamplesPerChannel(int samplesPerChannel) {
			this.samplesPerChannel = samplesPerChannel;
		}

		public double getTimeout() {
			return timeout;
		}

		public void setTimeout(double timeout) {
			this.timeout = timeout;
		}

		public int getFillMode() {
			return fillMode;
		}

		public void setFillMode(int fillMode) {
			this.fillMode = fillMode;
		}

		public int getArraySizeInSamps() {
			return arraySizeInSamps;
		}

		public void setArraySizeInSamps(int arraySizeInSamps) {
			this.arraySizeInSamps = arraySizeInSamps;
		}

		public int getSampsPerChanRead() {
			return sampsPerChanRead;
		}

		public void setSampsPerChanRead(int sampsPerChanRead) {
			this.sampsPerChanRead = sampsPerChanRead;
		}
	}





	// ------------------------------------------ 
	public class DaqData 
	{
		private double[] data;

		public void makeNewData(int arraySize) {
			setData(new double[arraySize]);
		}

		public double[] getData() {
			return data;
		}

		public void setData(double[] data) {
			this.data = data;
		}	
	}    


	private static final String SILIB = "jninidaqmx";
	private boolean loadLibraryOK = false;
	private boolean loadLibraryTried = false;
	private static boolean versionShown = false;
	//
	//	static {
	//		load();
	//	}

	public void load() {
		if (loadLibraryTried == false) {
			try  {
				System.loadLibrary(SILIB);
				loadLibraryOK = true;
				sayVersionInfo();
			}
			catch (UnsatisfiedLinkError e)
			{
				// don't show any message - causes confusion for people not using NI.
//				System.out.println ("National Instruments Interface '" + SILIB + "' cannot be loaded for the following reason:");
//				System.out.println("Unsatisfied Link Error : " + e.getMessage());
//				loadLibraryOK = false;
			}
		}
		loadLibraryTried = true;
		//		System.out.println ("native lib '" + SILIB + "' found in 'java.library.path': "
		//				+ System.getProperty ("java.library.path"));
	}

	/**
	 * Print the NI version information on the terminal
	 * May be useful for assisting users. 
	 */
	private void sayVersionInfo() {
		if (versionShown == false) {
			int majV = getMajorVersion();
			int minV = getMinorVersion();
			System.out.println(String.format("National Instruments software loaded version %d.%d", majV, minV));
			versionShown = true;
		}
	}


	public void setTerminalConfig(int terminalConfig) {
		jniSetTerminalConfig(terminalConfig);
	}

	public int prepareDAQ(int deviceNo, int sampleRate, int[] inputChannelList, double[] rangeListLo, double[] rangeListHi, int[] deviceList){
		jniSetCallBacksPerSecond(CALLBACKSPERSECOND);
		return jniPrepareDAQ(deviceNo, sampleRate, BUFFERSECONDS, inputChannelList, rangeListLo, rangeListHi, deviceList);
	}

	public int startDAQ() {
		return jniStartDAQ();
	}

	public int stopDAQ() {
		return jniStopDAQ();
	}

	/**
	 * Mostly used as a callback from the C side to show string messages
	 * @param string
	 */
	public void showString(char[] string) {
		System.out.println(string);
		System.out.flush();
	}

	public static void sayString(char[] string) {
		System.out.println(string);
		System.out.flush();
	}

	public static void fullBuffer(int iChan, double[] data) {
		//		System.out.println(String.format("%d samples of data received for channel %d",
		//				data.length, iChan));
		// copy niDaqProcess in case it get's nulled from another thread between these next two calls. 
		NIDAQProcess aProcess = niDaqProcess;
		if (aProcess != null) {
			aProcess.fullBuffer(iChan, data);
		}
	}

	public static void resetPamguard(int errorCode, char[] errorString) {
		System.out.println(String.format("Reset PAMGUARD NI Code %d: %s",
				errorCode, new String(errorString)));
		PamController pamController = PamController.getInstance();
		pamController.restartPamguard();
	}


	/**
	 * @return the niDaqProcess
	 */
	public static NIDAQProcess getNiDaqProcess() {
		return niDaqProcess;
	}


	/**
	 * @param niDaqProcess the niDaqProcess to set
	 */
	public static void setNiDaqProcess(NIDAQProcess niDaqProcess) {
		Nidaq.niDaqProcess = niDaqProcess;
	}


} 
