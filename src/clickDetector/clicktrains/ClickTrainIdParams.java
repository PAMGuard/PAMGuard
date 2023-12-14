package clickDetector.clicktrains;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Click train id params (separated out from main click params). 
 * @author dg50
 *
 */
public class ClickTrainIdParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	private int dataVersion = 2;

	public boolean runClickTrainId = false;
	public double[] iciRange = {0.1, 2.0};
	public double maxIciChange = 1.2;
	public double okAngleError = 1.0;
	public double initialPerpendicularDistance = 100;
	public int minTrainClicks = 6;
	public double minAngleChange = 5;
	public double iciUpdateRatio = 0.5; //1 == full update, 0 = no update
	public int minUpdateGap = 5; // min gap in secs between localisation and database updates. 
	

//	if (n.maxIciChange == 0) { // old defaults from ClickParams.clone function. 
//		n.iciRange[0] = 0.1;
//		n.iciRange[1] = 2.;
//		n.maxIciChange = 1.2;
//		n.okAngleError = 1;
//	}


	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ClickTrainIdParams clone()  {
		try {
			ClickTrainIdParams newParams = (ClickTrainIdParams) super.clone();
			if (newParams.dataVersion < 1 && newParams.minAngleChange == 0) {
				newParams.minAngleChange = 5;
			}
			if (newParams.dataVersion < 2 && newParams.minUpdateGap == 0) {
				newParams.minUpdateGap = 6;
			}
			newParams.dataVersion = 2;
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("dataVersion");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return dataVersion;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
