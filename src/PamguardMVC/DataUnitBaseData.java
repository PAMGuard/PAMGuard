package PamguardMVC;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Class for data unit basic data. this has been separated into 
 * a separate class so that it can be passed around more easily during
 * file and network read write ops when a whole data unit may not be 
 * available. 
 * @author dg50
 *
 */
public class DataUnitBaseData implements Cloneable {

	
	public static final short S1_TIMEMILLISECONDS 	= 0x1;
	public static final short S1_TIMENANOSECONDS 	= 0x2;
	public static final short S1_CHANNELMAP         = 0x4;
	public static final short S1_UID 				= 0x8;
	public static final short S1_STARTSAMPLE        = 0x10;
	public static final short S1_SAMPLEDURATION     = 0x20;
	public static final short S1_FREQUENCYLIMITS	= 0x40;
	public static final short S1_MILLISDURATION	    = 0x80;
	public static final short S1_TIMEDELAYSSECONDS  = 0x100;
	public static final short S1_HASBINARYANNOTATIONS=0x200;
	public static final short S1_HASSEQUENCEMAP		= 0x400; // 2017/12/12 added for file version = 6
	public static final short S1_HASNOISE           = 0x800; // 2021-07-24 Adding standardised noise measurement. 
	public static final short S1_HASSIGNAL          = 0x1000; // 2021-07-24 Adding standardised signal measurement. 
	public static final short S1_HASSIGNALEXCESS    = 0x2000; // 2021-07-24 Adding detection statistics where possible. 

	public static final short S1_SEARCHS2           = -0x8000;
	
//	private ArrayList<DataThing> dataThings = new ArrayList<>();
	
	/**
	 * Amplitude value is in dB relative to 1 micropascal
	 */
	static public final int AMPLITUDE_SCALE_DBREMPA = 0;
	/**
	 * Amplitude scale is linear relative to full scale
	 */
	static public final int AMPLITUDE_SCALE_LINREFSD = 1;
//	/**
//	 * Amplitude is taken from the spectrum (not spectrum level)
//	 */
//	static public final int AMPLITUDE_SCALE_SPECTRUM = 2;

	/**
	 * Used to calculate the number of bytes in the base data.  The order
	 * of the numbers in the array must match the order of the data identifiers
	 * given above.  The value in array is related to how the data is actually
	 * written to the binary file, and not what type the data itself is.  Data
	 * types and corresponding byte sizes are:
	 * <ul>short = 2</ul>
	 * <ul>int = 4</ul>
	 * <ul>float = 4</ul>
	 * <ul>long = 8</ul>
	 * For example, the first number in the array corresponds to
	 * S1_TIMEMILLISECONDS.  This is a long variable, and it's written
	 * as a long to the binary file.  Therefore the value is 8.  The
	 * second number corresponds to S1_TIMENANOSECONDS, and is the same.  The
	 * 6th number corresponds to S1_SAMPLEDURATION which is also a long variable,
	 * however it's written to the binary file as an int.  Therefore the
	 * value in the array is 4 not 8.<p>
	 * Similarly, the 7th value in the array
	 * corresponds to S1_FREQUENCYLIMITS, which is a 2-cell double array.
	 * That would mean it should take 16 bytes to store (2*8=16) BUT it's
	 * actually being stored as a float, so the value in binarySpace is
	 * actually 8 (2*4=8).<p>
	 * Note also that 9th value corresponds to S1_TIMEDELAYSSECONDS.  This is
	 * a double array of variable length.  Therefore, the value in the array below
	 * is 0 and it is handled as a special case in the getBaseDataBinaryLength method.
	 */
	public int[] binarySpace = {8, // S1_TIMEMILLISECONDS, written as long
								8, // S1_TIMENANOSECONDS, written as long
								4, // S1_CHANNELMAP, written as int
								8, // S1_UID, written as long
								8, // S1_STARTSAMPLE, written as long
								4, // S1_SAMPLEDURATION, written as int
								8, // S1_FREQUENCYLIMITS, written as 2*float
								4, // S1_MILLISDURATION, written as float
								0, // S1_TIMEDELAYSSECONDS, handled in getBaseDataBinaryLength as a special case
								0, // annotation data handled in a completely different way. 
								4, // S1_HASSEQUENCEMAP, written as int
								4, // S1_HASNOISE, written as float
								4, // S1_HASSIGNAL, written as float
								4};// S1_HASSIGNALEXCESS written as float
	
