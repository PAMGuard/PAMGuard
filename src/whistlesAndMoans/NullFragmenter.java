package whistlesAndMoans;

/** 
 * the Null fragmenter doesn't break Connected Regions at all. 
 * @author Doug
 *
 */
public class NullFragmenter implements RegionFragmenter {

	private ConnectedRegion connectedRegion;
	
	@Override
	public int fragmentRegion(ConnectedRegion connectedRegion) {
		this.connectedRegion = connectedRegion;
		return 1;
	}

	@Override
	public ConnectedRegion getFragment(int fragment) {
		return connectedRegion;
	}

	@Override
	public int getNumFragments() {
		return (connectedRegion == null ? 0: 1);
	}

}
