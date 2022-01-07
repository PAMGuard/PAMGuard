package GPS;

/**
 * A 'global' synch object that can be used by both the GPS data block 
 * AND the streamer data block since they are often working together
 * with dual iterators, so can lock / concurrently modify, etc if 
 * not carefully synchronised together. 
 * @author dg50
 *
 */
public class NavDataSynchronisation {

	private static final Object synchObject = new Object();

	/**
	 * @return the synchobject
	 */
	public static Object getSynchobject() {
		return synchObject;
	}
	

}
