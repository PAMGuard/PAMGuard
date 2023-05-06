package soundtrap;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pamguard.x3.sud.SUDClickDetectorInfo;
import org.pamguard.x3.sud.SUDXMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soundtrap.xml.CDETInfo;
import soundtrap.xml.DWVInfo;
import soundtrap.xml.SoundTrapXMLTools;
import soundtrap.xml.TimeInformation;
import soundtrap.xml.WAVInfo;
import PamUtils.FileParts;
import PamUtils.PamCalendar;
import d3.D3XMLFile;
import d3.SoundTrapTime;

public class STXMLFile {

//	public static final String defaultDateFormat = "dd/MM/yyyy HH:mm:ss";
	public static final String defaultDateFormat = "yyyy-MM-dd'T'HH:mm:ss"; // 2018-03-22 Soundtrap changed to ISO8601 standard
	private File xmlFile;
	private Document doc;
	private NodeList cfgNodes;
	private Node cDetNode;
	private CDETInfo cdetInfo;
	private DWVInfo dwvInfo;
	private WAVInfo wavInfo;
	private String soundTrapId;
	private	String dateFormat = defaultDateFormat;
	private SUDClickDetectorInfo sudDetectorInfo;

	
	public static void main(String[] args) {
		String fn = "D:\\Work\\Bug Fix\\Jennifer 2017-10-02 soundtrap date format\\1677738025.170810001924.log.xml";
		File f = new File(fn);
		STXMLFile.openXMLFile(f,"MM/dd/yyyy hh:mm:ss a");
	}
	
	private STXMLFile(Document doc, File xmlFile) {
		this(doc, xmlFile, defaultDateFormat);
	}

	private STXMLFile(Document doc, File xmlFile, String dateFormat) {
		this.doc = doc;
		this.xmlFile = xmlFile;
		this.dateFormat = dateFormat;
		// extract the sound trap id. The part of the name before the first '.'
		String name = xmlFile.getName();
		int firstDot = name.indexOf('.');
		soundTrapId = name.substring(0, firstDot);
		
		try {
			unpackXMLDoc();
		} catch (Exception e) {
			System.out.printf("Exception unpacking soundtrap xml file %s: %s\n", xmlFile.getName(), e.getMessage());
		}
	}
	
	private void unpackXMLDoc() throws Exception{
		// try to get the new format complete settings as has been developed for the 
		// SUD direct reader. 		
		
		cfgNodes = doc.getElementsByTagName("CFG");
		NodeList procEvents = doc.getElementsByTagName("PROC_EVENT");
		
		cDetNode = SoundTrapXMLTools.findNodeByProc(cfgNodes, "CDET");
		if (cDetNode != null) {
			unpackCDET(cDetNode);
		}
		
		Node dwvNode = SoundTrapXMLTools.findNodeWithChildValue(cfgNodes, "SUFFIX", "dwv");
		if (dwvNode != null) {
			dwvInfo = new DWVInfo(dwvNode);
			dwvInfo.dwvBlockLen = SoundTrapXMLTools.findIntegerChildValue(dwvNode, "BLKLEN");
			dwvInfo.fs = SoundTrapXMLTools.findSourceSampleRate(cfgNodes, dwvNode);
			dwvInfo.setTimeInfo(findTimeInfo(procEvents, dwvInfo.id));
		}

		Node wavNode = SoundTrapXMLTools.findNodeWithChildValue(cfgNodes, "SUFFIX", "wav");
		if (wavNode != null) {
			wavInfo = new WAVInfo(wavNode);
			wavInfo.fs = SoundTrapXMLTools.findSourceSampleRate(cfgNodes, wavNode);
		}
		else {
			return;
		}
		
		// pull out the time information for dwv and wav files.  
//		System.out.println("N proc events = " + procEvents.getLength());
		if (wavInfo != null && procEvents != null) {
			TimeInformation ti = findTimeInfo(procEvents, wavInfo.id);
			wavInfo.setTimeInfo(ti);
		}
		if (wavInfo == null) {
			System.out.printf("WAV Info node unavailable in file %s\n", xmlFile.getName());
		}
		if (procEvents == null) {
			System.out.printf("PROC_EVENT nodes unavailable in file %s\n", xmlFile.getName());
		}
	}

