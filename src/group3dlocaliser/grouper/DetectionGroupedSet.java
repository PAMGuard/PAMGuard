package group3dlocaliser.grouper;

import java.util.ArrayList;
import java.util.List;

import PamguardMVC.PamDataUnit;

public class DetectionGroupedSet {
	
	private List<List<PamDataUnit>> groupLists = new ArrayList<>();

	public DetectionGroupedSet() {
		
	}

	public void addGroup(List<PamDataUnit> dataList) {
		groupLists.add(dataList);
	}
	
	public int getNumGroups() {
		return groupLists.size();
	}
	
	public List<PamDataUnit> getGroup(int iGroup) {
		return groupLists.get(iGroup);
	}
	/**
	 * Return true if the given uid is inlcuded anywhere in this group
	 * @param uid
	 * @return true if uid is in group
	 */
	public boolean hasUID(long uid) {
		for (List<PamDataUnit> l1 : groupLists) {
			for (PamDataUnit l2: l1) {
				if (l2.getUID() == uid) {
					return true;
				}
			}
		}
		return false;
	}
}
