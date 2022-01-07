package dataGram;

/**
 * Interface to add to OfflineDataMapPoints so that they can serve up
 * a datagram. In principle, most will use this interface, but I had to leave the 
 * def of Datagram datagram in BinaryOfflineDataMapPoint so that existing serialised
 * data would de-serialise OK - otherwise, all datamaps would need to 
 * re-create from scratch which might take ages. 
 * @author Doug Gillespie
 *
 */
public interface DatagramPoint {
	/**
	 * @return the datagram
	 */
	public Datagram getDatagram() ;

	/**
	 * @param datagram the datagram to set
	 */
	public void setDatagram(Datagram datagram) ;
}
