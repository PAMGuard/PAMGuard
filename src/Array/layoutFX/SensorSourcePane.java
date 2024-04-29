package Array.layoutFX;

import java.util.ArrayList;

import Array.sensors.ArrayParameterType;
import Array.sensors.ArraySensorDataBlock;
import Array.sensors.ArraySensorDataUnit;
import Array.sensors.ArraySensorFieldType;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;

public class SensorSourcePane {
	
	private ArraySensorFieldType sensorType;
	
	private ComboBox<String> sensorDropDown;
	
	private ArrayList<PamDataBlock> currentBlocks;

	private boolean defaultOption;

	private boolean fixedOption;
	
	private int nSpecials;
	
	private ArrayParameterType[] specialTypes = new ArrayParameterType[2];

	public SensorSourcePane(ArraySensorFieldType sensorType, boolean fixedOption, boolean defaultOption) {
		this.sensorType = sensorType;
		this.fixedOption = fixedOption;
		this.defaultOption = defaultOption;
		sensorDropDown = new ComboBox<>();
		fillDropDown();
		sensorDropDown.setTooltip(new Tooltip("Sensor updates for " + sensorType.toString()));
	}
	
	/**
	 * Get the type of sensor. 
	 * @return the type of sensor. 
	 */
	public ArraySensorFieldType getSensorType() {
		return sensorType;
	}

	public void setOnAction(EventHandler<ActionEvent> e) {
		sensorDropDown.setOnAction(e);
	}

	public void fillDropDown() {
		currentBlocks = getDataBlocks();
		sensorDropDown.getItems().clear();
		nSpecials = 0;
		if (fixedOption) {
			sensorDropDown.getItems().add("Fixed Value");
			specialTypes[nSpecials++] = ArrayParameterType.FIXED;
		}
		if (defaultOption) {
			sensorDropDown.getItems().add("Default value");
			specialTypes[nSpecials++] = ArrayParameterType.DEFAULT;
		}
		for (PamDataBlock aBlock : currentBlocks) {
			sensorDropDown.getItems().add(aBlock.getDataName());
		}
	}
	
	/**
	 * Set the type of parameter being used, fixed, default or sensor 
	 * @param paramType
	 */
	public void setParameterType(ArrayParameterType paramType) {
		for (int i = 0; i < nSpecials; i++) {
			if (paramType == specialTypes[i]) {
				sensorDropDown.getSelectionModel().select(i);
				return;
			}
		}
	}
	
	/**
	 * Get the type of parameter being used, fixed, default or sensor 
	 * @return
	 */
	public ArrayParameterType getParameterType() {
		int ind = sensorDropDown.getSelectionModel().getSelectedIndex();
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
		sensorDropDown.getSelectionModel().select(ind+nSpecials); // offset by 1 to allow for null option. 
		return true;
	}
	
	/**
	 * 
	 * @return Currently selected datablock for this sensor (can be null)
	 */
	public PamDataBlock getDataBlock() {
		int ind = sensorDropDown.getSelectionModel().getSelectedIndex();
		if (ind <= 0 || currentBlocks == null) {
			return null;
		}
		else {
			return currentBlocks.get(ind-nSpecials);
		}
	}

	/**
	 * Get the sensor pane. 
	 * @return the sensor pane
	 */
	public Node getPane() {
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
