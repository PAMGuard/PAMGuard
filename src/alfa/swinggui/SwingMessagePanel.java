package alfa.swinggui;

import javax.swing.BoxLayout;
import javax.swing.JComponent;

import PamView.panel.PamPanel;
import alfa.comms.MessageProcess;
import rockBlock.RockBlockControl;
import rockBlock.swing.RockBlockMessageInTable;
import rockBlock.swing.RockBlockMessageOutTable;

public class SwingMessagePanel {

	private MessageProcess messageProcess;
	
	private RockBlockMessageOutTable rockBlockMessageTable;
	
	private RockBlockMessageInTable messageInTable;
	
	private PamPanel mainPanel;
	
	public static final int SHOW_OUTGOING = 0x1;
	public static final int SHOW_INCOMING = 0x2;
	public static final int SHOW_BOTH = 0x3;

	public SwingMessagePanel(MessageProcess messageProcess, int showWhat) {
		this.messageProcess = messageProcess;
		mainPanel = new PamPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		RockBlockControl rockBlockControl = messageProcess.findRockBlock();
		if (rockBlockControl != null) {
			rockBlockMessageTable = new RockBlockMessageOutTable(rockBlockControl);
			if ((showWhat & SHOW_OUTGOING) != 0) {
				mainPanel.add(rockBlockMessageTable.getComponent());
			}
			messageInTable = new RockBlockMessageInTable(rockBlockControl);
			if ((showWhat & SHOW_INCOMING) != 0) {
				mainPanel.add(messageInTable.getComponent());
			}
		}
	}

	public JComponent getComponent() {
		return mainPanel;
	}

}
