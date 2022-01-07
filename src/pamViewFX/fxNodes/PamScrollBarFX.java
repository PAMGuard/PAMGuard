package pamViewFX.fxNodes;

import javafx.scene.control.ScrollBar;

/**
 * Sub class of the standard scroll bar
 * <p>
 * The JavaFX scroll bar works slightly differently from the Swing scroll bar in that the the value is scroll max when the thumb is all 
 * the way to the right (horizontal). In swing the thumb is scrollmax- thumb width when all the way to the right. 
 * The swing scroll bar is actually more convenient for many displays and there's a lot of underlying legacy code which 
 * uses the swing scroll bar system. 
 * <p>
 * PamScrollBarFX allows users to use the scroll bar either with the new JavFX getValue() system or the old Swing 
 * system The swing system is enabled by default; 
 * <p>
 * Use getModValue() to get modified value (if using Swing system or future system)
 * 
 * @author Jamie Macaulay
 *
 */
public class PamScrollBarFX extends ScrollBar{
	
	/**
	 * When the thumb is at the end of the scroll bar the value is the same as max scroll bar value. This is the JavaFX standard scroll bar system
	 */
	public final static int JAVAFX_SCROLL_SYSTEM=0;
	
	/**
	 * When the thumb is at the end of the scroll bar the value is the max scroll bar value minus the width of the thumb. This is the system used in Swing. 
	 */
	public final static int SWING_SCROLL_SYSTEM=1;
	
	private int currentScrollSystem=SWING_SCROLL_SYSTEM; 

	public PamScrollBarFX() {
		super();
	}
	
	/**
	 * Get the modified value. This is the same as value if the scroll bar is set to JAVAFX_SCROLL_SYSTEM but will be different 
	 * if any other system is used e.g. SWING_SCROLL_SYSTEM
	 * @return the value of the scroll bar, modified depending on currentScrollSystem value. 
	 */
	public double getModValue(){
		switch (currentScrollSystem){
		case JAVAFX_SCROLL_SYSTEM:
			return super.getValue(); 
		case SWING_SCROLL_SYSTEM:
			if (getMax()-getVisibleAmount()==0) return 0; //stops NAN being returned
			return getValue()*(getMax()-getVisibleAmount())/(getMax()-getMin()); 
		default:
			return super.getValue(); 
		}
		
	}
	
	/**
	 * Set the value of the scroll bar. This is the same as value if the scroll bar is set to JAVAFX_SCROLL_SYSTEM but will be converted to a different value
	 * if any other system is used e.g. SWING_SCROLL_SYSTEM
	 * @param the value of the scroll bar to set. 
	 */
	public void setModValue(double value){
		switch (currentScrollSystem){
		case JAVAFX_SCROLL_SYSTEM:
			 super.setValue(value);
			 break;
		case SWING_SCROLL_SYSTEM:
			 super.setValue(value*(getMax()-getMin())/(getMax()-getVisibleAmount()));
			 break;
		default:
			 super.setValue(value);
		}
		
	}
	
	/**
	 * Get the current scroll system. The two main systems are SWING_SCROLL_SYSTEM and JAVAFX_SCROLL_SYSTEM. 
	 * @return the current scroll system being used by the scroll bar. 
	 */
	public int getCurrentScrollSystem() {
		return currentScrollSystem;
	}

	public void setCurrentScrollSystem(int currentScrollSystem) {
		this.currentScrollSystem = currentScrollSystem;
	}

	
}
