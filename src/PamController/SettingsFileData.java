package PamController;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/*
 * Class containing data first read in on Pamguard start up
 * which will tell us where settings were last stored and 
 * what to do - just load default, or pop up options list
 */
public class SettingsFileData implements Serializable, Cloneable {

	static final long serialVersionUID = 1;
	
	public boolean showFileList = true;
	
	protected int maxFiles = 20;
	
	public ArrayList<File> recentFiles = new ArrayList<File>();
	
	protected boolean showTipAtStartup = true;
	
	private double scalingFactor = 1.0;
			
	public SettingsFileData() {
//		recentFiles.add(new File(defaultFile));
	}

	public void setFirstFile(File firstFile) {
		recentFiles.remove(firstFile);
		recentFiles.add(0, firstFile);
	}
	
	public void trimList() {
		int l;
		maxFiles = Math.max(maxFiles, 1);
		while ((l = recentFiles.size()) > maxFiles) {
			recentFiles.remove(l-1);
		}
	}
	
	public File getFirstFile() {
		if (recentFiles.size() < 1) return null;
		return recentFiles.get(0);
	}
	
	
	public double getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(double scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	@Override
	public SettingsFileData clone()  {
		try {
			SettingsFileData set = (SettingsFileData) super.clone();
			set.showFileList=true;
			return set;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
