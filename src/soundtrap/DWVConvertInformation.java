package soundtrap;

/**
 * Status information on how far conversion progress has gone. 
 * @author Doug
 *
 */
public class DWVConvertInformation {

	private STGroupInfo fileGroup;
	private int nDWV;
	private int nDone;


	public DWVConvertInformation(STGroupInfo fileGroup, int nDWV, int nDone) {
		this.fileGroup = fileGroup;
		this.nDWV = nDWV;
		this.nDone = nDone;
	}


	/**
	 * @return the fileGroup
	 */
	public STGroupInfo getFileGroup() {
		return fileGroup;
	}


	/**
	 * @return the tnDWV
	 */
	public int getnDWV() {
		return nDWV;
	}


	/**
	 * @return the nDone
	 */
	public int getnDone() {
		return nDone;
	}

}