  	/**
	 * Special flag to say that there is another set of up to 16 more flags
	 * indicating other data types. 
	 */
	public static final short S1_S2 = (short) 0x10000;

	/**
	 * Contents of the first set of flags. Future versions may 
	 * extend to multiple sets, but for now, we only need s1. 
	 */
	private short s1Contents;

	/**
	 * time the NewPamDataUnit was created based using standard Java time
	 */
	private long timeMilliseconds;

	/**
	 * Time in nanoseconds. Will be null for old data. Mostly 
	 * used when localising multiple data streams synched with GPS PPS. 
	 */
	private Long timeNanoseconds;
	
	/**
	 * Start sample used in Acoustic Data Units. 
	 */
	private Long startSample;
	
	/**
	 * Duration in samples, used in Acoustic Data Units.
	 * (this was long in original data unit, but that's crazy. We'll 
	 * keep the basic data as Long, but will only save as Integer.  
	 */
	private Long sampleDuration;

	/**
	 * Duration in milliseconds. 
	 */
	private Double millisecondDuration;
	
	/**
	 * map of channels used in the data. <p>
	 * N.B the PamDataBlock also has a channelBitmap. The channelBitmap in
	 * a PamDataBlock is a list of channels that might be present in the
	 * data units. The channelBitmap in the data unit represents channels 
	 * that are actually present in that data unit. For instance, if sampling 
	 * 2 channels (ch0 and ch1) of raw audio data, the channelBitmap in the 
	 * data block would equal 3, but the channel maps in the data units (which 
	 * contain one channel of data each) will alternate between 1 and 2. 
	 * <p> note that these are the software channels and that there may not be a 1:1 
	 * relationship between software channels and hydrophones. 
	 * <p>
	 * This parameter is included in PamDataUnit and not in the subclass AcousticDataUnit
	 * since it is sometimes needed by non acoustic data. 
	 */
	protected int channelBitmap;
	
	
	/**
	 * A sequence map with very similar function to channelBitmap introduced 
	 * to support beam forming of data, in which case beam output has a multi channel channelBitmap
	 * but still needs a uniquely identifiable 'channel' sequence number for 
	 * displays, downstream detectors, etc. 
	 */
	private Integer sequenceBitmap;

	/**
	 * Unique identifier for this data unit. 
	 */
	private long uid;

	/**
	 * Frequency limits of the data in the data unit. 
	 */
	private double[] frequency = { 0., 0. };
	
	/**
	 * measured amplitude contains the most easily used value of 
	 * amplitude. The constants defined above tell us how it was measured (this list
	 * will need extending, as will the getAmplitudedB function. 
	 */
	private double measuredAmplitude;
	
	/**
	 * Signal excess. This is some sort of detection statistic expressed
	 * in decibels. It's nature will vary a bit between detectors and MAY relate
	 * to SNR, but that depends on how you define SNR. What it primarily tells us 
	 * is how close to the limits of detection we are. 
	 */
	private float signalExcess;
	
	/**
	 * Noise background
	 */
	private Float noiseBackground;
	
	/**
	 * Signal sound pressure level
	 */
	private Float signalSPL;

	/**
	 * the type of amplitude measurements, as specified by the list of constants
	 */
	private int measuredAmplitudeType;
	
	/**
	 * 
	 */
	private double calculatedAmlitudeDB = Double.NaN;
	
	/**
	 * Array of time delays, in seconds
	 */
	private double[] timeDelaysSeconds = null;
	

