package annotation.userforms;

import java.util.ArrayList;

import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;
import loggerForms.FormDescription;
import loggerForms.FormsTableItem;
import loggerForms.UDFTableDefinition;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.InputControlDescription;

/**
 * SQL Logging addon for Logger form annotations. 
 * @author Doug
 *
 */
public class UserFormSQLAddon implements SQLLoggingAddon {

	private UserFormAnnotationType userFormAnnotationType;


	public UserFormSQLAddon(UserFormAnnotationType userFormAnnotationType) {
		this.userFormAnnotationType = userFormAnnotationType;
	}

	ArrayList<PamTableItem> loggerTableItems = new ArrayList<>();


	@Override
	public void addTableItems(EmptyTableDefinition pamTableDefinition) {
		loggerTableItems.clear();
		FormDescription formDescription = userFormAnnotationType.findFormDescription();
		if (formDescription == null) {
			return;
		}
		UDFTableDefinition formTableDef = formDescription.getUdfTableDefinition();
		ArrayList<InputControlDescription> controlDescriptions = formDescription.getInputControlDescriptions();
		for (ControlDescription cd:controlDescriptions) {
			FormsTableItem[] ctrlTableItems = cd.getFormsTableItems();
			if (ctrlTableItems != null) {
				for (int i = 0; i < ctrlTableItems.length; i++) {
					loggerTableItems.add(ctrlTableItems[i]);
					pamTableDefinition.addTableItem(ctrlTableItems[i]);
				}
			}
		}
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		if (pamDataUnit == null) {
			clearTableItems();
			return false;
		}
		FormDescription formDescription = userFormAnnotationType.findFormDescription();
		if (formDescription == null) {
			clearTableItems();
			return false;
		}
		UserFormAnnotation formAnnotation = (UserFormAnnotation) pamDataUnit.findDataAnnotation(UserFormAnnotation.class);
		if (formAnnotation == null) {
			clearTableItems();
			return true; // acceptable condition. 
		}
		Object[] datas = formAnnotation.getLoggerFormData();
		
		// Don't rely on the inputCtrls array in the FormDescription; FormControl creates a new FormDescription
		// every time a class that can include a form annotation (e.g. Group Detection Localiser, Spectrogram Annotation, etc)
		// is created.  So the inputControls list in FormDescription will only point to the last set of controls created,
		// which isn't necessarily the ones that this table is pointing to.  Instead, load the loggerTableItems with
		// the data because loggerTableItems and pamTableDefinition both point to the same thing
//		ArrayList<ControlDescription> inputCtrls = formDescription.getInputControlDescriptions();
//		ControlDescription cd;
//		for (int i = 0; i < inputCtrls.size(); i++) {
//			cd = inputCtrls.get(i);
//			cd.moveDataToTableItems(datas[i]);
//		}
		for (int i = 0; i < loggerTableItems.size(); i++) {
			loggerTableItems.get(i).setValue(datas[i]);
		}
		return true;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		FormDescription formDescription = userFormAnnotationType.findFormDescription();
		if (formDescription == null) {
			return false;
		}
		ArrayList<InputControlDescription> inputCtrls = formDescription.getInputControlDescriptions();
		Object[] data = new Object[inputCtrls.size()];
		ControlDescription cd;
		int notNull = 0;
		try {
			// Don't rely on the inputCtrls array in the FormDescription; FormControl creates a new FormDescription
			// every time a class that can include a form annotation (e.g. Group Detection Localiser, Spectrogram Annotation, etc)
			// is created.  So the inputControls list in FormDescription will only point to the last set of controls created,
			// which isn't necessarily the ones that this table is pointing to.  Instead, use the loggerTableItems because the
			// pamTableDefinition has just been loaded with the correct data (in SQLLogging.transferDataFromResult) and
			// loggerTableItems points to the same objects as pamTableDefinition
//			for (int i = 0; i < inputCtrls.size(); i++) {
//				cd = inputCtrls.get(i);
//				data[i] = cd.moveDataFromTableItems();
//			}
			for (int i=0; i<loggerTableItems.size(); i++) {
				data[i] = loggerTableItems.get(i).getValue();
				if (data[i] != null) {
					notNull++;
				}
			}
		}
		catch (Exception e) {
			System.out.println("Error loading Logger annotation data:" + e.getMessage());
			return false;
		}
		if (notNull > 0) {
			UserFormAnnotation an = new UserFormAnnotation(userFormAnnotationType, data);
			pamDataUnit.addDataAnnotation(an);
			return true;
		}
		return false;
	}

	/**
	 * Set data in every column to null;
	 */
	private void clearTableItems() {
		for (PamTableItem pti:loggerTableItems) {
			pti.setValue(null);
		}
	}

	@Override
	public String getName() {
		return userFormAnnotationType.getAnnotationName();
	}

}
