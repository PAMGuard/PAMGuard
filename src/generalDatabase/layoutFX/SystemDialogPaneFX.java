package generalDatabase.layoutFX;

import javafx.scene.layout.Pane;

/**
 * Different db systems can proide a system specific dialog panel
 * by implementing this interface
 * @author Doug
 *
 */
public interface SystemDialogPaneFX {

	Pane getPane();

	boolean getParams();

	void setParams();
}

