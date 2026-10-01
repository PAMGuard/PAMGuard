package difar.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.PamColors.PamColor;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import difar.DIFARMessage;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.DifarMatchSelector;

/**
 * Shows which detections on other buoys could have been the same call as the
 * one being looked at, and what happened to each of them.
 * <p>
 * Every combination the matching tried is listed, best first. The one that was
 * used is highlighted. Others that fit, such as the pairs inside a triplet
 * that was used, are marked as fitting but not used. The rest carry the reason
 * they were not used, which is usually that the arrival times do not fit where
 * the bearings cross.
 * <p>
 * The table shows the matching as it would happen now, with the settings now in
 * force. It is not a record of what happened when the data were collected.
 */
public class DifarMatchPanel extends PamPanel implements DIFARDisplayUnit {

	private static final long serialVersionUID = 1L;

	private final DifarControl difarControl;

	private final MatchTableModel tableModel = new MatchTableModel();

	private final JTable table = new JTable(tableModel);

	private final DifarMatchClipStrip clipStrip;

	/** The detection whose candidates are shown, or null. */
	private DifarDataUnit currentUnit;

	public DifarMatchPanel(DifarControl difarControl) {
		super(PamColor.BORDER);
		this.difarControl = difarControl;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder("Triangulation match selector"));

