package annotationMark.spectrogram;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.component.PamFontIcon;
import PamView.component.PamFontIcon.IconColour;
import PamView.component.PamSettingsIconButton;
import PamView.dialog.warn.WarnOnce;
import PamView.tables.SortableTableValue;
import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.calcs.measurement.SoundMeasurement;
import annotation.calcs.measurement.SoundMeasurementAnnotation;
import annotation.calcs.measurement.SoundMeasurementAnnotationType;
import annotation.calcs.snr.SNRAnnotation;
import annotation.calcs.snr.SNRAnnotationType;
import annotation.calcs.spl.SPLAnnotation;
import annotation.calcs.spl.SPLAnnotationType;
import annotation.string.StringAnnotation;
import annotation.string.StringAnnotationType;
import annotationMark.MarkDataBlock;
import annotationMark.MarkDataUnit;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractScrollManager;
import userDisplay.UserDisplayComponentAdapter;

/**
 * User display showing a table of spectrogram annotations with a column for each of
 * their measurements and annotations, much like a selection table. Columns can be
 * sorted by clicking on their headers. Double click a row to scroll the displays to the
 * annotation, or right click for options to edit, re-measure, play and delete it. The
 * table can be exported to a csv file.
 * <p>
 * In viewer mode the table only shows annotations in the currently loaded period, and
 * scrolling to an annotation only moves the displays within that period. The page
 * buttons move the loaded period, as on the displays' own scroll bars.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramAnnotationTable extends UserDisplayComponentAdapter {

	private SpectrogramAnnotationModule annotationModule;

	private MarkDataBlock dataBlock;

	private JPanel mainPanel;

	private JTable table;

	private AnnotationTableModel tableModel;

	private JLabel countLabel;

	/**
	 * Shows the loaded period in viewer mode.
	 */
	private JLabel periodLabel;

	private SwingTableColumnWidths widthManager;

	private DataObserver dataObserver;

	/**
	 * Copy of the annotations shown in the table.
	 */
	private ArrayList<MarkDataUnit> dataCopy = new ArrayList<>();

	/**
	 * The current columns. Rebuilt whenever the annotation choices change.
	 */
	private ArrayList<TableColumnDef> columns = new ArrayList<>();

	/**
	 * Column definition - a name, a type and how to get the value from an annotation.
	 */
	private static class TableColumnDef {
		String name;
		Class<?> type;
		Function<MarkDataUnit, Object> value;

		TableColumnDef(String name, Class<?> type, Function<MarkDataUnit, Object> value) {
			this.name = name;
			this.type = type;
			this.value = value;
		}
	}

	public SpectrogramAnnotationTable(SpectrogramAnnotationModule annotationModule) {
		this.annotationModule = annotationModule;
		this.dataBlock = annotationModule.getAnnotationDataBlock();

		tableModel = new AnnotationTableModel();
		table = new JTable(tableModel);
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setToolTipText("Click a column header to sort the table");
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// the delete key deletes the selected annotations (after asking).
		table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAnnotations");
		table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteAnnotations");
		table.getActionMap().put("deleteAnnotations", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteUnits(getSelectedUnits());
			}
		});
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.addMouseListener(new TableMouse());

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, new JScrollPane(table));

		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		controlPanel.setBorder(new TitledBorder("Annotations"));
		JButton reMeasureAll = new JButton("Recalculate all");
		reMeasureAll.setToolTipText("Re-measure all the annotations in the table from the raw sound data");
		reMeasureAll.addActionListener((e) -> reMeasureAll());
		JButton export = new JButton("Export csv ...");
		export.setToolTipText("Export the table to a comma separated values file");
		export.addActionListener((e) -> exportCSV());
		controlPanel.add(reMeasureAll);
		controlPanel.add(export);
		JButton delete = new JButton("Delete");
		delete.setToolTipText("Delete the selected annotations from the display and database (or press the Delete key)");
		delete.addActionListener((e) -> deleteUnits(getSelectedUnits()));
		controlPanel.add(delete);
		controlPanel.add(countLabel = new JLabel());
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(BorderLayout.CENTER, controlPanel);
		if (isViewer()) {
			southPanel.add(BorderLayout.EAST, createNavigationPanel());
		}
		mainPanel.add(BorderLayout.SOUTH, southPanel);

		dataBlock.addObserver(dataObserver = new DataObserver());

		updateTableData();
	}

	/**
	 * The loaded period and the page back, load options and page forward buttons, like
	 * those on the viewer scroll bars.
	 */
	private JPanel createNavigationPanel() {
		JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		navPanel.setBorder(new TitledBorder("Viewer data"));
		JButton pageBack = new JButton(PamFontIcon.of(MaterialDesignR.REWIND, PamSettingsIconButton.NORMAL_SIZE, IconColour.ACCENT));
		pageBack.setToolTipText("Move loaded data back");
		pageBack.addActionListener((e) -> page(false));
		JButton pageForward = new JButton(PamFontIcon.of(MaterialDesignF.FAST_FORWARD, PamSettingsIconButton.NORMAL_SIZE, IconColour.ACCENT));
		pageForward.setToolTipText("Move loaded data forward");
		pageForward.addActionListener((e) -> page(true));
		JButton loadOptions = new JButton(PamFontIcon.of(MaterialDesignM.MENU_DOWN, PamSettingsIconButton.NORMAL_SIZE));
		loadOptions.setToolTipText("<html>Scroll and data loading options: "
				+ "Left click for scroll time, Right for other options</html>");
		loadOptions.addActionListener((e) -> showLoadOptions(loadOptions));
		loadOptions.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showLoadMenu(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				showLoadMenu(e);
			}
		});
		periodLabel = new JLabel(" ");
		navPanel.add(periodLabel);
		navPanel.add(pageBack);
		navPanel.add(loadOptions);
		navPanel.add(pageForward);
		return navPanel;
	}

	/**
	 * Open the scroll bars' load options dialog (start time, duration, page step ...).
	 */
	private void showLoadOptions(JComponent button) {
		AbstractPamScroller scroller = getLoadScroller();
		if (scroller != null) {
			scroller.showLoadOptionsDialog(SwingUtilities.getWindowAncestor(mainPanel), button);
		}
	}

	/**
	 * The scroll bars' standard options menu, on a right click of the load options button.
	 */
	private void showLoadMenu(MouseEvent e) {
		if (!e.isPopupTrigger()) {
			return;
		}
		AbstractPamScroller scroller = getLoadScroller();
		if (scroller == null) {
			return;
		}
		JPopupMenu menu = AbstractScrollManager.getScrollManager().getStandardOptionsMenu(scroller);
		if (menu != null) {
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private static boolean isViewer() {
		return PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
	}

	/**
	 * Get a scroller whose range is the loaded period. They all move together, so prefer
	 * one showing the annotations, otherwise take the first.
	 * @return scroller, or null if there are none (e.g. not viewer mode).
	 */
	private AbstractPamScroller getLoadScroller() {
		if (!isViewer()) {
			return null;
		}
		List<AbstractPamScroller> scrollers = AbstractScrollManager.getScrollManager().getPamScrollers();
		if (scrollers == null || scrollers.isEmpty()) {
			return null;
		}
		for (AbstractPamScroller scroller : scrollers) {
			if (scroller.isDataBlockUsed(dataBlock)) {
				return scroller;
			}
		}
		return scrollers.get(0);
	}

	/**
	 * Move the loaded period on (or back) a page. The scroll manager moves the other
	 * scrollers to match and loads the data, after which the table updates.
	 */
	private void page(boolean forward) {
		AbstractPamScroller scroller = getLoadScroller();
		if (scroller == null) {
			return;
		}
		if (forward) {
			scroller.pageForward();
		}
		else {
			scroller.pageBack();
		}
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}

	@Override
	public String getFrameTitle() {
		return annotationModule.getUnitName() + " table";
	}

	@Override
	public void closeComponent() {
		dataBlock.deleteObserver(dataObserver);
		super.closeComponent();
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.DATA_LOAD_COMPLETE:
		case PamControllerInterface.OFFLINE_DATA_LOADED:
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			updateLater();
		}
	}

	@Override
	public void setUniqueName(String uniqueName) {
		widthManager = new SwingTableColumnWidths(uniqueName, table);
		super.setUniqueName(uniqueName);
	}

	/**
	 * Called when the annotation settings change, which may change the columns.
	 */
	public void settingsChanged() {
		updateLater();
	}

	/**
	 * Update the table on the Swing thread. Annotations can be added from the JavaFX
	 * thread (marks made on an FX display), so updates are always queued.
	 */
	private void updateLater() {
		SwingUtilities.invokeLater(() -> updateTableData());
	}

	/**
	 * Re-read the annotations from the data block, rebuilding the columns if the
	 * annotation choices have changed.
	 */
	private void updateTableData() {
		ArrayList<TableColumnDef> newColumns = createColumns();
		boolean structureChanged = !sameColumns(newColumns);
		if (structureChanged && widthManager != null) {
			// read the widths while the model still has the old columns, so it matches
			// the table's column model.
			widthManager.getColumnWidths();
		}
		columns = newColumns;

		// in viewer mode, only the annotations in the loaded period.
		AbstractPamScroller scroller = getLoadScroller();
		long loadStart = scroller == null ? Long.MIN_VALUE : scroller.getMinimumMillis();
		long loadEnd = scroller == null ? Long.MAX_VALUE : scroller.getMaximumMillis();
		ArrayList<MarkDataUnit> newData = new ArrayList<>();
		synchronized (dataBlock.getSynchLock()) {
			ListIterator<MarkDataUnit> it = dataBlock.getListIterator(0);
			while (it.hasNext()) {
				MarkDataUnit unit = it.next();
				if (unit.getTimeMilliseconds() >= loadStart && unit.getTimeMilliseconds() <= loadEnd) {
					newData.add(unit);
				}
			}
		}
		Collections.sort(newData);
		dataCopy = newData;

		if (structureChanged) {
			tableModel.fireTableStructureChanged();
			if (widthManager != null) {
				widthManager.setColumnWidths();
			}
		}
		else {
			tableModel.fireTableDataChanged();
		}
		countLabel.setText(String.format("  %d annotations", dataCopy.size()));
		if (periodLabel != null) {
			periodLabel.setText(scroller == null ? " " : String.format("%s to %s",
					PamCalendar.formatDBDateTime(loadStart), PamCalendar.formatDBDateTime(loadEnd)));
		}
	}

	private boolean sameColumns(ArrayList<TableColumnDef> newColumns) {
		if (newColumns.size() != columns.size()) {
			return false;
		}
		for (int i = 0; i < newColumns.size(); i++) {
			if (!newColumns.get(i).name.equals(columns.get(i).name)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Create the columns: standard ones for the annotation box, then columns for each
	 * annotation type in use. SPL gets a column per value.
	 */
	private ArrayList<TableColumnDef> createColumns() {
		ArrayList<TableColumnDef> cols = new ArrayList<>();
		cols.add(new TableColumnDef("UID", Long.class, (u) -> u.getUID()));
		cols.add(new TableColumnDef("Start Time", SortableTableValue.class,
				(u) -> new SortableTableValue(PamCalendar.formatDBDateTime(u.getTimeMilliseconds(), true),
						u.getTimeMilliseconds())));
		cols.add(new TableColumnDef("Channel", Integer.class, (u) -> PamUtils.getLowestChannel(u.getChannelBitmap())));
		cols.add(new TableColumnDef("Duration (s)", Double.class, (u) -> u.getDurationInMilliseconds() / 1000.));
		cols.add(new TableColumnDef("Low Freq (Hz)", Double.class, (u) -> freq(u, 0)));
		cols.add(new TableColumnDef("High Freq (Hz)", Double.class, (u) -> freq(u, 1)));

		List<DataAnnotationType<?>> usedTypes = annotationModule.getAnnotationHandler().getUsedAnnotationTypes();
		for (DataAnnotationType<?> anType : usedTypes) {
			if (anType instanceof SoundMeasurementAnnotationType) {
				SoundMeasurementAnnotationType measureType = (SoundMeasurementAnnotationType) anType;
				SoundMeasurement m = measureType.getMeasurement();
				String name = m.getUnit().isEmpty() ? m.getDisplayName()
						: String.format("%s (%s)", m.getDisplayName(), m.getUnit());
				cols.add(new TableColumnDef(name, Double.class, (u) -> {
					SoundMeasurementAnnotation an = measureType.findAnnotation(u);
					return an == null ? null : nanToNull(an.getValue());
				}));
			}
			else if (anType instanceof SNRAnnotationType) {
				cols.add(new TableColumnDef("SNR (dB)", Double.class, (u) -> {
					SNRAnnotation an = (SNRAnnotation) u.findDataAnnotation(SNRAnnotation.class);
					return an == null ? null : nanToNull(an.getSnr());
				}));
			}
			else if (anType instanceof SPLAnnotationType) {
				String[] names = {"SPL RMS (dB)", "SPL 0-Pk (dB)", "SPL Pk-Pk (dB)", "SEL (dB)"};
				for (int i = 0; i < names.length; i++) {
					final int iMeasure = i;
					cols.add(new TableColumnDef(names[i], Double.class, (u) -> {
						SPLAnnotation an = (SPLAnnotation) u.findDataAnnotation(SPLAnnotation.class);
						return an == null ? null : nanToNull(an.getMeasure(iMeasure));
					}));
				}
			}
			else if (anType instanceof StringAnnotationType) {
				cols.add(new TableColumnDef(anType.getAnnotationName(), String.class, (u) -> {
					DataAnnotation an = u.findDataAnnotation(StringAnnotation.class, anType.getAnnotationName());
					return an == null ? null : ((StringAnnotation) an).getString();
				}));
			}
			else {
				cols.add(new TableColumnDef(anType.getAnnotationName(), String.class, (u) -> {
					DataAnnotation an = u.findDataAnnotation(anType.getAnnotationClass(), anType.getAnnotationName());
					return an == null ? null : stripHtml(an.toString());
				}));
			}
		}
		return cols;
	}

	private static Double freq(MarkDataUnit u, int i) {
		double[] f = u.getFrequency();
		return f == null || f.length < 2 ? null : f[i];
	}

	private static Double nanToNull(double val) {
		return Double.isNaN(val) || Double.isInfinite(val) ? null : val;
	}

	private static String stripHtml(String str) {
		if (str == null) {
			return null;
		}
		return str.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
	}

	/**
	 * @return the annotation in the selected row, or null.
	 */
	private List<MarkDataUnit> getSelectedUnits() {
		ArrayList<MarkDataUnit> units = new ArrayList<>();
		for (int row : table.getSelectedRows()) {
			int modelRow = table.convertRowIndexToModel(row);
			if (modelRow >= 0 && modelRow < dataCopy.size()) {
				units.add(dataCopy.get(modelRow));
			}
		}
		return units;
	}

	private MarkDataUnit getSelectedUnit() {
		int row = table.getSelectedRow();
		if (row < 0) {
			return null;
		}
		int modelRow = table.convertRowIndexToModel(row);
		if (modelRow < 0 || modelRow >= dataCopy.size()) {
			return null;
		}
		return dataCopy.get(modelRow);
	}

	private class TableMouse extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				MarkDataUnit unit = getSelectedUnit();
				if (unit != null) {
					scrollTo(unit, 0);
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}
	}

	private void showPopupMenu(MouseEvent e) {
		int row = table.rowAtPoint(e.getPoint());
		if (row >= 0 && !table.isRowSelected(row)) {
			// right clicking an unselected row selects it, otherwise keep a multiple selection.
			table.setRowSelectionInterval(row, row);
		}
		MarkDataUnit unit = row >= 0 ? dataCopy.get(table.convertRowIndexToModel(row)) : getSelectedUnit();
		List<MarkDataUnit> selectedUnits = getSelectedUnits();
		if (unit == null) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItem;
		int[] beforeTimes = {0, 10, 60};
		for (int before : beforeTimes) {
			String title = before == 0 ? String.format("Scroll to annotation UID %d", unit.getUID())
					: String.format("Scroll to %ds before annotation UID %d", before, unit.getUID());
			menuItem = new JMenuItem(title);
			menuItem.addActionListener((ev) -> scrollTo(unit, before));
			menu.add(menuItem);
		}
		menu.addSeparator();
		menuItem = new JMenuItem("Edit annotation ...");
		menuItem.addActionListener((ev) -> editUnit(unit));
		menu.add(menuItem);
		menuItem = new JMenuItem("Recalculate measurements");
		menuItem.addActionListener((ev) -> {
			reMeasure(unit);
			updateTableData();
		});
		menu.add(menuItem);
		menu.add(annotationModule.createPlayMenuItem(unit));
		menu.addSeparator();
		menuItem = new JMenuItem(selectedUnits.size() > 1 ? String.format("Delete %d selected annotations ...", selectedUnits.size())
				: "Delete annotation UID " + unit.getUID() + " ...");
		menuItem.addActionListener((ev) -> deleteUnits(selectedUnits.size() > 1 ? selectedUnits : List.of(unit)));
		menu.add(menuItem);
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Scroll the displays to an annotation. In viewer mode this only moves the displays
	 * within the loaded period - it never loads more data (use the page buttons for that).
	 */
	private void scrollTo(MarkDataUnit unit, int secsBefore) {
		if (!isViewer()) {
			AbstractScrollManager.getScrollManager().startDataAt(dataBlock, unit.getTimeMilliseconds() - secsBefore * 1000L);
			return;
		}
		long t = unit.getTimeMilliseconds();
		for (AbstractPamScroller scroller : AbstractScrollManager.getScrollManager().getPamScrollers()) {
			long min = scroller.getMinimumMillis();
			long max = scroller.getMaximumMillis();
			if (t < min || t > max) {
				continue;
			}
			long value = Math.min(t - secsBefore * 1000L, max - scroller.getVisibleAmount());
			scroller.setValueMillis(Math.max(min, value));
		}
	}

	private void editUnit(MarkDataUnit unit) {
		annotationModule.editAnnotation(SwingUtilities.getWindowAncestor(mainPanel), unit);
		updateTableData();
	}

	private void reMeasure(MarkDataUnit unit) {
		annotationModule.reMeasure(unit);
		dataBlock.updatePamData(unit, System.currentTimeMillis());
	}

	private void reMeasureAll() {
		if (dataCopy.isEmpty()) {
			return;
		}
		int ans = WarnOnce.showWarning(PamController.getMainFrame(), "Recalculate measurements",
				String.format("Recalculate the measurements of all %d annotations in the table ? "
						+ "This loads the raw sound for each, so may take a while.", dataCopy.size()),
				WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return;
		}
		for (MarkDataUnit unit : new ArrayList<>(dataCopy)) {
			reMeasure(unit);
		}
		updateTableData();
	}

	private void deleteUnits(List<MarkDataUnit> units) {
		if (annotationModule.deleteAnnotations(SwingUtilities.getWindowAncestor(mainPanel), units)) {
			updateTableData();
		}
	}

	/**
	 * Export the table as shown (including any sorting) to a csv file.
	 */
	private void exportCSV() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Comma separated values (*.csv)", "csv"));
		chooser.setSelectedFile(new File(annotationModule.getUnitName().replaceAll("[^A-Za-z0-9_-]", "_") + ".csv"));
		if (chooser.showSaveDialog(mainPanel) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = chooser.getSelectedFile();
		if (!file.getName().toLowerCase().endsWith(".csv")) {
			file = new File(file.getAbsolutePath() + ".csv");
		}
		try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
			ArrayList<String> header = new ArrayList<>();
			for (TableColumnDef col : columns) {
				header.add(csvField(col.name));
			}
			pw.println(String.join(",", header));
			for (int row = 0; row < table.getRowCount(); row++) {
				MarkDataUnit unit = dataCopy.get(table.convertRowIndexToModel(row));
				ArrayList<String> fields = new ArrayList<>();
				for (TableColumnDef col : columns) {
					Object val = col.value.apply(unit);
					fields.add(val == null ? "" : csvField(val.toString()));
				}
				pw.println(String.join(",", fields));
			}
		}
		catch (IOException ex) {
			WarnOnce.showWarning(PamController.getMainFrame(), "Export csv",
					"Could not write " + file.getAbsolutePath() + ": " + ex.getMessage(), WarnOnce.WARNING_MESSAGE);
		}
	}

	private static String csvField(String str) {
		if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
			return "\"" + str.replace("\"", "\"\"") + "\"";
		}
		return str;
	}

	private class AnnotationTableModel extends AbstractTableModel {

		@Override
		public int getRowCount() {
			return dataCopy.size();
		}

		@Override
		public int getColumnCount() {
			return columns.size();
		}

		@Override
		public String getColumnName(int column) {
			return columns.get(column).name;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return columns.get(column).type;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= dataCopy.size() || columnIndex >= columns.size()) {
				return null;
			}
			try {
				return columns.get(columnIndex).value.apply(dataCopy.get(rowIndex));
			}
			catch (Exception e) {
				return null;
			}
		}
	}

	private class DataObserver extends PamObserverAdapter {
		@Override
		public String getObserverName() {
			return annotationModule.getUnitName() + " table";
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			updateLater();
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			updateLater();
		}
	}

}
