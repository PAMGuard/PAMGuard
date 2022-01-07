package PamUtils.time.nmea;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JPanel;

import GPS.GpsDataUnit;
import NMEA.NMEADataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;

public class NMEATimeDialog extends PamDialog {
	
	private SourcePanel sourcePanel;
	
	private static NMEATimeDialog singleInstance;
	
	private NMEATimeParameters nmeaTimeParameters;

	private NMEATimeDialog(Window parentFrame) {
		super(parentFrame, "NMEA Time options", false);
		JPanel mainPanel = new JPanel(new BorderLayout());
		sourcePanel = new SourcePanel(parentFrame, "NMEA Data Source", NMEADataUnit.class, false, false);
		mainPanel.add(BorderLayout.CENTER, sourcePanel.getPanel());
		setDialogComponent(mainPanel);
	}
	
	public static NMEATimeParameters showDialog(Window parentFrame, NMEATimeParameters nmeaTimeParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new NMEATimeDialog(parentFrame);
		}
		singleInstance.setParams(nmeaTimeParameters);
		singleInstance.setVisible(true);
		return singleInstance.nmeaTimeParameters;
	}

	private void setParams(NMEATimeParameters nmeaTimeParameters) {
		this.nmeaTimeParameters = nmeaTimeParameters.clone();
		sourcePanel.setSource(nmeaTimeParameters.nmeaSource);
	}

	@Override
	public boolean getParams() {
		nmeaTimeParameters.nmeaSource = sourcePanel.getSourceName();
		if (nmeaTimeParameters.nmeaSource == null) {
			return showWarning("You must select a GPS data source");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		nmeaTimeParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