	public DataUnitBaseData() {	
		
	}

	/**
	 * @param timeMilliseconds
	 * @param channelBitmap
	 * @param timeNanoseconds
	 */
	public DataUnitBaseData(long timeMilliseconds, int channelBitmap, Long timeNanoseconds) {
		super();
		setTimeMilliseconds(timeMilliseconds);
		setChannelBitmap(channelBitmap);
		setTimeNanoseconds(timeNanoseconds);
	}
	
	/**
	 * Constructor used to hand the basic data to AcousticDataUnit. 
	 * @param timeMilliseconds
	 * @param channelBitmap
	 * @param startSample
	 * @param duration
	 */
	public DataUnitBaseData(long timeMilliseconds,
			int channelBitmap, long startSample, long duration) {
		setTimeMilliseconds(timeMilliseconds);
		setChannelBitmap(channelBitmap);
		setStartSample(startSample);
		setSampleDuration(duration);
//		channelMapThing = new IntegerThing();
		
	}

	/**
	 * @param timeMilliseconds
	 * @param channelBitmap
	 * @param timeNanoseconds
	 */
	public DataUnitBaseData(long timeMilliseconds, int channelBitmap) {
		super();
		setTimeMilliseconds(timeMilliseconds);
		setChannelBitmap(channelBitmap);
	}

	/**
	 * @return the timeMilliseconds
	 */
	public long getTimeMilliseconds() {
		return timeMilliseconds;
	}

	/**
	 * @param timeMilliseconds the timeMilliseconds to set
	 */
	public void setTimeMilliseconds(long timeMilliseconds) {
		this.timeMilliseconds = timeMilliseconds;
		s1Contents = setContents(s1Contents, S1_TIMEMILLISECONDS, true);
	}

	/**
	 * @return the timeNanoseconds
	 */
	public Long getTimeNanoseconds() {
		return timeNanoseconds;
	}

	/**
	 * @param timeNanoseconds the timeNanoseconds to set
	 */
	public void setTimeNanoseconds(Long timeNanoseconds) {
		this.timeNanoseconds = timeNanoseconds;
		s1Contents = setContents(s1Contents, S1_TIMENANOSECONDS, timeNanoseconds != null);
	}

	/**
	 * @return the channelBitmap
	 */
	public int getChannelBitmap() {
		return channelBitmap;
	}

	/**
	 * @param channelBitmap the channelBitmap to set
	 */
	public void setChannelBitmap(int channelBitmap) {
		this.channelBitmap = channelBitmap;
		s1Contents = setContents(s1Contents, S1_CHANNELMAP, true);
	}

	/**
	 * @return the uid
	 */
	public long getUID() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUID(long uid) {
		this.uid = uid;
		s1Contents = setContents(s1Contents, S1_UID, true);
	}

	/**
	 * @return the startSample
	 */
	public Long getStartSample() {
		return startSample;
	}

	/**
	 * @param startSample the startSample to set
	 */
	public void setStartSample(Long startSample) {
		this.startSample = startSample;
		s1Contents = setContents(s1Contents, S1_STARTSAMPLE, startSample != null);
	}

	/**
	 * @return the sampleDuration
	 */
	public Long getSampleDuration() {
		return sampleDuration;
	}

	/**
	 * @param sampleDuration the sampleDuration to set
	 */
	public void setSampleDuration(Long sampleDuration) {
		this.sampleDuration = sampleDuration;
		s1Contents = setContents(s1Contents, S1_SAMPLEDURATION, sampleDuration != null);
	}
	
	/**
	 * @return the millisecondDuration
	 */
	public Double getMillisecondDuration() {
		return millisecondDuration;
	}

	/**
	 * @param millisecondDuration the millisecondDuration to set
	 */
	public void setMillisecondDuration(Double millisecondDuration) {
		this.millisecondDuration = millisecondDuration;
		s1Contents = setContents(s1Contents, S1_MILLISDURATION, millisecondDuration != null);
	}

