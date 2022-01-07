package generalDatabase.clauses;

import generalDatabase.SQLTypes;

public class FromClause extends PAMSelectClause {

	private long fromTime;

	public FromClause() {
		// TODO Auto-generated constructor stub
	}

	public FromClause(long fromTime) {
		super();
		this.fromTime = fromTime;
	}

	@Override
	public String getSelectClause(SQLTypes sqlTypes) {
		String str = String.format("WHERE UTC > %s", sqlTypes.formatDBDateTimeQueryString(fromTime));
		return str;
	}

}
