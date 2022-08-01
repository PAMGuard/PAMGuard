package PamView;

import javax.swing.JFrame;

import PamController.PAMControllerGUI;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamSettings;
import PamModel.PamModel;
import PamModel.PamModuleInfo;
import pamViewFX.pamTask.PamTaskUpdate;

/**
 * Null GUI controller which will get used with the -nogui options. 
 * @author dg50
 *
 */
public class NullGuiController implements PAMControllerGUI {

	private PamController pamController;

	public NullGuiController(PamController pamController) {
		this.pamController = pamController;
	}

	@Override
	public void pamStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamEnded() {
		// TODO Auto-generated method stub
		

	}

	@Override
	public void modelChanged(int changeType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addControlledUnit(PamControlledUnit controlledUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showControlledUnit(PamControlledUnit unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeControlledUnit(PamControlledUnit controlledUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFrameNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public JFrame getGuiFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableGUIControl(boolean enable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addView(PamViewInterface newView) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroyModel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyLoadProgress(PamTaskUpdate progress) {
		// TODO Auto-generated method stub

	}

	@Override
	public PamSettings getInitialSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public PamViewInterface initPrimaryView(PamController pamController, PamModel pamModelInterface) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCallBack() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getModuleName(Object parentFrame, PamModuleInfo moduleInfo) {
		// TODO Auto-generated method stub
		return null;
	}

}
