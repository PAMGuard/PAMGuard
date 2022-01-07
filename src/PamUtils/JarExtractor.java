/**
 * 
 */
package PamUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author David J McLaren
 *
 */
public class JarExtractor {

	public void extractFileFromJarRoot(String fileToExtract){

		File dir1 = new File (".");

		try {
			/*String home = getClass().getProtectionDomain().
			getCodeSource().getLocation().toString().substring(10);*/
			String home = getClass().getProtectionDomain().
			getCodeSource().getLocation().toString();
			
			
			home = home.replaceAll("%20", " ");
			//home=home.substring(0,home.lastIndexOf("/")+1);
			
			home = home.substring(home.indexOf("/")+1);
			
			
			//int pos = home.indexOf("!");

//			System.out.println("home: " + home);
			
			//home = home.substring(0, pos);     
			JarFile jar = new JarFile(home);
			

			/*		      Enumeration enumJars = jar.entries();
		      while (enumJars.hasMoreElements(  )) {
		    	  System.out.println("enum jar elemnts: " + enumJars.nextElement().toString());
		      }*/

			JarEntry entry = jar.getJarEntry(fileToExtract);

			if(entry==null){
				System.out.println("Entry is null!!");
				return;
			}

			//"rxtxSerial.dll"
			
			File efile = new File(dir1.getCanonicalPath(), fileToExtract);

			InputStream in = new BufferedInputStream(jar.getInputStream(entry));
			OutputStream out = 	new BufferedOutputStream(new FileOutputStream(efile));
			byte[] buffer = new byte[2048];
			for (;;)  {
				int nBytes = in.read(buffer);
				if (nBytes <= 0) break;
				out.write(buffer, 0, nBytes);
			}

			out.flush();
			out.close();
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
