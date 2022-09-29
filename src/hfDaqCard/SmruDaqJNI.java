package hfDaqCard;

import java.util.ArrayList;
import java.util.Arrays;

import PamUtils.BubbleSort;

/**
 * Functions to interface with the SAIL Ltd DAQ card C code via JNI
 * @author Doug Gillespie
 *
 */
public class SmruDaqJNI {

	// Master Slave modes (matches jni C code). 
	static public final int SMRU_VAL_STANDALONE	= 0;
	static public final int SMRU_VAL_MASTER = 1;
	static public final int SMRU_VAL_SLAVE	= 2;
	static public final int SMRU_VAL_SLAVE_MASTERED = 3;
	
	static public final int SMRU_RET_OK = 0;

	/**
	 * Have rebuilt SAIL Daq interface in 2022, but in principle this was 
	 * just a straight rebuild of the exact same JNI code. the main purpose was to check
	 * we could still rebuild it after all these years and it seems to work. No real
	 * need though to move on from the older and trusted version. 
	 */
	private static final String SILIB = "SailDaqJNI";
//	private static final String SILIB = "SailDaqV7";
	
	/**
	 * this is the verbose level for the C code part. 
	 */
	private static final int verboseLevel = 0;

	private static final String DEVNAME = "/dev/cypress_smru0";
	
	private static final int MINJNIVERSION = 5;

	private SmruDaqSystem smruDaqSystem;

	private boolean haveLibrary;

	private boolean loadLibraryTried;
	
	/**
	 * 
	 * @return The jni library version number. 
	 */
	
	native private int jniGetVersion();
	/**
	 * Call to initialise buffers. Gets called once as
	 * soon as the dll is loaded. 
	 * @return 0
	 */
	native private int jniInitialise();

	/**
	 * Prepare a Daq card
	 * @param cardNumber hardware index of the card
	 * @param reset perform a full reset (load firmware)
	 * @return 0 on success or a -ve error code
	 */
	native private int jniPrepareDevice(int cardNumber, boolean reset);
	
	/**
	 * Set the card synch mode (when multiple cards in use)
	 * @param cardNumber hardware index of the card
	 * @param mode SMRU_VAL_STANDALONE, SMRU_VAL_MASTER or SMRU_VAL_SLAVE
	 * @return 0 on success or an error code
	 */
	native private int jniSetSynchMode(int cardNumber, int mode);
	
	/**
	 * Get the card synch mode (when multiple cards in use)
	 * @param cardNumber hardware index of the card
	 * @return SMRU_VAL_STANDALONE, SMRU_VAL_MASTER or SMRU_VAL_SLAVE
	 */
	native private int jniGetSynchMode(int cardNumber);

	/**
	 * Open the device. This function should generally NOT be called 
	 * after a jniCloseCard. Instead, use jniPrepareDevice
	 * @param device hardware index of the card
	 * @return 0 on success or an error code
	 */
	native private int jniOpenCard(int device);

	/**
	 * Close a Daq card. 
	 * @param device hardware index of the card
	 * @return 0 on success or an error code
	 */
	native private int jniCloseCard(int device);
	
//	/**
//	 * Reset a Daq card. 
//	 * @param device hardware index of the card
//	 * @return 0 on success or an error code
//	 */
//	native private int jniResetCard(int device);
	

	native private int jniFlushCard(int device);
	/**
	 * Set an LED status. 
	 * @param device hardware index of the card
	 * @param led led number (0 or 1)
	 * @param state led state 0=off, 1=on
	 * @return 0 on success or an error code
	 */
	native private int jniSetLED(int device, int led, int state);

	/**
	 * 
	 * @return the number of DAQ cards available.
	 */
	native private int jniGetUsbDevices();
	
	/**
	 * Set the print verbosity (debug output) of the Daq system.
	 * 0 = no print out, 3 = lots 
	 * @param verbose verbose level
	 */
	native private void jniSetVerbose(int verbose);
	
	/**
	 * 
	 * @return the set verbose level
	 */
	native private int jniGetVerbose();
	
	/**
	 * 
	 * @param device hardware index of the card
	 * @return The device serial number
	 */
	native private long jniGetDeviceSerialNumber(int device);

