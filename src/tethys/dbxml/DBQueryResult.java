package tethys.dbxml;

public class DBQueryResult {

	public long queryTimeMillis;
	
	public String queryResult;
	
	public String schemaPlan;
	
	public Exception queryException;

	public DBQueryResult(long queryTimeMillis, String queryResult, String schemaPlan) {
		super();
		this.queryTimeMillis = queryTimeMillis;
		this.queryResult = queryResult;
		this.schemaPlan = schemaPlan;
	}

	public DBQueryResult(long queryTimeMillis, Exception queryException) {
		super();
		this.queryTimeMillis = queryTimeMillis;
		this.queryException = queryException;
	}
	
}
