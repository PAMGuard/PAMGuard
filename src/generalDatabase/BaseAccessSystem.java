package generalDatabase;

import java.io.File;
import java.util.ArrayList;

abstract public class BaseAccessSystem extends DBSystem {

	/**
	 * @return the recentDatabases
	 */
	abstract public ArrayList<File> getRecentDatabases();


}
