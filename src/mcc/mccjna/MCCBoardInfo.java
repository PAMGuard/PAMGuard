package mcc.mccjna;

import java.util.Hashtable;

/**
 * Information about an MCCboard
 * @author dg50
 *
 */
public class MCCBoardInfo {
	
	private int boardIndex;
	
	private int boardNumber; 
	
	private String boardName;
	
	private Hashtable<String, int[]> allowableRanges;

	/**
	 * @param boardNumber
	 * @param boardName
	 */
	public MCCBoardInfo(int boardIndex, int boardNumber, String boardName) {
		super();
		this.boardIndex = boardIndex;
		this.boardNumber = boardNumber;
		this.boardName = boardName;
	}

	/**
	 * @return the boardIndex
	 */
	public int getBoardIndex() {
		return boardIndex;
	}

	/**
	 * @return the boardNumber
	 */
	public int getBoardNumber() {
		return boardNumber;
	}

	/**
	 * @return the boardName
	 */
	public String getBoardName() {
		return boardName;
	}

	@Override
	public String toString() {
		return String.format("%d: %s", boardNumber, boardName);
	}
	
	public int[] getAllowableRanges(int terminalType, boolean continuous, int poleType) {
		String idString = makeSetName(terminalType, continuous, poleType);
		if (allowableRanges == null) {
			allowableRanges = new Hashtable<>();
		}
		int[] ranges = allowableRanges.get(idString);
		if (ranges == null || ranges.length == 0) {
			ranges = MCCUtils.getAvailableRanges(boardNumber, terminalType, continuous, 
					poleType == MCCConstants.UNIPOLAR, poleType == MCCConstants.BIPOLAR);
			if (ranges != null) {
				allowableRanges.put(idString, ranges);
			}
		}
		return ranges;
	}
	
	/**
	 * Make a string that will uniquely define a set of allowable gains. 
	 * @param continuous continuous sampling 
	 * @param terminalType either DIFFERENTIAL or SINGLE_ENDED
	 * @param poleType either UNIPOLAR or BIPOLAR
	 * @return Unique identifying string to use in Hashtable 
	 */
	private String makeSetName(int terminalType, boolean continuous, int poleType) {
		return String.format("%s_%d_%d", continuous ? "CONTINUOUS" : "SINGLESAMPLE", 
				terminalType, poleType);
	}
	
}
