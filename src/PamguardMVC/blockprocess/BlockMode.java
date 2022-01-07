package PamguardMVC.blockprocess;

import PamModel.parametermanager.EnumToolTip;

/**
 * Possible blocking modes, none, always blocked, each processed independently,
 * block the first block to get the background then continue as normal, or
 * reverse the first block, then continue as normal.
 * 
 * @author dg50
 *
 */
public enum BlockMode implements EnumToolTip {

	NONE, BLOCKED, BLOCKBYFILE, BLOCKFIRST, REVERSEFIRST;

	@Override
	public String getToolTip() {
		switch (this) {
		case BLOCKED:
			return "Process in blocks of fixed duration";
		case BLOCKBYFILE:
			return "Process file at a time";
		case BLOCKFIRST:
			return "Process first section as block, then normal continuous processing";
		case NONE:
			return "No blocking. This may cause the detector to take some time to 'settle'";
		case REVERSEFIRST:
			return "Reverse first section, then normal processing";
		default:
			break;

		}
		return null;
	}

	@Override
	public String toString() {
		switch (this) {
		case BLOCKED:
			return "Blocked Processing";
		case BLOCKBYFILE:
			return "Block by Filee";
		case BLOCKFIRST:
			return "Block First";
		case NONE:
			return "No Blocking";
		case REVERSEFIRST:
			return "Reverse First";
		default:
			break;

		}
		return null;
	}

}
