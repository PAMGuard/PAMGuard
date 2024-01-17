package annotation.handler;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsNameProvider;
import PamView.dialog.GenericSwingDialog;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.SQLLogging;

/**
 * Annotation manager that should be able to do absolutely everything (almost) self contained
 * within itself without having to add about 4 different functions to it's parent controller or process
 * @author dg50
 *
 */
public abstract class OneStopAnnotationHandler extends AnnotationChoiceHandler implements PamSettings {
	
	private AnnotationChoices annotationChoices = new AnnotationChoices();
	private SettingsNameProvider settingsNameProvider;

	public OneStopAnnotationHandler(SettingsNameProvider settingsNameProvider, PamDataBlock<PamDataUnit> pamDataBlock) {
		super(pamDataBlock);
		this.settingsNameProvider = settingsNameProvider;
		PamSettingManager.getInstance().registerSettings(this);
		createAnnotationTypes();
	}
	
	/**
	 * Add some annotation types for the annotation handler
	 */
	public abstract void createAnnotationTypes();

	@Override
	public AnnotationChoices getAnnotationChoices() {
		if (annotationChoices == null) {
			annotationChoices = new AnnotationChoices();
		}
		return annotationChoices;
	}

	@Override
	public String getUnitType() {
		return "Annotation Choices";
	}

	@Override
	public Serializable getSettingsReference() {
		return getAnnotationChoices();
	}

	@Override
	public long getSettingsVersion() {
		return AnnotationChoices.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		annotationChoices = (AnnotationChoices) pamControlledUnitSettings.getSettings();
		return true;
	}

	@Override
	public String getUnitName() {
		return settingsNameProvider.getUnitName();
	}
	
	/**
	 * This can be used to open annotation choice information in it's own dialog<p>
	 * an alternative is to call getSelectionPanel() and incorporate it into a greater
	 * dialog, but if you do, you must call checkSQLLogging after the dialog closes. 
	 * @param window
	 * @return true if dialog selected OK (not cancelled)
	 */
	public boolean showDialog(Window window) {
		boolean ok = GenericSwingDialog.showDialog(window, getUnitName(), this.getSelectionPanel());
		if (ok) {
			checkSQLLogging();
		}
		return ok;
	}

	/**
	 * Get a standard menu item for showing the Annotation dialog. 
	 * @param window
	 * @return menu item
	 */
	public JMenuItem getDialogMenuItem(Window window) {
		return getDialogMenuItem(window, "Annotations");
	}
	
	/**
	 * Get a standard menu item for showing the Annotation dialog. 
	 * @param window
	 * @param menuName String name for the menu item
	 * @return menu item
	 */
	public JMenuItem getDialogMenuItem(Window window, String menuName) {
		JMenuItem menuItem = new JMenuItem(menuName);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showDialog(window);
			}
		});
		return menuItem;
	}

	/**
	 * Check that logging on the datablock is all set up for the current
	 * annotation selection
	 */
	public void checkSQLLogging() {
		SQLLogging logging = getPamDataBlock().getLogging();
		if (logging == null) {
			return;
		}
		// get, modify and set the modified tabelDef
		PamTableDefinition tableDef = logging.getBaseTableDefinition();
		logging.setTableDefinition(tableDef);
		addAnnotationSqlAddons(logging); // this will add the additional columns. 
		DBControlUnit dbc = DBControlUnit.findDatabaseControl();
		if (dbc != null) {
			dbc.getDbProcess().checkTable(logging.getTableDefinition());
		}
	}

}
