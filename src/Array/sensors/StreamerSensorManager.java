package Array.sensors;

import java.util.ArrayList;
import java.util.ListIterator;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import GPS.GpsData;
import PamController.PamController;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Functions to manage streamer sensor data. Separate from Streamer since
 * streamer is serialized and putting more stuff in there is too much faff. 
 * <p>A streamer may use 0-n different datablocks to get different types of sensor data. Some data from 
 * some block may have more than one param, others only one, so will have a complicated mess of how to find the
 * right blocks and get the right data out of them. 
 * @author dg50
 *
 */
public class StreamerSensorManager {

	private Streamer streamer;
	
	private ArrayList<PamDataBlock> sensorDataBlocks;
	
	/*
	 * these make a cunning LUT of which fields are associated with which data
	 * block in the list. The max size will be 4x4 in the extreme case of a different
	 * parameter being used in every datablock. the LUT will be very sparsely populated. 
	 */
	private ArraySensorFieldType[][] sensorFieldLUT;
	int[] fieldsPerBlock;

	public StreamerSensorManager(Streamer streamer) {
		super();
		this.streamer = streamer;
		sensorDataBlocks = new ArrayList<>();
		findSensorStreams();
	}
	
	private void findSensorStreams() {
		sensorDataBlocks.clear();
		ArraySensorFieldType[] fieldTypes = ArraySensorFieldType.values();
		sensorFieldLUT = new ArraySensorFieldType[fieldTypes.length][fieldTypes.length];
		fieldsPerBlock = new int[fieldTypes.length];
		for (int i = 0; i < fieldTypes.length; i++) {
			ArrayParameterType sensSource = streamer.getOrientationTypes(fieldTypes[i]);
			if (sensSource != ArrayParameterType.SENSOR) {
				continue; // it's either fixed or default, so no need to find sensor data
			}
			String sensorBlock = streamer.getSensorDataBlocks(fieldTypes[i]);
			PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(sensorBlock);
			if (dataBlock == null) {
				// this happens a few times at startup as the data are deserialised. 
//				System.err.printf("Sensor datablock %s cannot be found for %s in streamer %d\n", sensorBlock, fieldTypes[i], streamer.getStreamerIndex());
				continue;
			}
			int currInd = sensorDataBlocks.indexOf(dataBlock);
			if (currInd < 0) {
				currInd = sensorDataBlocks.size();
				sensorDataBlocks.add(dataBlock);
			}
			sensorFieldLUT[currInd][fieldsPerBlock[currInd]++] = fieldTypes[i];
		}
	}
	
	public boolean getOrientationData(GpsData gpsData, long timeMilliseconds) {
		
		// do the blocks that have sensor data
		boolean ok = addSensorData(gpsData, timeMilliseconds);
		
		// now do any other parameters which should be set to a fixed value.
		// these should be mutually exclusive to those set above with sensor data. 
		ArraySensorFieldType[] fieldTypes = ArraySensorFieldType.values();
		for (int i = 0; i < fieldTypes.length; i++) {
			ArrayParameterType sensSource = streamer.getOrientationTypes(fieldTypes[i]);
			if (sensSource == ArrayParameterType.FIXED) {
				gpsData.setOrientationField(fieldTypes[i], streamer.getSensorField(fieldTypes[i]));
			}
//			else if (sensSource == ArrayParameterType.DEFAULT) {
//				setField(gpsData, fieldTypes[i], null); // don't do anything, all should be null apart from heading anyway. 
//			}
		}
			
		return ok;
	}
	
	
	/**
	 * work through the sensor data blocks and find the closest data to each unit we're interested in following 
	 * appropriate interpolation rules. 
	 * @param gpsData
	 * @param timeMilliseconds
	 * @return true if all data were found, false if any missing. 
	 */
	private boolean addSensorData(GpsData gpsData, long timeMilliseconds) {
		boolean ok = true;
		for (int iBlock = 0; iBlock < sensorDataBlocks.size(); iBlock++) {
			PamDataBlock sensorBlock = sensorDataBlocks.get(iBlock);
			PamDataUnit precUnit = null, nextUnit = null, aUnit;
			synchronized (sensorBlock.getSynchLock()) {
				ListIterator<PamDataUnit> iter = sensorBlock.getListIterator(timeMilliseconds, 1<<streamer.getStreamerIndex(), PamDataBlock.MATCH_AFTER, PamDataBlock.POSITION_AFTER);
				if (iter == null) {
					return false;
				}
				while (iter.hasPrevious()) {
					aUnit = iter.previous();
					if (aUnit == null) {
						continue;
					}
					if (aUnit.getTimeMilliseconds() >= timeMilliseconds) {
						nextUnit = aUnit;
					}
					if (aUnit.getTimeMilliseconds() <= timeMilliseconds) {
						precUnit = aUnit;
						break;
					}
				}
			}
			for (int i = 0; i < fieldsPerBlock[iBlock]; i++) {
				ok |= setField(gpsData, sensorFieldLUT[iBlock][i], timeMilliseconds, precUnit, nextUnit);
			}
		}
		return ok;
	}
	

	/**
	 * Set a field in GPS data taking into account any interpolation specified by the streamer
	 * @param gpsData
	 * @param arraySensorFieldTypes
	 * @param timeMilliseconds
	 * @param precUnit
	 * @param nextUnit
	 * @return
	 */
	private boolean setField(GpsData gpsData, ArraySensorFieldType fieldType, long timeMilliseconds,
			PamDataUnit precUnit, PamDataUnit nextUnit) {
		if (precUnit == null && nextUnit == null) {
			return false;
		}		
		Double val = getValue(fieldType, timeMilliseconds, precUnit, nextUnit);
		if (val == null) {
			return false;
		}
		
		return gpsData.setOrientationField(fieldType, val);
	}

	private Double getValue(ArraySensorFieldType fieldType, long timeMilliseconds, PamDataUnit precUnit,
			PamDataUnit nextUnit) {
		// if we come into this function, one or other of the data units must not be null
		if (precUnit == null) {
			return ((ArraySensorDataUnit) nextUnit).getField(streamer.getStreamerIndex(), fieldType);
		}
		if (nextUnit == null) {
			return ((ArraySensorDataUnit) precUnit).getField(streamer.getStreamerIndex(), fieldType);
		}
		// else neither are null. 
		Double prevVal = ((ArraySensorDataUnit) precUnit).getField(streamer.getStreamerIndex(), fieldType);
		Double nextVal = ((ArraySensorDataUnit) nextUnit).getField(streamer.getStreamerIndex(), fieldType);
		if (prevVal == null) {
			return nextVal;
		}
		if (nextVal == null) {
			return prevVal;
		}
		// neither value is null, so can average or whatever.  
		int interp = ArrayManager.getArrayManager().getCurrentArray().getHydrophoneInterpolation();
		if (interp == PamArray.ORIGIN_USE_PRECEEDING) {
			return prevVal;
		}
		else if (interp == PamArray.ORIGIN_USE_LATEST) {
			return nextVal;
		}
		// otherwise interpolate. 
		double wn, wp;
		wp = nextUnit.getTimeMilliseconds()-timeMilliseconds;
		wn = timeMilliseconds - precUnit.getTimeMilliseconds();
		double wTot = wn+wp;
		wn /= wTot;
		wp /= wTot;
		if (fieldType == ArraySensorFieldType.HEIGHT) {
			// simple geometric average
			return nextVal*wn+prevVal*wp;
		}
		else {
			// all other fields are angles, so do an angular average. 
			return PamUtils.angleAverageDegrees(prevVal, nextVal, wp, wn);
		}
		
	}
	
}
