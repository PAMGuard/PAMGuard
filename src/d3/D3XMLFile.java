package d3;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import PamUtils.FileParts;


/**
 * Class for unpacking XML metadata files created from Mark Johnson's D3 software
 * 
 * @author Doug Gillespie
 *
 */
public class D3XMLFile {

	private static final String dateFormatString = "yyyy,MM,dd,HH,mm,ss"; //e.g. "2012,4,28,8,41,1"
	SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
	private Document doc;
	private long startTime = Long.MIN_VALUE;
	private long endTime = Long.MIN_VALUE;
	private File xmlFile;
	private int[] sensorList;
	private String fullId;
	private long shortId;

	private D3XMLFile(Document doc, File xmlFile) {
		this.doc = doc;
		this.xmlFile = xmlFile;
		findFileTimes();
		findSensorIds();
	}

	/**
	 * Open an XML file for a corresponding wav file 
	 * by changing the file end to xml and then trying to
	 * find an xml file in the same directory. 
	 * @param file wav file (or valid xml file) 
	 * @return an XML file, from which additional information can then be extracted. 
	 */
	public static D3XMLFile openXMLFile(File file) {
		String wavFileName = file.getAbsolutePath();
		FileParts fp = new FileParts(file);
		String end = fp.getFileEnd();
		if (end == null) {
			return null;
		}
		if (!end.equalsIgnoreCase("wav") && !end.equalsIgnoreCase("xml")) {
			return null;
		}
		String xmlFileName = wavFileName.replace(".wav", ".xml");
		xmlFileName = xmlFileName.replace(".WAV", ".xml");
		//		if (xmlFileName.equals(wavFileName)) {
		//			// no .wav to replace - possibly an AIF file. 
		//			return null;
		//		}
		File xmlFile = new File(xmlFileName);
		if (!xmlFile.exists()) {
			return null;
		}
		/*
		 * Try reading the document.
		 */
		Document doc;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
		} catch (ParserConfigurationException e) {
			System.out.println(String.format("Parser Error in XML file %s: %s", xmlFileName, e.getMessage()));
			return null;
		} catch (SAXException e) {
			System.out.println(String.format("SAX Error in XML file %s: %s", xmlFileName, e.getMessage()));
			return null;
		} catch (IOException e) {
			System.out.println(String.format("IO Error in XML file %s: %s", xmlFileName, e.getMessage()));
			return null;
		}
		doc.getDocumentElement().normalize();
	
		return new D3XMLFile(doc, xmlFile);
	}

	/*
	 * Get the list of sensors used. 
	 */
	public int[] getSensorList() {
		if (sensorList == null) {
			makeSensorList();
		}
		return sensorList;
	}
		
	/**
	 * Extract a list of sensors listed in this xml file. 
	 */
	private void makeSensorList() {
		// TODO Auto-generated method stub
		Element sensEl = findProcCFG("SENSOR");
		if (sensEl == null) {
			return;
		}

		NodeList eList = sensEl.getElementsByTagName("CHANS");
		if (eList.getLength() == 1) {
			Element chanEl = ((Element)eList.item(0));
			String strChan = chanEl.getAttribute("N");
			if (strChan == null) {
				return;
			}
			int n = -1;
			try {
				n = Integer.parseInt(strChan);
			}
			catch (NumberFormatException e) {
				return;
			}
			sensorList = new int[n];
			String strList = chanEl.getTextContent();
			//			System.out.println(strList);
			String[] bits = strList.split(",");
			try {
				for (int i = 0; i < Math.min(bits.length, n); i++) {
					sensorList[i] = Integer.valueOf(bits[i].trim());
				}
			}
			catch (NumberFormatException e) {
				System.err.println("Unable to parse D3 sensor string " + strList);
				sensorList = null;
			}
		}
	}

	/**
	 * Get the start time from the XML file
	 * @param file xml file
	 * @return time in milliseconds or Long.MIN_VALUE if it fails. 
	 */
	public static long getXMLStartTime(File file) {
		D3XMLFile dFile = openXMLFile(file);
		if (dFile == null) {
			return Long.MIN_VALUE;
		}
		return dFile.getStartTime();
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}


	private boolean findSensorIds() {
		NodeList cfgList = doc.getElementsByTagName("DEVID");
		if (cfgList.getLength() == 0) {
			return false;
		}
		Element el = (Element) cfgList.item(0);
		fullId = el.getTextContent();
		if (fullId == null) {
			return false;
		}
		fullId = fullId.trim();
		/* 
		 * also remove blank spaces. Mark in his wizdom has changed
		 * some of the ways things are packed for d4 which was a number
		 * like 701e 1408, but apparently it gets the right result so 
		 * long as I concatonate and read as a long hex. 
		 */
		fullId = fullId.replace(" ", "");
		try {
			String[] idParts = fullId.split(",");
			if (idParts.length <= 1) {
				shortId = Long.parseLong(fullId, 16);
			}
			else {
				String hexId = idParts[2]+idParts[3];
				shortId = Long.parseLong(hexId, 16);
			}
		}
		catch (Exception e) {
			System.out.println("Error unpacking device Id: " + fullId);
			return false;
		}
		return true;
	}

	private Element findProcCFG(String procName) {
		NodeList cfgList = doc.getElementsByTagName("CFG");
		Node aNode;
		for (int i = 0; i < cfgList.getLength(); i++) {
			aNode = cfgList.item(i);
			if (aNode.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) aNode;
				NodeList eList = el.getElementsByTagName("PROC");
				if (eList.getLength() == 1) {
					Element procEl = (Element) eList.item(0);
					String name = procEl.getTextContent();
					if (name != null) {
						name = name.trim();
					}
					if (procName.equals(name)) {
						return el;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Find the start and end times encoded in the XML file
	 * @return true if two times are found. 
	 */
	private boolean findFileTimes() {
		NodeList events = doc.getElementsByTagName("EVENT");
		String eventName;
		for (int i = 0; i < events.getLength(); i++) {
			Node event = events.item(i);
			eventName = "";
			if (event.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) event;
				String dateTime = eElement.getAttribute("TIME");
				Node fc = eElement.getFirstChild();
				if (fc != null) {
					Node sib = fc.getNextSibling();
					if (sib != null) {// && ((Element) sib).getNodeName()))
						eventName = sib.getNodeName();
					}
				}
				if (eventName.equals("START")) {
					startTime = unpackTimeString(dateTime);
				}
				else if (eventName.equals("END")) {
					endTime = unpackTimeString(dateTime);
				}
			}

		}
		return (startTime != Long.MIN_VALUE && endTime != Long.MIN_VALUE);
	}

	/**
	 * Convert the tag date string into something sensible
	 * @param timeString timestring from tag xml file
	 * @return date in milliseconds. 
	 */
	private long unpackTimeString(String timeString) {
		if (timeString == null) {
			return Long.MIN_VALUE;
		}
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date d;
		try {
			d = dateFormat.parse(timeString);
		} catch (ParseException e) {
			System.out.println(String.format("Unable to interpet XML string date %s in %s", 
					timeString, xmlFile.getName()));
			return Long.MIN_VALUE;
		}

		Calendar cl = Calendar.getInstance();
		cl.setTimeZone(TimeZone.getTimeZone("GMT"));
		cl.setTime(d);
		return cl.getTimeInMillis();
	}

	/**
	 * @return the fullId
	 */
	public String getFullId() {
		return fullId;
	}

	/**
	 * @return the shortId
	 */
	public long getShortId() {
		return shortId;
	}
}
