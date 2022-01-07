package dataModelFX;

import javafx.scene.paint.Color;

/**
 * Controls the colour of lines. 
 * @author Jamie Macaulay
 *
 */
public class DataModelStyle  {
	
	/**
	 * Standard lines carry data to different modules 
	 */
	public final static Color moduleLines=Color.DODGERBLUE;
	
	/**
	 * Lines which go to a display module. 
	 */
	public final static Color displayLines=Color.PLUM; 

	/**
	 * Lines which go to any module which saves data, e.g. binary store or database. 
	 */
//	public final static Color outputFileLines=new Color(0,0,0, 0.4); 
	public final static Color outputFileLines=Color.ORANGE;
	
	/**
	 * The size of the icons. 
	 */
	public final static double iconSize = 100; 



}
