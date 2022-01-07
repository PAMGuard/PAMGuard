package clickTrainDetector.layout.mht;

import PamController.SettingsPane;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm;
/**
 * The settings pane for a MHT chi2 variable 
 * @author jamie
 *
 * @param <T>
 */
public abstract class MHTVarSettingsPane<T> extends SettingsPane<T> {
	
	/**
	 * Reference to the click train algorithm 
	 */
	private MHTClickTrainAlgorithm mhtAlgorithm;  

	public MHTVarSettingsPane(Object ownerWindow) {
		super(ownerWindow);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the mhtAlgorithm
	 */
	public MHTClickTrainAlgorithm getMhtAlgorithm() {
		return mhtAlgorithm;
	}

	/**
	 * @param mhtAlgorithm the mhtAlgorithm to set
	 */
	public void setMhtAlgorithm(MHTClickTrainAlgorithm mhtAlgorithm) {
		this.mhtAlgorithm = mhtAlgorithm;
	}


}
