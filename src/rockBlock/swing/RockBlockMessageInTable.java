package rockBlock.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import PamUtils.PamCalendar;
import PamView.component.DataBlockTableView;
import PamView.panel.PamPanel;
import rockBlock.RockBlockControl;
import rockBlock.RockBlockDataBlock;
import rockBlock.RockBlockIncomingMessage;

public class RockBlockMessageInTable {

	private MessageTable messageTable;
	private PamPanel mainPanel;

	public RockBlockMessageInTable(RockBlockControl rockBlockControl) {
		messageTable = new MessageTable(rockBlockControl.getRockBlockProcess().getIncomingMessages());
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, messageTable.getComponent());
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

	private class MessageTable extends DataBlockTableView<RockBlockIncomingMessage> {

		public MessageTable(RockBlockDataBlock<RockBlockIncomingMessage> pamDataBlock) {
			super(pamDataBlock, "Incoming Messages");
		}
		
		private final String colNames[] = {"Time", "Message"}; 

		@Override
		public String[] getColumnNames() {
			return colNames;
		}

		@Override
		public Object getColumnData(RockBlockIncomingMessage dataUnit, int column) {
			switch(column) {
			case 0:
				return PamCalendar.formatTodaysTime(dataUnit.getTimeMilliseconds(), true);
			case 1:
				return dataUnit.getMessage();
			}
			return null;
		}
		
	}

}
