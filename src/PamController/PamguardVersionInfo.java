package PamController;

/**
 * Class to hold static license and release information for PAMGUARD.
 * Should be updated for each major and minor release. 
 * 
 * @author Douglas Gillespie
 *
 */
public class PamguardVersionInfo {

	public static enum ReleaseType {CORE, BETA, OTHER};
	
	/**
	 * Type of release - used to switch off and on some features. 
	 * @return release type 
	 */
	static public ReleaseType getReleaseType() {
		return ReleaseType.BETA; 
	}
	
	/**
	 * Set the minimum and maximum Java versions that this version of
	 * PAMGuard can work with. 
	 */
	static public final String minJavaVersion = "11.0.0";
	static public final String maxJavaVersion = "21.99.99";
	

	/**
	 * Version number, major version.minorversion.sub-release.
	 * Note: can't go higher than sub-release 'f'
	 */
	static public final String version = "2.02.11c";
	
	/**
	 * Release date
	 */
	static public final String date = "29 April 2024";
	
//	/**
//	 * Release type - Beta or Core
//	 */
//	static public final String release = "SMRU";

	/**
	 * This is redundant now that we're no longer using SVN. 
	 * Will have to do a better job of updating the version number
	 * and correctly tagging code in the git repo. 
	 */
//	static public final String revisionString = "$Rev: 6240 $";
	
	/**
	 * GNU License statement
	 */
	static public final String license = "This program is free software: you can redistribute it and/or modify " +
    "it under the terms of the GNU General Public License as published by " +
    "the Free Software Foundation, either version 3 of the License, or " +
    "(at your option) any later version. " +
    "\n\n" +
    "This program is distributed in the hope that it will be useful, " +
    "but WITHOUT ANY WARRANTY; without even the implied warranty of " +
    "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " +
    "GNU General Public License for more details. " +
    "\n\n" +
    "You should have received a copy of the GNU General Public License " +
    "along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>." +
    "";
	
	/**
	 * Pamguard web address
	 */
	static public final String webAddress = "www.pamguard.org";
	

	
//	/**
//	 * @return the code revision number from the SVN repository
//	 */
//	static public int getRevision() {
//		try {
//			int spacePos = revisionString.indexOf(' ');
//			if (spacePos == -1) {
//				return 0;
//			}
//			String newStr = revisionString.substring(spacePos+1);
//			spacePos = newStr.indexOf(' ');
//			if (spacePos > 0) {
//				newStr = newStr.substring(0, spacePos);
//			}
//			return Integer.valueOf(newStr);
//		}
//		catch (NumberFormatException e) {
//			return 0;
//		}
//	}

	/**
	 * Don't delete this - it's required by the installer builder.  
	 * @param args
	 */
	public static void main(String[] args){
//		System.out.println("VerNam:"+getReleaseType().toString()+":VerNum:"+version+":Rev:"+getRevision()+":D:"+date);
		System.out.println("VerNam:"+getReleaseType().toString()+":VerNum:"+version+":D:"+date);
	}
	
}
