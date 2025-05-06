package wavFiles.xwav;

import PamUtils.PamCalendar;

/**
 * information from one point in a harp header. 
 * @author dg50
 *
 */
public class HarpCycle implements Cloneable {

	long tMillis;
	long byteLoc;
	long byteLength;
	long writeLength;
	long sampleRate;
	int gain;
	public long durationMillis;
	long samplesSkipped; // samples prior to this chunk. 
	
	public HarpCycle() {
	}
	
	/**
	 * End time of this cycle info in millis. 
	 * @return
	 */
	public long getEndMillis() {
		return tMillis + durationMillis;
	}
	
	/**
	 * Start of other cycle is at end of this cycle ? Within 500ms anyway. 
	 * @param other
	 * @return
	 */
	public boolean isConsecutive(HarpCycle other) {
		long gap = other.tMillis - this.getEndMillis();
		return Math.abs(gap) < 500;
	}
	
	/**
	 * True if same sample rate and gain, false otherwise
	 * @param other
	 * @return
	 */
	public boolean compatible(HarpCycle other) {
		if (this.sampleRate != other.sampleRate) {
			return false;
		}
		if (this.gain != other.gain) {
			return false;
		}
		return true;
	}
	
	/**
	 * Add another cycle to this one, extending its byte length and durarion
	 * @param other
	 */
	public void merge(HarpCycle other) {
		this.byteLength += other.byteLength;
		this.durationMillis += other.durationMillis;
	}

	@Override
	public HarpCycle clone() {
		try {
			return (HarpCycle) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		String str = String.format("%s - %s, %3.2fs, fs=%d", 
				PamCalendar.formatDBDateTime(tMillis, true),
				PamCalendar.formatDBDateTime(getEndMillis(), true),
				(double) durationMillis/1000., sampleRate);
		return str;
	}

	/**
	 * @return the tMillis
	 */
	public long gettMillis() {
		return tMillis;
	}

	/**
	 * @return the byteLoc
	 */
	public long getByteLoc() {
		return byteLoc;
	}

	/**
	 * @return the byteLength
	 */
	public long getByteLength() {
		return byteLength;
	}

	/**
	 * @return the writeLength
	 */
	public long getWriteLength() {
		return writeLength;
	}

	/**
	 * @return the sampleRate
	 */
	public long getSampleRate() {
		return sampleRate;
	}

	/**
	 * @return the gain
	 */
	public int getGain() {
		return gain;
	}

	/**
	 * @return the durationMillis
	 */
	public long getDurationMillis() {
		return durationMillis;
	}

	/**
	 * @return the samplesSkipped
	 */
	public long getSamplesSkipped() {
		return samplesSkipped;
	}

}