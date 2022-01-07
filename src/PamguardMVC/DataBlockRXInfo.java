package PamguardMVC;

public class DataBlockRXInfo extends LastDataUnitStore {

	private PamDataUnit lastDataUnit;
	
	private int nDataUnits;
	
	private int totalBytesRX;

	public DataBlockRXInfo(PamDataUnit lastDataUnit, int dataBytesRX) {
		super();
		this.lastDataUnit = lastDataUnit;
		this.totalBytesRX = dataBytesRX;
		nDataUnits = 1;
	}

	public DataBlockRXInfo() {
		// TODO Auto-generated constructor stub
	}

	public void newDataunit(PamDataUnit lastDataUnit, int dataBytesRX) {
		this.lastDataUnit = lastDataUnit;
		this.totalBytesRX += dataBytesRX;
		nDataUnits += 1;
	}

	/**
	 * @return the lastDataUnit
	 */
	public PamDataUnit getLastDataUnit() {
		return lastDataUnit;
	}

	/**
	 * @return the nDataUnits
	 */
	public int getnDataUnits() {
		return nDataUnits;
	}

	/**
	 * @return the totalBytesRX
	 */
	public int getTotalBytesRX() {
		return totalBytesRX;
	}
	
	
}
