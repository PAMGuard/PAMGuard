package Filters;

import java.awt.Frame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;

public class FilterDataSourceDialog extends PamDialog {

	private static FilterDataSourceDialog singleInstance;
	
	private static FilterParameters_2 filterParameters;
	
	PamDataBlock outputDataBlock;
	
	SourcePanel sourcePanel;
	

	private FilterDataSourceDialog(Frame parentFrame) {
		
		super(parentFrame, "Filter Data Source", false);

		sourcePanel = new SourcePanel(this, RawDataUnit.class, true, false);
		
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Raw audio data"));
		p.add(sourcePanel.getPanel());
		setDialogComponent(p);
		
		// changed case of Docs to work in Fatjar 17/8/08
		setHelpPoint("sound_processing.FiltersHelp.Docs.Filters_filters");
	}
	
	public static FilterParameters_2 showDialog(FilterParameters_2 filterParameters, 
			Frame parentFrame, PamDataBlock excludeDataBlock) {
		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new FilterDataSourceDialog(parentFrame);
		}
		FilterDataSourceDialog.filterParameters = filterParameters.clone();
		singleInstance.outputDataBlock = excludeDataBlock;
		singleInstance.showParams();
		singleInstance.setVisible(true);
		return FilterDataSourceDialog.filterParameters;
	}
	
	@Override
	public void cancelButtonPressed() {
		FilterDataSourceDialog.filterParameters = null;		
	}

	private void showParams() {
		PamRawDataBlock rawDataBlock = PamController.getInstance().getRawDataBlock(filterParameters.rawDataSource);
		sourcePanel.clearExcludeList();
		sourcePanel.excludeDataBlock(outputDataBlock, true);
		sourcePanel.setSource(rawDataBlock);
		sourcePanel.setChannelList(filterParameters.channelBitmap);
	}
	
	@Override
	public boolean getParams() {
		PamDataBlock rawSource = sourcePanel.getSource();
		if (rawSource != null) {
			filterParameters.rawDataSource = rawSource.getDataName();
		}
		filterParameters.channelBitmap = sourcePanel.getChannelList();
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
