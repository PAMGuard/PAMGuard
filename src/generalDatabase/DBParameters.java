package generalDatabase;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

public class DBParameters implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 0;

	private int databaseSystem = 0;

	private Boolean useAutoCommit = false;
	
	/**
	 * This is only added so that it appears in the XML parameter set, so has
	 * a setter, but no getter. 
	 */
	private String databaseName;

	//  String databaseName = "C:\\Pamguard\\TestDB.mdb";
	//  
	//  String userName = "";
	//  
	//  String passWord = "";

	@Override
	public DBParameters clone() {
		try {
			return (DBParameters) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public boolean getUseAutoCommit() {
		if (useAutoCommit == null) {
			useAutoCommit = true;
		}
		return useAutoCommit;
	}

	public void setUseAutoCommit(boolean useAutoCommit) {
		this.useAutoCommit = useAutoCommit;
	}

	/**
	 * Set the database system.
	 * @param dBIndexSystem - the index of the system to set
	 */
	public void setDataBaseSystem(int dBIndexSystem) {
		this.setDatabaseSystem(dBIndexSystem);
	}

	/**
	 * Get the current database system
	 * @return the current database system
	 */
	public int getDatabaseSystem() {
		return databaseSystem;
	}

	/**
	 * Set the current database system
	 * @param databaseSystem- the database system to set. 
	 */
	public void setDatabaseSystem(int databaseSystem) {
		this.databaseSystem = databaseSystem;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		if (databaseName != null) {
			try {
				Field field = this.getClass().getDeclaredField("databaseName");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return databaseName;
					}
				});
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
		try {
			Field field = this.getClass().getDeclaredField("useAutoCommit");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return Boolean.valueOf(useAutoCommit);
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("databaseSystem");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return Integer.valueOf(databaseSystem);
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	/**
	 * @param databaseName the databaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

}
