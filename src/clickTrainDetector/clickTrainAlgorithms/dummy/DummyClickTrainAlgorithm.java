package clickTrainDetector.clickTrainAlgorithms.dummy;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;
import clickTrainDetector.clickTrainAlgorithms.ClickTrainAlgorithm;
import clickTrainDetector.layout.CTDetectorGraphics;
import clickTrainDetector.layout.dummy.DummyCTGraphics;

/**
 * A do-nothing {@link ClickTrainAlgorithm}.
 * <p>
 * This algorithm never creates a click train. Its purpose is to provide a clean
 * way to remove existing click trains in viewer mode: the click train offline
 * task always deletes the old click trains before processing
 * ({@code ClickTrainOfflineProcess} forces {@code deleteOld = true}), so running
 * this algorithm over a section of data deletes every click train in that
 * section without producing any replacements. Select it and run with "Process
 * all data" to clear the entire dataset.
 *
 * @author Jamie Macaulay
 */
public class DummyClickTrainAlgorithm implements ClickTrainAlgorithm {

	public static final String DUMMY_NAME = "No detector (clear click trains)";

	@SuppressWarnings("unused")
	private final ClickTrainControl clickTrainControl;

	private DummyCTGraphics graphics;

	public DummyClickTrainAlgorithm(ClickTrainControl clickTrainControl) {
		this.clickTrainControl = clickTrainControl;
	}

	@Override
	public String getName() {
		return DUMMY_NAME;
	}

	@Override
	public void newDataUnit(PamDataUnit<?, ?> dataUnit) {
		// does nothing - no click trains are ever created.
	}

	@Override
	public void update(int flag, Object object) {
		// does nothing - no state to manage.
	}

	@Override
	public CTDetectorGraphics getClickTrainGraphics() {
		if (graphics == null) {
			graphics = new DummyCTGraphics();
		}
		return graphics;
	}

	@Override
	public CTAlgorithmInfoLogging getCTAlgorithmInfoLogging() {
		// this algorithm never logs any algorithm info as it creates no click trains.
		return null;
	}

}
