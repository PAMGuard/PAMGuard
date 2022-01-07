package generalDatabase.sqlite;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

public class SQLiteParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private ArrayList<File> recentDatabases = new ArrayList<File>();
	
	private String databaseName; // only used in outputting parameter xml

	@Override
	protected SQLiteParameters clone()  {
		try {
			SQLiteParameters newParams = (SQLiteParameters) super.clone();
			newParams.recentDatabases = new ArrayList<File>();
			if (this.recentDatabases != null) {
				newParams.recentDatabases.addAll(this.recentDatabases);
			}
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return this;
		}
		
	}

	/**
	 * @return the recentDatabases
	 */
	public ArrayList<File> getRecentDatabases() {
		return recentDatabases;
	}

	/**
	 * @param recentDatabases the recentDatabases to set
	 */
	public void setRecentDatabases(ArrayList<File> recentDatabases) {
		this.recentDatabases = recentDatabases;
	}


	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet paramSet = new PamParameterSet(this);
		if (recentDatabases != null && recentDatabases.size() > 0) {
			databaseName = recentDatabases.get(0).getAbsolutePath();
			try {
				Field field = this.getClass().getDeclaredField("databaseName");
				PrivatePamParameterData ppd;
				paramSet.put(ppd = new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return databaseName;
					}
				});
				ppd.setToolTip("SQLite database name");
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
		}
				
				
		return paramSet;
	}
}
