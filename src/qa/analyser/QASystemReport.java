package qa.analyser;

import java.util.ArrayList;

import org.checkerframework.common.util.report.qual.ReportOverride;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.Hydrophone;
import Array.PamArray;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamSettingManager;
import PamUtils.FrequencyFormat;
import PamView.PamTable;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import qa.resource.ReportTextBits;
import reportWriter.Report;
import reportWriter.ReportFactory;
import reportWriter.ReportSection;

public class QASystemReport {

	public QASystemReport() {
		
	}
	
	/**
	 * Write a report section describing the overall system, hydrophone
	 * calibrations, etc for source for a given datablock. 
	 * @param detector
	 * @return
	 */
	public Report makeSystemSection(QAReportOptions reportOptions, PamDataBlock detectorDataBlock) {

		Report report = ReportFactory.createReport("General section");
		PamController pamController = PamController.getInstance();
		ReportSection section = new ReportSection("System configuration information", 1);
		section.addSectionText("Note that this is the configuration at the time the report was generated and may "
				+ "not be the exact configuration used when the tests were conducted.");
		section.addSectionText("Configuration: " + pamController.getPSFName());
		AcquisitionControl daqControl = findAcquisition(detectorDataBlock);
		if (daqControl == null) {
			section.addSectionText("Configuration error: No acqusition system found");
		}
		else {
			section.addSectionText("Acquisition System: " + daqControl.getModuleSummary());
		}
		report.addSection(section);
		report.addSection(makeArraySection(ArrayManager.getArrayManager().getCurrentArray(), daqControl));
		

		Report modsReport = makeModulesSection();
		report.addReport(modsReport);
		
		return report;
	}
	
	/**
	 * 
	 * @return a table of all the modules available in the PAMGuard system ...
	 */
	public Report makeModulesSection() {
		PamController pamController = PamController.getInstance();
		int nUnits = pamController.getNumControlledUnits();
		String[][] tableData = new String[nUnits][4];
		String[] tableCols = {"Module Type", "Module Name", "Frequency Range", "Module Input"};
		for (int i = 0; i < nUnits; i++) {
			PamControlledUnit module = pamController.getControlledUnit(i);
			tableData[i][0] = module.getUnitType();
			tableData[i][1] = module.getUnitName();
			// try to find an input source ....
			PamProcess proc = module.getPamProcess(0);
			if (proc == null) {
				continue;
			}
			double[] fRange = proc.getFrequencyRange();
			if (fRange != null && fRange.length == 2 && fRange[1] > 0) {
				tableData[i][2] = FrequencyFormat.formatFrequencyRange(fRange, true);
			}
			PamDataBlock<PamDataUnit> source = proc.getParentDataBlock();
			if (source != null) {
				tableData[i][3] = source.getDataName();
			}
		}
		PamTable table = new PamTable(tableData, tableCols);
		ReportSection section = new ReportSection("PAMGuard modules", 2);
		section.setTable(table, "Current PAMGuard modules list");
		Report report = new Report("");
		report.addSection(section);
		section = new ReportSection();
		report.addSection(section);
		return report;
	}

	/**
	 * Make system section for one or many acquisitions depending on PAMGaurd configuration. 
	 * @return
	 */
	public Report makeSystemSection(QAReportOptions reportOptions) {
		Report report = ReportFactory.createReport("General section");
		PamController pamController = PamController.getInstance();
		ArrayList<PamControlledUnit> daqSystems = pamController.findControlledUnits(AcquisitionControl.unitType);
		if (daqSystems == null || daqSystems.size() < 1) {
			return makeSystemSection(reportOptions);
		}
		ReportSection section = new ReportSection("System configuration and test information", 1);
		section.addSectionText(String.format("Configuration contains %d acquisition modules", daqSystems.size()));
		for (int i = 0; i < daqSystems.size(); i++) {
			PamControlledUnit daq = daqSystems.get(i);
			Report r = makeSystemSection(reportOptions, daq.getPamProcess(0).getOutputDataBlock(0));
			report.addReport(r);
		}
		
		
		return report;
	}
	
	/**
	 * Describe the hydrophone array in the section
	 * @param section
	 * @param array
	 */
	private ReportSection makeArraySection(PamArray array, AcquisitionControl daqControl) {
		ReportSection section = new ReportSection("Hydrophone Array", 2);
		if (array == null) {
			section.addSectionText("Configuration error: No array configuration found");
			return section;
		}

		section.addSectionText("Array Name: " + array.getArrayName());
		section.addSectionText(ReportTextBits.HYDROPHONETXT);
		int nPhones = array.getHydrophoneCount();
		String tableData[][] = new String[nPhones][6];
		String micro = "\u00B5";
		String[] columnNames = {"Phone", "Type", "Sensitivity (dB re.1V/"+micro+"Pa)", "Gain (dB)", "Clip Level 0-p (dB re.1"+micro+"Pa)", "Position (m - x,y,depth)"};
		ArrayList<Hydrophone> phones = array.getHydrophoneArray();
		for (int i = 0; i < nPhones; i++) {
			Hydrophone phone = phones.get(i);
			tableData[i][0] = Integer.valueOf(i).toString();
			tableData[i][1] = phone.getType();
			tableData[i][2] = String.format("%3.1f", phone.getSensitivity());
			tableData[i][3] = String.format("%3.1f", phone.getPreampGain());
			if (daqControl != null) {
				double clip = daqControl.getAcquisitionProcess().rawAmplitude2dB(1, i, false);
				tableData[i][4] = String.format("%3.1f", clip);
			}
			else {
				tableData[i][4] = "Err";
			}
			double[] coords = phone.getHiddenCoordinates();
			String pos = String.format("%1.1f,%3.1f,%3.1f", coords[0],coords[1], - phone.getZ());
			tableData[i][5] = pos;
		}
		PamTable pamTable = new PamTable(tableData, columnNames);
		section.setTable(pamTable, "Hydrophone array configuration");
		return section;
	}

	/**
	 * Find an acquisition control, ideally the one for the given data block, 
	 * if not, just take anything you can. 
	 * @param detectorDataBlock
	 * @return Acquisition control 
	 */
	private AcquisitionControl findAcquisition(PamDataBlock detectorDataBlock) {
		AcquisitionControl daqControl = null;
		if (detectorDataBlock != null) {
			PamProcess daqProc = detectorDataBlock.getSourceProcess();
			if (daqProc instanceof AcquisitionProcess) {
				daqControl = ((AcquisitionProcess) daqProc).getAcquisitionControl();
			}
		}
		if (daqControl == null) {
			// find anything. 
			daqControl = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.unitType);
		}
		return daqControl;
	}

}
