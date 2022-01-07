package alfa.status;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.status.ModuleStatus;
import PamController.status.RemedialAction;
import PamModel.PamModuleInfo;
import PamView.PamGui;
import alfa.ControlledModuleInfo;

public class CreateModuleAction implements RemedialAction {

	private ControlledModuleInfo moduleInfo;

	public CreateModuleAction(ControlledModuleInfo moduleInfo) {
		this.moduleInfo = moduleInfo;
	}

	@Override
	public String getInfo() {
		String info = "Create module " + moduleInfo.getDefaultName();
		return info;
	}

	@Override
	public ModuleStatus takeAction(ModuleStatus currentStatus) {
		PamController pamController = PamController.getInstance();
		PamControlledUnit newUnit = pamController.addModule(moduleInfo.getPamModuleInfo(), moduleInfo.getDefaultName());
		if (newUnit == null) {
			return null;
		}
		else {
			return newUnit.getModuleStatus();
		}
	}

}
