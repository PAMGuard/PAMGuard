/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package clickDetector;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import Acquisition.AcquisitionProcess;
import Acquisition.DaqSystem;
import Acquisition.FileInputSystem;
import Array.ArrayManager;
import Array.PamArray;
import PamUtils.FileFunctions;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamProcess;
import fftManager.FastFFT;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Creates a file in the RainbowClick file format. Required Rainbowclick
 *         structures are subclasses of RainbowFile. The class is also
 *         responsible for holding the file handle, etc.
 *         <p>
 *         Not all RainbowClick structures are implemented since may of them
 *         contain no useful data. Parameter settings will be added to other Pam
 *         storage as that developes.
 */
public class RainbowFile implements ClickFileStorage {

	static final byte CURRENT_FILE_FORMAT = 0x15;

	/*
	 * Constants copied over from structs.h
	 */
	// some windows constants
	static final int MAX_PATH = 260;

	// a few flags to say whats in the dummy clicks !
	static final int NC_SCSETTINGS = 0x1;

	static final int NC_ANALPARMS = 0x2;

	static final int NC_FILTPARMS = 0x4;

	static final int NC_RAWFILTPARMS = 0x8;

	static final int NC_DATHEADER = 0x10;

	static final int NC_ANALTYPE = 0x20;

	static final int NC_SECTIONTIME = 0x40;

	static final int NC_RUNPARDATA = 0x80;

	static final int NC_DISPARMS = 0x100;

	static final int NC_OPTIONS = 0x200;

	static final int NC_OUTPUTPARS = 0x400;

	static final int NC_TRACKOPTIONS = 0x800;

	static final int NC_DATABASESTUFF = 0x1000;

	static final int NC_FILEOPTIONS = 0x2000;

	static final int NC_WAVFILEINFO = 0x4000;

	static final int NC_DIGITALFILTER = 0x8000;

	static final int NC_DIGITALFILTERDISPLAY = 0x10000;

	static final int NC_AUTOADJUST = 0x20000;

	static final int NC_ONLINEID = 0x40000;

	static final int NC_NARROWREJECT = 0x80000;

	static final int NC_STORAGEOPTIONS = 0x100000;

	static final int NC_DIGITALPREFILTER = 0x200000;

	static final int NC_EVERYTHING = 0x2FFFFF; // absolutely everything

	// stuff for start of each section
	// static final int NC_SECTIONSTUFF = NC_SCSETTINGS | NC_ANALPARMS |
	// NC_RAWFILTPARMS | NC_DATHEADER | NC_ANALTYPE |
	// NC_SECTIONTIME |
	// NC_DIGITALFILTER | NC_NARROWREJECT | NC_DIGITALPREFILTER;
	static final int NC_SECTIONSTUFF = NC_SCSETTINGS | NC_ANALPARMS
	| NC_SECTIONTIME | NC_FILTPARMS | NC_RAWFILTPARMS | NC_WAVFILEINFO;

	// data type flags
	static public final int HEADER_CLICK = 0;

	static public final int HEADER_SECTION = 1;

	static public final int HEADER_GPSDATA = 2;

	static public final int HEADER_NOISE = 3;

	static public final int HEADER_SEISMIC = 4;

	static public final int HEADER_NOISEWAVE = 5;

	private ClickDetector clickDetector;

	private String fileName;

	private File rainbowFile;

	// OutputStream outputStream;
	private volatile WindowsFile file;

	private WindowsBuffer clickBuffer;

	private long sampleOffset;

	private long fileStartTime;

	private long fileSamples;

	private long structureEndPoint;

	private float sampleRate;

	private Calendar rainbowCalendar;

	private ArrayList<RainbowFileSectionData> sectionDataList = new ArrayList<RainbowFileSectionData>();

	/**
	 * Used mainly offline to get how many clicks there are in the file. 
	 */
	private int nClicksInFile;

	public RainbowFile(ClickDetector clickDetector) {
		this.clickDetector = clickDetector;
		rainbowCalendar = Calendar.getInstance();
		rainbowCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		clickBuffer = new WindowsBuffer(4096);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		closeClickStorage();
	}

