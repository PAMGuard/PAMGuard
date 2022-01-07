package d3;

public class TLogLine  {

	static public final int ID_WAV = 0x8;
	static public final int ID_SENSOR = 0x4;
	
	long timeMillis;
	
	int id;
	
	long samples;
	
	public TLogLine(String[] data) {
		if (data != null && data.length == 4) {
			unpackStrings(data);
		}
	}

	private void unpackStrings(String[] data) {
		try {
			long secs = Long.valueOf(data[0]);
			long micros = Long.valueOf(data[1]);
			timeMillis = secs*1000 + micros/1000;
			id = Integer.valueOf(data[2]);
			samples = Long.valueOf(data[3]);
		}
		catch (NumberFormatException e) {
			
		}
	}
}
