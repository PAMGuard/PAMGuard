package dataPlotsFX;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import javafx.scene.paint.Color;
import dataPlots.layout.DataListInfo;
import dataPlots.layout.GraphParameters;
import dataPlotsFX.data.DataTypeInfo;
import dataPlotsFX.data.TDScaleInfoData;

/**
 * The settings for an individual graph. 
 * @author Jamie Macaulay
 *
 */
public class TDGraphParametersFX implements Serializable, Cloneable, ManagedParameters {
	
	public static final long serialVersionUID = 1L;
	
	/**
	 * 
	 * Simple pop up menu
	 */
	public final static int SIMPLE_POP_UP =0; 
	
	/**
	 * Adv pop up menu;
	 */
	public final static int  ADV_POP_UP= 1; 

	
	/**
	 * Auto scale the axis.
	 */
	public boolean autoScale= false;
	
	/**
	 * Flag which indicates what type of pop up menu to use;
	 */
	public int popUpMenuType = SIMPLE_POP_UP; 

	/**
	 * The current data type to show.
	 */
	public DataTypeInfo currentDataType= new DataTypeInfo(ParameterType.AMPLITUDE, ParameterUnits.DB); ; 
	
	/**
	 * Allows TDDataInfo to be created when graph is initialised. Can't just have TDDataInfo here as a whole data block would 
	 * end up being serialised (yes Americans, that is how you spell initialised and serialised). 
	 */
	public ArrayList<DataListInfo> dataListInfos;
	
	/**
	 * List of scale information for each scale type for the graph. 
	 */
	private Hashtable<String, TDScaleInfoData> scaleInfoData;
	
	/**
	 * Channel bitmap list. Channels which are plotted by each plot pane. 
	 * If on plot pane that plot all channels then channels[0]=0; 
	 */
	int[] channels={0};
	
	/**
	 * The colour of the plot panel background. 
	 */
	transient public Color plotFill=Color.WHITE;
	
	/**
	 * Used only when saving as Color (javafx) is not serializable. 
	 */
	public String plotFillS = "white";


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected GraphParameters clone() {
		try {
			return (GraphParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void  addDataListInfo(DataListInfo dataListInfo) {
		if (dataListInfos == null) {
			dataListInfos = new ArrayList<DataListInfo>();
		}
		dataListInfos.add(dataListInfo);
	}

	/**
	 * @return the scaleInfoData
	 */
	private Hashtable<String, TDScaleInfoData> getScaleInfoData() {
		if (scaleInfoData == null) {
			scaleInfoData = new Hashtable<>();
		}
		return scaleInfoData;
	}
	
	/**
	 * Set scale information data for a particular data type. 
	 * @param dataType Data type (type and units)
	 * @param scaleData scale information
	 */
	public void setScaleInfoData(DataTypeInfo dataType, TDScaleInfoData scaleData) {
		getScaleInfoData().put(dataType.getTypeString(), scaleData);
	}
	
	/**
	 * Get scale information for a particular data type
	 * @param dataType Data type (type and units)
	 * @return scale information. 
	 */
	public TDScaleInfoData getScaleInfoData(DataTypeInfo dataType) {
		return getScaleInfoData().get(dataType.getTypeString());
	}

	/**
	 * Clear old scale information data. 
	 */
	public void clearScaleInformationData() {
		getScaleInfoData().clear();
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("channels");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return channels;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}


