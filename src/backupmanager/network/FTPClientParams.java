package backupmanager.network;

import java.io.Serializable;


public class FTPClientParams implements Cloneable, Serializable {
	
	/**
	 * 
	 */
	static final long serialVersionUID = -3256867953095135976L;
	public String host;
	public String password;
	public String user;
	
	
	public FTPClientParams clone() {
		try {
			FTPClientParams params = (FTPClientParams) super.clone();
			params.host=host;
			params.password = password;
			params.user = user;
			return params;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	

}
