package videoRangePanel.vrmethods.landMarkMethod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import PamUtils.LatLong;
import PamUtils.TxtFileUtils;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;

/**
 * Imports landmarks. 
 * @author Jamie Macaulay
 * 
 *
 */
public class LandMarkCSVLogging   {
	
	
	public LandMarkCSVLogging() {
		
		
	}
	
	/**
	 * Open a file dialog and import a .csv of landmarks. 
	 * @return imported LandMarkGroup. 
	 */
	public  LandMarkGroup importLandMarks(Window owner){
		 FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Open Resource File");
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("CSV Files", "*.csv"),
		         new ExtensionFilter("Txt Files", "*.txt"));
		 File selectedFile = fileChooser.showOpenDialog(owner);
		 if (selectedFile!=null) {
			 return importLandMarks(selectedFile);
		 }
		 else {
			 PamDialogFX.showWarning("Could not import the file. The file selected was null."); 
			 return null; 
		 }
	}
	
	/**
	 * Import landmarks from a .csv file 
	 * @param file - the file
	 * @return list of imported landmarks. 
	 */
	public LandMarkGroup importLandMarks(File file){
		
		List<String> txtData = TxtFileUtils.importTxtToCollection(file.getAbsolutePath()); 
		
		LandMarkGroup landMarkGroup = new LandMarkGroup();
		LandMark landMark; 
		
		//print out the data
		System.out.println("Imported raw txt file of landmarks:");
		for (int i=0; i<txtData.size(); i++) {
			System.out.println(txtData.get(i));
		}
		
		
		for (int i=0; i<txtData.size(); i++) {
	
			landMark = parseLandMarkString(txtData.get(i)); 
			//System.out.println("New Landmark string: " + txtData.get(i));
			//System.out.println("New Landmark: " + landMark);
			if (landMark!=null) {
				landMarkGroup.add(landMark);
			}
			else {
				//might be more info. 
				ArrayList<String> parsedData = TxtFileUtils.parseTxtLine(txtData.get(i), ","); 
				if (parsedData.size()==2 && i<3) {
					landMarkGroup.setGroupName(parsedData.get(1));
				}
			}
		}

		return landMarkGroup;
	}
	
	/**
	 * Opens a file dialog and allows users to export and landmark group
	 * @param landMarkGroup - the landmark group to export.
	 */
	public void export(LandMarkGroup landMarkGroup, Window owner){
		 FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Open Resource File");
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("CSV Files", "*.csv"));
		 File selectedFile = fileChooser.showSaveDialog(owner);
		 if (selectedFile!=null) {
			 export(selectedFile,  landMarkGroup);
		 }
		 else PamDialogFX.showWarning("Could not export to file. The file selected was null."); 
	}

	
	/**
	 * Import landmarks from a .csv file 
	 * @param file - the file
	 * @return list of imported landmarks. 
	 */
	public void export(File file, LandMarkGroup landMarkGroup){
		
		
        try {
			FileWriter writer = new FileWriter(file);
			
			//first write header so people know what it all means. 
			String line0="Landmark Name, Latitiude (decimal) ,Longitude (decimal), Height (m), Bearing (m), "
					+ "Pitch (m), Latitude Origin (decimal), Longitude Origin (decimal), Height Origin (m)\n ";
			writer.write(line0);

			//the landmark group might have a name; 
			String line1="Landmark Group Name, " + landMarkGroup.getName() + "\n"; 
			writer.write(line1);
			for (int i=0; i<landMarkGroup.size(); i++) {
				saveCSVResult(landMarkGroup.get(i), writer); 
			}
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Save a landmark as  a line in a .csv file 
	 * @param landMark
	 * @param fwLoc
	 */
	public static void saveCSVResult(LandMark landMark, FileWriter fwLoc){
		try {
			String locResults;
			locResults= createLandMarkString(landMark);
			
//	 		System.out.println("Loc Results: "+locResults);
	 		fwLoc.write(locResults + "\n");
        } 
		
		catch (IOException e) {
	 		e.printStackTrace();
	 	}
	}
	
	
	/**
	 * 
	 * Create string line for a landmark object.  
	 * @param landMark - the landmark to convert to a string line
	 * @return the string representation of a landmark for saving to .csv
	 */
	private static String createLandMarkString(LandMark landMark) {
		
		String delimeter=",";
		
		String name="";
		String lat="";
		String lon=""; 
		String height="";
	
		String bearing="";
		String pitch=""; 
		String latorigin="";		
		String lonorigin=""; 
		String heightorigin="";
		
		//name od
		if (landMark.getName()!=null) name=landMark.getName();
		
		//positional landmark 
		if (landMark.getPosition()!=null) lat=String.format("%.8f", landMark.getPosition().getLatitude()); 
		if (landMark.getPosition()!=null) lon=String.format("%.8f", landMark.getPosition().getLongitude()); 
		if (landMark.getPosition()!=null) height=String.format("%.4f", landMark.getPosition().getHeight()); 
		
		
		//bearing landmark 
		if (landMark.getBearing()!=null) bearing=landMark.getBearing().toString();
		if (landMark.getPitch()!=null) pitch=landMark.getPitch().toString();

		if (landMark.getLatLongOrigin()!=null) latorigin=String.format("%.8f", landMark.getLatLongOrigin().getLatitude()); 
		if (landMark.getLatLongOrigin()!=null) lonorigin=String.format("%.8f", landMark.getLatLongOrigin().getLongitude()); 
		if (landMark.getLatLongOrigin()!=null) heightorigin=String.format("%.4f", landMark.getLatLongOrigin().getHeight()); 
		
		
		String landMarkString = name+delimeter
				+lat+delimeter
				+ lon+ delimeter
				+ height + delimeter
				+ bearing + delimeter
				+pitch + delimeter
				+ latorigin + delimeter
				+lonorigin +delimeter 
				+heightorigin +delimeter; 
		

		return landMarkString;
	}

	/**
	 * Convert a string from .csv file into a landmark object.
	 * @return landmark representation of the string. Null if could not be parsed from String. 
	 */
	public LandMark parseLandMarkString(String landMarkString) {
		
		ArrayList<String> lndmrkImport = TxtFileUtils.parseTxtLine(landMarkString, ","); 
	
//		for (int i=0; i<lndmrkImport.size(); i++) {
//			System.out.println(i + "    " +	lndmrkImport.get(i)); 
//		}
				
		//System.out.println("Size: " + lndmrkImport.size());
		boolean data1, data2; 
		if (lndmrkImport.size()==4 || lndmrkImport.size()==5 || lndmrkImport.size()==9) {

			LandMark landMark = new LandMark();
			
			landMark.setName(lndmrkImport.get(0));

			try {
				Double latitiude = Double.valueOf(lndmrkImport.get(1));
				Double longitude = Double.valueOf(lndmrkImport.get(2));
				Double height = Double.valueOf(lndmrkImport.get(3));
				LatLong latLong = new LatLong(latitiude, longitude, height); 
				landMark.setPosition(latLong);
				data1=true;
			}
			catch (Exception e) {
				//no values
				e.printStackTrace();
				data1=false;
			}

			try {
				Double bearing = Double.valueOf(lndmrkImport.get(4));
				Double pitch = Double.valueOf(lndmrkImport.get(5));
				landMark.setBearing(bearing);
				landMark.setPitch(pitch);

				Double latitiudeOrigin = Double.valueOf(lndmrkImport.get(6));
				Double longitudeOtigin = Double.valueOf(lndmrkImport.get(7));
				Double heightOrigin = Double.valueOf(lndmrkImport.get(8));
				
				LatLong latLongOrigin = new LatLong(latitiudeOrigin, longitudeOtigin, heightOrigin); 
				
				landMark.setLatLongOrigin(latLongOrigin);
				data2=true;
			}
			catch (Exception e) {
				//no values
				data2=false; 
				e.printStackTrace();
			}
			
			//if neither lat lon or bearing landmark was successfully loaded
			//return null. 
			if (data1 || data2)	return landMark; 
			else return null; 
		}
		
		
		return null;
	}
	

}
