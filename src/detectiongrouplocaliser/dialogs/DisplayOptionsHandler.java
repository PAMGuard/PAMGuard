package detectiongrouplocaliser.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JRadioButton;

import detectiongrouplocaliser.DetectionGroupControl;

public class DisplayOptionsHandler {

	private DetectionGroupControl detectionGroupControl;

	/*
	 * lags for showing current period or the whole lot. 
	 */
	public static final int SHOW_CURRENT = 0;
	public static final int SHOW_ALL = 1;

	public DisplayOptionsHandler(DetectionGroupControl detectionGroupControl) {
		super();
		this.detectionGroupControl = detectionGroupControl;
	}
	
	private ArrayList<DGRadioButton> radioButtons = new ArrayList<>();
	
	/**
	 * Create a radio button for use in a dialog and register it with any 
	 * button lists as required. 
	 * @param optionId option number (SHOW_CURRENT or SHOW_ALL)
	 * @param text text to show in the button. 
	 * @param immediateAction take action to change loaded data immediately. 
	 * @return
	 */
	public JRadioButton createButton(int optionId, String text, boolean immediateAction) {
		DGRadioButton rb = new DGRadioButton(optionId, text);
		String tip = getToolTip(optionId);
		rb.setToolTipText(tip);
		radioButtons.add(rb);
		rb.setSelected(optionId == detectionGroupControl.getDetectionGroupSettings().getOfflineShowOption());
		if (immediateAction) {
			rb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					buttonAction(rb, optionId);
				}
			});
		}
		return rb;
	}
	
	protected void buttonAction(DGRadioButton rb, int optionId) {
		if (!rb.isSelected()) {
			return; // no action when a button deselected.
		}
		int currentOption = detectionGroupControl.getDetectionGroupSettings().getOfflineShowOption();
		if (optionId == currentOption) {
			return; // again, no need to do anything. 
		}
		setSelectedOption(optionId);
	}

	private void setSelectedOption(int optionId) {
		for (DGRadioButton rb:radioButtons) {
			rb.setSelected(rb.optionId == optionId);
		}
		detectionGroupControl.getDetectionGroupSettings().setOfflineShowOption(optionId);
		detectionGroupControl.getDetectionGroupProcess().changeOfflineLoadSelection();
	}
	
	/**
	 * Create default tool tip texts. 
	 * @param optionId
	 * @return
	 */
	private String getToolTip(int optionId) {
		switch (optionId) {
		case SHOW_ALL: 
			return "Load and display all detection groups in the current database";
		case SHOW_CURRENT: 
			return "Load and display only those detectin groups overlapping the current dava view period";
		}
		return null;
	}

	private class DGRadioButton extends JRadioButton {
		
		int optionId;

		public DGRadioButton(int optionId, String text) {
			super(text);
			this.optionId = optionId;
		}
	}
	
}
