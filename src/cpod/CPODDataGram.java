package cpod;

import java.util.ArrayList;

import dataGram.Datagram;
import dataGram.DatagramDataPoint;

public class CPODDataGram extends Datagram {

	private static final long serialVersionUID = 1L;


	public CPODDataGram(int intervalSeconds) {
		super(intervalSeconds);
	}

	/* (non-Javadoc)
	 * @see dataGram.Datagram#getDataPoints()
	 */
	@Override
	public ArrayList<DatagramDataPoint> getDataPoints() {
		// TODO Auto-generated method stub
		return super.getDataPoints();
	}

	/* (non-Javadoc)
	 * @see dataGram.Datagram#getNumDataPoints()
	 */
	@Override
	public int getNumDataPoints() {
		// TODO Auto-generated method stub
		return super.getNumDataPoints();
	}

	/* (non-Javadoc)
	 * @see dataGram.Datagram#getDataPoint(int)
	 */
	@Override
	public DatagramDataPoint getDataPoint(int iDataPoint) {
		// TODO Auto-generated method stub
		return super.getDataPoint(iDataPoint);
	}

}