	/**
	 * @return the s1Contents
	 */
	public short getS1Contents() {
		return s1Contents;
	}

	/**
	 * Set the appropriate bit in the contents flag so say if it's 
	 * present or not.  
	 * @param set bitmap to set (e.g. s1Contents)
	 * @param id flag Id, e.g. S1_HASNOISE
	 * @param exists true if it's present, false otherwise. 
	 * @return updated bitmap
	 */
	private short setContents(short set, short id, boolean exists) {
		if (exists) {
			set |= id;
		}
		else {
			set &= ~id;
		}
		return set;
	}

	/**
	 * Get the number of bytes which are going to be written 
	 * to save all this data. Note that currently the S1_TIMEDELAYSSECONDS
	 * flag corresponds to the timeDelaysSeconds array, which is variable
	 * length.  Therefore it is handled as a special case in the code below.
	 * @return number of bytes to be written. 
	 */
	public int getBaseDataBinaryLength() {
		int len = 2; // size of the s1Contents
		for (int i = 0; i < binarySpace.length; i++) {
			if ((s1Contents&(1<<i)) != 0) {
				len += binarySpace[i];
			}
		}
		
		// S1_TIMEDELAYSSECONDS special case.  Since the timeDelaysSeconds variable
		// is a double array of variable length, the size must be calculated here.  
		if ((s1Contents & S1_TIMEDELAYSSECONDS) != 0) {
			len += 2;	// a short value holding the number of time delays
			for (int i=0; i < timeDelaysSeconds.length; i++) {
				len += 4;	// even though the time delays are doubles, they are stored as floats
			}
		}
		return len;
	}

