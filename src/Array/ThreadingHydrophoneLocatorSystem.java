package Array;

public class ThreadingHydrophoneLocatorSystem extends HydrophoneLocatorSystem {

	static protected final String sysName = "Threading Streamer";
	public ThreadingHydrophoneLocatorSystem() {
		super(sysName);
	}

	@Override
	public LocatorDialogPanel getDialogPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HydrophoneLocator getLocator(PamArray array, Streamer streamer) {
		return new ThreadingHydrophoneLocator(array, streamer);
	}

	@Override
	public Class getLocatorClass() {
		return ThreadingHydrophoneLocator.class;
	}
}
