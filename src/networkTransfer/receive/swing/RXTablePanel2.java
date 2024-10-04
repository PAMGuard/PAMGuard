package networkTransfer.receive.swing;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import GPS.GpsData;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.component.DataBlockTableView;
import PamguardMVC.PamDataBlock;
import networkTransfer.receive.BuoyStatusDataUnit;
import networkTransfer.receive.BuoyStatusValue;
import networkTransfer.receive.NetworkReceiver;
import networkTransfer.receive.PairedValueInfo;

public class RXTablePanel2 extends DataBlockTableView<BuoyStatusDataUnit>{

	private NetworkReceiver networkReceiver;

	public RXTablePanel2(NetworkReceiver networkReceiver) {
		super(networkReceiver.getBuoyStatusDataBlock(), networkReceiver.getUnitName());
		this.networkReceiver = networkReceiver;
	}

	@Override
	public String[] getColumnNames() {
		String[] names = Arrays.copyOf(colNames1, getColumnCount());
		int ic = colNames1.length;
		if (networkReceiver != null) {
			ArrayList<PairedValueInfo> extraInfo = networkReceiver.getExtraTableInfo();
			for (PairedValueInfo pi:extraInfo) {
				names[ic++] = pi.getColumnName();
			}
		}
		for (int i = 0; i < colNames2.length; i++) {
			names[ic++] = colNames2[i];
		}
		if (networkReceiver != null) {
			ArrayList<PamDataBlock> rxBlocks = networkReceiver.getRxDataBlocks();
			if (rxBlocks != null) {
				for (PamDataBlock rxb : rxBlocks) {
					names[ic++] = rxb.getDataName();
				}
			}
		}
		return names;
	}

	public int getColumnCount() {

		int n = colNames1.length + colNames2.length;
		if (networkReceiver == null) {
			return n;
		}
		//		genericDataList = rxStats.getBuoyGenericDataList();
		n += networkReceiver.getExtraTableInfo().size();

		if (networkReceiver.getRxDataBlocks() != null) {
			n += networkReceiver.getRxDataBlocks().size();
		}
		return n;
	}

	private static String[] colNames1 = {"Station Id","IP Addr", "Channel", "Status"};
	private static String[] colNames2 = {"Last Data", "Position", "Tot' Packets"};

	@Override
	public Object getColumnData(BuoyStatusDataUnit b, int column) {
		long t;
	
		Integer col = getCols1Index(column);
		if (col != null){
			switch(col) {
			case 0:
				return String.format("%d(%d)", b.getBuoyId1(), b.getBuoyId2());
			case 1:
				return b.getIPAddr();
			case 2:
				return b.getLowestChannel();
			case 3:
				boolean conState = b.getSocket() != null;
				if (conState) {
					return String.format("Connected : %s", NetworkReceiver.getPamCommandString(b.getCommandStatus()));
				}
				else {
					return "Disconnected";
				}
			}
		}
		col = getCols2Index(column);
		if (col != null) {
			switch (col) {
			case 0:
				t = b.getLastDataTime();
				if (t == 0) {
					return "no data";
				}
				else {
					return PamCalendar.formatDateTime2(t);
				}
			case 1:
				return b.getPositionString();
			case 2:
				int unk = b.getUnknownPackets();
				int tot = b.getTotalPackets();
				if (unk == 0) {
					return tot;
				}
				else {
					return String.format("%d(+%d unk)", tot, unk);
				}
			}
		}
		col = getExtraInfoIndex(column);
		if (col != null) {
			PairedValueInfo pairInfo = networkReceiver.getExtraTableInfo().get(col);
			 BuoyStatusValue data = b.getPairVal(pairInfo.getPairName());
			return pairInfo.formatTableData(b, data);
		}
	
		col = getDatablockIndex(column);
		if (col != null) {
			PamDataBlock dataBlock = dataBlockforColumn(column);
			if (dataBlock != null) {
				return b.getBlockPacketCount(dataBlock);
			}
		}
		return null;
	}

