package clickTrainDetector.layout.mht;

import java.awt.Component;

import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTParams;
import clickTrainDetector.layout.CTDetectorGraphics;
import javafx.scene.layout.Pane;

/**
 * Graphics for the click train detector. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MHTGraphics implements CTDetectorGraphics {

	/**
	 * Reference to the click train algorithm. 
	 */
	private MHTClickTrainAlgorithm mhtClickTrainAlgoirthm;

	/**
	 * The MHT settings pane. 
	 */
	private MHTSettingsPane mhtSettingsPane; 

	/**
	 * The MHT Side pane. 
	 */
	private MHTSidePane mhtSidePane;

	/**
	 * The swing side panel. 
	 */
	private MHTSidePaneSwing sidePaneSwing; 


	public MHTGraphics(MHTClickTrainAlgorithm mhtClickTrainAlgoirthm){
		this.mhtClickTrainAlgoirthm=mhtClickTrainAlgoirthm;  
	}


	@Override
	public Pane getCTSettingsPane() {
		if (mhtSettingsPane==null) {
			mhtSettingsPane = new MHTSettingsPane(mhtClickTrainAlgoirthm);
		}
		mhtSettingsPane.setParams(mhtClickTrainAlgoirthm.getParams());
		return (Pane) mhtSettingsPane.getContentNode();
	}


	@Override
	public boolean getParams() {
		MHTParams mhtParams = mhtSettingsPane.getParams(mhtClickTrainAlgoirthm.getParams()); 
		if (mhtParams==null) {
			System.err.print("MHT Algorithm returned null params");
			return false;
		}
		else {
			mhtClickTrainAlgoirthm.setParams(mhtParams); 
			//			System.out.println("N pruneback: " + mhtParams.mhtKernal.nPruneback);
			return true;
		}

	}

	@Override
	public Pane getCTSidePane() {
		if (mhtSidePane==null) {
			mhtSidePane = new MHTSidePane(mhtClickTrainAlgoirthm);
		} 
		return (Pane) mhtSidePane.getNode();
	}


	@Override
	public Component getCTSidePaneSwing() {
		if (sidePaneSwing==null) {
			this.sidePaneSwing = new MHTSidePaneSwing(mhtClickTrainAlgoirthm); 
		}
		return sidePaneSwing.getComponent();
	}

	/**
	 * Update the side panel. 
	 * @param kCount - the kcount
	 */
	public void updateGraphics() {
		if (sidePaneSwing!=null) sidePaneSwing.updateBuffer(); 
	}


	@Override
	public void notifyUpdate(int flag, Object data) {
		switch (flag) {
		case ClickTrainControl.NEW_PARAMS:
			if (sidePaneSwing!=null) sidePaneSwing.updateNewParams();
			break; 
		case ClickTrainControl.NEW_PARENT_DATABLOCK:
			//pass along to the MHTSettings pane. 
			this.mhtSettingsPane.notifyChange(ClickTrainControl.NEW_PARENT_DATABLOCK, data);
			break; 
		}

	}

}
