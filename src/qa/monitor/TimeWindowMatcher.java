package qa.monitor;

public class TimeWindowMatcher extends TimeDetectionMatcher {

	public TimeWindowMatcher(long preWindowMillis, long postWindowMillis) {
		setPreWindowMillis(preWindowMillis);
		setPostWindowMillis(postWindowMillis);
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean showSettings(Object parentWindow) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return "Extended time match";
	}

	@Override
	public String getDescription() {
		long pre = getPreWindowMillis();
		long post = getPostWindowMillis();
//		if (pre != 0 && post != 0) {
			return String.format("Time window from %3.1fs before to %3.1fs after", (double) pre / 1000., (double) post / 1000.);
//		}
		
//		return super.getDescription();
	}

}
