package PamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
//example by pitchoun from http://www.theserverside.com/discussions/thread.tss?thread_id=34906
//FolderZipper provide a static method to zip a folder.
/**
 *  Description of the Class
 *
 * @author     cjb22
 * @created    22 September 2009
 */
public class FolderZipper {

	/**
	 * Zip the srcFolder into the destFileZipFile. All the folder subtree of the src folder is added to the destZipFile
	 * archive.
	 *
	 * TODO handle the usecase of srcFolder being en file.
	 *
	 * @param  srcFolder                 String, the path of the srcFolder
	 * @param  destZipFile               String, the path of the destination zipFile. This file will be created or erased.
	 * @param  useFolderNameAsRootInZip  Description of the Parameter
	 */
	public static void zipFolder(String srcFolder, String destZipFile, boolean useFolderNameAsRootInZip) {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;
		try {
			fileWriter = new FileOutputStream(destZipFile);
			zip = new ZipOutputStream(fileWriter);
		} catch (Exception ex) {
			ex.printStackTrace();
//			System.exit(0);
		}
		
		if (useFolderNameAsRootInZip) {
			addToZip(null, srcFolder, zip);
		} else {
			File folder = new File(srcFolder);
			String fileList[] = folder.list();
			try {
				int i = 0;
				while (true) {
					addToZip( null, srcFolder + File.separator + fileList[i], zip);
					i++;
				}
			} catch (Exception ex) {
			}
		}
		
		try {
			zip.flush();
			zip.close();
			fileWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Write the content of srcFile in a new ZipEntry, named path+srcFile, of the zip stream. The result
	 * is that the srcFile will be in the path folder in the generated archive.
	 *
	 * @param  path     String, the relatif path with the root archive.
	 * @param  srcFile  String, the absolute path of the file to add
	 * @param  zip      ZipOutputStram, the stream to use to write the given file.
	 */
	private static void addToZip(String path, String srcFile, ZipOutputStream zip) {

		File folder = new File(srcFile);
		String newEntry;
		System.out.println("addToZip:" + path + ":" + srcFile);
		if (folder.isDirectory()) {
			if (path !=null) {
					newEntry=path + File.separator + folder.getName();
				}
				else {
					newEntry= folder.getName();
				}
			addFolderToZip(path, srcFile, zip);
		} else {
// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			try {
				FileInputStream in = new FileInputStream(srcFile);
			        if (path !=null) {
					newEntry=path + File.separator + folder.getName();
				}
				else {
					newEntry= folder.getName();
				}
				zip.putNextEntry(new ZipEntry(newEntry));
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				//in.close();
			}
			
		}
	}


	/**
	 * add the srcFolder to the zip stream.
	 *
	 * @param  path       String, the relatif path with the root archive.
	 * @param  zip        ZipOutputStram, the stream to use to write the given file.
	 * @param  srcFolder  The feature to be added to the FolderToZip attribute
	 */
	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) {
		File folder = new File(srcFolder);
		String fileList[] = folder.list();
		String newEntry;
		
		System.out.println("addFolderToZip:" + path + ":" + srcFolder);

		try {
			for(int i=0;i<fileList.length;i++) {//was while true and exception was not displayed
				if (path !=null) {
					newEntry=path + File.separator + folder.getName();
				}
				else {
					newEntry= folder.getName();
				}
				addToZip(newEntry, srcFolder + File.separator + fileList[i], zip);
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