	/**
	 * Read basic data for data units from an input stream. 
	 * @param inputStream Input stream (generally a file, network socket or byte array)
	 * @param fileVersion Format version number. 
	 * @return true
	 * @throws IOException
	 */
	public boolean readBaseData(DataInputStream inputStream, int fileVersion) throws IOException {
		setTimeMilliseconds(inputStream.readLong());
		if (fileVersion == 2) {
			setTimeNanoseconds(inputStream.readLong());
			setChannelBitmap(inputStream.readInt());
		}
		if (fileVersion >= 3) {
			s1Contents = inputStream.readShort();
			if ((s1Contents & S1_TIMENANOSECONDS) != 0) {
				setTimeNanoseconds(inputStream.readLong());
			}
			if ((s1Contents & S1_CHANNELMAP) != 0) {
				setChannelBitmap(inputStream.readInt());
			}
			if ((s1Contents & S1_UID) != 0) {
				setUID(inputStream.readLong());
			}
			if ((s1Contents & S1_STARTSAMPLE) != 0) {
				setStartSample(inputStream.readLong());
			}
			if ((s1Contents & S1_SAMPLEDURATION) != 0) {
				setSampleDuration((long) inputStream.readInt());
			}
		}
		if (fileVersion >= 4) {
			if ((s1Contents & S1_FREQUENCYLIMITS) != 0) {
				double[] freqLims = {(double) inputStream.readFloat(), (double) inputStream.readFloat()};
				setFrequency(freqLims);
			}
			if ((s1Contents & S1_MILLISDURATION) != 0) {
				setMillisecondDuration(new Double(inputStream.readFloat()));
			}
			if ((s1Contents & S1_TIMEDELAYSSECONDS) != 0) {
				short numDelays = inputStream.readShort();
				if (numDelays<0) {
					System.out.println("DataUnitBase: negative array size: ");
					return false; 
				}
				timeDelaysSeconds=new double[numDelays];
				for (int i=0; i<numDelays; i++) {
					timeDelaysSeconds[i]=inputStream.readFloat();
				}
			}
		}
		if (fileVersion >= 6) {
			if ((s1Contents & S1_HASSEQUENCEMAP) != 0) {
				setSequenceBitmap(inputStream.readInt());
			}
		}
		if ((s1Contents & S1_HASNOISE) != 0) {
			setNoiseBackground(inputStream.readFloat());
		}
		if ((s1Contents & S1_HASSIGNAL) != 0) {
			setSignalSPL(inputStream.readFloat());
		}
		if ((s1Contents & S1_HASSIGNALEXCESS) != 0) {
			setSignalExcess(inputStream.readFloat());
		}
		return true;
	}
	/**
	 * Write base Data Unit data to an output stream
	 * @param outputStream output stream (generally a file, network socket or byte array)
	 * @param fileVersion file version
	 * @return true
	 * @throws IOException
	 */
	public boolean writeBaseData(DataOutputStream outputStream, int fileVersion) throws IOException {
		outputStream.writeLong(timeMilliseconds);
		outputStream.writeShort(s1Contents);
		if ((s1Contents & S1_TIMENANOSECONDS) != 0) {
			writeLong(outputStream, timeNanoseconds);
		}
		if ((s1Contents & S1_CHANNELMAP) != 0) {
			writeInt(outputStream, channelBitmap);
		}
		if ((s1Contents & S1_UID) != 0) {
			writeLong(outputStream, uid);
		}
		if ((s1Contents & S1_STARTSAMPLE) != 0) {
			writeLong(outputStream, startSample);
		}
		if ((s1Contents & S1_SAMPLEDURATION) != 0) {
			writeInt(outputStream, sampleDuration);
		}
		if ((s1Contents & S1_FREQUENCYLIMITS) != 0) {
			writeFloat(outputStream, (float) frequency[0]);
			writeFloat(outputStream, (float) frequency[1]);
		}
		if ((s1Contents & S1_MILLISDURATION) != 0) {
			writeFloat(outputStream, new Float(millisecondDuration));
		}
		if ((s1Contents & S1_TIMEDELAYSSECONDS) != 0) {
			writeShort(outputStream, (short) timeDelaysSeconds.length);
			for (int i=0; i<timeDelaysSeconds.length; i++) {
				writeFloat(outputStream, (float) timeDelaysSeconds[i]);
			}
		}
		if ((s1Contents & S1_HASSEQUENCEMAP) != 0) {
			writeInt(outputStream, sequenceBitmap);
		}
		if ((s1Contents & S1_HASNOISE) != 0) {
			writeFloat(outputStream, noiseBackground);
		}
		if ((s1Contents & S1_HASSIGNAL) != 0) {
			writeFloat(outputStream, signalSPL);
		}
		if ((s1Contents & S1_HASSIGNALEXCESS) != 0) {
			writeFloat(outputStream, signalExcess);
		}
		return true;
	}
	
	/**
	 * Write a float to the output stream, replacing with 0 if it's null.
	 * @param outputStream
	 * @param floatValue
	 * @throws IOException
	 */
	private void writeFloat(DataOutputStream outputStream, Float floatValue) throws IOException  {
		if (floatValue == null) {
			outputStream.writeFloat(0);
		}
		else {
			outputStream.writeFloat(floatValue);
		}
	}

	/**
	 * Write a long to the output stream, replacing with 0 if it's null.
	 * @param outputStream
	 * @param longValue
	 * @throws IOException
	 */
	private void writeLong(DataOutputStream outputStream, Long longValue) throws IOException {
		if (longValue == null) {
			outputStream.writeLong(0);
		}
		else {
			outputStream.writeLong(longValue);
		}
	}
	
	/**
	 * Write an integer to the output stream, replacing with 0 if it's null.
	 * @param outputStream
	 * @param longValue
	 * @throws IOException
	 */
	private void writeInt(DataOutputStream outputStream, Integer intValue) throws IOException {
		if (intValue == null) {
			outputStream.writeInt(0);
		}
		else {
			outputStream.writeInt(intValue);
		}
	}

