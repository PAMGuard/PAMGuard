package PamController.pamWizard.swing;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamController.pamWizard.configurations.ConfigApplyContext;
import PamController.pamWizard.configurations.ConfigWizardData;
import PamController.pamWizard.configurations.PamConfigInspection;
import PamView.dialog.PamDialog;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;

/**
 * Third page of the import wizard: where the output should go.
 * <p>
 * A shipped configuration carries whatever storage paths its author was using -
 * often a drive letter on a machine on the other side of the world - so these
 * always have to be replaced. The user picks one project folder and the binary
 * store and database are derived from it, but either can be overridden.
 * <p>
 * Only shown for configurations which actually contain a binary store or a
 * database.
 *
 * @author Jamie Macaulay
 */
public class StoragePathsCard extends PamWizardCard<ConfigWizardData> {

	private static final long serialVersionUID = 1L;

	private final JTextField projectField = new JTextField(32);
	private final JTextField binaryField = new JTextField(32);
	private final JTextField databaseField = new JTextField(32);

	private final JLabel binaryLabel = new JLabel("Binary store folder:");
	private final JLabel databaseLabel = new JLabel("Database file:");
	private final JButton binaryBrowse = new JButton("Browse...");
	private final JButton databaseBrowse = new JButton("Browse...");

	/**
	 * True while the project folder is filling in the derived paths, so that doing so
	 * is not mistaken for the user editing them by hand.
	 */
	private boolean deriving = false;

	/**
	 * Set once the user edits a derived path, after which changing the project folder
	 * leaves their choice alone.
	 */
	private boolean binaryEdited = false;
	private boolean databaseEdited = false;

	public StoragePathsCard(PamWizard pamWizard) {
		super(pamWizard, "Data storage");
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel title = new JLabel("Where should PAMGuard put the data it creates?");
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

		JPanel fields = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 3);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;

		JButton projectBrowse = new JButton("Browse...");
		addRow(fields, c, 0, new JLabel("Project folder:"), projectField, projectBrowse);
		addRow(fields, c, 1, binaryLabel, binaryField, binaryBrowse);
		addRow(fields, c, 2, databaseLabel, databaseField, databaseBrowse);

		projectBrowse.addActionListener(e -> browseFolder(projectField, "Select a project folder"));
		binaryBrowse.addActionListener(e -> browseFolder(binaryField, "Select a binary storage folder"));
		databaseBrowse.addActionListener(e -> browseDatabase());

