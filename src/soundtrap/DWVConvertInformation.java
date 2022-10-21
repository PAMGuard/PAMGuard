package soundtrap;

/**
 * Status information on how far conversion progress has gone. 
 * @author Doug
 *
 */
public class DWVConvertInformation {

	private STGroupInfo fileGroup;
	private int nDWV;
	private int nDWVDone;
	private int nFileDone;
	private int nFile;


	public DWVConvertInformation(STGroupInfo fileGroup, int nFile, int nFileDone, int nDWV, int nDWVDone) {
		this.fileGroup = fileGroup;
		this.nFile = nFile;
		this.nFileDone = nFileDone;
		this.nDWV = nDWV;
		this.nDWVDone = nDWVDone;
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
	 * @return the nDWVDone
	 */
	public int getnDWVDone() {
		return nDWVDone;
	}


	/**
	 * @return the nFileDone
	 */
	public int getnFileDone() {
		return nFileDone;
	}


	/**
	 * @return the nFile
	 */
	public int getnFile() {
		return nFile;
	}

}
