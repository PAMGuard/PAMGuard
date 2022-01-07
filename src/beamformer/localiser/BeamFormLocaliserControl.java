package beamformer.localiser;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import javax.swing.JMenuItem;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsPane;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import beamformer.BeamFormerBaseControl;
import beamformer.BeamFormerParams;
import beamformer.annotation.BFAnnotationType;
import beamformer.localiser.dialog.BFLocSettingsPane2;
import beamformer.localiser.plot.BeamLocDisplayProvider;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

public class BeamFormLocaliserControl extends BeamFormerBaseControl implements PamSettings {

	public static final String unitType = "Beamformer Localiser";

	private BFDetectionMonitor bfDetectionMonitor;

	private BeamFormLocProcess beamFormLocProcess;

	private BFLocaliserParams bfLocaliserParams = new BFLocaliserParams();

//	private BFLocSettingsPane bfLocSettingsPane;

	private QueuedDataBlock queuedDataBlock;


	private BeamLocaliserObservable beamLocaliserObservable = new BeamLocaliserObservable();
	
	private BFAnnotationType bfAnnotationType;

	@Deprecated // all functionality now in bearing localiser. 
	public BeamFormLocaliserControl(String unitName) {
		super(unitType, unitName);
		setBeamFormerParams(bfLocaliserParams);
		
		bfAnnotationType = new BFAnnotationType(this);

		beamFormLocProcess = new BeamFormLocProcess(this);
		setBeamFormerProcess(beamFormLocProcess);
		addPamProcess(beamFormLocProcess);

		bfDetectionMonitor = new BFDetectionMonitor(this);
		addPamProcess(0, bfDetectionMonitor);

		queuedDataBlock = new QueuedDataBlock(PamDataUnit.class, "Queued Data", bfDetectionMonitor, 0);

		PamSettingManager.getInstance().registerSettings(this);

		new BeamLocDisplayProvider(this);
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#removeUnit()
	 */
	@Override
	public boolean removeUnit() {
		return super.removeUnit();
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		switch(changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			bfDetectionMonitor.prepareProcess();
			break;
		}
		super.notifyModelChanged(changeType);
	}

	/**
	 * @return the bfLocaliserParams
	 */
	public BFLocaliserParams getBfLocaliserParams() {
		return bfLocaliserParams;
	}
	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame);
			}
		});
		return menuItem;
	}

	private PamDialogFX2AWT<BFLocaliserParams> settingsDialog;
	
	protected void showSettingsDialog(Frame parentFrame) {
		if (settingsDialog == null || parentFrame != settingsDialog.getOwner()) {
			SettingsPane<BFLocaliserParams> setPane = new BFLocSettingsPane2(parentFrame, this);;
			settingsDialog = new PamDialogFX2AWT<BFLocaliserParams>(parentFrame, setPane, false);
		}
		BFLocaliserParams newParams = settingsDialog.showDialog(bfLocaliserParams);
		if (newParams != null) {
			bfLocaliserParams = newParams;
			getBeamFormerProcess().prepareProcess();
			bfDetectionMonitor.prepareProcess();
			beamLocaliserObservable.updateSettings();
		}
	}


	@Override
	public Serializable getSettingsReference() {
		return bfLocaliserParams;
	}

	@Override
	public long getSettingsVersion() {
		return BFLocaliserParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		bfLocaliserParams = ((BFLocaliserParams) pamControlledUnitSettings.getSettings()).clone();
		return (bfLocaliserParams != null);
	}

	/* (non-Javadoc)
	 * @see beamformer.BeamFormerBaseControl#getBeamFormerParams()
	 */
	@Override
	public BeamFormerParams getBeamFormerParams() {
		return bfLocaliserParams;
	}

	/* (non-Javadoc)
	 * @see beamformer.BeamFormerBaseControl#setBeamFormerParams(beamformer.BeamFormerParams)
	 */
	@Override
	public void setBeamFormerParams(BeamFormerParams beamFormerParams) {
		this.bfLocaliserParams = (BFLocaliserParams) beamFormerParams;
		super.setBeamFormerParams(beamFormerParams);
	}

	public void newTriggerData(PamDataUnit pamDataUnit) {
		queuedDataBlock.addPamData(pamDataUnit, pamDataUnit.getUID());
	}

	/**
	 * @return the queuedDataBlock
	 */
	public QueuedDataBlock getQueuedDataBlock() {
		return queuedDataBlock;
	}

	/**
	 * Run the beam former within the given time-frequency box. 
	 * @param timeRange
	 * @param freqRange
	 * @param markChannels
	 */
	public boolean beamFormDataUnit(PamDataUnit pamDataUnit) {
		return beamFormLocProcess.beamFormDataUnit(pamDataUnit);
	}

	/**
	 * Return an observable which will get updates as beam forming takes place
	 * can be used to update graphics, etc. 
	 * @return the beamLocaliserObservable
	 */
	public BeamLocaliserObservable getBeamLocaliserObservable() {
		return beamLocaliserObservable;
	}

	/**
	 * @return the bfDetectionMonitor
	 */
	public BFDetectionMonitor getBfDetectionMonitor() {
		return bfDetectionMonitor;
	}

	/**
	 * @return the bfAnnotationType
	 */
	public BFAnnotationType getBfAnnotationType() {
		return bfAnnotationType;
	}

	/**
	 * This gets called when the output datablock has more localisation content
	 * options added to it (e.g. from the crossed bearing localiser). We need to 
	 * pass this information on to the trigger data source so that it can update
	 * it's own list, thereby making itself available to displays, etc. 
	 * @param localisationContents
	 */
	public void addDownstreamLocalisationContents(int localisationContents) {
		/*
		 * Find the trigger source datablock and add loc contents to it ...
		 */
		PamDataBlock triggerBlock = PamController.getInstance().getDataBlockByLongName(bfLocaliserParams.detectionSource);
		if (triggerBlock != null) {
			triggerBlock.addLocalisationContents(localisationContents);
		}
	}
}
