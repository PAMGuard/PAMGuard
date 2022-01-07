package pamScrollSystem.jumping;

import java.io.Serializable;


public class ScrollJumpParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	/**
	 * Align next found data unit at edge of screen
	 */
	public static final int ALIGN_AT_EDGE = 1;
	
	/**
	 * Align next found data unit in centre of screen
	 */
	public static final int ALIGN_AT_CENTRE = 2;
	
	/**
	 * When the next sound is found, align it at the edge of the display
	 * or in the centre 
	 */
	public int alignment = ALIGN_AT_EDGE;
	
	/**
	 * If no more sounds are found, look in the datamap for the datablocks
	 * and move the outer scroller to the next time with data. 
	 */
	public boolean allowOuterScroll = false;

	@Override
	protected ScrollJumpParams clone() {
		try {
			return (ScrollJumpParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


}
