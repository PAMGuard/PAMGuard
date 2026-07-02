package networkTransfer.receive.status.base;

import java.util.Timer;
import java.util.TimerTask;

import PamController.PamControlledUnit;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import networkTransfer.receive.NetworkReceiver;
import networkTransfer.receive.status.BuoyStatusDataBlock;

public class NetReceiverStatusManager extends PamProcess {
	
	NetworkReceiver netReceiver;
	BuoyStatusDataBlock buoyStatusBlock;
	Timer statusTimer;
	PamDataBlock<NetReceiverStatusDataUnit> netRxStatusBlock;
	StatusReportTimerTask timerTask;

	public NetReceiverStatusManager(PamControlledUnit pamControlledUnit) {
		super(pamControlledUnit, null);
		this.processName = "NetRxStatus";
		netReceiver = (NetworkReceiver) pamControlledUnit;
		buoyStatusBlock = netReceiver.getBuoyStatusDataBlock();
		netRxStatusBlock = new PamDataBlock<NetReceiverStatusDataUnit>(NetReceiverStatusDataUnit.class, processName, this, 0);
		netRxStatusBlock.setJSONDataSource(new NetReceiveJsonDataSource());
		this.addOutputDataBlock(netRxStatusBlock);
		statusTimer = new Timer();
		timerTask = new StatusReportTimerTask();
		statusTimer.schedule(timerTask, 10000L, 1000L*30L);
	}

	@Override
	public String getObserverName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
	
	private class StatusReportTimerTask extends TimerTask{

		@Override
		public void run() {
			try {
				NetReceiverStatusDataUnit netRxStatus = new NetReceiverStatusDataUnit(PamCalendar.getTimeInMillis(),buoyStatusBlock);
				netRxStatusBlock.addPamData(netRxStatus);
			}catch(Exception e) {
				System.out.println("Error collecting base station status data. Error: "+e.getMessage());
			}
			
		}
		
	}

}
