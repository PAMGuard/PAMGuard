package clickTrainDetector.layout.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import pamViewFX.PamSidePaneFX;

/**
 * Basic side pane which shows the MHT metrics. 
 * The MHT Side pane shows 
 * <p>
 * Number of click trains detected in last ten minutes
 * <p>
 * The currenty state of the MHTKernel Buffer i.e. how full it is
 * @author Jamie Macaulay 
 *
 */
public class MHTSidePane implements PamSidePaneFX {

	public MHTSidePane(MHTClickTrainAlgorithm mhtClickTrainAlgoirthm) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return new BorderPane(new Label("Hello"));
	}

}
