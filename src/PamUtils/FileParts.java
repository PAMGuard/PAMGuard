package PamUtils;

import java.io.File;
import java.net.URI;

/**
 * Class for breaking a file name down into it's constituent 
 * parts. 
 * 
 * @author Doug Gillespie
 *
 */
public class FileParts {

	private String fileName;
	
	private String folderName;
	
	private String fileEnd;
	
	private String[] folderParts;
	
	public FileParts() {
		
	}
	
	public FileParts(File file) {
		setFileName(file.getAbsolutePath());
	}
	
	public FileParts(String absPath) {
		setFileName(absPath);
	}
	
	public void setFileName(String absPath) {
		// turn it back into a file !
		File f = new File(absPath);
		if (f.isDirectory()) {
			fileName = fileEnd = null;
			folderName = absPath;
		}
		else {
			String name = f.getName();
			folderName = f.getParent();
			int dot = name.indexOf('.');
			if (dot > 0) {
			fileName = name.substring(0, dot);
			fileEnd = name.substring(dot+1);
			}
			else {
				fileName = name;
				fileEnd = null;
			}
		}
		// find the positions of all the / and \ characters. 
		if (folderName == null) {
			folderName = null;
			folderParts = null;
			return;
		}
		else {
			int nBreaks = 0;
			int breakPos = -1;
			char sepChar = File.separatorChar;
			while ((breakPos = folderName.indexOf(sepChar, breakPos+1)) >= 0) {
				nBreaks++;
			}
			folderParts = new String[nBreaks+1];
			nBreaks = 0;
			breakPos = -1;
			int lastPos = 0;
			while ((breakPos = folderName.indexOf(sepChar, breakPos+1)) >= 0) {
				folderParts[nBreaks] = folderName.substring(lastPos, breakPos);
				lastPos = breakPos+1;
				nBreaks++;
			}
			folderParts[nBreaks] = folderName.substring(lastPos);
			
		}
	}

	public String getFileName() {
		return fileName;
	}

	public String getFolderName() {
		return folderName;
	}

	public String getFileEnd() {
		return fileEnd;
	}
	
	public String getFileNameAndEnd() {
		String str = fileName;
		if (str == null || fileEnd == null) {
			return str;
		}
		str += File.separatorChar + fileEnd;
		return str;
	}

	public String[] getFolderParts() {
		return folderParts;
	}
	
	public String getLastFolderPart () {
		if (folderParts == null) {
			return null;
		}
		return folderParts[folderParts.length-1];
	}
	
	static private String fileSep;
	static public String getFileSeparator() {
//		if (fileSep == null || fileSep.length() == 0) {
//			fileSep = System.getProperty("file.separator");
//			if (fileSep == null) {
//				fileSep = "\\";
//			}
//		}
//		return fileSep;
		return File.separator;
	}
	
	/**
	 * @author Graham Weatherup
	 * 
	 * @param aFileOrDir
	 * @param aHigherDirectory
	 * @return Directories between file/dir in question and the higher
	 *  directory specified. Returns null if not in the higher directory at all.
	 *  NB Uses isWithin!
	 *  <p>
	 *  Add another String to the array with the file/dir name to give a "relative file"
	 */
	public static String[] getFolderPartsBetween(File aFileOrDir, File aHigherDirectory){
		String[]a;
		Integer num=isWithin(aFileOrDir, aHigherDirectory);
		if (num==null){
			return null;
		}else{
			FileParts A=new FileParts(aFileOrDir);
			int i=num;
			String[] filePartsBelow= new String[num];
			String[] full = A.getFolderParts();
			while(i>0){
				filePartsBelow[i] = full[full.length-i];
			}
			return filePartsBelow;
		}
	}
	
	/**
	 * @author Graham Weatherup
	 * 
	 * @param aFileOrDir
	 * @param aHigherDirectory
	 * @return depth of subfolders of file/dir in higher directory 
	 * NB: Based on Abstract Paths. If referring to same file through different route, will return false.
	 * Eg. with a net drive mapped as both N and M; isWithin(new File("M:\\foo\\bar.txt"),new File("N:\\foo")) will return false! 
	 */
	public static Integer isWithin(File aFileOrDir, File aHigherDirectory){
		File parent = aFileOrDir.getParentFile();
		Integer numLevelsBetween=0;
		while (parent!=null){
			if (parent.equals(aHigherDirectory)){
				return numLevelsBetween;
			}else{
				parent=parent.getParentFile();
				numLevelsBetween++;
			}
			
		}
		return null;
	}
	
	/**
	 * @author Graham Weatherup
	 * 
	 * @param aDir
	 * @param deeperFileOrDir
	 * @param c unused but to make it different from other method!
	 * @return true if aDir contains deeperFileDir
	 */
	public static boolean isWithin(File aDir, File deeperFileOrDir, boolean c){
		URI relPath = aDir.toURI().relativize(deeperFileOrDir.toURI());
		boolean isWithin = relPath!=null;
		return isWithin;
	}
}
