package PamView.dialog.warn;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Serializable list of which warnings should be hidden. 
 * @author dg50
 *
 */
public class WarnOnceList implements Serializable, Cloneable{


	public static final long serialVersionUID = 1L;

	private Hashtable<String, Boolean> listChoice = new Hashtable<>();
	
	public boolean isShowWarning(String warningName) {
		Boolean ans = listChoice.get(warningName);
		if (ans == null) {
			return true;
		}
		return ans;
	}
	
	public void setShowWarning(String warningName, boolean show) {
		listChoice.put(warningName, show);
	}

	public void clearList() {
		listChoice.clear();
	}

}
