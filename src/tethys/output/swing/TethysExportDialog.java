package tethys.output.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;

/**
 * Start of a dialog for controlling the export of Tethys data. For first iteration
 * this will just be a list of output streams (PamDataBlocks) which have a database
 * connection. Each will have a checkbox. On OK it will return back a class listing 
 * what to output and the calling function can do as it will. Future versions will 
 * probably want to push the functionality into a SwingWorker to show progress, etc. 
 * but that can come later. 
 * 
 * Normally, I use single instance dialogs for this sort of thing. 
 * @author dg50
 *
 */
public class TethysExportDialog extends PamDialog {
	
	private static TethysExportDialog singleInstance;
	
	private TethysControl tethysControl;
	
	private TethysExportParams exportParams;
	
	private JPanel streamsPanel;
	
	private ArrayList<DataStreamSet> dataStreamSets = new ArrayList<>();

	private TethysExportDialog(Window parentFrame, TethysControl tethysControl) {
		super(parentFrame, "Tethys Export", false);
		this.tethysControl = tethysControl;
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		/*
		 * Expect to add at least one more panel at the top of this to have options
		 * for things like connection details to the database. If not another panel, 
		 * then they can be arranged on tabs, as a wizard, etc. 
		 */
		streamsPanel = new JPanel();
		streamsPanel.setBorder(new TitledBorder("Data Streams"));
		mainPanel.add(BorderLayout.CENTER, streamsPanel);
		
		setDialogComponent(mainPanel);
		setResizable(true);
		
	}
	
	public static TethysExportParams showDialog(Window parentFrame, TethysControl tethysControl) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.tethysControl != tethysControl) {
			singleInstance = new TethysExportDialog(parentFrame, tethysControl);
		}
		singleInstance.makeStreamsPanel();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.exportParams;
	}
	

	/**
	 * remake the panel. Gets rebuilt whenever dialog opens in case
	 * the list of available data has changed. 
	 */
	private void makeStreamsPanel() {
		streamsPanel.removeAll();
		streamsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		dataStreamSets = findDataStreams();
		streamsPanel.add(new JLabel(" Data Stream ", JLabel.CENTER), c);
		c.gridx++;
		streamsPanel.add(new JLabel(" Data Select ", JLabel.CENTER), c);
		for (DataStreamSet aSet : dataStreamSets) {
			c.gridx = 0;
			c.gridy++;
			streamsPanel.add(aSet.checkBox, c);
			// try to add a data selector
			DataSelector dataSelector = aSet.dataBlock.getDataSelector(tethysControl.getDataSelectName(), false);
			if (dataSelector != null) {
				c.gridx++;
				JButton button = dataSelector.getDialogButton(this);
				if (button != null) {
					streamsPanel.add(button, c);
				}
			}
		}
		pack();
	}
	
	/**
	 * Get a set of data blocks that can provide Tethys data. 
	 * @return datablocks which can provide Tethys data
	 */
	private ArrayList<DataStreamSet> findDataStreams() {
		ArrayList<DataStreamSet> sets = new ArrayList<>();
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aDataBlock : allDataBlocks) {
			if (aDataBlock.getTethysDataProvider() != null) {
				sets.add(new DataStreamSet(aDataBlock));
			}
		}
		return sets;
	}

	private void setParams() {
		this.exportParams = tethysControl.getTethysExportParams();
		if (exportParams == null) {
			exportParams = new TethysExportParams();
		}
		else {
			exportParams = exportParams.clone();
		}
		setParams(exportParams);
	}

	private void setParams(TethysExportParams exportParams) {
		if (exportParams == null || dataStreamSets == null) {
			return;
		}
		for (DataStreamSet streamSet : dataStreamSets) {
			StreamExportParams streamOpts = exportParams.getStreamParams(streamSet.dataBlock);
			if (streamOpts == null) {
				continue;
			}
			streamSet.checkBox.setSelected(streamOpts.selected);
		}
		
	}

	@Override
	public boolean getParams() {
		if (exportParams == null || dataStreamSets == null) {
			return false;
		}
		int nSel = 0;
		for (DataStreamSet streamSet : dataStreamSets) {
			StreamExportParams streamOpts = new StreamExportParams(streamSet.dataBlock.getLongDataName(), streamSet.checkBox.isSelected());
			exportParams.setStreamParams(streamSet.dataBlock, streamOpts);
			nSel++;
		}
		return nSel > 0;
	}

	@Override
	public void cancelButtonPressed() {
		exportParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private class DataStreamSet {
		
		private PamDataBlock dataBlock;
		
		private JCheckBox checkBox;

		public DataStreamSet(PamDataBlock dataBlock) {
			super();
			this.dataBlock = dataBlock;
			checkBox = new JCheckBox(dataBlock.getDataName());
			checkBox.setToolTipText(dataBlock.getLongDataName());
		}
		
		
	}
}
