package rockBlock.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import PamUtils.PamCalendar;
import PamView.component.DataBlockTableView;
import PamView.panel.PamPanel;
import rockBlock.RockBlockControl;
import rockBlock.RockBlockOutgoingDataBlock;
import rockBlock.RockBlockOutgoingMessage;

public class RockBlockMessageOutTable {

	private PamPanel mainPanel;
	private MessageTable messageTable;
	
	public RockBlockMessageOutTable(RockBlockControl rockBlockControl) {
		messageTable = new MessageTable(rockBlockControl.getRockBlockProcess().getOutgoingMessages());
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, messageTable.getComponent());
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}

	private class MessageTable extends DataBlockTableView<RockBlockOutgoingMessage> {

		public MessageTable(RockBlockOutgoingDataBlock pamDataBlock) {
			super(pamDataBlock, "Outgoing Messages");
			// TODO Auto-generated constructor stub
		}
		
		private final String colNames[] = {"Time", "Message", "Status"}; 

		@Override
		public String[] getColumnNames() {
			return colNames;
		}

		@Override
		public Object getColumnData(RockBlockOutgoingMessage dataUnit, int column) {
			switch(column) {
			case 0:
				return PamCalendar.formatTodaysTime(dataUnit.getTimeMilliseconds(), true);
			case 1:
				return dataUnit.getMessage();
			case 2:
				return (dataUnit.isMessageSent() ? "Sent" : "Queued");
			}
			return null;
		}
		
	}
}
