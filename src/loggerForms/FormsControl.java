package loggerForms;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.status.ModuleStatus;
import PamView.PamGui;
import PamView.PamSidePanel;
import PamView.PamTabPanel;
import PamView.PamView;
import PamguardMVC.PamDataBlock;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import loggerForms.monitor.FormsMonitorMaster;

/**
 * 
 * @author Graham Weatherup controls the logger forms module
 */
public class FormsControl extends PamControlledUnit implements PamSettings {

	public static ArrayList<String> restrictedTitles = new ArrayList<String>();

	private ArrayList<FormDescription> formDescriptions = new ArrayList<FormDescription>();
	private ArrayList<UDFErrors> UDFErrors = new ArrayList<UDFErrors>();

	private FormsTabPanel formsTabPanel;

	private FormsAlertSidePanel formsAlertSidePanel;

	private FormsProcess formsProcess;

	public static final String unitType = "Logger Forms";

	private FormsMonitorMaster formsMonitor;

	private FormsParameters formsParameters = new FormsParameters();

//	/** A set of dummy parameters, used solely to pull together different settings for XML export */
//	private FormsTempParams dummyParams;

//	private KeyboardFocusManager keyManager; 
//	private FormsHotKeyControl hotKeyControl;

	public FormsControl(String unitName) {
		super(unitType, unitName);
		addPamProcess(formsProcess = new FormsProcess(this, "Forms Output"));
		formsTabPanel = new FormsTabPanel(this);
		formsAlertSidePanel = new FormsAlertSidePanel(this);
		formsMonitor = new FormsMonitorMaster(this, formsProcess);
		PamSettingManager.getInstance().registerSettings(this);

	}

//	@Override
//	public JMenuBar getTabSpecificMenuBar(Frame parentFrame,
//			JMenuBar standardMenu, PamGui pamGui) {
//		JMenu
//		return this.createDetectionMenu(parentFrame);
////		return super.getTabSpecificMenuBar(parentFrame, standardMenu, pamGui);
//	}

	/**
	 * 
	 */
	private boolean buildRestrictedTitles() {

		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return false;
		}
		String keywordString = null;
		try {
			keywordString = DBControlUnit.findDatabaseControl().getDatabaseSystem().getKeywords();
		} catch (NullPointerException e) {
			return false;
		}

		String[] keywords;
		if (keywordString != null) {
			keywords = keywordString.split(",");

			for (String k : keywords) {
				restrictedTitles.add(k);
			}
		}

