/**
 * 
 */
package IshmaelDetector;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import IshmaelDetector.dataPlotFX.IshmaelDetPlotProvider;
import IshmaelDetector.dataPlotFX.IshmaelFnPlotProvider;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamUtils;
import PamView.dialog.GroupedSourcePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import dataPlotsFX.data.TDDataProviderRegisterFX;


/**
 * Control for the Ishmael Detector modules.
 * 
 * @author Hisham Qayum and Dave Mellinger
 */
@SuppressWarnings("rawtypes")
public abstract class IshDetControl extends PamControlledUnit implements PamSettings {
	
	IshDetFnProcess ishDetFnProcess;
	IshDetParams ishDetParams;
	IshPeakProcess ishPeakProcess;
	IshDetGraphics ishDetGraphics;
	IshDetSave ishDetSave;

	/** Initializer. 
	 * 
	 * <p>IMPORTANT: The subclass initializer should construct the ishDetParams
	 * to pass here.  See EnergySumControl for an example.
	 */ 
	public IshDetControl(String controlName, String unitName, IshDetParams ishDetParams)
	{
		super(controlName, unitName);
		this.ishDetParams = ishDetParams;

		//Detection function.
		PamDataBlock defaultInputDataBlock = getDefaultInputDataBlock();
//		ishDetParams.inputDataSource = defaultInputDataBlock.getDataName();	not needed, gets set when the settings are restored

		PamSettingManager.getInstance().registerSettings(this);
		
		//Call subclass to provide appropriate instance of detection process.
		ishDetFnProcess = getNewDetProcess(defaultInputDataBlock);
		
		addPamProcess(ishDetFnProcess); //make it show up in the Data Model window
		ishDetFnProcess.setParentDataBlock(defaultInputDataBlock);

		//Peak picker.
		//PamDataBlock detfnDataBlock = ishDetProcess.outputDataBlock;
		ishPeakProcess = new IshPeakProcess(this, getOutputDataBlock());
		addPamProcess(ishPeakProcess);
		ishPeakProcess.prepareMyParams();
		
		//Display.
		ishDetGraphics = new IshDetGraphics(this, getOutputDataBlock());
		//FX display data providers
		IshmaelDetPlotProvider ishDetPlotProviderFX = new IshmaelDetPlotProvider(this);
		IshmaelFnPlotProvider ishFnPlotProviderFX = new IshmaelFnPlotProvider(this);

		TDDataProviderRegisterFX.getInstance().registerDataInfo(ishDetPlotProviderFX);
		TDDataProviderRegisterFX.getInstance().registerDataInfo(ishFnPlotProviderFX);
		
		//Saver.
		ishDetSave = new IshDetSave(this);
		

	}
	
	/**
	 * Get the Ishmael Fn process - this creates the detector output but 
	 * does not perform the binary classification. 
	 * @returnc the Ishmael Fn process
	 */
	public IshDetFnProcess getIshDetFnProcess() {
		return ishDetFnProcess;
	}

	/** Return any old data block of the right type so that the detection 
	 * process's input can get hooked up to something from the get-go.  The
	 * input is typically re-hooked when the settings file is read.
	 * @return PamDataBlock
	 */
	public abstract PamDataBlock getDefaultInputDataBlock();
	
	/** Create a new IshDetProcess of the appropriate type and return it.
	 * For example, EnergySumControl returns an EnergySumProcess.
	 */
	public abstract IshDetFnProcess getNewDetProcess(PamDataBlock defaultDataBlock);
	
	public abstract PamRawDataBlock getRawInputDataBlock();
	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#SetupControlledUnit()
	 */
	//@Override
	//public void setupControlledUnit() {
	//	super.setupControlledUnit();
	//}

	/* This is a hack to get the non-detection processes initialized.  Since
	 * the detection process is the only one registered via addPamProcess, it
	 * is the only one that gets a call to prepareProcess.  Here we make sure
	 * that my other processes (peak-picking, graphics, save) get initialized. 
	 * These other processes don't have addPamProcess called on them because
	 * then they would show up in the Data Model.
	 */
	public void prepareNonDetProcesses() {
		ishDetGraphics.prepareForRun();
		ishPeakProcess.prepareForRun();
		ishDetSave.prepareForRun();
	}
	
	public JMenuItem createDetectionMenu(Frame parentFrame, String menuString) {
		JMenuItem menuItem = new JMenuItem(menuString);
		menuItem.addActionListener(new IshDetSettings(parentFrame));
		return menuItem;
	}
	
	class IshDetSettings implements ActionListener {
		Frame parentFrame;
		public IshDetSettings(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}
		@Override
		public void actionPerformed(ActionEvent e) { 
			//old way: showParamsDialog(parentFrame, ishDetParams);
			showParamsDialog1(parentFrame);
		}
	}

