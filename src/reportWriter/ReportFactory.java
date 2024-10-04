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



package reportWriter;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

import PamView.PamTable;

/**
 * Static class containing all of the processing necessary to generate a docx report from a Report object.
 * This is basically the interface between PAMGuard and docx4j.  See the ReportWriterTest class for an
 * example of how to create a report
 * 
 * @author mo55
 *
 */
public final class ReportFactory {

	/**
	 * Empty constructor - never used, because it's private and all methods are static
	 */
	private ReportFactory() {
	}

	/**
	 * Create a report, using the passed String as the title.  Default Word template will be used
	 * 
	 * @param reportTitle
	 * @return
	 */
	public static Report createReport(String reportTitle) {
		return (new Report(reportTitle));
	}

	/**
	 * Converts a report to a docx document and saves to the drive.  This uses the default docx4j template and styles
	 * @param report The report to convert
	 * @param outputFilename The filename (including path) to save to.  Note fileName should include the docx extension
	 * 
	 * @return true if successful, false otherwise
	 */
	public static boolean convertReportToDocx(Report report, String outputFilename) {
		return convertReportToDocx(report, outputFilename, getDefaultTemplate());
	}

	private static WordprocessingMLPackage loadTemplateFile(String templateFilename) {
		if (templateFilename == null) {
			return null;
		}
		/**
		 * works with input stream rather than file when the template file is a 
		 * resource packed up in the jar file. 
		 */
		InputStream ins = ReportFactory.class.getResourceAsStream(templateFilename);
		if (ins == null) {
			System.out.printf("Error - unable to find report template file \"%s\"\n", templateFilename);
			return null;
		}
		WordprocessingMLPackage wordPackage;
		try {
//			InputStream is = new input 
			wordPackage = WordprocessingMLPackage.load(ins);
			ins.close();
		} catch (Exception e) {
			System.out.printf("Error - unable to load Word doc template \"%s\"\n", templateFilename);
			System.err.println(e.getMessage());
			return null;
		}
		return wordPackage;

	}

	private static String getDefaultTemplate() {
		String path = "/" + ReportFactory.class.getPackage().getName() + "/templates/PAMRepTemplate.docx";
		return path;
////		String path =  "/templates/PAMRepTemplate.docx";
////		URL template = ClassLoader.getSystemResource(path);
//
//		try {
//			//https://stackoverflow.com/questions/10144210/java-jar-file-use-resource-errors-uri-is-not-hierarchical/10144757
//			URL res = ReportFactory.class.getResource(path);
//			String extl = res.toExternalForm();
////			File f = new File(extl);
////			String extl2 = f.getAbsolutePath();
//			URI uri = res.toURI();
//			if (extl.startsWith("file:/")) {
//				extl = extl.substring(6);
//			}
//			return extl;
////			File theFile = new File(uri);
////			return theFile.getAbsolutePath();
//		} catch (Exception e) {
//			System.out.printf("Unable to create template file resource name\"%s\" ", path);
//			e.printStackTrace();
//		}
//		return null;
	}

