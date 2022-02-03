package loggerForms;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Class to hold plot options for a single form. 
 * Individual options will have to be looked at on a control by control 
 * basis. Will attempt to implement everything as a 2D array of bools. For
 * most controls, this will have a single entry, but for lookups 
 * it will be an array itself. 
 * @author Doug Gillespie
 *
 */
public class FormPlotOptions implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
//	private boolean plotEverything;
	
	private boolean[][] controlChoices = new boolean[1][1];


	/**
	 * 
	 * @param controlIndex index of control in form
	 * @return true if all items with this control set should be plotted
	 */
	public boolean isPlotControl(int controlIndex) {
		return isPlotControl(controlIndex, 0);
	}
	
	/**
	 * 
	 * @param controlIndex index of a control
	 * @param itemIndex index of a LUT item within a control
	 * @return true if a lookup item of a  certain index in a the selected
	 * control should be plotted
	 */
	public boolean isPlotControl(int controlIndex, int itemIndex) {
		checkDimension(controlIndex, itemIndex);
		return controlChoices[controlIndex][itemIndex] | true;
	}
	
	/**
	 * 
	 * @param controlIndex control index who's plot option should be set. 
	 * @param plot true if items with this control set should be plotted
	 */
	public void setPlotControl(int controlIndex, boolean plot) {
		setPlotControl(controlIndex, 0, plot);
	}
	
	/**
	 * 
	 * @param controlIndex controlIndex control index who's plot option should be set.
	 * @param itemIndex LUT item who's plot option should be set
	 * @param plot item option. 
	 */
	public void setPlotControl(int controlIndex, int itemIndex, boolean plot) {
		checkDimension(controlIndex, itemIndex);
		controlChoices[controlIndex][itemIndex] = plot;
	}

	/**
	 * Check the dimension of the main 2D boolean array and expand as necessary
	 * @param controlIndex index of control
	 * @param itemIndex index of item
	 */
	private void checkDimension(int controlIndex, int itemIndex) {
		if (controlIndex >= controlChoices.length) {
			controlChoices = Arrays.copyOf(controlChoices, controlIndex + 1);
		}
		if (controlChoices[controlIndex] == null) {
			controlChoices[controlIndex] = new boolean[itemIndex+1];
		}
		else if(itemIndex >= controlChoices[controlIndex].length) {
			controlChoices[controlIndex] = Arrays.copyOf(controlChoices[controlIndex], itemIndex + 1);
		}
	}

	@Override
	protected FormPlotOptions clone() {
		try {
			return (FormPlotOptions) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("controlChoices");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return controlChoices;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