		projectField.getDocument().addDocumentListener(new SimpleDocumentListener(this::projectFolderChanged));
		binaryField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
			if (!deriving) {
				binaryEdited = true;
			}
		}));
		databaseField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
			if (!deriving) {
				databaseEdited = true;
			}
		}));

		JLabel note = new JLabel("<html><i>The folders are created if they do not already exist.</i></html>");
		note.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

		// hold the content against the top of the page rather than centring it.
		JPanel top = new JPanel(new BorderLayout());
		top.add(BorderLayout.NORTH, title);
		top.add(BorderLayout.CENTER, fields);
		top.add(BorderLayout.SOUTH, note);

		add(BorderLayout.NORTH, top);
	}

	private void addRow(JPanel panel, GridBagConstraints c, int row, JLabel label, JTextField field, JButton browse) {
		c.gridy = row;
		c.gridx = 0;
		c.weightx = 0;
		panel.add(label, c);
		c.gridx = 1;
		c.weightx = 1;
		panel.add(field, c);
		c.gridx = 2;
		c.weightx = 0;
		panel.add(browse, c);
	}

	@Override
	public void setParams(ConfigWizardData cardParams) {
		ConfigApplyContext context = cardParams.getApplyContext();

		File project = context.getProjectFolder();
		if (project == null) {
			project = ConfigApplyContext.getDefaultProjectFolder();
		}
		deriving = true;
		projectField.setText(project == null ? "" : project.getAbsolutePath());
		deriving = false;

		// re-derive from the project folder, since the configuration name may have
		// changed since this page was last shown.
		binaryEdited = false;
		databaseEdited = false;
		cardParams.setProjectFolder(project);
		showDerived(cardParams);

		// hide the rows the chosen configuration has no use for.
		PamConfigInspection inspection = cardParams.getSelectedInspection();
		boolean hasBinary = inspection != null && inspection.hasBinaryStore();
		boolean hasDatabase = inspection != null && inspection.hasDatabase();
		setRowVisible(binaryLabel, binaryField, binaryBrowse, hasBinary);
		setRowVisible(databaseLabel, databaseField, databaseBrowse, hasDatabase);
	}

	@Override
	public boolean getParams(ConfigWizardData cardParams) {
		ConfigApplyContext context = cardParams.getApplyContext();
		PamConfigInspection inspection = cardParams.getSelectedInspection();

		String projectText = projectField.getText().trim();
		if (projectText.isEmpty()) {
			return PamDialog.showWarning(getPamWizard(), "No project folder",
					"Please choose a folder for PAMGuard to write its data to.");
		}
		context.setProjectFolder(new File(projectText), cardParams.getSelectedConfig().getConfigName());

		if (inspection != null && inspection.hasBinaryStore()) {
			String binaryText = binaryField.getText().trim();
			if (binaryText.isEmpty()) {
				return PamDialog.showWarning(getPamWizard(), "No binary store",
						"This configuration writes binary data, so it needs a folder to write them to.");
			}
			context.setBinaryFolder(new File(binaryText));
		}
		else {
			context.setBinaryFolder(null);
		}

		if (inspection != null && inspection.hasDatabase()) {
			String databaseText = databaseField.getText().trim();
			if (databaseText.isEmpty()) {
				return PamDialog.showWarning(getPamWizard(), "No database",
						"This configuration uses a database, so it needs a database file.");
			}
			context.setDatabaseFile(new File(databaseText));
		}
		else {
			context.setDatabaseFile(null);
		}

		return true;
	}

	/**
	 * Re-derive the binary and database paths from the project folder, leaving alone
	 * anything the user has typed themselves.
	 */
	private void projectFolderChanged() {
		if (deriving || getPamWizard() == null) {
			return;
		}
		String text = projectField.getText().trim();
		if (text.isEmpty()) {
			return;
		}
		File folder = new File(text);
		deriving = true;
		if (!binaryEdited) {
			binaryField.setText(new File(folder, ConfigApplyContext.BINARY_FOLDER_NAME).getAbsolutePath());
		}
		if (!databaseEdited) {
			String current = databaseField.getText().trim();
			String name = current.isEmpty() ? "pamguard.sqlite3" : new File(current).getName();
			databaseField.setText(new File(folder, name).getAbsolutePath());
		}
		deriving = false;
	}

	private void showDerived(ConfigWizardData cardParams) {
		ConfigApplyContext context = cardParams.getApplyContext();
		deriving = true;
		binaryField.setText(context.getBinaryFolder() == null ? "" : context.getBinaryFolder().getAbsolutePath());
		databaseField.setText(context.getDatabaseFile() == null ? "" : context.getDatabaseFile().getAbsolutePath());
		deriving = false;
	}

	private void setRowVisible(JLabel label, JTextField field, JButton browse, boolean visible) {
		label.setVisible(visible);
		field.setVisible(visible);
		browse.setVisible(visible);
	}

	private void browseFolder(JTextField field, String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		String current = field.getText().trim();
		if (!current.isEmpty()) {
			chooser.setCurrentDirectory(new File(current));
		}
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			field.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void browseDatabase() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select a database file");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		String current = databaseField.getText().trim();
		if (!current.isEmpty()) {
			chooser.setSelectedFile(new File(current));
		}
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			databaseField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	/**
	 * Runs the same action for any change to a text field.
	 */
	private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {

		private final Runnable action;

		SimpleDocumentListener(Runnable action) {
			this.action = action;
		}

		@Override
		public void insertUpdate(javax.swing.event.DocumentEvent e) {
			action.run();
		}

		@Override
		public void removeUpdate(javax.swing.event.DocumentEvent e) {
			action.run();
		}

		@Override
		public void changedUpdate(javax.swing.event.DocumentEvent e) {
			action.run();
		}
	}
}
