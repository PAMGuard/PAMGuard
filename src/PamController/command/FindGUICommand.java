package PamController.command;

import PamController.PamController;
import PamView.GuiFrameManager;

public class FindGUICommand extends ExtCommand {

	public FindGUICommand() {
		super("findgui", true);
	}

	@Override
	public String execute(String command) {
		GuiFrameManager frameManager = PamController.getInstance().getGuiFrameManager();
		if (frameManager == null) {
			return "No GUI to move";
		}
		frameManager.findGUI();
		return "GUI Moved";
	}

	@Override
	public String getHint() {
		return "Move GUI components to the main monitor";
	}

}
