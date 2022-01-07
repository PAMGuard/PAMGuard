package generalDatabase;

import generalDatabase.pamCursor.PamCursor;

import java.awt.Component;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;

abstract public class BaseAccessSystem extends DBSystem {

	/**
	 * @return the recentDatabases
	 */
	abstract public ArrayList<File> getRecentDatabases();


}
