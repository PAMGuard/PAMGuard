package likelihoodDetectionModule;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * The Class ConfigurationDialogSettings holds parameters about the
 * configuration dialog box that are persistent from one invocation
 * to the next.
 */
public class ConfigurationDialogSettings implements Serializable, ManagedParameters {
	
	/** The Constant serialVersionUID. */
	static final long serialVersionUID = 234123;
	
	/** The first column width. */
	public int firstColumnWidth = 75;
	
	/** The second column width. */
	public int secondColumnWidth = 75;
	
	/** The expanded state. */
	String expandedState = new String();
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("expandedState");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return expandedState;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
