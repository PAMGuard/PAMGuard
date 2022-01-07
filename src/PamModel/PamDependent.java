package PamModel;

/**
 * Interface to describe dependencies. Normally, it's PamControlledUnit
 * dependent on PamControlledUnit, but need to generalise a little to
 * deal with things like Sepctrogram displays which are not a PamControlledUnit
 * (they sit in UserDisplay's which are). Dependencies describe a 
 * type of required data (enum DataType), a preferred or default source which 
 * may be created automatically if none of the preferred data types exist and
 * also an option DataBlock name, allowing the user to be very specific 
 * @author Doug
 *
 */
public interface PamDependent {

	public void addDependency(PamDependency dependancy);
	
	public PamDependency getDependency();
	
	public String getDependentUserName();
	
}
