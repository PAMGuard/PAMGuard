package Array;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import Array.streamerOrigin.HydrophoneOriginMethod;
import PamController.PamController;

/**
 * Panel for the ArrayDialog to show a selection of hydrophone arrays and
 * listed details of the currently selected array
 * @author Doug Gillespie
 * @see Array.ArrayDialog
 * @see Array.PamArray
 *
 */
public class HydrophoneDialogPanel implements ActionListener, ListSelectionListener {

	private JPanel hydrophonePanel;
	
	private JButton deleteButton, addButton, editButton;
	
	private JTable hydrophoneTable;
	
	private JComboBox<PamArray> recentArrays;
	
//	private JComboBox<HydrophoneLocatorSystem> arrayLocators;
	
	private HydrophoneTableData hydrophoneTableData;

	private StreamerTableData streamerTableData = new StreamerTableData();
	
	private StreamerPanel streamerPanel;
	
//	private StaticTowedPanel staticTowedPanel;
	
//	private StaticPositionPanel staticPositionPanel;
	
	private String[] hydrophoneColumns = {"Id", "x", "y", "depth", "x Err", "y Err", " depth Err", "Streamer"};
	
	private int[] hydrophoneMap;

//	String[] streamerColumns = {"Id", "x", "y", "depth", "x Err", "y Err", "depth Err", "buoy"};
	String[] streamerColumns = {"Id", "Name", "x", "y", "depth", "Reference", "Locator"};
	int[] streamerColumWidths = {10, 20, 15, 15, 15, 70, 70};

	
	private ArrayDialog arrayDialog;
	
//	private JPanel locatorPanelContainer;
	private LocatorDialogPanel locatorDialogPanel;

	private JLabel configPanelLabel;

	private ArrayManager arrayManager;

	HydrophoneDialogPanel (ArrayDialog arrayDialog, ArrayManager arrayManager) {
		
		this.arrayDialog = arrayDialog;
		this.arrayManager = arrayManager;
		
//		staticTowedPanel = new StaticTowedPanel();
		
		streamerPanel = new StreamerPanel();

//		staticPositionPanel = new StaticPositionPanel();
		
		hydrophoneTableData = new HydrophoneTableData();
		
		hydrophonePanel = makePanel();
		
		updateData();
	}
	
	private JPanel makePanel(){
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
//		locatorPanelContainer = new JPanel();
		//panel.setBorder(new EmptyBorder(10,10,10,10));
		
		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BorderLayout());
		panel.setBorder(new TitledBorder("Array Configuration"));
		configPanel.add(BorderLayout.NORTH, configPanelLabel = new JLabel(""));
		hydrophoneTable = new JTable(hydrophoneTableData);
		hydrophoneTable.setToolTipText("Hydrophone coordinates are relative to Streamer coordinates");
		hydrophoneTable.setBorder(new EmptyBorder(10,10,10,10));
		hydrophoneTable.getSelectionModel().addListSelectionListener(this);
		hydrophoneTable.addMouseListener(new HydrophoneMouse());
//		hydrophoneTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//		hydrophoneTable.getColumnModel().getColumn(5).setPreferredWidth(150);
		JScrollPane scrollPane = new JScrollPane(hydrophoneTable);
		scrollPane.setPreferredSize(new Dimension(320, 90));
		configPanel.add(BorderLayout.CENTER, scrollPane);
		JPanel s = new JPanel();
		s.setLayout(new FlowLayout(FlowLayout.LEFT));
		s.add(addButton = new JButton("Add..."));
		s.add(editButton = new JButton("Edit..."));
		s.add(deleteButton = new JButton("Delete"));
		addButton.addActionListener(this);
		editButton.addActionListener(this);
		deleteButton.addActionListener(this);
		configPanel.add(BorderLayout.SOUTH, s);
		
		JSplitPane bottomPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		bottomPanel.setDividerLocation(120);
		bottomPanel.setDividerSize(5);
		panel.add(BorderLayout.CENTER, bottomPanel);
		bottomPanel.add(streamerPanel);
		bottomPanel.add(configPanel);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		JPanel arrayPanel = new JPanel(new BorderLayout());
		topPanel.add(BorderLayout.NORTH, recentArrays = new JComboBox());
//		topPanel.add(BorderLayout.CENTER, streamerPanel);
		recentArrays.addActionListener(this);
		panel.add(BorderLayout.NORTH, topPanel);
		
		setRecieverLabels();
		
