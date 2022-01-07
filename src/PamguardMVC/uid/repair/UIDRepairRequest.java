package PamguardMVC.uid.repair;

import java.util.ArrayList;
import java.util.Iterator;

import PamguardMVC.PamDataBlock;
import PamguardMVC.uid.UIDStatusReport;

public class UIDRepairRequest {

	public PamDataBlock dataBlock;
			
	public UIDStatusReport dbStatusReport, binStatusReport;

	public UIDRepairRequest(PamDataBlock pamDataBlock) {
		this.dataBlock = pamDataBlock;
	}
	
	public int getDBStatus() {
		return getStatus(dbStatusReport);
	}
	
	public int getBinStatus() {
		return getStatus(binStatusReport); 
	}
	
	public int getStatus(UIDStatusReport report) {
		if (report == null) {
			return UIDStatusReport.UID_NO_DATA;
		}
		else {
			return report.getUidStatus();
		}
	}
	
	/**
	 * 
	 * @return true if this block actually needs repairing
	 */
	public boolean needsRepair() {
		if (getStatus(dbStatusReport) >= UIDStatusReport.UID_ABSENT) {
			return true;
		}
		if (getStatus(binStatusReport) >= UIDStatusReport.UID_ABSENT) {
			return true;
		}
		return false;
	}

	public static ArrayList<UIDRepairRequest> trimRepairList(ArrayList<UIDRepairRequest> repairList) {
		Iterator<UIDRepairRequest> it = repairList.iterator();
		while (it.hasNext()) {
			UIDRepairRequest rr = it.next();
			if (rr.needsRepair() == false) {
				it.remove();
			}
		}
		return repairList;
	}

}
