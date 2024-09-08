/**
 * 
 */
package IshmaelDetector;

/**
 * 
 * The Ishmael detector parameters. 
 * 
 * @author Dave Mellinger
 * @author Jamie Macaulay
 *
 */
import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.GroupedSourceParameters;

public class IshDetParams implements Serializable, Cloneable, ManagedParameters {
	
	
	static public final long serialVersionUID = 0;
	
	@Deprecated
	private String name = "";				//copied from FFTParams; not sure how used

	/**
	 * The input data source- string name of the datablock 
	 */
	@Deprecated // now in groupedSourceParams. Left here for compatibility
	private String inputDataSource;
	
	@Deprecated // now in groupedSourceParams. Left here for compatibility
	private int channelList = 1;
	
	@Deprecated //should not have GUI params in main params. 
	public double vscale = 50;				//passed to IshDetGraphics
	
	/**
	 * Holds the parent data block, channel selections and grouping. 
	 */
	public GroupedSourceParameters groupedSourceParmas = new GroupedSourceParameters(); 

	
	public double thresh = 1;				//detection threshold
	
	/**
	 * The time required over a threshold before a pam data unit is saved
	 * (seconds)
	 */
	public double minTime = 0;				//time required over threshold

	/**
	 * The maximum time allowed over threshold in seconds. If zero then the max time is not used. 
	 */
	public double maxTime = 99999;


	/**
	 * Smoothing of detection data in seconds. 
	 */
	public double smoothing = 0;  
	
	/**
	 * Minimum time until next detection (seconds)
	 */
	public double refractoryTime = 0;

	@Deprecated
	public String getName() { return name; }

	@Deprecated
	public void setName(String name) { this.name = name; }

	@Override
	protected IshDetParams clone() {
		try {
			IshDetParams np = (IshDetParams) super.clone();
			
			// check if the groupedSourceParams object exists and has valid values.  If not, this is probably an old
			// psf/psfx file so copy over the old values into a new object
			if (np.groupedSourceParmas == null || np.groupedSourceParmas.getDataSource() == null) {
				np.groupedSourceParmas = new GroupedSourceParameters();
				np.groupedSourceParmas.setDataSource(np.inputDataSource);
				np.groupedSourceParmas.setChanOrSeqBitmap(channelList);
			}
			return np;
		}
		catch (CloneNotSupportedException Ex) {
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("inputDataSource");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return inputDataSource;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("inputDataSource");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return inputDataSource;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
		try {
			Field field = this.getClass().getDeclaredField("channelList");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return channelList;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("channelList");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return channelList;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
		try {
			Field field = this.getClass().getDeclaredField("inputDataSource");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return inputDataSource;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("inputDataSource");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return inputDataSource;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
		return ps;
	}
}
