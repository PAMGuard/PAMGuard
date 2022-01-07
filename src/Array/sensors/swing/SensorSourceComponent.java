package Array.sensors.swing;
/**
 * Swing component with a dropdown for selecting array sensor choices. 
 * @author dg50
 *
 */

import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import Array.sensors.ArrayParameterType;
import Array.sensors.ArraySensorDataBlock;
import Array.sensors.ArraySensorDataUnit;
import Array.sensors.ArraySensorFieldType;
import PamController.PamController;
import PamguardMVC.PamDataBlock;

public class SensorSourceComponent {
	
	private ArraySensorFieldType sensorType;
	
	private JComboBox<String> sensorDropDown;
	
	private ArrayList<PamDataBlock> currentBlocks;

	private boolean defaultOption;

	private boolean fixedOption;
	
	private int nSpecials;
	
	private ArrayParameterType[] specialTypes = new ArrayParameterType[2];

	public SensorSourceComponent(ArraySensorFieldType sensorType, boolean fixedOption, boolean defaultOption) {
		super();
		this.sensorType = sensorType;
		this.fixedOption = fixedOption;
		this.defaultOption = defaultOption;
		sensorDropDown = new JComboBox<>();
		fillDropDown();
		sensorDropDown.setToolTipText("Sensor updates for " + sensorType.toString());
	}
	
	/**
	 * Add an action listener to the drop down list. 
	 * @param actionListener
	 */
	public void addActionListenr(ActionListener actionListener) {
		sensorDropDown.addActionListener(actionListener);
	}
	
	public void fillDropDown() {
		currentBlocks = getDataBlocks();
		sensorDropDown.removeAllItems();
		nSpecials = 0;
		if (fixedOption) {
			sensorDropDown.addItem("Fixed Value");
			specialTypes[nSpecials++] = ArrayParameterType.FIXED;
		}
		if (defaultOption) {
			sensorDropDown.addItem("Default value");
			specialTypes[nSpecials++] = ArrayParameterType.DEFAULT;
		}
		for (PamDataBlock aBlock : currentBlocks) {
			sensorDropDown.addItem(aBlock.getDataName());
		}
	}
	
	/**
	 * Set the type of parameter being used, fixed, default or sensor 
	 * @param paramType
	 */
	public void setParameterType(ArrayParameterType paramType) {
		for (int i = 0; i < nSpecials; i++) {
			if (paramType == specialTypes[i]) {
				sensorDropDown.setSelectedIndex(i);
				return;
			}
		}
	}
	
	/**
	 * Get the type of parameter being used, fixed, default or sensor 
	 * @return
	 */
	public ArrayParameterType getParameterType() {
		int ind = sensorDropDown.getSelectedIndex();
		if (ind < 0) {
			return null;
		}
		if (ind < nSpecials) {
			return specialTypes[ind];
		}
		return ArrayParameterType.SENSOR;
	}
	
	/**
	 * Set the selected datablock for sensor data. Before calling this, you should call
	 * fillDropDown to make sure list of blocks is up to date. 
	 * @param aDataBlock datablock to select
	 * @return true if that block was selected OK, i.e. it was in the list. 
	 */
	public boolean setDataBlock(PamDataBlock aDataBlock) {
		if (currentBlocks == null) {
			return false;
		}
		int ind = currentBlocks.indexOf(aDataBlock);
		if (ind < 0) {
			return false;
		}
		sensorDropDown.setSelectedIndex(ind+nSpecials); // offset by 1 to allow for null option. 
		return true;
	}
	
	/**
	 * 
	 * @return Currently selected datablock for this sensor (can be null)
	 */
	public PamDataBlock getDataBlock() {
		int ind = sensorDropDown.getSelectedIndex();
		if (ind <= 0 || currentBlocks == null) {
			return null;
		}
		else {
			return currentBlocks.get(ind-nSpecials);
		}
	}

	public JComponent getComponent() {
		return sensorDropDown;
	}

	/**
	 * Get a list of datablocks that might be able to provide info on this 
	 * sensor field type
	 * @return
	 */
	private ArrayList<PamDataBlock> getDataBlocks() {
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks(ArraySensorDataUnit.class, true);
		ArrayList<PamDataBlock> sensBlocks = new ArrayList<>();
		if (allDataBlocks == null) {
			return sensBlocks;
		}
		// go through take out the ones that support this sensor. 
		for (PamDataBlock aBlock : allDataBlocks) {
			if (aBlock instanceof ArraySensorDataBlock == false) {
				continue;
			}
			ArraySensorDataBlock sensBlock = (ArraySensorDataBlock) aBlock;
			if (sensBlock.hasSensorField(sensorType)) {
				sensBlocks.add(aBlock);
			}
		}
		return sensBlocks;
	}
}
