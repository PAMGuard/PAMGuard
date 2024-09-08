package clickTrainDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.dataselector.CTSelectParams;
import detectiongrouplocaliser.DetectionGroupDataUnit;

/**
 * Base class for a click train data unit. 
 * Note this must implrement PamDetection to work with the clip generator. 
 * 
 * @author Jamie Macaulay 
 *
 */
public abstract class CTDetectionGroupDataUnit extends DetectionGroupDataUnit implements PamDetection {

	/**
	 * A list of summary data units for a click train. These data units are used for plotting. 
	 */
	ArrayList<PamDataUnit> summaryDataUnits; 


	/**
	 * The minimum bearing angle of all data units in RADIANS
	 */
	private double minAngle = 0;

	/**
	 * The maximum bearing angle of all bearing units in RADIANS
	 */
	private double maxAngle = 0;

	public CTDetectionGroupDataUnit(long timeMilliseconds, List<PamDataUnit> list) {
		super(timeMilliseconds, list);
	}
	
	
	@Override
	public int addSubDetections(List<PamDataUnit> list) {
		int h = super.addSubDetections(list);
		this.calcMinMaxAng();
		
		//make sure there is a non null start sample otherwise
		//the click trian detector will crash in real time mode. 
//		this.calcStartSample(); 
		return h; 
	}
	

	/**
	 * Calculate a valid start sample. This is important for the clip generator. 
	 */
	public void calcStartSample() {
		Long startSample = Long.MAX_VALUE; 
		
		//find lowest start sample...? 
		//TODO might not work if click trains are between files...meh. 
		if (getSubDetectionsCount()>0) {
			Long aStartSample; 
			for (int i=0; i<this.getSubDetectionsCount(); i++) {
				aStartSample = getSubDetection(i).getStartSample(); 
				if (aStartSample!=null && aStartSample<startSample) {
					startSample = aStartSample; 
				}
			}
		}
		else if (super.getStartSample()!=null) {
			startSample = super.getStartSample(); 
		} 
		
		//if all else has failed set to zero
		if (startSample==null) startSample = 0L;
		
		setStartSample(startSample); 
		
	}
	

	//	public ClickTrainControl getClickTrainControl() {
	//		this.getParentDataBlock().getClickTrainControl(); 
	//	}


	/**
	 * Update the data unit list. The summary data units are used to represent a
	 * click train with a sub set of data units; usually this is for plotting
	 * purposes but could also be used for sending data where bandwidth is limited
	 * e.g. satellite or 4G.
	 * 
	 * @param ctDataUnit the data unit list.
	 */
	public void calcSummaryUnits(CTSelectParams ctDataUnitParams) {

		summaryDataUnits = new ArrayList<PamDataUnit>();

		ArrayList<PamDataUnit<?,?>> subDet = this.getSubDetections();

		if (subDet==null) {
			return; 
		}

		ListIterator<PamDataUnit<?, ?>> iterator = subDet.listIterator();

		//check that the datablock has localisation contents. 
		if (subDet.get(0).getParentDataBlock().getLocalisationContents().hasLocContent(LocContents.HAS_BEARING)){	
			//now that that is done calculate the summary data units. 
			summaryDataUnits.add(subDet.get(0)); 
			long startTime = subDet.get(0).getTimeMilliseconds(); 
			double lastAngle = subDet.get(0).getLocalisation().getAngles()[0]; 
			//iterate through the list to find the sub data units
			PamDataUnit dataUnit; 
			double angle; 
			int n=0; 
			//CTLocalisation.minimum and maximum angles
			while (iterator.hasNext()) {
				dataUnit=iterator.next();
				angle=dataUnit.getLocalisation().getAngles()[0]; 
				if ((dataUnit.getTimeMilliseconds()-startTime)>ctDataUnitParams.minTime
						&& ((dataUnit.getTimeMilliseconds()-startTime) > ctDataUnitParams.maxTime || 
								Math.abs(lastAngle-angle) > ctDataUnitParams.maxAngleChange)) {

					startTime=dataUnit.getTimeMilliseconds();
					lastAngle= angle; 
					summaryDataUnits.add(subDet.get(n)); 
				}
				n++;
			}
		}
	}

	
	/**
	 * Calculate the minimum and maximum angle of the whole click train. 
	 */
	public void calcMinMaxAng() {
		ArrayList<PamDataUnit<?,?>> subDet = this.getSubDetections();

		if (subDet==null || subDet.get(0).getLocalisation()==null) {
			return; 
		}

		ListIterator<PamDataUnit<?, ?>> iterator = subDet.listIterator();

		int n=0; 
		//CTLocalisation.minimum and maximum angles
		double maxAngle = -Double.MAX_VALUE;
		double minAngle = Double.MAX_VALUE;

		if (subDet.get(0).getLocalisation()!=null) {
			double lastAngle = subDet.get(0).getLocalisation().getAngles()[0];


			PamDataUnit dataUnit; 
			double angle; 
			while (iterator.hasNext()) {
				dataUnit=iterator.next();
				angle=dataUnit.getLocalisation().getAngles()[0]; 

				lastAngle= angle; 

				//check that the datablock has localisation contents. 
				if (subDet.get(0).getParentDataBlock().getLocalisationContents().hasLocContent(LocContents.HAS_BEARING)){
					if (lastAngle>maxAngle) maxAngle=lastAngle;
					if (lastAngle<minAngle) minAngle=lastAngle;

					n++;
				}
			}

			this.minAngle=minAngle;
			this.maxAngle=maxAngle;
		}
	}

	/**
	 * Clear data units from the summary list
	 */
	public void clearSummaryDataUnits() {
		summaryDataUnits.clear(); 
	}

	/**
	 * Get the summary data units from the last calculation. 
	 * @return summary data units. 
	 */
	public ArrayList<PamDataUnit> getSummaryUnits() {
		return summaryDataUnits;
	}

	/**
	 * Get the angle range in RADIANS. 
	 * @return the angle range. 
	 */
	public double getAngleRange() {
		return maxAngle-minAngle;
	}


}
