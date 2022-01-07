package targetMotionOld.dialog;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import Localiser.ModelControlPanel;
import targetMotionOld.TargetMotionLocaliser;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 */
public class TMModelControlPanel extends ModelControlPanel implements TMDialogComponent {

	private TargetMotionDialog targetMotionDialog;
	
	private TargetMotionLocaliser targetMotionLocaliser;

	/**
	 * @param targetMotionLocaliser
	 * @param targetMotionDialog
	 */
	public TMModelControlPanel(TargetMotionLocaliser targetMotionLocaliser,
			TargetMotionDialog targetMotionDialog) {
		super(targetMotionLocaliser.getModels());
		super.getPanel().setBorder(new TitledBorder("Model Control"));
		this.targetMotionLocaliser = targetMotionLocaliser;
		this.targetMotionDialog = targetMotionDialog;
	}
	
	
	@Override
	public void modelEnable(){
		targetMotionDialog.enableControls();
	}
	
	@Override
	public void setCurrentEventIndex(int eventIndex, Object sender) {
		if (sender == this) return;
	}
	/**
	 * @return the mainPanel
	 */
	public JPanel getPanel() {
		return super.getPanel();
	}
	
	@Override
	public boolean canRun() {
		for (int i = 0; i < this.getNModels(); i++) {
			if (super.isEnabled(i)) {
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void enableControls() {
		super.enableControls();
	}
	
	
}