	/**
	 * Converts a report to a docx document and saves to the drive
	 * @param report The report to convert
	 * @param outputFilename The filename (including path) to save to.  Note fileName should include the docx extension
	 * @param templateFilename The filename (including path) to use as a template.  Can be null, to use the default.  Note that
	 * a template file can be used to set up the different Styles
	 * 
	 * @return true if successful, false otherwise
	 */
	public static boolean convertReportToDocx(Report report, String outputFilename, String templateFilename) {
		// create the Word package
		WordprocessingMLPackage wordPackage;

		int tableCount = 0;
		int figureCount = 0;

		/**
		 * Try the template file, and if it doesn't exist or can't
		 * be loaded for some reason, we'll get null back, then open 
		 * an empty file instead. 
		 */
//		Debug.out.println("Report using template file " + templateFilename);
		wordPackage = loadTemplateFile(templateFilename);

		if (wordPackage==null) {
			// use docx4j default empty Word file..
			try {
				wordPackage = WordprocessingMLPackage.createPackage();
			} catch (InvalidFormatException e) {
				System.out.println("Report writer Error - unable to create default Word doc: " + e.getMessage());
				return false;
			}
		}

		// set the report title using the Title style
		MainDocumentPart mainDoc = wordPackage.getMainDocumentPart();
		mainDoc.getContent().clear(); // get rid of an annoying cr in the template file. 
		mainDoc.addStyledParagraphOfText("Title", report.getReportTitle());
		ObjectFactory factory = Context.getWmlObjectFactory();

		// loop through the sections one at a time, adding them to the word package
		if (1 > 0) for (ReportSection aSection : report.getAllSections()) {

			// set the section title, using the Heading1 style
			if (aSection.getSectionTitle()!=null) {
				String style = getHeadingStyle(aSection);
				mainDoc.addStyledParagraphOfText(style, aSection.getSectionTitle());
			}

			/*
			 *  add the section text only with a simple linebreak
			 *  between bits of text. 
			 */		
			//			if (!aSection.getSectionText().isEmpty()) {
			//				P paragraph = factory.createP();
			//				R run = factory.createR();
			//				for (int i=0; i<aSection.getSectionText().size(); i++) {
			//					String sectionText = aSection.getSectionText().get(i);
			//					Text text = factory.createText();
			//					text.setValue(sectionText);
			//					run.getContent().add(text);
			//					if (i<(aSection.getSectionText().size()-1)) {
			//						run.getContent().add(getLineBreak());
			//					}
			//				}
			//				paragraph.getContent().add(run);
			//				mainDoc.addObject(paragraph);
			//			}

			// add the section text with full paragraphmark at end of every bit. 
			if (!aSection.getSectionText().isEmpty()) {
				for (int i=0; i<aSection.getSectionText().size(); i++) {
					P paragraph = factory.createP();
					R run = factory.createR();
					String sectionText = aSection.getSectionText().get(i);
					Text text = factory.createText();
					text.setValue(sectionText);
					run.getContent().add(text);
					//					if (i<(aSection.getSectionText().size()-1)) {
					//						run.getContent().add(getLineBreak());
					//					}
					paragraph.getContent().add(run);
					mainDoc.addObject(paragraph);
				}
			}

			// add an image
			if (aSection.getImage()!=null) {
				P newParagraph = processImage(aSection.getImage(), aSection.getImageWidth(), wordPackage);
				if (newParagraph!=null) {
					mainDoc.addObject(newParagraph);
				}
				if (aSection.getImageCaption() != null) {
					figureCount++;
					P paragraph = factory.createP();
					R run = factory.createR();
					Text text = factory.createText();
					text.setValue(String.format("Figure %d. %s", figureCount, aSection.getImageCaption()));
					run.getContent().add(text);
					//					run.getContent().add(getLineBreak());
					paragraph.getContent().add(run);
					mainDoc.addObject(paragraph);
				}
			}

			// add a table
			if (aSection.getTable()!=null) {
				if (aSection.getTableCaption() != null) {
					tableCount++;
					P paragraph = factory.createP();
					R run = factory.createR();
					Text text = factory.createText();
					text.setValue(String.format("Table %d. %s", tableCount, aSection.getTableCaption()));
					run.getContent().add(text);
					//					run.getContent().add(getLineBreak());
					paragraph.getContent().add(run);
					mainDoc.addObject(paragraph);
				}
				Tbl newTable = processTable(aSection.getTable(), wordPackage);
				if (newTable!=null) {
					mainDoc.addObject(newTable);
					//					mainDoc.addObject(getLineBreak());
				}
			}
		}


		// create the file and save
		try {
			File outputFile = new File(outputFilename);
			wordPackage.save(outputFile);
		} catch (Docx4JException e) {
			System.out.println("Error saving document " + outputFilename);
			e.printStackTrace();
			return false;
		}

		// if we got this far, everything worked so return true
		return true;
	}

	private static String getHeadingStyle(ReportSection aSection) {
		int headNo = aSection.getHeadingLevel();
		return String.format("Heading%d", headNo);
	}

