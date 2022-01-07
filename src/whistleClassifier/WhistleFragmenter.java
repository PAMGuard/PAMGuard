package whistleClassifier;

/**
 * Break a whistle up into fragments. 
 * <p>
 * Really all we need to know is the start of each fragment
 * 
 * @author Doug Gillespie
 *
 */
public abstract class WhistleFragmenter {

	private int fragmentLength;
	
	abstract public int[] getFragmentStarts(int shapeLen);
	
	public void setFragmentLength(int fragmentLength) {
		this.fragmentLength = fragmentLength;
	}
	
	public int getFragmentLength() {
		return fragmentLength;
	}
	
	/**
	 * Create an array of whistle fragments from a whistle contour. 
	 * @param whistleContour contour to extract fragments from . 
	 * @return array of fragments
	 */
	public WhistleFragment[] getFragments(WhistleContour whistleContour) {
		double[] t = whistleContour.getTimesInSeconds();
		double[] f = whistleContour.getFreqsHz();
		int[] fragStarts = getFragmentStarts(t.length);
		if (fragStarts == null || fragStarts.length == 0) {
			return null;
		}
		WhistleFragment[] frags = new WhistleFragment[fragStarts.length];
		for (int i = 0; i < frags.length; i++) {
			frags[i] = new WhistleFragment(whistleContour, fragStarts[i], getFragmentLength());
		}
		return frags;
	}
	
}
