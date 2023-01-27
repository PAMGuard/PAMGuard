package clickDetector.ClickClassifiers.annotation;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

/**
 * Save classification information to the database. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class ClickAnnotationSQL implements SQLLoggingAddon {

	/**
	 * Click classification type
	 */
	private ClickClassificationType clickClassifierType;
	
	/**
	 * The Pam Table Item for the classifier set (which classifier indexes the click has passed)
	 */
	private PamTableItem classifierSetTable;

	public ClickAnnotationSQL(ClickClassificationType bearingAnnotationType) {
		super();
		this.clickClassifierType = bearingAnnotationType;
		classifierSetTable = new PamTableItem("Classification Set", Types.CHAR, 30);
	}

	@Override
	public void addTableItems(EmptyTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(classifierSetTable);
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		ClickClassifierAnnotation clickAnnotation = (ClickClassifierAnnotation) pamDataUnit.findDataAnnotation(ClickClassificationType.class);
		
		//create a comma delimited string
		String data= "";
		for (int i=0; i<clickAnnotation.getClassiferSet().length; i++) {
			data=data+clickAnnotation.getClassiferSet()[i] +","; 
		}
	
		classifierSetTable.setValue(data);
		return true;
	}

	@Override
	
	public boolean loadData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		String array = classifierSetTable.getDeblankedStringValue();
		
		//read in the classification set. This a list of all the classifiers the clicks passed. 
		String[] values= array.split(",");
		int[] clssfArray=new int[values.length]; 
		for (int i=0; i<values.length; i++) {
			clssfArray[i]=Integer.valueOf(values[i]); 
		}
		
		pamDataUnit.addDataAnnotation(new ClickClassifierAnnotation(clickClassifierType, clssfArray));
		return false;
	}

	@Override
	public String getName() {
		return clickClassifierType.getAnnotationName();
	}

}

