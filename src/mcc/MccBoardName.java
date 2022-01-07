package mcc;

public class MccBoardName {

	private int boardNumber;
	
	private String boardName;
	

	/**
	 * @param boardIndex
	 */
	public MccBoardName(int boardNumber, String boardName) {
		super();
		this.boardNumber = boardNumber;
		this.boardName = boardName;
	}


	/**
	 * @return the boardIndex
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
}
