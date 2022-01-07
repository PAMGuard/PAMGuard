package backupmanager.bespoke;

import java.io.Serializable;
import java.util.ArrayList;

public class BespokeSettings implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;

	public ArrayList<BespokeIdentity> identities = new ArrayList<BespokeIdentity>();
	
}
