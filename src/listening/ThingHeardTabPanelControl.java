package listening;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.ScrollPaneAddon;
import Array.ArrayManager;
import Array.PamArray;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamTabPanel;
import PamView.PamTable;
import PamView.dialog.PamButton;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.PamRadioButton;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamDataBlock;

public class ThingHeardTabPanelControl implements PamTabPanel {


	private ListeningControl listeningControl;

	private ListeningTabPanel listeningTabPanel;

	private int runMode;

	public ThingHeardTabPanelControl(ListeningControl listeningControl) {
		super();
		runMode = PamController.getInstance().getRunMode();
		this.listeningControl = listeningControl;
		listeningTabPanel = new ListeningTabPanel();
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		return null;
	}

	@Override
	public JComponent getPanel() {
		return listeningTabPanel;
	}

	@Override
	public JToolBar getToolBar() {
		return null;
	}

	protected void newSettings() {
		listeningTabPanel.fillPanels();
		buttonsPanel.invalidate();
	}

	protected EffortPanel effortPanel;
	protected ButtonsPanel buttonsPanel;
	protected HistoryPanel historyPanel;
	class ListeningTabPanel extends PamBorderPanel {

		ListeningTabPanel() {
			effortPanel = new EffortPanel();
			buttonsPanel = new ButtonsPanel();
			historyPanel = new HistoryPanel();
			PamBorderPanel topPanel = new PamBorderPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
			topPanel.add(effortPanel);
			topPanel.add(buttonsPanel);
			setLayout(new BorderLayout());
			if (runMode != PamController.RUN_PAMVIEW) {
				add(BorderLayout.NORTH, topPanel);
			}
			add(BorderLayout.CENTER, historyPanel);

			fillPanels();
			//			pack();
		}

		protected void fillPanels() {
			effortPanel.createPanel();
			buttonsPanel.createPanel();
		}
	}

	class EffortPanel extends PamBorderPanel implements ActionListener {

		private JLabel currentStatus, statusTime, statusDuration;

		private JCheckBox[] hydrophoneBoxes;

		javax.swing.Timer t;

		public EffortPanel() {
			super();
			setBorder(new TitledBorder("Effort"));
			t = new Timer(1000, this);
			t.start();
		}

		protected void createPanel() {
			removeAll();
			PamArray array = ArrayManager.getArrayManager().getCurrentArray();
			int nPhones = array.getHydrophoneCount();
			hydrophoneBoxes = new JCheckBox[nPhones];

			ButtonGroup buttonGroup = new ButtonGroup();
			PamRadioButton button;
			ListeningParameters par = listeningControl.listeningParameters;
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridwidth = nPhones + 1;
			for (int i = 0; i < par.effortStati.size(); i++) {
				PamDialog.addComponent(this, button = new PamRadioButton(par.effortStati.get(i)), c);
				buttonGroup.add(button);
				button.setFont(PamColors.getInstance().getBoldFont());
				button.addActionListener(new EffortListener(i));
				c.gridy++;
			}
			c.gridx = 0;
			c.gridwidth = 1;
			PamLabel label;
			PamDialog.addComponent(this, label = new PamLabel("Hydrophones Monitored : "), c);
			label.setFont(PamColors.getInstance().getBoldFont());
			for (int i = 0; i < nPhones; i++) {
				c.gridx++;
				PamDialog.addComponent(this, hydrophoneBoxes[i] = 
					new PamCheckBox(String.format(" %d", i)), c);
				hydrophoneBoxes[i].setSelected((par.hydrophones & 1<<i) != 0);
				hydrophoneBoxes[i].setFont(PamColors.getInstance().getBoldFont());
			}
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = nPhones + 1;
			currentStatus = new JLabel("Current Status unknown");
			currentStatus.setFont(PamColors.getInstance().getBoldFont());
			PamDialog.addComponent(this, currentStatus, c);
			c.gridy++;
			statusTime = new JLabel("  ");
			statusTime.setFont(PamColors.getInstance().getBoldFont());
			PamDialog.addComponent(this, statusTime, c);
			c.gridy++;
			statusDuration = new JLabel("  ");
			statusDuration.setFont(PamColors.getInstance().getBoldFont());
			PamDialog.addComponent(this, statusDuration, c);
		}

