package clickDetector.offlineFuncs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import PamView.dialog.PamDialog;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickBTDisplay;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;

/**
 * Dialog for adding and removing clicks to and from 
 * offline events. 
 * @author Doug Gillespie
 *
 */
public class LabelClicksDialog extends PamDialog {
	
	private OfflineEventListPanel offlineEventListPanel;
	
	private static LabelClicksDialog singleInstance;
	
	private ClickControl clickControl;
	
//	private ClickBTDisplay btDisplay;
	
	private ClickDetection singleClick;
	
	private JButton newEventButton;
	
	private Window parentFrame;
	
	private OfflineEventDataBlock offlineEventDataBlock;
	
	private List<PamDataUnit> markedClicks;

	private OverlayMark overlayMark;

	private OfflineEventDataUnit selectedEvent;

	private LabelClicksDialog(Window parentFrame, ClickControl clickControl) {
		super(parentFrame, clickControl.getUnitName() + " Label clicks", false);
		this.parentFrame = parentFrame;
		this.clickControl = clickControl;
		offlineEventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel eventListPanel = new JPanel(new BorderLayout());
		offlineEventListPanel = new OfflineEventListPanel(clickControl);
		eventListPanel.add(BorderLayout.CENTER, offlineEventListPanel.getPanel());
		eventListPanel.add(BorderLayout.NORTH, new JLabel("Event list"));
		mainPanel.add(BorderLayout.CENTER, eventListPanel);
		
		JPanel southPanel = new JPanel(new BorderLayout());
		JPanel sePanel = new JPanel(new BorderLayout());
		sePanel.add(BorderLayout.NORTH, newEventButton = new JButton("New Event ..."));
		southPanel.add(BorderLayout.EAST, sePanel);
		newEventButton.addActionListener(new NewEvent());
		southPanel.add(BorderLayout.WEST, offlineEventListPanel.getSelectionPanel());
//		southPanel.add(editSpeciesButton = new JButton("Edit list"));
//		editSpeciesButton.addActionListener(new EditSpeciesList());
		mainPanel.add(BorderLayout.SOUTH, southPanel);
		
		offlineEventListPanel.addMouseListener(new TableMouse());
		offlineEventListPanel.addListSelectionListener(new ListSelection());
		
		setResizable(true);
		
		setDialogComponent(mainPanel);
	}

	public static OfflineEventDataUnit showDialog(Window parentFrame, ClickControl clickControl, 
			OverlayMark overlayMark, PamDataUnit singleClick) {
		List<PamDataUnit> unitList = new ArrayList<>();
		unitList.add(singleClick);
		return showDialog(parentFrame, clickControl, overlayMark, unitList);
	}
	
	public static OfflineEventDataUnit showDialog(Window parentFrame, ClickControl clickControl, 
			OverlayMark overlayMark, List<PamDataUnit> markedClicks) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || 
				singleInstance.clickControl != clickControl) {
			singleInstance = new LabelClicksDialog(parentFrame, clickControl);
		}
		singleInstance.markedClicks = markedClicks;
		singleInstance.overlayMark = overlayMark;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.selectedEvent;
	}
	
	private void setParams() {
		offlineEventListPanel.tableDataChanged();
		if (singleClick != null) {
			markedClicks = new ArrayList<PamDataUnit>();
			markedClicks.add(singleClick);
		}
		else {
//			markedClicks = btDisplay.getMarkedClicks();
		}
		enableControls();
	}

	private void enableControls() {
		boolean haveClicks = (markedClicks != null && markedClicks.size() > 0);
		newEventButton.setEnabled(haveClicks);
		
		OfflineEventDataUnit selEvent = offlineEventListPanel.getSelectedEvent();
		getOkButton().setEnabled(selEvent != null);
	}
	
	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// called when the OK button is pressed. 
		 selectedEvent = offlineEventListPanel.getSelectedEvent();
		if (selectedEvent != null) {
			addClicksToEvent(selectedEvent, false);
			return true;
		}
		return false;
	}

	/**
	 * Add a group of clicks to a new event, then optinally close the dialog
	 * @param event event to add to 
	 * @param thenClose option to close dialog
	 */
	private void addClicksToEvent(OfflineEventDataUnit event, boolean thenClose) {
		removeFromOldEvent(markedClicks);
		event.addSubDetections(markedClicks);
		offlineEventListPanel.tableDataChanged();
		clickControl.setLatestOfflineEvent(event);
		if (thenClose) {
			setVisible(false);
		}		
	}

	/**
	 * clicks may have already been part of an event, so need to remove them from that 
	 * event first, and if there is nothing left in that event, delete the event. 
	 * @param markedClicks2
	 */
	private void removeFromOldEvent(List<PamDataUnit> markedClicks) {
		clickControl.removeFromEvents(markedClicks);
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	private class ListSelection implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			enableControls();
		}
	}
	
	private class TableMouse extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				mouseDoubleClick();
			}
		}
	}
	
	/**
	 * Double clicking on an event will automatically add the marked clicks 
	 * to that event and close the dialog. 
	 */
	public void mouseDoubleClick() {
		OfflineEventDataUnit event = offlineEventListPanel.getSelectedEvent();
		if (event == null) {
			return;
		}
		event = OfflineEventDialog.showDialog(parentFrame, clickControl, event);
		if (event != null) {
			addClicksToEvent(event, true);
			event.notifyUpdate();
		}
	}

	private class NewEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			newEvent();
		}
	}

	public void newEvent() {
//		int nextNum = offlineEventDataBlock.getNextFreeEventNumber();
		int nextNum = 0;
		if (clickControl.getClicksOffline() != null) {
			nextNum = clickControl.getClicksOffline().getNextEventColourIndex();
		}
		selectedEvent = new OfflineEventDataUnit(null, nextNum, null);
		selectedEvent = OfflineEventDialog.showDialog(parentFrame, clickControl, selectedEvent);
		if (selectedEvent != null) {
			addClicksToEvent(selectedEvent, false);
			if (selectedEvent.getParentDataBlock() == null) {
				offlineEventDataBlock.addPamData(selectedEvent);
				offlineEventDataBlock.notifyObservers(selectedEvent);
			}
			setVisible(false);
		}
	}
	

}