	/**
	 * 
	 * @param device hardware index of the card
	 * @param led led number (0 or 1)
	 * @return led state (0 or 1)
	 */
	native private int jniGetLED(int device, int led);

	/**
	 * Set a bitmap of channels to read 0 to F.
	 * @param device hardware index of the card
	 * @param mask channel bitmap (0 - F)
	 * @return 0 on success
	 */
	native private int jniSetChannelMask(int device, int mask);

	/**
	 * Set the sample rate index for the device
	 * @param device hardware index of the card
	 * @param sampleRateIndex 0-3 for sample rates 62500, 250000, 500000, 1000000Hz
	 * @return 0 on success
	 */
	native private int jniSetSampleRateIndex(int device, int sampleRateIndex);

	/**
	 * 
	 * @param device hardware index of the card
	 * @param channel channel number (0-3)
	 * @param gainIndex gain index 0-7 for linear gains of 0, 1, 2, 4, 8, 16, 32, 64x
	 * @param filterIndex filter index 0-4 for high pass filters at 10., 100., 2000., 20000., 0. Hz
	 * @return 0 on success
	 */
	native private int jniPrepareChannel(int device, int channel, int gainIndex, int filterIndex);
	
	/**
	 * Start the device
	 * @param device hardware index of the card
	 * @return 0 on success
	 */
	native private int jniStartSystem(int device);
	/**
	 * Stop the device
	 * @param device hardware index of the card
	 * @return 0 on success
	 */
	native private int jniStopSystem(int device);

	/**
	 * Query whether a system has stopped. 
	 * @param device hardware index of the card
	 * @return 
	 */
	native private int jniSystemStopped(int device);

	/**
	 * Query the status of a card
	 * @param device hardware index of the card
	 * @return 0 if running, 1 otherwise
	 */
	native private int jniGetRunStatus(int device);
	
	/**
	 * Read samples from the jni buffers for a single channel
	 * @param device  hardware index of the card
	 * @param channel channel (0 - 3)
	 * @param samples number of samples to read
	 * @return array of int16 data samples. 
	 */
	native private short[] jniReadSamples(int device, int channel, int samples);

	/** 
	 *
	 * Get the number of available samples for a single channel.
	 * When acquiring data, this should be periodically called
	 * to ensure that the buffers are not over filling, i.e. the
	 * value returned from this does not approach the value
	 * returned by sailGetChannelBufferSize.
	 *
	 * @param device Device number
	 * @param channel Channel number (0 - 3)
	 *
	 * @return the number of available samples.
	 * @see jniGetChannelBufferSize
	 */
	private native int jniGetAvailableSamples(int device, int channel);
	
	/** 
	 *
	 * Get the buffer size for each channel being read. This is
	 * currently a fixed value.
	 *
	 * @return the size of the data buffer for each channel in samples.
	 */
	private native int jniGetChannelBufferSize();
	
	/**
	 *
	 * Set the buffer size for each channel being read. Note that this
	 * call does not actually allocate memory. Memory allocation occurs
	 * when sailStartSystem is called
	 *
	 * @param bufferSamples the size of the data buffer for each channel in samples.
	 * @return 0 if successful, 1 if the system is running and the buffer
	 * cannot be set.
	 */
	private native int jniSetChannelBufferSize(int bufferSamples) ;
	
	private long[] serialNumbers = new long[SmruDaqParameters.MAX_DEVICES];

	private int nDevices;

	private int[] boardOrder;
	private boolean[] boardOpen = new boolean[SmruDaqParameters.MAX_DEVICES];
	private int libVersion = -1;
	
	public static final int RESET_NONE = 0;
	public static final int RESET_AUTO = 1;
	public static final int RESET_FULL = 2;
	
	public static final int ERROR_SUCCESS = 0;

	public SmruDaqJNI(SmruDaqSystem smruDaqSystem) {
		super();
		this.smruDaqSystem = smruDaqSystem;
		loadLibrary();
		if (haveLibrary()) {
			setVerbose(verboseLevel);

			/**
			 * List the devices, but don't do any resetting and 
			 * also leave them in a closed state so other programmes 
			 * can access them. 
			 */
			for (int i = 0; i < 2; i++) {
				/*
				 * for some reason, the second time the cards are opened
				 * they often fail to read their sn / start correctly. 
				 * This only happens after the cards have been running and 
				 * PAMGuard has restarted. And yes, it is only the first call 
				 * got get the serial number after the second call to prepareCard !
				 * No idea why this is, but am for now dealing with it by 
				 * double calling listDevices and double checking on the
				 * serial number.   
				 */
//				System.out.println("List devices " + i);
				listDevices();
			}
		}
	}

