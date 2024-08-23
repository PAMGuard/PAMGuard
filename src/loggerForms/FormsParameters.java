package loggerForms;

import java.io.Serializable;

/**
 * Some general parameters for PAMGuard Viewer mode
 * @author dg50
 *
 */
public class FormsParameters implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;

	public boolean allowViewerChanges = false;

	@Override
	protected FormsParameters clone() {
		try {
			return (FormsParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
