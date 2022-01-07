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
import java.util.ArrayList;

import PamView.PamTable;

/**
 * A section in the report.  This is the object that contains all of the information
 * that will eventually go into the docx document.  Each section can have any of the following:
 * <ul>
 * <li>A title</li>
 * <li>Any amount of text</li>
 * <li>A table (PamTable class)</li>
 * <li>A figure (BufferedImage class)</li>
 * </ul>
 * A Report is made up of multiple sections, each one after the other.  See the
 * ReportWriterTest class for an example of how to create a Report with multiple
 * sections.
 * 
 * @author mo55
 *
 */
public class ReportSection {
	
	/** The title of this section - can be null */
	private String sectionTitle = null;
	
	/** 
	 * Heading level, 1, 2, 3, etc.
	 */
	private int headingLevel = 0;
	
	/** The text to include in this section - can be null */
	private ArrayList<String> sectionText = new ArrayList<String>();
	
	/** The table to include at the end of the section text - can be null */
	private PamTable table = null;
	
	private String tableCaption = null;
	
	/** The image to include at the end of the section text - can be null */
	private BufferedImage image = null;
	
	private String imageCaption = null;
	
	/** The width of the image.  If this is 0, the image will be scaled to the width of the page */
	private long imageWidth = 0;

	/**
	 * Constructor with no section title
	 */
	public ReportSection() {
	}


	/**
	 * Constructor with section title at heading level 1
	 * @param sectionTitle
	 */
	public ReportSection(String sectionTitle) {
		this(sectionTitle, 1);
	}
	/**
	 * Constructor with section title
	 * @param sectionTitle
	 * @param heading level, e.g. 1, 2, 3 etc. 
	 */
	public ReportSection(String sectionTitle, int headingLevel) {
		this.sectionTitle = sectionTitle;
		this.headingLevel = headingLevel;
	}

	/**
	 * 
	 * @return the section title
	 */
	public String getSectionTitle() {
		return sectionTitle;
	}
	
	/**
	 * 
	 * @return The heading level (1, 2, 3, etc.)
	 */
	public int getHeadingLevel() {
		return headingLevel;
	}

	/**
	 * 
	 * @param sectionTitle Section title to set
	 */
	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}
	
	/**
	 * 
	 * @return The section text
	 */
	public ArrayList<String> getSectionText() {
		return sectionText;
	}


	/**
	 * Add text to the section. This will appear in the same paragraph. 
	 * @param newText Text to add
	 */
	public void addSectionText(String newText) {
		sectionText.add(newText);
	}

	/**
	 * Clear all text in the sectin
	 */
	public void clearSectionText() {
		sectionText.clear();
	}
	
	/**
	 * Get a table from the section. This will appear AFTER any section text. 
	 * @return A table of data (can be null)
	 */
	public PamTable getTable() {
		return table;
	}

	/**
	 * Set a table with no caption
	 * @param table table
	 */
	public void setTable(PamTable table) {
		setTable(table, null);
	}
	
	/**
	 * Set a table with a caption. The caption will display above the table
	 * @param table Table
	 * @param tableCaption Caption. Table numbers will be added automatically. 
	 */
	public void setTable(PamTable table, String tableCaption) {
		this.table = table;
		this.tableCaption = tableCaption;
	}


	/**
	 * Get an image to add to the document. This will appear after any section 
	 * text
	 * @return an image
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * Get a caption for the image. This will appear below the 
	 * image and automatically be numbered Figure 1, Figure 2, etc. 
	 * @return an image
	 */
	public String getImageCaption() {
		return imageCaption;
	}


	/**
	 * Set an image with no caption
	 * @param image
	 */
	public void setImage(BufferedImage image) {
		setImage(image, null);
	}
	
	/**
	 * Set an image with text for the caption. 
	 * @param image
	 * @param imageCaption
	 */
	public void setImage(BufferedImage image, String imageCaption) {
//		this.image = image;
//		this.imageCaption = imageCaption;
		setImage(image, imageCaption, false);
	}

	/**
	 * Set an image with text for the caption. 
	 * @param image
	 * @param imageCaption
	 */
	public void setImage(BufferedImage image, String imageCaption, boolean trimWhiteSpace) {
		if (trimWhiteSpace) {
			this.image = ReportFactory.trimImage(image);
		} 
		else {
			this.image = image;
		}
		this.imageCaption = imageCaption;
	}


	/**
	 * <p>Get the width of the image.  If the value is 0, the image will automatically be scaled to fit
	 * the width of the page.</p>
	 * <p>The units are twips (twentieth of a point).  For reference,
	 * half the width of an A4 page (210mm/2 = 105mm) is 5953 twips.  Half the width of a 'letter' size
	 * page (8.5"/2 = 4.25") is 6120 twips.</p>
	 * @return the width of the image in twips
	 */
	public long getImageWidth() {
		return imageWidth;
	}

	
	/**
	 * <p>Set the width of the image.  If the value is 0, the image will automatically be scaled to fit
	 * the width of the page.</p>
	 * <p>The units are twips (twentieth of a point).  For reference,
	 * half the width of an A4 page (210mm/2 = 105mm) is 5953 twips.  Half the width of a 'letter' size
	 * page (8.5"/2 = 4.25") is 6120 twips.</p>
	 */
	public void setImageWidth(long imageWidth) {
		this.imageWidth = imageWidth;
	}


	/**
	 * Override the hashCode method to look at the sectionTitle String
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sectionTitle == null) ? 0 : sectionTitle.hashCode());
		return result;
	}

	/**
	 * Override the equals method to look at the sectionTitle String
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportSection other = (ReportSection) obj;
		if (sectionTitle == null) {
			if (other.sectionTitle != null)
				return false;
		} else if (!sectionTitle.equals(other.sectionTitle))
			return false;
		return true;
	}


	/**
	 * @return the tableCaption
	 */
	public String getTableCaption() {
		return tableCaption;
	}


	/**
	 * @param tableCaption the tableCaption to set
	 */
	public void setTableCaption(String tableCaption) {
		this.tableCaption = tableCaption;
	}

}
