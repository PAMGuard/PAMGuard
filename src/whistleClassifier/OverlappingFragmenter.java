package whistleClassifier;

import java.util.Random;

public class OverlappingFragmenter extends WhistleFragmenter {

	private Random random;
	
	private int fragmentLength = 8;
	
	public OverlappingFragmenter() {
		random = new Random();
	}
	
	@Override
	public int[] getFragmentStarts(int shapeLen) {

		int nFragments = (int) Math.ceil((double) shapeLen / fragmentLength);

		if (fragmentLength > shapeLen) return null;

		int[] fragmentStarts = new int[nFragments];

		if (nFragments == 1) {
			fragmentStarts[0] = random.nextInt(shapeLen - fragmentLength + 1); 
		}
		else {
			double stepSize = (shapeLen-fragmentLength) / (nFragments-1);
			for (int i = 0; i < nFragments; i++) {
				fragmentStarts[i] = (int) (i * stepSize);
			}
		}


		return fragmentStarts;

	}

	@Override
	public int getFragmentLength() {
		return fragmentLength;
	}

	@Override
	public void setFragmentLength(int fragmentLength) {
		this.fragmentLength = fragmentLength;
	}

}
