package targetMotionModule.panels;

import javax.swing.JPanel;

import targetMotionModule.TargetMotionLocaliser;
import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;

@SuppressWarnings("rawtypes")
public abstract class MapPanel<T extends PamDataUnit> implements TMDialogComponent {

	public abstract JPanel getPanel();

	protected TargetMotionLocaliser<T> targetMotionLocaliser;
	
	protected TargetMotionMainPanel<T> targetMotionDialog;
	

	/**
	 * @param targetMotionDialog
	 */
	public MapPanel(TargetMotionLocaliser<T> targetMotionLocaliser,
			TargetMotionMainPanel<T> targetMotionDialog) {
		super();
		this.targetMotionLocaliser = targetMotionLocaliser;
		this.targetMotionDialog = targetMotionDialog;
	}

	abstract public void notifyNewResults();

	abstract public void settings();

	abstract public void showMap(boolean b);
	
}
