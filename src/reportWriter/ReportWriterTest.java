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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import PamView.PamTable;

/**
 * Class that tests out the report generation
 * 
 * @author mo55
 *
 */
public class ReportWriterTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Report myReport = ReportFactory.createReport("My first report");
		
		ReportSection aSection = new ReportSection("Section 1");
		aSection.addSectionText("This is the body text in Section 1.  And here is a table...");
		Object[][] cellData = { { "1-1", "1-2" }, { "2-1", "2-2" } };
		String[] columnNames = { "col1", "col2" };
		PamTable table = new PamTable(cellData, columnNames);
		aSection.setTable(table);
		myReport.addSection(aSection);

		ReportSection aSection2 = new ReportSection();
		String sec2Text = "This is the body text in Section 2.  This section doesn't have a title, ";
		sec2Text+="but it's got a lot more text.  Unfortunately docx4j doesn't understand inline codes ";
		sec2Text+="for page breaks.";
		aSection2.addSectionText(sec2Text);
		sec2Text = "So in order to do a new line, keep adding separate String objects to the section. ";
		sec2Text+="The String objects are stored in an ArrayList, and when the document is being ";
		sec2Text+="generated a line break will be inserted between each ArrayList entry.";
		aSection2.addSectionText(sec2Text);
		sec2Text = "Note that these are line breaks, and NOT new paragraphs.  New paragraphs usually ";
		sec2Text+="have extra line spacing before or after, and sometimes that's not desirable if ";
		sec2Text+="you just want to list things.  If you want a new paragraph, start a new Section.";
		aSection2.addSectionText(sec2Text);
		myReport.addSection(aSection2);
		
		ReportSection aSection2b = new ReportSection();
		String sec2Textb = "See - this looks like a new paragraph now.  It's defined as a new section with no title.";
		aSection2b.addSectionText(sec2Textb);
		myReport.addSection(aSection2b);
		
		
		ReportSection aSection3 = new ReportSection("Section 3");
		aSection3.addSectionText("This is the body text in Section 3.  Here is an image...");
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("D:\\Work\\OneDrive\\OneDrive - University of St Andrews\\Work\\QAM Module\\Whistle test.jpeg"));
			aSection3.setImage(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		myReport.addSection(aSection3);
		
		ReportSection aSection3b = new ReportSection();
		aSection3b.addSectionText("Here is the image again, but scaled to about half the page width");
		aSection3b.setImage(img);
		aSection3b.setImageWidth(5500);
		myReport.addSection(aSection3b);

		ReportSection aSection4 = new ReportSection();
		aSection4.addSectionText("This is the body text in Section 4.  Again, no section title.  Just checking spacing after images.");
		myReport.addSection(aSection4);

		ReportSection aSection5 = new ReportSection("Section 5");
		aSection5.addSectionText("This is the body text in Section 5");
		myReport.addSection(aSection5);

		ReportSection aSection6 = new ReportSection("Section 6");
		aSection6.addSectionText("This is the body text in Section 6.  Notice that there is no section 5.  Just testing the deleteSection method.  Here is a JavaFX-created chart.");
		myReport.addSection(aSection6);
		
		myReport.deleteSection("Section 5");
		
		// generate a chart
		double[] xVals = new double[20];
		double[] yVals = new double[20];
		for (int i=0; i<xVals.length; i++) {
			xVals[i] = i;
			yVals[i] = i*i;
		}
		ReportChart newChart = new ReportChart(null);
		newChart.addSeries("Series 1", xVals, yVals);
		BufferedImage chartImage = newChart.getImage();
		aSection6.setImage(chartImage);
		
		ReportSection aSection7 = new ReportSection("Section 7");
		aSection7.addSectionText("This is the body text in Section 7.  Here is a JavaFX-created chart with titles, multiple series and legend, and scaled to half width.");
		double[] yVals2 = new double[xVals.length];
		double[] yVals3 = new double[xVals.length];
		for (int i=0; i<xVals.length; i++) {
			yVals2[i] = i;
			yVals3[i] = i*i*i;
		}
		ReportChart newChart2 = new ReportChart("My Second Chart");
		newChart2.addSeries("Squared", xVals, yVals);
		newChart2.addSeries("Linear", xVals, yVals2);
		newChart2.addSeries("Cubed", xVals, yVals3);
		newChart2.setAxisTitles("X-Value", "Y-Value");
		newChart2.setXAxisParams(0, 25, 5);
		newChart2.setYAxisParams(0, 300, 25);
		BufferedImage chartImage2 = newChart2.getImage();
		aSection7.setImage(chartImage2);
		aSection7.setImageWidth(5500);
		myReport.addSection(aSection7);
		
		String filename = "D:\\Work\\OneDrive\\OneDrive - University of St Andrews\\Work\\QAM Module\\myFirstGeneratedReport_docx4jDefaults.docx";
		ReportFactory.convertReportToDocx(myReport, filename);
//		String filename = "myFirstGeneratedReport_templateDefaults.docx";
//		String template =  "C:\\Users\\mo55\\Documents\\Work\\NMMF Project\\NMMFTemplate.docx";
//		ReportFactory.convertReportToDocx(myReport, filename,null);
		
		ReportFactory.openReportInWordProcessor(filename);
	}
}
