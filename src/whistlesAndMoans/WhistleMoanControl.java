package whistlesAndMoans;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dataPlots.data.TDDataProviderRegister;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.whistlePlotFX.WhistleMoanProviderFX;
import detectionPlotFX.data.DDPlotRegister;
import detectionPlotFX.rawDDPlot.ClickDDPlotProvider;
import detectionPlotFX.whistleDDPlot.WhistleDDPlotProvider;
import spectrogramNoiseReduction.SpectrogramNoiseProcess;
import whistlesAndMoans.layoutFX.WhistleMoanGUIFX;
import whistlesAndMoans.plots.WhistlePlotProvider;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamSidePanel;
import PamView.WrapperControlledGUISwing;

public class WhistleMoanControl extends PamControlledUnit implements PamSettings {

	private WhistleToneConnectProcess whistleToneProcess;

	protected WhistleToneParameters whistleToneParameters = new WhistleToneParameters();

	private SpectrogramNoiseProcess spectrogramNoiseProcess;

	/**
	 * The JavaFX GUI for the whistle and moan detector. 
	 */
	private WhistleMoanGUIFX whistleMoanGUIFX;
	
	/**
	 * Plot provider for whislte detections
	 */
	private WhistleDDPlotProvider whistleDDPlotProvider;

	/**
	 * The swing GUI. 
	 */
	private WrapperControlledGUISwing binaryStoreGUISwing;

	public static final String UNITTYPE = "WhistlesMoans";

	public WhistleMoanControl(String unitName) {
		super(UNITTYPE, unitName);

		spectrogramNoiseProcess = new SpectrogramNoiseProcess(this);
		addPamProcess(spectrogramNoiseProcess);

		whistleToneProcess = new WhistleToneConnectProcess(this);
		addPamProcess(whistleToneProcess);

		PamSettingManager.getInstance().registerSettings(this);

		//		UserDisplayControl.addUserDisplayProvider(new WhistleBearingPlotProvider(this));

		TDDataProviderRegister.getInstance().registerDataInfo(new WhistlePlotProvider(this));
		TDDataProviderRegisterFX.getInstance().registerDataInfo(new WhistleMoanProviderFX(this));
		//register the DD display
		DDPlotRegister.getInstance().registerDataInfo(whistleDDPlotProvider  = new WhistleDDPlotProvider(this));
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			whistleToneProcess.setupProcess();
		}
		if (whistleMoanGUIFX!=null) whistleMoanGUIFX.notifyGUIChange(changeType);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName());
		menuItem.addActionListener(new DetectionSettings(parentFrame));
		return menuItem;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDisplayMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName());
		menuItem.addActionListener(new DisplaySettings(parentFrame));
		return menuItem;
	}

	class DetectionSettings implements ActionListener {

		private Frame parentFrame;

		public DetectionSettings(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			settingsDialog(parentFrame);	
		}

	}
	class DisplaySettings implements ActionListener {

		private Frame parentFrame;

		public DisplaySettings(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			displayDialog(parentFrame);	
		}

	}

	private void settingsDialog(Frame parentFrame) {
		WhistleToneParameters newSettings = WhistleToneDialog.showDialog(parentFrame, 
				this);
		if (newSettings != null) {
			whistleToneParameters = newSettings.clone();
			whistleToneProcess.setupProcess();
		}
	}

	private void displayDialog(Frame parentFrame) {
		WhistleToneParameters newSettings = WMDisplayDialog.showDialog(this, parentFrame);
		if (newSettings != null) {
			whistleToneParameters = newSettings.clone();
		}
	}

	@Override
	public PamSidePanel getSidePanel() {
		return whistleToneProcess.dataCounter.getSidePanel();
	}

	/**
	 * @return the spectrogramNoiseProcess
	 */
	public SpectrogramNoiseProcess getSpectrogramNoiseProcess() {
		return spectrogramNoiseProcess;
	}

	/**
	 * @return the whistleToneProcess
	 */
	public WhistleToneConnectProcess getWhistleToneProcess() {
		return whistleToneProcess;
	}


	@Override
	public Serializable getSettingsReference() {
		return whistleToneParameters;
	}

	@Override
	public long getSettingsVersion() {
		return WhistleToneParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		whistleToneParameters = ((WhistleToneParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public String getModuleSummary() {
		return whistleToneProcess.getModuleSummary();
	}

	@Override
	public Object getShortUnitType() {
		return "WMD";
	}

	public WhistleToneParameters getWhistleToneParameters() {
		return whistleToneParameters;
	}

	/**
	 * Set the whistle and tone params. 
	 */
	public void setWhistleMoanControl(WhistleToneParameters newParams) {
		this.whistleToneParameters=newParams;
	}

	/**
	 * Get the GUI for the PAMControlled unit. This has multiple GUI options 
	 * which are instantiated depending on the view type. 
	 * @param flag. The GUI type flag defined in PAMGuiManager. 
	 * @return the GUI for the PamControlledUnit unit. 
	 */
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (whistleMoanGUIFX ==null) {
				whistleMoanGUIFX= new WhistleMoanGUIFX(this);
			}
			return whistleMoanGUIFX;
		}
		if (flag==PamGUIManager.SWING) {
			if (binaryStoreGUISwing ==null) {
				binaryStoreGUISwing= new WrapperControlledGUISwing(this);	
			}
			return binaryStoreGUISwing;
		}
		return null;
	}
}
