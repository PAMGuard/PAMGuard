package PamguardMVC;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A few useful functions for handling raw data which are better off in a simple
 * class rather than linked to a data unit, such as the functions in RawDataTransforms.<br>
 * This was copied from the ClipBinaryStorage class so cannot easily be changed without breaking 
 * backwards compatibility. <br> 
 * If it must be changed (which will happen one day) then I suggest an id flag as the first int16 which 
 * is always negative. Then if it's >0 we know it's a channel number in the old format, if <0 a new as
 * yet to be defined format. <br>
 * An obvious future development would be to write x3 or even simple zipped packets. 
 * @author dg50
 *
 */
public class RawDataUtils {

	/**
	 * Write the wave clip in scaled int8 format into a data output stream. 
	 * @param dos Data output stream
	 * @param rawData raw data
	 * @throws IOException 
	 */
	public void writeWaveClipInt8(DataOutputStream dos, double[][] rawData) throws IOException {
		int nChan = rawData.length;
		int nSamps = rawData[0].length;
		double minVal = 0, maxVal = 0;
		for (int iC = 0; iC < nChan; iC++) {
			double[] chanData = rawData[iC];
			for (int iS = 0; iS < nSamps; iS++) {
				minVal = Math.min(minVal, chanData[iS]);
				maxVal = Math.max(maxVal, chanData[iS]);		
			}
		}
		maxVal = Math.max(maxVal, -minVal);
		float scale = (float) (127./maxVal);
		dos.writeShort(nChan);
		dos.writeInt(nSamps);
		dos.writeFloat(scale);
		for (int iC = 0; iC < nChan; iC++) {
			double[] chanData = rawData[iC];
			for (int iS = 0; iS < nSamps; iS++) {
				dos.writeByte((int) (chanData[iS] * scale));
			}
		}
	}

	/**
	 * Read a waveform clip in scaled int8 format from a data input stream
	 * @param dis data input stream
	 * @return waveform double array
	 * @throws IOException
	 */
	public double[][] readWavClipInt8(DataInputStream dis) throws IOException {
		int nChan = dis.readShort();
		int nSamps = dis.readInt();
		double scale = 1./dis.readFloat();
		double[][] rawData = new double[nChan][nSamps];
		for (int iC = 0; iC < nChan; iC++) {
			double[] chanData = rawData[iC];
			for (int iS = 0; iS < nSamps; iS++) {
				chanData[iS] = (double) dis.readByte() * scale;
			}
		}
		return rawData;
	}

}
