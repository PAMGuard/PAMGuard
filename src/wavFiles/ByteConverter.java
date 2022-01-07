package wavFiles;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

//import org.kc7bfi.jflac.sound.spi.FlacEncoding;
import org.jflac.sound.spi.FlacEncoding;

/**
 * Classes to convert between various wav and aif stream formats and 
 * double data scaled from -1 to +1 for PAMGUARD. 
 * <p>
 * The double data will often need to be packed into a nChan*nSamples
 * 2D array, so provide functionality for this as well as packing
 * single channels of data. 
 * <p>
 * Wav files follow a Windows standard of little endienness and Aif files
 * follow the Mac/Linux standard of bigendienness. Java is Bigendian. 
 * 
 * @author Doug Gillespie
 *
 */
abstract public class ByteConverter {

	/**
	 * Create a byte converter based on the number of bytes per sample
	 * and on the endianness of the byte data. <p>
	 * WAV files are in little endian format
	 * AIFF files are in big endian format
	 * AU files are in big endian format. 
	 * Java uses big endian irrespective of platform. 
	 * Therefore wav files need to be byte swapped, AIFF and AU files don't. 
	 * @param bytesPerSample number of bytes per sample (1,2,3 or 4)
	 * @param bigEndian true if it's big Endian data, false for small Endians.
	 * @return byte converter for that format. 
	 */
	public static ByteConverter createByteConverter(int bytesPerSample, boolean bigEndian, Encoding encoding) {
		if (encoding == Encoding.PCM_SIGNED) {
			switch(bytesPerSample) {
			case 1:
				return (bigEndian ? new ByteConverterAifInt8(): new ByteConverterWavInt8());
			case 2:
				return (bigEndian ? new ByteConverterAifInt16(): new ByteConverterWavInt16());
			case 3:
				return (bigEndian ? new ByteConverterAifInt24(): new ByteConverterWavInt24());
			case 4:
				return (bigEndian ? new ByteConverterAifInt32(): new ByteConverterWavInt32());
			}
		}
		else if (encoding == Encoding.PCM_FLOAT) {
			return new ByteConverterFloat32();
		}
		else if (encoding == FlacEncoding.FLAC) {
			return createByteConverter(bytesPerSample, false, Encoding.PCM_SIGNED);
		}
		return null;
	}

	public static ByteConverter createByteConverter(AudioFormat audioFormat) {
		if (audioFormat == null) {
			return null;
		}
		boolean isBig = audioFormat.isBigEndian();
		int bitSize = audioFormat.getSampleSizeInBits();
		int bytesPerSample;
		switch (bitSize) {
		case 12: // special case !
			bytesPerSample = 2;
			break;
		default:
			bytesPerSample = bitSize/8;
		}
		Encoding encoding = audioFormat.getEncoding();
		return createByteConverter(bytesPerSample, isBig, encoding);
	}

	/** 
	 * Converts a byte array into a double array. 
	 * The double array can be longer than required to hold the byte array
	 * in which case remaining doubles will be left untouched
	 * and not set to zero.
	 * @param byteData byte data
	 * @param doubleData double data
	 * @return number of doubles converted
	 */ 
	abstract public int bytesToDouble(byte byteData[], double doubleData[][], int numBytes);

	/**
	 * Converts a double array into a byte array.
	 * The byte array can be longer than required to hold the double 
	 * data in which case remaining bytes will be left untouched
	 * and not set to zero.
	 * @param doubleData double data
	 * @param byteData byte data
	 * @return number of bytes converted
	 */
	abstract public int doubleToBytes(double doubleData[][], byte byteData[], int numSamples);
}
