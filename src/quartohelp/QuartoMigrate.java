package quartohelp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
 * @author dg50
 *
 */
public class QuartoMigrate {

	String helpsrc = "C:\\Users\\dg50\\source\\repos\\PAMGuardPAMGuard\\src\\help";
	String helpdst = "C:\\Users\\dg50\\source\\repos\\PAMGuardPAMGuard\\src\\quartohelp";

	/**
	 * Fu
	 */
	public QuartoMigrate() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		QuartoMigrate qm = new QuartoMigrate();
//		qm.convertSource(new File(qm.helpsrc));
		qm.convertIndex();
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

	String[] imFiles = {".png", ".jpg", ".jpeg", ".bmp"};

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
		System.out.println("Converting to quarto from " + aFile.getAbsolutePath());
		checkDestFolder(aFile);
		String srcName = aFile.getAbsolutePath();
		String dstName = srcName.replace(helpsrc, helpdst);
		dstName = dstName.replace(".html", ".qmd");
		File dstFile = new File(dstName);
		
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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(dstFile));
			writeHeaderMD(br, h2);
			br.write(md);
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	private void writeHeaderMD(BufferedWriter br, String h2) throws IOException {
		br.write("---\r\n");
		if (h2 != null) {
			h2 = h2.trim();
			br.write(String.format("title: \"%s\"\r\n", h2));
		}
		br.write("---\r\n");
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
