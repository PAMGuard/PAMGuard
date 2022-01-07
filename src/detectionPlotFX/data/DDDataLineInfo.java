package detectionPlotFX.data;

import PamUtils.LatLong;
import dataPlots.data.DataLineInfo;

/**
 * Information on the type of data that can be displayed 
 * <p>
 * Every DDDataInfo has a list of DDDaataLineInfos indicating what types of data can be displayed. 
 * <p>
 * Each DDDataline info also returns the name an units of the x and y axis. 
 * @author Jamie Macaulay.
 *
 */
public class DDDataLineInfo {
	
	/**
	 * The different types of graphs that can be displayed
	 * @author Jamie Macaulay
	 *
	 */
	public static enum PlotType {
	    WAVEFORM, SPECTRUM, WIGNER, TRIIGGER, PHASE3D
	}
	
	/*
	 * Some standard names that might get reused - list here
	 * to save having to try to spell them accurately elsewhere. 
	 */
	public static final String UNITS_ANGLE = LatLong.deg;
	public static final String UNITS_FREQUENCY = "Hz";
	public static final String UNITS_TEMPERATURE = LatLong.deg+".C";
	
	public DDDataLineInfo(){
		
	}
	
	/**
	 * Get the DataLine info for the x, y and z axis. 
	 * @param type - the type of plot. 
	 * @return an 2 or 3 element array of axis. If 2 elements is x and y axis, if 3 elements is x, y and z axis. 
	 */
	public static DataLineInfo[] getAxisInfo(PlotType type){
		DataLineInfo x = null;
		DataLineInfo y = null;
		DataLineInfo z = null;
		int dim=2; //the number of dimensions. 
		switch(type){
		case PHASE3D:
			dim=3; 
			break;
		case SPECTRUM:
			x=new DataLineInfo(null, null);
			break;
		case TRIIGGER:
			break;
		case WAVEFORM:
			break;
		case WIGNER:
			dim=3; 
			break;
		default:
			break;
		
		}
				
		DataLineInfo[] axis=new DataLineInfo[dim];
		axis[0]=x;
		axis[1]=y;
		if (dim>=3) axis[2]=z; 
		
		return axis;
	}

	
	public static String getName(PlotType type){
		String name="";
		switch(type){
		case PHASE3D:
			name="Phase";
			break;
		case SPECTRUM:
			name="Frequency Spectrum";
			break;
		case TRIIGGER:
			name="Trigger";
			break;
		case WAVEFORM:
			name="Waveform";
			break;
		case WIGNER:
			name="Wigner"; 
			break;
		default:
			name=""; 
			break;
		
		}
		return name;
	}
	
	
	

}
