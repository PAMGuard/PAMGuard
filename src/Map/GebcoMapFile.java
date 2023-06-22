package Map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamFileChooser;
import PamView.dialog.warn.WarnOnce;

public class GebcoMapFile implements MapFileManager {
	
	Vector<MapContour> mapContours;
	
	Vector<Integer> availableContours;
	
	public GebcoMapFile() {
		super();
		mapContours = new Vector<MapContour>();
		availableContours = new Vector<Integer>();
	}

	/* (non-Javadoc)
	 * @see Map.MapFile#selectMapFile()
	 */
	public File selectMapFile(File currentFile) {
		JFileChooser fileChooser;
		File gebcoFile= null;
		
		
		try {
			fileChooser = new PamFileChooser();
			fileChooser.setDialogTitle("Map File Selection...");
			fileChooser.setFileHidingEnabled(true);
			fileChooser.setApproveButtonText("OPEN");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (currentFile != null) {
				fileChooser.setSelectedFile(currentFile);
			}
			javax.swing.filechooser.FileFilter[] filters = fileChooser.getChoosableFileFilters();
			
			for (int i = 0; i < filters.length; i++) {
				fileChooser.removeChoosableFileFilter(filters[i]);
			}
			MapFileFilter mapFileFilter = new MapFileFilter();
			mapFileFilter.addFileType("asc");
			mapFileFilter.addFileType("ASC");
			fileChooser.setFileFilter(mapFileFilter);
			
			int state = fileChooser.showOpenDialog(null);
			gebcoFile = fileChooser.getSelectedFile();
			
			
			if (state == JFileChooser.CANCEL_OPTION) {
				return null;
			}
			
			if (!gebcoFile.exists()) {
				JOptionPane.showMessageDialog(null, "Invalid .asc map file",
						"Invalid Map File", JOptionPane.WARNING_MESSAGE);
				
				gebcoFile = null;
			} 
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		
//		System.out.println("GetMapFile");
		return gebcoFile;
	}
	
	public boolean readFileData(File file, boolean contours) {
		return readMapFile(file, contours);
	}
	
	/* (non-Javadoc)
	 * @see Map.MapFile#readFileData(java.io.File)
	 */
	public boolean readFileData(File file) {
		return readMapFile(file, true);
	}

	private boolean readMapFile(File gebcoFile, boolean readContours) {

		mapContours.removeAllElements();
		
		if (gebcoFile == null || gebcoFile.exists() == false) return false;
		
		FileReader fileReader = null;
		BufferedReader reader;
		try {
			fileReader = new FileReader(gebcoFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		reader = new BufferedReader(fileReader);
		int spaceIndex;
		String line = null;
		String num1, num2;
		double lat, lon;
		int pointCount;
		int depth;
		MapContour mapContour;
		boolean error = false;
		try {
			int iLine = 0;
			while((line = reader.readLine())!=null){
				iLine++;
				line = line.trim();
				spaceIndex = line.indexOf(' ');
				if (spaceIndex < 0) {
					String msg = String.format("Error in map file at line %d \"%s\"", iLine, line);
					WarnOnce.showNamedWarning("Gebco Map File Warning", PamController.getMainFrame(), gebcoFile.getName(), msg, WarnOnce.WARNING_MESSAGE);
					error = true;
					break;
				}
				num1 = line.substring(0,spaceIndex).trim();
				num2 = line.substring(spaceIndex).trim();
				depth = Integer.valueOf(num1);
				pointCount = Integer.valueOf(num2);
				mapContour = new MapContour(depth);
				for (int i = 0; i < pointCount; i++) {
					line = reader.readLine();
					if (line == null) break;
					line = line.trim();
					spaceIndex = line.indexOf(' ');
					num1 = line.substring(0,spaceIndex).trim();
					num2 = line.substring(spaceIndex).trim();
					lat = Double.valueOf(num1);
					lon = Double.valueOf(num2);
					mapContour.addLatLong(new LatLong(lat, lon));
				}
				if (depth == 0 || readContours) {
					if (hasMapContour(depth) == false) {
						availableContours.add(depth);
					}
					mapContours.add(mapContour);
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		catch (NumberFormatException nex) {
			nex.printStackTrace();
			error = true;
		}
		catch (IndexOutOfBoundsException iex) {
			iex.printStackTrace();
			error = true;
		}
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(mapContours);
		Collections.sort(availableContours);
		return !error;
	}
	
	
	/* (non-Javadoc)
	 * @see Map.MapFileManager#getContourIndex(double)
	 */
	public int getContourIndex(double depth) {
		for (int i = 0; i < availableContours.size(); i++) {
			if (availableContours.get(i) == depth) {
				return i;
			}
		}
		return -1;
	}

	public boolean hasMapContour(double depth) {
		for (int i = 0; i < availableContours.size(); i++) {
			if (availableContours.get(i) == depth) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see Map.MapFile#getAvailableContours()
	 */
	public Vector<Integer> getAvailableContours() {
		return availableContours;
	}
	
	/* (non-Javadoc)
	 * @see Map.MapFileManager#getContourCount()
	 */
	public int getContourCount() {
		if (mapContours == null) return 0;
		return mapContours.size();
	}

	/* (non-Javadoc)
	 * @see Map.MapFile#getMapContour(int)
	 */
	public MapContour getMapContour(int contourIndex) {
		return mapContours.get(contourIndex);
	}

	/* (non-Javadoc)
	 * @see Map.MapFileManager#clearFileData()
	 */
	public void clearFileData() {
		mapContours.removeAllElements();
		availableContours.removeAllElements();
	}
	
}
