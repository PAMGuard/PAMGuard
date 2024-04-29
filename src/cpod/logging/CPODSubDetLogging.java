package cpod.logging;

import PamguardMVC.PamDataUnit;
import cpod.CPODClickTrainDataBlock;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Logs the the children of the click train detection. 
 * @author Jamie Macaulay
 *
 */
public class CPODSubDetLogging extends SQLLogging {



		public CPODSubDetLogging(CPODClickTrainLogging clickTrainDetLogging, CPODClickTrainDataBlock clickTRainDaatBlock) {
			super(clickTRainDaatBlock);		
			setTableDefinition(new PamSubtableDefinition(clickTrainDetLogging.getClickTrainControl().getUnitName()+"_Children"));

		}

		@Override
		public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
			// TODO Auto-generated method stub

		}

	}