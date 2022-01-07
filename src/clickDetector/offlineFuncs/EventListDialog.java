package clickDetector.offlineFuncs;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import PamController.PamController;
import PamView.dialog.PamDialog;
import clickDetector.ClickControl;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;

public class EventListDialog extends PamDialog {
	
	private OfflineEventListPanel offlineEventListPanel;
	
	private static EventListDialog singleInstance;
	
	private ClickControl clickControl;
		
	private Window parentFrame;
	
	private OfflineEventDataBlock offlineEventDataBlock;

	private JPanel mainPanel;
	
	protected EventListDialog(Window parentFrame, ClickControl clickControl) {
		super(parentFrame, clickControl.getUnitName() + " Event List", false);
		this.parentFrame = parentFrame;
		this.clickControl = clickControl;
		offlineEventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
		offlineEventDataBlock.addObserver(new EventObserver());
		
		mainPanel = new JPanel(new BorderLayout());
		JPanel eventListPanel = new JPanel(new BorderLayout());
		offlineEventListPanel = new OfflineEventListPanel(clickControl);
		eventListPanel.add(BorderLayout.CENTER, offlineEventListPanel.getPanel());
		eventListPanel.add(BorderLayout.NORTH, new JLabel("Event list"));
		mainPanel.add(BorderLayout.CENTER, eventListPanel);
		
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(new TitledBorder("Selection"));
		southPanel.add(BorderLayout.WEST, offlineEventListPanel.getSelectionPanel());
		mainPanel.add(BorderLayout.SOUTH, southPanel);
				
		offlineEventListPanel.addMouseListener(new TableMouse());
		offlineEventListPanel.addListSelectionListener(new ListSelection());
		
		setResizable(true);
		
		setDialogComponent(mainPanel);
		setModal(false);
	}
	
	public JPanel getMainPanel(){
		return mainPanel;
		
	}
	
	

	public static void showDialog(Window parentFrame, ClickControl clickControl) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || 
				singleInstance.clickControl != clickControl) {
			singleInstance = new EventListDialog(parentFrame, clickControl);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
	}
	
	private void setParams() {
		offlineEventListPanel.tableDataChanged();
		offlineEventListPanel.setSelectedEvent(clickControl.getLatestOfflineEvent());
		enableControls();
	}

	private void enableControls() {
		
//		OfflineEventDataUnit selEvent = offlineEventListPanel.getSelectedEvent();
//		getOkButton().setEnabled(selEvent != null);
	}
	
	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// called when the OK button is pressed. 
//		OfflineEventDataUnit selEvent = offlineEventListPanel.getSelectedEvent();
//		if (selEvent != null) {
//			addClicksToEvent(selEvent, false);
//			return true;
//		}
		return true;
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

		@Override
		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showPopupMenu(me);
			}
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showPopupMenu(me);
			}
		}
	}

	public void mouseDoubleClick() {
		OfflineEventDataUnit event = offlineEventListPanel.getSelectedEvent();
		if (event != null) {
			editEvent(event);
		}
	}
	
	private void editEvent(OfflineEventDataUnit event) {
		if (event == null) {
			return;
		}
		OfflineEventDataUnit event2 = OfflineEventDialog.showDialog(getOwner(), clickControl, event);
		if (event2 != null) {
			// do we need to do anything else ?
			// should probably send round an update so the map, etc know. 
			offlineEventListPanel.tableDataChanged();
			offlineEventDataBlock.updatePamData(event, System.currentTimeMillis());
		}
	}

	public void showPopupMenu(MouseEvent me) {
		OfflineEventDataUnit event = offlineEventListPanel.getSelectedEvent();
		if (event == null) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItem;
		int evNo = event.getDatabaseIndex();
		menuItem = new JMenuItem(String.format("Goto event %d ...", evNo));
		menuItem.addActionListener(new GotoEvent(event));
		menu.add(menuItem);
		menuItem = new JMenuItem(String.format("Edit event %d ...", evNo));
		menuItem.addActionListener(new EditEvent(event));
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new JMenuItem(String.format("Delete event %d ...", evNo));
		menuItem.addActionListener(new DeleteEvent(event));
		menu.add(menuItem);
		
		menu.show(me.getComponent(), me.getX(), me.getY());
	}
	
	private class EditEvent implements ActionListener {
		
		private OfflineEventDataUnit event;

		public EditEvent(OfflineEventDataUnit event) {
			this.event = event;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			editEvent(event);
		}
	}
	private class DeleteEvent implements ActionListener {
		
		private OfflineEventDataUnit event;

		public DeleteEvent(OfflineEventDataUnit event) {
			this.event = event;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			deleteEvent(event);
		}
	}
	
	private class GotoEvent implements ActionListener {
		
		private OfflineEventDataUnit event;

		public GotoEvent(OfflineEventDataUnit event) {
			this.event = event;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			clickControl.gotoEvent(event);
		}
		
	}

	public void deleteEvent(OfflineEventDataUnit event) {
		String msg = String.format("Are you sure you want to delete event %d with %d clicks ?",
				event.getDatabaseIndex(), event.getNClicks());
		int ans = JOptionPane.showConfirmDialog(mainPanel, msg, "Warning", JOptionPane.YES_NO_OPTION);
		if (ans == JOptionPane.YES_OPTION) {
			clickControl.deleteEvent(event);
			offlineEventListPanel.tableDataChanged();
		}
	}
	
	private class EventObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "Event list dialog";
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			offlineEventListPanel.tableDataChanged();
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			offlineEventListPanel.tableDataChanged();			
		}
		
	}

	public static void notifyModelChanged(int changeType) {
		if (singleInstance != null) {
			singleInstance.modelChanged(changeType);
		}
	}

	private void modelChanged(int changeType) {
		if (changeType == PamController.OFFLINE_DATA_LOADED) {
			offlineEventListPanel.tableDataChanged();
		}
		
	}
	
}
