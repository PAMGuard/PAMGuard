package tethys.dbxml;

public class ServerStatus {

	public boolean ok;

	public Exception error;

	public ServerStatus(boolean ok, Exception error) {
		super();
		this.ok = ok;
		this.error = error;
	}

	public String getFormatted() {
		if (ok) {
			return "Server OK";
		}
		if (error == null) {
			return "Unknown error";
		}
		String msg = error.getLocalizedMessage();
		if (msg.startsWith("Exception")) {
			msg.substring(9);
		}
		return msg;
	}

	@Override
	public String toString() {
		return getFormatted();
	}


}
