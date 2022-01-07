package listening;

import PamguardMVC.PamDataUnit;;

public class ListeningEffortData extends PamDataUnit {

	private String status;
	
	public ListeningEffortData(long timeMilliseconds, String status, int channels) {
		super(timeMilliseconds);
		this.status = status;
		setChannelBitmap(channels);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