	/**
	 * Called when PAMGuard starts / creates an acquisition module
	 * to list attached devices. 
	 * @return number of devices found. 
	 */
	private int listDevices() {
		if (haveLibrary == false) {
			return -1;
		}
		nDevices = getNumDevices();
		if (nDevices > SmruDaqParameters.MAX_DEVICES) {
			nDevices = SmruDaqParameters.MAX_DEVICES;
		}
		smruDaqSystem.terminalPrint("Number of SAIL DAQ cards found = " + nDevices, 1);
		for (int i = 0; i < nDevices; i++) {
			int ok = jniPrepareDevice(i, false);
			if (ok != SMRU_RET_OK) {
				smruDaqSystem.terminalPrint(String.format("Opening SAIL DAQ card %d returned code %d", i, ok), 1);
				continue;
			}
//			jniResetCard(i); // might help !
//			int fAns = jniFlushCard(i);
//			smruDaqSystem.terminalPrint(String.format("Flush card %d returned %d", i, fAns), 0);
			/*
			 * This is the call which seems to hang, so hammer it !
			 */
			int nSN = 10;
			float[] tSN = new float[nSN];
			for (int t = 0; t < nSN; t++) {
				long n = System.nanoTime();
				long newSN = readDeviceSerialNumber(i);
				tSN[t] = ((float) (System.nanoTime()-n)) / 1.0e6f;
				if (newSN == 0 || newSN !=serialNumbers[i] || tSN[t]>200) {
					smruDaqSystem.terminalPrint(String.format("Opening SAIL DAQ card %d with sn 0x%X call %d in %3.4fms", 
							i, newSN, t, tSN[t]), 1);
					serialNumbers[i] = newSN;
				}
				else {
					break;
				}
//				serialNumbers[i] = newSN;
			}
			jniCloseCard(i);
			boardOpen[i] = false;
		}
		sortBordList(nDevices);
		return nDevices;
	}
	
	/**
	 * If any devices have failed to obtain a serial number, 
	 * try giving them a more serious reset. 
	 * @return
	 */
	boolean checkDevices(boolean all) {
		boolean ok = true;
		for (int i = 0; i < nDevices; i++) {
			if (serialNumbers[i] <= 0 || all) {
				serialNumbers[i] = resetBoard(i, false);
			}
		}
		sortBordList(nDevices);
		return ok;
	}
	
	private void sortBordList(int nDevices) {
		long[] usedArray = Arrays.copyOf(serialNumbers, nDevices);
		boardOrder = new int[SmruDaqParameters.MAX_DEVICES];
		// put some dum numbers in so that boards with no sn maintain their order.
		for (int i = 0; i < nDevices; i++) {
			if (usedArray[i] == 0) usedArray[i] = i;
		}
		BubbleSort.sortAcending(usedArray, boardOrder);		
	}
	
	/**
	 * 
	 * @param iBoard board number (hardware index). 
	 * @param fullReset also reload the firmware onto the board. 
	 * @return serial number. This will be zero if the reset failed. 
	 */
	long resetBoard(int iBoard, boolean fullReset) {
		if (haveLibrary == false) return 0;
		jniCloseCard(iBoard);
//		jniResetCard(iBoard);
		int ok = prepareDevice(iBoard, fullReset);
		smruDaqSystem.terminalPrint("In resetBoard with fullRest = " + fullReset, 1);
		serialNumbers[iBoard] = readDeviceSerialNumber(iBoard);
		if (serialNumbers[iBoard] != 0) {
			// resort the boards according to serial number. 
			long[] usedArray = Arrays.copyOf(serialNumbers, nDevices);
			boardOrder = new int[SmruDaqParameters.MAX_DEVICES];
			BubbleSort.sortAcending(usedArray, boardOrder);
		}
		return serialNumbers[iBoard];
	}

