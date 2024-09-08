package loggerForms;

import java.util.ArrayList;

import PamController.PamViewParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.InputControlDescription;
import pamScrollSystem.ViewLoadObserver;

public class FormsLogging extends SQLLogging {

	private FormDescription formDescription;
	
	protected FormsLogging(FormDescription formDescription, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.formDescription = formDescription;
		formDescription.getOutputTableDef().setUpdatePolicy(SQLLogging.UPDATE_POLICY_OVERWRITE);
		setTableDefinition(formDescription.getOutputTableDef());
	}

	@Override
	public String getViewerLoadClause(SQLTypes sqlTypes, PamViewParameters pvp) {
		// modified clause in case the form defines a start time element in which 
		// case we need an OR of that and UTC. 
		String standardClause = super.getViewerLoadClause(sqlTypes, pvp);
		PropertyDescription startProp = formDescription.findProperty(PropertyTypes.STARTTIME);
		if (startProp == null) {
			return standardClause;
		}
		String startName = startProp.getDbTitle();
		if (startName == null) {
			return standardClause;
		}
		startName = EmptyTableDefinition.deblankString(startName); // replace blanks with underscores. 
		startName = sqlTypes.formatColumnName(startName); // wrap as standard for this database taype. 
		String t1 = sqlTypes.formatDBDateTimeQueryString(pvp.viewStartTime);
		String t2 = sqlTypes.formatDBDateTimeQueryString(pvp.viewEndTime);
		String newClause = String.format(" WHERE (UTC BETWEEN %s AND %s) OR (%s BETWEEN %s AND %s) ORDER BY %s, UTC, UTCMilliseconds", 
				t1, t2, startName, t1, t2, startName);
		
		return newClause;
	}
	
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		FormsDataUnit formDataUnit = (FormsDataUnit) pamDataUnit;
		
		Object[] datas = formDataUnit.getFormData();
		
		ArrayList<InputControlDescription> inputCtrls = formDescription.getInputControlDescriptions();
		InputControlDescription cd;
		for (int i = 0; i < inputCtrls.size(); i++) {
			cd = inputCtrls.get(i);
			cd.moveDataToTableItems(datas[i]);
			
		}
		
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds,int databaseIndex) {

		ArrayList<InputControlDescription> inputCtrls = formDescription.getInputControlDescriptions();
		Object[] formData = new Object[inputCtrls.size()];
		ControlDescription cd;
		for (int i = 0; i < inputCtrls.size(); i++) {
			cd = inputCtrls.get(i);
			formData[i] = cd.moveDataFromTableItems();
			if (formData[i] != null && String.class.isAssignableFrom(formData[i].getClass())) {
				formData[i] = ((String) formData[i]).trim();
			}
		}
		
		
		FormsDataUnit formsDataUnit = new FormsDataUnit(null, timeMilliseconds, formDescription, formData);
		formsDataUnit.setDatabaseIndex(databaseIndex);
		getPamDataBlock().addPamData(formsDataUnit);
		
		return formsDataUnit;
		
	}



	@Override
	public boolean loadViewData(PamConnection con, PamViewParameters pamViewParameters, ViewLoadObserver loadObserver) {
		boolean ans = super.loadViewData(con, pamViewParameters, loadObserver);
		if (ans) {
			getPamDataBlock().sortData();
		}
		return ans;
	}

}
