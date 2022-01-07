package difar;

/**
 * Observer interface for DIfar demultiplexing. 
 * Gets called at regular intervals during the demux process so 
 * that display progress can be updated. 
 * @author Doug Gillespie
 *
 */
public interface DemuxObserver {
	void setStatus(double percentComplete, boolean lock75, boolean lock15);
}