		try {
			PamConnection con = DBControlUnit.findConnection();
			if (con == null) {
				return false;
			}
			keywordString = con.getConnection().getMetaData().getSQLKeywords();

			keywords = keywordString.split(",");

			for (String k : keywords) {
				restrictedTitles.add(k);
			}
//			System.out.println(keywordString);
//			System.out.println("");
//			System.out.println(keywords);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			generateForms();
			break;
		}
	}

	private void updateFormDataMaps() {
		ArrayList<PamDataBlock> allFormDataBlocks = formsProcess.getOutputDataBlocks();
		DBControlUnit.findDatabaseControl().mapNewDataBlock(null, allFormDataBlocks);

	}

	public int getNumFormDescriptions() {
		return formDescriptions.size();
	}

	/**
	 * Get the form description for a specific index.
	 * 
	 * @param iForm form index
	 * @return for description
	 */
	public FormDescription getFormDescription(int iForm) {
		return formDescriptions.get(iForm);
	}

	/**
	 * Get a form index from a form description
	 * 
	 * @param formDescription Form Descriptions
	 * @return form index or -1 if not found
	 */
	public int getFormIndex(FormDescription formDescription) {
		return formDescriptions.indexOf(formDescription);
	}

	/**
	 * Find a form which has a particular order value.
	 * 
	 * @param order order (starts from 1 generally)
	 * @return a form, or null if none have that order.
	 */
	public FormDescription findFormByOrder(int order) {
		for (FormDescription aForm : formDescriptions) {
			PropertyDescription formProperty = aForm.findProperty(PropertyTypes.ORDER);
			if (formProperty == null) {
				continue;
			}
			Integer lVal = formProperty.getLength();
			if (lVal != null && lVal == order) {
				return aForm;
			}
		}
		return null;
	}

	/**
	 * Find a form description with a given name.
	 * 
	 * @param formName
	 * @return form description or null.
	 */
	public FormDescription findFormDescription(String formName) {
		if (formName == null) {
			return null;
		}
		for (FormDescription ad : formDescriptions) {
			if (ad.getFormName().equals(formName)) {
				return ad;
			}
			if (ad.getUdfName().equals(formName)) {
				return ad;
			}
		}
		return null;
	}

	public Character getOutputTableNameCounterSuffix(FormDescription thisFormDescription) {

		String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String thisTableName = thisFormDescription.getDBTABLENAME();
		int count = 0;
		int position = 0;

		for (FormDescription formDescription : formDescriptions) {
			if ((formDescription.getDBTABLENAME() == thisFormDescription.getDBTABLENAME())) {
				position = count;
				break;
			}

			count++;

		}
		// if (count==0){
		// return "";
		// }else{
		// return Character.;
		// Integer.toString(arg0, arg1)
		// System.out.println("*********");
		// System.out.println(position);

		// }
		// System.out.println(letters.charAt(position));
		// System.out.println("*********");
		return letters.charAt(position);
	}

	/**
	 * Generates a list of tables beginning with UDF_ and reads their contents into
	 * a FormDescription
	 */
	public void readUDFTables() {
		/*
		 * first find the database and get the database connection
		 */
		formDescriptions.clear();
		if (UDFErrors != null) {
			UDFErrors.clear();
		}

		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			System.out.println("No database module - create one to use forms");
			return;
		} else {

			ArrayList<String> udfTableNameList = new ArrayList<String>();

			PamConnection dbCon = dbControl.getConnection();

			if (dbCon == null) {
				System.out.println("Database not opened: Logger forms cannot be read");
				return;
			}

			try {
				DatabaseMetaData dbmd = dbCon.getConnection().getMetaData();
				String[] types = { "TABLE" };
				ResultSet resultSet = dbmd.getTables(null, null, "%", types);// not getting all tables from db in ODB

				// Loop through database tables
				while (resultSet.next()) {
					String tableName = resultSet.getString(3);
//					System.out.println("LogFor: "+tableName);
					// If starts with 'UDF_' create form description from it.
					if (tableName.toUpperCase().startsWith("UDF_")) {
						udfTableNameList.add(tableName);
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (UDFErrors.size() > 0) {
			}

			for (String tableName : udfTableNameList) {
				FormDescription formDescription = new FormDescription(this, tableName);
				formDescriptions.add(formDescription);
			}

			// at this point, before datablocks are created, sort the forms into order.
			Collections.sort(formDescriptions);
//			hotKeyControl.removeAllListeners();
			// finally create the datablocks.
			// at the same time, can reset the order properties to a simple sequence.
			int iForm = 0;
			for (FormDescription aFD : formDescriptions) {
				formsProcess.addOutputDataBlock(aFD.getFormsDataBlock());
				aFD.setFormOrderProperty(++iForm);
				/*
				 * And see if it's got a hotkey
				 */
				String hotKey = aFD.getHOTKEY();
				if (hotKey != null) {
//					hotKeyControl.addKeyListener(aFD, hotKey);
				}
			}

		}
	}

	public void addFormDescription(String newFormName) {
		FormDescription formDescription = new FormDescription(this, newFormName);
		formsProcess.addOutputDataBlock(formDescription.getFormsDataBlock());
	}

	/**
	 * Get the correct type of reference to the forms tab panel.
	 * 
	 * @return reference to the forms tab panel.
	 */
	public FormsTabPanel getFormsTabPanel() {
		return formsTabPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamController.PamControlledUnit#getTabPanel()
	 */
	@Override
	public PamTabPanel getTabPanel() {
		return formsTabPanel;
	}

	@Override
	public PamSidePanel getSidePanel() {
		if (formsAlertSidePanel == null) {
			formsAlertSidePanel = new FormsAlertSidePanel(this);
		}
		return formsAlertSidePanel;
	}

	private void createProcesses() {

	}

	/**
	 * @return the formsProcess
	 */
	public FormsProcess getFormsProcess() {
		return formsProcess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem detMenu = new JMenu(getUnitName());
		JMenuItem menuItem = new JMenuItem("Create New Form ...");
		detMenu.add(menuItem);
		menuItem.addActionListener(new NewLoggerForm(parentFrame));
//		if (SMRUEnable.isEnable()) {
		JMenu editMenu = new JMenu("Edit form");
		for (int i = 0; i < getNumFormDescriptions(); i++) {
			FormDescription fd = getFormDescription(i);
			JMenuItem fm = new JMenuItem(fd.getFormName() + "...");
			fm.addActionListener(new EditForm(parentFrame, fd));
			editMenu.add(fm);
		}
		detMenu.add(editMenu);
//		}
		detMenu.add(menuItem = new JMenuItem("Regenerate all forms"));
		menuItem.addActionListener(new ReGenerateForms(parentFrame));
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			JCheckBoxMenuItem changebox = new JCheckBoxMenuItem("Allow Viewer changes");
			changebox.setToolTipText("Allow the editing, adding, and deleting of data in Viewer mode");
			changebox.setSelected(formsParameters.allowViewerChanges);
			changebox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					formsParameters.allowViewerChanges = changebox.isSelected();
					notifyOptionsChange();
				}
			});
			detMenu.add(changebox);
		}

		return detMenu;
	}

	class NewLoggerForm implements ActionListener {

		private Frame parentFrame;

		public NewLoggerForm(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			newLoggerform(parentFrame);
		}
	}
	
	private void notifyOptionsChange() {
		if (formDescriptions == null) {
			return;
		}
		for (FormDescription formDesc: formDescriptions) {
			formDesc.optionsChange();
		}
	}

	class ReGenerateForms implements ActionListener {

		private Frame parentFrame;

		public ReGenerateForms(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			regenerateForms(parentFrame);

		}

	}

	class EditForm implements ActionListener {
		private FormDescription formDescription;
		private Frame parentFrame;

		public EditForm(Frame parentFrame, FormDescription formDescription) {
			super();
			this.parentFrame = parentFrame;
			this.formDescription = formDescription;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			this.formDescription.editForm(parentFrame);
		}
	}

	/**
	 * Create a new logger form
	 * 
	 * @param parentFrame parent frame
	 * @return selected name for new form, or null if nothing created.
	 */
	public String newLoggerform(Frame parentFrame) {
		String newName = JOptionPane.showInputDialog(parentFrame, "Enter the name for the new user form",
				"New Logger Form", JOptionPane.OK_CANCEL_OPTION);
		if (newName == null) {
			return null;
		}
		// will make a form table definition with a standard structure and name UDF_ ...
		// check the current name starts with UDF and add if necessary.
		if (!newName.toUpperCase().startsWith("UDF_")) {
			newName = "UDF_" + newName;
		}
		String message = String.format("The table definition %s will now be created in the database.", newName);
		message += "\nNote that youwill have to exit PAMGUARD and enter form control data by hand into this table.";
		message += "\nFuture releases will (hopefully) contain a more friendly programmable interface";
		int ans = JOptionPane.showConfirmDialog(parentFrame, message, "Create Form", JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.CANCEL_OPTION) {
			return null;
		}
		UDFTableDefinition tableDef = new UDFTableDefinition(newName);
		message = String.format("The table %s could not be created in the databse %s", newName,
				DBControlUnit.findDatabaseControl().getDatabaseName());
		if (!DBControlUnit.findDatabaseControl().getDbProcess().checkTable(tableDef)) {
			JOptionPane.showMessageDialog(parentFrame, "Error Creating form", message, JOptionPane.ERROR_MESSAGE);
		}
		return newName;
	}

	/**
	 * Generate all forms and associated processes, notifying databases, maps, etc
	 * so that any required actions can be taken.
	 */
	private void generateForms() {

		buildRestrictedTitles();

		readUDFTables();

		createProcesses();

		formsTabPanel.createForms();
		// initialise

		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return;
		}

		DBControlUnit.findDatabaseControl().getDbProcess().updateProcessList();
		if (isViewer) {
			updateFormDataMaps();
		}

		formsAlertSidePanel.getFormsAlertPanel().updateFormsShowing();
		// will also at this point need to tell the main gui frame to update
		// it's menu since it needs to incorporate a list of forms for editing
		PamView pamView = getPamView();
		if (pamView != null && PamGui.class.isAssignableFrom(pamView.getClass())) {
			PamGui pamGui = (PamGui) pamView;
			pamGui.showTabSpecificSettings();
		}

		formsMonitor.rebuiltForms();
	}

	/**
	 * Delete and recreate all forms / form data, etc.
	 * 
	 * @param parentFrame
	 */
	public void regenerateForms(Window parentFrame) {
		formsTabPanel.removeAllForms();

		for (FormDescription formDescription : formDescriptions) {
			formDescription.destroyForms();
		}
		formDescriptions.clear();
		formsProcess.removeAllDataBlocks();
		generateForms();
		FormsPlotOptionsDialog.deleteDialog();

	}

	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		JMenuItem plotMenu = new JMenuItem(getUnitName() + " plot options ...");
		plotMenu.addActionListener(new DisplayMenu(parentFrame));
		return plotMenu;
	}

	private class DisplayMenu implements ActionListener {

		private Frame parentFrame;

		public DisplayMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			displayOptions(parentFrame);
		}
	}

	public void displayOptions(Frame parentFrame) {
		boolean ans = FormsPlotOptionsDialog.showDialog(parentFrame, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamController.PamControlledUnit#canClose()
	 */
	@Override
	public boolean canClose() {
		// return false if any forms have open sub forms.
		int subFormCount = 0;
		for (FormDescription fd : formDescriptions) {
			subFormCount += fd.getSubformCount();
		}
		if (subFormCount == 0) {
			return true;
		}
		String message = "One or more forms have open sub tab forms. Do you still want to close PAMguard";
		int ans = JOptionPane.showConfirmDialog(getGuiFrame(), message, getUnitName(), JOptionPane.YES_NO_OPTION);
		return (ans == JOptionPane.YES_OPTION);
	}

	/**
	 * Rewrite all UDF tables in forms which have been altered.
	 */
	public void rewriteChangedUDFTables() {
		for (FormDescription aForm : formDescriptions) {
			if (aForm.isNeedsUDFSave()) {
				aForm.writeUDFTable(null);
				aForm.setNeedsUDFSave(false);
			}
		}
	}

	/**
	 * @return the formsMonitor
	 */
	public FormsMonitorMaster getFormsMonitor() {
		return formsMonitor;
	}

	@Override
	public ModuleStatus getModuleStatus() {
		if (formDescriptions == null || formDescriptions.size() == 0) {
			return new ModuleStatus(ModuleStatus.STATUS_WARNING, "No Logger Form definitions available");
		}
		for (int i = 0; i < formDescriptions.size(); i++) {
			FormDescription fd = formDescriptions.get(i);
//			fd.gete
		}
		return new ModuleStatus(ModuleStatus.STATUS_OK);
	}

	/**
	 * Some things that are meant to be boolean are coming out as int or string so
	 * need to do some type checking.
	 * 
	 * @param value
	 * @return
	 */
	public static Boolean checkBadBoolean(Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			String str = (String) value;
			str = str.strip();
			return str.equals("1") || str.toLowerCase().equals("false");
		}
		if (value instanceof Integer) {
			int val = (Integer) value;
			return val != 0;
		}

		return null;
	}

	@Override
	public Serializable getSettingsReference() {
		return formsParameters;
	}

	@Override
	public long getSettingsVersion() {
		return FormsParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.formsParameters = (FormsParameters) pamControlledUnitSettings.getSettings();
		return true;
	}

	/**
	 * @return the formsParameters
	 */
	public FormsParameters getFormsParameters() {
		return formsParameters;
	}

}
