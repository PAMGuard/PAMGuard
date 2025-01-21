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
	public final static int LARGE = 3;
	public final static int OLD = 4;


	/**
	 * Get the  path to the default icon for PAMGuard.
	 * @param sizeFlag - the size flag. 
	 * @return
	 */
	public static String getPAMGuardIconPath(int sizeFlag) {
		
		//Note: whether the there is a / in front of resource is very important here
		//putting a / in front of the Resources will mean the class loader will not work
		

		//the default is windows. Icon automatically resize on Windows. 
		// Even on Windows, use a / slash. A \\ doesn't work (OK in debugger, not in built version)
//		String path = "Resources/pamguardIcon.png";
		String path = "Resources/PAMGuardIcon2Opaque.png";

		if (System.getProperty("os.name").equals("Linux") 
				|| System.getProperty("os.name").startsWith("Mac")
				|| System.getProperty("os.name").startsWith("Win")) {
			switch (sizeFlag) {
			case SMALL:
				path = "Resources/PAMGuardIcon2small.png";
				break;
			case NORMAL:
				path = "Resources/PAMGuardIcon2medium.png";
				break;
			case LARGE:
				path = "Resources/PAMGuardIcon2.png";
				break;
			case OLD:
				path = "Resources/pamguardIconM.png";
				break;
			}
		}
		else {
			switch (sizeFlag) {
			case SMALL:
				path = "Resources/PAMGuardIcon2small.png";
				break;
			case NORMAL:
				path = "Resources/PAMGuardIcon2medium.png";
				break;
			}
		}

		return path; 

	}
	
	public static ImageIcon getPAMGuardImageIcon(int sizeFlag) {
		return new ImageIcon(ClassLoader.getSystemResource(getPAMGuardIconPath(sizeFlag)));
	}
	
	
	public static ImageIcon getPAMGuardImageIcon() {
		return getPAMGuardImageIcon(NORMAL);
	}


}
