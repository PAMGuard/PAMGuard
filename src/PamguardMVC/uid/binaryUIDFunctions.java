/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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

package PamguardMVC.uid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.comparator.NameFileComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import PamController.PamController;
import PamController.PamguardVersionInfo;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryHeaderAndFooter;
import binaryFileStorage.BinaryStore;

/**
 * Class to handle basic UID functions related to the binary store
 * 
 * @author mo
 *
 */
public class binaryUIDFunctions {

	private PamController pamController;
	private static final String BINARYUIDLOGFILE = "PamguardBinaryStoreUIDLog.xml";

	/**
	 * Main constructor
	 * 
	 * @param pamController
	 */
	public binaryUIDFunctions(PamController pamController) {
		this.pamController = pamController;
	}

	/**
	 * Reads the UID information from the binary store log file. The file has an xml
	 * format
	 * 
	 * @return
	 */
	public ArrayList<UIDTrackerData> readLogFile() {

		ArrayList<UIDTrackerData> theData = new ArrayList<UIDTrackerData>();
		File logFileData = this.getLogFile();

		try {

			// set up the XML reader
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(logFileData);
			doc.getDocumentElement().normalize();

			// get list of elements using the MODULE tag
			NodeList nList = doc.getElementsByTagName("MODULE");

			// step through the list, extracting the filename prefix and last UID for each
			UIDTrackerData dataPoint;
			for (int i = 0; i < nList.getLength(); i++) {
				Node curNode = nList.item(i);
				if (curNode.getNodeType() == Node.ELEMENT_NODE) {
					Element curElement = (Element) curNode;
					dataPoint = new UIDTrackerData(
							curElement.getElementsByTagName("BinFilePrefix").item(0).getTextContent(),
							Long.parseLong(curElement.getElementsByTagName("lastUID").item(0).getTextContent()));
					theData.add(dataPoint);
				}
			}
		} catch (Exception e) {
			System.out.println("Error reading binary UID data from log file" + logFileData);
//			e.printStackTrace();
		}

//        try (BufferedReader br = new BufferedReader(new FileReader(logFileData))) {
//        	
//        	// read the first line of the file
//    	    String line = br.readLine();
//
//    	    // step through the file one line at a time
//    	    while (line != null) {
//    	    	
//    	    	// line format is: tablename,UID
//    	    	// so parse the line and put the values into a uidTrackerData object
//    	    	String[] tokens = line.split(",");
//				uidTrackerData uidData = new uidTrackerData(tokens[1],Long.parseLong(tokens[2]));
//				theData.add(uidData);
//
//    	        // read the next line
//    	        line = br.readLine();
//    	    }
//    	} catch (Exception e) {
//            System.out.println("Error reading binary store UID log file");
//			e.printStackTrace();
//		}

		// return the list
		return theData;
	}

	/**
	 * Create a File object using the binary store log filename and the current
	 * binary store folder
	 * 
	 * @return
	 */
	public File getLogFile() {
		BinaryStore binStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		if (binStore == null) {
			return null;
		}
		String root = binStore.getBinaryStoreSettings().getStoreLocation();
		if (root == null) {
			return null;
		}
		return (new File(root, BINARYUIDLOGFILE));
	}

	/**
	 * Check if the log file exists
	 * 
	 * @return
	 */
	public boolean checkFileExists() {
		File logfile = this.getLogFile();
		if (logfile == null) {
			return false;
		}
		return (this.getLogFile().isFile());
	}

