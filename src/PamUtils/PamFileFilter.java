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
import java.io.FileFilter;
import java.util.ArrayList;

public class PamFileFilter extends javax.swing.filechooser.FileFilter implements FileFilter {

	 private ArrayList<String> extensions = new ArrayList<String>();
	 
	 private String description;
	 
	 private String prefix = null;
	 
	 protected boolean acceptFolders = true;
	 	 

	public PamFileFilter(String description, String defExtension) {
		 this.description = description;
		 extensions.add(defExtension);
	 }
	 
	 public void addFileType(String extString) {
		 extensions.add(extString.toLowerCase());
	 }
	 
	 /**
	  * Specify a prefix for the file filter
	  * 
	  * @param prefix
	  */
	 public void addFilePrefix(String prefix) {
		 this.prefix = prefix;
	 }
	 
	@Override
	public boolean accept(File f) {
		if (f.isDirectory() && acceptFolders) {
			return true;
		}

		String extension = getExtension(f);
		if (extension != null) {
			for (int i=0;i<extensions.size();i++) {
				String lName = f.getName().toLowerCase();
				String ext = extensions.get(i);
				if (ext == null || lName.endsWith(ext)){
					if (prefix==null) {
						return true;
					} else {
						if (f.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
							return true;
						}
					}
				}
			}
		}
		
		else {
			return false;
		}
		return false;
	}

	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
	
	/**
	 * Checks a file end. If forceEnd is true, then the end must be of the
	 * same type as end, otherwise, it will allow any file end, but if none
	 * exists, will add the given default one. 
	 * @param f file to check
	 * @param end file end (with or without the '.')
	 * @param forceEnd force it to add the end to the file name
	 * @return File corrected file name. 
	 */
	public static File checkFileEnd(File f, String end, boolean forceEnd) {
		// quickly check that the file has the correct ending
		// if the user typed their own, it may not be there.
		// expects end to not have the ., so remove if it's there
		if (end.charAt(0) == '.') {
			end = end.substring(1);
		}
		String fEnd = getExtension(f);
		if (fEnd != null && fEnd.equals(end)) return f;
		if (fEnd != null && fEnd.length() > 1 && forceEnd == false) return f;
		// either there is no end, or it's the wrong type.
		String newName = f.getAbsolutePath();
		if (fEnd != null) { // remove it !
			newName = newName.substring(0, newName.lastIndexOf('.'));
		}
		newName += "." + end;
		return new File(newName);
	}

	// The description of this filter
	@Override
	public String getDescription() {
		return description;
	}

	public boolean isAcceptFolders() {
		return acceptFolders;
	}

	public void setAcceptFolders(boolean acceptFolders) {
		this.acceptFolders = acceptFolders;
	}
	
	public static String getFileExtension(String file){
		int mid= file.lastIndexOf(".");
		return file.substring(mid+1,file.length());
	}
	
	/**
	 * Get all the file extensions. 
	 * @return the file extensions
	 */
	public ArrayList<String> getFileExtensions() {
		return this.extensions;
	}
}
