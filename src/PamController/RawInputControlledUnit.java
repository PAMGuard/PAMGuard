package PamController;

public abstract class RawInputControlledUnit extends PamControlledUnit {


	public static final int RAW_INPUT_UNKNOWN = 0;
	public static final int RAW_INPUT_FILEARCHIVE = 1;
	public static final int RAW_INPUT_REALTIME = 2;
	
	public RawInputControlledUnit(String unitType, String unitName) {
		super(unitType, unitName);
	}
	
	/**
	 * Type of data input, which can be one of RAW_INPUT_UNKNOWN (0),
	 *  RAW_INPUT_FILEARCHIVE (1), or RAW_INPUT_REALTIME (2)
	 * @return
	 */
	public abstract int getRawInputType();

}
