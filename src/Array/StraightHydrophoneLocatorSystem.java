package Array;

public class StraightHydrophoneLocatorSystem extends HydrophoneLocatorSystem {

	static protected final String sysName = "Straight / Rigid Streamer";
	public StraightHydrophoneLocatorSystem() {
		super(sysName);
	}

	@Override
	public LocatorDialogPanel getDialogPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HydrophoneLocator getLocator(PamArray array, Streamer streamer) {
		return new StraightHydrophoneLocator(array, streamer);
	}

	@Override
	public Class getLocatorClass() {
		return StraightHydrophoneLocator.class;
	}
}