	/**
	 * Saves the UID data for the binary stores to the binary store log file
	 * 
	 * @param allDataBlocks a list of all datablocks in the system
	 * @return
	 */
	public boolean saveBinData(ArrayList<PamDataBlock> allDataBlocks) {
		// get out right away if there is no binary store.
		BinaryStore binStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		if (binStore == null) {
			return false;
		}

		// If the file already exists, overwrite it
		if (this.checkFileExists()) {
			System.out.println("Warning - log file already exists.  Attempting to overwrite.");
			if (!this.removeLogFile()) {
				System.out.println("   Error - can''t delete log file.  Current UID information will not be saved");
				return false;
			}
		}

		// create ArrayList to hold data
		List<UIDTrackerData> dataToSave = new ArrayList<UIDTrackerData>();

		// loop through list, searching for data blocks which have been saving
		// information to the binary store
		for (PamDataBlock aDataBlock : allDataBlocks) {
			if (aDataBlock.getShouldBinary(null)) {
				UIDTrackerData line = new UIDTrackerData(aDataBlock.getBinaryDataSource().createFilenamePrefix(),
						aDataBlock.getUidHandler().getCurrentUID());
				dataToSave.add(line);
			}
		}

		// write the data to the log file
		return (writeLogDataToXMLFile(dataToSave));
	}

	/**
	 * Goes through the binary index files and figures out the last UID for each
	 * module
	 * 
	 * @return a List of uidTrackerData objects containing module and UID
	 *         information
	 */
	public List<UIDTrackerData> getUIDsFromBinaryStores() {

		// initialize List
		List<UIDTrackerData> binData = new ArrayList<UIDTrackerData>();

		// Check if the binary store is being used. If not, exit right away
		BinaryStore binStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		if (binStore == null) {
			return null;
		}

		// get list of pgdf files and sort them
		List<File> binFiles = binStore.listAllFiles();
		if (binFiles.isEmpty()) {
//			System.out.println("Warning - no binary files found in " + binStore.getBinaryStoreSettings().getStoreLocation());
			return null;
		}
		Collections.sort(binFiles);

		// get a list of the filename prefixes (which will link the files to the
		// datablocks). The
		// date is appended to the end of the prefix, and we want the last date because
		// that will
		// contain the highest UID. But since we've sorted the list, all we really need
		// to do is
		// step through the list and see when the prefix changes. That will be the
		// latest file for
		// that prefix, and the one we want to query.
		// get the prefix by stripping off the date and time that automatically get
		// appended to the filename.
		// The time occurs after the last underscore character, and the date after the
		// second-last
		// underscore character
		String fullName = binFiles.get(0).getName();
		int idx = fullName.lastIndexOf("_");
		idx = fullName.lastIndexOf("_", idx - 1);
		String lastPrefix = fullName.substring(0, idx + 1); // make sure to include the final underscore character in
															// the String
		String currentPrefix;
		String prefixToUse = "";
		Long lastUID;

		for (int i = 0; i < binFiles.size(); i++) {

			// get the prefix from the current file
			fullName = binFiles.get(i).getName();
			idx = fullName.lastIndexOf("_");
			idx = fullName.lastIndexOf("_", idx - 1);
			currentPrefix = fullName.substring(0, idx + 1); // make sure to include the final underscore character in
															// the String

			// if this is different from the last prefix, get the footer from the prior
			// file. Also
			// do this if this is the last file in the list
			// Important: try to get the footer from the matching index file, because that
			// will be a lot faster.
			File fileToCheck = null;
			if (!currentPrefix.equals(lastPrefix)) {
				fileToCheck = binFiles.get(i - 1);
				prefixToUse = lastPrefix;
			} else if (i == binFiles.size() - 1) {
				fileToCheck = binFiles.get(i);
				prefixToUse = currentPrefix;
			}
			if (fileToCheck != null) {

				// first check for a matching index file. If it exists, get the footer from it
				// instead
				// because it will be a lot faster than reading through an entire binary file
				File indexFile = binStore.findIndexFile(fileToCheck, true);
				if (indexFile != null) {
					fileToCheck = indexFile;
				}
				BinaryHeaderAndFooter headAndFoot = binStore.readHeaderAndFooter(fileToCheck); //
				BinaryFooter footer = headAndFoot.binaryFooter;
				if (footer == null) {
					continue;
				}
				lastUID = footer.getHighestUID();
				if (lastUID != null) {
					UIDTrackerData dataPoint = new UIDTrackerData(prefixToUse, lastUID);
					binData.add(dataPoint);
				}
				if (footer.getFileEndReason() == BinaryFooter.END_CRASHED) {
					String title = "Binary File Corrupted";
					String msg = "PAMGuard is not able to read the footer information from the following binary file:<br><br>"
							+ fileToCheck.getAbsolutePath()
							+ "<br><br>This may happen if PAMGuard was not able to shut down properly the last time it was run.<br>  PAMGuard"
							+ "has attempted to recompile the information directly from the binary file and rebuild the footer, and was able "
							+ "to recover " + footer.getNObjects() + " data objects.  The last object UID recovered is "
							+ footer.getHighestUID()
							+ ".  It is not possible to know whether all of the original data objects have been recovered.<br><br>";
					String help = null;
					int ans = WarnOnce.showWarning(PamController.getMainFrame(), title,
							msg, WarnOnce.WARNING_MESSAGE, help);
				}
			}

			// increment the counter and go to the next file
			lastPrefix = currentPrefix;
		}

		// return the list
		return binData;
	}