	@Override
	protected void finalize() throws Throwable {
		smruDaqSystem.terminalPrint("Finalise Daq JNI - close card", 3);
		nDevices = getNumDevices();
		for (int i = 0; i < nDevices; i++) {
			closeCard(i);
		}
	}

	/**
	 * Load the jni library
	 */
	private void loadLibrary() {
		if (loadLibraryTried == false) {
			try  {
				System.loadLibrary(SILIB);
				int v = jniGetVersion();
				if (v < MINJNIVERSION) {
					System.out.println("Your SAIL interface drivers are out of date and may not function correctly");
					System.out.println("Please contact the manufacturer for an update.");
					return;
				}
				haveLibrary = true;
				libVersion = v;
				jniInitialise();
			}
			catch (UnsatisfiedLinkError e)
			{
				System.out.println("SAIL DAQ Card lib '" + SILIB + "' cannot be loaded for the following reason:");
				System.out.println("Unsatisfied Link Error : " + e.getMessage());
				haveLibrary = false;
			}
		}
		loadLibraryTried = true;
	}

	/**
	 * 
	 * @return true if jni library loaded ok
	 */
	private boolean haveLibrary() {
		return haveLibrary;
	}
	
	/**
	 * Get the serial number for the device. Since at 
	 * this point we can't sort the devices, this uses 
	 * the un ordered device index. 
	 * @param device (hardware index)
	 * @return serial number of 0 if call fails. 
	 */
	protected long readDeviceSerialNumber(int device) {
		if (!haveLibrary()) {
			return 0;
		}
		return jniGetDeviceSerialNumber(device);
	}

	/**
	 * Get the number of available devices. 
	 * @return
	 */
	public int getNumDevices() {
		if (!haveLibrary()) {
			return 0;
		}
		return jniGetUsbDevices();
	}
	
	/**
	 * Set the verbose level of the jni software
	 * @param verbose  0=very little printing, 3 = lots of printing
	 */
	public void setVerbose(int verbose) {
		if (haveLibrary && libVersion >= 3) {
			jniSetVerbose(verbose);
		}
		else {
			System.out.println("Unable to set verbose level for Daq card - library not loaded or out of date");
		}
	}
	
	/**
	 * get the verbose level of the jni software
	 * 
	 * @return 0=very little printing, 3 = lots of printing
	 */
	public int getVerbose() {
		if (haveLibrary && libVersion >= 3) {
			return jniGetVerbose();
		}
		else {
			System.out.println("Unable to get verbose level for Daq card - library not loaded or out of date");
			return -1;
		}
	}
	
	/**
	 * Swap an led status
	 * @param board (software number)
	 * @param led 0 or 1
	 * @return  = off, 1 = on
	 */
	public int toggleLED(int board, int led) {
		board = boardOrder[board];
		int state = getLED(board, led);
		System.out.println("state="+state);
		if (state == 0) {
			state = 1;
		}
		else {
			state = 0;
		}
		setLED(board, led, state);
		return getLED(board, led);
	}
	/**
	 * Set the led status
	 * @param board (software number)
	 * @param led 0 or 1
	 * @return  = off, 1 = on
	 */
	public int setLED(int board, int led, int state) {
		if (!haveLibrary()) {
			return 0;
		}
		return jniSetLED(boardOrder[board], led, state);
	}

	/**
	 * Get the led status
	 * @param board (software number)
	 * @param led 0 or 1
	 * @return  = off, 1 = on
	 */
	public int getLED(int board, int led) {
		if (!haveLibrary()) {
			return 0;
		}
		return jniGetLED(boardOrder[board], led);
	}

	/**
	 * This function will also load the card information and 
	 * open the card - so jni code keeps those handles 
	 * and keeps on re-using them. 
	 * @param board (hardware index) N.B This is one of
	 * few functions which uses raw board (hardware) number
	 * and not the ordered lookup of board numbers (software number). 
 	 * @param reset
	 * @return 0 on succes
	 */
	protected int prepareDevice(int board, boolean reset) {
		if (!haveLibrary()) {
			return Integer.MIN_VALUE;
		}
		smruDaqSystem.terminalPrint("In prepare device with reset = " + reset, 1);
		int ans = jniPrepareDevice(board, reset);
		boardOpen[board] = (ans == 0);
		return ans;
	}
	
