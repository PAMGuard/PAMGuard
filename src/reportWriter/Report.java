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

import java.util.ArrayList;

/**
 * Top level Report object.  Contains a title and a list of sections.  All methods
 * to organize the Report (e.g. section manipulation) are in this class.
 * 
 * @author mo55
 *
 */
public class Report {
	
	/** Report title */
	private String reportTitle;
	
	/** List of all the sections in the report */
	private ArrayList<ReportSection> sections = new ArrayList<ReportSection>();

	/**
	 * Create a new report
	 */
	public Report(String title) {
		this.reportTitle = title;
	}
	
	/**
	 * Return the report title
	 * 
	 * @return
	 */
	public String getReportTitle() {
		return reportTitle;
	}

	/**
	 * Add a new section to the report
	 * 
	 * @param newSection
	 */
	public void addSection(ReportSection newSection) {
		sections.add(newSection);
	}
	
	public int addSections(ReportSection[] newSections) {
		int added = 0;
		if (newSections == null) {
			return 0;
		}
		for (int i = 0; i < newSections.length; i++) {
			if (newSections[i] != null) {
				added++;
				addSection(newSections[i]);
			}
		}
		return added;
	}

	/**
	 * Delete a section, based on the section title.  The title passed to
	 * this method must exactly match the section title.  Note that this method
	 * will delete the first section it finds with a matching title, including
	 * null.  If multiple sections have the same title, use the method
	 * listOfTitles to get the order and deleteSection(int) instead.
	 * 
	 * @param titleToDelete
	 * @return true if successfully deleted, false if unsuccessful or not found
	 */
	public boolean deleteSection(String titleToDelete) {
		for (ReportSection aSection : sections) {
			if (aSection.getSectionTitle() == titleToDelete) {
				sections.remove(aSection);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Delete a section, based on the index number.  Use the
	 * listOfTitles method first to figure out which index number
	 * you want to get rid of.
	 * 
	 * @param indexToDelete
	 * @return
	 */
	public boolean deleteSection(int indexToDelete) {
		if (indexToDelete<0 || indexToDelete>=sections.size()) {
			return false;
		}
		sections.remove(indexToDelete);
		return true;
	}
	
	/**
	 * Return the number of sections currently in the report
	 * @return
	 */
	public int getNumSections() {
		return sections.size();
	}
	
	/**
	 * get all sections, as an ArrayList
	 * @return
	 */
	public ArrayList<ReportSection> getAllSections() {
		return sections;
	}
	
	/**
	 * Get a section based on the title.  If the section is not found, null is returned
	 * @param titleToGet
	 * @return the section, or null if the section title is not found
	 */
	public ReportSection getSection(String titleToGet) {
		for (ReportSection aSection : sections) {
			if (aSection.getSectionTitle() == titleToGet) {
				return aSection;
			}
		}
		return null;
	}

	/**
	 * Returns a list of the section titles
	 * @return
	 */
	public String[] listOfTitles() {
		String[] listOfTitles = new String[sections.size()];
		for (int i=0; i<sections.size(); i++) {
			listOfTitles[i] = sections.get(i).getSectionTitle();
		}
		return listOfTitles;
	}
	
	/**
	 * Add another report. Basically just concatenate all the sections
	 * <br>The title from the other report will be lost in current version. 
	 * @param otherReport another report (collection of sections). 
	 */
	public void addReport(Report otherReport) {
		if (otherReport == null) {
			return;
		}
		sections.addAll(otherReport.sections);
	}
}
