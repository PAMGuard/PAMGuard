package cpod;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import PamUtils.PamArrayUtils;
import PamUtils.PamCalendar;


/**
 * Functions for importing FPOD files.
 * <p>
 * Note this class should be independent of any PAMGuard functionality. 
 * <p>
 * Although some anming has changed a lot of the variable names are consistent with Pascal code
 * used in FPOD.exe software from whihc this is absed. 
 * 
 * @author Jamie Macaulay
 */
public class FPODReader {

	/**
	 * Look up array to convert byte values to linear sound level
	 */
	private static int[] LinearPkValsArr;

	/**
	 * Look up array to convert byte values to linear sound level if using extended amps. 
	 */
	private static int[][] ClippedPkArr;


	/**
	 * Look up sine array fro reconstructing waveforms. 
	 */
	private static double[] SineArr;

	/**
	 * Look up sine array for converting IPI (inter-pulse-interval) to kHz values
	 */
	private static int[] IPItoKHZ = new int[257];

	/**
	 * Length of the FPOD header in bytes. 
	 */
	public static final int FPOD_HEADER = 1024;


	/**
	 * The length of a standard FPOD entry
	 */
	public static final int FP1_FPOD_DATA_LEN = 16;


	/**
	 * The click length for FP3 files. 
	 */
	public static final int FP3_FPOD_DATA_LEN = 32;

	/**
	 * Scale factor to convert waveform measurements to PAMGuard -1 to 1 measurements.
	 */
	public static final double WAV_SCALE_FACTOR = 255.;


	public static final float FPOD_WAV_SAMPLERATE = 1000000;


