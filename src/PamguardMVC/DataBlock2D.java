package PamguardMVC;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import dataPlotsFX.data.DataTypeInfo;

abstract public class DataBlock2D<Tunit extends PamDataUnit> extends AcousticDataBlock<Tunit> {
	
	private DataTypeInfo dataTypeInfo;

	public DataBlock2D(Class unitClass, String dataName, PamProcess parentProcess, int channelMap) {
		super(unitClass, dataName, parentProcess, channelMap);
		dataTypeInfo = new DataTypeInfo(ParameterType.FREQUENCY, ParameterUnits.HZ);
	}

	/**
	 * Get the advance between slices in samples. For FFT data this is 
	 * the same thing as the FFT Hop
	 * @return advance in samples between slices. 
	 */
	abstract public int getHopSamples();
	
	/**
	 * Get the length of contained data. For FFT data this will 
	 * be FFTLength()/2. 
	 * @return length of data in each slice. 
	 */
	abstract public int getDataWidth(int sequenceNumber);
	
	/**
	 * Get the minimum value which can occur in the data. e.g. 0 for FFT data.
	 * @return the data's minimum value
	 */
	abstract public double getMinDataValue();
	
	/**
	 * Get the maximum value which can occur in the data. e.g. sampleRate/2 for FFT data.
	 * @return the data's maximum value
	 */
	abstract public double getMaxDataValue();
	
	/**
	 * Are the data on a log scale
	 * @return true if log, default is false. 
	 */
	public boolean isLogScale() {
		return false;
	}
	
	/**
	 * Convert a bin to a value. e.g. a fft bin to a frequency
	 * @param bin bin number, e.g. pos in fft. 
	 * @param sequenceNumnber (channel)
	 * @return data value
	 */
	public double bin2Value(double bin, int sequenceNumber) {
		if (isLogScale()) {
			double c = Math.log(getMaxDataValue()/getMinDataValue())/getDataWidth(sequenceNumber);
			return getMinDataValue() * Math.exp(bin*c);
		}
		else {
			return (bin / getDataWidth(sequenceNumber)) * (getMaxDataValue()-getMinDataValue()) + getMinDataValue(); 
		}
	}
	
	/**
	 * convert a data value to a bin
	 * @param value data value
	 * @param sequenceNumber (channel)
	 * @return bin within data (can exceed limits if too large !)
	 */
	public double value2bin(double value, int sequenceNumber) {
		if (isLogScale()) {
			double c = Math.log(getMaxDataValue()/getMinDataValue())/getDataWidth(sequenceNumber);
			return Math.log(value/getMinDataValue())/c;
		}
		else {
			double v = (value - getMinDataValue()) / (getMaxDataValue()-getMinDataValue()) * getDataWidth(sequenceNumber);
			return v;
		}
	}
	
	/**
	 * Get the scale units to display on axis, etc. 
	 * @return data type information.
	 */
	abstract public DataTypeInfo getScaleInfo();

	@Override
	public Element getDataBlockXML(Document doc) {
		Element el = super.getDataBlockXML(doc);
		DataTypeInfo dti = getScaleInfo();
		if (dti != null) {
			if (dti.dataType != null) {
				el.setAttribute("DataType", dti.dataType.toString());
			}
			if (dti.dataUnits != null) {
				el.setAttribute("DataUnits", dti.dataUnits.toString());
			}
		}
		return el;
	}
}