		protected void setStatus(int index) {
			currentStatus.setText("Current status : " + 
					listeningControl.listeningParameters.effortStati.get(index));
			sayStatusTime();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			sayStatusTime();
		}

		private void sayStatusTime() {
			ListeningEffortData lastEffort = listeningControl.listeningProcess.lastEffort;
			if (lastEffort == null) {
				return;
			}
			long time = PamCalendar.getTimeInMillis() - lastEffort.getTimeMilliseconds();
			//			time += 3600 * 25 * 1000;
			statusTime.setText(String.format(
					"Since %s",
					PamCalendar.formatDateTime(lastEffort.getTimeMilliseconds())));
			statusDuration.setText(String.format(
					"Duration %s",
					PamCalendar.formatDuration(time)));	
		}

		protected int getHydrophones() {
			int phones = 0;
			for (int i = 0; i < hydrophoneBoxes.length; i++) {
				if (hydrophoneBoxes[i].isSelected()) {
					phones |= (1<<i);
				}
			}
			return phones;
		}

	}

	class ButtonsPanel extends PamBorderPanel {

		JTextField comment;
		public ButtonsPanel() {
			super();
			setBorder(new TitledBorder("Things Heard"));
		}
		protected void createPanel() {
			removeAll();
			ListeningParameters par = listeningControl.listeningParameters;
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			PamDialog.addComponent(this, new PamLabel("Species / type"), c);
			c.gridx ++;
			c.gridwidth = par.nVolumes + 1;
			PamDialog.addComponent(this, new PamLabel("Volume"), c);
			c.gridwidth = 1;
			PamButton button;
			PamLabel label;
			c.anchor = GridBagConstraints.EAST;
			c.fill = 0;
			for (int iSpecies = 0; iSpecies < par.speciesList.size(); iSpecies++) {
				c.gridx = 0;
				c.gridy++;

				PamDialog.addComponent(this, label = new PamLabel(par.speciesList.get(iSpecies).getName()), c);
				label.setFont(PamColors.getInstance().getBoldFont());
				for (int i = 0; i <= par.nVolumes; i++) {
					c.gridx++;
					PamDialog.addComponent(this, button = new PamButton(String.format("%d", i)), c);
					button.setFont(PamColors.getInstance().getBoldFont());
					button.addActionListener(new ButtonListener(iSpecies, i));
				}

			}
			c.gridx++;
			c.anchor = GridBagConstraints.EAST;
			c.fill = 0;
			PamDialog.addComponent(this, label = new PamLabel("Hit Enter to store comment"), c);
			c.gridy++;
			c.gridx =0;
			PamDialog.addComponent(this, label = new PamLabel("Comment:"), c);
			label.setFont(PamColors.getInstance().getBoldFont());
			c.gridx++;
			c.gridwidth=par.nVolumes+2;
			c.fill = GridBagConstraints.HORIZONTAL;
			PamDialog.addComponent(this, comment = new JTextField(ListeningControl.COMMENT_LENGTH), c);
			comment.addActionListener(new CommentHit());
		}
		String getComment() {
			String str = comment.getText();
			comment.setText("");
			return str;
		}
		class CommentHit implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				listeningControl.buttonPress(-1, -1, effortPanel.getHydrophones(), buttonsPanel.getComment());
				historyPanel.dataChanged();
			}

		}
	}

	class HistoryPanel extends PamBorderPanel {

		ThingsHeardTableData tableData;
		JTable table;
		public HistoryPanel() {
			super();
			setBorder(new TitledBorder("History"));
			setLayout(new GridLayout(1,0));
			JPanel innerPanel = new JPanel(new BorderLayout());
			tableData = new ThingsHeardTableData();
			table = new PamTable(tableData);
			table.setFont(PamColors.getInstance().getBoldFont());
			int[] tableWidths = tableData.getColumnWidths();
			for (int i = 0; i < tableWidths.length; i++) {
				table.getColumnModel().getColumn(i).setPreferredWidth(tableWidths[i]);
			}
			JScrollPane scrollPanel = new JScrollPane(table);
			if (runMode == PamController.RUN_PAMVIEW) {
				ScrollPaneAddon scrollPaneAddon = new ScrollPaneAddon(scrollPanel, 
						"Things Heard", AbstractPamScrollerAWT.HORIZONTAL, 1000, 3600*24*1000, true);
//				scrollPanel.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER , scrollPaneAddon.getButtonPanel());
				scrollPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				JPanel riP = new PamBorderPanel(new BorderLayout());
				riP.add(BorderLayout.EAST, scrollPaneAddon.getButtonPanel());
				innerPanel.add(BorderLayout.NORTH, riP);
				scrollPaneAddon.addDataBlock(listeningControl.listeningProcess.heardDataBlock);
				scrollPaneAddon.addDataBlock(listeningControl.listeningProcess.effortDataBlock);
				scrollPaneAddon.addObserver(new ScrollObserver());
			}
			innerPanel.add(BorderLayout.CENTER, scrollPanel);
			this.add(innerPanel);

		}
		protected void dataChanged() {
			tableData.fireTableDataChanged();
		}
		protected void createPanel() {
			//			removeAll();

		}
	}

	class ButtonListener implements ActionListener {

		private int speciesIndex;
		private int volume;

		public ButtonListener(int speciesIndex, int volume) {
			super();
			this.speciesIndex = speciesIndex;
			this.volume = volume;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			listeningControl.buttonPress(speciesIndex, volume, effortPanel.getHydrophones(), buttonsPanel.getComment());
			historyPanel.dataChanged();
		}

	}
	
	class ScrollObserver implements PamScrollObserver {

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			
		}

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			// TODO Auto-generated method stub
			
		}
		
	}

	class EffortListener implements ActionListener {

		private int effortIndex;

		public EffortListener(int effortIndex) {
			super();
			this.effortIndex = effortIndex;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			effortPanel.setStatus(effortIndex);
			listeningControl.effortButton(effortIndex, effortPanel.getHydrophones());

		}
	}
	private PamDataBlock<ThingHeard> heardDataBlock = null;
	private PamDataBlock<ThingHeard> findDataBlock() {
		if (heardDataBlock == null) {
			heardDataBlock = listeningControl.listeningProcess.heardDataBlock;
		}
		return heardDataBlock;
	}

	class ThingsHeardTableData extends AbstractTableModel {

		String[] tableColumns = {"Time", "Species", "Volume", "Comment"};

		int[] columnWidths = {50, 20, 5, 250};

		public int[] getColumnWidths() {
			return columnWidths;
		}

		@Override
		public int getColumnCount() {
			return tableColumns.length;
		}

		@Override
		public String getColumnName(int col) {
			return tableColumns[col];
		}

		@Override
		public int getRowCount() {
			if (findDataBlock() == null) {
				return 0;
			}
			return heardDataBlock.getUnitsCount();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (findDataBlock() == null) {
				return null;
			}
			synchronized (heardDataBlock.getSynchLock()) {
				int row = rowIndex;
				if (runMode != PamController.RUN_PAMVIEW) {
					row = getRowCount() - rowIndex - 1;
				}
				if (row < 0 || row > heardDataBlock.getUnitsCount()) {
					return null;
				}
				ThingHeard thingHeard = heardDataBlock.getDataUnit(row, PamDataBlock.REFERENCE_CURRENT);
				switch (columnIndex) {
				case 0:
					return PamCalendar.formatDateTime(thingHeard.getTimeMilliseconds());
				case 1:
					if (thingHeard.getSpeciesItem() != null) {
						return thingHeard.getSpeciesItem().getName();
					}
					break;
				case 2:
					if (thingHeard.getSpeciesItem() != null) {
						return String.format("%d", thingHeard.getVolume());
					}
					break;
				case 3:
					return thingHeard.getComment();
				}
			}
			return null;
		}

	}
}
