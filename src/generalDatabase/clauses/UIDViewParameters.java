package generalDatabase.clauses;

import java.util.ListIterator;

import PamController.PamViewParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.SQLTypes;

/**
 * Modification of PAMViewParameters to work using a list of UID's rather than
 * a between times type string.  
 *  
 * @author dg50
 *
 */
public class UIDViewParameters extends PamViewParameters {

	private static final long serialVersionUID = 1L;
	
	private String clause;
	
//	public UIDViewParameters(String columnName, long[] uidList) {
//		clause = makeClause(columnName, uidList);
//	}
	
	private UIDViewParameters() {
		
	}

	public static UIDViewParameters createUIDViewParameters(PamDataBlock parentBlock) { 
		return createUIDViewParameters("ParentUID", parentBlock);
	}
	
	public static UIDViewParameters createUIDViewParameters(String columnName, PamDataBlock parentBlock) { 
		UIDViewParameters viewParams = new UIDViewParameters();
		long[] uidList = null;
		synchronized (parentBlock.getSynchLock()) {
			int n = parentBlock.getUnitsCount();
			uidList = new long[n];
			ListIterator<PamDataUnit> it = parentBlock.getListIterator(0);
			int i = 0;
			while (it.hasNext()) {
				uidList[i++] = it.next().getUID();
			}
		}
		viewParams.makeClause(columnName, uidList);
		return viewParams;
	}

	public static UIDViewParameters createDatabaseIDViewParameters(PamDataBlock parentBlock) { 
		return createDatabaseIDViewParameters("ParentID", parentBlock);
	}
	
	public static UIDViewParameters createDatabaseIDViewParameters(String columnName, PamDataBlock parentBlock) { 
		UIDViewParameters viewParams = new UIDViewParameters();
		long[] uidList = null;
		synchronized (parentBlock.getSynchLock()) {
			int n = parentBlock.getUnitsCount();
			uidList = new long[n];
			ListIterator<PamDataUnit> it = parentBlock.getListIterator(0);
			int i = 0;
			while (it.hasNext()) {
				uidList[i++] = it.next().getDatabaseIndex();
			}
		}
		viewParams.makeClause(columnName, uidList);
		return viewParams;
	}

	private void makeClause(String columnName, long[] uidList) {
		String c = String.format(" WHERE %s IN (", columnName);
		for (int i = 0; i < uidList.length; i++) {
			c += uidList[i];
			if (i < uidList.length-1) 
				c += ",";
		}
		c += ")";
		clause = c;
	}

	/* (non-Javadoc)
	 * @see PamController.PamViewParameters#getSelectClause(generalDatabase.SQLTypes)
	 */
	@Override
	public String getSelectClause(SQLTypes sqlTypes) {
		return clause;
	}

}
