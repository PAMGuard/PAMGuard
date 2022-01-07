package targetMotionOld.dialog;

import javax.swing.JPanel;

import targetMotionOld.TargetMotionLocaliser;
import Localiser.detectionGroupLocaliser.GroupDetection;
import PamDetection.PamDetection;

public abstract class DialogMap<T extends GroupDetection> implements TMDialogComponent {

	public abstract JPanel getPanel();

	protected TargetMotionLocaliser<T> targetMotionLocaliser;
	
	protected TargetMotionDialog<T> targetMotionDialog;

	/**
	 * @param targetMotionDialog
	 */
	public DialogMap(TargetMotionLocaliser<T> targetMotionLocaliser,
			TargetMotionDialog<T> targetMotionDialog) {
		super();
		this.targetMotionLocaliser = targetMotionLocaliser;
		this.targetMotionDialog = targetMotionDialog;
	}

	abstract public void notifyNewResults();

	abstract public void settings();

	abstract public void showMap(boolean b);
	
}