	/**
	 * Returns the highest UID number for the PamDataBlock passed. This method first
	 * looks through the Tracker Table, and if it can't find a suitable UID it then
	 * looks through the database table directly associated with the PamDataBlock.
	 * Returns -1 if there was a problem retrieving the information
	 * 
	 * @param aDataBlock
	 * @return
	 */
	public long findMaxUIDforDataBlock(PamDataBlock aDataBlock) {

		// Initialise return variable and figure out the prefix that the binary files
		// would be using
		long maxUID = -1;
		String filePrefix = aDataBlock.getBinaryDataSource().createFilenamePrefix();

		// first, try to get the information from the log file
		if (this.checkFileExists()) {
			try {
				File logFileData = this.getLogFile();

				// set up the XML reader
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(logFileData);
				doc.getDocumentElement().normalize();
				XPathFactory xPathFactory = XPathFactory.newInstance();
				XPath xPath = xPathFactory.newXPath();

				// search through the XML file for the Node containing the correct file prefix
				String search = String.format("/PAMGUARD/MODULES/MODULE/BinFilePrefix[text()='%s']", filePrefix);
				XPathExpression expr = xPath.compile(search);
				NodeList nList = (NodeList) (expr.evaluate(doc, XPathConstants.NODESET));
				if (nList.getLength() >= 1) {
					Element parent = (Element) nList.item(0).getParentNode();
					maxUID = Long.parseLong(parent.getElementsByTagName("lastUID").item(0).getTextContent());
				}
			} catch (Exception e) {
				System.out.println("Error reading binary UID data from log file");
				e.printStackTrace();
			}
		}
		// if that doesn't work, go to the binary files directly
		if (maxUID == -1) {

			// Check if the binary store is being used. If not, exit right away
			BinaryStore binStore = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
			if (binStore == null) {
				return maxUID;
			}

			// get list of pgdf files and sort them
			List<File> binFiles = binStore.listAllFilesWithPrefix(filePrefix);
			if (binFiles == null || binFiles.isEmpty()) {
//    			System.out.println("Warning - no " + filePrefix + " binary files found in " + binStore.getBinaryStoreSettings().getStoreLocation());
				return maxUID;
			}
			Collections.sort(binFiles, NameFileComparator.NAME_COMPARATOR);

			// loop through the binary files from the last one to the first, and stop as
			// soon as we find
			// a valid UID. Read the info from an index file, if possible, since that would
			// be fastest
			for (int i = binFiles.size() - 1; i >= 0; i--) {
				File fileToCheck = binFiles.get(i);
				File indexFile = binStore.findIndexFile(fileToCheck, true);
				if (indexFile != null) {
					BinaryHeaderAndFooter headAndFoot = binStore.readHeaderAndFooter(indexFile); //
					BinaryFooter footer = headAndFoot.binaryFooter;
					if (footer != null) {
						Long highestUID = footer.getHighestUID();
						if (highestUID != null) {
							maxUID = (long) highestUID;
							break;
						}
					}
				}
				BinaryHeaderAndFooter headAndFoot = binStore.readHeaderAndFooter(fileToCheck); //
				BinaryFooter footer = headAndFoot.binaryFooter;
				if (footer != null) {
					Long highestUID = footer.getHighestUID();
					if (highestUID != null) {
						maxUID = (long) highestUID;
						break;
					}
					if (footer.getFileEndReason() == BinaryFooter.END_CRASHED) {
						String title = "Binary File Corrupted";
						String msg = "PAMGuard is not able to read the footer information from the following binary file:<br><br>"
								+ fileToCheck.getAbsolutePath()
								+ "<br><br>This may happen if PAMGuard was not able to shut down properly the last time it was run.<br>  PAMGuard"
								+ "has attempted to recompile the information directly from the binary file and rebuild the footer, and was able "
								+ "to recover " + footer.getNObjects()
								+ " data objects.  The last object UID recovered is " + footer.getHighestUID()
								+ ".  It is not possible to know whether all of the original data objects have been recovered.<br><br>";
						String help = null;
						int ans = WarnOnce.showWarning(PamController.getMainFrame(),
								title, msg, WarnOnce.WARNING_MESSAGE, help);
					}
				}
			}
		}

		// return the UID
		return maxUID;
	}

