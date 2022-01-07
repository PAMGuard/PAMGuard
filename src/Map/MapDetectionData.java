package Map;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.paneloverlay.OverlayDataInfo;
import PamguardMVC.PamDataBlock;

public class MapDetectionData extends OverlayDataInfo implements Serializable, Cloneable, ManagedParameters {
	
	static public final long serialVersionUID = 0;

	public MapDetectionData(String dataName) {
		super(dataName);
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Plot up to this many seconds back in time from now. 
	 */
	private long displaySeconds;
	
	private long displayMilliseconds;
	
//	public long forcedStart;
	
	/**
	 * Plot all available data, regardless of time. 
	 */
	public boolean allAvailable;
	
	
	/**
	 * Fade over time, disappearing at 'displaySeconds'
	 */
	public boolean fade;
	
	/**
	 * Display data ahead of the scrollbar (future data) instead of behind
	 */
	public boolean lookAhead;
	
	
	/**
	 * Associated data block
	 */
	transient public PamDataBlock dataBlock;

	@Override
	public MapDetectionData clone() {

		try {
			return (MapDetectionData) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
		
	}

	/**
	 * @return the displayMilliseconds
	 */
	public long getDisplayMilliseconds() {
		if (displaySeconds != 0 && displayMilliseconds == 0) {
			displayMilliseconds = displaySeconds * 1000;
			displaySeconds = 0;
		}
		return displayMilliseconds;
	}

	/**
	 * @param displayMilliseconds the displayMilliseconds to set
	 */
	public void setDisplayMilliseconds(long displayMilliseconds) {
		this.displayMilliseconds = displayMilliseconds;
	}
		
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("displaySeconds");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return displaySeconds;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