	/**
	 * Write a Long value as a 32 bit integer. 
	 * @param outputStream 
	 * @param longValue
	 * @throws IOException 
	 */
	private void writeInt(DataOutputStream outputStream, Long longValue) throws IOException {
		if (longValue == null) {
			outputStream.writeInt(0);
		}
		else {
			long v = longValue;
			outputStream.writeInt((int) v);
		}
	}

	/**
	 * Write a short to the output stream, replacing with 0 if it's null.
	 * @param outputStream
	 * @param longValue
	 * @throws IOException
	 */
	private void writeShort(DataOutputStream outputStream, Short shortValue) throws IOException {
		if (shortValue == null) {
			outputStream.writeShort(0);
		}
		else {
			outputStream.writeShort(shortValue);
		}
	}
	
	/**
	 * Set the frequency limits
	 * @return
	 */
	public double[] getFrequency() {
		return frequency;
	}

	/**
	 * Get the current frequency limits for the data unit
	 * @param frequency
	 */
	public void setFrequency(double[] frequency) {
		this.frequency = frequency;
		s1Contents = setContents(s1Contents, S1_FREQUENCYLIMITS, frequency != null);
	}

	/**
	 * Get the measured amplitude
	 * @return
	 */
	public double getMeasuredAmplitude() {
		return measuredAmplitude;
	}

	/**
	 * set the measured amplitude
	 * @param measuredAmplitude
	 */
	public void setMeasuredAmplitude(double measuredAmplitude) {
		this.measuredAmplitude = measuredAmplitude;
	}

	public int getMeasuredAmplitudeType() {
		return measuredAmplitudeType;
	}

	public void setMeasuredAmplitudeType(int measuredAmplitudeType) {
		this.measuredAmplitudeType = measuredAmplitudeType;
	}

	public double getCalculatedAmlitudeDB() {
		return calculatedAmlitudeDB;
	}

	public void setCalculatedAmlitudeDB(double calculatedAmlitudeDB) {
		this.calculatedAmlitudeDB = calculatedAmlitudeDB;
	}

	public double[] getTimeDelaysSeconds() {
		return timeDelaysSeconds;
	}

	public void setTimeDelaysSeconds(double[] timeDelaysSeconds) {
		this.timeDelaysSeconds = timeDelaysSeconds;
		s1Contents = setContents(s1Contents, S1_TIMEDELAYSSECONDS, timeDelaysSeconds != null);
	}
	

	/**
	 * Extract any data from baseData that's not already 
	 * in this one, and add it !
	 * @param dataUnitBaseData
	 */
	public void mergeBaseData(DataUnitBaseData baseData) {

		short os1 = baseData.s1Contents;
		if (bitDiff(s1Contents, os1, S1_TIMENANOSECONDS) > 0) {
			setTimeNanoseconds(baseData.getTimeNanoseconds());
		}
		if (bitDiff(s1Contents, os1, S1_CHANNELMAP) > 0) {
			setChannelBitmap(baseData.getChannelBitmap());
		}
		if (bitDiff(s1Contents, os1, S1_UID) > 0) {
			setUID(baseData.getUID());
		}
		if (bitDiff(s1Contents, os1, S1_STARTSAMPLE) > 0) {
			setStartSample(baseData.getStartSample());
		}
		if (bitDiff(s1Contents, os1, S1_SAMPLEDURATION) > 0) {
			setSampleDuration(baseData.getSampleDuration());
		}
		if (bitDiff(s1Contents, os1, S1_FREQUENCYLIMITS) > 0) {
			setFrequency(baseData.getFrequency());
		}
		if (bitDiff(s1Contents, os1, S1_MILLISDURATION) > 0) {
			setMillisecondDuration(baseData.getMillisecondDuration());
		}
		if (bitDiff(s1Contents, os1, S1_TIMEDELAYSSECONDS) > 0) {
			setTimeDelaysSeconds(baseData.getTimeDelaysSeconds());
		}
		if (bitDiff(s1Contents, os1, S1_HASSEQUENCEMAP) > 0) {
			setSequenceBitmap(baseData.getSequenceBitmap());
		}
		
	}
	
