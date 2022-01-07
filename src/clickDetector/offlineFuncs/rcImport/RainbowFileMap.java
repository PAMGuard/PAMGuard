package clickDetector.offlineFuncs.rcImport;

import java.util.ArrayList;

public class RainbowFileMap {
	
	private ArrayList<RCMapPoint> rcMapPoints = new ArrayList<RCMapPoint>();
	
	public RainbowFileMap() {
		super();
		// TODO Auto-generated constructor stub
	}

	void clear() {
		rcMapPoints.clear();
	}
	
	void addMapPoint(String rainbowFile, int rainbowSection,
			String pamGuardFile) {
		rcMapPoints.add(new RCMapPoint(rainbowFile, rainbowSection, pamGuardFile));
	}
	
	RCMapPoint findMapPoint(String rainbowFile, int rainbowSection) {
		for (RCMapPoint mp:rcMapPoints) {
			if (mp.rainbowFile.equals(rainbowFile) && mp.rainbowSectoin == rainbowSection) {
				return mp;
			}
		}
		return null;
	}
	
	String getPamFileName(String rainbowFile, int rainbowSection) {
		RCMapPoint mp = findMapPoint(rainbowFile, rainbowSection);
		if (mp == null) {
			return null;
		}
		return mp.pamGuardFile;
	}


	private class RCMapPoint {
		String rainbowFile;
		int rainbowSectoin;
		String pamGuardFile;
		public RCMapPoint(String rainbowFile, int rainbowSectoin,
				String pamGuardFile) {
			super();
			this.rainbowFile = rainbowFile;
			this.rainbowSectoin = rainbowSectoin;
			this. pamGuardFile = pamGuardFile;
		}
	}
}
