package generalDatabase.clauses;

import PamController.PamViewParameters;
import generalDatabase.SQLTypes;

public class FixedClause extends PamViewParameters {

	private String clause;

	public FixedClause(String clause) {
		this.clause = clause;
	}

	/* (non-Javadoc)
	 * @see PamController.PamViewParameters#getSelectClause(generalDatabase.SQLTypes)
	 */
	@Override
	public String getSelectClause(SQLTypes sqlTypes) {
		return clause;
	}

}
