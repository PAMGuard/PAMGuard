package generalDatabase;

import PamguardMVC.PamDataUnit;

/**
 * simple abstract class for any extra database functions which 
 * do not easily work with PamDataUnits and PamDatablocks
 * 
 * @author Doug Gillespie
 *
 */
abstract public class DbSpecial extends SQLLogging{

	private DBControl dbControl;
	
	public DbSpecial(DBControl dbControl) {
		super(null);
		this.dbControl = dbControl;
	}

	public boolean logData(PamDataUnit dataUnit) {
		return super.logData(dbControl.getConnection(), dataUnit);
	}

	abstract public void pamStart(PamConnection con);
	
	abstract public void pamStop(PamConnection con);

	/**
	 * @return the dbControl
	 */
	public DBControl getDbControl() {
		return dbControl;
	}
	
	
}
