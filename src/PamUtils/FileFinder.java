/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package PamUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * @author David J McLaren
 *
 */
public class FileFinder {

	static public List findFileExt(File initDir, String extension) throws FileNotFoundException{
	    
	    List result = new ArrayList();
	    File[] filesAndDirs = initDir.listFiles();
	    List filesDirs = Arrays.asList(filesAndDirs);
	    Iterator fileIterator = filesDirs.iterator();
	    File file = null;

	    while ( fileIterator.hasNext() ) {
	      file = (File)fileIterator.next();
	      
	      if (file.getName().endsWith(extension.toString())){
	      result.add(file);
	      }
	      
	      if (!file.isFile()) {
	    	  //recursive directory search
	        List deeperList = findFileExt(file,extension);
	        result.addAll(deeperList);
	      }
	    }
	    Collections.sort(result);
	    return result;
	  }
	
	static public List findFileName( File initDir, String fileName) throws FileNotFoundException{
	    
	    List result = new ArrayList();
	    File[] filesAndDirs = initDir.listFiles();
	    List filesDirs = Arrays.asList(filesAndDirs);
	    Iterator fileIterator = filesDirs.iterator();
	    File file = null;

	    while ( fileIterator.hasNext() ) {
	      file = (File)fileIterator.next();
	      
	      if (file.getName().equals(fileName.toString())){
	      result.add(file);
	      }
	      
	      if (!file.isFile()) {
	    	  //recursive directory search
	        List deeperList = findFileName(file,fileName);
	        result.addAll(deeperList);
	      }
	    }
	    Collections.sort(result);
	    return result;
	  }
	
	
	
	
	
	
	
	
	
//	public static ArrayList<File> findFiles(File dirToLookIn, ArrayList<File> fileNamesToFind){
//		
//		ArrayList<File> filesStillToFind = (ArrayList<File>) fileNamesToFind.clone();
//		ArrayList<File> allChildren= new ArrayList<File>(fileNamesToFind.size());
//		getAllChildren(allChildren, dirToLookIn);
//		ArrayList<File> foundFiles= new ArrayList<File>(fileNamesToFind.size());
//		ArrayList<File> fileListToLookIn = (ArrayList<File>) allChildren.clone();
//		
//		for (int i=0;i<filesStillToFind.size();i++){
//			boolean found=false;
//			innerFor:
//			for(int j=0;j<fileListToLookIn.size();j++){
//				if (fileListToLookIn.get(j).getAbsolutePath().endsWith(filesStillToFind.get(i).getName())){
//					//add to new list
//					foundFiles.add(filesStillToFind.get(i));
//					//remove from old list
//					filesStillToFind.remove(i);
//					fileListToLookIn.remove(j);
//					found=true;
//					break innerFor;
//				}
//				
//			}
//			if(!found){
//				foundFiles.add(null);
//				System.out.print(filesStillToFind.get(i)+"cannot be found in"+dirToLookIn);
//			}
//		}
//		
//		return foundFiles;
//	}
//	
//	private static void getAllChildren(ArrayList<File> List, File dir){
//		
//		File[] children = dir.listFiles();
//		for (File child:children){
//			if (child.isDirectory()){
//				getAllChildren(List, child);
//			}
//			if (child.isFile()){
//				List.add(child);
//			}
//		}
//		
//		
//	}
	
}
