package annotation.userforms;

import PamController.PamController;
import PamView.symbol.AnnotationSymbolChooser;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataBlock;
import annotation.AnnotationDialogPanel;
import annotation.AnnotationSettingsPanel;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;
import annotation.dataselect.AnnotationDataSelCreator;
import annotation.handler.AnnotationOptions;
import annotation.userforms.datasel.UserFormDataSelCreator;
import annotation.userforms.species.FormsAnnotationSpeciesManager;
import annotation.xml.AnnotationXMLWriter;
import annotation.xml.SQLXMLWriter;
import generalDatabase.DBControlUnit;
import generalDatabase.SQLLoggingAddon;
import loggerForms.FormDescription;
import loggerForms.FormsControl;
import loggerForms.LoggerForm;
import tethys.species.DataBlockSpeciesManager;

public class UserFormAnnotationType extends DataAnnotationType<UserFormAnnotation<?>> {


	private UserFormAnnotationOptions userFormAnnotationOptions;
	
	/**
	 * Reference either to the main logger forms control or to a dummy
	 * unregistered one. 
	 */
	private static FormsControl formsControl;

	private LoggerForm loggerForm;

	private UserFormSQLAddon userFormSQLAddon;
	
	private UserFormSymbolModifier userFormSymbolChooser;
	
	private UserFormBinaryHandler userFormBinaryHandler;
	
	private UserFormDataSelCreator userFormDataSelCreator;
	
	private FormsAnnotationSpeciesManager formsAnnotationSpeciesManager;
	
	/**
	 * 
	 */
	public UserFormAnnotationType() {
		this(null);
	}
	public UserFormAnnotationType(PamDataBlock pamDataBlock) {
		super();
		setTargetDataBlock(pamDataBlock);
		userFormAnnotationOptions  = new UserFormAnnotationOptions(getAnnotationName());
		userFormSQLAddon = new UserFormSQLAddon(this);

		userFormDataSelCreator = new UserFormDataSelCreator(this, true);
	}

	@Override
	public String getAnnotationName() {
		return "User form annotation";
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return true;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#canAutoAnnotate()
	 */
	@Override
	public boolean canAutoAnnotate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getSettingsPanel()
	 */
	@Override
	public AnnotationSettingsPanel getSettingsPanel() {
		return new UserFormSettingPanel(this);
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#hasSettingsPanel()
	 */
	@Override
	public boolean hasSettingsPanel() {
		return true;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getAnnotationOptions()
	 */
	@Override
	public AnnotationOptions getAnnotationOptions() {
		return userFormAnnotationOptions;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#setAnnotationOptions(annotation.handler.AnnotationOptions)
	 */
	@Override
	public void setAnnotationOptions(AnnotationOptions annotationOptions) {
		if (UserFormAnnotationOptions.class.isAssignableFrom(annotationOptions.getClass())) {
			userFormAnnotationOptions = (UserFormAnnotationOptions) annotationOptions;
		}
		createLoggerForm();
	}
	
	private boolean tablesRead = false;
	/**
	 * Read the udf tables - only needs to be called once. 
	 * @return
	 */
	private void readUDFTables() {
		if (tablesRead == false) {
			getFormsControl().readUDFTables();
			tablesRead = true;
		}
	}
	
	/**
	 * Find the form descriptions, load form definitions if not already done. 
	 * @return form description or null;
	 */
	protected FormDescription findFormDescription() {
		boolean dbOk = (DBControlUnit.findConnection() != null);
		if (!dbOk) return null;
		readUDFTables();
		return getFormsControl().findFormDescription(userFormAnnotationOptions.getUdfFormName());
	}
	
	private void createLoggerForm() {
		loggerForm = null;
		FormDescription fd = findFormDescription();
		if (fd == null) {
			return;
		}
		loggerForm = fd.createForm();
		userFormDataSelCreator.configureFormsDataSelector(fd);
	}

	/**
	 * Get a logger forms controller. This may be the main Logger forms unit, but if there
	 * isn't one, we create one anyway and use it globally for all the form annotations. 
	 * @return Logger forms controller. 
	 */
	public static FormsControl getFormsControl() {
		if (formsControl == null) {
			formsControl = (FormsControl) PamController.getInstance().findControlledUnit(FormsControl.unitType); 
		}
		if (formsControl == null) {
			formsControl = new FormsControl("Dummy forms");
		}
		return formsControl;
	}

	/**
	 * @return the userFormAnnotationOptions
	 */
	public UserFormAnnotationOptions getUserFormAnnotationOptions() {
		return userFormAnnotationOptions;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getDialogPanel()
	 */
	@Override
	public AnnotationDialogPanel getDialogPanel() {
		if (loggerForm == null) return null;
		return new UserFormDialogPanel(this, loggerForm);
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getSQLLoggingAddon()
	 */
	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return userFormSQLAddon;
	}


	@Override
	public Class getAnnotationClass() {
		return UserFormAnnotation.class;
	}

	@Override
	public SymbolModifier getSymbolModifier(PamSymbolChooser symbolChooser) {
		if (loggerForm == null) {
			return null;
		}
		return new UserFormSymbolModifier(loggerForm.getFormDescription(), symbolChooser);
	}	
	
	/**
	 * Get the form description for this annotation (the annotation only holds one)
	 * @return form description. Will be null if not configured. 
	 */
	public FormDescription getFormDescription() {
		if (loggerForm != null) {
			return loggerForm.getFormDescription();
		}
		else {
			return null;
		}
	}

	@Override
	public AnnotationBinaryHandler<UserFormAnnotation<?>> getBinaryHandler() {
		if (userFormBinaryHandler == null) {
			synchronized (this) {
				if (userFormBinaryHandler == null) {
					userFormBinaryHandler = new UserFormBinaryHandler(this);
				}
			}
		}
		return userFormBinaryHandler;
	}
//	@Override
//	public SymbolModifier getSymbolModifier(St) {
//		if (userFormSymbolChooser == null) {
//			userFormSymbolChooser = new UserFormSymbolChooser();
//		}
//		return userFormSymbolChooser;
//	}

	@Override
	public AnnotationDataSelCreator getDataSelectCreator(String selectorName, boolean allowScores) {
		return userFormDataSelCreator;
	}

	@Override
	public AnnotationXMLWriter<UserFormAnnotation<?>> getXMLWriter() {
		// TODO Auto-generated method stub
		return new SQLXMLWriter<>(this);
	}
	
	@Override
	public DataBlockSpeciesManager getDataBlockSpeciesManager() {
		if (formsAnnotationSpeciesManager == null) {
			formsAnnotationSpeciesManager = new FormsAnnotationSpeciesManager(this, getTargetDataBlock());
		}
		return formsAnnotationSpeciesManager;
	}
}
