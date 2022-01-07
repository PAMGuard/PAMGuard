package pamViewFX.fxNodes.pamDialogFX;

import java.awt.Window;

import PamController.SettingsPane;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

// now does nothing - can remove from code and use PamdialogFX2AWT instead.
@Deprecated
public class PamSettingsDialogFX2AWT<T> extends PamDialogFX2AWT<T> {

	private static final long serialVersionUID = 1L;

	public PamSettingsDialogFX2AWT(Window owner, SettingsPane<T> settingsPane, boolean hasDefault) {
		super(owner, settingsPane, hasDefault);
	}

//	@Override
//	public Pane createContentPane() {
//		return (Pane) settingsPane.getContentNode();
//	}


}
