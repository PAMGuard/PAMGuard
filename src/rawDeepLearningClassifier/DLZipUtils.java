package rawDeepLearningClassifier;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class DLZipUtils {
	
	/**
	 * Find the first file within a zip folder that matches a pattern. This peaks into the zip file instead of decompressing it. 
	 * @param zipUri - uri to the zip file
	 * @param filePattern - the file pattern to match - the file must contain this string. 
	 * @return null if no file found and the file pqth if the file is founf
	 * @throws ZipException
	 * @throws IOException
	 */
	public  static String getZipFilePath(URI zipUri, String filePattern) throws ZipException, IOException {
		return getZipFilePath(new File(zipUri),  filePattern);
	}
	
	
	/**
	 * Extract a single file from a zip folder.
	 * @param zipPackage - URI to the the zip file
	 * @param fileToBeExtracted - the name of the file to be extracted 
	 * @param - the folder path  to extract the file to.
	 * @return - return the File that  has been extracted.  
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public static File extractFile(String zipPackage, String fileToBeExtracted, String outFolder) throws IOException, URISyntaxException {
		 return extractFile( new URI(zipPackage),  fileToBeExtracted,  outFolder) ;
	}

	
	/**
	 * Extract a single file from a zip folder.
	 * @param zipPackage - path the the zip file
	 * @param fileToBeExtracted - the name of the file to be extracted 
	 * @param - the folder path  to extract the file to.
	 * @return - return the File that  has been extracted.  
	 * @throws IOException
	 */
	public static File extractFile(URI zipPackage, String fileToBeExtracted, String outFolder) throws IOException {
		File fileOut = new File(outFolder, fileToBeExtracted); 
		
		//need this incase the file is within a folder within the zip file.
		if (new File(fileOut.getParent()).exists() == false) {
			boolean directory = new File(fileOut.getParent()).mkdirs();
			if (!directory) {
				System.err.println("Could not create directory: " + fileOut.getParent());
				return null; // or throw an exception
			}
		}
		
        OutputStream out = new FileOutputStream(fileOut);
        
        
        FileInputStream fileInputStream = new FileInputStream(new File(zipPackage));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream );
        ZipInputStream zin = new ZipInputStream(bufferedInputStream);
        ZipEntry ze = null;
        while ((ze = zin.getNextEntry()) != null) {
        	//System.out.println("Extracting: " + ze.getName());
            if (ze.getName().equals(fileToBeExtracted)) {
                byte[] buffer = new byte[9000];
                int len;
                while ((len = zin.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.close();
                break;
            }
        }
        zin.close();
        
        return fileOut;
	}
	
	
	/**
	 * Find the first file within a zip folder that matches a pattern. 
	 * @param zipFile - uri to the zip file
	 * @param filePattern - the file pattern to match - the file must contain this string. 
	 * @return null if no file found and the file pqth if the file is founf
	 * @throws ZipException
	 * @throws IOException
	 */
	public static String getZipFilePath(File zipFileIn, String filePattern) throws ZipException, IOException {
			
		try (ZipFile zipFile = new ZipFile(zipFileIn)) {
		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    //this iterates through all files, including in sub folders. 
		    String name;
		    while (entries.hasMoreElements()) {
		        ZipEntry entry = entries.nextElement();
		        // Check if entry is a directory
		        name = new File(entry.getName()).getName();
		        if (!entry.isDirectory() && !name.startsWith(".")) {
		           //System.out.println(entry); 
		           if (entry.getName().contains(filePattern)) {
		        	   return entry.getName();
		           }
		        }
		    }
		}
		return null;
	}

}