	/**
	 * Import an FPOD file. 
	 * @param cpFile - the FP1 file. 
	 * @param from - the click index to save from. e.g. 100 means that only click 100 + in the file is saved
	 * @param maxNum - the maximum number of data units to import. 
	 * @return the total number of clicks in the file. 
	 * @throws IOException 
	 */
	public static int importFile(File cpFile, ArrayList<FPODdata> fpodData, int from, int maxNum ) throws IOException {

		populateRawToRealPkArrays();
		populateIPIArray();

		BufferedInputStream bis = null;
		int bytesRead;
		FileInputStream fileInputStream = null;
		int totalBytes = 0;
		try {
			bis = new BufferedInputStream(fileInputStream = new FileInputStream(cpFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
		FPODHeader header = new FPODHeader();
		if (readHeader(bis, header) != FPOD_HEADER) {
			return 0;
		}

		int fileEnds = 0;
		boolean isClick;
		FPODdata fpodClick;
		// first record is always a minute mark, so start
		// at -1 to avoid being skipped forward one minute.
		int nClicks = 0, nMinutes = -1;
		int nWavClicks = 0;

		//keep track of wav data
		FPODWavData wavData = null;
		
		//holds a map of the click train detections. 
		HashMap<Integer, CPODClassification> clickTrains = new 	HashMap<Integer, CPODClassification>();
		
		//the click train of the current data unit. 
		CPODClassification cpodClassification =  null;
		
		int wavbufpos = 0;
		try {
			while (true) {

				fpodClick = new FPODdata();

				byte[] bufPosN = new byte[FP1_FPOD_DATA_LEN];

				int byteRead = bis.read(bufPosN, 0, FP1_FPOD_DATA_LEN);
				if (byteRead!=FP1_FPOD_DATA_LEN) {
					System.out.println("Total len: " + totalBytes);
					break;
				}

				//				ftFP3  is FP3 file in Padcal?
				//				ftFP2  is FP2 file in Padcal?
				//				ftFP1 is FP1 file

				totalBytes+=byteRead;

				isClick = toUnsigned(bufPosN[0]) < 184;

				if (isClick) {
					// time within minute in 5 microsecond units
					fpodClick.FiveMusec = ((bufPosN[0] & 0xFF) << 16) | // Shift first byte 16 bits to the left
							((bufPosN[1] & 0xFF) << 8) | // Shift second byte 8 bits to the left
							(bufPosN[2] & 0xFF); // Leave third byte as is

					//N of cycles in the click - up to 255cycles
					fpodClick.Ncyc = toUnsigned(bufPosN[3]); //number of cycles 


					//Wavenumber of loudest cycle; range of IPIs in click
					fpodClick.PkAt = (bufPosN[4] & 0xF0) >> 4;

							// decompress the click IPI values
							if ((bufPosN[4] & 0xF) == 15) {
								fpodClick.ClkIPIrange = 65; // Since > 64 isn't directly assignable, assign 65 instead.
							} else if ((bufPosN[4] & 0x8) == 8) {
								fpodClick.ClkIPIrange = (((bufPosN[4] & 0x7) + 1) << 3);
							} else {
								fpodClick.ClkIPIrange = (bufPosN[4] & 0x7);
							}

							// IPI of Pk-1
							fpodClick.IPIpreMax = toUnsigned(bufPosN[5]) + 1; // Increment byte at BufPosn[5] and assign to
							// NrClk.IPIpreMax

							// IPI of Pk this is the loudest cycle in the click
							fpodClick.IPIatMax = toUnsigned(bufPosN[6]) + 1; // Increment byte at BufPosn[6] and assign to
							// NrClk.IPIatMax

							int localIPIatMax;
							if (header.FPGAcodeVersion > 801) {
								localIPIatMax = fpodClick.IPIatMax;
							} else {
								localIPIatMax = fpodClick.IPIpreMax;
							}

							if (bufPosN[7] > 0) {
								// IPI of Pk+1
								fpodClick.IPIplus1 = toUnsigned(bufPosN[7]) + 1;
								if (bufPosN[8] > 0) {
									// IPI of Pk+1
									fpodClick.IPIplus2 = toUnsigned(bufPosN[8]) + 1;
								}
							}

							// now on to amplitudes
							if (bufPosN[9] > 0) {
								// Amplitude of P-1
								fpodClick.RawPkminus1 = toUnsigned(bufPosN[9]);

								fpodClick.Pkminus1Extnd = RawToRealPk(toUnsigned(bufPosN[9]), localIPIatMax,
										header.HasExtendedAmps);
							}

							fpodClick.MaxPkRaw = (short) Math.max(2, toUnsigned(bufPosN[10]));
							//							fpodClick.MaxPkLinear = RawToLinearPk(toUnsigned(bufPosN[10]));
							fpodClick.MaxPkExtnd = RawToRealPk(toUnsigned(bufPosN[10]), localIPIatMax,
									header.HasExtendedAmps);

							if (toUnsigned(bufPosN[11]) > 0) {
								fpodClick.RawPkplus1 = toUnsigned(bufPosN[11]);
								fpodClick.Pkplus1Extnd = RawToRealPk(toUnsigned(bufPosN[11]), localIPIatMax,
										header.HasExtendedAmps);
							}

							fpodClick.IPIbefore = toUnsigned(bufPosN[12]) + 1;
							fpodClick.AmpReversals = bufPosN[13] & 15;

							// Defer MSnibble[13][14] until [15] has been read
							if (header.FileType == 2) {
								//								fpodClick.Clstr.cNall = toUnsigned(bufPosN[15]); //not implemented in PAMGuard
							} else if (toUnsigned(bufPosN[15]) < 2) { // POD marks where a sonar has been found
								fpodClick.EndIPI = fpodClick.IPIpreMax;
							} else {
								//fpodClick.HasWave = (bufPosN[15] & 1) == 1;
								fpodClick.EndIPI = bufPosN[15] & 254 + 1; // + 1 on all IPIs because counts from the POD start at
							}

							//the duration is in 5us units. 
							fpodClick.duration =  ((bufPosN[13] & 240)*16 + toUnsigned(bufPosN[14]));

							///rm...can't exactly explain this but it's translated from FPOD Pascal code - calculates bandwidth
							int ampDfSum = Math.abs(fpodClick.Pkminus1Extnd - fpodClick.MaxPkRaw);
							int ampSum = Math.round((fpodClick.Pkminus1Extnd + fpodClick.MaxPkRaw) / 2);
							int ipIdfSum = Math.abs(fpodClick.IPIpreMax - fpodClick.IPIatMax);
							int ipISum = fpodClick.IPIatMax;

							if (fpodClick.Pkplus1Extnd > 0) {
								ampDfSum += Math.abs(fpodClick.Pkplus1Extnd - fpodClick.MaxPkRaw);
								ampSum += Math.round((fpodClick.Pkplus1Extnd + fpodClick.MaxPkRaw) / 2);
								ipIdfSum += Math.abs(fpodClick.IPIatMax - fpodClick.Pkplus1Extnd);
								ipISum += fpodClick.Pkplus1Extnd;
							}

							//set the nadwidth
							fpodClick.BW = Math.max(0, Math.min(11, Math.round(ipIdfSum << 4 / ipISum)) + Math.min(20, Math.round((ampDfSum + (ampDfSum >> 1)) << 3 / ampSum) - 1));


							//							if (fpodClick.HasWave) {
							//								System.err.println("Pod data: " + nClicks + "  " + fpodClick.FiveMusec + "  hasWav: " + fpodClick.HasWave);
							//							}

							//set the time in millis. 

							long timeMillis = (long) (CPODUtils.podTimeToMillis(header.FirstLoggedMin) + (nMinutes*60*1000.) + fpodClick.FiveMusec/200);
							fpodClick.setTimeMillis(timeMillis);
							fpodClick.HasWave = false;


							/**
							 * 
							 * Set the raw (sort of - actually peak positions and IPI) waveform data 
							 * 
							 * Note that the wav data is recorded before the click - this is why this 
							 * section of code is here. A non null wavData object indicates that wav data has been processed
							 * and it must belong to this click. We then set wavData to null indicated there is no wav data 
							 * until the wavData is again present. 
							 */
							if (wavData!=null) {

								fpodClick.HasWave = true;
								//							fpodClick.setNWavRecs(0);
								wavData.setClickCyclesStartAt(21 - fpodClick.Ncyc);

								for (int count = wavData.getWavPtr(); count >= 1; count--) {
									wavData.setWavValsIPI((short) 255, count);
									wavData.setWavValsSPL((short) 0, count);
								}

								fpodClick.setWavData(wavData); 
								//								int[] wave = makeResampledWaveform(fpodClick);

								//Test clicks
								//								if (fpodClick.getTimeMillis()==1689706702252L) {//porpoise click
								//								if (fpodClick.getTimeMillis()==	1689615091621L) {
								//								int[] wave = makeResampledWaveform(fpodClick);
								//								System.out.println("WAV DATA " + nClicks + "  " + fpodClick.getWavData().getNWavRecs() + "  " + wavData.getWavPtr());
								//								
								//								System.out.println(" Peaks: ");
								//								for (int i = 0; i< wavData.getWavValsSPL().length; i++) {
								//									System.out.print(" " + wavData.getWavValsSPL()[i]);
								//								}
								//								
								//								System.out.println();
								//
								//								System.out.println(" Waveform: ");
								//								for (int i = 0; i< wave.length; i++) {
								//									System.out.print(" " + wave[i]);
								//								}
								//								return 0;
								//								}

								//								fpodClick.getWavData();


								//does this set the reference to null - NOPE;
								wavData = null;

							}
							
							/**
							 * Set the classification (if FP3 file)
							 */
							if (clickTrains!=null) {
								fpodClick.setClassification(cpodClassification);
							}

							fpodClick.setMinute(nMinutes);

							if (from<0 || (nClicks>from && nClicks<(from+maxNum))) {
								//add the click to the FPODdata.
								fpodData.add(fpodClick);
							}


							if (nClicks%400000==0) {
								System.out.println("Pod data: " + nClicks + "  " + PamCalendar.formatDateTime(fpodClick.getTimeMillis()) + "  " +fpodClick.dataString() + "  "  +toUnsigned(bufPosN[13]) + "  "  + toUnsigned(bufPosN[14]));
							}

							nClicks++;


				}
				else if (toUnsigned(bufPosN[0])==250) {
					
					//wav data preceedes the next click

					if (wavData==null) {
						//starting a new wav record
						wavData = new FPODWavData(); 
						wavData.setNWavRecs(0);
						wavData.setWavPtr(21);
						wavbufpos = 0;
						nWavClicks++;
					}

					//the block has already been read...
					//				        if (Fs[FN].isInPlay() && bufpos >= Fs[FN].getNumBytesRead()) {
					//				            readNextBlock(); // Assumes you have a method for reading the next block
					//				        }
					if (toUnsigned(bufPosN[wavbufpos]) == 250) { //a little redundant but keep in just incase
						wavData.setNWavRecs(wavData.getNWavRecs() + 1);
						if (wavData.getNWavRecs() < 4) {
							int posn = wavData.getWavPtr();
							for (int count = 0; count <= 6; count++) {
								wavData.setWavValsIPI(toUnsigned((bufPosN[wavbufpos + (count << 1) + 1])), posn);
								wavData.setWavValsSPL(toUnsigned((bufPosN[wavbufpos + (count << 1) + 2])), posn);
								posn--;
							}
							wavData.setWavPtr(wavData.getWavPtr() - 7);
						}
					}

					//						JamArr.printArray(fpodClick.getWavValsSPL());
					//wav data
					nWavClicks++;
				}
				
				else if(toUnsigned(bufPosN[0])==249) {
					//click train data - this is not included - for now
					//click train data precedes the next click
					
					//the train ID is unique to the minute, 
					short trainID = toUnsigned(bufPosN[15]);
										
					//1 is NBHF
					//2 is dolphin
					//3 is uncalssified
					//4 is sonar?
					short species = (short) ((bufPosN[14] >>> 2) & 3);
		
					//quality level for the click train
					short qualitylevel = (short) ((bufPosN[14]) & 3);

					boolean echo = false;
					if ((bufPosN[14] & 32) == 32) {
					    echo = true;
					}
					
	
					//generate a unique train ID within the file			
					int trainUID = Integer.valueOf(String.format("%06d", nMinutes) + String.format("%d", trainID));
					
					//find the click train from the hash map - if it is not there, create a new one. 
					 cpodClassification = clickTrains.get(trainUID);
					
					if (cpodClassification==null) {
						cpodClassification = new CPODClassification();
						cpodClassification.isEcho = echo;
						cpodClassification.clicktrainID = trainUID;
						cpodClassification.species = CPODUtils.getSpecies(species); 
						

						clickTrains.put(trainUID, cpodClassification); 
						
						//System.out.println("Click train ID: " + trainUID + " minutes: " + nMinutes + " species: " + species + " quality level: " + qualitylevel);

					}

					
				}
				else if(toUnsigned(bufPosN[0])==254){
					nMinutes ++;
				}

			}

			System.out.println("Number clks " + nClicks + " nWav: " + nWavClicks + " minutes: " + nMinutes);
			bis.close();
			return totalBytes;

		} catch (IOException e) {
			e.printStackTrace();
			return totalBytes;
		}
	}


	/**
	 * Convert a raw binary peak to true linear peak. 
	 * @param Pk - the raw maximum peak
	 * @param IPI - the inter pulse interval
	 * @param UseExtendedAmps - true to use extended amps
	 * @return the real linear peak of he wave
	 */
	public static int RawToRealPk(int Pk, int IPI, boolean UseExtendedAmps) {
		if (Pk == 0) {
			return 1;
		}
		if (IPI < 10) { // or (IPI > 256)
			return LinearPkValsArr[Pk];
		}
		return UseExtendedAmps && (Pk > 222) ? ClippedPkArr[Pk][IPI - 1] : LinearPkValsArr[Pk];
	}


	/**
	 * Populate the arrays used for calculatung true peaks
	 */
	private static void populateRawToRealPkArrays() {
		final int constMaxAmpKHZScaler = 50;
		final int constMinPkampAllowed = 384;  // (224 - 128) shl 2;

		int count, IPI, val, MaxPkampAllowed, Pk;
		boolean MaxExceeded;
		int[] RiseTimeConversionArr = new int[256 - 223 + 1];  // Initialized for indices 223 to 255

		LinearPkValsArr = new int[256];

		// Populate LinearPkValsArr
		for (count = 0; count <= 127; count++) {
			LinearPkValsArr[count] = count;
		}
		for (count = 128; count <= 191; count++) {
			LinearPkValsArr[count] = (count - 64) << 1;
		}
		for (count = 192; count <= 255; count++) {
			LinearPkValsArr[count] = (count - 128) << 2;
		}



		// Populate RiseTimeConversionArr
		for (Pk = 223; Pk <= 255; Pk++) {
			if (Pk < 232) {
				RiseTimeConversionArr[Pk - 223] = 32 + (231 - Pk) * 4;
			} else if (Pk < 240) {
				RiseTimeConversionArr[Pk - 223] = 16 + (239 - Pk) * 2;
			} else {
				RiseTimeConversionArr[Pk - 223] = 255 - Pk;
			}
		}

		ClippedPkArr = new int[256][256];

		// Populate ClippedPkArr
		for (IPI = 10; IPI <= 256; IPI++) {
			MaxExceeded = false;
			MaxPkampAllowed = IPI * constMaxAmpKHZScaler;
			for (Pk = 223; Pk <= 255; Pk++) {
				if (!MaxExceeded) {
					val = (int) Math.round(Math.pow(4000.0 / IPI, -0.75) * Math.pow(10, 5.24 * Math.pow(RiseTimeConversionArr[Pk - 223], -0.11)));
				} else {
					val = MaxPkampAllowed;
				}
				if (val > MaxPkampAllowed) {
					val = MaxPkampAllowed;
					MaxExceeded = true;
				} else {
					val = Math.max(val, constMinPkampAllowed);
				}
				ClippedPkArr[Pk][IPI - 1] = Math.min(IPI * constMaxAmpKHZScaler, Math.max(384, val));
			}
		}
	}


	/**
	 * Convert IPI to KHz. 
	 * @param IPI - the IPI
	 * @return the kHz value
	 */
	public static int IPItoKhz(int IPI) {
		return IPItoKHZ[IPI];
	}

	/**
	 * Ppulate the IPI array 
	 */
	public static void populateIPIArray() {

		for (int count = 0; count < 16; count++) {
			IPItoKHZ[count] = 255;
		}

		for (int count = 16; count < 256; count++) {
			IPItoKHZ[count] = Math.round(4000 / count);
		}

		IPItoKHZ[64] = 63; // Smoothes an uncomfortable step here
		IPItoKHZ[256] = 1; // An 'indicative' value
	}


	/**
	 * Read the FPOD header
	 * @param bis
	 * @return the number of bytes read. 
	 * @throws IOException 
	 */
	private static int readHeader(BufferedInputStream bis, FPODHeader PODset) throws IOException {

		byte[] HdrBuf = new byte[FPOD_HEADER];
		bis.read(HdrBuf, 0, FPOD_HEADER);

		// INITS - GET F METADATA gfmd
		PODset.gain = HdrBuf[1];
		// ... (Previously translated code for PODID and PodNstr5char)

		// Assuming MinToDDMMYrstr is a function converting minutes to a DDMMYYYY string
		PODset.DateOfCalibrationStr = MinToDDMMYrstr((HdrBuf[5] << 8 | HdrBuf[6]) * 1440);

		int mainBoardNumber = HdrBuf[7] * 10000 + HdrBuf[8] * 100 + HdrBuf[9];
		PODset.MainBoardNumber = mainBoardNumber;

		PODset.PreAmpVersion = 			toUnsigned(HdrBuf[10]);
		PODset.HydrophoneVersion = 		toUnsigned(HdrBuf[11]);
		PODset.FPGAtype = 				toUnsigned(HdrBuf[12]);
		PODset.FromWavFileData = 		toUnsigned(HdrBuf[13]) != 0;  // Convert boolean value
		PODset.PaDataVersion = 1;  // xx revise this
		PODset.PICtype = 				toUnsigned(HdrBuf[14]);
		PODset.HousingType = 			toUnsigned(HdrBuf[16]);
		PODset.ReleaseTone1Min=          toUnsigned(HdrBuf[17]);
		PODset.ReleaseTone1Max=         toUnsigned(HdrBuf[18]);
		PODset.ReleaseTone2Min=         toUnsigned(HdrBuf[19]);
		PODset.ReleaseTone2Max=         toUnsigned(HdrBuf[20]);
		PODset.ReleaseTone3Min=         toUnsigned(HdrBuf[21]);
		PODset.ReleaseTone3Max=         toUnsigned(HdrBuf[22]);
		PODset.ReleaseTone4Min=         toUnsigned(HdrBuf[23]);
		PODset.ReleaseTone4Max=         toUnsigned(HdrBuf[24]);
		PODset.ReleaseTone5Min=        	toUnsigned(HdrBuf[25]);
		PODset.ReleaseTone5Max=         toUnsigned(HdrBuf[26]);
		PODset.AcRelNcyc=              	toUnsigned(HdrBuf[27]);
		PODset.AcRelSlotLength=        	toUnsigned(HdrBuf[28]);
		PODset.AcRelSlotSpacing=       	toUnsigned(HdrBuf[29]);
		PODset.RelaysSecs=              toUnsigned(HdrBuf[30]);  // xxcheck where this is
		//unused
		// these were zero
		PODset.PICcodeMajorVersion=     toUnsigned(HdrBuf[37]);

		if (PODset.PICcodeMajorVersion > 14) {
			PODset.SteeperStepping = true;
		}

		PODset.PICcodeVersionStr = String.format("%d.%d", toUnsigned(HdrBuf[37]), toUnsigned(HdrBuf[38]));
		PODset.FPGAcodeVersion = (HdrBuf[39] << 8) | HdrBuf[40];
		PODset.FPGAcodeVersionStr =String.format("%d.%d", toUnsigned(HdrBuf[39]), toUnsigned(HdrBuf[40]));

		//			// Assuming F.CurrentActivityLbl is a label component
		//			F.CurrentActivityLbl.setText("PIC code v" + PODset.PICcodeVersionStr +
		//			                             " FPGA code v" + PODset.FPGAcodeVersionStr);

		if (PODset.FromWavFileData) {
			PODset.PODID = (HdrBuf[2] >>> 24) | (HdrBuf[3] >>> 16) | (HdrBuf[4] >>> 8) | HdrBuf[5];
		}

		if (PODset.FPGAcodeVersion > 0) {
			PODset.HasExtendedAmps = true;
		}

		// SETS:
		PODset.UTCoffset = toUnsigned(HdrBuf[64]);

		switch (HdrBuf[65]) {
		case 0:
			PODset.FilterKHZ = 120;
			break;
		case 1:
			PODset.FilterKHZ = 80;
			break;
		case 2:
			PODset.FilterKHZ = 40;
			break;
		case 3:
			PODset.FilterKHZ = 20;
			break;
		}

		switch (HdrBuf[66]) {
		case 0:
			PODset.FileMinutecountLimit = 2048;
			break;
		case 1:
			PODset.FileMinutecountLimit = 8192;
			break;
		case 2:
			PODset.FileMinutecountLimit = 4096;
			break;
		case 3:
			PODset.FileMinutecountLimit = 16384;
			break;
		case 16:
			PODset.FileMinutecountLimit = 4096;
			break;
		case 17:
			PODset.FileMinutecountLimit = 8192;
			break;
		case 18:
			PODset.FileMinutecountLimit = 16384;
			break;
		case 19:
			PODset.FileMinutecountLimit = 32768;
			break;
		case 32:
			PODset.FileMinutecountLimit = 8192;
			break;  

		case 33:
			PODset.FileMinutecountLimit = 16384;
			break;
		case 34:
			PODset.FileMinutecountLimit = 32768;
			break;   
		case 35:
			PODset.FileMinutecountLimit = 65536;
			break;
		default:
			// Handle error or unexpected value
			System.out.println("FileMinutecountLimit error in reading header of file "  + ", code = " + HdrBuf[66]);
			PODset.FileMinutecountLimit = 65536; // Set a default value
		}


		PODset.MinimumCycleAmplitude =  toUnsigned(HdrBuf[67]);
		PODset.MinIPI =  		toUnsigned(HdrBuf[68]);
		PODset.MaxIPI =  		toUnsigned(HdrBuf[69]);
		PODset.MinNofCyc = HdrBuf[70] & 15;
		PODset.PeakDetectionMode = (HdrBuf[70] >> 6);
		PODset.MaxSPLforReversalCount =  toUnsigned(HdrBuf[71]);  // ww check these
		PODset.BWaddon =  		toUnsigned(HdrBuf[72]);

		PODset.StrongLimit1 = 	toUnsigned( HdrBuf[73]);  // ? rescale these to true values, as currently on non-linear scale ww
		PODset.StrongLimit2 =  	toUnsigned(HdrBuf[74]);
		PODset.StrongLimit3 =  	toUnsigned(HdrBuf[75]);
		PODset.StrongLimit4 =  	toUnsigned(HdrBuf[76]);
		PODset.UsingQtrAmpDrop = (HdrBuf[77] & 1) == 1;
		// PODset.MaxSPLforBWtest = HdrBuf[77]; add code re versions before 14 that used this.... ww
		PODset.SonarLongNcyc =  toUnsigned(HdrBuf[78]);
		PODset.SonarFiltering = toUnsigned(HdrBuf[78]) > 0;
		PODset.MaxSPLforAmpDropTest =  toUnsigned(HdrBuf[79]);
		// unused                HdrBuf[80];
		PODset.ThermalGainControl = toUnsigned(HdrBuf[81]) == 1;
		PODset.BatterySwitchLevel =  toUnsigned(HdrBuf[82]);

		PODset.WavMode = 		tWavMode(HdrBuf[83]);
		PODset.WavSNR =  		toUnsigned(HdrBuf[84]);
		PODset.WavPkAddOn =  	toUnsigned(HdrBuf[85]);
		PODset.WavMinPk =  		toUnsigned(HdrBuf[86]);
		PODset.WavMinLimit = (HdrBuf[87] << 3);
		PODset.WavTotalLimit = (HdrBuf[88] >> 2);
		PODset.WavMinICI =  	toUnsigned(HdrBuf[89]);
		PODset.WavMaxICI =  	toUnsigned(HdrBuf[90]);
		PODset.WavOKseqN =  	toUnsigned(HdrBuf[91]);
		PODset.WavMaxRawPerTrain =  toUnsigned(HdrBuf[92]);
		PODset.NoisyLevel = ( HdrBuf[93] << 8);
		PODset.QuietLevel = ( HdrBuf[94] << 8);
		PODset.NoisyMinsLimit =  toUnsigned(HdrBuf[95]);
		PODset.QuietMinsLimit =  toUnsigned(HdrBuf[96]);

		PODset.SwitchNmin = 	 toUnsigned( HdrBuf[98]);
		PODset.SwitchNmax =  	 toUnsigned(HdrBuf[100]);


		if (HdrBuf[101] < 10) {
			PODset.ReleaseAfterStr = "200" + Integer.toString(HdrBuf[101]);
		} else {
			PODset.ReleaseAfterStr = "20" + Integer.toString(HdrBuf[101]);
		}
		PODset.ReleaseAfterStr += " m" + Integer.toString(HdrBuf[102]) + " d" + Integer.toString(HdrBuf[103]) + " h" + Integer.toString(HdrBuf[104]);

		if (HdrBuf[105] < 10) {
			PODset.ReleaseOnStr = "200" + Integer.toString(HdrBuf[105]);
		} else {
			PODset.ReleaseOnStr = "20" + Integer.toString(HdrBuf[105]);
		}
		PODset.ReleaseOnStr += " m" + Integer.toString(HdrBuf[106]) + " d" + Integer.toString(HdrBuf[107]) + " h" + Integer.toString(HdrBuf[108]);

		if (HdrBuf[109] < 10) {
			PODset.StartOnStr = "200" + Integer.toString(HdrBuf[109]);
		} else {
			PODset.StartOnStr = "20" + Integer.toString(HdrBuf[109]);
		}
		PODset.StartOnStr += " m" + Integer.toString(HdrBuf[110]) + " d" + Integer.toString(HdrBuf[111]) + "@00:00";


		switch (HdrBuf[112]) {
		case 0:
			PODset.MinsOFFbetweenON = 0;
			break;
		case (byte) 128:
			PODset.MinsOFFbetweenON = 1;
		break;
		case (byte) 131:
			PODset.MinsOFFbetweenON = 4;
		break;
		case (byte) 136:
			PODset.MinsOFFbetweenON = 9;
		break;
		default:
			PODset.MinsOFFbetweenON = 0;
		}

		// ... (more translations)

		PODset.DeploymentDepth = (HdrBuf[129] << 8) + HdrBuf[130];  // pOpenF
		PODset.WaterDepth = (HdrBuf[131] << 8) + HdrBuf[132];


		// Lat Long and Location are in the same position in all file structures
		String tempS = "";
		for (int posn = 133; posn <= 144; posn++) {
			if (Character.isLetterOrDigit(HdrBuf[posn])) {
				tempS += (char) HdrBuf[posn];
			}
		}
		PODset.LatText = tempS;
		// PODset.LatText = ParseLatLong(tempS, true).Value;  // Uncomment if needed

		tempS = "";
		for (int posn = 145; posn <= 156; posn++) {
			if (Character.isLetterOrDigit(HdrBuf[posn])) {
				tempS += (char) HdrBuf[posn];
			}
		}
		PODset.LongText = tempS;
		// PODset.LongText = ParseLatLong(tempS, false).Value;  // Uncomment if needed

		// Extract LocationText, NotesText, and GMTText
		for (int posn = 157; posn <= 187; posn++) {
			if (Character.isLetterOrDigit(HdrBuf[posn])) {
				tempS += (char) HdrBuf[posn];
			}
		}
		PODset.LocationText = tempS;

		tempS = "";
		for (int posn = 188; posn <= 231; posn++) {
			if (Character.isLetterOrDigit(HdrBuf[posn])) {
				tempS += (char) HdrBuf[posn];
			}
		}
		PODset.NotesText = tempS;

		tempS = "";
		for (int posn = 232; posn <= 233; posn++) {
			if (Character.isLetterOrDigit(HdrBuf[posn])) {
				tempS += (char) HdrBuf[posn];
			}
		}
		PODset.GMTText = tempS;

		// Combine bytes for NclxInFP1file
		PODset.NclxInFP1file = (HdrBuf[235] << 24) | (HdrBuf[236] << 16) | (HdrBuf[237] << 8) | HdrBuf[238];

		// Create TDAversionStr
		PODset.TDAversionStr = Integer.toString(HdrBuf[239]);
		if (HdrBuf[240] > 0) {
			PODset.TDAversionStr += "." + Integer.toString(HdrBuf[240]);
		}

		//		// Conditionally create ShortNameV
		//		if (PODset.FileType == ftFP3) {
		//			PODset.ShortNameV = PODset/ShortName + "(v" + TDAversionStr + ")";
		//		} else {
		//			PODset. ShortNameV = PODset.ShortName;
		//		}


		// Info for warnings
		PODset.AllSpHiModRatio = 	toUnsigned(HdrBuf[241]);
		PODset.DolHiModTrnCount = 	toUnsigned(Expanded(HdrBuf[242])); // Assuming Expanded is defined
		PODset.NBHFHiModTrnCount = 	toUnsigned(Expanded(HdrBuf[243]));
		PODset.SonarHiModTrnCount = toUnsigned(Expanded(HdrBuf[244]));
		PODset.NBHFmode = 			toUnsigned(HdrBuf[245]);
		PODset.NBHF10to5KHZbelow = 	toUnsigned(HdrBuf[246]);
		PODset.NBHF5to10KHZabove = 	toUnsigned(HdrBuf[247]);
		PODset.NBHFtargetMode = 	toUnsigned(HdrBuf[248]);
		PODset.NBHFdownsweeps = 	toUnsigned(HdrBuf[249]);
		// PODset.DolBadFrac = Expanded(HdrBuf[250]);
		// PODset.NBHFBadFrac = Expanded(HdrBuf[251]);
		// PODset.SonarBadFrac = Expanded(HdrBuf[252]);
		PODset.nLandmarks = 		toUnsigned(HdrBuf[253]);

		// These are same positions as CPOD files
		PODset.FirstLoggedMin = Math.max(Int32FromBytes(HdrBuf, 256, 4), 1); // MoveFilePosToMin exits if it gets a zero
		PODset.LastLoggedMin = 	Math.max(Int32FromBytes(HdrBuf, 260, 4), PODset.FirstLoggedMin);


		if (PODset.LastLoggedMin == 0 || PODset.LastLoggedMin < PODset.FirstLoggedMin) {
			// ShowTimedMessage(5000, 'PODset.LastLoggedMin = 0) or (PODset.LastLoggedMin < PODset.FirstLoggedMin'); // Adjust for Java
		}

		PODset.GoToMin =		 Math.min(Int32FromBytes(HdrBuf, 264, 4), PODset.LastLoggedMin);
		PODset.GoToFiveMusec = 	 Int32FromBytes(HdrBuf, 268, 4);
		PODset.StoredFirstProcessMin = Math.max(Int32FromBytes(HdrBuf, 272, 4), PODset.FirstLoggedMin);
		if (PODset.StoredFirstProcessMin < PODset.FirstLoggedMin || PODset.StoredFirstProcessMin > PODset.LastLoggedMin) {
			PODset.StoredFirstProcessMin = PODset.FirstLoggedMin;
		}
		PODset.StoredLastProcessMin = Math.min(Int32FromBytes(HdrBuf, 276, 4), PODset.LastLoggedMin);
		if (PODset.StoredLastProcessMin < PODset.FirstLoggedMin || PODset.StoredLastProcessMin > PODset.LastLoggedMin) {
			PODset.StoredLastProcessMin = PODset.LastLoggedMin;
		}
		PODset.nLoggingMins = (HdrBuf[280] << 24) | (HdrBuf[281] << 16) | (HdrBuf[282] << 8) | HdrBuf[283];

		return FPOD_HEADER;
	}

	/**
	 * Convert 4 bytes to an Java integer
	 * @param hdrBuf - a byte array of any length
	 * @param i - the index to start from
	 * @param j - the number of bytes to convert to an integer
	 * @return the integer value
	 */
	private static int Int32FromBytes(byte[] hdrBuf, int start, int nbytes) {
		int ret = 0;
		for (int i=start; i<start+nbytes; i++) {
			ret <<= 8;
			ret |= (int) hdrBuf[i] & 0xFF;
		}
		//		System.out.println("HELLO TIME: " + ret);
		return ret;
	}

	private static byte Expanded(byte b) {
		// TODO Dunno what this does. 
		return b;
	}

	/**
	 * Java will only have read signed bytes. Nick clearly
	 * uses a lot of unsigned data, so convert and inflate to int16. 
	 * @param signedByte
	 * @return unsigned version as int16. 
	 */
	public static short toUnsigned(byte signedByte) {
		return CPODUtils.toUnsigned(signedByte);
	}

	private static byte tWavMode(byte b) {
		// TODO Dunno what this function does. 
		return b;
	}

	private static String MinToDDMMYrstr(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Holds an FPOD detection. 
	 */
	public static class FPODdata {


		public int BW;

		/**
		 * 	0	time within minute in 5 microsecond units, MSB
		 *	1	time in 5 microsecond units
		 *	2	time in 5 microsecond units, LSB
		 *	3	N of cycles in the click - up to 255cycles
		 *	4	Wavenumber of loudest cycle; range of IPIs in click
		 *	5	IPI  of Pk-1
		 *	6	IPI of  Pk  this is the loudest cycle in the click
		 *	7	IPI of  Pk+1
		 *	8	IPI of  Pk+2
		 *	9	Amplitude of  P-1
		 *	10	Amplitude of  Pmax,  the loudest cycle in the click
		 *	11	Amplitude of  P+1
		 *	12	IPI before click start
		 *	13	N of Amplitude Reversals in click envelope; Duration of click (MSB)
		 *	14	Duration of click (LSB)
		 *	15	IPI of last cycle, compressed; flag if boat sonar found
		 */

		//time

		/**
		 * The time in 5us chunks from the start of the current minute
		 */
		public int FiveMusec;

		/**
		 * The time in Java millis
		 */
		private long timeMillis;


		/**
		 * N of cycles in the click - up to 255cycles
		 */
		public short Ncyc;

		//Wave number of loudest cycle; range of IPIs in click
		public int PkAt;

		//TODO - no idea what amp reversals is...
		public int AmpReversals;

		/**
		 * The duration in 5ms chunks?
		 */
		public int duration;


		//amplitude peaks

		public int MaxPkExtnd;

		//		public short MaxPkLinear;

		public short MaxPkRaw;

		public int Pkplus1Extnd;

		public short RawPkplus1;

		public int Pkminus1Extnd;

		public int RawPkminus1;


		//*****Inter pulse intervals*****//

		public int IPIplus2;

		public int IPIplus1;

		public int IPIatMax;

		public int IPIpreMax;

		public int EndIPI;

		public int IPIbefore;

		public int ClkIPIrange;

		//******Waveform******//

		/**
		 * True if the click has a waveform
		 */
		public boolean HasWave;

		/**
		 * The minute from the strat of the file. 
		 */
		public int minute;

		private FPODWavData wavData;
		
		//******Click Train Info******//


		/**
		 * Clasisifcation  of the click i.e. which clicks train it belongs to. Can be null. 
		 */
		private CPODClassification cpodClassification;


		public void setMinute(int nMinutes) {
			this.minute = nMinutes;

		}

		public void setClassification(CPODClassification cpodClassification) {
			this.cpodClassification=cpodClassification;
		}
		
		/**
		 * Get the species classification i.e. which click train the click belongs to.
		 * @return the species classification object. 
		 */
		public CPODClassification getClassification( ) {
			return cpodClassification;
		}

		public FPODWavData getWavData() {
			return this.wavData;
		}

		public void setWavData(FPODWavData wavData) {
			this.wavData=wavData;

		}

		public long getTimeMillis() {
			return this.timeMillis;
		}

		public void setTimeMillis(long timeMillis) {
			this.timeMillis=timeMillis;

		}

		public String dataString() {
			String data = "";

			data += " 5usTime: " + FiveMusec;
			data += " Ncyc: " + Ncyc;
			data += " MaxPkExtnd: " + MaxPkExtnd;
			data += " midKhz: " + IPItoKhz(IPIatMax);
			data += " BW: " + BW;
			data += " Duration: " + duration;


			return data;
		}


	}


	/**
	 * Holds FPOD wav data. Note this is not actually wav data 
	 * but the amplitude and inter pulse interval of successive peaks 
	 * in the waveform. 
	 */
	public static class FPODWavData {

		private static final int WAV_LEN = 22;

		public int clickCyclesStart ;


		/**
		 * The inter pulse intervals of the waveform
		 */
		private short[] WavValsIPI;

		/**
		 * The sound pressure levels of the waveform. 
		 */
		private short[] WavValsSPL;

		/**
		 * Pointer for current position in the wav array
		 */
		public int WavPtr;

		/**
		 * Number of wav amplitudes. 
		 */
		public int nWavRecs;

		/*** Wav file functions****/

		public int getWavPtr() {
			return WavPtr;
		}

		public int getNWavRecs() {
			return this.nWavRecs;
		}

		public void setWavPtr(int i) {
			this.WavPtr = i;
		}

		public void setNWavRecs(int i) {
			this.nWavRecs = i;
		}

		public short[] getWavValsSPL() {
			return WavValsSPL;
		}

		public short[] getWavValsIPI() {
			return WavValsIPI;
		}

		public void setWavValsIPI(short unsigned, int posn) {
			if (WavValsIPI==null) {
				WavValsIPI = new short[WAV_LEN];
			}
			WavValsIPI[posn] = unsigned;
		}

		public void setWavValsSPL(short unsigned, int posn) {
			if (WavValsSPL==null) {
				WavValsSPL = new short[WAV_LEN];
			}
			WavValsSPL[posn] = unsigned;
		}

		public void setClickCyclesStartAt(int i) {
			this.clickCyclesStart = i;
		}

	}

	/**
	 * Holds an FPOD header information
	 * <p>
	 * Note that is pretty opaque what all this means. The important parameters have been commented. 
	 */
	public static class FPODHeader {

		public int FileType;
		public int nLoggingMins;
		public int StoredLastProcessMin;
		public int StoredFirstProcessMin;
		public int GoToFiveMusec;
		public int GoToMin;
		public int LastLoggedMin;
		public int FirstLoggedMin;
		public short nLandmarks;
		public short DolHiModTrnCount;
		public short SonarHiModTrnCount;
		public short NBHFmode;
		public short NBHF10to5KHZbelow;
		public short NBHF5to10KHZabove;
		public short NBHFdownsweeps;
		public short NBHFtargetMode;
		public short NBHFHiModTrnCount;
		public short AllSpHiModRatio;
		public String GMTText;
		public String NotesText;
		public String LocationText;
		public Object ShortNameV;
		public String TDAversionStr;
		public int NclxInFP1file;
		public String LongText;
		public String LatText;
		public String StartOnStr;
		public String ReleaseOnStr;
		public int WaterDepth;
		public int DeploymentDepth;
		public int MinsOFFbetweenON;
		public String ReleaseAfterStr;
		public short SwitchNmax;
		public short SwitchNmin;
		public short QuietMinsLimit;
		public short NoisyMinsLimit;
		public int QuietLevel;
		public int NoisyLevel;
		public short WavMaxICI;
		public short WavOKseqN;
		public short WavMaxRawPerTrain;
		public short WavMinICI;
		public int WavTotalLimit;
		public boolean SonarFiltering;
		public short WavMinPk;
		public int WavMinLimit;
		public short WavSNR;
		public short WavPkAddOn;
		public short WavMode;
		public short BatterySwitchLevel;
		public boolean ThermalGainControl;
		public short MaxSPLforAmpDropTest;
		public short SonarLongNcyc;
		public boolean UsingQtrAmpDrop;
		public short StrongLimit3;
		public short StrongLimit4;
		public short StrongLimit2;
		public short StrongLimit1;
		public short BWaddon;
		public short MaxSPLforReversalCount;
		public int PeakDetectionMode;
		public int MinNofCyc;
		public short MaxIPI;
		public short MinIPI;
		public short MinimumCycleAmplitude;
		public int FileMinutecountLimit;
		public short UTCoffset;
		public boolean HasExtendedAmps;
		public int FilterKHZ;
		public boolean SteeperStepping;
		public int FPGAcodeVersion;
		public String PICcodeVersionStr;
		public String FPGAcodeVersionStr;
		public int PODID;
		public short PICcodeMajorVersion;
		public short RelaysSecs;
		public short AcRelSlotLength;
		public short AcRelSlotSpacing;
		public short AcRelNcyc;
		public short ReleaseTone5Min;
		public short ReleaseTone5Max;
		public short ReleaseTone4Max;
		public short ReleaseTone4Min;
		public short ReleaseTone2Max;
		public short ReleaseTone3Min;
		public short ReleaseTone3Max;
		public short ReleaseTone1Max;
		public short HousingType;
		public short ReleaseTone1Min;
		public short ReleaseTone2Min;
		public short PICtype;
		public int PaDataVersion;
		public boolean FromWavFileData;
		public short FPGAtype;
		public short HydrophoneVersion;
		public int MainBoardNumber;
		public short PreAmpVersion;
		public short gain;
		public String DateOfCalibrationStr;

	}



	public static void BuildSineArray() {
		SineArr = new double[2001];
		final double constPiFrac = Math.PI / 1000;
		double S;
		for (int count = 0; count < 2000; count++) {
			S = Math.sin(constPiFrac * count);
			SineArr[count] = S;
		}
	}

	/**
	 * Scale wave data so it is returned as a double
	 * @param wavData - the wavdata
	 * @return the scaled wav data between -1 and 1; 
	 */
	public static double[] scaleWavData(int[] wavData) {
		double[] wavArr = new double[wavData.length];
		for (int i=0; i<wavData.length; i++) {
			wavArr[i] = wavData[i]/WAV_SCALE_FACTOR;
		}
		return wavArr;
	}

	/**
	 * Reconstructs sinusoidal waveform from the peaks which have been sampled at 4MHz 
	 * 
	 * @param click - FPOD click with waveform information. 
	 */
	public static int[] makeResampledWaveform(FPODdata click) {

		if (SineArr==null) {
			BuildSineArray();
		}

		int[] MhzSampledArr = new int[2000]; 

		int count, cyc, SinePtsPerUs, SinePtr, FirstClkCyc, IPIsum, NewIPIx, OldIPIx;

		// MhzSampledArr initialization (assuming it's an array)

		// Read back from end of WavIPIarr to find start of continuous sound data
		int RawStartPtr = 21;


		while (RawStartPtr > 0 &&   click.getWavData().getWavValsIPI()[RawStartPtr] < 255) {
			click.getWavData().getWavValsSPL()[RawStartPtr] = (short) LinearPkValsArr[click.getWavData().getWavValsSPL()[RawStartPtr]];
			RawStartPtr--;
		}

		RawStartPtr = Math.min(21, RawStartPtr + 1);
		FirstClkCyc = 21 - click.Ncyc;

		// Construct each cycle in MhzSampledArr
		SinePtr = 0;
		int MHzArrPtr = 0;
		int MaxSPLval = 0;
		IPIsum = 0;

		cyc = 21;

		do {
			// Populate MhzSampledArr
			SinePtsPerUs = Math.round(4000 / click.getWavData().getWavValsIPI()[cyc]);
			while (SinePtr <= 1999 && MHzArrPtr < MhzSampledArr.length) {
				MhzSampledArr[MHzArrPtr] = (int) Math.round(SineArr[SinePtr]
						* click.getWavData().getWavValsSPL()[cyc]);

				//	                System.out.println( MhzSampledArr[MHzArrPtr] +" index: " + MHzArrPtr + " SinePtr " + SinePtr + " cyc " + cyc);
				MHzArrPtr++;
				SinePtr += SinePtsPerUs;
			} 

			if (MHzArrPtr >= MhzSampledArr.length) {
				//System.err.println("FPOD Waveform index greater than len? " + MhzSampledArr.length);
				break; // Fix: extend this array if needed
			}

			SinePtr -= 2000;
			if (cyc == FirstClkCyc) {
				int StartOfClickHighRes = MHzArrPtr;
			}
			IPIsum += click.getWavData().getWavValsIPI()[cyc];
			cyc--;
		} while (cyc > click.wavData.WavPtr);

		// Bring line up to zero
		if (MHzArrPtr < MhzSampledArr.length) {
			MhzSampledArr[MHzArrPtr] = 0;
		}
		
		

		int[] waveform = Arrays.copyOf(MhzSampledArr, MHzArrPtr);
		//waveform is backwards so flip it. 
		return PamArrayUtils.flip(waveform);
	}


	/**
	 * Test the program
	 * @param args
	 */
	public static void main(String[] args) {
		//		String filePath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/CPOD/FPOD_NunBank/0866 NunBankB 2023 06 27 FPOD_6480 file0.FP1";

		String filePath = "D:\\DropBox\\PAMGuard_dev\\CPOD\\FPOD_NunBank\\0866 NunBankB 2023 06 27 FPOD_6480 file0.FP1";
//		String filePath = "D:\\DropBox\\PAMGuard_dev\\CPOD\\FPOD_NunBank\\0866 NunBankB 2023 06 27 FPOD_6480 file0.FP3";

		File fpfile = new File(filePath); 

		ArrayList<FPODdata> fpodData = new ArrayList<FPODdata>();

		try {
			importFile( fpfile, fpodData, 0, Integer.MAX_VALUE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


}