	/**
	 * 
	 * Set a board synch mode. 
	 * @param board board number (software index)
	 * @param mode synch mode (0,1,2,3)
	 * @return SMRU_RET_OK or -MinVal if an error
	 */
	public int setSynchMode(int board, int mode) {
		if (!haveLibrary()) {
			return Integer.MIN_VALUE;
		}
		return jniSetSynchMode(boardOrder[board], mode);
	}

	/**
	 * Get a boards synch mode
	 * @param board board number (software index)
	 * @return synch mode or -something if an error
	 */
	public int getSynchMode(int board) {
		if (!haveLibrary()) {
			return Integer.MIN_VALUE;
		}
		return jniGetSynchMode(boardOrder[board]);
	}
//	/**
//	 * Open the daq card
//	 * @param board (software index)
//	 * @return 0 if no error
//	 */
//	public int openCard(int board) {
//		if (!haveLibrary()) {
//			return -1;
//		}
//		return jniOpenCard(boardOrder[board]);
//	}
	/**
	 * Close the daq card
	 * @param board (software index)
	 * @return 0 on Success
	 */
	public int closeCard(int board) {
		if (!haveLibrary()) {
			return -1;
		}
		boardOpen[getBoardOrder(board)] = false;
		return jniCloseCard(boardOrder[board]);
	}
	
	/**
	 * 
	 * @param boardHardNo board hardware number
	 * @return true if board is open
	 */
	public boolean isboardOpen(int boardHardNo) {
		return boardOpen[boardHardNo];
	}
	
//	/**
//	 * Reset the daq card
//	 * @param board (software index)
//	 * @return true if Ok
//	 */
//	public boolean resetCard(int board) {
//		/**
//		 * all this does is call stop - not worth having. 
//		 */
//		if (!haveLibrary()) {
//			return false;
//		}
//		return (jniResetCard(boardOrder[board])==0);
//	}
	/**
	 * Set the daq card sample rate
	 * @param board (software index)
	 * @param sampleRateIndex sample rate index
	 * @return true if OK
	 */
	public boolean setSampleRateIndex(int board, int sampleRateIndex) {
		if (!haveLibrary()) {
			return false;
		}
		return (jniSetSampleRateIndex(boardOrder[board], sampleRateIndex) == 0);
	}
	/**
	 * Set mask of channels to read. 
	 * @param board (software index)
	 * @param channelMask mask of channels to use 0 - F
	 * @return true if OK
	 */
	public boolean setChannelMask(int board, int channelMask) {
		if (!haveLibrary()) {
			return false;
		}
		return (jniSetChannelMask(boardOrder[board], channelMask) == 0);
	}

	/**
	 * Sets the gain and filters for a channel
	 * @param board board number (software index)
	 * @param channel channel number
	 * @param gainIndex gain index
	 * @param filterIndex filter index
	 * @return true if Ok
	 */
	public boolean prepareChannel(int board, int channel, int gainIndex, int filterIndex) {
		if (!haveLibrary()) {
			return false;
		}
		return (jniPrepareChannel(boardOrder[board], channel, gainIndex, filterIndex) == 0);
	}
	
	/**
	 * Start the daq card
	 * @param board (software index)
	 * @return
	 */
	public boolean startSystem(int board) {
		if (!haveLibrary()) {
			return false;
		}
		return (jniStartSystem(boardOrder[board]) == 0);
	}
	
