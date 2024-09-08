package cpod.logging;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import cpod.CPODClassification;
import cpod.CPODClassification.CPODSpeciesType;
import cpod.CPODClickTrainDataUnit;
import cpod.CPODControl2;
import cpod.CPODUtils;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;

public class CPODClickTrainLogging extends SuperDetLogging {
	
	/**
	 * The pam table items for saving.
	 */
	private PamTableItem species, quality, isEcho, cpodFileID;

	private CPODControl2 cpodControl;

	public CPODClickTrainLogging(CPODControl2 cpodControl, SuperDetDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.cpodControl=cpodControl;
		setTableDefinition(createBaseTable());
	}

	
	/**
	 * Create the basic table definition for the group detection. 
	 * @return basic table - annotations will be added shortly !
	 */
	public PamTableDefinition createBaseTable() {
		
		PamTableDefinition tableDef = new PamTableDefinition(cpodControl.getUnitName(), UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(cpodFileID 	= new PamTableItem("CPOD_file_ID", Types.INTEGER));
		tableDef.addTableItem(species 	= new PamTableItem("Species", Types.CHAR, 10));
		tableDef.addTableItem(quality 	= new PamTableItem("Confidence", Types.INTEGER));
		tableDef.addTableItem(isEcho 	= new PamTableItem("isEcho", Types.BOOLEAN));

		return tableDef;
	}
	
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		CPODClickTrainDataUnit ctDataUnit = (CPODClickTrainDataUnit) pamDataUnit;
		if (ctDataUnit.getSubDetectionsCount()>0) {
			species.setValue(ctDataUnit.getSpecies().toString());
			quality.setValue(ctDataUnit.getConfidence());
			isEcho.setValue(ctDataUnit.isEcho());

		}
		
	}
	
	@Override
	protected CPODClickTrainDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {

		
		CPODClassification classification = new CPODClassification();
		
		String speciesStr = species.getStringValue(); 
		
		CPODSpeciesType speciesType = CPODUtils.getSpecies(speciesStr);
		
		classification.clicktrainID = cpodFileID.getIntegerValue();
		classification.species = speciesType;
		classification.clicktrainID = cpodFileID.getIntegerValue();
		classification.isEcho = isEcho.getBooleanValue();

		CPODClickTrainDataUnit dataUnit = new CPODClickTrainDataUnit(timeMilliseconds, null, classification);

		return dataUnit;
	}


	public CPODControl2 getClickTrainControl() {
		return cpodControl;
	}


}
