package generalDatabase.dataExport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import GPS.GPSControl;
import GPS.GPSDataMatcher;
import GPS.GpsData;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;

import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import generalDatabase.pamCursor.PamCursor;

public class FileExportsystem extends DataExportSystem {

	DataExportDialog exportDialog;
	
	/**
	 * @param exportDialog
	 */
	public FileExportsystem(DataExportDialog exportDialog) {
		super();
		this.exportDialog = exportDialog;
	}

	@Override
	boolean exportData(ArrayList<PamTableItem> tableItems, PamCursor exportCursor) {
		/**
		 * Open a dialog to get a text file name, then write all the data to it. 
		 * Will worry about options such as types of deliminator later on 
		 */
		/*
		 * See if we can find some Gps data which might match with these data to give positions. . 
		 * also need to find the UTC table itme. 
		 */
		PamTableItem utcTableItem = null;
		for (PamTableItem ti:tableItems) {
			if (ti.getName().equals("UTC")) {
				utcTableItem = ti;
				break;
			}
		}
		GPSControl gpsControl = GPSControl.getGpsControl();
		GPSDataMatcher gpsDataMatcher = null;
		if (gpsControl != null && utcTableItem != null) {
			gpsDataMatcher = gpsControl.getGpsDataMatcher();
		}
		
		File file = getExportFile();
		if (file == null) {
			return false;
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			int iCol = 0;
			int nCol = tableItems.size();
			Object dataObject;
			for (int i = 0; i < nCol; i++) {
				if (iCol++ > 0) {
					writeDeliminator(writer);
				}
				writer.append(tableItems.get(i).getName());
			}
			if (gpsDataMatcher != null) {
				writer.append(", Latitude, Longitude");
			}
			writer.newLine();
			exportCursor.beforeFirst();
			Long dataUTC = null;
			while (exportCursor.next()) {
				exportCursor.moveDataToTableDef(true);
				iCol = 0;
				dataUTC = null;
				GpsData gpsData = null;
				for (int i = 0; i < nCol; i++) {
					if (iCol++ > 0) {
						writeDeliminator(writer);
					}
					dataObject = stripTextCommas(tableItems.get(i).getValue());
					if (tableItems.get(i) == utcTableItem) {
						dataUTC = SQLTypes.millisFromTimeStamp(dataObject);
					}
					if (dataObject != null) {
						writer.append(dataObject.toString());
					}
				}
					if (gpsDataMatcher != null && dataUTC != null) {
						// get the UTS time from the database. 
						gpsData = gpsDataMatcher.matchData(dataUTC);
						if (gpsData != null) {
							writer.append(String.format(", %7.6f, %7.6f", gpsData.getLatitude(), gpsData.getLongitude()));
						}
						else {
							writer.append(", , ");
						}
					}
				writer.newLine();
			}
			
			writer.close();
		} catch (IOException e) {
			System.out.println("Unable to export data fo file : " + file.getAbsolutePath());
		} catch (SQLException sqlE) {
			System.out.println("SQL exception exporting data to file : " + file.getAbsolutePath());
			sqlE.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Strip commas out of text or it crews up any CSV output. 
	 * @param object
	 * @return same text without commas. 
	 */
	private Object stripTextCommas(Object object) {
		if (object == null) {
			return object;
		}
		if (object.getClass() != String.class) {
			return object;
		}
		String str = (String) object;
		str = str.replace(",", "-");
		return str.trim();
	}

	private void writeDeliminator(BufferedWriter writer) throws IOException {
		writer.append(", ");
	}

	private File getExportFile() {
		PamFileFilter fileFilter = new PamFileFilter("Text Files", ".txt");
		fileFilter.addFileType(".csv");
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(fileFilter);
//		fileChooser.setSelectedFile(new File(fileName));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
//		fileChooser.set
		int state = fileChooser.showSaveDialog(null);
		if (state == JFileChooser.APPROVE_OPTION) {
			File aFile = fileChooser.getSelectedFile();
			return PamFileFilter.checkFileEnd(aFile, "txt", true);
		}
		else {
			return null;
		}
	}

	@Override
	String getSystemName() {
		return "File system";
	}

}
