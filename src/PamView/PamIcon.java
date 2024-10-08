package PamView;

import javax.swing.ImageIcon;

/**
 * Get the PAMGuard icon. 
 * <p>
 * Handles the PAMGuard icon depending on operating system. 
 */
public class PamIcon {

	public final static int SMALL = 1;
	public final static int NORMAL = 2;


	/**
	 * Get the  path to the default icon for PAMGuard.
	 * @param sizeFlag - the size flag. 
	 * @return
	 */
	public static String getPAMGuardIconPath(int sizeFlag) {

		//the default is windows. Icon automatically resize on Windows. 
		String path = "/Resources/pamguardIcon.png";

		if (System.getProperty("os.name").equals("Linux") || System.getProperty("os.name").startsWith("Mac")) {
			switch (sizeFlag) {
			case SMALL:
				path = "/Resources/PAMGuardIcon2small.png";
				break;
			case NORMAL:
				path = "/Resources/PAMGuardIcon2.png";
				break;
			}
		}

		System.out.println("JHello path: " + path); 
		return path; 

	}
	
	public static ImageIcon getPAMGuardImageIcon(int sizeFlag) {
		return new ImageIcon(ClassLoader.getSystemResource(getPAMGuardIconPath(sizeFlag)));
	}
	
	
	public static ImageIcon getPAMGuardImageIcon() {
		return getPAMGuardImageIcon(NORMAL);
	}


}
