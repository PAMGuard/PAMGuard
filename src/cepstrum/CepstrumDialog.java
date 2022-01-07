package cepstrum;

import java.awt.Window;

import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataUnit;

public class CepstrumDialog extends PamDialog {

	private SourcePanel sourcePanel;
	
	private static CepstrumDialog singleInstance;
	
	private CepstrumParams cepstrumParams;
	
	private CepstrumDialog(Window parentFrame) {
		super(parentFrame, "Cepstrum Settings", false);
		sourcePanel = new SourcePanel(this, "FFT Data Source", FFTDataUnit.class, true, true);
		setDialogComponent(sourcePanel.getPanel());
	}
	
	public static CepstrumParams showDialog(Window parentWindow, CepstrumParams oldParams) {
		if (singleInstance == null || singleInstance.getOwner() != parentWindow) {
			singleInstance = new CepstrumDialog(parentWindow);
		}
		singleInstance.setParams(oldParams);
		singleInstance.setVisible(true);
		return singleInstance.cepstrumParams;
	}

	private void setParams(CepstrumParams oldParams) {
		this.cepstrumParams = oldParams.clone();
		sourcePanel.setSource(cepstrumParams.sourceDataBlock);
		sourcePanel.setChannelList(cepstrumParams.channelMap);
		
	}

	@Override
	public boolean getParams() {
		PamDataBlock sourceBlock = sourcePanel.getSource();
		if (sourceBlock == null) {
			return showWarning("You must select a data source");
		}
		cepstrumParams.sourceDataBlock = sourceBlock.getLongDataName();
		cepstrumParams.channelMap = sourcePanel.getChannelList();
		if (cepstrumParams.channelMap == 0) {
			return showWarning("You must select at least one data channel");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		cepstrumParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