	abstract public void showParamsDialog1(Frame parentFrame);
	
//	class menuSmoothingDetection implements ActionListener {
//		public void actionPerformed(ActionEvent ev) {
//			KernelSmoothingParameters newParams = KernelSmoothingDialog.show(smoothingParameters, smoothingProcess.getOutputDataBlock(0));
//			if (newParams != null) {
//				smoothingParameters = newParams.clone();
////				edgeSettings.prepareProcess();
//				newSettings();
//				PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
//			}
//		}
//	}

//	public long getSettingsVersion() {
//		return KernelSmoothingParameters.serialVersionUID;
//	}

	/** This is called after a settings file is read.  The subclass should 
	 * get newParams and clone it as ishDetParams before calling here.
	 */
	@Override
	public boolean restoreSettings(PamControlledUnitSettings dummy) {
		//Subclass should clone newParams before calling here!!
		if (ishDetFnProcess  != null) ishDetFnProcess .setupConnections();
		if (ishPeakProcess   != null) ishPeakProcess  .setupConnections();
		return true;
	}
	
	protected void installNewParams(Frame parentFrame, IshDetParams newParams) {
		if (newParams != null) {
			ishDetParams = newParams.clone(); //makes a new EnergySumParams etc.
			if (ishDetFnProcess != null) {
				ishDetFnProcess.setupConnections();
				PamController.getInstance().notifyModelChanged(
						PamControllerInterface.CHANGED_PROCESS_SETTINGS);
			}
			if (ishPeakProcess != null)
				ishPeakProcess.setupConnections();
		}
		this.ishDetGraphics.setFirstTime(true);
	}
	
	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#pamToStart()
	 */
	@Override
	public void pamToStart() {
		super.pamToStart();
		if (ishDetFnProcess != null) {
			ishDetFnProcess.setupConnections();
		}
		if (ishPeakProcess != null)
			ishPeakProcess.setupConnections();
	}

	public PamDataBlock getOutputDataBlock() {
		return ishDetFnProcess.outputDataBlock;
	}

	public IshDetParams getIshDetectionParams() {
		return ishDetParams;
	}
	
	/**
	 * Get a bitmap of the active channels
	 * <p>
	 * If the channel is selected and there is no grouping then channel data unit is analysed. 
	 * If there are groups then the only the first channel in the group is analysed. 
	 * @return bitmap of active channels
	 */
	public  int getActiveChannels() {
		int nGroups = ishDetParams.groupedSourceParmas.countChannelGroups();
		
		// if there are no groups, just pass back the current channel bitmap
		if (nGroups==0) {
			return ishDetParams.groupedSourceParmas.getChanOrSeqBitmap();
		}
		
		//otherwise, grab the first channel from the grouped detector. 
		int[] activechans = new int[nGroups];
		int[] chanGroups= PamUtils.getChannelArray(ishDetParams.groupedSourceParmas.getGroupMap());
		//System.out.println("Number groups: " + chanGroups.length);
		for (int i=0; i<activechans.length; i++) {
			//check whether first channel in group corresponds to the dat
			//System.out.println("SINGLE CHANL: " + PamUtils.getLowestChannel(ishDetParams.groupedSourceParmas.getGroupChannels(i)) + " Group: " + chanGroups[i]);
			activechans[i]=PamUtils.getLowestChannel(ishDetParams.groupedSourceParmas.getGroupChannels(chanGroups[i]));
		}
		
		return PamUtils.makeChannelMap(activechans); 
	}
	
	
	/**
	 * Check whether a data unit should be analysed for peak and saved depending on grouped detection
	 * <p>
	 * If the channel is selected and there is no grouping then channel data unit is analysed. 
	 * If there are groups then the only the first channel in the group is analysed. 
	 * @param ishFnDataUnit - the Ishamel raw data unit. 
	 * @return
	 */
	public boolean isChanActive(int channelMap) {
		
		if (ishDetParams.groupedSourceParmas.getGroupingType() == GroupedSourcePanel.GROUP_SINGLES)
			return ((channelMap & ishDetParams.groupedSourceParmas.getChanOrSeqBitmap()) == 0); 
		
		int[] chanGroups = ishDetParams.groupedSourceParmas.getChannelGroups(); 
		for (int i=0; i<chanGroups.length; i++) {
			//check whether first channel in group corresponds to the data unit channel
			if (PamUtils.hasChannel(channelMap, PamUtils.getSingleChannel(chanGroups[i]))) return true; 
		}
		return false;
	}

	/**
	 * Get the Ishmael peak process. The peak process selects handles
	 * binary classification of the detector output. 
	 * @return the Ishamel peak process. 
	 */
	public IshPeakProcess getIshPeakProcess() {
		return this.ishPeakProcess;
	}
	
}