	int bitDiff(int s1, int s2, int bit) {
		return (s2 & bit) - (s1 & bit);
	}

	/**
	 * @return the sequenceBitmap
	 */
	public Integer getSequenceBitmap() {
		return sequenceBitmap;
	}

	/**
	 * @param sequenceBitmap the sequenceBitmap to set
	 */
	public void setSequenceBitmap(Integer sequenceBitmap) {
		this.sequenceBitmap = sequenceBitmap;
		s1Contents = setContents(s1Contents, S1_HASSEQUENCEMAP, sequenceBitmap!=null);
	}
	
	/**
	 * @return the signalExcess
	 */
	public float getSignalExcess() {
		return signalExcess;
	}

	/**
	 * @param signalExcess the signalExcess to set
	 */
	public void setSignalExcess(float signalExcess) {
		this.signalExcess = signalExcess;
		s1Contents = setContents(s1Contents, S1_HASSIGNALEXCESS, true);
	}

	/**
	 * Quick method to set flag saying binary store has annotation data
	 * which will immediately follow the main data unit data. 
	 * @param has flag to say has annotation data. 
	 */
	public void setHasBinaryAnnotations(boolean has) {
		s1Contents = setContents(s1Contents, S1_HASBINARYANNOTATIONS, has);
	}
	
	/**
	 * 
	 * @return true if the header indicates that binary annotations are present. 
	 */
	public boolean isHasBinaryAnnotation() {
		return ((s1Contents & S1_HASBINARYANNOTATIONS) != 0);
	}

//	abstract public class DataThing<T> {
//				
//		protected int arrayPosition;
//		
//		abstract public int getWriteSize();
//		
//		public T getValue(Object[] dataArray) {
//			return (T) dataArray[arrayPosition];
//		};
//		
//		public void setValue(T value, Object[] dataArray, boolean[] dataList) {
//			dataArray[arrayPosition] = value;
//			dataList[arrayPosition] = (value != null);
//		}
//		
//		abstract public boolean writeValue(DataOutputStream dataOutputStream, Object[] dataArray, boolean[] dataList) throws IOException;
//		
//		abstract public T readValue(DataInputStream dataInputStream, Object[] dataArray, boolean[] dataList);
//	}
//	
//	public class IntegerThing extends DataThing<Integer> {
//
//		
//
//		/* (non-Javadoc)
//		 * @see PamguardMVC.DataUnitBaseData.DataThing#getWriteSize()
//		 */
//		@Override
//		public int getWriteSize() {
//			// TODO Auto-generated method stub
//			return 4;
//		}
//		
//	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DataUnitBaseData clone() {
		try {
			return (DataUnitBaseData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Set the end time in milliseconds. 
	 * @param endTime
	 */
	public void setEndTime(long endTime) {
		setMillisecondDuration((double) (endTime - getTimeMilliseconds()));
	}

	/**
	 * 
	 * @return The data units end time in milliseconds. If a duration has not been set this
	 * will be the same as the start time
	 */
	public long getEndTime() {
		long end = timeMilliseconds;
		if (millisecondDuration != null) {
			end += millisecondDuration;
		}
		return end;
	}

	/**
	 * @return the noiseBackground
	 */
	public Float getNoiseBackground() {
		return noiseBackground;
	}

	/**
	 * @param noiseBackground the noiseBackground to set
	 */
	public void setNoiseBackground(Float noiseBackground) {
		this.noiseBackground = noiseBackground;
		s1Contents = setContents(s1Contents, S1_HASNOISE, noiseBackground != null);
	}

	/**
	 * @return the signalSPL
	 */
	public Float getSignalSPL() {
		return signalSPL;
	}

	/**
	 * @param signalSPL the signalSPL to set
	 */
	public void setSignalSPL(Float signalSPL) {
		this.signalSPL = signalSPL;
		s1Contents = setContents(s1Contents, S1_HASSIGNAL, signalSPL != null);
	}
}
