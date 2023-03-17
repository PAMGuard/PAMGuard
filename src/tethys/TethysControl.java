package tethys;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamView.PamTabPanel;
import PamguardMVC.PamDataBlock;
import metadata.MetaDataContol;
import metadata.deployment.DeploymentData;
import nilus.Deployment.Instrument;
import tethys.dbxml.DBXMLConnect;
import tethys.dbxml.DBXMLQueries;
//import nilus.Deployment;
//import nilus.Deployment.Instrument;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.output.TethysExporter;
import tethys.output.swing.TethysExportDialog;
import tethys.swing.TethysTabPanel;

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
	public static String xmlNameSpace = "http://tethys.sdsu.edu/schema/1.0";
	

	private TethysExportParams tethysExportParams = new TethysExportParams();
	
	private DBXMLConnect dbxmlConnect;
	
	private TethysTabPanel tethysTabPanel;
	
	private DBXMLQueries dbxmlQueries;
	
	private ArrayList<TethysStateObserver> stateObservers;

	public TethysControl(String unitName) {
		super(unitType, unitName);
		stateObservers = new ArrayList();
		dbxmlConnect = new DBXMLConnect(this);
		dbxmlQueries = new DBXMLQueries(this);
	}

	/**
	 * Get DBXML Connector. This class contains all the functions that are needed
	 * to talk to the database. 
	 * @return DBXML functions. 
	 */
	public DBXMLConnect getDbxmlConnect() {
		return dbxmlConnect;
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
		JMenuItem openClient = new JMenuItem("Open client in browser");
		openClient.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openTethysClient();
			}
		});
		tethysMenu.add(openClient);
		return tethysMenu;
	}

	protected void openTethysClient() {
		String urlString = tethysExportParams.getFullServerName() + "/Client";
		System.out.println("Opening url " + urlString);
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (url == null) {
			return;
		}
		try {
			Desktop.getDesktop().browse(url.toURI());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public PamTabPanel getTabPanel() {
		if (tethysTabPanel == null) {
			tethysTabPanel = new TethysTabPanel(this);
		}
		return tethysTabPanel;
	}

	/**
	 * @return the tethysExportParams
	 */
	public TethysExportParams getTethysExportParams() {
		return tethysExportParams;
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
	
	public DeploymentData getGlobalDeplopymentData() {
		PamControlledUnit aUnit = PamController.getInstance().findControlledUnit(MetaDataContol.class, null);
//		if (aUnit instanceof MetaDataContol == false || true) {
//			deployment.setProject("thisIsAProject");
//			deployment.setPlatform("Yay a platform");
//			Instrument instrument = new Instrument();
//			instrument.setType("machiney");
//			instrument.setInstrumentId("12345555");			
//			deployment.setInstrument(instrument);
//			return false;
//		}
		
		MetaDataContol metaControl = (MetaDataContol) aUnit;
		DeploymentData deploymentData = metaControl != null ? metaControl.getDeploymentData() : new DeploymentData();			
			
		deploymentData.setProject("thisIsAProject");
		deploymentData.setPlatform("Yay a platform");
		deploymentData.setCruise("cruisey");
		deploymentData.setDeploymentId(142536);
		deploymentData.setInstrumentId("super instrument");
		deploymentData.setSite("in the ocean somewhere");
		deploymentData.setRegion("ocean water");
		deploymentData.setInstrumentType("sensor of sorts");		
		
		return deploymentData;
	}

	/**
	 * Add a new state observer. 
	 * @param stateObserver
	 */
	public void addStateObserver(TethysStateObserver stateObserver) {
		stateObservers.add(stateObserver);
	}

	/**
	 * Remove a state observer. 
	 * @param stateObserver
	 * @return true if it existed. 
	 */
	public boolean removeStateObserver(TethysStateObserver stateObserver) {
		return stateObservers.remove(stateObserver);
	}
	
	/**
	 * Send state updates around to all state observers. 
	 * @param tethysState
	 */
	public void sendStateUpdate(TethysState tethysState) {
		for (TethysStateObserver stateObserver : this.stateObservers) {
			stateObserver.updateState(tethysState);
		}
	}
	/**
	 * A name for any deta selectors. 
	 * @return
	 */
	public String getDataSelectName() {
		return getUnitName();
	}

	public DBXMLQueries getDbxmlQueries() {
		return dbxmlQueries;
	}

}
