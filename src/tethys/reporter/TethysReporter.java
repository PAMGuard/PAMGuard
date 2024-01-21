package tethys.reporter;

import java.awt.Window;
import java.util.ArrayList;

import PamController.PamController;
import PamView.dialog.warn.WarnOnce;

/**
 * Set of functions to provide mesage reports on Tethys output. This 
 * will work with the existing WarnOnce type pop-up, the primary purpose
 * of the functions here being to collate information, possibly from 
 * several document writes, before issuing an overall report. 
 * @author dg50
 *
 */
public class TethysReporter {

	private static TethysReporter singleInstance;
	
	private ArrayList<TethysReport> tethysReports;

	private TethysReporter() {
		tethysReports = new ArrayList<TethysReport>();
	}
	
	/**
	 * Get the reporter. 
	 * @return
	 */
	public static final TethysReporter getTethysReporter() {
		if (singleInstance == null) {
			singleInstance = new TethysReporter();
		}
		return singleInstance;
	}
	
	/**
	 * Clear all reports
	 */
	synchronized public void clear() {
		tethysReports.clear();
	}
	
	/**
	 * Add a report after attempting to write a document
	 * @param report
	 */
	synchronized public void addReport(TethysReport report) {
		tethysReports.add(report);
	}
	
	/**
	 * Get the current number of reports
	 * @return number of reports
	 */
	synchronized public int getSize() {
		return tethysReports.size();
	}
	
	/**
	 * Get a summary string of all reported writes using html to separate each ont a separat eline
	 * @return
	 */
	synchronized public String getReportString() {
		if (tethysReports.size() == 0) {
			return "No reports";
		}
		String str = "<html>";
		for (int i = 0; i < tethysReports.size(); i++) {
			TethysReport aReport = tethysReports.get(i);
			String res = aReport.isSuccess() ? "Success" : "Failure";
			if (i > 0) {
				str += "<br>";
			}
		    str += String.format("%s writing %s document %s to Tethys", res, aReport.getCollection().collectionName(), aReport.getDocName());	
		}
		
		
		str += "</html>";
		return str;
	}
	
	/**
	 * Get a count of failed document writes
	 * @return failure count
	 */
	public int countFails() {
		int fails = 0;
		for (TethysReport aReport : tethysReports) {
			if (aReport.isSuccess() == false) {
				fails++;
			}
		}
		return fails;
	}

	/**
	 * Show a report in a popup window
	 * @param clear clear the list of reports afterwards
	 */
	public void showReport(boolean clear) {
		showReport(PamController.getMainFrame(), clear);
	}
	
	/**
	 * Show a report on a popup window
	 * @param window parent frame
	 * @param clear clear the list of reports afterwards
	 */
	public void showReport(Window window, boolean clear) {
		boolean probs = countFails() > 0;
		WarnOnce.showNamedWarning("TethysReporter", window, "Tethys Document Writer", getReportString(), WarnOnce.WARNING_MESSAGE);
		if (clear) {
			clear();
		}
	}
}
