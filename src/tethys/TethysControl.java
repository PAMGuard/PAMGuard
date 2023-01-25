package tethys;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import nilus.Deployment;
import nilus.Deployment.Instrument;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.output.swing.TethysExportDialog;

/**
 * Quick play with a simple system for outputting data to Tethys. At it's start
 * this is simply going to offer a dialog and have a few functions which show how 
 * to access data within PAMGuard. 
 * @author dg50
 *
 */
public class TethysControl extends PamControlledUnit {

	public static final String unitType = "Tethys Interface";
	public static String defaultName = "Tethys";
	

	private TethysExportParams tethysExportParams = new TethysExportParams();
	

	public TethysControl(String unitName) {
		super(unitType, unitName);
	}

	@Override
	public JMenuItem createFileMenu(JFrame parentFrame) {
		JMenu tethysMenu = new JMenu("Tethys");
		JMenuItem tethysExport = new JMenuItem("Export ...");
		tethysMenu.add(tethysExport);
		tethysExport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tethysExport(parentFrame);
			}
		});
		return tethysMenu;
	}

	/**
	 * @return the tethysExportParams
	 */
	public TethysExportParams getTethysExportParams() {
		return tethysExportParams;
	}
	
	/**
	 * Get a nilus deployment document, as full populated as possible. 
	 * @return
	 */
	public Deployment getDeployment() {
		Deployment deployment = new Deployment();
		Deployment.Instrument instrument = new Instrument();
//		instrument.
//		deployment.
		
		return null;
	}

	/**
	 * We'll probably want to 
	 * @param parentFrame
	 */
	protected void tethysExport(JFrame parentFrame) {
		TethysExportParams newExportParams = TethysExportDialog.showDialog(parentFrame, this);
		if (newExportParams != null) {
			// dialog returns null if cancel was pressed. 
			tethysExportParams = newExportParams;
			exportTethysData(tethysExportParams);
		}
	}

	/**
	 * We'll arrive here if the dialog has been opened and we want to export Tethys data. 
	 * @param tethysExportParams2
	 */
	private void exportTethysData(TethysExportParams tethysExportParams) {
		TethysExporter tethysExporter = new TethysExporter(this, tethysExportParams);
		tethysExporter.doExport();		
	}

}