	/**
	 * Delete the binary store UID log file
	 */
	public boolean removeLogFile() {
		File logfile = this.getLogFile();
		if (logfile == null) {
			return true;
		}
		return (logfile.delete());
	}

	/**
	 * Writes the module/UID information to the log file
	 * 
	 * @param dataToWrite a List object containing uidTrackerData objects with
	 *                    module/UID info
	 * @return boolean: true=success, false=failure
	 */
	private boolean writeLogDataToXMLFile(List<UIDTrackerData> dataToWrite) {
		Document doc = PamUtils.XMLUtils.createBlankDoc();
		Element root = doc.createElement("PAMGUARD");
		// start with the basic version information about PAMGAURD.
		Element vInfo = doc.createElement("VERSIONINFO");
		root.appendChild(vInfo);
		vInfo.setAttribute("Created", PamCalendar.formatDateTime(System.currentTimeMillis()));
		vInfo.setAttribute("Version", PamguardVersionInfo.version);
		vInfo.setAttribute("Release", PamguardVersionInfo.getReleaseType().toString());
		vInfo.setAttribute("MinAspVersion", "2.35.23");

		Element modules = doc.createElement("MODULES");
		Element moduleData;
		Element moduleName;
		Element lastUID;
		for (UIDTrackerData dataUnit : dataToWrite) {
			moduleData = doc.createElement("MODULE");
			moduleName = doc.createElement("BinFilePrefix");
			moduleName.appendChild(doc.createTextNode(dataUnit.getName()));
			moduleData.appendChild(moduleName);
			lastUID = doc.createElement("lastUID");
			lastUID.appendChild(doc.createTextNode(Long.toString(dataUnit.getUid())));
			moduleData.appendChild(lastUID);
			modules.appendChild(moduleData);
		}
		root.appendChild(modules);
		doc.appendChild(root);

		/**
		 * XML document now created - output it to file.
		 */
//		FileOutputStream fos = null;
		File logfile = this.getLogFile();
//		try {
//			fos = new FileOutputStream(logfile.getAbsolutePath());
//		} catch (FileNotFoundException e1) {
//			System.err.println(e1.getMessage());;
//			return false;
//		}
//		OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
//		of.setIndent(1);
//		of.setLineSeparator("\r\n");
//		of.setIndenting(true);
//		XMLSerializer serializer = new XMLSerializer(fos,of);
//		try {
//			serializer.asDOMSerializer();
//			serializer.serialize( doc.getDocumentElement() );
//			fos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
//			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, null);
//			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "pamguard.dtd");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(domSource, result);
			String asString = writer.toString();
			if (asString != null && logfile != null) {
				BufferedWriter out = new BufferedWriter(new FileWriter(logfile.getAbsolutePath(), false));
				out.write(asString);
				out.close();
			}
		} catch (TransformerException e) {
			System.err.println(e.getMessage());
//			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.err.println(e.getMessage());
//			e.printStackTrace();
			return false;
		}
		return true;
	}

}
