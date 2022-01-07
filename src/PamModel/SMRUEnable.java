package PamModel;

import PamUtils.PamUtils;

public class SMRUEnable {

	private static boolean enable = false;
	
	private static boolean meygen17 = false;

	private static boolean enableDecimus;
	
	public static final int meygenGoodBitmap = 0xFFFFFBF;
	
	public static final int getGoodChannels(int channelMap) {
		if (meygen17) {
			return channelMap & meygenGoodBitmap;
		}
		else {
			return channelMap;
		}
	}
	
	public static int[] makeUsedChannelLUT(int channelMap) {
//		return PamUtils.getChannelPositionLUT(getGoodChannels(channelMap));
		int usedMap = getGoodChannels(channelMap);
		int nOut = PamUtils.getNumChannels(usedMap);
		int iIn = PamUtils.getNumChannels(channelMap);
		int[] chInd = new int[nOut];
		for (int i = 0; i < nOut; i++) {
			chInd[i] = PamUtils.getChannelPos(PamUtils.getNthChannel(i, usedMap), channelMap);
		}
		return chInd;
	}

	private static long meygenChannel6Fail = 1510061936000L; // 2017-11-07 13:38:56 
	/**
	 * @return the meygen17 flag to indicate this is data from the 
	 * Meygen turbine in 2017. 
	 */
	public static boolean isMeygen17(long timeMilliseconds) {
		return meygen17 && timeMilliseconds > meygenChannel6Fail;
	}

	/**
	 * flag to indicate this is data from the 
	 * Meygen turbine in 2017. 
	 * @param meygen17 the meygen17 to set
	 */
	public static void setMeygen17(boolean meygen17) {
		SMRUEnable.meygen17 = meygen17;
	}

	/**
	 * @return the enable
	 */
	public static boolean isEnable() {
		return enable;
	}

	/**
	 * @param enable the enable to set
	 */
	public static void setEnable(boolean enable) {
		SMRUEnable.enable = enable;
	}

	/**
	 * @return the enableDecimus
	 */
	public static boolean isEnableDecimus() {
		return enableDecimus;
	}

	/**
	 * @param enableDecimus the enableDecimus to set
	 */
	public static void setEnableDecimus(boolean enableDecimus) {
		SMRUEnable.enableDecimus = enableDecimus;
	}
	
}
