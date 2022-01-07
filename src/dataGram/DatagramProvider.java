package dataGram;

import PamguardMVC.PamDataUnit;

/**
 * Interface implemented on a PamDataBlock which is able to provide 
 * Datagram type data. 
 * @author Doug Gillespie
 * 
 *
 */
public interface DatagramProvider {

	/**
	 * Get the number of points required in a datagram by these data
	 * (e.g. half the fft length)
	 * @return the number of points in each time slice
	 */
	int getNumDataGramPoints();
	
	/**
	 * Add datagram data to the dataGram line. 
	 * The implementation should simply add data into the
	 * passed array.  
	 * @param dataGramLine
	 * @return number of points added ? 
	 */
	int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine);
	
	/**
	 * 
	 * @return information on the datagram scale
	 */
	DatagramScaleInformation getScaleInformation();

}
