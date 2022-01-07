/**
 * 
 */
package Map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import PamUtils.MapContourValues;
import PamUtils.PamFileChooser;

/**
 * @author David
 *
 */
public class GetMapFile {

	File contourFile;
	FileReader fileReader;
	BufferedReader reader;
	int contourDepth;
	int numPointsInBlock;
	String contourString, numPointsString, latString, longString;
	int numStartIndex;
	int numEndIndex;
	int counter=0;
	int counter2=0;
	int blockCounter=-1;
	boolean landContour = false;
	double latVal, longVal;
	MapContourValues contourLatLong = new MapContourValues();
	ArrayList<MapContourValues> contourPoints = new ArrayList<MapContourValues>();
	ArrayList<MapContourValues> contourBlockNums = new ArrayList<MapContourValues>();
	boolean mapFileSuccess = false;
	boolean fileOpened = false;
	int state;
	
	/**
	 * @return true if sucessful
	 */
	public File openMapDialog(){

		JFileChooser fileChooser;
		
		
		
		try {
			fileChooser = new PamFileChooser();
			fileChooser.setDialogTitle("Map File Selection...");
			fileChooser.setFileHidingEnabled(true);
			fileChooser.setApproveButtonText("OPEN");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			javax.swing.filechooser.FileFilter[] filters = fileChooser.getChoosableFileFilters();
				
			for (int i = 0; i < filters.length; i++) {
				fileChooser.removeChoosableFileFilter(filters[i]);
			}
			MapFileFilter mapFileFilter = new MapFileFilter();
			mapFileFilter.addFileType("asc");
			mapFileFilter.addFileType("ASC");
			fileChooser.addChoosableFileFilter(mapFileFilter);
			
			state = fileChooser.showOpenDialog(null);
			contourFile = fileChooser.getSelectedFile();


			if (state == JFileChooser.CANCEL_OPTION) {
				fileOpened = false;
//				JOptionPane.showMessageDialog(null, "no map I'm afraid",
//						"Invalid Map File", JOptionPane.WARNING_MESSAGE);
				return null;
			}

			if (!contourFile.exists()) {
				JOptionPane.showMessageDialog(null, "Invalid .asc map file",
						"Invalid Map File", JOptionPane.WARNING_MESSAGE);

				fileOpened = false;
				contourFile = null;
			} else {
				fileOpened = true;

			}
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		
//		System.out.println("GetMapFile");
		return contourFile;
	}
	
	
	public ArrayList<MapContourValues> getMapContours(){
		
		ArrayList<MapContourValues> newContourPoints = new ArrayList<MapContourValues>();

		try {
			fileReader = new FileReader(contourFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader = new BufferedReader(fileReader);
		
		String line = null;
		counter2=0;
		try {
			while((line = reader.readLine())!=null){
				//System.out.println(line);
				if(line.lastIndexOf('.')<0 ){
				//	System.out.println("before");
				//	System.out.println(line);
				//	System.out.println("after");
				contourString=line.substring(0,6);
				numStartIndex = contourString.lastIndexOf(' ')+1;
				numEndIndex = 6;
				try {
					contourDepth = Integer.parseInt(contourString.substring(numStartIndex,numEndIndex));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				blockCounter++;
				
				
				}
				else{
				
				latString = line.substring(0,8);
				numStartIndex = latString.lastIndexOf(' ')+1;
				try {
					latVal = Double.parseDouble(latString.substring(numStartIndex,8));
				} catch (NumberFormatException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//System.out.println(latVal);
				longString = line.substring(9,18); 
				numStartIndex = longString.lastIndexOf(' ')+1;
				try {
					longVal = Double.parseDouble(longString.substring(numStartIndex,9));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				contourLatLong.x=longVal;
				contourLatLong.y=latVal;
				
				contourPoints.add(counter2,new MapContourValues(contourLatLong.x,contourLatLong.y,contourDepth, blockCounter));
			
				counter2=counter2+1;
				}
				counter++;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	//	System.out.println("finished");

		return contourPoints;
		
	}
	
	
}
