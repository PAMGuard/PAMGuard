package dataPlotsFX;

import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import javafx.geometry.Orientation;
import userDisplayFX.UserDisplayNodeParams;

/**
 * Stores parameters for a TDDisplay. A TDDisplay may have many graphs and these all have their own parameter calsses. All graphs share a single time scroller and hence this class
 * deals mainly with storing parameters for that, along with an ArrayList of TDGraphFX parameters corresponding to each current TDGraphFX displayed. 
 * @author Jamie Macaulay
 *
 */
public class TDParametersFX implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 3L;
	
	/**
	 * Orientation of the graph, HORIZONTAL or VERTICAL  
	 */
	public Orientation orientation = Orientation.HORIZONTAL;
	
	/**
	 * True to wrap the time display, false to scroll. 
	 */
	public boolean wrap=false; 
	
	/**
	 * The start of the display;
	 */
	public double startMillis=0;  
	
	/**
	 * The number of milliseconds of scrollable data. This is basically the data that is stored in memory and can be scrolled through. 
	 * DG. This was set at 5 minutes and would cause all panels to keep that much data, which caused out of memory errors
	 * with FFT data - which doesn't need to be kept in any case. 
	 * Now fixed so it doesn't apply to fft data, so should be o to revert to  5minutes. 
	 */
	public double scrollableTimeRange = 300000;
	
	/**
	 * The index of the selected datragram in the acoustic sc roll bar.
	 */
	public int scrollerDataGramIndex=0; 
	
	/**
	 * Whether the control pane is showing or not. 
	 */
	public boolean showControl=false; 
	
	/**
	 * The number of milliseconds displayed on the screen. 
	 */
	public long visibleTimeRange = 10000L;

	/**
	 * The datenum in millis for the start of the time scroll bar. 
	 */
	public double scrollStartMillis; 
	
	/**
	 * A list of parameters for each tdGraph in the display
	 */
	public ArrayList<TDGraphParametersFX> graphParameters;
	
	/**
	 * Heights of split panes added to the display. 
	 */
	public double[] splitHeights;
	
	
	/**
	 * The type of overlay marker which is currently employed. 
	 */
	public int overlayMarkerType = 0;
	
	/**
	 * Set of params which tell PG about the display. 
	 */
	public UserDisplayNodeParams displayProviderParams = new UserDisplayNodeParams();


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TDParametersFX clone() {
		try {
			return (TDParametersFX) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addGraphParameters(TDGraphParametersFX newGraph) {
		if (graphParameters == null) {
			graphParameters = new ArrayList<TDGraphParametersFX>();
		}
		graphParameters.add(newGraph);
	}

	/**
	 * Print the graph params. For debugging. 
	 */
	public void print() {
		System.out.println("The number of graphs is: " + this.graphParameters.size());
		for (int i=0; i<this.graphParameters.size(); i++) {
			System.out.println("TDGraph: " + i);
			if (graphParameters!=null && graphParameters.get(i).dataListInfos!=null) {
				for (int j=0; j<graphParameters.get(i).dataListInfos.size(); j++) {
					System.out.print(graphParameters.get(i).dataListInfos.get(j).providerName); 
				}
				System.out.println("");
			}
		}
		
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}

