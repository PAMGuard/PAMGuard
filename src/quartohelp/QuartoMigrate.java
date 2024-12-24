package quartohelp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.ListIterator;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.OptionsBuilder;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Functions to convert the existing javahelp html files to quarto qmd files
 * and automatically create a help side bar. 
 * <br>To use this, you need to set the source and dest folders, then run main.
 * you then need to copy the text output into the yaml file for the quarto project.  <br>
 * It messes up the table of modules, so you then need to launch PAMGuard in viewer mode with the
 * -smrudev option, export the table and copy the text back into the qmd file. 
 *  
 * @author dg50
 *
 */
public class QuartoMigrate {

	String helpsrc = "C:\\Users\\dg50\\source\\repos\\PAMGuardPAMGuard\\src\\help";
	String helpdst = "C:\\Users\\dg50\\source\\repos\\PAMGuardPAMGuard\\src\\quartohelp";

	String tblStart = "<table";
	String tblEnd = "</table>";
	String hdStart = "<th";
	String hdEnd = "</th>";
	String rowStart = "<tr";
	String rowEnd = "</tr>";
	String dStart = "<td";
	String dEnd = "</td>";
	String vertLine = "jalgfjajgfkda";

	/**
	 * Fu
	 */
	public QuartoMigrate() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		QuartoMigrate qm = new QuartoMigrate();
		qm.convertSource(new File(qm.helpsrc));
//		qm.convertIndex();
	}

	/**
	 * Convert all html files to qmd files. 
	 */
	private void convertSource(File root) {
		File[] files = root.listFiles();
		for (int i = 0; i < files.length; i++) {
			File aFile = files[i];
			String name = aFile.getName();
			if (name.startsWith(".")) {
				continue;
			}
			if (aFile.isDirectory()) {
				convertSource(aFile);
			}			
			else if (name.endsWith(".html")) {
//				convertHTMLFile(aFile);
				convertToMD(aFile);
//				convertToMD2(aFile);
			}
			else if (isImage(aFile)){
				// just copy the file since we'll probably want it ? 
				copyFile(aFile);
			}
		}

	}

	private void copyFile(File aFile) {
		String srcName = aFile.getAbsolutePath();
		String dstName = srcName.replace(helpsrc, helpdst);
		File dstFile = new File(dstName);
		checkDestFolder(dstFile);
		try {
			Files.copy(aFile.toPath(), dstFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	String[] imFiles = {".png", ".jpg", ".jpeg", ".bmp", ".gif"};

	private boolean isImage(File aFile) {
		String fn = aFile.getName().toLowerCase();
		for(int i = 0; i < imFiles.length; i++) {
			if (fn.endsWith(imFiles[i])) {
				return true;
			}
		}
		return false;
	}


	private void convertToMD(File aFile) {
//		System.out.println("Converting to quarto from " + aFile.getAbsolutePath());
		checkDestFolder(aFile);
		String srcName = aFile.getAbsolutePath();
		String dstName = srcName.replace(helpsrc, helpdst);
		dstName = dstName.replace(".html", ".qmd");
		File dstFile = new File(dstName);
//		if (srcName.contains("modules.html")) {
//			System.out.println(srcName);
//		}
		
		String h2 = null;
		ArrayList<String> html = new ArrayList();
		try {
			BufferedReader br = new BufferedReader(  new FileReader(aFile));
			while (true) {
				String aL = br.readLine();
				if (aL == null) {
					break;
				}
				html.add(aL);
			}
			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		h2 = findHeading(html);
		
		CopyDown copydown = new CopyDown();
		OptionsBuilder optionsBuilder = OptionsBuilder.anOptions();
		String md = null;
		try {
			String text = new String(Files.readAllBytes(aFile.toPath()), StandardCharsets.UTF_8);
			if (text.contains("<table")) {
				System.out.println(" table in "  + srcName);
			}
//			else {
//				return;
//			}
//			if (srcName.contains("nNIDAQ.html")) {
//				System.out.println("Pause");
//			}
			text = fixTables(text);
			text = removeHead(text);
			text = text.replace("PAMGUARD", "PAMGuard");
			md = copydown.convert(text);
//			System.out.println(md);
			if (h2 == null) {
				int firstRet = md.indexOf("\n");
				if (firstRet > 0) {
					h2 = md.substring(0, firstRet);
				}
				else {
					// take the file name
					h2 = dstFile.getName();
					h2.replace(".qmd", "");
				}
			}
			md = removeXtraHeadings(md, h2);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// finish fixing the table proble,
		md = md.replace(vertLine, "|");
		md = md.replace("|\r\n", "|");
		try {
			BufferedOutputStream br = new BufferedOutputStream(new FileOutputStream(dstFile));
			writeHeaderMD(br, h2);
			br.write(md.getBytes());
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Try a manual replacement of table text. 
	 * @param text
	 * @return
	 */
	private String fixTables(String text) {
		int ind = 0;
		while (true) {
//			ind = text.indexOf(tblStart, ind);
			int startInd = text.indexOf(tblStart);
			ind = tagEnd(text, tblStart, ind);
			if (ind < 0) {
				return text;
			}
			ind = text.indexOf(tblEnd, ind+1);
			int endInd = ind + tblEnd.length();
			// now find rows within that space. 
			// first try to find a header.
			String tblHtml = text.substring(startInd, endInd);
			String tblText = "\r\n";
			int rInd = 0;
			int nRows = 0;
			while (true) {
//				int hS = text.indexOf(hdStart, rInd);
//				int hE = text.indexOf(hdEnd, rInd);
//				if (hS >= 0) {
//					String row = makeMDRow(text, hS, hE);
//					tblText += row;
//					rInd = hE;
//					continue;
//				}
//				int rS = text.indexOf(rowStart, rInd);
				int rS = tagEnd(tblHtml, rowStart, rInd);
				int rE = tblHtml.indexOf(rowEnd, rInd);
				if (rS >= 0) {
					String row = makeMDRow(tblHtml, rS, rE, nRows++);
					tblText += "<p>" + row ;
					rInd = rE + rowEnd.length();
					continue;
				}
				break;
			}
			String old = text.substring(startInd, endInd);
//			System.out.println("replace " + old);
//			System.out.println("with " + tblText);
			String old1 = text.substring(0, startInd);
			String old2 = text.substring(endInd);
			text = old1 + "<br><p>" + tblText + "<p><br><br><p>" + old2;
//			return tblText;
		}
		
//		return text;
	}
	
	private int tagEnd(String text, String tag, int startInd) {
		int ind = text.indexOf(tag, startInd);
		if (ind < 0) {
			return ind;
		}
		ind = text.indexOf(">", ind+tag.length());
		if (ind < 0) {
			return ind;
		}
		return ind+1;
	}

	/**
	 * Pull out  arow of data from between the hS and hE
	 * @param text
	 * @param hS
	 * @param hE
	 * @param nRows 
	 * @return
	 */
	private String makeMDRow(String text, int hS, int hE, int nRows) {
		text = text.substring(hS, hE);
		if (text.contains("Era designator")) {
			System.out.println("Era designator");
		}
		boolean isHead = (text.indexOf(hdStart) >= 0) || nRows == 0;
		text = text.replace(hdStart, dStart);
		text = text.replace(hdEnd, dEnd);
		int ind = 0;
		String row = vertLine + " ";
		String headRow = vertLine + "";
		while (true) {
//			int s = text.indexOf(dStart, ind);
			int s = tagEnd(text, dStart, ind);
			int e = text.indexOf(dEnd, ind);
			if (s < 0) {
				break;
			}
			String dat = text.substring(s, e);
			// remove any returns in the row .
			dat = dat.replace('\r', ' ');
			dat = dat.replace('\n', ' ');
			dat = removeRowPs(dat);
			row += dat + " " + vertLine + " ";
			headRow += "----" + vertLine;
			ind = e + dEnd.length();
		}
//		row += "<br>";
		if (isHead) {
			row +=  "<p>" + headRow ;
		}
		return row;
	}

	/**
	 * Remove paragraph marks from row data since they screw it. 
	 * @param dat
	 * @return
	 */
	private String removeRowPs(String dat) {
		int ind = 0;
		while (true) {
			int pS = dat.indexOf("<p",0);
			if (pS < 0) {
				break;
			}
			int pE = tagEnd(dat, "<p", 0);
			dat = dat.substring(0, pS) + " " + dat.substring(pE);
		}
		dat = dat.replace("</p>", " ");
		
		return dat;
	}

	/**
	 *  other lib didn't handle tables. Try a DIY approach.  
	 * @param aFile
	 */
	private void convertToMD2(File aFile) {
		// TODO Auto-generated method stub
		System.out.println("Converting file " + aFile.getAbsolutePath());
		checkDestFolder(aFile);
		String srcName = aFile.getAbsolutePath();
		String dstName = srcName.replace(helpsrc, helpdst);
		dstName = dstName.replace(".html", ".qmd");
		File dstFile = new File(dstName);
		if (srcName.contains("FileTimeZone.html")) {
			System.out.println("modules page");
		}
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		dbf.setValidating(false);
////		dbf.setNamespaceAware(true);
//		dbf.setIgnoringComments(true);
//		dbf.setIgnoringElementContentWhitespace(true);
////		dbf.setExpandEntityReferences(false);
//		Document doc = null;
//		try {
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			doc = db.parse(new InputSource(new FileReader(aFile)));
//		} catch (SAXException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return;
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return;
//		}
//		System.out.println(doc);
//		NodeList nodes = doc.getChildNodes();
//		System.out.printf("Nodes in %s is %d\n", aFile.getAbsoluteFile(), nodes.getLength());
		
		String h2 = null;
		ArrayList<String> html = new ArrayList();
		try {
			BufferedReader br = new BufferedReader(  new FileReader(aFile));
			while (true) {
				String aL = br.readLine();
				if (aL == null) {
					break;
				}
				html.add(aL);
			}
			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		h2 = findHeading(html);
		
		
		org.jsoup.nodes.Document doc = null;
		try {
			doc = Jsoup.parse(aFile, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements body = doc.getElementsByTag("body");
		
//		org.jsoup.nodes.Element body = doc.getElementById("content");
		Elements els = body.first().children();
		ListIterator<org.jsoup.nodes.Element> it = els.listIterator();
		String md = "";
		String dblBreak = "\r\n\r\n";
		while (it.hasNext()) {
			org.jsoup.nodes.Element el = it.next();
			Tag tag = el.tag();
			String tagType = tag.getName().toLowerCase();
			String t = el.ownText();
			String h = el.html();
//			System.out.println(t);
//			System.out.println(h);
			switch (tagType) {
			case "html":
			case "body":
			case "#root":
			case "em":
			case "head":
			case "strong":
				continue;
			case "h1":
				md += "\r\n# " + el.ownText() + dblBreak;
				break;
			case "h2":
				md += "\r\n## " + el.ownText() + dblBreak;
				break;
			case "h3":
				md += "\r\n### " + el.ownText() + dblBreak;
				break;
			case "h4":
				md += "\r\n#### " + el.ownText() + dblBreak;
				break;
			case "p":
			case "P":
			case "section":
//				md += el.html() + dblBreak;
				md += t + dblBreak;
				break;
			case "center":
				md += "<center>" + el.ownText() + "<\\center>" + dblBreak;
				break;
			case "table":

				md += "<"+tagType+">" + h + "<\\"+tagType+">" + dblBreak;
				break;
			case "a":
				String href = el.attr("href");
				String name = el.attr("name");
				int nChild = el.childNodes().size();
				String txt = "";
				if (nChild > 0) {
					org.jsoup.nodes.Node nn = el.childNode(0);
					txt = nn.toString();
				}
				if (href != null && href.length() > 0) {
					md += "[" + txt + "](" + href + ")";
				}
				else if (name != null && name.length() > 0) {
					md += "[" + txt + "](#" + name + ")";
				}
				
				break;
			case "br":
				md += "\r\n";
				break;
			case "img":
				Elements srcEl = el.getElementsByAttribute("src");
				String src1 = el.attr("src");
				String width = el.attr("width");
				md += "\r\n![](" + src1 + ")\r\n";
				break;
			default:
				System.out.println("Unknown tag: " + tagType);
				// dump the contents anyway, with the tag.

				md += "<"+tagType+">" + el.ownText() + "<\\"+tagType+">" + dblBreak;
				break;
			}
		}
//		System.out.println(md);
//		try {
//			BufferedWriter br = new BufferedWriter(new FileWriter(dstFile));
//			writeHeaderMD(br, h2);
//			br.write(md);
//			br.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	

	/**
	 * Ending up with multiple repeated heading since Quarto adds the document title 
	 * into the main document at the top. <br>
	 * Remove any heading that is the same text as h2
	 * See how many headings there are before body and remove all but the last. 
	 * @param md
	 * @param h2
	 * @return
	 */
	private String removeXtraHeadings(String md, String h2) {
		String[] lines = md.split("\n");
		boolean[] toSkip = new boolean[lines.length];
		if (h2 != null) {
			// is this split into lines ? 
		}
//		if (md.contains("Configuring the NMEA Data Source")) {
//			System.out.println("Configuring the NMEA Data Source");
//		}
		String lastHeading = null;
		// skip all headings before the body in every file.
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].length() == 0) {
				continue;
			}
			int isTitLine = 0;
			if (lines[i].startsWith("#")) {
				isTitLine = 1; // line is not a heading 
			}
			else {
				// is it old style
				if (i < lines.length-1 && (lines[i+1].startsWith("===") || lines[i+1].startsWith("---"))) {
					isTitLine = 2;
				}
			}
			if (isTitLine == 0) {
				break;
			}
			for (int t = 0; t < isTitLine; t++) {
				toSkip[i+t] = true;
			}
			if (isTitLine > 1) {
				i += isTitLine-1;
			}
		}
		
		String newMD = "";
		for (int i = 0; i < lines.length; i++) {
			if (toSkip[i]) {
				continue;
			}
			newMD += lines[i] + "\r\n"; 
		}
		return newMD;
	}
	

	/**
	 * Remove the xml between <head> and </head>
	 * @param text
	 * @return
	 */
	private String removeHead(String text) {
		int s = text.indexOf("<head>");
		int e = text.indexOf("</head>");
		if (s < 0 || e < 0) {
			return text;
		}
		text = text.replace(text.substring(s, e+7),"");
		return text;
	}

	private void writeHeaderMD(BufferedOutputStream br, String h2) throws IOException {
		br.write(new String("---\r\n").getBytes());
		if (h2 != null) {
			h2 = h2.trim();
			br.write(String.format("title: \"%s\"\r\n", h2).getBytes());
		}
		br.write(new String("---\r\n").getBytes());
	}

	private void convertHTMLFile(File aFile) {
		System.out.println("Converting to quarto from " + aFile.getAbsolutePath());
		checkDestFolder(aFile);
		String srcName = aFile.getAbsolutePath();
		String dstName = srcName.replace(helpsrc, helpdst);
		dstName = dstName.replace(".html", ".qmd");
		File dstFile = new File(dstName);
		/*
		 *   unpack everything in the file, looking in particular for the first heading.
		 *   Later on,may have to add the meta data from the other XML information ?   
		 */
		String h2 = null;
		ArrayList<String> html = new ArrayList();
		try {
			BufferedReader br = new BufferedReader(  new FileReader(aFile));
			while (true) {
				String aL = br.readLine();
				if (aL == null) {
					break;
				}
				html.add(aL);
			}
			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		h2 = findHeading(html);
		// now write it as qmd with appropriate header. 
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(dstFile));
			writeHeader(br, h2);
			for (int i = 0; i < html.size(); i++) {
				if (skipLine(html.get(i))) continue;
				br.write(html.get(i) + "\r\n");
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean skipLine(String aL) {
		if (aL == null) {
			return true;
		}
		if (aL.contains("<!DOCTYPE HTML")) return true;
		if (aL.contains(".css") && aL.contains("STYLESHEET")) return true;
		String strip = aL.trim();
		if (strip.contains("<h1>PAMGUARD</h1>")) return true;// don't want this extra title. 

		return false;
	}
	private String findHeading(ArrayList<String> html) {
		String h = findHeading(html, 2);
		if (h != null) {
			return stipHTML(h);
		}
		h = findHeading(html, 1);
		if (h != null) {
			return stipHTML(h);
		}
		h = findHeading(html, 3);
		if (h != null) {
			return stipHTML(h);
		}
		return null;
	}
	
	/**
	 * Some heading strings have extra crap thatneeds to be stripped out. 
	 * e.g. <h1><a name="_Toc396467139"><span lang="EN-GB">Preparation and expectations</span></a></h1>
	 * @param html
	 * @return
	 */
	private String stipHTML(String html) {
		String str = html;
		while (true) {
			int s = str.indexOf("<");
			if (s < 0) {
				return str;
			}
			int e = str.indexOf(">");
			String rem = str.substring(s, e+1);
			str = str.replace(rem, "");
		}
	}

	private String findHeading(ArrayList<String> html, int headLev) {
		String stStr = String.format("<h%d>", headLev);
		String eStr = String.format("</h%d>", headLev);
		String h2 = null;
		for (String aL : html) {

			if (aL.contains(stStr)) {
				int hs = aL.indexOf(stStr);
				int he = aL.indexOf(eStr);
				try {
					if (he > 0) {
						h2 = aL.substring(hs+4, he);
					}
					else {
						h2 = aL.substring(hs+4);
					}
					return h2;
				}
				catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
		return h2;

	}

	private void writeHeader(BufferedWriter br, String h2) throws IOException {
		br.write("---\r\n");
		if (h2 != null) {
			br.write(String.format("title: \"%s\"\r\n", h2));
		}
		br.write("format: html\r\n");
		br.write("---\r\n");

	}

	private void checkDestFolder(File srcFile) {
		// first check the output folder exists.
		String srcName = srcFile.getAbsolutePath();
		String dstName = srcName.replace(helpsrc, helpdst);
		dstName = dstName.replace(".html", ".qmd");
		File dstFile = new File(dstName);
		File fold = dstFile.getParentFile();
		if (fold.exists() == false) {
			System.out.println("Create folder " + fold.getAbsolutePath());
			fold.mkdirs();
		}

	}

	private void convertIndex() {
		// convert the index file that makes the javahelp index. 
		String tocName = helpsrc + File.separator + "PAMGUARDTOC - Copy.xml";
		System.out.println("Converting " + tocName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();


		// optional, but recommended
		// process XML securely, avoid attacks like XML External Entities (XXE)
		try {
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			// parse XML file
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = (Document) db.parse(new File(tocName));

			// optional, but recommended
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();


			Element el = doc.getDocumentElement();
			NodeList tocs = doc.getChildNodes();
			Element tocEl = (Element) (tocs.item(0));
			tocs = tocEl.getChildNodes();
			makeSpace(4);
			System.out.printf("contents:\n");
			for (int i = 0; i < tocs.getLength(); i++) {
				Node atoc = tocs.item(i);
				if (atoc.getNodeType() == Node.ELEMENT_NODE) {
					parseElement((Element) atoc, 0);
				}
//				if (i == 3) break;
			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseElement(Element el, int level) {

		NodeList tocs = el.getChildNodes();

		int indent = 4 + 4*(level);

		String gname = el.getNodeName();
		String gtext = el.getAttribute("text");
		String gtarget = el.getAttribute("target");

		if (tocs.getLength() == 0) return;
		if (tocs.getLength() > 0) {
			makeSpace(indent);
			System.out.printf("  - section: \"%s\"\n", gtext);
			if (gtarget.length() > 0) {
				gtarget = gtarget.replace(".", "/");
				gtarget += ".qmd";
				makeSpace(indent);
				System.out.printf("    href: %s\n", gtarget);
				replaceqmdtitle(gtarget, gtext);
			}
			makeSpace(indent);
			System.out.printf("    contents:\n");
			
		}
		indent +=4;

		for (int i = 0; i < tocs.getLength(); i++) {

			Node node = tocs.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				Element element = (Element) node;

				// get staff's attribute
				String name = element.getNodeName();
				String text = element.getAttribute("text");
				String target = element.getAttribute("target");

				if (target != null && target.length() > 0) {
					target = target.replace(".", "/");
					target += ".qmd";
					//                System.out.printf("Level %d name \"%s\" text \"%s\" target \"%s\"\n" , level, name, text, target);
					//                if (target.length() == 0) {
					//                makeSpace(indent);
					//                System.out.printf("contents:\n");
//					makeSpace(indent);
//					System.out.printf("  - text: %s\n", text);
//					makeSpace(indent);
//					System.out.printf("    href: %s\n", target);
					makeSpace(indent);
					System.out.printf("  - %s\n", target);
					replaceqmdtitle(target, text);
					
				}
				//                }
				parseElement(element, level+1);
			}
		}
	}

	/**
	 * Change the title in the QMD file to text. This is what 
	 * was taken from the TOC, so good if they all match up. 
	 * @param target
	 * @param text
	 */
	private void replaceqmdtitle(String target, String text) {
		String dst = helpdst + File.separator + target;
		File dstFile = new File(dst);
		if (dstFile.exists() == false) {
			return;
		}
		ArrayList<String> cont = new ArrayList<>();
		// read it all
		int headLines = 0;
		boolean changed = false;
		try {
			BufferedReader br = new BufferedReader(  new FileReader(dstFile));
			while (true) {
				String aL = br.readLine();
				if (aL == null) {
					break;
				}
				if (aL.equals("---")) {
					headLines++;
				}
				if (headLines < 2 && aL.startsWith("title:")) {
					String newTit = "title: \"" + text.trim() + "\"";
					if (aL.equals(newTit) == false) {
						changed = true;
						aL = newTit;
					}
				}
				
				cont.add(aL);
			}
			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (changed == false) return;
		// otherwise rewrite the file. 
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(dstFile));
			for (int i = 0; i < cont.size(); i++) {
				bw.write(cont.get(i) + "\r\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void makeSpace(int indent) {
		for (int i = 0; i < indent; i++) {
			System.out.printf(" ");
		}

	}

}