	/**
	 *
	 * Stop data acquisition on a DAQ card.
	 * @param device Device number
	 * @return true if successful
	 */
	public boolean stopSystem(int board) {
		if (!haveLibrary()) {
			return false;
		}
		try {
			return (jniStopSystem(boardOrder[board]) == 0);
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 *
	 * Can be called once the DAQ has been stopped.
	 * Will free allocated memory buffers. <br>
	 * with multi-thread applications ensure that
	 * once this has been called, no other thread
	 * attempts calls to sailReadSamples or an
	 * error will occur.
	 *
	 * @param device Device number
	 * @return always 0.
	 * @see readSamples
	 */
	public boolean systemStopped(int board) {
		if (!haveLibrary()) {
			return false;
		}
		return (jniSystemStopped(boardOrder[board]) == 0);
	}
	/**
	 * Read samples from a daq card
	 * @param board (software index)
	 * @param channel channel number
	 * @param nSamples number of samples to read. 
	 * @return
	 */
	public short[] readSamples(int board, int channel, int nSamples) {
		if (!haveLibrary()) {
			return null;
		}
		return jniReadSamples(boardOrder[board], channel, nSamples);
	}

	/**
	 *
	 * @return the number of installed SAIL Daq cards
	 */
	public int getnDevices() {
		return nDevices;
	}

	/**
	 * Get a cards serial number. This is a 64 bit integer
	 * number which is unique to every card.
	 * @param device Device number
	 * @return serial number or 0 if the card is not present / cannot be initialised.
	 */
	public long getSerialNumber(int boardId) {
		return serialNumbers[boardOrder[boardId]];
	}

	/**
	 * Convert a software index to a hardware index. 
	 * Get the true order of the board in the device LUT from 
	 * the board order the users sees which was sorted by serial number 
	 * @param boardId (software index)
	 * @return board number (hardware index)
	 */
	public int getBoardOrder(int boardId) {
		if (boardOrder == null || boardId >= boardOrder.length || boardId < 0) {
			return -1;
		}
		return boardOrder[boardId];
	}
	

	/** 
	 *
	 * Get the number of available samples for a single channel.
	 * When acquiring data, this should be periodically called
	 * to ensure that the buffers are not over filling, i.e. the
	 * value returned from this does not approach the value
	 * returned by sailGetChannelBufferSize.
	 *
	 * @param device Device number
	 * @param channel Channel number (0 - 3)
	 *
	 * @return the number of available samples.
	 * @see jniGetChannelBufferSize
	 */
	public int getAvailableSamples(int device, int channel) {
		if (haveLibrary == false) {
			return -1;
		}
		return jniGetAvailableSamples(device, channel);
	}	
	
	/** 
	 *
	 * Get the buffer size for each channel being read. This is
	 * currently a fixed value.
	 *
	 * @return the size of the data buffer for each channel in samples.
	 */
	public int getChannelBufferSize() {
		if (haveLibrary == false) {
			return -1;
		}
		return jniGetChannelBufferSize();		
	}	
	
	/**
	 *
	 * Set the buffer size for each channel being read. Note that this
	 * call does not actually allocate memory. Memory allocation occurs
	 * when sailStartSystem is called
	 *
	 * @param bufferSamples the size of the data buffer for each channel in samples.
	 * @return 0 if successful, 1 if the system is running and the buffer
	 * cannot be set.
	 */
	public int setChannelBufferSize(int bufferSamples) {
		if (haveLibrary == false) {
			return -1;
		}
		return jniSetChannelBufferSize(bufferSamples);
	}
		
	/**
	 * Flash board LED's. 
	 * @param board board software number
	 * @param i number of flashes. 
	 */
	public boolean flashLEDs(int board, int nFlashes) {
		/**
		 * If the card is closed, it will be opened. 
		 * If it gets opened, it will be closed again at the end of the function
		 */
		int hardId = getBoardOrder(board);
		boolean wasOpen = isboardOpen(hardId);
		if (wasOpen == false) {
			smruDaqSystem.terminalPrint("Opening card to flash board " + board, 2);
			boolean isOpen = prepareDevice(hardId, false) == 0;
			if (isOpen == false) {
				return false;
			}
		}
		for (int i = 0; i < nFlashes; i++) {
			setLED(board, 0, i%2);
			setLED(board, 1, 1-(i%2));
			try {
				Thread.sleep(250);
			} catch (InterruptedException e1) {
				break;
			}
		}
		setLED(board, 0, 0);
		setLED(board, 0, 0);
		if (wasOpen == false) {
			smruDaqSystem.terminalPrint("Closing card after flash board " + board, 2);
			closeCard(board);
		}
		return true;
		
	}
	
	/**
	 * Get the dll library name. 
	 * @return
	 */
	public String getLibrary() {
		return SILIB;
	}
	
	/**
	 * Get the version of the dll library. 
	 * @return
	 */
	public int getLibarayVersion() {
		return libVersion;
	}


}