	/**
	 * Open a new rainbowFile for storage. 
	 */
	synchronized public boolean openClickStorage(long sampleOffset) {
		this.sampleOffset = sampleOffset;

		fileStartTime = PamCalendar.getTimeInMillis();
		rainbowCalendar.setTimeInMillis(fileStartTime);

		ClickParameters clickParameters = clickDetector.getClickControl().clickParameters;

		// try to work out if the source process is file input or not
		File soundFile = null;
		try {
			AcquisitionProcess sourceProcess = (AcquisitionProcess) clickDetector.getSourceProcess();
			Acquisition.DaqSystem daqSystem = sourceProcess.getAcquisitionControl().findDaqSystem(null);
			if (daqSystem != null & daqSystem.isRealTime() == false) {
				// assume it's a file name.
				Acquisition.FileInputSystem fileSystem = (FileInputSystem) daqSystem;
				soundFile = fileSystem.getCurrentFile();
			}
		}
		catch (Exception ex) {
			soundFile = null;
		}

		if (soundFile == null) fileName = getDateFileName();
		else fileName = getWavFileName(soundFile);
		// Create a file based on the current time
		// %4.4d%2.2d%2.2d_%2.2d%2.2d%2.2d

		try {
			rainbowFile = new File(fileName);
			if (rainbowFile.exists()) {
				rainbowFile.delete();
			}
			file = new WindowsFile(fileName, "rwd");

		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		nClicksInFile = 0;
		return true;
	}

	/**
	 * Open an old rainbowclick file to reload data for viewing
	 * or for batch conversion into binary files. 
	 */
	public boolean openClickStorage(File oldFile) {
		sectionDataList.clear();
		if (oldFile.exists() == false) {
			return false;
		}
		try {
			file = new WindowsFile(oldFile.getAbsolutePath(), "rw");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		RainbowFileSectionData rsd;
		ClickDetection click = new ClickDetection(clickDetector);
		boolean ans = false;
		long filePos;
		//		ClickParameters clickParameters = new ClickParameters();
		// read the file header and all the structures. 
		// are only going to support single section files
		nClicksInFile = readFileHeader();
		try {
			while(true) {
				rsd = new RainbowFileSectionData();
				rsd.sectionHeadPos = file.getFilePointer();
				ans = readClickStructures(rsd.clickParams, false);
				rsd.sectionStartTime = fileStartTime;
				rsd.sectionSamples = fileSamples;
				rsd.sectionDataPos = file.getFilePointer();
				if (ans == false) {
					break;
				}
				else {
					sectionDataList.add(rsd);
				}
				while (true) {
					// then cycle through clicks until a next section is met ...
					filePos = file.getFilePointer();
					ans = readClickHeader(click);
					if (ans == false) {
						break;
					}
					if (click.dataType == HEADER_CLICK) {
						skipWavData(click);
						continue;
					}
					else if (click.dataType == HEADER_NOISE) {
						
					}
					else if (click.dataType == HEADER_SECTION) {
						file.seek(filePos);
						break;
					}
					else if (click.dataType == HEADER_NOISEWAVE) {
						file.skipBytes((int) (click.getSampleDuration()*4));
					}
					else {
						System.out.println(String.format("Unknown click type = %d", click.dataType));
						
					}
				}
				if (ans == false) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		rainbowFile = oldFile;


		return true;
	}

	public boolean gotoSectionHead(int iSection) {
		if (iSection >= sectionDataList.size()) {
			return false;
		}
		if (file == null) {
			return false;
		}
		try {
			file.seek(sectionDataList.get(iSection).sectionHeadPos);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		useSectionParams(sectionDataList.get(iSection));
		return true;
	}
	private void useSectionParams(RainbowFileSectionData rainbowFileSectionData) {
		fileStartTime = rainbowFileSectionData.sectionStartTime;
		fileSamples = rainbowFileSectionData.sectionSamples;
		structureEndPoint = rainbowFileSectionData.sectionDataPos;
	}

	public boolean gotoSectionData(int iSection) {
		if (iSection >= sectionDataList.size()) {
			return false;
		}
		if (file == null) {
			return false;
		}
		try {
			file.seek(sectionDataList.get(iSection).sectionDataPos);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		useSectionParams(sectionDataList.get(iSection));
		return true;
	}

	/**
	 * Create a default data file name
	 * @return name
	 */
	String getDateFileName() {
		ClickParameters clickParameters = clickDetector.getClickControl().clickParameters;
		return String.format("%s\\%s%3$tY%3$tm%3$td_%3$tH%3$tM%3$tS%4$s",
				clickParameters.storageDirectory,
				clickParameters.storageInitials, rainbowCalendar,
				// c.get(c.YEAR),
				// c.get(c.MONTH),
				// c.get(c.DAY_OF_MONTH),
				// c.get(c.HOUR_OF_DAY),
				// c.get(c.MINUTE),
				// c.get(c.SECOND),
		".clk");
	}
	String getWavFileName(File wavFile) {

		ClickParameters clickParameters = clickDetector.getClickControl().clickParameters;
		String nameBit = wavFile.getName();
		nameBit = nameBit.substring(0, nameBit.lastIndexOf('.'));
		return String.format("%s\\%s%s",
				clickParameters.storageDirectory, nameBit,
		".clk");
	}

	synchronized public void closeClickStorage() {
		if (file == null) {
			return;
		}
		try {
			file.close();
			file = null;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		rainbowFile = null;
		sectionDataList.clear();
	}

	/**
	 * @return the number of sections in an old style RC file. 
	 */
	public int getNumSections() {
		return sectionDataList.size();
	}

	synchronized public boolean writeClickStructures(ClickParameters clickParameters) {

		if (file == null) {
			return false;
		}
		try {
			file.seek(0);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		if (!writeFileHeader((int) clickDetector.getClickCount()))
			return false;

		ClickDetection click = new ClickDetection(3, 0, 0, clickDetector, null, 0);
		click.dataType = HEADER_SECTION;
		click.flags = NC_SECTIONSTUFF;
		try {
			click.filePos = file.getFilePointer();
		} catch (IOException ex) {
		}

		this.writeClickHeader(click);
		//		static final int NC_SECTIONSTUFF = NC_SCSETTINGS | NC_ANALPARMS
		//		| NC_SECTIONTIME | NC_FILTPARMS | NC_RAWFILTPARMS;

		int struct;
		for (int i = 0; i < 32; i++) {
			struct = 1<<i;
			if ((struct & click.flags) > 0) {
				writeClickStructure(struct, clickParameters);
			}
		}
		try {
			structureEndPoint = file.getFilePointer(); 
			file.seek(file.length());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return true;
	}
	synchronized public boolean readClickStructures(ClickParameters clickParameters){
		return readClickStructures(clickParameters, true);
	}
	/**
	 * Read click head structures from a rainbowclick file. 
	 * @param clickParameters
	 * @return true if read ok, false otherwise
	 */
	synchronized public boolean readClickStructures(ClickParameters clickParameters, boolean fromStart) {

		if (file == null) {
			return false;
		}
		if (fromStart) {
			try {
				file.seek(0);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			int nClicks = readFileHeader();
		}

		ClickDetection click = new ClickDetection(3, 0, 0, clickDetector, null, 0);
		click.dataType = HEADER_SECTION;
		click.flags = NC_SECTIONSTUFF;
		try {
			click.filePos = file.getFilePointer();
		} catch (IOException ex) {
		}

		this.readClickHeader(click);
		//		static final int NC_SECTIONSTUFF = NC_SCSETTINGS | NC_ANALPARMS
		//		| NC_SECTIONTIME | NC_FILTPARMS | NC_RAWFILTPARMS;

		int struct;
		for (int i = 0; i < 32; i++) {
			struct = 1<<i;
			if ((struct & click.flags) > 0) {
				if (readClickStructure(struct, clickParameters) == false) {
					JOptionPane.showMessageDialog(null, "Error in Click File", 
							"Incompatible click file format. \n" +
							"Only use files created with PAMGUARD", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}

		try {
			structureEndPoint = file.getFilePointer(); 
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean moveToClicks() {
		try {
			file.seek(structureEndPoint);
			return structureEndPoint < file.length();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Get the next clicks from the file. Assume that the file pointer
	 * is in the right place, etc. 
	 * @return next click, or null if EOF. 
	 */
	public synchronized ClickDetection getNextClick() {
		long filePointer = 0;
		try {
			filePointer = file.getFilePointer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ClickDetection click = new ClickDetection(clickDetector);
		if (!readClickHeader(click)) {
			return null;
		}
		switch (click.dataType) {
		case HEADER_CLICK:
			readWavData(click);
			break;
		case HEADER_NOISE:
			break;
		case HEADER_SECTION:
			break;
		case HEADER_NOISEWAVE:
			readWavData(click);
		}
		// now read or skip the wav data. 
		return click;
	}

	synchronized public boolean writeClickStructure(int structId, ClickParameters clickParameters) {

		//		static final int NC_SECTIONSTUFF = NC_SCSETTINGS | NC_ANALPARMS
		//		| NC_SECTIONTIME | NC_FILTPARMS | NC_RAWFILTPARMS;
		switch(structId) {
		case NC_SCSETTINGS:
			return writeSCSettings(clickParameters);
		case NC_ANALPARMS:
			return writeAnalParms(clickParameters);
		case NC_FILTPARMS:
		case NC_RAWFILTPARMS:
			return writeFiltParms(clickParameters);
		case NC_SECTIONTIME:
			return writeSectionTime(this.fileStartTime,
					(int) (clickDetector.getSamplesProcessed() - sampleOffset),
					(short) 2);
		case NC_WAVFILEINFO:
			return writeWavFileInfo();
		default:
			return false;

		}
		//		if (!writeSCSettings(clickParameters))
		//			return false;
		//		if (!writeAnalParms(clickParameters))
		//			return false;
		//		if (!writeFiltParms(clickParameters)) // filtpar,s
		//			return false;
		//		if (!writeFiltParms(clickParameters)) // rawfiltparms
		//			return false;
		//		if (!writeSectionTime(this.fileStartTime,
		//				(int) (clickDetector.getSamplesProcessed() - sampleOffset),
		//				(short) 2))
		//			return false;
		//
		//
		//		return true;
	}
	synchronized public boolean readClickStructure(int structId, ClickParameters clickParameters) {
		switch(structId) {
		case NC_SCSETTINGS:
			return readSCSettings(clickParameters);
		case NC_ANALPARMS:
			return readAnalParms(clickParameters);
		case NC_FILTPARMS:
		case NC_RAWFILTPARMS:
			return readFiltParms(clickParameters);
		case NC_SECTIONTIME:
			return readSectionTime();
		case NC_WAVFILEINFO:
			return readWavFileInfo();
		default:
			return skipClickStructure(structId);
		}
	}

	private boolean skipClickStructure(int structId) {

		long structSize = getRainbowStructureSize(structId);
		if (structSize <= 0) {
			return false;
		}
		try {
			file.seek(file.getFilePointer() + structSize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	private long getRainbowStructureSize(int structId) {
		/**
	static final int NC_ANALTYPE = 0x20;
	static final int NC_SECTIONTIME = 0x40;
	static final int NC_RUNPARDATA = 0x80;
	static final int NC_DISPARMS = 0x100;
	static final int NC_OPTIONS = 0x200;
	static final int NC_OUTPUTPARS = 0x400;
	static final int NC_TRACKOPTIONS = 0x800;
	static final int NC_DATABASESTUFF = 0x1000;
	static final int NC_FILEOPTIONS = 0x2000;
	static final int NC_WAVFILEINFO = 0x4000;
	static final int NC_DIGITALFILTER = 0x8000;
	static final int NC_DIGITALFILTERDISPLAY = 0x10000;
	static final int NC_AUTOADJUST = 0x20000;
	static final int NC_ONLINEID = 0x40000;
	static final int NC_NARROWREJECT = 0x80000;
	static final int NC_STORAGEOPTIONS = 0x100000;
	static final int NC_DIGITALPREFILTER = 0x200000;
		 */
		switch(structId) {
		case NC_SCSETTINGS:
			return 16; // covered
		case NC_ANALPARMS:
			return 78;
		case NC_FILTPARMS:
			return 38;
		case NC_RAWFILTPARMS:
			return 0;
		case NC_DATHEADER:
			return 18;
		case NC_ANALTYPE:
			return 52;
		case NC_SECTIONTIME:
			return 40;
		case NC_RUNPARDATA:
			return 56;
		case NC_DISPARMS:
			return 124;
		case NC_OPTIONS:
			return 24;
		case NC_OUTPUTPARS:
			return 127;
		case NC_TRACKOPTIONS:
			return 45;
		case NC_DATABASESTUFF:
			return 283;
		case NC_FILEOPTIONS:
			return 288;
		case NC_WAVFILEINFO:
			return 0;
		case NC_DIGITALPREFILTER:
		case NC_DIGITALFILTER:
			return 44;
		case NC_DIGITALFILTERDISPLAY:
			return 31;
		case NC_AUTOADJUST:
			return 54;
		case NC_ONLINEID:
			return 119;
		case NC_NARROWREJECT:
			return 44;
		case NC_STORAGEOPTIONS:
			return 0;
		}
		return -1;
	}

	synchronized public boolean writeSCSettings(ClickParameters clickParameters) {
		// typedef struct {
		// WORD wFormatTag;
		// WORD nChannels;
		// DWORD nSamplesPerSec;
		// DWORD nAvgBytesPerSec;
		// WORD nBlockAlign;
		// WORD wBitsPerSample;
		// WORD cbSize;
		// } WAVEFORMATEX;

		try {
			file.writeWinShort(1); // WAVE_FORMAT_PCM
			file.writeWinShort(2);
			file.writeWinInt((int) clickDetector.getSampleRate());
			file.writeWinInt((int) clickDetector.getSampleRate() * 4);
			file.writeWinShort(4);
			file.writeWinShort(16);
			// file.writeWinShort(0); // Rainbowclick uses WaveFormat, not
			// WaveFormatEx
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	synchronized public boolean readSCSettings(ClickParameters clickParameters) {
		// typedef struct {
		// WORD wFormatTag;
		// WORD nChannels;
		// DWORD nSamplesPerSec;
		// DWORD nAvgBytesPerSec;
		// WORD nBlockAlign;
		// WORD wBitsPerSample;
		// WORD cbSize;
		// } WAVEFORMATEX;

		try {
			file.readWinShort(); // WAVE_FORMAT_PCM
			file.readWinShort();
			sampleRate = file.readWinInt();
			clickDetector.setSampleRate(sampleRate, false);
			file.readWinInt();
			file.readWinShort();
			file.readWinShort();
			// file.writeWinShort(0); // Rainbowclick uses WaveFormat, not
			// WaveFormatEx
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	synchronized public boolean writeClickHeader(ClickDetection click) {
		if (file == null) {
			return false;
		}
		try {
			if (click.filePos > 0) {
				file.seek(click.filePos); // old click, so go back and rewrite
				// in old location
			} else {
				file.seek(click.filePos = file.length()); // new click, so
				// write at the end
				// of the file
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return writeClickHeader(file, click);
	}

	synchronized public boolean reWriteClick(ClickDetection click) {
		if (file == null) {
			return false;
		}
		return writeClickHeader(click);
		//		return writeClickHeader(click);
	}

	@Override
	//public boolean writeClick(ClickDetection click);

	synchronized public boolean writeClick(ClickDetection click) {
		if (file == null) {
			return false;
		}
		/*
		 * the click may have already been saved in which case fPos is > 0. //
		 */
		if (click.filePos > 0) {
			return reWriteClick(click);
		}
		// try {
		// file.seek(click.filePos);
		// }
		// catch (IOException ex) {}
		// }
		try {
			clickBuffer.clear();
			double[][] waveData = click.getWaveData();
			if (file.getFilePointer() != file.length()) {
				file.seek(file.length());
			}
			long filePos = file.getFilePointer();
			writeClickHeader(clickBuffer, click);
			click.filePos = filePos;
			for (int iS = 0; iS < click.getSampleDuration(); iS++) {
				for (int iC = 0; iC < click.getNChan(); iC++) {
					clickBuffer.writeWinShort((short) (waveData[iC][iS] * 32767.));
				}
			}
			file.write(clickBuffer.getBytes(), 0, clickBuffer.getUsedBytes());
			nClicksInFile ++;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	synchronized private boolean writeClickHeader(WriteWinFile winFile, ClickDetection click) {

		// write click information in format compatible with rainbowclick

		// uint16 click_no; // start at 1, NOT 0 !
		// uint32 start_time; // time bucket in wav file
		// uint16 duration; // duration in time buckets
		// // change DataType fom uint32 to uint8, add three spares, using one
		// to store trigger info !
		// uint8 DataType; // flag to say its not a click ! was int32
		// uint8 triggs;
		// uint16 Spare1;
		// int16 amplitude[NCHAN]; // max height of click
		// union
		// {
		// float bearing; // in degrees (-999. if undefined)
		// int16 timediff[2];
		// uint32 flags; // flags for writing structures to file
		// float NoiseLevel;
		// };
		// int8 whale; // 21/9/04. Change whale (which is just the colour) to
		// int8
		// uint8 ClickType; // and free up 8 bits for click type info
		// uint16 attributes;
		// uint16 Coda;
		// uint16 Tracker;
		// uint16 EventNumber;
		// uint8 usedChannels
		// int8 spare2;
		// float FilteredAmplitude;

		short tracked = 0;
		float aveAmp;
		if (click.tracked)
			tracked = 1;

		try {
			winFile.writeWinShort((int) click.clickNumber);
			winFile.writeWinInt((int) (click.getStartSample() - sampleOffset));
			winFile.writeWinShort(click.getSampleDuration().intValue());
			winFile.writeByte(click.dataType); // DataType
			winFile.writeByte((byte) click.triggerList);
			winFile.writeWinShort(0); // spare1;
			for (int i = 0; i < click.getNChan(); i++) {
				winFile.writeWinShort((short) ((Math.min(click.getAmplitude(i),
						1.0) * 32767.)));
			}
			double delayCorrection = 0; // get's written after usedChannels
			if (click.flags == 0) {
				int delay = 0;
				if (click.getClickLocalisation() != null) {
					double dDelay = click.getClickLocalisation().getFirstDelay();
					delay = (int) Math.round(dDelay);
					delayCorrection = dDelay-delay;
				}
				winFile.writeWinShort((short) delay);
				winFile.writeWinShort(0); // other half of union to make 4 bytes
			} else {
				winFile.writeWinInt((short) click.flags);
			}
			winFile.writeByte(0); // click whale
			winFile.writeByte(click.getClickType()); // click Type
			winFile.writeWinShort(0); // attributes
			winFile.writeWinShort(0); // Coda
			winFile.writeWinShort(tracked);
			winFile.writeWinShort(0); // event number
			winFile.writeByte((short) click.getChannelBitmap());
			//			winFile.writeByte(0); // spare2
			winFile.writeByte((byte) (delayCorrection * 127));
			aveAmp = 0;
			for (int iChan = 0; iChan < click.getNChan(); iChan++) {
				aveAmp += click.getAmplitude(iChan);
			}
			aveAmp /= click.getNChan();
			winFile.writeWinFloat((aveAmp * 32767f)); // filtered amplitude

		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	synchronized private boolean readClickHeader(ClickDetection click) {

		// write click information in format compatible with rainbowclick

		// uint16 click_no; // start at 1, NOT 0 !
		// uint32 start_time; // time bucket in wav file
		// uint16 duration; // duration in time buckets
		// // change DataType fom uint32 to uint8, add three spares, using one
		// to store trigger info !
		// uint8 DataType; // flag to say its not a click ! was int32
		// uint8 triggs;
		// uint16 Spare1;
		// int16 amplitude[NCHAN]; // max height of click
		// union
		// {
		// float bearing; // in degrees (-999. if undefined)
		// int16 timediff[2];
		// uint32 flags; // flags for writing structures to file
		// float NoiseLevel;
		// };
		// int8 whale; // 21/9/04. Change whale (which is just the colour) to
		// int8
		// uint8 ClickType; // and free up 8 bits for click type info
		// uint16 attributes;
		// uint16 Coda;
		// uint16 Tracker;
		// uint16 EventNumber;
		// uint8 usedChannels
		// int8 spare2;
		// float FilteredAmplitude;

		short tracked = 0;
		float aveAmp;
		long millisInFile;
		if (click.tracked)
			tracked = 1;

		try {
			click.clickNumber = file.readWinShort();
			click.setStartSample((long) file.readWinInt());
			millisInFile = (long) (click.getStartSample() * 1000 / sampleRate);
			click.setTimeMilliseconds(fileStartTime + millisInFile);
			click.setSampleDuration((long) file.readWinShort());
			click.dataType = file.readByte(); // DataType
			click.triggerList = file.readByte();
			file.readWinShort(); // spare1;
			int nAmp = 0;
			double totAmp = 0;
			for (int i = 0; i < 2; i++) {
				click.setAmplitude(i, totAmp = file.readWinShort() / 32767.);
			}
			click.setMeasuredAmplitude(totAmp / nAmp);

			double delayCorrection = 0; // get's written after usedChannels
			WindowsBuffer dualBuffer = new WindowsBuffer(4);
			file.read(dualBuffer.getBytes(), 0, 4);
			click.flags = dualBuffer.readWinInt();
			dualBuffer.reset();
			int delay = dualBuffer.readWinShort();
			click.setDelayInSamples(0,delay);
			file.readByte(); // click whale
			click.setClickType(file.readByte()); // click Type
			file.readWinShort(); // attributes
			file.readWinShort(); // Coda
			click.setTracked(file.readWinShort() != 0);
			file.readWinShort(); // event number
			click.setChannelBitmap(file.readByte());
			int nChan = PamUtils.getNumChannels(click.getChannelBitmap());
			if (nChan == 0) {
				click.setChannelBitmap(3);
			}
			//			winFile.writeByte(0); // spare2
			delayCorrection = file.readByte();
			aveAmp = 0;

			//			for (int iChan = 0; iChan < click.getNChan(); iChan++) {
			//				aveAmp += click.getAmplitude(iChan);
			//			}
			//			aveAmp /= click.getNChan();
			float fAmp = file.readWinFloat(); // filtered amplitude
			if (fAmp > 0) {
				click.setMeasuredAmplitude(fAmp / 32768);
			}
			click.setMeasuredAmplitudeType(DataUnitBaseData.AMPLITUDE_SCALE_LINREFSD);


		} 
		catch (EOFException eof) {
			return false;
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	private boolean readWavData(ClickDetection click) {
		//		return skipWavData(click);
		int nChan = PamUtils.getNumChannels(click.getChannelBitmap());
		if (nChan == 0) {
			nChan = 2;
		}
		int duration = click.getSampleDuration().intValue();
		int dataBytes = (nChan * 2 * duration);
		double[][] waveData = new double[nChan][duration];
		WindowsBuffer buff = new WindowsBuffer(dataBytes);
		try {
			file.read(buff.getBytes(), 0, dataBytes);
			for (int s = 0; s < duration; s++) {
				for (int c = 0; c < nChan; c++) {
					waveData[c][s] = buff.readWinShort() / 32768.;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		click.setWaveData(waveData);
		return true;
	}
	private boolean skipWavData(ClickDetection click) {
		int nChan = PamUtils.getNumChannels(click.getChannelBitmap());
		int dataBytes = (int) (nChan * 2 * click.getSampleDuration());
		try {
			file.skipBytes(dataBytes);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	synchronized boolean writeFileHeader(int nClicks) {

		// int nClicks;
		// int nWhales;
		// long FlukeTime;
		// long RecordTime;
		// char format;
		// char spare;
		try {
			file.writeWinInt(nClicks);
			file.writeWinInt(0);
			file.writeWinInt(0);
			file.writeWinInt(0);
			file.writeByte(CURRENT_FILE_FORMAT);
			file.writeByte(0);
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	synchronized int readFileHeader() {

		// int nClicks;
		// int nWhales;
		// long FlukeTime;
		// long RecordTime;
		// char format;
		// char spare;
		int nClicks = 0;
		int format = 0;
		try {
			nClicks = file.readWinInt();
			file.readWinInt();
			file.readWinInt();
			file.readWinInt();
			format = file.readByte();
			file.writeByte(0);
		} catch (IOException ex) {
			ex.printStackTrace();
			return 0;
		}

		return nClicks;
	}

	synchronized boolean writeAnalParms(ClickParameters clickParameters) {

		// float alpha_long, alpha_short;
		// short SnapInt; // interval between snap shots of analysis
		// float SnapDur; // and duration of snapshot in seconds
		// unsigned SnapStart; // start in samples
		// unsigned SnapEnd;
		// short threshoff, threshon;
		// short presample, postsample, datalength;
		// short SnapGain;
		// bool T2Active;
		// bool RunClickIdentification;
		// float alpha_long2;
		// short minsep;
		// // FVRemove and FVRecalc were both BOOl, have changed to bool and
		// added extras
		// bool FVRemove;
		// bool FVReCalc;
		// bool invertChannel;
		// uint8 Spare1;
		// uint32 Spare2;
		// int FVeto;
		// bool BigVeto;
		// USHORT BigThreshold;
		// float BigSeconds;
		// bool MeasureNoise;
		// float NoiseInterval;
		// float MinEnergy;
		// float HydrophoneSensitivity;
		// float ADCPeakToPeak;

		try {
			file.writeWinFloat((float) clickParameters.longFilter);
			file.writeWinFloat((float) clickParameters.shortFilter);
			file.writeWinShort(0); // snapInt
			file.writeWinFloat(0); // snapDur
			file.writeWinInt(0); // snapStart
			file.writeWinInt(0); // SnapEnd
			file.writeWinShort(0); // threshoff not uses in PAM version
			file.writeWinShort(0); // threshon not uses in PAM version
			file.writeWinShort(clickParameters.preSample);
			file.writeWinShort(clickParameters.postSample);
			file.writeWinShort(clickParameters.maxLength);
			file.writeWinShort(0); // snapGain
			file.writeBoolean(false); // T2Active
			file.writeBoolean(false); // RunClickIdentification
			file.writeWinFloat((float) clickParameters.longFilter2);
			file.writeWinShort(clickParameters.minSep);
			file.writeBoolean(false); // FVRemove
			file.writeBoolean(false); // FVRecalc
			file.writeBoolean(false); // invertChannel
			file.writeByte(0);
			file.writeWinInt(0); // spare 2
			file.writeWinInt(0); // fVeto
			file.writeBoolean(false);
			file.writeWinShort(0); // BigThreshold
			file.writeWinFloat(0);
			file.writeBoolean(false); // measure noies
			file.writeWinFloat(0);
			file.writeWinFloat(0);
			file.writeWinFloat(-170f);
			file.writeWinFloat(1);

		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	synchronized boolean readAnalParms(ClickParameters clickParameters) {

		// float alpha_long, alpha_short;
		// short SnapInt; // interval between snap shots of analysis
		// float SnapDur; // and duration of snapshot in seconds
		// unsigned SnapStart; // start in samples
		// unsigned SnapEnd;
		// short threshoff, threshon;
		// short presample, postsample, datalength;
		// short SnapGain;
		// bool T2Active;
		// bool RunClickIdentification;
		// float alpha_long2;
		// short minsep;
		// // FVRemove and FVRecalc were both BOOl, have changed to bool and
		// added extras
		// bool FVRemove;
		// bool FVReCalc;
		// bool invertChannel;
		// uint8 Spare1;
		// uint32 Spare2;
		// int FVeto;
		// bool BigVeto;
		// USHORT BigThreshold;
		// float BigSeconds;
		// bool MeasureNoise;
		// float NoiseInterval;
		// float MinEnergy;
		// float HydrophoneSensitivity;
		// float ADCPeakToPeak;

		try {
			clickParameters.longFilter = file.readWinFloat();
			clickParameters.shortFilter = file.readWinFloat();
			file.readWinShort(); // snapInt
			file.readWinFloat(); // snapDur
			file.readWinInt(); // snapStart
			file.readWinInt(); // SnapEnd
			file.readWinShort(); // threshoff not uses in PAM version
			file.readWinShort(); // threshon not uses in PAM version
			clickParameters.preSample = file.readWinShort();
			clickParameters.postSample = file.readWinShort();
			clickParameters.maxLength = file.readWinShort();
			file.readWinShort(); // snapGain
			file.readBoolean(); // T2Active
			file.readBoolean(); // RunClickIdentification
			clickParameters.longFilter2 = file.readWinFloat();
			clickParameters.minSep = file.readWinShort();
			file.readBoolean(); // FVRemove
			file.readBoolean(); // FVRecalc
			file.readBoolean(); // invertChannel
			file.readByte();
			file.readWinInt(); // spare 2
			file.readWinInt(); // fVeto
			file.readBoolean();
			file.readWinShort(); // BigThreshold
			file.readWinFloat();
			file.readBoolean(); // measure noies
			file.readWinFloat();
			file.readWinFloat();
			file.readWinFloat();
			file.readWinFloat();

		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}


	/*
	 * Haven't actually included this yet.
	 */
	synchronized boolean writeAnalType(ClickParameters clickParameters) {

		/*
		 * 
#define ANALTYPESPARES 10
// these are the same as the NI trad DAQ ones and can be used directly in RunNatInst
#define INPUT_DIFF 0
#define INPUT_RSE  1
#define INPUT_NRSE 2
class ANALTYPE
{
  public:
    int InputDevice;
    int TriggerMethod;
    int PlayBack;
    int Identification;
    int BearingCalc;
    int BufferLength;
    int BoardNum;
    short nBits;
    float WavStartTime;
    int16 BoardGain;
    bool LoopWavFile;
    int16 InputType;      
    int8 multiChannel[NCHAN];
    char Spares[ANALTYPESPARES];
    bool StoreUnfilteredData;
    uint32 WavStartSample();
    ANALTYPE();
    short GetNumBits();
    bool checkMultiChannels(int nChannels, bool autoFix);
};
		 */
		int channels = clickDetector.getClickControl().clickParameters.getChannelBitmap();
		int ch0 = PamUtils.getNthChannel(0, channels);
		int ch1 = PamUtils.getNthChannel(1, channels);
		try {
			file.writeWinInt(getInputDevice());
			file.writeWinInt(1); // analmethod 1
			file.writeWinInt(1); // analmethod 1
			file.writeWinInt(1); // analmethod 1
			file.writeWinInt(1); // analmethod 1
			file.writeWinInt(6); // analmethod 1
			file.writeWinInt(0); // analmethod 1
			file.writeWinShort(16); // would be good to get this right !
			file.writeWinFloat(0);
			file.writeWinShort(1);
			file.writeByte(0);
			file.writeWinShort(0);
			file.writeByte(ch0);
			file.writeByte(ch1);
			for (int i = 0; i < 10; i++) {
				file.writeByte(0);
			}
			file.writeByte(1);

		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	synchronized boolean readAnalType(ClickParameters clickParameters) {

		/*
		 * 
#define ANALTYPESPARES 10
// these are the same as the NI trad DAQ ones and can be used directly in RunNatInst
#define INPUT_DIFF 0
#define INPUT_RSE  1
#define INPUT_NRSE 2
class ANALTYPE
{
  public:
    int InputDevice;
    int TriggerMethod;
    int PlayBack;
    int Identification;
    int BearingCalc;
    int BufferLength;
    int BoardNum;
    short nBits;
    float WavStartTime;
    int16 BoardGain;
    bool LoopWavFile;
    int16 InputType;      
    int8 multiChannel[NCHAN];
    char Spares[ANALTYPESPARES];
    bool StoreUnfilteredData;
    uint32 WavStartSample();
    ANALTYPE();
    short GetNumBits();
    bool checkMultiChannels(int nChannels, bool autoFix);
};
		 */
		int channels = clickDetector.getClickControl().clickParameters.getChannelBitmap();
		int ch0 = PamUtils.getNthChannel(0, channels);
		int ch1 = PamUtils.getNthChannel(1, channels);
		int inputDev;
		try {
			inputDev = file.readWinInt();
			file.readWinInt(); // analmethod 1
			file.readWinInt(); // analmethod 1
			file.readWinInt(); // analmethod 1
			file.readWinInt(); // analmethod 1
			file.readWinInt(); // analmethod 1
			file.readWinInt(); // analmethod 1
			file.readWinShort(); // would be good to get this right !
			file.readWinFloat();
			file.readWinShort();
			file.readByte();
			file.readWinShort();
			file.readByte();
			file.readByte();
			for (int i = 0; i < 10; i++) {
				file.readByte();
			}
			file.readByte();

		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	int getInputDevice() {
		AcquisitionProcess daqProcess = null;
		try {
			daqProcess = (AcquisitionProcess) clickDetector.getSourceProcess();
		}
		catch (ClassCastException ex) {
			return 0;
		}
		if (daqProcess.getAcquisitionControl().findDaqSystem(null).isRealTime()) {
			return 1; // sound card
		}
		else {
			return 2; // wav file. 
		}
	}


	synchronized boolean writeFiltParms(ClickParameters clickParameters) {
		final int FILTPARSPARES = 19;
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		float hSep = 2.0f;
		if (currentArray != null) {
			if (currentArray.getHydrophoneCount() >= 2) {
				hSep = (float) currentArray.getSeparationInSeconds(0,1)*1000;
			}
		}
		int fftLength;
		try {
			file.writeWinShort(0);
			file.writeWinShort(0);
			file.writeWinShort(0);
			file.writeWinShort(0);
			file.writeWinFloat(hSep);
			file.writeWinShort(fftLength = FastFFT.nextBinaryExp(clickParameters.maxLength));
			file.writeWinShort(FastFFT.log2(fftLength));
			file.writeWinShort(1); //window func
			file.writeByte(0);
			for (int i = 0; i < FILTPARSPARES; i++) {
				file.writeByte(0);
			}
		}
		catch (IOException ex) {
			return false;
		}
		return true;
	}

	synchronized boolean readFiltParms(ClickParameters clickParameters) {
		final int FILTPARSPARES = 19;
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		float hSep = 2.0f;
		if (currentArray != null) {
			if (currentArray.getHydrophoneCount() >= 2) {
				hSep = (float) currentArray.getSeparationInSeconds(0,1)*1000;
			}
		}
		int fftLength;
		try {
			file.readWinShort();
			file.readWinShort();
			file.readWinShort();
			file.readWinShort();
			hSep = file.readWinFloat();
			clickParameters.maxLength = file.readWinShort();
			file.readWinShort();
			file.readWinShort(); //window func
			file.readByte();
			for (int i = 0; i < FILTPARSPARES; i++) {
				file.readByte();
			}
		}
		catch (IOException ex) {
			return false;
		}
		return true;
	}

	synchronized boolean writeSectionTime(long timeMS, int duration, short whyStopped) {

		// SYSTEMTIME StartTime;
		// unsigned Duration;
		// WORD WhyStopped;
		// char Spares[SECTIMESPARES];
		final int SECTIONTIMESPARES = 18;
		writeSystemTime(timeMS);
		try {
			file.writeWinInt(duration);
			file.writeWinShort(whyStopped);
			for (int i = 0; i < SECTIONTIMESPARES; i++) {
				file.writeByte(0);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	synchronized boolean readSectionTime() {

		// SYSTEMTIME StartTime;
		// unsigned Duration;
		// WORD WhyStopped;
		// char Spares[SECTIMESPARES];
		short whyStopped;
		final int SECTIONTIMESPARES = 18;
		fileStartTime = readSystemTime();
		//		System.out.println("File start time = " + PamCalendar.formatDateTime(fileStartTime));
		try {
			fileSamples = file.readWinInt();
			whyStopped = (short) file.readWinShort();
			for (int i = 0; i < SECTIONTIMESPARES; i++) {
				file.readByte();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	synchronized boolean writeSystemTime(long timeMS) {
		// WORD wYear;
		// WORD wMonth;
		// WORD wDayOfWeek;
		// WORD wDay;
		// WORD wHour;
		// WORD wMinute;
		// WORD wSecond;
		// WORD wMilliseconds;
		rainbowCalendar.setTimeInMillis(timeMS);

		try {
			file.writeWinShort(rainbowCalendar.get(Calendar.YEAR));
			file.writeWinShort(rainbowCalendar.get(Calendar.MONTH)+1); // c's SYSTEMTIME has Jan = 1, java has Jan = 0;
			file.writeWinShort(rainbowCalendar.get(Calendar.DAY_OF_WEEK));
			file.writeWinShort(rainbowCalendar.get(Calendar.DAY_OF_MONTH));
			file.writeWinShort(rainbowCalendar.get(Calendar.HOUR_OF_DAY));
			file.writeWinShort(rainbowCalendar.get(Calendar.MINUTE));
			file.writeWinShort(rainbowCalendar.get(Calendar.SECOND));
			file.writeWinShort(rainbowCalendar.get(Calendar.MILLISECOND));

			// file.writeWinShort(1);
			// file.writeWinShort(2);
			// file.writeWinShort(3);
			// file.writeWinShort(4);
			// file.writeWinShort(5);
			// file.writeWinShort(6);
			// file.writeWinShort(7);
			// file.writeWinShort(8);

		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}

		return true;
	}

	synchronized long readSystemTime() {
		// WORD wYear;
		// WORD wMonth;
		// WORD wDayOfWeek;
		// WORD wDay;
		// WORD wHour;
		// WORD wMinute;
		// WORD wSecond;
		// WORD wMilliseconds;
		//		rainbowCalendar.setTimeInMillis(timeMS);
		int year, month, weekDay, monthDay, hour, minute, second, millisecond;

		try {
			year = file.readWinShort();
			if (year < 1900) {
				year += 256;
			}
			month = file.readWinShort(); // c's SYSTEMTIME has Jan = 1, java has Jan = 0;
			weekDay = file.readWinShort();
			monthDay = file.readWinShort();
			hour = file.readWinShort();
			minute = file.readWinShort();
			second = file.readWinShort();
			millisecond = file.readWinShort();
			rainbowCalendar.set(Calendar.YEAR, year);
			rainbowCalendar.set(Calendar.MONTH, month-1);
			rainbowCalendar.set(Calendar.DAY_OF_WEEK, weekDay);
			rainbowCalendar.set(Calendar.DAY_OF_MONTH, monthDay);
			rainbowCalendar.set(Calendar.HOUR_OF_DAY, hour);
			rainbowCalendar.set(Calendar.MINUTE, minute);
			rainbowCalendar.set(Calendar.SECOND, second);
			rainbowCalendar.set(Calendar.MILLISECOND, millisecond);

			// file.writeWinShort(1);
			// file.writeWinShort(2);
			// file.writeWinShort(3);
			// file.writeWinShort(4);
			// file.writeWinShort(5);
			// file.writeWinShort(6);
			// file.writeWinShort(7);
			// file.writeWinShort(8);

		} catch (IOException ex) {
			ex.printStackTrace();
			return 0;
		}

		return rainbowCalendar.getTimeInMillis();
	}

	/*
	 * 
#define WAVNAMESPARES     128
#define WAVFILE_OK          0
#define WAVFILE_CANCEL      1
#define WAVFILE_WRONGFORMAT 2
class WAVFILEINFO
{
  public:
    char WavFileName[MAX_PATH];
    char Spares[WAVNAMESPARES];
    WAVFILEINFO();
    void ClearInfo();
};
	 */
	synchronized private boolean writeWavFileInfo() {
		byte[] nameData = new byte[MAX_PATH + 128];

		String fileName = getSourceWavFileName();
		char c;
		if (fileName != null) {
			for (int i = 0; i < Math.min(fileName.length(), MAX_PATH -1); i++) {
				nameData[i] = (byte) fileName.charAt(i);
				c = (char) nameData[i];
				//				System.out.print(c);
			}
		}
		//		System.out.println("file name data");
		try {
			for (int i = 0; i < nameData.length; i++) {
				file.writeByte(nameData[i]);
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return true;
	}
	synchronized private boolean readWavFileInfo() {
		byte[] nameData = new byte[MAX_PATH + 128];

		//		String fileName = getSourceWavFileName();
		//		char c;
		//		if (fileName != null) {
		//			for (int i = 0; i < Math.min(fileName.length(), MAX_PATH -1); i++) {
		//				nameData[i] = (byte) fileName.charAt(i);
		//				c = (char) nameData[i];
		//				System.out.print(c);
		//			}
		//		}
		//		System.out.println("file name data");
		try {
			for (int i = 0; i < nameData.length; i++) {
				nameData[i] = file.readByte();
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return true;
	}

	private String getSourceWavFileName() {
		File file = getSourceWavFile();
		if (file == null) {
			return null;
		}

		String fileName = file.getAbsolutePath();
		if (fileName == null) return null;
		if (fileName.length() >= MAX_PATH) {
			fileName = file.getName();
		}

		return fileName;
	}

	private File getSourceWavFile() {

		// try to work out if the data source was a wav file or not.
		PamProcess sourceProcess = clickDetector.getSourceProcess();
		/*
		 * hopefully it's an acquisition process !
		 * 
		 */
		AcquisitionProcess daqProcess = null;
		if (sourceProcess.getClass() == AcquisitionProcess.class) {
			daqProcess = (AcquisitionProcess) sourceProcess;
		}

		if (daqProcess == null) {
			return null;
		}
		DaqSystem daqSystem = daqProcess.getAcquisitionControl().findDaqSystem(null);
		if (daqSystem == null) return null;
		if (daqSystem.isRealTime() == true){
			return null;
		}
		// looks like it's file or file folder.
		FileInputSystem fileSystem = null;
		try {
			fileSystem = (FileInputSystem) daqSystem;
		}
		catch (Exception ex) {
			return null;
		}
		if (fileSystem == null){
			return null;
		}

		return fileSystem.getCurrentFile();
	}

	public String getStorageName() {
		return fileName;
	}

	@Override
	synchronized public boolean checkStorage() {
		// only thing to check here is that the directory exists.
		ClickParameters clickParameters = clickDetector.getClickControl().clickParameters;

		String dirName = clickParameters.storageDirectory;

		return checkStorage(dirName);

	}

	static public boolean checkStorage(String dirName) {
		File file = new File(dirName);
		String dlgTitle = "Click Detector Rainbow File Storage";
		if (file.exists() && file.isDirectory()) {
			return true;
		} else if (file.exists() == false) {
			int ans = JOptionPane.showOptionDialog(null, "Folder " + dirName
					+ " does not exist. \nWould you like to create it ?",
					dlgTitle,
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
					null, null, null);
			if (ans == JOptionPane.NO_OPTION) {
				return false;
			}
			if (ans == JOptionPane.YES_OPTION) {
				try {
					if (file.mkdirs() == false) {
						return checkStorage(dirName);
					}
					FileFunctions.setNonIndexingBit(file);
				} catch (SecurityException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			JOptionPane
			.showMessageDialog(null, dirName
					+ " is a file, not a folder \nCreate a new folder for click storage",
					dlgTitle, JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	/**
	 * @return the file start time in standard Java milliseconds
	 */
	public long getFileStartTime() {
		return fileStartTime;
	}
	/**
	 * @return the file end time in standard Java milliseconds
	 */
	public long getFileEndTime() {
		return fileStartTime + (long) (fileSamples * 1000 / sampleRate);
	}

	/**
	 * 
	 * @return the files sample rate in Hz
	 */
	public float getSampleRate() {
		return sampleRate;
	}

	/**
	 * Get the current File object
	 * @return File object.
	 */
	public File getRainbowFile() {
		return rainbowFile;
	}

	/**
	 * @return the nClicksInFile
	 */
	public int getNClicksInFile() {
		return nClicksInFile;
	}
}