		clipStrip = new DifarMatchClipStrip(difarControl);
		JScrollPane stripScroller = new JScrollPane(clipStrip,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		stripScroller.setBorder(BorderFactory.createEmptyBorder());
		add(BorderLayout.NORTH, stripScroller);

		table.setDefaultRenderer(Object.class, new MatchCellRenderer());
		table.setFillsViewportHeight(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				chooseSelectedMatch();
			}
		});
		add(BorderLayout.CENTER, new JScrollPane(table));
	}

	/**
	 * Use the match in the selected row for this detection, in place of the one
	 * chosen automatically. Saving the clip then saves this match. Choosing a
	 * rejected group is allowed: the reason it was rejected stays on show.
	 */
	private void chooseSelectedMatch() {
		if (currentUnit == null) {
			return;
		}
		int row = table.getSelectedRow();
		if (row < 0) {
			return;
		}
		DifarMatchSelector.Match match = tableModel.getMatch(table.convertRowIndexToModel(row));
		if (match == null) {
			return;
		}
		difarControl.getDifarProcess().applyMatch(currentUnit, match);
		tableModel.setUsed(match);
		updateClipStrip();
	}

	/**
	 * Show the clips of the match in use, then the other candidates.
	 */
	private void updateClipStrip() {
		List<PamDataUnit> matched = tableModel.used == null
				? Collections.emptyList() : tableModel.used.getUnits();
		clipStrip.show(currentUnit, matched, tableModel.getCandidateUnits());
	}

	/**
	 * Size each column to its contents, leaving the status column to take up
	 * whatever is left.
	 */
	private void sizeColumns() {
		int lastColumn = table.getColumnCount() - 1;
		for (int column = 0; column <= lastColumn; column++) {
			TableColumn tableColumn = table.getColumnModel().getColumn(column);
			int width = table.getTableHeader().getDefaultRenderer()
					.getTableCellRendererComponent(table, tableColumn.getHeaderValue(),
							false, false, 0, column).getPreferredSize().width;
			for (int row = 0; row < table.getRowCount(); row++) {
				Component cell = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
				width = Math.max(width, cell.getPreferredSize().width);
			}
			width += 10;
			tableColumn.setMinWidth(Math.min(width, 60));
			tableColumn.setPreferredWidth(width);
			tableColumn.setWidth(width);
		}
		table.doLayout();
	}

	@Override
	public String getName() {
		return difarControl.getUnitName() + " match candidates";
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public int difarNotification(DIFARMessage difarMessage) {
		/*
		 * A unit is matched as part of processing it, so its candidates are
		 * ready once the demultiplexing has finished. That covers both clicking
		 * a clip in viewer mode and processing one normally.
		 */
		if (difarMessage.message == DIFARMessage.DemuxComplete) {
			showUnit(difarMessage.difarDataUnit);
		}
		return 0;
	}

	/**
	 * Show the candidates for a detection.
	 * @param unit the detection, or null to clear the table.
	 */
	public void showUnit(PamDataUnit unit) {
		List<DifarMatchSelector.Match> matches = unit == null
				? null : difarControl.getDifarProcess().getMatchLog().get(unit);
		currentUnit = unit instanceof DifarDataUnit ? (DifarDataUnit) unit : null;
		SwingUtilities.invokeLater(() -> {
			tableModel.setMatches(matches);
			sizeColumns();
			updateClipStrip();
		});
	}

	/** Rows are candidate groups, best first. */
	private class MatchTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private final String[] columns = {"Buoys", "Match candidates", "Bearing (deg)",
				"Timing (s)", "Chi2/dof", "Status"};

		private List<DifarMatchSelector.Match> matches;

		/** The match that was used, or null. Only this one is marked used. */
		private DifarMatchSelector.Match used;

		void setMatches(List<DifarMatchSelector.Match> matches) {
			this.matches = matches;
			this.used = DifarMatchSelector.chooseMatch(matches);
			fireTableDataChanged();
		}

		/** Mark a match as the one in use, after the user has picked it. */
		void setUsed(DifarMatchSelector.Match used) {
			this.used = used;
			fireTableDataChanged();
		}

		boolean isUsed(DifarMatchSelector.Match match) {
			return match != null && match == used;
		}

		/** @return every candidate detection, over all groups, once each. */
		List<PamDataUnit> getCandidateUnits() {
			List<PamDataUnit> units = new ArrayList<>();
			if (matches != null) {
				for (DifarMatchSelector.Match match : matches) {
					for (PamDataUnit unit : match.getUnits()) {
						if (!units.contains(unit)) {
							units.add(unit);
						}
					}
				}
			}
			return units;
		}

		DifarMatchSelector.Match getMatch(int row) {
			if (matches == null || row < 0 || row >= matches.size()) {
				return null;
			}
			return matches.get(row);
		}

		@Override
		public int getRowCount() {
			return matches == null ? 0 : matches.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Object getValueAt(int row, int column) {
			DifarMatchSelector.Match match = getMatch(row);
			if (match == null) {
				return null;
			}
			switch (column) {
			case 0:
				return match.getUnits().size();
			case 1:
				return otherDetections(match);
			case 2:
				return format(match.getResiduals().getMaxBearingErrorDegrees(), 1);
			case 3:
				return format(match.getResiduals().getMaxTimeDelayErrorSeconds(), 2);
			case 4:
				return format(match.getChi2PerDegreeOfFreedom(), 2);
			case 5:
				if (isUsed(match)) {
					return "used";
				}
				return match.isAccepted() ? "fits, not used" : match.getRejectReason();
			}
			return null;
		}

		/** @return the other detections of a group, as channel and time. */
		private String otherDetections(DifarMatchSelector.Match match) {
			StringBuilder text = new StringBuilder();
			for (int i = 1; i < match.getUnits().size(); i++) {
				PamDataUnit unit = match.getUnits().get(i);
				if (text.length() > 0) {
					text.append(", ");
				}
				text.append(String.format("ch%d %s",
						PamUtils.getSingleChannel(unit.getChannelBitmap()),
						PamCalendar.formatTime(unit.getTimeMilliseconds(), true)));
			}
			return text.toString();
		}

		private String format(double value, int decimals) {
			if (Double.isNaN(value)) {
				return "-";
			}
			return String.format("%." + decimals + "f", value);
		}
	}

	/**
	 * Marks the group that was used in bold, so it stands out. Colours are left
	 * to the table, which follows whatever look and colour scheme is in use.
	 */
	private class MatchCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			Component component = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
			DifarMatchSelector.Match match = tableModel.getMatch(table.convertRowIndexToModel(row));
			boolean used = tableModel.isUsed(match);
			component.setFont(component.getFont().deriveFont(used ? Font.BOLD : Font.PLAIN));
			return component;
		}
	}
}
