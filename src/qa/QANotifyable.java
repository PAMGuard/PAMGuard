package qa;

/**
 * Get notifications when something in QA changes. All standard PAMGuard notifications
 * will be sent, so use -ve numbers for codes for anything module specific. 
 * @author dg50
 *
 */
public interface QANotifyable {

	public static final int PAM_START = -1;
	public static final int PAM_STOP = -2;
	public static final int OPS_STATUS_CHANGE = -3;
	public static final int TEST_SELECT_CHANGED = -4;
	public static final int PARAMETER_CHANGE = -5;
	public static final int SEQUENCE_START = -6;
	public static final int SEQUENCE_END = -7;
	public static final int TEST_START = -8;
	public static final int TEST_END = -9;
	

	/**
	 * Something changed inQA module ...
	 * @param noteCode
	 * @param noteObject
	 */
	public void qaNotify(int noteCode, Object noteObject);
	
}
