package generalDatabase.clauses;

import generalDatabase.SQLTypes;

public abstract class PAMSelectClause {

	public PAMSelectClause() {
	}

	public abstract String getSelectClause(SQLTypes sqlTypes);

}
