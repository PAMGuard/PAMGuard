package loc3d_Thode;

import java.io.Serializable;

public class TowedArray3DProcessParameters implements Serializable, Cloneable {

static final long serialVersionUID = 1;
	
	/**
	 * Use first detector block in model
	 */
	int detectorDataBlock=0;
	
	/**Choose algorithm
	 * 
	 */
	int algchoice=0;
	
	/**
	 * How many multipath assumed to exist per event(anchor)?
	 */
	int minICINum = 3;  
	
	/**
	 * Maximum time delay to assoicate with a multipath
	 */
	long maxTimeDelay=150; //msec
	
	/**
	 * Amount of time to keep potential 3-D detections in memory, msec
	 */
	long maxTimeQueue=10000; //msec
	/**
	 * Bitmap of channels to be used - use all available. 
	 */
	int channelList = 0xFFFF;
	
	/*
	 * Percent error permitted for mismatch in detection bearing and echo time
	 * when estimating ICI for the current detection
	 * 
	 */
	long percentErrorAngle = 10;
	long percentICIMatchError =10; 
	
	/*
	 * Options to try to use cross-correlation on raw data to fine-tune TOA estimates
	 */
	boolean fine_tune_tdd, fine_tune_tds, yes_process;
	
	@Override
	/**
	 * overriding the clone function enables you to clone (copy) 
	 * these parameters easily in your code without having to 
	 * continually cast to (WorkshopProcessParameters) or handle
	 * the exception CloneNotSupportedException.
	 */
	protected TowedArray3DProcessParameters clone() {
		try {
			return (TowedArray3DProcessParameters) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
}