		return panel;
	}
	
	public JPanel getPanel() {
		return hydrophonePanel;
	}
	
	void enableButtons() {
		// add is always selected. edit and delete are only enabled if a row is selected
		int selRow = hydrophoneTable.getSelectedRow();
		editButton.setEnabled(selRow >= 0);
		deleteButton.setEnabled(selRow >= 0);
		streamerPanel.enableButtons();
	}
	
	public void setParams(PamArray selArray) {
		recentArrays.removeAllItems();		
		if (arrayManager != null) {
			ArrayList<PamArray> arrays = arrayManager.recentArrays;
			for (int i = 0; i < arrays.size(); i++) {
				recentArrays.addItem(arrays.get(i));
			}
		}
		else {
			recentArrays.addItem(selArray);
		}
		if (selArray != null) {
			recentArrays.setSelectedItem(selArray);
		}
		enableButtons();
		setRecieverLabels(); 
		hydrophoneTable.doLayout();
	}

	public boolean getParams() {
		PamArray currentArray = getDialogSelectedArray();
		if (currentArray == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Set the correct type of labels depending on the current medium i.e. if in air or water. 
	 */
	private void setRecieverLabels(){
		
		String reciever = PamController.getInstance().getGlobalMediumManager().getRecieverString(); 
		
		configPanelLabel.setText(reciever + " Elements");
		
		this.streamerPanel.setRecieverLabels();
	}
	
	
	public PamArray getDialogSelectedArray(){
		if (recentArrays == null) return null;
		return (PamArray) recentArrays.getSelectedItem();
	}
	
	private void updateData() {

		PamArray currentArray = getDialogSelectedArray();
		if (currentArray == null) return;
		streamerTableData.fireTableDataChanged();
		
		
		HydrophoneLocatorSystem hs = null;
		if (currentArray.getHydrophoneLocator() != null) {
			hs = HydrophoneLocators.getInstance().getLocatorSystem(currentArray.getHydrophoneLocator().getClass());
		}
//		if (hs != null) {
//			arrayLocators.setSelectedItem(hs);
//		}
		
//		staticPositionPanel.sayData();
		if (locatorDialogPanel != null) {
			locatorDialogPanel.setParams(currentArray);
		}
//		staticTowedPanel.sayData();
		hydrophoneTableData.fireTableDataChanged();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == recentArrays) {
			arrayDialog.newArraySelection();
			updateData();
		}	
		else if (e.getSource() == deleteButton) {
			deleteElement();
		}
		else if (e.getSource() == editButton) {
			editElement();
		}
		else if (e.getSource() == addButton) {
			addElement();
		}
		arrayDialog.newArraySelection();
		hydrophoneTableData.fireTableDataChanged();
		hydrophonePanel.repaint();
//		EnableButtons();		// gets called from ArrayDialog anyway
	}
	
	public void deleteElement() {
		PamArray currentArray = getDialogSelectedArray();
		if (currentArray == null) return;
		int selRow = hydrophoneTable.getSelectedRow();
		if (selRow < 0) return;
		if (JOptionPane.showConfirmDialog(arrayDialog, "Are you sure you want to delete "
				+ PamController.getInstance().getGlobalMediumManager().getRecieverString() + " element " + selRow,
				"? Confirm " + PamController.getInstance().getGlobalMediumManager().getRecieverString() + " Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		currentArray.removeHydrophone(currentArray.getHydrophone(selRow));
		updateData();
	}
	
	public void editElement() {
		arrayDialog.getParams();
		PamArray currentArray = getDialogSelectedArray();
		if (currentArray == null) return;
		int selRow = hydrophoneTable.getSelectedRow();
		if (selRow < 0) return;
		Hydrophone newHydrophone = HydrophoneElementDialog.showDialog(arrayDialog, 
				currentArray.getHydrophone(selRow), false, currentArray);
		if (newHydrophone != null) {
			currentArray.updateHydrophone(selRow, newHydrophone);
			arrayDialog.newArraySelection();
			updateData();
		}
	}
	
	public void addElement() {
		// count existing hydrophones to get an id for the new one
		PamArray currentArray = getDialogSelectedArray();
		// clone the last hydrophone in the list to pick up the same gain and
		// sensitivity data
		int nH = currentArray.getHydrophoneArray().size();
		Hydrophone newHydrophone;
		if (nH > 0) {
			Hydrophone lastHydrophone = currentArray.getHydrophone(nH-1);
			newHydrophone = lastHydrophone.clone();
			newHydrophone.setID(nH);
		}
		else {
			newHydrophone = new Hydrophone(nH);
		}
		newHydrophone = HydrophoneElementDialog.showDialog(arrayDialog, newHydrophone, true, currentArray);
		if (newHydrophone != null) {
			currentArray.addHydrophone(newHydrophone);
			arrayDialog.newArraySelection();
			updateData();
		}
	}
	

//	class ArrayLocatorsChange implements ActionListener {
//		@Override
//		public void actionPerformed(ActionEvent arg0) {
//			arrayLocatorsChange();			
//		}
//	}

//	public void arrayLocatorsChange() {
//		HydrophoneLocatorSystem locatorSystem = (HydrophoneLocatorSystem) arrayLocators.getSelectedItem();
//		setLocatorPanel(locatorSystem.getDialogPanel());
//	}
	
//	class StaticTowedPanel extends JPanel implements ActionListener{
//		JRadioButton towedButton, staticButton;
//
//		public StaticTowedPanel () {
////			setLayout(new GridLayout(1, 2));
//			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
//			setBorder(new EmptyBorder(5, 0, 5, 0));
//			add(staticButton = new JRadioButton("Static array"));
//			add(towedButton = new JRadioButton("Towed array    "));
//			add(new JLabel(" Locator: "));
//			add(arrayLocators = new JComboBox<HydrophoneLocatorSystem>());
//			ButtonGroup g = new ButtonGroup();
//			g.add(towedButton);
//			g.add(staticButton);
//			towedButton.addActionListener(this);
//			staticButton.addActionListener(this);
//			arrayLocators.addActionListener(this);
//			arrayLocators.removeAllItems();
//			for (int i = 0; i < HydrophoneLocators.getInstance().getCount(); i++) {
//				arrayLocators.addItem(HydrophoneLocators.getInstance().getSystem(i));
//			}
//		}
//		
//		public void actionPerformed(ActionEvent e) {
//			if (staticPositionPanel != null) {
//				staticPositionPanel.setVisible(staticButton.isSelected());
//				streamerPanel.setVisible(towedButton.isSelected());
//			}
//			PamArray currentArray = getDialogSelectedArray();
//			if (currentArray != null) {
//				currentArray.setHydrophoneLocator(arrayLocators.getSelectedItem().getClass());
////				currentArray.setArrayType(staticButton.isSelected() ? 
////						PamArray.ARRAY_TYPE_STATIC : PamArray.ARRAY_TYPE_TOWED);
////				currentArray.setArrayLocator(arrayLocators.getSelectedIndex());
//			}
//			arrayDialog.newArraySelection();
//			enableControls();
//		}
//		
//		public void sayData() {
//			PamArray currentArray = getDialogSelectedArray();
//			if (currentArray == null) return;
////			towedButton.setSelected(currentArray.getArrayType() == PamArray.ARRAY_TYPE_TOWED);
////			staticButton.setSelected(currentArray.getArrayType() == PamArray.ARRAY_TYPE_STATIC);
//			arrayLocators.setSelectedItem(HydrophoneLocators.getInstance().
//					getLocatorSystem(currentArray.getArrayLocatorClass()));
//			enableControls();
//		}
//		
////		public boolean getData() {
////			PamArray currentArray = getDialogSelectedArray();
////			if (currentArray != null) {
////				currentArray.setArrayType(staticButton.isSelected() ? 
////						PamArray.ARRAY_TYPE_STATIC : PamArray.ARRAY_TYPE_TOWED);
////				currentArray.setArrayLocator(arrayLocators.getSelectedIndex());
////			}
////			return true;
////		}
//		
//		public void enableControls() {
//			PamArray currentArray = getDialogSelectedArray();
//			arrayLocators.setEnabled(towedButton.isSelected());
//			if (staticButton.isSelected()) {
//				arrayLocators.setSelectedIndex(0);
//			}
//			else {
//				arrayLocators.setSelectedIndex(Math.max(0
//						, arrayLocators.getSelectedIndex()));
//			}
//		}
//	}
	
//	private void setLocatorPanel(LocatorDialogPanel dialogPanel) {
//		locatorPanelContainer.removeAll();
//		if (dialogPanel != null) {
//			locatorPanelContainer.add(BorderLayout.CENTER, dialogPanel.getDialogPanel());
//			dialogPanel.setParams(getDialogSelectedArray());
//		}
//		arrayDialog.invalidate();
//		arrayDialog.validate();
//	}


	class StreamerPanel extends JPanel implements ListSelectionListener {
		private JButton addButton, editButton, removeButton;
		
		private JTable streamerTable;
		
		private JLabel streamerLabel;
		
		public StreamerPanel() {
			super();
			streamerTable = new JTable(streamerTableData);
			streamerTable.getSelectionModel().addListSelectionListener(this);
			streamerTable.addMouseListener(new StreamerMouse());
			for (int i = 0; i < streamerTableData.getColumnCount(); i++) {
				streamerTable.getColumnModel().getColumn(i).setPreferredWidth(streamerColumWidths[i]);
			}
			
			JScrollPane scrollPane = new JScrollPane(streamerTable);
			scrollPane.setPreferredSize(new Dimension(290, 70));
			setLayout(new BorderLayout());
			add(BorderLayout.NORTH, streamerLabel = new JLabel("")); 
			add(BorderLayout.CENTER, scrollPane);
			JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT));
			b.add(addButton = new JButton("Add ..."));
			b.add(editButton = new JButton("Edit ..."));
			b.add(removeButton = new JButton("Delete"));
			addButton.addActionListener(new AddButton());
			editButton.addActionListener(new EditButton());
			removeButton.addActionListener(new RemoveButton());
			addButton.setToolTipText("Add a new streamer, buoy or cluster of hydrophones");
			editButton.setToolTipText("Edit a new streamer, buoy or cluster of hydrophones");
			addButton.setToolTipText("<html> Remove new streamer, buoy or cluster of hydrophones<p>"+
			"Note that this option is only enabled after associated hydrophones have been removed</html>");
			add(BorderLayout.SOUTH, b);
			enableButtons();
			
			setRecieverLabels();
		}
		
		private void dataChanged() {
			enableButtons();
			streamerTableData.fireTableDataChanged();
			arrayDialog.newArraySelection();
		}
		
		private void enableButtons() {
			int row = streamerTable.getSelectedRow();
			editButton.setEnabled(row >= 0);
			PamArray currArray = getDialogSelectedArray();
			int nH = 0;
			if (currArray != null) {
				nH = currArray.getStreamerHydrophoneCount(row);
			}
			removeButton.setEnabled(row >= 0 && streamerTableData.getRowCount() > 1 && nH == 0);
		}

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			enableButtons();
		}
		private void addStreamer() {
			PamArray currentArray = getDialogSelectedArray();
			int nStreamers = currentArray.getNumStreamers();
			Streamer streamer = new Streamer(nStreamers);
			streamer.setupLocator(currentArray);
			streamer = StreamerDialog.showDialog(arrayDialog, currentArray, streamer);
			if (streamer != null) {
				streamer.setupLocator(currentArray);
				streamer.makeStreamerDataUnit();
				currentArray.addStreamer(streamer);
				dataChanged();
			}
			
		}
		private void editStreamer() {
			int row = streamerTable.getSelectedRow();
			if (row < 0) {
				return;
			}
			arrayDialog.getParams();
			PamArray currentArray = getDialogSelectedArray();
			Streamer oldStreamer = currentArray.getStreamer(row);
			Streamer streamer = StreamerDialog.showDialog(arrayDialog, currentArray, oldStreamer);
			if (streamer != null) {
				streamer.setupLocator(currentArray);
				streamer.makeStreamerDataUnit();
				currentArray.updateStreamer(row, streamer);
				dataChanged();
			}
		}
		private void removeStreamer() {
			int row = streamerTable.getSelectedRow();
			if (row < 0) {
				return;
			}
			PamArray currentArray = getDialogSelectedArray();
			currentArray.removeStreamer(row);
			dataChanged();
		}
		class AddButton implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addStreamer();
			}
		}
		class EditButton implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				editStreamer();
			}
		}
		class RemoveButton implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				removeStreamer();
			}
		}
		
		class StreamerMouse extends MouseAdapter {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					editStreamer();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					streamerPopup(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					streamerPopup(e);
				}
			}

			
			
		}

		private void streamerPopup(MouseEvent e) {
			int row = streamerTable.getSelectedRow();
			if (row < 0) {
				return;
			}
			PamArray currentArray = getDialogSelectedArray();
			Streamer oldStreamer = currentArray.getStreamer(row);
			if (oldStreamer == null) {
				return;
			}
			JPopupMenu pm = new JPopupMenu();
			JMenuItem mi = new JMenuItem("Edit ...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editStreamer();
				}
			});
			pm.add(mi);
			mi = new JMenuItem("Delete ...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeStreamer();
				}
			});
			pm.add(mi);
			mi = new JMenuItem("Clone");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					coneStreamer(oldStreamer, true);
				}
			});
			pm.add(mi);
			pm.show(e.getComponent(), e.getX(), e.getY());
		}	

		private void coneStreamer(Streamer oldStreamer, boolean phonesToo) {
			Streamer newStreamer = oldStreamer.clone();
			PamArray currentArray = getDialogSelectedArray();
			newStreamer.setStreamerIndex(currentArray.getNumStreamers());
			int streamerId = currentArray.addStreamer(newStreamer);
			/**
			 * and the hydrophones ...
			 */
			if (!phonesToo) {
				return;
			}
			int nPhones = currentArray.getHydrophoneCount();
			int phoneId = nPhones;
			for (int i = 0; i < nPhones; i++) {
				Hydrophone phone = currentArray.getHydrophone(i);
				if (phone.getStreamerId() == oldStreamer.getStreamerIndex()) {
					Hydrophone newPhone = phone.clone();
					newPhone.setID(phoneId++);
					newPhone.setStreamerId(streamerId);
					currentArray.addHydrophone(newPhone);
				}
			}
			streamerTableData.fireTableDataChanged();
			hydrophoneTableData.fireTableDataChanged();
		}
		
		/**
		 * Set the correct type of labels depending on the current medium i.e. if in air or water. 
		 */
		private void setRecieverLabels(){

			String reciever = PamController.getInstance().getGlobalMediumManager().getRecieverString(); 

			streamerLabel.setText(reciever + " Streamers");

			hydrophoneColumns[3] = PamController.getInstance().getGlobalMediumManager().getZString(false); 
			hydrophoneColumns[6] = PamController.getInstance().getGlobalMediumManager().getZString(false) + "Err"; 
			if (hydrophoneTableData!=null) {
				hydrophoneTableData.fireTableStructureChanged();
			}
		} 
	}
	
	class StreamerTableData extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			if (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER) {
				return streamerColumns.length;
			}
			else {
				return streamerColumns.length;
			}
		}

		@Override
		public String getColumnName(int iCol) {	
			return streamerColumns[iCol];
		}

		@Override
		public int getRowCount() {
			PamArray currentArray = getDialogSelectedArray();
			if (currentArray == null) return 0;
			return currentArray.getNumStreamers();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			PamArray currentArray = getDialogSelectedArray();
			if (currentArray == null) return null;
			Streamer streamer = currentArray.getStreamer(iRow);
			MasterLocator masterLocator = currentArray.getMasterLocator();
			HydrophoneLocator locator = streamer.getHydrophoneLocator(); //masterLocator.locatorForStreamer(iRow);
			switch (iCol) {
			case 0:
				return iRow;
			case 1:
				return streamer.getStreamerName();
			case 2:
				return streamer.getX();
			case 3:
				return streamer.getY();
			case 4:
				return -streamer.getZ();
			case 5:
				if (masterLocator == null) {
					return "No Master Locator";
				}
				HydrophoneOriginMethod horigin = streamer.getHydrophoneOrigin();
				if (horigin == null) {
					return "no origin method";
				}
				return horigin.getName();
			case 6:
				if (masterLocator == null) {
					return "No Master Locator";
				}
				if (locator == null) {
					return "No streamer locator";
				}
				return locator.getName();
								
//				return streamer.getDx();
//			case 5:
//				return streamer.getDy();
//			case 6:
//				return streamer.getDz();
//			case 7:
//				return streamer.getBuoyId1();
			}
			return null;
		}
		
	}

	class HydrophoneMouse extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				editElement();
			}
		}
	}
	
	

	class HydrophoneTableData extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return hydrophoneColumns.length;
		}
		

		@Override
		public int getRowCount() {
			PamArray currentArray = getDialogSelectedArray();
			if (currentArray == null) return 0;
			return currentArray.getHydrophoneArray().size();
		}

		@Override
		public String getColumnName(int column) {
			return hydrophoneColumns[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			PamArray array = getDialogSelectedArray();
			Hydrophone hydrophone = array.getHydrophone(rowIndex);
			
			switch (columnIndex) {
			case 0:
				return rowIndex;
			case 1:
				return hydrophone.getX();
			case 2:
				return hydrophone.getY();
			case 3:
				return PamController.getInstance().getGlobalMediumManager().getZCoeff()*hydrophone.getZ();
			case 4:
				return hydrophone.getdX();
//				double[] bw = hydrophone.getBandwidth();
//				return String.format("%.1f-%.1f kHz", bw[0]/1000., bw[1]/1000.);
			case 5:
				return hydrophone.getdY();
			case 6:
				return hydrophone.getdZ();
			case 7:
				return hydrophone.getStreamerId();
			}
			return null;
		}
		
	}
	
	public int[] getHydrophoneMap() {
		return hydrophoneMap;
	}

	public void setHydrophoneMap(int[] hydrophoneMap) {
		this.hydrophoneMap = hydrophoneMap;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		enableButtons();		
	}
}