	/**
	 * Open file in whatever application the system associates with docx files.
	 *  
	 * @param filename the filename, including path, of the file to open.
	 * @return true is successful, false otherwise
	 */
	public static boolean openReportInWordProcessor(String filename) {
		File fileToOpen = new File(filename);
		if (!fileToOpen.exists()|| !fileToOpen.isFile()) {
			System.out.println("Error occurred trying to open " + filename + ".");
			return false;
		}
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(fileToOpen);
		} catch (IOException e) {
			System.out.println("Error opening docx file in external app");
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public static BufferedImage trimImage(BufferedImage image) {
		return trimImage(image, Color.WHITE, null);
	}
	public static BufferedImage trimImage(BufferedImage image, Color blankColour, Insets insets) {
		if (image == null) {
			return null;
		}
		if (insets == null) {
			insets = new Insets(5,5,5,5);
		}
		int[] blank = {blankColour.getRed(), blankColour.getGreen(), blankColour.getBlue()};
		int width = image.getWidth();
		int height = image.getHeight();
		int minX = width;
		int maxX = 0;
		int minY = height;
		int maxY = 0;
		int[] col = null;//new int[4];
		try {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					col = image.getRaster().getPixel(i, j, col);

					// check if the first 3 indices match the blank array (sometimes col will return with 4 indices) 
					boolean theSame = true;
					for (int k=0;k<blank.length;k++) {
						if (col[k] != blank[k]) {
							theSame=false;
						}
					}
					//				if (Arrays.equals(col, blank) == false) {
					if (!theSame) {
						minX = Math.min(minX,  i);
						maxX = Math.max(maxX, i);
						minY = Math.min(minY, j);
						maxY = Math.max(maxY, j);
					}
				}
			}
			minX = Math.max(0, minX-insets.left);
			maxX = Math.min(width, maxX+insets.right);
			minY = Math.max(0, minY-insets.top);
			maxY = Math.min(height, maxY+insets.bottom);
			int newWid = maxX-minX;
			int newHeight = maxY-minY;
			if (newWid < 0 || newHeight < 0) {
				return null; // blank image
			}
			return image.getSubimage(minX, minY, newWid, newHeight);
		}
		catch (Exception e){
			return image;
		}
	}

	/**
	 * @param imageToConvert
	 * @param imageWidth 
	 * @param wordPackage 
	 * @return
	 */
	private static P processImage(BufferedImage imageToConvert, long imageWidth, WordprocessingMLPackage wordPackage) {
		P p = null;
		imageToConvert = trimImage(imageToConvert);
		if (imageToConvert == null) {
			return null;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(imageToConvert, "png", baos);
			baos.flush();
			byte[] imageByteArray = baos.toByteArray();
			baos.close();
			BinaryPartAbstractImage image = BinaryPartAbstractImage.createImagePart(wordPackage, imageByteArray);
			Inline inline;
			if (imageWidth==0) {
				inline = image.createImageInline(null, null, 0, 1, false);
			} else {
				inline = image.createImageInline(null, null, 0, 1, imageWidth, false);
			}
			ObjectFactory factory = Context.getWmlObjectFactory();
			p = factory.createP();
			R run = factory.createR();
			p.getContent().add(run);
			Drawing drawing = factory.createDrawing();
			run.getContent().add(drawing);
			drawing.getAnchorOrInline().add(inline);
		} catch (Exception e) {
			System.out.println("Error trying to add image to report section");
			e.printStackTrace();
		}
		return p;
	}

	/**
	 * @param table
	 * @param wordPackage
	 * @return
	 */
	private static Tbl processTable(PamTable table, WordprocessingMLPackage wordPackage) {
		int numCols = table.getColumnCount();
		int numRows = table.getRowCount();	// does not include the header row
		int writableWidthTwips = wordPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
		Tbl tblTable = TblFactory.createTable(numRows+1, numCols, writableWidthTwips/numCols);
		ObjectFactory factory = Context.getWmlObjectFactory();
		List<Object> rows = tblTable.getContent();

		for (int row=-1; row<numRows; row++) {	// start at -1, to differentiate the header row
			Tr trRow = (Tr) rows.get(row+1);	// +1 because the rows object includes the header row
			List<Object> cells = trRow.getContent();
			for (int col=0; col<numCols; col++) {
				String cellText;

				// if this is the header row, use a different source for the text
				if (row==-1) {
					cellText = table.getColumnName(col);
				} else {
					cellText = (String) table.getValueAt(row, col);
				}

				// convert the cell text into an object that docx4j understands
				Tc theCell = (Tc) cells.get(col);
				Text text = factory.createText();
				text.setValue(cellText);
				R run = factory.createR();
				//				run.s
				run.getContent().add(text);
				P p = factory.createP();
				p.getContent().add(run);
				theCell.getContent().clear(); // get's rid of a carriage return at start of each cell
				theCell.getContent().add(p);
				//				System.out.println(theCell.toString());
			}
		}

		return tblTable;
	}

	private static Br getLineBreak() {
		ObjectFactory factory = Context.getWmlObjectFactory();
		Br br = factory.createBr();
		return br;
	}

	private static ParaRPr getParagraphBreak() {
		ObjectFactory factory = Context.getWmlObjectFactory();
		return factory.createParaRPr();
	}


}
