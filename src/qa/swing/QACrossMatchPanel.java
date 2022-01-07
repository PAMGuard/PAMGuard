package qa.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamUtils.FrequencyFormat;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.VerticalLabel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import qa.ClusterParameters;
import qa.QAControl;
import qa.QANotifyable;
import qa.QAParameters;
import qa.analyser.QAAnalyser;
import qa.generator.clusters.QACluster;

/**
 * Dialog type panel to match detectors with QA sound types. 
 * @author dg50
 *
 */
public class QACrossMatchPanel implements QANotifyable{

	private QAControl qaControl;

	private JPanel mainPanel;

	private QAAnalyser qaAnalyser;

	private ArrayList<QACluster> clusterList;

	private ArrayList<PamDataBlock> detectorList;

	private JCheckBox[][] boxSelectors;

	private JComboBox<DataComboLabel>[] primaryDetectors;

	private int settingParams = 0;

	public QACrossMatchPanel(QAControl qaControl) {
		this.qaControl = qaControl;
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Reporting Matrix"));

		qaAnalyser = qaControl.getQaAnalyser();
		qaControl.addNotifyable(this);

		createPanel();

	}

	/**
	 * Make the main panel - will have to redo every time a detector is added or removed. 
	 */
	synchronized public void createPanel() {
		mainPanel.removeAll();
		detectorList = qaControl.getQaMonitorProcess().getAllDetectors();
		clusterList = qaControl.getAvailableClusters();

		boxSelectors = new JCheckBox[clusterList.size()][detectorList.size()];
		primaryDetectors = new JComboBox[clusterList.size()];

		int detOffset = 2;
		int clusOffset = 2;
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = detOffset;
		c.gridy = clusOffset-1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.SOUTH;
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.add(BorderLayout.CENTER, new JLabel("<html>Other Detectors ==><p><p></html>", JLabel.RIGHT));
		labelPanel.add(BorderLayout.SOUTH, new JLabel("Primary Detector", JLabel.CENTER));
		//		mainPanel.add(new JLabel("Primary Detector", JLabel.CENTER), c);
		mainPanel.add(labelPanel, c);
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.gridx++;
		for (PamDataBlock dataBlock:detectorList) {
			PamProcess process = dataBlock.getParentProcess();
			//			System.out.printf("Data Block %s has Frequency range %s channels %d\n", dataBlock.getDataName(), 
			//					FrequencyFormat.formatFrequencyRange(dataBlock.getFrequencyRange(), true), dataBlock.getChannelMap());
			VerticalLabel vLabel;
			mainPanel.add(vLabel = new VerticalLabel(dataBlock.getLongDataName(), true), c);
			vLabel.setToolTipText("Frequency Range " + FrequencyFormat.formatFrequencyRange(dataBlock.getFrequencyRange(), true));
			c.gridx++;
		}
		c.gridx = detOffset-1;
		c.gridy = clusOffset;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.BASELINE;
		for (QACluster cluster:clusterList) {
			//			System.out.printf("Cluster %s has frequency range %s\n", cluster.getName(), cluster.getSoundGenerator().getFrequencyRange());
			JLabel label;
			mainPanel.add(label = new JLabel(cluster.getName() + " ", JLabel.RIGHT), c);
			label.setToolTipText("Frequency Range " + FrequencyFormat.formatFrequencyRange(cluster.getSoundGenerator().getFrequencyRange(), true));
			c.gridy++;
		}

		c.gridx = detOffset;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.BASELINE;
		int x0 = c.gridx;
		c.gridy = clusOffset;
		for (int iClus = 0; iClus < clusterList.size(); iClus++) {
			c.gridx = x0;
			mainPanel.add(primaryDetectors[iClus] = new JComboBox<>(), c);
			primaryDetectors[iClus].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getParams();
					setParams();
				}
			});
			c.gridx++;
			for (int iDet = 0; iDet< detectorList.size(); iDet++) {
				PamDataBlock dataBlock = detectorList.get(iDet);
				QACluster cluster = clusterList.get(iClus);
				PamCheckBox cb;
				mainPanel.add(cb = new PamCheckBox(""), c);
				c.gridx++;
				cb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						getParams();
					}
				});
				boxSelectors[iClus][iDet] = cb; 
			}
			c.gridy++;
		}

		fillComboBoxes();
		setParams();
	}

	private class DataComboLabel {
		private PamDataBlock dataBlock;
		/**
		 * @param dataBlock
		 */
		public DataComboLabel(PamDataBlock dataBlock) {
			super();
			this.dataBlock = dataBlock;
		}

		@Override
		public String toString() {
			return dataBlock.getLongDataName();
		}
	}

	synchronized private void fillComboBoxes() {
		if (boxSelectors == null) {
			return;
		}
		settingParams++;
		for (int iClus = 0; iClus < clusterList.size(); iClus++) {
			primaryDetectors[iClus].removeAllItems();
		}
		for (int iDet = 0; iDet< detectorList.size(); iDet++) {
			PamDataBlock dataBlock = detectorList.get(iDet);
			for (int iClus = 0; iClus < clusterList.size(); iClus++) {
				QACluster cluster = clusterList.get(iClus);
				boolean canDetect = qaAnalyser.canDetectCluster(cluster, dataBlock);
				if (canDetect) {
					primaryDetectors[iClus].addItem(new DataComboLabel(dataBlock));
				}
			}
		}
		settingParams--;
	}

	/**
	 * Set params and enable controls. 
	 */
	private void setParams() {
		if (settingParams > 0) {
			return;
		}
		settingParams++;
		QAParameters params = qaControl.getQaParameters();
		for (int iClus = 0; iClus < clusterList.size(); iClus++) {
			QACluster cluster = clusterList.get(iClus);
			ClusterParameters clusterParams = params.getClusterParameters(cluster);
			PamDataBlock primaryDataBlock = qaControl.getClusterPrimaryDetector(cluster);
			if ( qaAnalyser.canDetectCluster(cluster, primaryDataBlock) == false) {
				primaryDataBlock = null;
				clusterParams.primaryDetectionBlock = null;
//				qaControl.setc
			}
			setPrimaryDataBlock(primaryDetectors[iClus], primaryDataBlock);
			for (int iDet = 0; iDet< detectorList.size(); iDet++) {
				PamDataBlock dataBlock = detectorList.get(iDet);
				if (dataBlock == primaryDataBlock) {
					boxSelectors[iClus][iDet].setSelected(true);
					boxSelectors[iClus][iDet].setEnabled(false);
				}
				else {
					boolean canDetect = qaAnalyser.canDetectCluster(cluster, dataBlock);
					boxSelectors[iClus][iDet].setEnabled(canDetect);
					boxSelectors[iClus][iDet].setSelected(clusterParams.isSelectedDetector(dataBlock) & canDetect);
				}
			}
		}		
		settingParams--;
	}
	
	private void getParams() {
		if (clusterList == null || settingParams > 0) {
			return;
		}
		QAParameters params = qaControl.getQaParameters();
		for (int iClus = 0; iClus < clusterList.size(); iClus++) {
			QACluster cluster = clusterList.get(iClus);
			ClusterParameters clusterParams = params.getClusterParameters(cluster);
			DataComboLabel primarySel = (DataComboLabel) primaryDetectors[iClus].getSelectedItem();
			if (primarySel != null) {
				clusterParams.primaryDetectionBlock = primarySel.dataBlock.getLongDataName();
			}
			for (int iDet = 0; iDet< detectorList.size(); iDet++) {
				PamDataBlock dataBlock = detectorList.get(iDet);
				clusterParams.setSelectedDetector(dataBlock, boxSelectors[iClus][iDet].isSelected());
			}
		}
	}
	
	private void setPrimaryDataBlock(JComboBox<DataComboLabel> comboBox, PamDataBlock dataBlock) {
		int n = comboBox.getItemCount();
		for (int i = 0; i < n; i++) {
			DataComboLabel sel = comboBox.getItemAt(i);
			if (sel.dataBlock == dataBlock) {
				comboBox.setSelectedIndex(i);
				break;
			}
		}
	}
	
	public JPanel getPanel() {
		return mainPanel;
	}

	@Override
	public void qaNotify(int noteCode, Object noteObject) {
		switch(noteCode) {
		case PamController.INITIALIZATION_COMPLETE:
		case PamController.ADD_CONTROLLEDUNIT:
		case PamController.REMOVE_CONTROLLEDUNIT:
			getParams();
			createPanel();
			break;
		case PamController.CHANGED_PROCESS_SETTINGS:
			setParams();
		}

	}

}
