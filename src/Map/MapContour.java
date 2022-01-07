package Map;

import java.util.Vector;

import PamUtils.LatLong;

public class MapContour implements Comparable<MapContour>{

	private int depth;
	
	private Vector<LatLong> latLongs;

	public MapContour(int depth) {
		this.depth = depth;
		latLongs = new Vector<LatLong>();
	}

	public void addLatLong(LatLong latLong) {
		latLongs.add(latLong);
	}
	/**
	 * @return Returns the depth.
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * @param depth The depth to set.
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * @return Returns the latLongs.
	 */
	public Vector<LatLong> getLatLongs() {
		return latLongs;
	}

	@Override
	public int compareTo(MapContour o) {
		return this.depth - o.depth;
	}

	

}
