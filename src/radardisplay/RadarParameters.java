package radardisplay;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;

import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import userDisplay.UserFrameParameters;

public class RadarParameters extends UserFrameParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 1;
	
	static public final int SIDES_ALL = 0;
	static public final int SIDES_RIGHTHALF = 1;
	static public final int SIDES_LEFTHALF = 2;
	static public final int SIDES_FRONTHALF = 3;
	static public final int SIDES_BACKHALF = 4;
	
	static public final int RADIAL_AMPLITIDE = 0;
	static public final int RADIAL_DISTANCE = 1;
	static public final int RADIAL_SLANT_ANGLE = 2;
	
	static public final int HEAD_UP = 0;
	static public final int NORTH_UP = 1;
	
	String windowName;
	
	int sides = SIDES_ALL;
	
	int radialAxis = RADIAL_AMPLITIDE;
	
	int rangeStartm = 0;
	
	int rangeEndm = 1000;
	
	int orientation = HEAD_UP;
	
	/**
	 * Ranges in dB are opposite way round to normal so that 
	 * small amplitudes are on the outside and large amplitudes are in the middle. 
	 */
	int rangeStartdB = 160;
	
	int rangeEnddB = 100;
	
	int angleGrid = 30;
	
	/** Use RadarDataInfo object instead - leave here for backwards compatibility */
	private boolean[] showDetector = new boolean[0];
	
	/** Use RadarDataInfo object instead - leave here for backwards compatibility */
	private boolean[] fadeDetector = new boolean[0];
	
	/** Use RadarDataInfo object instead - leave here for backwards compatibility */
	private int[] detectorLifetime;
	
	private Hashtable<String, RadarDataInfo> radarDataInfoChoices;

	// some stuff for the viewer ...
	long scrollMinMillis, scrollMaxMillis; // current 
	double viewRangeSeconds; // range off spinner. 
	public long scrollValue;
	
	public String getScaleName() {
		switch(radialAxis) {
		case RADIAL_AMPLITIDE:
			return "Amplitude";
		case RADIAL_DISTANCE:
			return "Distance";
		case RADIAL_SLANT_ANGLE:
			return "Slant Angle";
		}
		return "Unknown";
	}

	/**
	 * Get radar overlay data information for a given datablock. 
	 * @param dataBlock Data block
	 * @return Overlay Data information. 
	 */
	public RadarDataInfo getRadarDataInfo(PamDataBlock dataBlock) {
		String name = dataBlock.getLongDataName();
		return getRadarDataInfo(name);
	}
	
	/**
	 * Get radar overlay data information for a given datablock name. 
	 * @param dataBlockName Data block LONG name
	 * @return Overlay Data information. 
	 */
	public RadarDataInfo getRadarDataInfo(String dataBlockName) {
		if (radarDataInfoChoices == null) {
			radarDataInfoChoices = new Hashtable<>();
		}
		RadarDataInfo rdi = radarDataInfoChoices.get(dataBlockName);
		if (rdi == null) {
			rdi = new RadarDataInfo(dataBlockName);
			radarDataInfoChoices.put(dataBlockName, rdi);
		}
		return rdi;
	}
	
	/**
	 * Add/update value in hashtable for a specific data block
	 * 
	 * @param dataBlock the datablock to add/update
	 * @param rdi the RadarDataInfo object
	 */
	public void setRadarDataInfo(PamDataBlock dataBlock, RadarDataInfo rdi) {
		setRadarDataInfo(dataBlock.getLongDataName(), rdi);
	}
	
	/**
	 * Add/update value in hashtable for a specific data block LONG name
	 * 
	 * @param dataBlock the datablock long name to add/update
	 * @param rdi the RadarDataInfo object
	 */
	public void setRadarDataInfo(String dataBlockName, RadarDataInfo rdi) {
		if (radarDataInfoChoices == null) {
			radarDataInfoChoices = new Hashtable<>();
		}
		radarDataInfoChoices.put(dataBlockName, rdi);
	}
	
	/**
	 * If we have not defined radarDataInfoChoices yet but the detectorLifetime array
	 * exists, it means that the user is loading a psfx that was created previously so
	 * return a true.  If radarDataInfoChoices exists or neither of them exist, then
	 * return false.
	 * 
	 * @return
	 */
	public boolean isThereOnlyOldData() {
		if (radarDataInfoChoices == null && detectorLifetime != null) return true;
		return false;
	}

	/**
	 * 
	 * @param dataBlocks
	 * @return
	 */
	public boolean convertOldData(ArrayList<PamDataBlock> dataBlocks) {
		radarDataInfoChoices = new Hashtable<>();
		for (int i=0; i<dataBlocks.size(); i++) {
			String blockName = dataBlocks.get(i).getLongDataName();
			RadarDataInfo rdi = new RadarDataInfo(blockName);
			if (i<showDetector.length) {
				rdi.select = showDetector[i];
				rdi.setFadeDetector(fadeDetector[i]);
				rdi.setDetectorLifetime(detectorLifetime[i]);
			}
			radarDataInfoChoices.put(blockName, rdi);
		}
		showUpgradeWarning();
		return true;
	}
	
	/**
	 * Display a warning to let the user know that the display parameters were upgraded and may no longer be correct
	 */
	public void showUpgradeWarning() {
		String title = "Radar Display Parameters Upgraded";
		String msg = "<html><p>The Radar Display parameters have been upgraded.  Please open the " +
				"Radar Display dialog panel to ensure that the correct datablocks are being displayed</p></html>";
		String help = null;
		int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
	}
	
	@Override
	public RadarParameters clone()  {
		try {
			RadarParameters newParams = (RadarParameters) super.clone();
			if (newParams.viewRangeSeconds == 0) {
				viewRangeSeconds = 600000L;
			}
//			if (newParams.showDetector == null) {
//				newParams.showDetector = new boolean[0];
//			}
//			if (newParams.fadeDetector == null && newParams.showDetector != null) {
//				newParams.fadeDetector = new boolean[newParams.showDetector.length];
//			}
			return newParams;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("angleGrid");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return angleGrid;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("detectorLifetime");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return detectorLifetime;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("fadeDetector");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return fadeDetector;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("orientation");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return orientation;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("radarDataInfoChoices");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return radarDataInfoChoices;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("radialAxis");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return radialAxis;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("rangeEnddB");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return rangeEnddB;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("rangeEndm");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return rangeEndm;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}		try {
			Field field = this.getClass().getDeclaredField("rangeStartdB");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return rangeStartdB;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("rangeStartm");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return rangeStartm;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("scrollMaxMillis");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return scrollMaxMillis;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("scrollMinMillis");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return scrollMinMillis;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("showDetector");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return showDetector;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}		try {
			Field field = this.getClass().getDeclaredField("sides");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return sides;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("viewRangeSeconds");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return viewRangeSeconds;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("windowName");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return windowName;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}

		return ps;
	}

}
