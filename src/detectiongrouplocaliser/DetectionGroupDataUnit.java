package detectiongrouplocaliser;

import java.util.List;

import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetection;

public class DetectionGroupDataUnit extends SuperDetection<PamDataUnit> {
	
	// stick to using the duration and start information in the standard data unit. 
//	private long groupEndTime;
	
//	private int nSubDetections;

	public DetectionGroupDataUnit(long timeMilliseconds, List<PamDataUnit> list) {
		super(timeMilliseconds);
		addSubDetections(list);
	}
//
//	/**
//	 * Add a list full of sub detections
//	 * @param list list of sub detections
//	 * @return number of sub detections added. 
//	 */
//	public int addDetectionList(List<PamDataUnit> list) {
//		if (list == null) {
//			return 0;
//		}
//		for (PamDataUnit dataUnit:list) {
//			this.addAndCountSubDetection(dataUnit);
//			long groupEndTime = Math.max(getEndTimeInMilliseconds(), dataUnit.getTimeMilliseconds());
//			setGroupEndTime(groupEndTime);
//			if (dataUnit.getTimeMilliseconds() < this.getTimeMilliseconds()) {
//				this.setTimeMilliseconds(dataUnit.getTimeMilliseconds());
//			}
//			this.setChannelBitmap(this.getChannelBitmap() | dataUnit.getChannelBitmap());
//		}
//		
//		//Debug.out.println("GroupStartTime: 2"  +this.getTimeMilliseconds()+  "GroupEndTime: " + groupEndTime + (this.getTimeMilliseconds()<groupEndTime));
//		return list.size();
//	}
//
//	/**
//	 * @return the groupEndTime
//	 */
//	public long getGroupEndTime() {
//		return getEndTimeInMilliseconds();
//	}
//
//	/**
//	 * @param groupEndTime the groupEndTime to set
//	 */
//	public void setGroupEndTime(long groupEndTime) {
//		this.setDurationInMilliseconds(groupEndTime- getTimeMilliseconds());
//	}
//
//	/**
//	 * Add a sub detection and count up that it's been added. 
//	 * @param subDetection - the sub detection to add
//	 * @return  
//	 */
//	public int addAndCountSubDetection(PamDataUnit subDetection) {
//		nSubDetections ++;
//		this.setChannelBitmap(this.getChannelBitmap() | subDetection.getChannelBitmap());
////		if (subDetection.getTimeMilliseconds() > getGroupEndTime()) {
////			groupEndTime = subDetection.getTimeMilliseconds();
////		}
////		if (subDetection.getTimeMilliseconds() < this.getTimeMilliseconds()) {
////			this.setTimeMilliseconds(subDetection.getTimeMilliseconds());
////		}
//		
//		//Debug.out.println("GroupStartTime: 1 "  +this.getTimeMilliseconds()+  "GroupEndTime: " + groupEndTime + (this.getTimeMilliseconds()<groupEndTime));
//		//do not super this so that DetectionGroupLocaliser and sub classes can override addSubDetection
//		return addSubDetection(subDetection);
//	}
//	
//	
//	/**
//	 * Get the duration of the sub detection (first to last detection)
//	 * @return the duration in milliseconds.
//	 */
//	public long getListDurationInMillis() {
//		//Debug.out.println("Group End time: " + groupEndTime + " Time millis: " + this.getTimeMilliseconds());
//		return  getGroupEndTime()-this.getTimeMilliseconds(); 
//	}
//
//
//	/**
//	 * Remove a sub section and count that it's been removed. 
//	 * @param subDetection
//	 */
//	public void removeAndCountSubDetection(PamDataUnit subDetection) {
//		nSubDetections--;
//		super.removeSubDetection(subDetection);
//	}
//	
//	/**
//	 * Remove a all sub detections and count that it's been removed. 
//	 * @param subDetection
//	 */
//	public void removeAllSubDetections() {
//		while (	super.getSubDetectionsCount()>0) {
//			nSubDetections--;
//			super.removeSubDetection(super.getSubDetection(0));
//		}
//	}
//
//	/**
//	 * @return the nSubDetections
//	 */
//	public int getnSubDetections() {
//		return nSubDetections;
//	}
//
//	/**
//	 * @param nSubDetections the nSubDetections to set
//	 */
//	public void setnSubDetections(int nSubDetections) {
//		this.nSubDetections = nSubDetections;
//	}

}
