package matchedTemplateClassifer.annotation;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;
import matchedTemplateClassifer.MatchedTemplateResult;

/**
 * Writes a matched click classifier annotation to the database. Unlikely to be used 
 * as clicks are rarely saved to the database. 
 * 
 * TODO - need to sort this properly for multiple templates. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MatchedClickAnnotationSQL implements SQLLoggingAddon {

	/**
	 * Click classification type
	 */
	private MatchedClickAnnotationType clickClassifierType;
	
	/**
	 * The PamTable Item for the classifier set (which classifier indexes the click has passed)
	 */
	private PamTableItem typeTable, mtThresholdsTable, mtMatchCorrsTable, mtRejectCorrsTable;
	
	/**
	 * The number of decimal places to to save as string in database.
	 */
	private static final int MT_DECIMAL_PLACES = 7;

	public MatchedClickAnnotationSQL(MatchedClickAnnotationType matchedClickType) {
		super();
		this.clickClassifierType = matchedClickType;
		typeTable = new PamTableItem("MT_Type", Types.INTEGER);
		mtThresholdsTable = new PamTableItem("MT_Thresholds", Types.CHAR, 256);
		mtMatchCorrsTable = new PamTableItem("MT_Match_Corrs", Types.CHAR, 256);
		mtRejectCorrsTable = new PamTableItem("MT_Reject_Corrs", Types.CHAR, 256);
	}

	@Override
	public void addTableItems(PamTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(typeTable);
		pamTableDefinition.addTableItem(mtThresholdsTable);
		pamTableDefinition.addTableItem(mtMatchCorrsTable);
		pamTableDefinition.addTableItem(mtRejectCorrsTable);

	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
				
		MatchedClickAnnotation clickAnnotation = 
				(MatchedClickAnnotation) pamDataUnit.findDataAnnotation(MatchedClickAnnotation.class);

		if (clickAnnotation==null) {
			Debug.err.println("Could not find matched click annotation for data unit: " + pamDataUnit.getUID());
//			Debug.out.println(pamDataUnit.getNumDataAnnotations()); 
//			for (int i=0;i<pamDataUnit.getNumDataAnnotations(); i++) {
//				Debug.out.println("Annotation: " +  i + "  " +  pamDataUnit.getDataAnnotation(i).getClass());
//			}
			return false;
		}
		
		typeTable.setValue(clickAnnotation.getClickType());
		
		//TODO - need to implement for multiple match click classifier templates. 
		//create a comma delimited string
		int nResults =clickAnnotation.getMatchedTemplateResult().size(); 
		double[] thresh = new double[nResults]; 
		double[] matchCorr = new double[nResults]; 
		double[] rejectCorr = new double[nResults]; 

		for (int i=0; i<nResults; i++) {
			thresh[i]= clickAnnotation.getMatchedTemplateResult().get(i).threshold;
			matchCorr[i]= clickAnnotation.getMatchedTemplateResult().get(i).matchCorr;
			rejectCorr[i]= clickAnnotation.getMatchedTemplateResult().get(i).rejectCorr;
		}
		
		mtThresholdsTable.setValue(PamArrayUtils.array2String(thresh, MT_DECIMAL_PLACES)); 
		mtMatchCorrsTable.setValue(PamArrayUtils.array2String(matchCorr, MT_DECIMAL_PLACES)); 
		mtRejectCorrsTable.setValue(PamArrayUtils.array2String(rejectCorr,MT_DECIMAL_PLACES)); 
	
		return true;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {

		String threshold		= mtThresholdsTable.getDeblankedStringValue();
		String mtMatchCorrs		= mtMatchCorrsTable.getDeblankedStringValue();
		String mtRejectCorrs	= mtRejectCorrsTable.getDeblankedStringValue();
		
		if (threshold==null || threshold==null || threshold==null) {
			Debug.err.println("MTClassifier info from database is null: " + pamDataUnit.getUID());
			return false; 
		}
		
		try {
			double[] thresholdsD 	= PamArrayUtils.string2array(threshold);
			double[] mtMatchCorrsD 	= PamArrayUtils.string2array(mtMatchCorrs);
			double[] mtRejectCorrsD = PamArrayUtils.string2array(mtRejectCorrs);
			
			List<MatchedTemplateResult> matchTemplateResults = new ArrayList<MatchedTemplateResult>(thresholdsD.length); 

			MatchedTemplateResult mtThresholdsResult; 
			for (int i=0; i<thresholdsD.length; i++) {
				mtThresholdsResult = new MatchedTemplateResult(); 
				mtThresholdsResult.threshold  = thresholdsD[i]; 
				mtThresholdsResult.matchCorr  = mtMatchCorrsD[i]; 
				mtThresholdsResult.rejectCorr = mtRejectCorrsD[i]; 
				matchTemplateResults.add(mtThresholdsResult); 
			}
			
			pamDataUnit.addDataAnnotation(new MatchedClickAnnotation(clickClassifierType, matchTemplateResults));
			
		}
		catch (Exception e){
			e.printStackTrace();
			return false; 
		}
		
		return true;
	}

	@Override
	public String getName() {
		return clickClassifierType.getAnnotationName();
	}

}

