package loc3d_Thode;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;


public class TowedArray3DController extends PamControlledUnit implements PamSettings{

	TowedArray3DProcess towedArray3DProcess; 
	
	TowedArray3DProcessParameters towedArray3DProcessParameters;
	
	private boolean initialisationComplete = false;
	
//	private TowedArray3DPluginPanelProvider findAnchorPluginPanelProvider;
	
	/**
	 * Must have a default constructor that takes a single String as an argument. 
	 * @param unitName Instance specific name to give this module. 
	 */
	public TowedArray3DController(String unitName) {
		super("Multipath", unitName);

		/*
		 * create the parameters that will control the process. 
		 * (do this before creating the process in case the process
		 * tries to access them from it's constructor). 
		 */ 
		towedArray3DProcessParameters = new TowedArray3DProcessParameters();
		
		/*
		 * make a TowedArray3DProcess - which will actually do the detecting
		 * for us. Although the super class PamControlledUnit keeps a list
		 * of processes in this module, it's also useful to keep a local 
		 * reference.
		 */
		addPamProcess(towedArray3DProcess = new TowedArray3DProcess(this));
		
		/*
		 * provide plug in panels for the bottom of the spectrogram displays
		 * (and any future displays that support plug in panels)
		 */
//		findAnchorPluginPanelProvider = new TowedArray3DPluginPanelProvider(this);
		
		/*
		 * Tell teh PAmguard settings manager that we have settings we wish to
		 * be saved between runs. IF settings already exist, the restoreSettings()
		 * function will get called back from here with the most recent settings. 
		 */
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		
		/*
		 * This gets called every time a new module is added - make sure
		 * that the TowedArray3DProcess get's a chance to look around and see
		 * if there is data it wants to subscribe to. 
		 */
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
		case PamControllerInterface.ADD_DATABLOCK:
		case PamControllerInterface.REMOVE_DATABLOCK:
			if (initialisationComplete) {
				towedArray3DProcess.prepareProcess();
			}
		}
	}

	/**
	 * This next function sets up a menu which wil be added to the main Display menu
	 */
	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Map symbol");
		menuItem.addActionListener(new MapSymbolSelect());
		return menuItem;
	}
	
	/*
	 * Menu actionlistener, using a standard Pamgaurd symbol dialog to 
	 * select the map symbol shape and colour. 
	 */
	class MapSymbolSelect implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

//			PamSymbol newSymbol = PamSymbolDialog.show(towedArray3DProcessParameters.mapSymbol);
//			if (newSymbol != null) {
//				towedArray3DProcessParameters.mapSymbol = newSymbol;
//			}
			
		}
		
	}
	

	/*
	 * Menu item and action for detection parameters...
	 *  (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Parameters");
		menuItem.addActionListener(new SetParameters(parentFrame, this));
		return menuItem;
	}
	
	class SetParameters implements ActionListener {

		Frame parentFrame;
		
		TowedArray3DController towedArray3DController;
		
		public SetParameters(Frame parentFrame, TowedArray3DController towedArray3DController) {
			this.parentFrame = parentFrame;
			this.towedArray3DController = towedArray3DController;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			TowedArray3DProcessParameters newParams = TowedArray3DParametersDialog.showDialog(parentFrame, towedArray3DController,
					towedArray3DProcessParameters);
			/*
			 * The dialog returns null if the cancel button was set. If it's 
			 * not null, then clone the parameters onto the main parameters reference
			 * and call preparePRocess to make sure they get used !
			 */
			if (newParams != null) {
				towedArray3DProcessParameters = newParams.clone();
				towedArray3DProcess.prepareProcess();
			}
			
		}
		
	}

	/**
	 * These next three functions are needed for the PamSettings interface
	 * which will enable Pamguard to save settings between runs
	 */
	@Override
	public Serializable getSettingsReference() {
		return towedArray3DProcessParameters;
	}

	@Override
	public long getSettingsVersion() {
		return TowedArray3DProcessParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		towedArray3DProcessParameters = (TowedArray3DProcessParameters) pamControlledUnitSettings.getSettings();
		return true;
	}
	

}
