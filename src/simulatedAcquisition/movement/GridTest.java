package simulatedAcquisition.movement;

public class GridTest {

	public static void main(String[] args) {
		GridMovement mM = new GridMovement(null);
		mM.start(0,  null);
		while (mM.takeStep(0, null));
	}

}
