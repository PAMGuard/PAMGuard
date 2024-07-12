package tethys.localization;

import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamguardMVC.PamDataBlock;

/**
 * Summary of localisation info in a datablock, specific to Tethys needs. 
 * @author dg50
 *
 */
public class TethysLocalisationInfo {

	private PamDataBlock pamDataBlock;

	public TethysLocalisationInfo(PamDataBlock pamDataBlock) {
		this.pamDataBlock = pamDataBlock;
	}
	
	public String getLoclisationTypes() {
		LocalisationInfo locCont = pamDataBlock.getLocalisationContents();
		if (locCont == null || locCont.getLocContent() == 0) {
			return null;
		}
		String str = null;
		int[] mainTypes = LocContents.mainTypes;
		for (int i = 0; i < mainTypes.length; i++) {
			if (locCont.hasLocContent(mainTypes[i])) {
				if (str == null) {
					str = LocContents.getTypeString(mainTypes[i]);
				}
				else {
					str += ", " + LocContents.getTypeString(mainTypes[i]);
				}
			}
		}
		return str;
	}

}
