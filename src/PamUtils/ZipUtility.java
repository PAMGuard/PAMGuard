package PamUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
/**
 * http://www.crazysquirrel.com/computing/java/basics/java-directory-zipping.jspx
 * 
 *
 */
public class ZipUtility {

  public static final int zipDirectory( File directory, File zip) throws IOException {
	  
//	  deleteEmptyDirs(directory);
	  
	  
	  
    ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zip ) );
    
    
//    zos.setLevel(ZipOutputStream.STORED);
    int a = zip( directory, directory, zos );
    System.out.println("Close ZOS...");
      zos.close();
      return a;
  }
  
//  static boolean deleteEmptyDirs(File directory){
//	  boolean isEmpty=true
//	  File[] files = directory.listFiles();
//	  for (int i = 0; i <files.length ; i++) {
//	      if(files[i].isDirectory()){
//	    	  deleteEmptyDirs(files[i]);
//	      } else {
//	    	  
//	      }
//	  }
//	  return 
//  }
  
  /**
   * 
   * 
   * orders files in File[] in order 
   * files without extension, files with extension, directories. 
   * 
   * (Used for zipping open odb s so mimetype is at start of zipfile)
   * 
   * @param files
   * @return files in order
   */
  static File[] sortFilesList(File[] filesIn){
	  
	  File[] files=filesIn;
	  
	  boolean hasDirs = true;
	  boolean hasFilesWithExts = true;
	  boolean hasFilesWoExts = true;
	  
	  
	  Integer n=files.length;
	  if (n==null){
		  return null;
	  }
	  
	  File[] newList =new File[n];
	  
	  int j=0;
	  
	  while (hasDirs){
		  
		  while (hasFilesWithExts){
			  
			  while (hasFilesWoExts){
				  
				  for (int i=0;i<files.length;i++){
					  if (files[i]!=null&&files[i].isFile()&&files[i].getName().indexOf(".")==-1){
						  newList[j]=files[i];
						  j++;
						  files[i]=null;
					  }
				  }
				  hasFilesWoExts=false;
				  
			  }
			  
			  for (int i=0;i<files.length;i++){
				  if (files[i]!=null&&files[i].isFile()){
					  newList[j]=files[i];
					  j++;
					  files[i]=null;
				  }
			  }
			  hasFilesWithExts=false;
			  
		  }
		  
		  for (int i=0;i<files.length;i++){
			  if (files[i]!=null&&files[i].isDirectory()){
				  newList[j]=files[i];
				  j++;
				  files[i]=null;
			  }
		  }
		  hasDirs=false;
	  }
	  
	  return newList;
  }
  
  /**
   * may fail on directory with empty directory inside
   * @param directory
   * @param base
   * @param zos
   * @return number of files zipped
   * @throws IOException
   */
  private static final int zip(File directory, File base,
      ZipOutputStream zos) throws IOException {
    
	  int filesZipped=0;
    boolean hasFiles=directory.list()!=null;
    System.out.println("N Files in : "+directory.getName()+" = "+Boolean.toString(hasFiles));
    File[] files = directory.listFiles();
    files=sortFilesList(files);
    
//    System.out.println("dfiles is :  "+directory.getName()+files);
	    byte[] buffer = new byte[8192];
	    int read = 0;
	    for (int i = 0; i <files.length ; i++) {
	    	
	      if (files[i].isDirectory()&&files[i].listFiles().length>0) {
	    	  System.out.println(files[i].getName()+" isDir with files");
	        filesZipped=filesZipped+zip(files[i], base, zos);
	      } else if(files[i].isDirectory()){
	    	  //Do Nothing
	    	  
	    	  
	    	  /*
	    	   * may fail on directory with empty directory inside-Doesn't
	    	   */
	    	  
	    	  
	    	  System.out.println(files[i].getName()+" isDir without files");
	    	  
	      } else if(files[i].isFile()&&files[i].getName().endsWith("mimetype")){
//	    	  filesZipped=filesZipped+1;
//	    	  System.out.println(files[i].getName()+" is not Dir :ZIP! uncompressed");
//	    	  
//		        FileInputStream in = new FileInputStream(files[i]);
//		        ZipEntry entry = new ZipEntry(files[i].getPath().substring(
//		            base.getPath().length() + 1));
//		        entry.setMethod(ZipOutputStream.STORED);
//		        entry.setSize(files[i].length());
//		        CRC32 crc = new CRC32();
//		        crc.update((int) files[i].length());
//		        entry.setCrc(crc.getValue());
//
//		        System.out.println("chksum "+entry.getCrc());
//		        System.out.println("ent siz"+entry.getSize());
//
//		        System.out.println("entCsiz"+entry.getCompressedSize());
//		        
//		        
//		        zos.putNextEntry(entry);
//		        while (-1 != (read = in.read(buffer))) {
//		          zos.write(buffer, 0, read);
//		        }
//		        
////		        entry.setMethod(ZipOutputStream.DEFLATED);
//		        in.close();
	      
	    	} else if(files[i].isFile()&&!files[i].getName().endsWith("mimetype")){  
	    	  filesZipped=filesZipped+1;
	    	  System.out.println(files[i].getName()+" is not Dir :ZIP!");
	        FileInputStream in = new FileInputStream(files[i]);
	        ZipEntry entry = new ZipEntry(files[i].getPath().substring(
	            base.getPath().length() + 1));
//	        entry.setMethod(ZipOutputStream.DEFLATED);
	        
//	        CRC32 crc = new CRC32();
	        
//	        entry.setCrc(crc.getValue());
	        System.out.println("chksum "+entry.getCrc());
	        System.out.println("ent siz"+entry.getSize());

	        System.out.println("entCsiz"+entry.getCompressedSize());
	        zos.putNextEntry(entry);
	        while (-1 != (read = in.read(buffer))) {
	          zos.write(buffer, 0, read);
	        }
	        in.close();
	      }
	  }
	    return filesZipped;
    
  }

  public static final void unzip(File zip, File extractTo) throws IOException {
    ZipFile archive = new ZipFile(zip);
    Enumeration e = archive.entries();
    while (e.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) e.nextElement();
      File file = new File(extractTo, entry.getName());

      System.out.println(entry.getName()+ " is Dir: "+entry.getName().endsWith("/"));
      System.out.println(file.getAbsolutePath()+" exists: "+file.exists());
      if (entry.getName().endsWith("/") && !file.exists()) {
    	  
        file.mkdirs();
        FileFunctions.setNonIndexingBit(file);
        
      } else if(entry.getName().endsWith("/")){
        
      } else if(!file.exists()){
        if (!file.getParentFile().exists()) {
          file.getParentFile().mkdirs();
          FileFunctions.setNonIndexingBit(file.getParentFile());
        }

        InputStream in = archive.getInputStream(entry);
//        System.out.println(file);
        BufferedOutputStream out = new BufferedOutputStream(
            new FileOutputStream(file));

        byte[] buffer = new byte[8192];
        int read;

        while (-1 != (read = in.read(buffer))) {
          out.write(buffer, 0, read);
        }

        in.close();
        out.close();
        
      }else{
    	System.out.println(file +" already exists!!");  
      }
    }
  }
}
