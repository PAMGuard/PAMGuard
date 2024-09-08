/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package PamView;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;

/**
 * @author Doug Gillespie
 *         <p>
 *         Creates a simple pop-up window which displays lists of Data Objects
 *         and Pam Processes
 * 
 */
public class PamObjectList extends PamDialog implements WindowListener {

//	JFrame frame;

	// JTextArea textArea;
	private JPanel mainPanel;

	private JTable table;

	private Timer timer;

	private String[] columnNames;

	private TableData tableData;

	private static PamObjectList pamObjectList;

	public static void ShowObjectList(JFrame jFrame) {
		if (pamObjectList == null) {
			pamObjectList = new PamObjectList(PamController.getInstance());
		}
		pamObjectList.setVisible(true);
		pamObjectList.timer.start();
	}

	private PamObjectList(PamController pamController) {
		super(PamController.getMainFrame(), "PAMGuard Object List", false);

		mainPanel = new JPanel(new GridLayout(1, 0));
		mainPanel.setOpaque(true);

		columnNames = new String[] { "Module", "Data Block", "Count", "First", "Last", "UID Range" };
		int columnWidths[] = {90, 120, 10, 100, 100, 80}; 
		tableData = new TableData();
		table = new ObjectTable(tableData);
		for (int i = 0; i < columnWidths.length; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
		}
		table.addMouseMotionListener(new TableMouse());
		new SwingTableColumnWidths("PAMGuard Object List", table);
//		table.setToolTipText("PAMGUARD Data unit counts");

		JScrollPane scrollPanel = new JScrollPane(table);
		mainPanel.add(scrollPanel);

		mainPanel.setPreferredSize(new Dimension(650, 320));
		this.setDialogComponent(mainPanel);
		this.setModalityType(ModalityType.MODELESS);
		this.setResizable(true);
//		frame.setContentPane(mainPanel);

		// now sort out the menu
		// frame.setJMenuBar(PamMenu.CreateBasicMenu(null, new MenuListener()));

//		frame.pack();
		// frame.setVisible(true);

		timer = new Timer(1000, new TimerListener());
		timer.setInitialDelay(200);
		getOkButton().setVisible(false);
		getCancelButton().setText("Close");
		// timer.start();

	}
	
	class TableMouse extends MouseAdapter {

		/* (non-Javadoc)
		 * @see com.jogamp.newt.event.MouseAdapter#mouseDragged(com.jogamp.newt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			tableMouseMove(e);
		}

		/* (non-Javadoc)
		 * @see com.jogamp.newt.event.MouseAdapter#mouseMoved(com.jogamp.newt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			tableMouseMove(e);
		}
		
	}
	
	class ObjectTable extends JTable {

		public ObjectTable(TableData tableData) {
			super(tableData);
			setToolTipText("");
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getToolTipText()
		 */
		@Override
		public String getToolTipText() {
			return super.getToolTipText();
		}
		
	}

	class TableData extends AbstractTableModel {

		// ArrayList<PamProcess> processList;

		public TableData() {
			// processList= pamModelInterface.GetModelProcessList();
			// for (int i = 0; i < processList.size(); i++){
			// //processList.get(i).GetOutputDataBlock().addObserver(this);
			// }
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public synchronized int getRowCount() {
			return PamController.getInstance().getDataBlocks().size();
		}

		@Override
		public synchronized int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public synchronized Object getValueAt(int row, int col) {
			/*
			 * need to find the right process by going through and seeing which
			 * one our block is in for the given row
			 */
			ArrayList<PamDataBlock> blockList = PamController.getInstance().getDataBlocks();
			int blocks = 0;

			PamProcess process = null;
			if (row >= blockList.size())
				return null;

			PamDataBlock block = blockList.get(row);
			process = block.getParentProcess();
			PamDataUnit aUnit;

			// if (process != null)
			String str = new String();
			switch (col) {
			case 0:
				if (process != null)
					return process.getPamControlledUnit().getUnitName();
			case 1:
				return block.toString();
			case 2:
				str = String.format("%d", block.getUnitsCount());
//				if (block.getLongestObserver() != null) str += "  " + block.getLongestObserver().toString();
				return str;
			case 3:
				aUnit = block.getFirstUnit();
				if (aUnit == null) {
					return("-");
				}
				else {
					return PamCalendar.formatDBDateTime(aUnit.getTimeMilliseconds());
				}
			case 4:
				aUnit = block.getLastUnit();
				if (aUnit == null) {
					return("-");
				}
				else {
					return PamCalendar.formatDBDateTime(aUnit.getTimeMilliseconds());
				}
			case 5:
				if (block.getUidHandler() == null) {
					return null;
				}
				aUnit = block.getFirstUnit();
				if (aUnit == null) {
					return String.format("Next: %d", block.getUidHandler().getCurrentUID());
				}
				long firstUID = aUnit.getUID();
				aUnit = block.getLastUnit();
				if (aUnit == null) {
					return("-");
				}
				long lastUID = aUnit.getUID();
				return String.format("%d - %d", firstUID, lastUID);
			}
			return "";
		}

	}

	class TimerListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			tableData.fireTableDataChanged();
		}
	}

//	class MenuListener implements ActionListener {
//		public void actionPerformed(ActionEvent ev) {
//			// table.
//			// tableData.fireTableRowsUpdated(0,tableData.getRowCount()-1);
//			JMenuItem menuItem = (JMenuItem) ev.getSource();
//			//System.out.println(menuItem.getText());
//
//			if (menuItem.getText().equals(("Start PAM"))) {
//				PamController.getInstance().pamStart();
//			} else if (menuItem.getText().equals(("Stop PAM"))) {
//				PamController.getInstance().pamStop();
//			}
//		}
//	}

	public void PamStarted() {
		timer.start();
	}

	public void tableMouseMove(MouseEvent e) {
		int row = table.rowAtPoint(new Point(e.getX(), e.getY()));
		if (row == -1) {
			table.setToolTipText("");
			return;
		}
		else {
			ArrayList<PamDataBlock> blockList = PamController.getInstance().getDataBlocks();
			int blocks = 0;

			if (row >= blockList.size()) {
				table.setToolTipText("");
				return;
			}

			PamDataBlock block = blockList.get(row);
			String tip = String.format("<html>%s observers:", block.getDataName());
//			for (PamObserver o:observers){
//				tip += String.format("<br>%s %ds", o.getObserverName(), o.getRequiredDataHistory(block, null));
//			}
			int nObservers = block.countObservers();
			
			if (nObservers == 0) {
				tip+="<br>No observers";
			}
			else {
				tip += "<table cellspacing=\"0\" cellpadding=\"0\" align=\"right\">";
				tip += "<th>Observer</th><th>HH:MM:SS</th>";
				for (int i = 0; i < nObservers; i++){
					PamObserver o = block.getPamObserver(i);
					long hist = o.getRequiredDataHistory(block, null);
					tip += String.format("<tr><td>%s  </td><td>-   %s</td></tr>", 
							o.getObserverName(),PamCalendar.formatTime(hist, 0, true));
				}
				tip += "</table>";
			}
			tip += "</html>";
			table.setToolTipText(tip);
			
		}
	}

	public void PamEnded() {
		// timer.stop();
	}

	/**
	 * Implementation of WindowListener
	 */
	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		timer.stop();
	}

	@Override
	public void windowOpened(WindowEvent e) {
		timer.start();
	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// PamSettingManager.getInstance().SaveSettings();

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// PamSettingManager.getInstance().SaveSettings();

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		timer.stop();
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
}
