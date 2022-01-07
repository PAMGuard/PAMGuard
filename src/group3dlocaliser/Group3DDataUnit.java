package group3dlocaliser;

import java.util.List;

import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class Group3DDataUnit extends SuperDetection<PamDataUnit> {

	private String parentUIDList;
	
//	private Integer superDatabaseId;

	public Group3DDataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
	}

	public Group3DDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
	}

	public Group3DDataUnit(DataUnitBaseData basicData) {
		super(basicData);
	}
	
	public Group3DDataUnit(List<PamDataUnit> dataUnits) {
		this(dataUnits.get(0).getBasicData().clone());
		addSubDetections(dataUnits);
	}

//	private void addSubDetections(List<PamDataUnit> dataUnits) {
//		for (PamDataUnit dataUnit:dataUnits) {
//			PamDataUnit currentSuperDet = dataUnit.getSuperDetection(0);
//			if (currentSuperDet != null) {
//				setSuperDatabaseId(currentSuperDet.getDatabaseIndex());
//			}
//			addSubDetection(dataUnit);
//			setChannelBitmap(getChannelBitmap() | dataUnit.getChannelBitmap());
//		}
//	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataUnit#getSummaryString()
	 */
	@Override
	public String getSummaryString() {
		String str = super.getSummaryString();
		String pList = getParentUIDList();
		if (pList != null) {
			str += "Parent datas:<br>"+pList;
		}
		AbstractLocalisation loc = getLocalisation();
		if (loc instanceof GroupLocalisation) {
			GroupLocalisation groupLoc = (GroupLocalisation) loc;
			GroupLocResult result = groupLoc.getGroupLocaResult(0);
			str += "<br>" + result.toString();
			LocaliserError err = result.getLocError();
			if (err != null) {
				str += "<br>" + err.toString();
			}
		}
		
		return str;
	}

	public void setParentUIDList(String parents) {
		this.parentUIDList = parents;
	}

	public String getParentUIDList() {
		if (parentUIDList != null) {
			return parentUIDList;
		}
		synchronized (this.getSubDetectionSyncronisation()) {
			int nSub = getSubDetectionsCount();
			if (nSub == 0) {
				return null;
			}
			String str = String.format("%d", getSubDetection(0).getUID());
			for (int i = 1; i < nSub; i++) {
				str += String.format(",%d", getSubDetection(i).getUID());
			}
			return str;
		}
	}

//	/**
//	 * @return the superDatabaseId
//	 */
//	public Integer getSuperDatabaseId() {
//		return superDatabaseId;
//	}
//
//	/**
//	 * @param superDatabaseId the superDatabaseId to set
//	 */
//	public void setSuperDatabaseId(Integer superDatabaseId) {
//		this.superDatabaseId = superDatabaseId;
//	}

}
