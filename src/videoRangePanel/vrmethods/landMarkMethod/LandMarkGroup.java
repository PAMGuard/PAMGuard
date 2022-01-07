package videoRangePanel.vrmethods.landMarkMethod;

import java.io.Serializable;
import java.util.ArrayList;

import PamUtils.LatLong;

/**
 * List for storing landmarks.
 * @author Jamie Macaulay
 *
 */
public class LandMarkGroup extends ArrayList<LandMark> implements Serializable, Cloneable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String groupName;
	private String comment;

	/**
	 * Get the name of the group
	 * @return the name of the group
	 */
	public String getName() {
		return groupName;
	}
	
	/**
	 * Set the name of the group
	 * @param groupName - the name of the group. 
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	@Override
	public LandMarkGroup clone() {
		return (LandMarkGroup) super.clone();
	}
	
	@Override
	public String toString(){
		return (groupName + " No. Landmarks: " + this.size()); 
	}

	/**
	 * Get comment for the group
	 * @return
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * Set comment for the group. 
	 * @param comment
	 */
	public void setCommentText(String comment) {
		this.comment= comment;
	}

	public void update(LandMarkGroup landMarkGroupList) {
		this.removeRange(0, this.size());
		//update landmarks within list
		for (int i=0; i<landMarkGroupList.size(); i++){
			this.add(landMarkGroupList.get(i));
		}
		//update the name
		groupName=landMarkGroupList.getName();
		//
		comment=landMarkGroupList.getComment();
	}

	/**
	 * If any of the landmarks are defined by bearings rather than a LatLong location then 
	 * the height at which the bearings for this group were taken is returned. 
	 * @return
	 */
	public Double getOriginHeight() {
		for (int i=0; i<this.size(); i++){
			if (get(i).getHeightOrigin()!=null) return get(i).getHeightOrigin();
		}
		return null;
	}
	
	/**
	 * If any of the landmarks are defined by bearings rather than a LatLong location then 
	 * the height at which the bearings for this group were taken is returned. 
	 * @return
	 */
	public LatLong getOriginLatLong() {
		for (int i=0; i<this.size(); i++){
			if (get(i).getLatLongOrigin()!=null) return get(i).getLatLongOrigin();
		}
		return null;
	}

	
	/**
	 * Set landmarks. If null clears the landmarks. 
	 * @param landMarks
	 */
	public void setLandMarks(ArrayList<LandMark> landMarks) {
		this.clear();
		if (landMarks!=null) this.addAll(landMarks);
	}
		
}
