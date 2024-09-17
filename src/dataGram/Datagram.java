package dataGram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import binaryFileStorage.BinaryTypes;
import dataMap.OfflineDataMapPoint;

/**
 * A datagram is a set of data which summarises the real data content in quite course bins
 * of about 10 minutes (though this may vary).<br> 
 * Generally, Datagram data look a bit like very course spectrogram data, except that they 
 * are designed to show data density as a function of frequency instead of energy density
 * as a function of frequency.<br>
 * They can be calculated automatically from many types of data and are stored in the 
 * binary index files for easy access when mapping data. 
 *  
 * @author Doug Gillespie
 *
 */
public class Datagram implements Serializable, ManagedParameters {
	
	private static final long serialVersionUID = 1L;

	private int intervalSeconds;

	private OfflineDataMapPoint offlineDataMapPoint;

	private ArrayList<DatagramDataPoint> dataPoints;

	/**
	 * @param intervalSeconds
	 */
	public Datagram(int intervalSeconds) {
		super();
		this.intervalSeconds = intervalSeconds;
		dataPoints = new ArrayList<DatagramDataPoint>();
	}

	/**
	 * @return the intervalSeconds
	 */
	public int getIntervalSeconds() {
		return intervalSeconds;
	}

	/**
	 * @return the offlineDataMapPoint
	 */
	public OfflineDataMapPoint getOfflineDataMapPoint() {
		return offlineDataMapPoint;
	}

	/**
	 * @return the dataPoints
	 */
	public ArrayList<DatagramDataPoint> getDataPoints() {
		return dataPoints;
	}

	/**
	 * Add a data point to a datagram
	 * @param datagramPoint
	 */
	public synchronized void addDataPoint(DatagramDataPoint datagramPoint) {
		//System.out.println("Datagram points: " + dataPoints.size() + " intervalSeconds: " + intervalSeconds);
		dataPoints.add(datagramPoint);
	}

	/**
	 * Get the number of data points. 
	 * @return the number of data points
	 */
	public int getNumDataPoints() {
		return dataPoints.size();
	}

	/**
	 * 
	 * @param iDataPoint
	 * @return a datagram point. 
	 */
	public DatagramDataPoint getDataPoint(int iDataPoint) {
		return dataPoints.get(iDataPoint);
	}

	public DatagramDataPoint getLastDataPoint() {
		if (dataPoints.size() < 1) {
			return null;
		}
		return dataPoints.get(dataPoints.size()-1);
	}

	/**
	 * Write the datagram to an output stream. 
	 * @param dos
	 * @return
	 */
	public boolean writeDatagram(DataOutputStream dos) {

		int dataPointSize = 0;
		int dataPointLen = 0;
		float[] data;
		DatagramDataPoint dataPoint;
		if (getNumDataPoints() > 0) {
			dataPoint = getDataPoint(0);
			dataPointSize = dataPoint.getData().length * 4; // float data
			dataPointSize += 2*8; // start and end times
			dataPointLen = dataPoint.getData().length;
		}
		int datagramLength = 8 + // basic header info - object id and length of the data. 
		4 + // interval seconds
		4 + // datagram format.
		4 + // number of data points
		4 + // length of each data point
		getNumDataPoints() * dataPointSize;

		try {
			dos.writeInt(datagramLength);
			dos.writeInt(BinaryTypes.DATAGRAM);
			dos.writeInt(intervalSeconds);
			dos.writeInt(0); // format
			dos.writeInt(getNumDataPoints());
			dos.writeInt(dataPointLen);
			for (int i = 0; i < getNumDataPoints(); i++) {
				dataPoint = getDataPoint(i);
				dos.writeLong(dataPoint.getStartTime());
				dos.writeLong(dataPoint.getEndTime());
				data = dataPoint.getData();
				for (int j = 0; j < dataPointLen; j++) {
					dos.writeFloat(data[j]);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public void readDatagramData(DataInputStream dis, int objectLength) {
		// read the data - not the object length and type !
		int nDataPoints = 0;
		int dataPointLength;
		long pointStart, pointEnd;
		float[] data;
		DatagramDataPoint dataPoint;
		try{
			intervalSeconds = dis.readInt();
			int format = dis.readInt(); // format
			nDataPoints = dis.readInt();
			dataPointLength = dis.readInt();
			for (int i = 0; i < nDataPoints; i++) {
				pointStart = dis.readLong();
				pointEnd = dis.readLong();
				dataPoint = new DatagramDataPoint(this, pointStart, pointEnd, dataPointLength);
				data = dataPoint.getData();
				for (int j = 0; j < dataPointLength; j++) {
					data[j] = dis.readFloat();
				}
				addDataPoint(dataPoint);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
