package whistleClassifier.logging;

import java.sql.Types;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import classifier.Classifier;
import generalDatabase.DBControl;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import whistleClassifier.WhistleClassificationDataUnit;
import whistleClassifier.WhistleClassificationParameters;
import whistleClassifier.WhistleClassifierControl;
import whistleClassifier.WhistleClassifierProcess;

public class WhistleClassifierLogging extends SQLLogging {

	WhistleClassifierControl whistleClassifierControl;

	WhistleClassifierProcess whistleClassifierProcess;

	PamTableDefinition tableDefinition;

	PamTableItem speciesName;
	PamTableItem nFragments;
	PamTableItem duration; // seconds
	PamTableItem[] speciesProbabilities;

	private String[] speciesNames;

	private PamTableItem lowFreqItem;

	private PamTableItem highFreqItem;

	private static int DEFSPECIESNAMELEN = 20;



	public WhistleClassifierLogging(WhistleClassifierProcess whistleClassifierProcess, PamDataBlock pamDataBlock) {
		super(pamDataBlock);

		setCanView(false);

		this.whistleClassifierProcess = whistleClassifierProcess;
		whistleClassifierControl = whistleClassifierProcess.getWhistleClassifierControl();


		tableDefinition = createTableDefinition();
		tableDefinition.setUseCheatIndexing(true);

	}

	public PamTableDefinition createTableDefinition() {

		PamTableDefinition tableDef = new PamTableDefinition(whistleClassifierControl.getUnitName(), UPDATE_POLICY_WRITENEW);

		Classifier fragmentClassifier = whistleClassifierControl.getFragmentClassifier();
		speciesNames = null;
		speciesProbabilities = null;
		WhistleClassificationParameters params = whistleClassifierControl.getWhistleClassificationParameters();
		if (params.fragmentClassifierParams != null) {
			speciesNames = params.fragmentClassifierParams.getSpeciesList();
		}
		//		String[] speciesNames = fragmentClassifier.getSpeciesList();
		int maxLen = DEFSPECIESNAMELEN;
		if (speciesNames != null) {
			for (int i = 0; i < speciesNames.length; i++) {
				maxLen = Math.max(maxLen, speciesNames[i].length()+1);
			}
			tableDef.addTableItem(speciesName = new PamTableItem("Species", Types.CHAR, maxLen));
			tableDef.addTableItem(duration = new PamTableItem("Duration", Types.DOUBLE));
			tableDef.addTableItem(lowFreqItem = new PamTableItem("lowFrequency", Types.DOUBLE));
			tableDef.addTableItem(highFreqItem = new PamTableItem("highFrequency", Types.DOUBLE));
			tableDef.addTableItem(nFragments = new PamTableItem("N Fragments", Types.INTEGER));
			speciesProbabilities = new PamTableItem[speciesNames.length];
			for (int i = 0; i < speciesNames.length; i++) {
				tableDef.addTableItem(speciesProbabilities[i] = new PamTableItem(speciesNames[i], Types.DOUBLE));
			}
		}

		setTableDefinition(tableDef);
		return tableDef;

	}

	/**
	 * called whenever the classifier configuration has changed so that
	 * new table columns can be created in the database. 
	 */
	public void checkLoggingTables() {
		/*
		 * No point in doing anything at all if there isn't a database
		 * connection
		 */
		DBControl dbControl = (DBControl) PamController.getInstance().findControlledUnit(DBControl.getDbUnitType());
		if (dbControl == null) {
			return;
		}
		
		String[] newList = null;
		WhistleClassificationParameters params = whistleClassifierControl.getWhistleClassificationParameters();
		if (params.fragmentClassifierParams != null) {
			newList = params.fragmentClassifierParams.getSpeciesList();
		}
		if (!speciesListChanged(newList)) {
			return; // no need to do anything. 
		}
		// will need to re-make the table definition and run checks to ensure that 
		// the correct tables are in place. 
		// first recreate the table definition
		tableDefinition = createTableDefinition();
		// then tell the main database process to check 
		// the table.
		dbControl.getDbProcess().checkTable(tableDefinition);
		// that should be all that's necessary. 
	}
	
	/**
	 * 
	 * @param newList new species list
	 * @return true if the list of species has changed in any way at all. 
	 */
	public boolean speciesListChanged(String[] newList) {
		if (newList == null && speciesNames == null) {
			// both null so nothing has changed.
			return false;
		}
		if (newList == null || speciesNames == null) {
			// one is null so something must have changed 
			return true;
		}
		// neither can be null at this point. 
		if (newList.length != speciesNames.length) {
			// different length lists, so something has changed.
			return true;
		}
		for (int i = 0; i < newList.length; i++) {
			// database column names are not case sensitive, so 
			// we don't care about case. 
			if (!newList[i].equalsIgnoreCase(speciesNames[i])) {
				return true;
			}
		}
		return false; // the two lists are the same. 
	}

//	@Override
//	public PamTableDefinition getTableDefinition() {
//		return tableDefinition;
//	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		WhistleClassificationDataUnit wcdu = (WhistleClassificationDataUnit) pamDataUnit;
		if (speciesName != null && speciesProbabilities != null) {
			speciesName.setValue(wcdu.getSpecies());
			nFragments.setValue(wcdu.getNFragments());
			Double dur = wcdu.getDurationInMilliseconds();
			if (dur != null) {
				duration.setValue(dur/1000.);
			}
			else {
				duration.setValue(null);
			}
			double[] fR = wcdu.getFrequency();
			if (fR == null || fR.length != 2) {
				lowFreqItem.setValue(null);
				highFreqItem.setValue(null);
			}
			else {
				lowFreqItem.setValue(fR[0]);
				highFreqItem.setValue(fR[1]);
			}
			
			double[] speciesProbs = wcdu.getSpeciesProbabilities();
			for (int i = 0; i < speciesProbs.length; i++) {
				speciesProbabilities[i].setValue(speciesProbs[i]);
			}
		}
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		WhistleClassificationDataUnit wcdu = new WhistleClassificationDataUnit(timeMilliseconds, 0, 0, 0);
		wcdu.setDatabaseIndex(databaseIndex);
		if (speciesName == null) {
			return null;
		}
		wcdu.setSpecies(speciesName.getDeblankedStringValue());
		wcdu.setNFragments(nFragments.getIntegerValue());
		int nSpecies = speciesProbabilities.length;
		double[] probs = new double[nSpecies];
		for (int i = 0; i < nSpecies; i++) {
			probs[i] = speciesProbabilities[i].getDoubleValue();
		}
		wcdu.setSpeciesProbabilities(probs);
		Double dur = duration.getDoubleValue();
		if (!Double.isNaN(dur)) {
			wcdu.setDurationInMilliseconds(dur*1000.);
		}
		double f1 = lowFreqItem.getDoubleValue();
		double f2 = highFreqItem.getDoubleValue();
		if (Double.isNaN(f1*f2) == false) {
			double[] f = {f1, f2};
			wcdu.setFrequency(f);
		}
//		getPamDataBlock().addPamData(wcdu);
		return wcdu;
	}



}
