package PamView.dialog;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

/**
 * Very simple panel to force a component to be at the north end 
 * of a panel. Used in ChannelListScroller. 
 * @author dg50
 *
 */
public class NorthPanel extends JPanel {
	NorthPanel(Component component) {
		super(new BorderLayout());
		add(component, BorderLayout.NORTH);
	}
}
