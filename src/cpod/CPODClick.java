package cpod;

import PamUtils.PamCalendar;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

/**
 * CPOD click. 
 */
public class CPODClick extends PamDataUnit<PamDataUnit,SuperDetection> implements AcousticDataUnit {

	private short nCyc;
	private short bw;
	private short kHz;
	private short endF;
	private short spl;
	private short slope;
	private long iciSamples;
	private short[] rawData;
	
	/**
	 * The amplitude in dB. 
	 */
	private Double amplitudedB;
	
	/**
	 * Create a CPOD click. (This is used to load CPOD clicks from the binary store)
	 * 
	 * @param baseData - the base data.
	 * @param shortData -contains the raw CPOD data
	 * @return a new CPOD click. 
	 */
	public static CPODClick makeClick(DataUnitBaseData baseData, short[] shortData) {

//		int micros = t%200;
		short nCyc = shortData[3];
		short bw = shortData[4];
		short kHz = shortData[5];
		short endF = shortData[6];
		short spl = shortData[7];
		short slope = shortData[8];
		CPODClick cpodClick = new CPODClick(baseData.getTimeMilliseconds(), baseData.getStartSample(), 
				nCyc, bw, kHz, endF, spl, slope, shortData);
		return cpodClick;
	}
	
	/**
	 * Create a CPOD click. This is usually called whenever the CPOD click is imported from a CSV file. 
	 * 
	 * @param minuteMillis The time in miliiseconds of the minute block preceding the
	 * current click. 
	 * @param shortData
	 * @return
	 */
	public static CPODClick makeClick(CPODControl cpodControl, long minuteMillis, short[] shortData) {

		int t = shortData[0]<<16 | 
				shortData[1]<<8 |
				shortData[2]; // 5 microsec intervals !
		
		long tMillis = minuteMillis + t/200;
		// now a bit of time stretching. Need to get the start time and see how
		// different this is, then it's a linear stretch. 
		tMillis = cpodControl.stretchClicktime(tMillis);
		
		
		// do a sample number within the file as 5us intervals
		long fileSamples = t + minuteMillis * 200;
//		int micros = t%200;
		short nCyc = shortData[3];
		short bw = shortData[4];
		short kHz = shortData[5];
		short endF = shortData[6];
		short spl = shortData[7];
		short slope = shortData[8];
		CPODClick cpodClick = new CPODClick(tMillis, fileSamples, nCyc, bw, kHz, endF, spl, slope, shortData);
		return cpodClick;
	}
	
	/**
	 * Create a CPOD click. This is usually called whenever the CPOD click is imported from a CSV file. 
	 * 
	 * @param minuteMillis The time in milliseconds of the minute block preceding the
	 * current click. 
	 * @param shortData
	 * @return
	 */
	public static CPODClick makeClick(CPODControl2 cpodControl, long minuteMillis, short[] shortData) {

		int t = shortData[0]<<16 | 
				shortData[1]<<8 |
				shortData[2]; // 5 microsec intervals !
		long tMillis = minuteMillis + t/200;
		
		// now a bit of time stretching. Need to get the start time and see how
		// different this is, then it's a linear stretch. 
		tMillis = cpodControl.stretchClicktime(tMillis);
		
		
		// do a sample number within the file as 5us intervals
		long fileSamples = t + minuteMillis * 200;
//		int micros = t%200;
		short nCyc = shortData[3];
		short bw = shortData[4];
		short kHz = shortData[5];
		short endF = shortData[6];
		short spl = shortData[7];
		short slope = shortData[8];
		CPODClick cpodClick = new CPODClick(tMillis, fileSamples, nCyc, bw, kHz, endF, spl, slope, shortData);
		return cpodClick;
	}

	/**
	 * Constructor for a CPOD click. 
	 * @param tMillis - the time in millis. 
	 * @param fileSamples - the file samples
	 * @param nCyc - the number of cycles
	 * @param bw -the bandwidth in kHZ
	 * @param kHz - the frequency in kHz. 
	 * @param endF - the end frequency in kHz. 
	 * @param spl - the spl (0-255) unitless.
	 * @param slope - the slope
	 * @param shortData - the raw data. 
	 */
	public CPODClick(long tMillis, long fileSamples, short nCyc, short bw,
			short kHz, short endF, short spl, short slope, short[] shortData) {
		super(tMillis, 1, fileSamples, nCyc);
		this.nCyc = nCyc;
		this.bw = bw;
		this.kHz = kHz;
		this.endF = endF;
		this.spl = spl;
		this.slope = slope;
		double[] f = new double[2];
		f[0] = Math.min(kHz, endF)*1000;
		f[1] = Math.max(kHz, endF)*1000;
		setFrequency(f);
		
		//estimate the duration in millis - not accurate but gives an idea.
		double duration = (nCyc/(double) kHz);
		this.setDurationInMilliseconds(duration);
	
		if (shortData!=null) {
			//only CPOD
			this.rawData = shortData.clone();
		}
	}

