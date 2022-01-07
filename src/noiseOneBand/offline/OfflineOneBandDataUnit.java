package noiseOneBand.offline;

import noiseOneBand.OneBandDataUnit;

public class OfflineOneBandDataUnit extends OneBandDataUnit {

	private int nDatas;
	private int interval;
	
	public OfflineOneBandDataUnit(long timeMilliseconds, int channelBitmap,
			long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the nDatas
	 */
	public int getnDatas() {
		return nDatas;
	}

	/**
	 * @param nDatas the nDatas to set
	 */
	public void setnDatas(int nDatas) {
		this.nDatas = nDatas;
	}

	/**
	 * @return the interval
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

}
