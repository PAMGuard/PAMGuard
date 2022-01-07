package networkTransfer.receive.swing;


import java.awt.event.MouseEvent;

import PamguardMVC.PamDataUnit;

/*
 * Listener to get table actions out into more related modules.
 */
public interface RXTableMouseListener<T extends PamDataUnit> {

	public void popupMenuAction(MouseEvent e, T dataUnit, String colName);
	
}
