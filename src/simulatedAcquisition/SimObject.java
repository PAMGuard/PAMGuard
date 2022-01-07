package simulatedAcquisition;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import simulatedAcquisition.movement.MovementModel;
import simulatedAcquisition.movement.MovementModels;
import PamUtils.LatLong;

/**
 * Information on a single simulated object
 * @author Doug Gillespie
 *
 */
public class SimObject implements Serializable, Cloneable, ManagedParameters {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Publically visible name for this signal. 
	 */
	public String name;
	
	/**
	 * Start position for this simulation
	 */
	public LatLong startPosition = new LatLong();
	
	/**
	 * Course over ground in degrees (true)
	 */
	public double course;
	
	/**
	 * Slant angle in degrees 90 = poinint up, -90 = pointint down
	 */
	public double slantAngle;

	/**
	 * Opposite of depth. Height preferred unit throughout
	 * PAMGuard since it's the same way up as my head.
	 */
	private double height = 0;
	
	/**
	 * Speed in metres per second. 
	 */
	public double speed;
	
	/**
	 * Flag to say beam pattern should be modelled using a piston model. 
	 */
	public boolean pistonBeam = false;
	
	/**
	 * Piston radius in metres. 
	 */
	public double pistonRadius = .075;
	
	/**
	 * Name of signal type, which is different to the 
	 * name of this particular signal. 
	 */
	public String signalName; 
	
	public double amplitude = 170;
	
	public double meanInterval = 1;
	
	public boolean randomIntervals = false;
	
	/**
	 * Whether to have an echo after each click
	 */
	public boolean echo=false; 
	
	/*8
	 * The echo delay in millis. 
	 */
	public double echoDelay=0.75;
	
	/**
	 * Separate echos
	 */
	public boolean seperateEcho = true;

	
	/**
	 * Movement model. 0 = as now. 
	 */
	public int movementModel = 0;

	protected transient SimObjectDataUnit simObjectDataUnit;
	
	private transient MovementModels movementModels;

	@Override
	protected SimObject clone() {
		try {
			SimObject newObject = (SimObject) super.clone();
			/** 
			 * Deal with the switch in coordinates from depth to height. 
			 */
			if (newObject.height == 0 && newObject.depth != 0) {
				newObject.height = -newObject.depth;
			}
			return newObject;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Deprecated
	private double depth = 0;
	
	public double getHeight() {
		if (height == 0 && depth != 0) {
			height = -depth;
		}
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * @return the movementModels
	 */
	public MovementModels getMovementModels() {
		if (movementModels == null) {
			movementModels = new MovementModels(this);
		}
		return movementModels;
	}
	
	/**
	 * 
	 * @return currently selected movement model. 
	 */
	public MovementModel getSelectedMovementModel() {
		return getMovementModels().getModel(movementModel);
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("depth");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return depth;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