	/**
	 * Find time information for a given id. 
	 * @param procEvents 
	 * @param id
	 * @return
	 */
	private TimeInformation findTimeInfo(NodeList procEvents, Integer id) {
		TimeInformation timeInfo = new TimeInformation();
		Hashtable<String, String> timeHash = new Hashtable<>();
		for (int i = 0; i < procEvents.getLength(); i++) {
			Node procEvent = procEvents.item(i);
			String procid = SoundTrapXMLTools.findAttribute(procEvent, "ID");
			if (procid == null) {
				continue;
			}
			procid = procid.trim();
			int idVal = Integer.valueOf(procid);
			if (idVal != id) {
				continue;
			}
			// each one should have a single attribute in a single child node.
			Node timeNode = SoundTrapXMLTools.findChildNode(procEvent, "WavFileHandler");
			if (timeNode == null) {
				continue;
			}
			NamedNodeMap atts = timeNode.getAttributes();
			Node att = atts.item(0);
			String attName = att.getNodeName();
			String attVal = att.getTextContent();
			timeHash.put(attName.trim(), attVal.trim());
		}
		// now pull out what we want from the hashtable and make a more 
		// sensible structure. 
		String utcStart = timeHash.get("SamplingStartTimeUTC");
		if (utcStart == null) return null;
		timeInfo.samplingStartTimeUTC = stDateToMillis(utcStart);
		String utcStop = timeHash.get("SamplingStopTimeUTC");
		if (utcStop == null) return null;
		timeInfo.samplingStopTimeUTC = stDateToMillis(utcStop);
//		System.out.printf("From %s to %s\n", PamCalendar.formatDBDateTime(timeInfo.samplingStartTimeUTC)
//				, PamCalendar.formatDBDateTime(timeInfo.samplingStopTimeUTC));
		
		return timeInfo;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	private long stDateToMillis(String stDate) {
//		String dateFormat = "dd/MM/yyyy HH:mm:ss";
//		String dateFormat = "MM/dd/yyyy hh:mm:ss a"; for Jennifer Keating 2017/10/03
		DateFormat df = new SimpleDateFormat(dateFormat);
		df.setTimeZone(PamCalendar.defaultTimeZone);
		Date d = null;
		try {
			d = df.parse(stDate);
		}
		catch (java.text.ParseException ex) {
			return 0;
		}
		Calendar cl = Calendar.getInstance();
		cl.setTime(d);
		return cl.getTimeInMillis();
	}
	/**
	 * Unpack the CDET node, extracting standard information on 
	 * detector setup. 
	 * @param node CDET Node from xml
	 * @return 
	 */
	private CDETInfo unpackCDET(Node node) {
		cdetInfo = new CDETInfo(node);
		cdetInfo.cdetThresh = SoundTrapXMLTools.findIntegerChildValue(node, "DETTHR");
		cdetInfo.cdetBlanking = SoundTrapXMLTools.findIntegerChildValue(node, "BLANKING");
		cdetInfo.cdetPredet = SoundTrapXMLTools.findIntegerChildValue(node, "PREDET");
		cdetInfo.cdetPostDet = SoundTrapXMLTools.findIntegerChildValue(node, "POSTDET");
		cdetInfo.cdetSRC = SoundTrapXMLTools.findIntegerChildAttribute(node, "SRC", "ID");
		cdetInfo.cdetLEN = SoundTrapXMLTools.findIntegerChildValue(node, "LEN");
		cdetInfo.fs = SoundTrapXMLTools.findSourceSampleRate(cfgNodes, node);
		return cdetInfo;
	}
	
	public static STXMLFile openXMLFile(File xmlFile) {
		return STXMLFile.openXMLFile(xmlFile, defaultDateFormat);
	}
	
	/**
	 * Open an XML file for a corresponding wav file 
	 * by changing the file end to xml and then trying to
	 * find an xml file in the same directory. 
	 * @param file wav file (or valid xml file) 
	 * @return an XML file, from which additional information can then be extracted. 
	 */
	public static STXMLFile openXMLFile(File xmlFile, String dateTimeFormat) {
		if (xmlFile == null || xmlFile.exists() == false) {
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
			doc = dBuilder.parse(xmlFile); // takes a log time for E:\\STOctober2016\\335839252\\335839252.161031002807.log.xml
		} catch (ParserConfigurationException e) {
			System.out.println(String.format("Parser Error in XML file %s: %s", xmlFile.getAbsoluteFile(), e.getMessage()));
			return null;
		} catch (SAXException e) {
			System.out.println(String.format("SAX Error in XML file %s: %s", xmlFile.getAbsoluteFile(), e.getMessage()));
			return null;
		} catch (IOException e) {
			System.out.println(String.format("IO Error in XML file %s: %s", xmlFile.getAbsoluteFile(), e.getMessage()));
			return null;
		}
		doc.getDocumentElement().normalize();
		
		STXMLFile stXMLFile = new STXMLFile(doc, xmlFile, dateTimeFormat);

		SUDXMLUtils sudXml = new SUDXMLUtils();
		SUDClickDetectorInfo detectorInfo = sudXml.extractDetectorInfo(doc);
		stXMLFile.setSudDetectorInfo(detectorInfo);
	
		return stXMLFile;
	}

	/**
	 * @return the cdetInfo
	 */
	public CDETInfo getCdetInfo() {
		return cdetInfo;
	}

	/**
	 * @return the dwvInfo
	 */
	public DWVInfo getDwvInfo() {
		return dwvInfo;
	}

	/**
	 * @return the wavInfo
	 */
	public WAVInfo getWavInfo() {
		return wavInfo;
	}

	/**
	 * @return the xmlFile
	 */
	public File getXmlFile() {
		return xmlFile;
	}

	/**
	 * @return the soundTrapId
	 */
	public String getSoundTrapId() {
		return soundTrapId;
	}

	private static String[] otherEnd = {".wav", ".0.wav", ".1.wav"};
	private static String[] xmlEnd = {".log.xml"};
	
	/**
	 * Find a sound trap XML file based on a rather too large set of possiblilities
	 * created as John keeps changing his code.
	 * @param otherFileName
	 * @return existing xml file or null. 
	 */
	public static File findXMLFile(File otherFileName) {
		if (otherFileName == null) {
			return null;
		}
		String otherPath = otherFileName.getAbsolutePath();
		for (int iOth = 0; iOth < otherEnd.length; iOth++) {
			int p = otherPath.lastIndexOf(otherEnd[iOth]);
			if (p < 0) {
				continue;
			}
			String otherStub = otherPath.substring(0, p);
			for (int iX = 0; iX < xmlEnd.length; iX++) {
				File xFile = new File(otherStub + xmlEnd[iX]);
				if (xFile.exists()) {
					return xFile;
				}
			}
		}
		return null;
	}

	/**
	 * @return the sudDetectorInfo
	 */
	public SUDClickDetectorInfo getSudDetectorInfo() {
		return sudDetectorInfo;
	}

	/**
	 * @param sudDetectorInfo the sudDetectorInfo to set
	 */
	public void setSudDetectorInfo(SUDClickDetectorInfo sudDetectorInfo) {
		this.sudDetectorInfo = sudDetectorInfo;
	}
}
