package alfa.server;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import alfa.effortmonitor.IntervalDataUnit;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class ServerIntervalDataSelector extends DataSelector {

	private ServerIntervalDataBlock serverDataBlock;
	
	private SIDSParams sidsParams = new SIDSParams();

	public ServerIntervalDataSelector(ServerIntervalDataBlock serverDataBlock, String selectorName) {
		super(serverDataBlock, selectorName, false);
		this.serverDataBlock = serverDataBlock;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof SIDSParams) {
			this.sidsParams = (SIDSParams) dataSelectParams;
		}
	}

	@Override
	public SIDSParams getParams() {
		return sidsParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new SIDSDialogPanel(this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		if (sidsParams.showAll) {
			return 1;
		}
		IntervalDataUnit idu = (IntervalDataUnit) pamDataUnit;
		Long imei = idu.getImeiNumber();
		if (imei == null) {
			return 0;
		}
		return imei.equals(sidsParams.selectedSystemId) ? 1 : 0;
	}

	/**
	 * @return the serverDataBlock
	 */
	protected ServerIntervalDataBlock getServerDataBlock() {
		return serverDataBlock;
	}




}