	/**
	 * Get the raw data. 
	 * @return the raw data. 
	 */
	public short[] getRawData() {
		return rawData;
	}

	/**
	 * @return the nCyc
	 */
	public short getnCyc() {
		return nCyc;
	}

	/**
	 * @param nCyc the nCyc to set
	 */
	public void setnCyc(short nCyc) {
		this.nCyc = nCyc;
	}

	/**
	 * @return the bw
	 */
	public short getBw() {
		return bw;
	}

	/**
	 * @param bw the bw to set
	 */
	public void setBw(short bw) {
		this.bw = bw;
	}

	/**
	 * @return the kHz
	 */
	public short getkHz() {
		return kHz;
	}

	/**
	 * @param kHz the kHz to set
	 */
	public void setkHz(short kHz) {
		this.kHz = kHz;
	}

	/**
	 * @return the endF
	 */
	public short getEndF() {
		return endF;
	}

	/**
	 * @param endF the endF to set
	 */
	public void setEndF(short endF) {
		this.endF = endF;
	}

	/**
	 * @return the spl
	 */
	public short getSpl() {
		return spl;
	}

	/**
	 * @param spl the spl to set
	 */
	public void setSpl(short spl) {
		this.spl = spl;
	}

	/**
	 * @return the slope
	 */
	public short getSlope() {
		return slope;
	}

	/**
	 * @param slope the slope to set
	 */
	public void setSlope(short slope) {
		this.slope = slope;
	}

	public void setICISamples(long iciSamples) {
		this.iciSamples = iciSamples;
	}
	
	public long getICISamples() {
		return iciSamples;
	}
	
	/**
	 * Get a rough estimation of the recieved amplitude of a CPOD in dB
	 * @return the amplitude in dB. 
	 */
	@Override
	public double getAmplitudeDB() {
		if (amplitudedB==null) {
			amplitudedB = 20*Math.log10(spl) +90;
		}
		return amplitudedB;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AcousticDataUnit#getSummaryString()
	 */
	@Override
	public String getSummaryString() {
		//System.out.println("Hello CPOD summary string:"); 
		
		String str = "<html>";
		PamDataBlock parentDataBlock = getParentDataBlock();
		if (parentDataBlock != null) {
			str += parentDataBlock.getDataName() + "<p>";
		}
		long tm = getTimeMilliseconds();
		str += PamCalendar.formatDate(tm) + " " + PamCalendar.formatTime(tm, 3) + "<p>";
		str += String.format("Start Freq: %dkHz<p>", getkHz());
		str += String.format("N Cycles: %d<p>", getnCyc());
		str += String.format("BandWidth: %dkHz<p>", getBw());
		str += String.format("End Freq: %dkHz<p>", getEndF());
		str += String.format("Slope: %d<p>", getSlope());
		str += String.format("SPL: %d", getSpl());
		if (rawData.length == 40) {
			str += String.format("<p>QClass %d, SpClass %d", getBits(rawData[19], (short) 0x3), 
					getBits(rawData[19], (short) 0b11100));
			str += String.format("<p>Train %d, %d click", rawData[20], rawData[23]);
			str += String.format("<p>Qn %d, RateGood %d, SpGood %d, SpClass %d",
					getBits(rawData[36], (short)3), getBits(rawData[36], (short) 4),
					getBits(rawData[36], (short)8), getBits(rawData[36], (short) 240));
		}
		if (rawData != null) {
			int nRaw = rawData.length;
			int nRow = nRaw/5;
			for (int r = 0; r < nRow; r++) {
				str += "<p>Raw: ";
				for (int i = 0; i < 5; i++) {
					str+= String.format("%03d, ", rawData[r*5+i]);
				}
			}
		}
//		str += "<\html>";
		return str;
	}

	short getBits(short data, short bitMap) {
		short firstBit = 0;
		for (int i = 0; i < 8; i++) {
			if ((bitMap & (1<<i)) != 0) {
				break;
			}
			firstBit++;
		}
		data &= bitMap;
		return (short) (data>>firstBit);
	}
}