	@Override
	public String getToolTipText(BuoyStatusDataUnit b, int column) {
		Integer col = getCols1Index(column);
		if (col != null){
			switch(col) {
			case 0:
				return "Station Id";
			case 1:
				return "Network address";
			case 2:
				return "First acoustic channel";
			case 3:
				return "Comms status";
			}
		}
		col = getCols2Index(column);
		if (col != null) {
			switch (col) {
			case 0:
				return "Time last data arrived";
			case 1:
				String str =  "Sender location";
				if (b != null) {
					GpsData gpsData = b.getGpsData();
					if (gpsData != null) {
						long gpsTime = gpsData.getTimeInMillis();
						str = String.format("<html>%s<br>Last update %s<br>(%d Seconds ago)</html>", str, 
								PamCalendar.formatDBDateTime(gpsTime), (PamCalendar.getTimeInMillis()-gpsTime)/1000);
					}
				}
				return str;
			case 2:
				return "Total packets received";
			}
		}
		col = getExtraInfoIndex(column);
		if (col != null) {
			PairedValueInfo pairInfo = networkReceiver.getExtraTableInfo().get(col);
			if (pairInfo == null) {
				return null;
			}
			if (b == null) {
				return pairInfo.getPairName();
			}
			BuoyStatusValue data = b.getPairVal(pairInfo.getPairName());
			String tip = pairInfo.getToolTipText(b, data);
			if (data != null) {
				return String.format("<html>%s<br>last updated at %s</html>", tip, PamCalendar.formatDateTime(data.getTimemillis()));
			}
			else {
				return String.format("%s unavailable", pairInfo.getColumnName());
			}
		}

		col = getDatablockIndex(column);
		if (col != null) {
			if (b == null) {
				return null;
			}
			PamDataBlock dataBlock = dataBlockforColumn(column);
			if (dataBlock != null) {
				int nUnits = b.getBlockPacketCount(dataBlock);
				int rxbytes = b.getBlockPacketRXbytes(dataBlock);
				if (nUnits > 0) {
					return String.format("Received data units, average size %d bytes", rxbytes/nUnits);
				}
			}
			return "block packets received";
		}
		return null;
	}

	public String getColumnName(int iCol) {
		Integer c = getCols1Index(iCol);
		if (c != null) {
			return colNames1[c];
		}
		c = getExtraInfoIndex(iCol);
		if (c != null) {
			return networkReceiver.getExtraTableInfo().get(c).getPairName();
		}
		c = getCols2Index(iCol);
		if (c != null) {
			return colNames2[c];
		}
		c = getDatablockIndex(iCol);
		if (c != null) {
			return networkReceiver.getRxDataBlocks().get(c).getDataName();
		}
		return null;
	}
	
	private PamDataBlock dataBlockforColumn(int iCol) {
		 Integer c = getDatablockIndex(iCol);
		if (c == null) {
			return null;
		}
		ArrayList<PamDataBlock> rxBlocks = networkReceiver.getRxDataBlocks();
		if (c < 0 || c >= rxBlocks.size()) {
			return null;
		}
		return rxBlocks.get(c);
	}
	
	

	private Integer getCols1Index(int column) {
		return (column < colNames1.length ? column : null); 
	}

	private Integer getExtraInfoIndex(int column) {
		if (networkReceiver == null) {
			return null;
		}
		column -= (colNames1.length);
		return (column >= 0 & column < networkReceiver.getExtraTableInfo().size() ? column : null);
	}

	private Integer getCols2Index(int column) {
		if (networkReceiver == null) {
			return null;
		}
		column -= (colNames1.length + networkReceiver.getExtraTableInfo().size());
		return (column >= 0 & column < colNames2.length ? column : null);
	}

	private Integer getDatablockIndex(int column) {
		if (networkReceiver == null || networkReceiver.getRxDataBlocks() == null) {
			return null;
		}
		column -= (colNames1.length + networkReceiver.getExtraTableInfo().size() + colNames2.length);
		return (column >= 0 & column < networkReceiver.getRxDataBlocks().size() ? column : null);
	}

	public void notifyModelChanged(int changeType) {
		fireTableStructureChanged();

	}

	/**
	 * @param setPairData
	 */
	public void configurationChange() {
		fireTableStructureChanged();
	}

	@Override
	public void popupMenuAction(MouseEvent e, BuoyStatusDataUnit dataUnit, String colName) {
		if (networkReceiver.getTableMouseListener() != null) {
			networkReceiver.getTableMouseListener().popupMenuAction(e, dataUnit, colName);
		}
	}


}
