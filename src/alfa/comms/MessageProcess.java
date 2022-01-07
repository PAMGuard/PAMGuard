package alfa.comms;

import javax.swing.JComponent;

import PamController.PamController;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import alfa.ALFAControl;
import alfa.swinggui.SwingMessagePanel;
import rockBlock.RockBlockControl;

public class MessageProcess extends PamProcess {

	private RockBlockControl rockBlockControl;

	public MessageProcess(ALFAControl alfaControl) {
		super(alfaControl, null);
		setParentDataBlock(alfaControl.getEffortMonitor().getIntervalDataBlock());
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		updateData(o, arg);
	}

	@Override
	public void updateData(PamObservable o, PamDataUnit arg) {
		if (arg instanceof ALFACommsDataUnit) {
			newALFAComms((ALFACommsDataUnit) arg);
		}
	}

	private void newALFAComms(ALFACommsDataUnit alfaCommsDataUnit) {
		if (alfaCommsDataUnit.isReadyToSend() == false) {
			return;
		}
		if (rockBlockControl == null) {
			return;
		}
		String txt = alfaCommsDataUnit.getCommsString();
		System.out.println(txt);
		rockBlockControl.sendText(txt);
	}


	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
		case PamController.ADD_CONTROLLEDUNIT:
		case PamController.REMOVE_CONTROLLEDUNIT:
		if (PamController.getInstance().isInitializationComplete()) {
			findRockBlock();
		}
		}
	}

	public RockBlockControl findRockBlock() {
		if (rockBlockControl == null) {
			rockBlockControl = (RockBlockControl) PamController.getInstance().findControlledUnit(RockBlockControl.class, null);
		}
		return rockBlockControl;
	}

	/**
	 * Get a panel with tables for incoming and outgoing messages
	 * @param showWhat 1 = outgoing, 2 = incoming 3 = both
	 * @return
	 */
	public JComponent getSwingCommsPanel(int showWhat) {
		SwingMessagePanel swingMessagePanel = new SwingMessagePanel(this, showWhat);
		return swingMessagePanel.getComponent();
		
	}


}
